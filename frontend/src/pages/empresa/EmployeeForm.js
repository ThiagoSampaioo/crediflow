"use client"
import {
  TextField,
  Button,
  Grid,
  Paper,
  Typography,
  Box,
  MenuItem,
  Divider,
  CircularProgress,
} from "@mui/material"
import { useState, useEffect } from "react"
import { useParams, useNavigate } from "react-router-dom"
import { useApi } from "../../services/api"
import { useSnackbar } from "../../contexts/SnackbarContext"
import EmployeeList from "./EmployeeList"
import { useAuth } from "../../services/keycloak"
import axios from "axios"

const EmployeeForm = () => {
  const { id } = useParams()
  const isEditing = Boolean(id)
  const api = useApi()
  const { showSnackbar } = useSnackbar()
  const navigate = useNavigate()
  const { userInfo } = useAuth()
  const userId = userInfo?.id 
  const [refresh, setRefresh] = useState(false)
  const [formData, setFormData] = useState({
    name: "",
    cpf: "",
    email: "",
    phone: "",
    birthDate: "",
    occupation: "",
    salary: 0, // Salário do funcionário
    zipCode: "",
    street: "",
    streetNumber: "",
    neighborhood: "",
    city: "",
    state: "",
    country: "Brasil",
    companyId: userId, // ID da empresa do usuário autenticado
  })
  const [loading, setLoading] = useState(false)
  console.log("id Company:", userId)
  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData((prev) => ({ ...prev, [name]: value }))
  }

  useEffect(() => {
    if (isEditing) {
      const loadEmployee = async () => {
        try {
          const data = await api.get(`/customers/${id}`)
          setFormData(data)
          console.log("Dados do funcionário carregados:", data)
        } catch (err) {
          showSnackbar("Erro ao carregar funcionário: " + err.message, "error")
        }
      }
      loadEmployee()
    }
  }, [id])

  const resetForm = () => {
    setFormData({
      name: "",
      cpf: "",
      email: "",
      phone: "",
      birthDate: "",
      occupation: "",
      salary: 0,
      zipCode: "",
      street: "",
      streetNumber: "",
      neighborhood: "",
      city: "",
      state: "",
      country: "Brasil",
      companyId: userId,
    })
    navigate("/dashboard/funcionarios/novo")
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      if (isEditing) {
        await api.put(`/customers/${id}`, formData)
        showSnackbar("Funcionário atualizado com sucesso!", "success")
       
      } else {
        await api.post("/customers", formData)
        showSnackbar("Funcionário cadastrado com sucesso!", "success")
      }
      resetForm()
      setRefresh(!refresh) // Força a atualização da lista de funcionários
    } catch (err) {
      showSnackbar("Erro ao salvar funcionário: " + err.message, "error")
    } finally {
      setLoading(false)
    }
  }

  return (
    <Paper sx={{ p: 4, borderRadius: 3, boxShadow: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom fontWeight={600}>
        {isEditing ? "Editar Funcionário" : "Novo Funcionário"}
      </Typography>

      <Box component="form" onSubmit={handleSubmit} noValidate sx={{ mt: 3 }}>
        {/* Dados Pessoais */}
        <Typography variant="h6" gutterBottom>Dados Pessoais</Typography>
        <Divider sx={{ mb: 2 }} />
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}><TextField size="small" name="name" required fullWidth label="Nome Completo" value={formData.name} onChange={handleChange} /></Grid>
          <Grid item xs={12} sm={6}><TextField size="small" name="cpf" required fullWidth label="CPF" value={formData.cpf} onChange={handleChange} /></Grid>
          <Grid item xs={12} sm={6}><TextField size="small" name="email" fullWidth label="Email" value={formData.email} onChange={handleChange} /></Grid>
          <Grid item xs={12} sm={6}><TextField size="small" name="phone" fullWidth label="Telefone" value={formData.phone} onChange={handleChange} /></Grid>
          <Grid item xs={12} sm={6}><TextField size="small" name="birthDate" type="date" fullWidth label="Data de Nascimento" InputLabelProps={{ shrink: true }} value={formData.birthDate} onChange={handleChange} /></Grid>
          <Grid item xs={12} sm={6}><TextField size="small" name="occupation" fullWidth label="Ocupação" value={formData.occupation} onChange={handleChange} /></Grid>
          <Grid item xs={12} sm={6}><TextField size="small" name="salary" type="number" fullWidth label="Salário" value={formData.salary} onChange={handleChange} /></Grid>
        </Grid>

        {/* Endereço */}
        <Box mt={4}>
          <Typography variant="h6" gutterBottom>Endereço</Typography>
          <Divider sx={{ mb: 2 }} />
          <Grid container spacing={2}>
            <Grid item xs={12} sm={4}><TextField size="small" name="zipCode" fullWidth label="CEP" value={formData.zipCode} 
            onChange={async (e)=> {
              await handleChange(e)
              if (e.target.value.length === 9) { // Verifica se o CEP tem 9 caracteres
                try {
                  const response = await axios.get(`https://viacep.com.br/ws/${e.target.value.replace("-", "")}/json/`);
                  const { logradouro, bairro, localidade, uf } = response.data;
                  console.log("Dados do CEP:", response.data);
                  setFormData((prev) => ({
                    ...prev,
                    street: logradouro,
                    neighborhood: bairro,
                    city: localidade,
                    state: uf
                  }));
                } catch (error) {
                  showSnackbar("Erro ao buscar endereço pelo CEP: " + error.message, "error");
                }
              }
            }} 
            /></Grid>
            <Grid item xs={12} sm={8}><TextField size="small" name="street" fullWidth label="Rua" value={formData.street} onChange={handleChange} /></Grid>
            <Grid item xs={12} sm={4}><TextField size="small" name="streetNumber" fullWidth label="Número" value={formData.streetNumber} onChange={handleChange} /></Grid>
            <Grid item xs={12} sm={8}><TextField size="small" name="neighborhood" fullWidth label="Bairro" value={formData.neighborhood} onChange={handleChange} /></Grid>
            <Grid item xs={12} sm={6}><TextField size="small" name="city" fullWidth label="Cidade" value={formData.city} onChange={handleChange} /></Grid>
            <Grid item xs={12} sm={6}><TextField size="small" name="state" fullWidth label="Estado" value={formData.state} onChange={handleChange} /></Grid>
          </Grid>
        </Box>


        <Button type="submit" fullWidth variant="contained" size="large" sx={{ mt: 5 }} disabled={loading}>
          {
            loading ?
            <CircularProgress size={24} color="inherit" />
            :
            isEditing ? "Salvar Alterações" : "Cadastrar Funcionário"
          }
        </Button>
      </Box>

      <Box mt={6}>
        <EmployeeList refresh={refresh} />

      </Box>
    </Paper>
  )
}

export default EmployeeForm
