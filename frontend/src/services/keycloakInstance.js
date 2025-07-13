import Keycloak from "keycloak-js"

const keycloakConfig = {
  url: "http://localhost:8080",
  realm: "crediflow",
  clientId: "frontend",
}

const keycloak = new Keycloak(keycloakConfig)

// ✅ flag global para impedir múltiplos inits
if (typeof window !== "undefined" && !window.__KEYCLOAK_INITIALIZED__) {
  window.__KEYCLOAK_INITIALIZED__ = false
}

export const initKeycloak = async () => {
  if (typeof window !== "undefined" && window.__KEYCLOAK_INITIALIZED__) {
    return keycloak // já inicializado, retorna
  }

  await keycloak.init({
    onLoad: "login-required",
    checkLoginIframe: false,
  })

  if (typeof window !== "undefined") {
    window.__KEYCLOAK_INITIALIZED__ = true
  }

  return keycloak
}

export default keycloak
