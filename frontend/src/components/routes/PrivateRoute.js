"use client"
import { Navigate, useLocation } from "react-router-dom"
import { useAuth } from "../../services/keycloak"
import Loader from "../common/Loader"

/**
 * Componente de Rota Privada
 * Verifica se o usuário está autenticado e se possui a role necessária.
 * @param {object} props - Propriedades do componente.
 * @param {React.ReactNode} props.children - Componente filho a ser renderizado.
 * @param {string[]} [props.roles] - Array de roles permitidas para acessar a rota.
 */
const PrivateRoute = ({ children, roles }) => {
  const { authenticated, roles: userRoles, isReady } = useAuth()
  const location = useLocation()

  if (!isReady || !authenticated) {
    return <Loader />
  }

  if (!authenticated) {
    // Redireciona para a página de login se não estiver autenticado
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  // Se a rota exige roles específicas, verifica se o usuário as possui
  if (roles && roles.length > 0) {
    const hasRequiredRole = userRoles.some((role) => roles.includes(role))
    if (!hasRequiredRole) {
      // Se não tiver a role, redireciona para uma página de "não autorizado" ou para o dashboard
      // Aqui, redirecionamos para o dashboard principal para simplicidade
      if (userRoles.includes("admin")) {
        return <Navigate to="/dashboard/admin" replace />
      }
      if (userRoles.includes("company")) {
        return <Navigate to="/dashboard/company" replace />
      }
      if (userRoles.includes("client")) {
        return <Navigate to="/dashboard/client" replace />
      }
    }
  }

  return children
}

export default PrivateRoute
