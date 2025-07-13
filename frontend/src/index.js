import React from "react"
import ReactDOM from "react-dom/client"
import App from "./App"
import { KeycloakProvider } from "./services/keycloak"
import { SnackbarProvider } from "./contexts/SnackbarContext"
import Loader from "./components/common/Loader"

const root = ReactDOM.createRoot(document.getElementById("root"))

root.render(

    <KeycloakProvider onLoading={<Loader />}>
      <SnackbarProvider>
        <App />
      </SnackbarProvider>
    </KeycloakProvider>

)
