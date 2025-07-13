import {
  Box, Typography, Button, useTheme, useMediaQuery, Card, CardContent, Stack
} from "@mui/material"
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  IconButton,
  Divider,
  Grid,
} from "@mui/material"
import PersonIcon from "@mui/icons-material/Person"
import EmailIcon from "@mui/icons-material/Email"
import PhoneIcon from "@mui/icons-material/Phone"
import LocationOnIcon from "@mui/icons-material/LocationOn"
import BadgeIcon from "@mui/icons-material/Badge"
import { DataGrid } from "@mui/x-data-grid"
import EditIcon from "@mui/icons-material/Edit"
import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import { useApi } from "../../services/api"
import { useAuth } from "../../services/keycloak"
import { useSnackbar } from "../../contexts/SnackbarContext"

const EmployeeList = ({ refresh }) => {
  const navigate = useNavigate()
  const api = useApi()
  const auth = useAuth()
  const { showSnackbar } = useSnackbar()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"))

  const [employees, setEmployees] = useState([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(5)
  const [loading, setLoading] = useState(false)
  const [erro, setErro] = useState("")
  const [dialogOpen, setDialogOpen] = useState(false)
  const [selectedEmployee, setSelectedEmployee] = useState(null)

  const carregarFuncionarios = async () => {
    setLoading(true)
    try {
      const keycloakId = auth.keycloak?.subject
      if (!keycloakId) {
        showSnackbar("Empresa não identificada no token", "error")
        return
      }

      const data = await api.get(`/customers/company/${keycloakId}`, {
        params: { page, size: pageSize },
      })

      const mapped = data.items.map((item) => ({
        ...item,
      }))
      setEmployees(mapped)
      setTotal(data.total || 0)
    } catch (err) {
      setErro(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    carregarFuncionarios()
  }, [page, pageSize, refresh])

  const handleEdit = (id) => {
    navigate(`/dashboard/funcionarios/editar/${id}`)
  }

  const columns = [
    { field: "id", headerName: "ID", width: 80 },
    { field: "name", headerName: "Nome", width: 200 },
    { field: "cpf", headerName: "CPF", width: 150 },
    { field: "email", headerName: "Email", width: 200 },
    { field: "phone", headerName: "Telefone", width: 150 },
    { field: "enabled", headerName: "Login Ativado", width: 150, valueGetter: (params) => params ? "Sim" : "Não" },
    {
      field: "actions",
      headerName: "Ações",
      width: 100,
      renderCell: (params) => (
        <IconButton onClick={() => handleEdit(params.id)}>
          <EditIcon />
        </IconButton>
      ),
    },
    {
      field: "details",
      headerName: "Detalhes",
      width: 100,
      renderCell: (params) => (
        <IconButton
          onClick={() => {
            setSelectedEmployee(params.row)
            setDialogOpen(true)
          }}
        >
          <PersonIcon />
        </IconButton>
      ),
    },
  ]

  if (erro) return <div>Erro: {erro}</div>

  return (
    <>
      {isMobile ? (
        <>
          <Stack spacing={2}>
            {employees.map((employee) => (
              <Card
                key={employee.id}
                elevation={3}
                onClick={() => {
                  setSelectedEmployee(employee)
                  setDialogOpen(true)
                }}
                sx={{ cursor: "pointer" }}
              >
                <CardContent>
                  <Box display="flex" alignItems="center" gap={1}>
                    <BadgeIcon />
                    <Typography variant="h6">{employee.name}</Typography>
                  </Box>
                  <Divider sx={{ my: 2 }} />
                  <Typography variant="body2">CPF: {employee.cpf}</Typography>
                  <Typography variant="body2">Email: {employee.email}</Typography>
                  <Typography variant="body2">Telefone: {employee.phone}</Typography>
                  <Typography variant="body2">
                    Login Ativado: {employee.enabled ? "Sim" : "Não"}
                  </Typography>
                    <Divider sx={{ my: 2 }} />
                  <Box display="flex" justifyContent="space-between">
                    <Button
                      variant="contained"
                      size="small"
                      startIcon={<EditIcon />}
                      onClick={(e) => {
                        e.stopPropagation()
                        handleEdit(employee.id)
                      }}  
                    >
                      Editar
                    </Button>
                  
                      <Button
                        variant="outlined"
                        size="small"
                        startIcon={<PersonIcon />}
                        onClick={(e) => {
                          e.stopPropagation()
                          setSelectedEmployee(employee)
                          setDialogOpen(true)
                        }}
                      >
                        Detalhes
                      </Button>
                    </Box>
                 
                </CardContent>
              </Card>
            ))}
          </Stack>
          <Stack direction="row" spacing={2} justifyContent="center" mt={2}>
            <Button
              variant="outlined"
              disabled={page === 0}
              onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
            >
              Anterior
            </Button>
            <Typography variant="body2" alignSelf="center">Página {page + 1}</Typography>
            <Button
              variant="outlined"
              disabled={(page + 1) * pageSize >= total}
              onClick={() => setPage((prev) => prev + 1)}
            >
              Próxima
            </Button>
          </Stack>
        </>
      ) : (
        <Box sx={{ height: 500, width: "100%" }}>
          <DataGrid
            rows={employees}
            columns={columns}
            pagination
            page={page}
            pageSize={pageSize}
            rowCount={total}
            paginationMode="server"
            onPageChange={(newPage) => setPage(newPage)}
            onPageSizeChange={(newSize) => {
              setPageSize(newSize)
              setPage(0)
            }}
            rowsPerPageOptions={[5, 10, 20]}
            loading={loading}
            disableSelectionOnClick
            getRowId={(row) => row.id}
          />
        </Box>
      )}

         <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          <Box display="flex" alignItems="center" gap={1}>
            <PersonIcon />
            Detalhes do Funcionário
          </Box>
        </DialogTitle>

        <DialogContent dividers>
          {selectedEmployee ? (
            <Box>
              <Typography variant="h6" gutterBottom>
                Dados Pessoais
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography><strong>Nome:</strong> {selectedEmployee.name}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography><strong>CPF:</strong> {selectedEmployee.cpf}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography><strong>Data de Nascimento:</strong> {selectedEmployee.birthDate || "-"}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography><strong>Ocupação:</strong> {selectedEmployee.occupation || "-"}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography><strong>Salário:</strong> R$ {selectedEmployee.salary?.toFixed(2) || "0.00"}</Typography>
                </Grid>
              </Grid>

              <Divider sx={{ my: 2 }} />

              <Typography variant="h6" gutterBottom>
                Contato
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography><EmailIcon fontSize="small" sx={{ mr: 1 }} /> {selectedEmployee.email || "-"}</Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography><PhoneIcon fontSize="small" sx={{ mr: 1 }} /> {selectedEmployee.phone || "-"}</Typography>
                </Grid>
              </Grid>

              <Divider sx={{ my: 2 }} />

              <Typography variant="h6" gutterBottom>
                Endereço
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography>
                    <LocationOnIcon fontSize="small" sx={{ mr: 1 }} />
                    {selectedEmployee.street}, Nº {selectedEmployee.streetNumber}
                  </Typography>
                </Grid>
                <Grid item xs={12} sm={6}>
                  <Typography><strong>Bairro:</strong> {selectedEmployee.neighborhood}</Typography>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Typography><strong>Cidade:</strong> {selectedEmployee.city}</Typography>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Typography><strong>Estado:</strong> {selectedEmployee.state}</Typography>
                </Grid>
                <Grid item xs={12} sm={4}>
                  <Typography><strong>CEP:</strong> {selectedEmployee.zipCode}</Typography>
                </Grid>
              </Grid>

              <Divider sx={{ my: 2 }} />

              <Typography variant="h6" gutterBottom>
                Informações da Conta
              </Typography>
              <Grid container spacing={2}>
                <Grid item xs={12} sm={6}>
                  <Typography>
                    <strong>Login no sistema:</strong>{" "}
                    {selectedEmployee.enabled ? "Habilitado" : "Desabilitado"}
                  </Typography>
                </Grid>
              </Grid>
            </Box>
          ) : (
            <Typography>Carregando dados...</Typography>
          )}
        </DialogContent>

        <DialogActions>
          {selectedEmployee && (
        // mudando o status do usuário
            <Button
              variant="contained"
              color={selectedEmployee.enabled ? "warning" : "success"}
              onClick={async () => {
                const novoStatus = !selectedEmployee.enabled
                const sucesso = await auth.toggleUserEnabled(selectedEmployee.keycloakId, novoStatus)
                if (sucesso) {
                  showSnackbar(
                    `Usuário ${novoStatus ? "habilitado" : "desabilitado"} com sucesso.`,
                    "success"
                  )
                  setSelectedEmployee((prev) => ({ ...prev, enabled: novoStatus }))
                  carregarFuncionarios() // Recarrega a lista de funcionários
                } else {
                  showSnackbar("Erro ao atualizar status do usuário.", "error")
                }
              }}
            >
              {selectedEmployee.enabled ? "Desabilitar Login" : "Habilitar Login"}
            </Button>
          )}
          <Button onClick={() => setDialogOpen(false)} variant="outlined" color="secondary">
            Fechar
          </Button>
        </DialogActions>
      </Dialog>
    </>
  )
}

export default EmployeeList
