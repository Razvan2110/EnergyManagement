import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authService, usersService } from '../services'
import './Register.css'

export default function Register() {
  const navigate = useNavigate()
  const [form, setForm] = useState({ username: '', password: '', email: '', firstName: '', lastName: '' })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const handleChange = e => setForm({ ...form, [e.target.name]: e.target.value })

  const handleSubmit = async e => {
    e.preventDefault()
    setError('')
    setSuccess(false)
    setLoading(true)
    try {
      const authResp = await authService.register({ username: form.username, password: form.password, email: form.email })
      if (!authResp || !authResp.id) throw new Error('Auth registration failed')
      await usersService.createUser({
        authId: authResp.id,
        username: form.username,
        email: form.email,
        firstName: form.firstName,
        lastName: form.lastName
      })
      setSuccess(true)
      setTimeout(() => navigate('/login'), 1500)
    } catch (err) {
      setError(err.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="register-container">
      <form className="register-form" onSubmit={handleSubmit}>
        <h2>Create Account</h2>
        <input name="username" placeholder="Username" value={form.username} onChange={handleChange} required />
        <input name="email" placeholder="Email" value={form.email} onChange={handleChange} required type="email" />
        <input name="firstName" placeholder="First Name" value={form.firstName} onChange={handleChange} />
        <input name="lastName" placeholder="Last Name" value={form.lastName} onChange={handleChange} />
        <input name="password" placeholder="Password" value={form.password} onChange={handleChange} required type="password" />
        <button type="submit" disabled={loading}>{loading ? 'Registering...' : 'Register'}</button>
        {error && <p className="error-msg">{error}</p>}
        {success && <p className="success-msg">Registration successful!</p>}
      </form>
    </div>
  )
}
