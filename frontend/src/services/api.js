import axios from "axios"
import { useAuth } from "./keycloak"

const BASE_URL = "http://localhost:8082" // URL do seu backend

const apiRequest = async (endpoint, options = {}, auth) => {
  const { keycloak } = auth
  console.log("token:", keycloak?.token) // Log do token para depuração
  if (!keycloak || !keycloak.token) {
    throw new Error("Usuário não autenticado.")
  }

  const headers = {
    Authorization: `Bearer ${keycloak.token}`,
    "Content-Type": "application/json",
    ...options.headers,
  }


  try {
    const response = await axios({
      url: `${BASE_URL}${endpoint}`,
      method: options.method || "GET",
      data: options.body, // para POST/PUT/PATCH
      headers,
      params: options.params, // caso queira passar query params
    })

    return response.data
  } catch (error) {
    const msg =
      error.response?.data?.message ||
      error.message ||
      `Erro na requisição: ${error.response?.statusText || "Erro desconhecido"}`
    throw new Error(msg)
  }
}

export const useApi = () => {
  const auth = useAuth()

  return {
    get: (endpoint, options = {}) =>
      apiRequest(endpoint, { method: "GET", ...options }, auth),
    post: (endpoint, body, options = {}) =>
      apiRequest(endpoint, { method: "POST", body, ...options }, auth),
    patch: (endpoint, body, options = {}) =>
      apiRequest(endpoint, { method: "PATCH", body, ...options }, auth),
    put: (endpoint, body, options = {}) =>
      apiRequest(endpoint, { method: "PUT", body, ...options }, auth),
    delete: (endpoint, options = {}) =>
      apiRequest(endpoint, { method: "DELETE", ...options }, auth),
  }
}
