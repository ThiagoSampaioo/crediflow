"use client"
import { createContext, useContext, useState, useEffect, useCallback } from "react"
import Keycloak from "keycloak-js"
import axios from "axios"
import WelcomeDialog from "./welcome"

const keycloakConfig = {
  url: "http://localhost:8080",
  realm: "crediflow",
  clientId: "frontend",
}

const keycloak = new Keycloak(keycloakConfig)
const AuthContext = createContext(null)

export const useAuth = () => useContext(AuthContext)

export const KeycloakProvider = ({ children, onLoading }) => {
  const [auth, setAuth] = useState({
    keycloak: null,
    authenticated: false,
    roles: [],
    tenantCode: null,
    userInfo: null,
    isReady: false,
    bankAccount: null,
  })

  const initKeycloak = useCallback(async () => {
    try {
      const authenticated = await keycloak.init({ onLoad: "login-required", checkLoginIframe: false })
      const userInfo = authenticated ? await keycloak.loadUserInfo() : null
      const roles = keycloak.realmAccess?.roles || []
      const tenantCode = userInfo?.attributes?.tenant_code?.[0] || null

      setAuth({ keycloak, authenticated, roles, tenantCode, userInfo, isReady: true })
    } catch (error) {
      console.error("Falha ao inicializar o Keycloak", error)
      setAuth((prev) => ({ ...prev, isReady: true }))
    }
  }, [])

  useEffect(() => {
    initKeycloak()
  }, [initKeycloak])




  useEffect(() => {
    const fetchUserData = async () => {
      try {
        const { keycloak, authenticated, roles } = auth
        if (!keycloak || !authenticated) return

        const keycloakId = keycloak.subject
        const token = keycloak.token
        axios.defaults.headers.common["Authorization"] = `Bearer ${token}`

        let response
        if (roles.includes("client")) {
          response = await axios.get(`http://localhost:8082/customers/keycloak/${keycloakId}`)
        } else if (roles.includes("company")) {
          response = await axios.get(`http://localhost:8082/companies/keycloak/${keycloakId}`)
        } else {
          console.warn("Usuário sem role válida.")
          return
        }

        setAuth((prev) => ({ ...prev, userInfo: response.data }))
      } catch (error) {
        console.error("Erro ao buscar dados do usuário:", error)
      }
    }

    if (auth.authenticated) {
      fetchUserData()
    }
  }, [auth.authenticated, auth.keycloak, auth.roles])

  // Busca conta bancária do cliente
  const fetchBankAccount = async () => {
    try {
      if (!auth.keycloak || !auth.authenticated || !auth.roles.includes("client")) return
      if (!auth?.userInfo?.id) return
      const res = await axios.get(`http://localhost:8082/bank-accounts/by-customer/${auth.userInfo.id}`)
      setAuth((prev) => ({ ...prev, bankAccount: res.data }))
      console.log("Conta bancária do cliente:", res.data)
    } catch (error) {
      console.error("Erro ao buscar conta bancária:", error)
    } finally {
    }
  }
  useEffect(() => {
    fetchBankAccount()
  }, [auth?.userInfo?.id])


  const toggleUserEnabled = async (userId, enabled = true) => {
    try {
      const adminToken = auth.keycloak.token // Precisa ter role de admin ou permissão adequada

      await axios.put(
        `http://localhost:8082/auth/toggle/${userId}?enabled=${enabled}`,
        { enabled },
        {
          headers: {
            Authorization: `Bearer ${adminToken}`,
            "Content-Type": "application/json",
          },
        }
      )

      console.log(`Usuário ${userId} foi ${enabled ? "habilitado" : "desabilitado"} com sucesso.`)
      return true
    } catch (error) {
      console.error("Erro ao alterar status do usuário:", error)
      return false
    }
  }

  if (!auth.isReady) {
    return onLoading || <div>Carregando autenticação...</div>
  }

  console.log("AuthContext:", auth)

  // Inclui a função no context
  return (
    <>
      <AuthContext.Provider value={{ ...auth, toggleUserEnabled, fetchBankAccount }}>
        {children}
      </AuthContext.Provider>
      <WelcomeDialog userInfo={auth.userInfo} />

    </>
  )
}


