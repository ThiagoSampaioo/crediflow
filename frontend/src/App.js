import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom"
import { ThemeProvider } from "@mui/material/styles"
import CssBaseline from "@mui/material/CssBaseline"
import theme from "./theme"
import PrivateRoute from "./components/routes/PrivateRoute"
import DashboardLayout from "./components/layout/DashboardLayout"

// Páginas
import Login from "./pages/Login"
// Admin
import AdminDashboard from "./pages/admin/AdminDashboard"
import CompanyForm from "./pages/admin/CompanyForm"
import ProposalList from "./pages/admin/ProposalList"
// Empresa
import EmpresaDashboard from "./pages/empresa/EmpresaDashboard"
import EmployeeList from "./pages/empresa/EmployeeList"
import EmployeeForm from "./pages/empresa/EmployeeForm"
// Cliente
import ClienteDashboard from "./pages/cliente/ClienteDashboard"
import LoanForm from "./pages/cliente/LoanForm"
import AccountStatement from "./pages/cliente/AccountStatement"
import PixTransfer from "./pages/cliente/BankTransfer"

function App() {
  
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Router>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route
            path="/"
            element={
              <PrivateRoute>
                <Navigate to="/dashboard" replace />
              </PrivateRoute>
            }
          />

          <Route
            path="/dashboard"
            element={
              <PrivateRoute>
                <DashboardLayout />
              </PrivateRoute>
            }
          >
            {/* Admin */}
            <Route
              path=""
              element={
                <PrivateRoute roles={["admin"]}>
                  <AdminDashboard />
                </PrivateRoute>
              }
            />
            <Route
              path="empresas/nova"
              element={
                <PrivateRoute roles={["admin"]}>
                  <CompanyForm />
                </PrivateRoute>
              }
            />
            <Route
              path="empresas/editar/:id"
              element={
                <PrivateRoute roles={["admin"]}>
                  <CompanyForm />
                </PrivateRoute>
              }
            />
            <Route
              path="propostas"
              element={
                <PrivateRoute roles={["admin"]}>
                  <ProposalList />
                </PrivateRoute>
              }
            />

            {/* Empresa */}
            <Route
              path="company"
              element={
                <PrivateRoute roles={["company"]}>
                  <EmpresaDashboard />
                </PrivateRoute>
              }
            />
            <Route
              path="funcionarios"
              element={
                <PrivateRoute roles={["company"]}>
                  <EmployeeList />
                </PrivateRoute>
              }
            />
            <Route
              path="funcionarios/novo"
              element={
                <PrivateRoute roles={["company"]}>
                  <EmployeeForm />
                </PrivateRoute>
              }
            />
            <Route
              path="funcionarios/editar/:id"
              element={
                <PrivateRoute roles={["company"]}>
                  <EmployeeForm />
                </PrivateRoute>
              }
            />

            {/* Cliente */}
            <Route
              path="client"
              element={
                <PrivateRoute roles={["client"]}>
                  <ClienteDashboard />
                </PrivateRoute>
              }
            />
            <Route
              path="credito"
              element={
                <PrivateRoute roles={["client"]}>
                  <LoanForm />
                </PrivateRoute>
              }
            />
            <Route
              path="extrato"
              element={
                <PrivateRoute roles={["client"]}>
                  <AccountStatement />
                </PrivateRoute>
              }
            />
            <Route
              path="transactions"
              element={
                <PrivateRoute roles={["client"]}>
                  <PixTransfer />
                </PrivateRoute>
              }
            />
          
          
          </Route>

          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </Router>
    </ThemeProvider>
  )
}

export default App
