import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import authService from "../services/authService"
import userService from "../services/usersService"
import deviceService from "../services/devicesService"
import monitoringService from "../services/monitoringService"
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, BarChart, Bar } from 'recharts';
import "./Dashboard.css"

export default function Dashboard() {
  const [user, setUser] = useState({ username: "", role: "" })
  const [users, setUsers] = useState([])
  const [devices, setDevices] = useState([])
  const [loading, setLoading] = useState(true)
  const [showUserForm, setShowUserForm] = useState(false)
  const [showDeviceForm, setShowDeviceForm] = useState(false)

  const [showChartModal, setShowChartModal] = useState(false)
  const [selectedDevice, setSelectedDevice] = useState(null)
  const [consumptionDate, setConsumptionDate] = useState(new Date().toISOString().split('T')[0])
  const [chartData, setChartData] = useState([])
  const [chartType, setChartType] = useState("bar")

  const [formData, setFormData] = useState({})
  const [formType, setFormType] = useState("")
  const [editId, setEditId] = useState(null)
  const navigate = useNavigate()

  const loadData = async () => {
    const token = localStorage.getItem("jwt")
    if (!token) {
      console.warn("⚠️ No token found → redirecting to login")
      return navigate("/login")
    }

    try {
      console.log("🔹 Starting loadData()... validating token")
      const validation = await authService.validate(token)
      console.log("✅ Validation response:", validation)

      if (!validation.valid) throw new Error("Invalid token")

      const normalizedRole = validation.role?.replace("ROLE_", "") || validation.role
      console.log("🧩 Normalized role:", normalizedRole)

      // Store globally for other components
      localStorage.setItem("role", normalizedRole)
      if (validation.id) localStorage.setItem("userId", validation.id)

      setUser({ username: validation.username, role: normalizedRole })

      if (normalizedRole === "ADMIN") {
        console.log("👑 Admin detected → fetching all users + devices")
        const [usersData, devicesData] = await Promise.all([
          userService.getAllUsers(),
          deviceService.getAllDevices(),
        ])
        console.log("✅ Admin data loaded", { usersData, devicesData })
        setUsers(usersData)
        setDevices(devicesData)
      } else if (normalizedRole === "USER") {
        console.log("🙍 Regular USER detected → loading own devices")
        // No longer fetching all users, using ID from validation
        const userId = validation.id;

        if (!userId) {
          console.warn("⚠️ No user ID found in validation response! Redirecting to login.")
          throw new Error("User ID missing")
        }

        const userDevices = await deviceService.getDevicesByUserId(userId)
        console.log("✅ User devices loaded:", userDevices)
        setDevices(userDevices)
      }
    } catch (err) {
      console.error("❌ loadData() failed:", err)
      localStorage.removeItem("jwt")
      navigate("/login")
    } finally {
      console.log("🔚 loadData() finished")
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const handleLogout = () => {
    localStorage.removeItem("jwt")
    localStorage.removeItem("username")
    navigate("/login")
  }

  // Consumption Logic
  const handleViewConsumption = (device) => {
    setSelectedDevice(device)
    setConsumptionDate(new Date().toISOString().split('T')[0])
    setShowChartModal(true)
    // Fetch initial data for today
    fetchConsumptionData(device.id, new Date().toISOString().split('T')[0])
  }

  const fetchConsumptionData = async (deviceId, date) => {
    try {
      const data = await monitoringService.getHourlyConsumption(deviceId, date)
      // Transform data for chart if necessary
      // Expecting list of { timestamp, deviceId, totalConsumption }
      const formattedData = data.map(item => ({
        hour: new Date(item.timestamp).getHours() + ":00",
        consumption: item.totalConsumption
      })).sort((a, b) => parseInt(a.hour) - parseInt(b.hour));

      setChartData(formattedData)
    } catch (err) {
      console.error("Failed to fetch consumption data", err)
      setChartData([])
    }
  }

  const handleDateChange = (e) => {
    const newDate = e.target.value
    setConsumptionDate(newDate)
    if (selectedDevice) {
      fetchConsumptionData(selectedDevice.id, newDate)
    }
  }


  // CRUD Handlers
  const handleAdd = (type) => {
    setFormType(type)
    setEditId(null)
    setFormData({})
    type === "user" ? setShowUserForm(true) : setShowDeviceForm(true)
  }

  const handleEdit = (type, item) => {
    setFormType(type)
    setEditId(item.id)
    setFormData(item)
    type === "user" ? setShowUserForm(true) : setShowDeviceForm(true)
  }

  const handleDelete = async (type, id) => {
    if (!window.confirm("Are you sure you want to delete this item?")) return

    if (type === "user") {
      await deviceService.deleteDevicesByUserId(id)
      await userService.deleteUser(id)
    } else {
      await deviceService.deleteDevice(id)
    }

    await loadData()
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (formType === "user") {
        if (editId) {
          await userService.updateUser(editId, {
            name: formData.name,
            email: formData.email,
            phone: formData.phone,
            role: formData.role,
          })
        } else {
          try {
            await authService.register({
              username: formData.username,
              password: formData.password,
              role: formData.role,
            })
          } catch (err) {
            if (err.message.includes("Duplicate") || err.message.includes("409")) {
              alert("Username already exists! Please choose another one.")
              return
            } else {
              throw err
            }
          }

          await userService.createUser({
            name: formData.name,
            email: formData.email,
            phone: formData.phone,
            role: formData.role,
          })
        }
        setShowUserForm(false)
      } else {
        // Device CRUD including power
        const devicePayload = {
          name: formData.name,
          power: parseFloat(formData.power),
          userId: formData.userId ? parseInt(formData.userId) : null,
        }

        if (editId) await deviceService.updateDevice(editId, devicePayload)
        else await deviceService.createDevice(devicePayload)

        setShowDeviceForm(false)
      }

      await loadData()
    } catch (err) {
      alert("Error saving: " + err.message)
    }
  }

  if (loading) return <div className="dashboard-loading">Loading...</div>

  return (
    <div className="dashboard-container">
      <div className="dashboard-card">
        <h1>Welcome, {user.username}</h1>
        <p>Role: {user.role}</p>

        {/* === ADMIN DASHBOARD === */}
        {user.role === "ADMIN" && (
          <>
            {/* USERS SECTION */}
            <section className="crud-section">
              <h2>Users</h2>
              <button className="add-btn" onClick={() => handleAdd("user")}>
                + Add User
              </button>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Role</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {users.map((u) => (
                    <tr key={u.id}>
                      <td>{u.id}</td>
                      <td>{u.name}</td>
                      <td>{u.email}</td>
                      <td>{u.phone}</td>
                      <td>{u.role}</td>
                      <td>
                        <button onClick={() => handleEdit("user", u)}>Edit</button>
                        <button onClick={() => handleDelete("user", u.id)}>Delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>

            {/* DEVICES SECTION */}
            <section className="crud-section">
              <h2>Devices</h2>
              <button className="add-btn" onClick={() => handleAdd("device")}>
                + Add Device
              </button>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Power (W)</th>
                    <th>User ID</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {devices.map((d) => (
                    <tr key={d.id}>
                      <td>{d.id}</td>
                      <td>{d.name}</td>
                      <td>{d.power ?? "N/A"}</td>
                      <td>{d.userId || "Unassigned"}</td>
                      <td>
                        <button onClick={() => handleEdit("device", d)}>Edit</button>
                        <button onClick={() => handleDelete("device", d.id)}>Delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </section>
          </>
        )}

        {/* === USER DASHBOARD === */}
        {user.role === "USER" && (
          <section className="crud-section">
            <h2>Your Devices</h2>
            <table className="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Power (W)</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {devices.length > 0 ? (
                  devices.map((d) => (
                    <tr key={d.id}>
                      <td>{d.id}</td>
                      <td>{d.name}</td>
                      <td>{d.power ?? "Unknown"}</td>
                      <td>
                        <button onClick={() => handleViewConsumption(d)}>View Consumption</button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="4">No devices assigned to you yet.</td>
                  </tr>
                )}
              </tbody>
            </table>
          </section>
        )}

        {/* === FORM MODAL === */}
        {(showUserForm || showDeviceForm) && (
          <div className="modal">
            <form className="modal-form" onSubmit={handleSubmit}>
              <h3>
                {editId ? "Edit" : "Add"} {formType === "user" ? "User" : "Device"}
              </h3>

              {formType === "user" ? (
                <>
                  {!editId && (
                    <>
                      <input
                        placeholder="Username"
                        value={formData.username || ""}
                        onChange={(e) =>
                          setFormData({ ...formData, username: e.target.value })
                        }
                        required
                      />
                      <input
                        type="password"
                        placeholder="Password"
                        value={formData.password || ""}
                        onChange={(e) =>
                          setFormData({ ...formData, password: e.target.value })
                        }
                        required
                      />
                    </>
                  )}
                  <input
                    placeholder="Full Name"
                    value={formData.name || ""}
                    onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                    required
                  />
                  <input
                    placeholder="Email"
                    type="email"
                    value={formData.email || ""}
                    onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                    required
                  />
                  <input
                    placeholder="Phone"
                    value={formData.phone || ""}
                    onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                    required
                  />
                  <select
                    value={formData.role || "USER"}
                    onChange={(e) =>
                      setFormData({ ...formData, role: e.target.value })
                    }
                  >
                    <option value="USER">USER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </>
              ) : (
                <>
                  <input
                    placeholder="Device Name"
                    value={formData.name || ""}
                    onChange={(e) =>
                      setFormData({ ...formData, name: e.target.value })
                    }
                    required
                  />
                  <input
                    type="number"
                    placeholder="Power (W)"
                    value={formData.power || ""}
                    onChange={(e) =>
                      setFormData({ ...formData, power: e.target.value })
                    }
                    required
                  />
                  <input
                    placeholder="User ID (optional)"
                    value={formData.userId || ""}
                    onChange={(e) =>
                      setFormData({ ...formData, userId: e.target.value })
                    }
                  />
                </>
              )}

              <div className="modal-buttons">
                <button type="submit">Save</button>
                <button
                  type="button"
                  onClick={() => {
                    setShowUserForm(false)
                    setShowDeviceForm(false)
                  }}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}

        {/* === CONSUMPTION CHART MODAL === */}
        {showChartModal && (
          <div className="modal">
            <div className="modal-content" style={{ backgroundColor: 'white', padding: '20px', borderRadius: '8px', width: '80%', maxWidth: '800px' }}>
              <h3>Hourly Consumption for {selectedDevice?.name}</h3>

              <div style={{ marginBottom: '20px', display: 'flex', gap: '20px', alignItems: 'center' }}>
                <div>
                  <label style={{ marginRight: '10px' }}>Select Date: </label>
                  <input
                    type="date"
                    value={consumptionDate}
                    onChange={handleDateChange}
                  />
                </div>
                <div>
                  <label style={{ marginRight: '10px' }}>Chart Type: </label>
                  <select
                    value={chartType}
                    onChange={(e) => setChartType(e.target.value)}
                    style={{ padding: '5px' }}
                  >
                    <option value="bar">Bar Chart</option>
                    <option value="line">Line Chart</option>
                  </select>
                </div>
              </div>

              <div style={{ paddingBottom: '20px', display: 'flex', justifyContent: 'center' }}>
                {chartType === "bar" ? (
                  <BarChart width={600} height={300} data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="hour" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="consumption" fill="#8884d8" name="Energy (kWh)" />
                  </BarChart>
                ) : (
                  <LineChart width={600} height={300} data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="hour" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line
                      type="monotone"
                      dataKey="consumption"
                      stroke="#8884d8"
                      strokeWidth={2}
                      name="Energy (kWh)"
                      activeDot={{ r: 8 }}
                    />
                  </LineChart>
                )}
              </div>

              <button onClick={() => setShowChartModal(false)} style={{ marginTop: '20px' }}>Close</button>
            </div>
          </div>
        )}

        <button onClick={handleLogout} className="logout-btn">
          Logout
        </button>
      </div>
    </div>
  )
}
