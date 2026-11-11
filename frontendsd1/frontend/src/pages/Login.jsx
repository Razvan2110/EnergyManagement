import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authService, attachTokenToAll } from '../services'
import './Login.css'

export default function Login() {
  const [form, setForm] = useState({ username: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async e => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const token = await authService.login(form)
      if (!token) throw new Error('Invalid credentials')

      // Validate immediately to get Role and ID
      const validation = await authService.validate(token)
      const normalizedRole = validation.role?.replace("ROLE_", "") || validation.role

      localStorage.setItem('jwt', token)
      localStorage.setItem('username', form.username)
      localStorage.setItem('role', normalizedRole)
      if (validation.id) localStorage.setItem('userId', validation.id)

      attachTokenToAll(token)
      navigate('/dashboard')
    } catch (err) {
      setError(err.message || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-container">
      <form className="login-form" onSubmit={handleSubmit}>
        <h2>Login</h2>
        <input
          name="username"
          placeholder="Username"
          value={form.username}
          onChange={handleChange}
          required
        />
        <input
          name="password"
          type="password"
          placeholder="Password"
          value={form.password}
          onChange={handleChange}
          required
        />
        <button type="submit" disabled={loading}>
          {loading ? 'Logging in...' : 'Login'}
        </button>
        {error && <p className="error-msg">{error}</p>}
      </form>
    </div>
  )
}
