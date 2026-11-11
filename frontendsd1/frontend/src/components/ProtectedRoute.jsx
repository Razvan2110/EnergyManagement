import { useEffect, useState } from "react"
import { Navigate } from "react-router-dom"
import { authService } from "../services"

export default function ProtectedRoute({ children, requiredRole }) {
  const [isValid, setIsValid] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem("jwt")
    if (!token) {
      setIsValid(false)
      setLoading(false)
      return
    }

    const checkToken = async () => {
      try {
        const result = await authService.validate(token)
        if (result.valid) {
         
          if (
            !requiredRole ||
            (Array.isArray(requiredRole)
              ? requiredRole.includes(result.role.replace("ROLE_", ""))
              : result.role.replace("ROLE_", "") === requiredRole)
          ) {
            setIsValid(true)
          } else {
            setIsValid(false)
          }
        } else {
          setIsValid(false)
        }
      } catch (err) {
        console.error("ProtectedRoute validation failed:", err)
        setIsValid(false)
      } finally {
        setLoading(false)
      }
    }

    checkToken()
  }, [requiredRole])

  if (loading) return <div>Loading...</div>
  if (!isValid) return <Navigate to="/login" replace />

  return children
}
