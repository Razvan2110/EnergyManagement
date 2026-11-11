import { BrowserRouter as Router, Routes, Route } from "react-router-dom"
import Login from "./pages/Login"
import Register from "./pages/Register"
import Dashboard from "./pages/Dashboard"
import ProtectedRoute from "./components/ProtectedRoute"

import NotificationComponent from "./components/NotificationComponent"
import ChatComponent from "./components/ChatComponent"

import AdminChatComponent from "./components/AdminChatComponent"

import { useLocation } from "react-router-dom"

function Layout() {
  const location = useLocation()
  // Check if username exists (indicates logged in)
  const isAuth = !!localStorage.getItem("username")

  return (
    <>
      {isAuth && <NotificationComponent />}
      {isAuth && <ChatComponent />}
      {isAuth && <AdminChatComponent />}
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* 🔒 Admin-only route */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute requiredRole={["ADMIN", "USER"]}>
              <Dashboard />
            </ProtectedRoute>

          }
        />

        <Route path="*" element={<Login />} />
      </Routes>
    </>
  )
}

export default function App() {
  return (
    <Router>
      <Layout />
    </Router>
  )
}
