import { useEffect, useState } from "react"
import {
  Box,
  IconButton,
  Card,
  CardContent,
  Typography,
  Stack,
  Button,
  useTheme,
  useMediaQuery,
  CircularProgress,
} from "@mui/material"
import EditIcon from "@mui/icons-material/Edit"
import { DataGrid } from "@mui/x-data-grid"
import { useNavigate } from "react-router-dom"
import { useApi } from "../../services/api"

const CompanyList = ({ refresh }) => {
  const navigate = useNavigate()
  const api = useApi()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"))

  const [empresas, setEmpresas] = useState([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(5)
  const [loading, setLoading] = useState(false)
  const [erro, setErro] = useState("")

  const carregarEmpresas = async () => {
    setLoading(true)
    try {
      const data = await api.get(`/companies`, {
        params: { page, size: pageSize },
      })
      setEmpresas(data.items || [])
      setTotal(data.total || 0)
    } catch (err) {
      setErro(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    carregarEmpresas()
  }, [page, pageSize, refresh])

  const handleEdit = (id) => {
    navigate(`/dashboard/empresas/editar/${id}`)
  }

  const columns = [
    { field: "id", headerName: "ID", width: 70 },
    { field: "name", headerName: "Nome da Empresa", width: 200 },
    { field: "cnpj", headerName: "CNPJ", width: 150 },
    { field: "email", headerName: "E-mail", width: 200 },
    { field: "phone", headerName: "Telefone", width: 150 },
    { field: "responsible", headerName: "Responsável", width: 180 },
    { field: "type", headerName: "Tipo", width: 100 },
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
  ]

  if (erro) return <div>Erro: {erro}</div>

  return isMobile ? (
    <Box>
      <Typography variant="h6" gutterBottom>Empresas Cadastradas</Typography>
      {loading ? (
        <Box display="flex" justifyContent="center" py={4}><CircularProgress /></Box>
      ) : (
        <Stack spacing={2}>
          {empresas.map((empresa) => (
            <Card key={empresa.id} elevation={3} sx={{ p: 2 }}>
              <CardContent>
                <Typography variant="h6">{empresa.name}</Typography>
                <Typography variant="body2">CNPJ: {empresa.cnpj}</Typography>
                <Typography variant="body2">Email: {empresa.email}</Typography>
                <Typography variant="body2">Telefone: {empresa.phone}</Typography>
                <Typography variant="body2">Responsável: {empresa.responsible}</Typography>
                <Typography variant="body2">Tipo: {empresa.type}</Typography>
              </CardContent>
              <Box textAlign="right" pr={2} pb={1}>
                <Button
                  variant="contained"
                  size="small"
                  startIcon={<EditIcon />}
                  onClick={() => handleEdit(empresa.id)}
                >
                  Editar
                </Button>
              </Box>
            </Card>
          ))}
        </Stack>
      )}
      <Stack direction="row" spacing={2} justifyContent="center" mt={3}>
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
    </Box>
  ) : (
    <Box sx={{ height: 500, width: "100%" }}>
      <DataGrid
        rows={empresas}
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
  )
}

export default CompanyList
