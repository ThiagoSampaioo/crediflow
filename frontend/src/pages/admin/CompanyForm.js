"use client"
import { useParams } from "react-router-dom"
import {
  TextField,
  Button,
  Grid,
  Paper,
  Typography,
  Box,
  MenuItem,
} from "@mui/material"
import { useState, useEffect } from "react"
import { useApi } from "../../services/api"
import CompanyList from "./CompanyList"
import { useNavigate } from "react-router-dom"


const CompanyForm = () => {
  const navigate = useNavigate()
  const { id } = useParams()
  const isEditing = Boolean(id)
  const api = useApi()
  console.log("isEditing:", isEditing, "id:", id)
  const [formData, setFormData] = useState({
    name: "",
    cnpj: "",
    email: "",
    phone: "",
    responsible: "",
    type: "PUBLIC", // default
    status: "ACTIVE", // default
  })

  const [erro, setErro] = useState("")
  const [refresh, setRefresh] = useState(false)
  useEffect(() => {
    const carregarEmpresa = async () => {
      if (isEditing) {
        try {
          const data = await api.get(`/companies/${id}`)
          setFormData(data)
        } catch (err) {
          setErro(err.message)
        }
      }
    }

    carregarEmpresa()
  }, [id])


  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  const resetForm = () => {
    setFormData({
      name: "",
      cnpj: "",
      email: "",
      phone: "",
      responsible: "",
      type: "PUBLIC", // default
      status: "ACTIVE", // default
    })
    setErro("") // Reseta o erro
    navigate("/dashboard/empresas/nova") // Redireciona para a lista de empresas
  }

  const handleSubmit = async (event) => {
    event.preventDefault()

    try {
      if (isEditing) {
        await api.put(`/companies/${id}`, formData)
        alert("Empresa atualizada com sucesso!")
        setRefresh(!refresh) // Força a atualização da lista
        resetForm() // Reseta o formulário após a edição
      } else {
        await api.post("/companies", formData)
        alert("Empresa cadastrada com sucesso!")
        setRefresh(!refresh) // Força a atualização da lista
        resetForm() // Reseta o formulário após o cadastro
      }
    } catch (err) {
      alert("Erro ao salvar: " + err.message)
    }
  }

  return (
    <Paper sx={{ p: 3 }}>
      <Typography variant="h5" component="h1" gutterBottom>
        {isEditing ? "Editar Empresa" : "Cadastrar Nova Empresa"}
      </Typography>
      {erro && (
        <Typography color="error" variant="body2">
          {erro}
        </Typography>
      )}
      <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 1 }}>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField
              name="name"
              required
              fullWidth
              label="Nome da Empresa"
              value={formData.name}
              onChange={handleChange}
              autoFocus
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              name="cnpj"
              required
              fullWidth
              label="CNPJ"
              value={formData.cnpj}
              onChange={handleChange}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              name="email"
              fullWidth
              label="Email"
              value={formData.email}
              onChange={handleChange}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              name="phone"
              fullWidth
              label="Telefone"
              value={formData.phone}
              onChange={handleChange}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              name="responsible"
              fullWidth
              label="Responsável"
              value={formData.responsible}
              onChange={handleChange}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              name="type"
              select
              fullWidth
              label="Tipo"
              value={formData.type}
              onChange={handleChange}
            >
              <MenuItem value="PUBLIC">Pública</MenuItem>
              <MenuItem value="PRIVATE">Privada</MenuItem>
            </TextField>
          </Grid>

          {/*<Grid item xs={12} sm={6}>
            <TextField
              name="status"
              select
              fullWidth
              label="Status"
              value={formData.status}
              onChange={handleChange}
            >
              <MenuItem value="ACTIVE">Ativa</MenuItem>
              <MenuItem value="INACTIVE">Inativa</MenuItem>
            </TextField>
          </Grid>*/}
        </Grid>
        <Button type="submit" fullWidth variant="contained" sx={{ mt: 3, mb: 2 }}>
          {isEditing ? "Salvar Alterações" : "Cadastrar Empresa"}
        </Button>
      </Box>
      <CompanyList refresh={refresh} />
    </Paper>
  )
}

export default CompanyForm
