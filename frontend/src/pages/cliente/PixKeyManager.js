

import React, { useEffect, useState } from "react"
import {
  Dialog, DialogTitle, DialogContent, DialogActions,
  Button, TextField, MenuItem, Typography, IconButton, Box, Stack
} from "@mui/material"
import { Add, Edit, Delete } from "@mui/icons-material"
import { useApi } from "../../services/api"
import { useAuth } from "../../services/keycloak"

const keyTypes = ["CPF", "EMAIL", "PHONE", "RANDOM"]

const PixKeyManager = () => {
  const auth = useAuth()
  const { userInfo } = useAuth()
  const api = useApi()

  const [keys, setKeys] = useState([])
  const [open, setOpen] = useState(false)
  const [editingKey, setEditingKey] = useState(null)
  const [form, setForm] = useState({ pixKey: "", keyType: "CPF" })

  const fetchPixKeys = async () => {
    const res = await api.get(`/pix-keys/account/${auth.bankAccount.id}`)
    console.log("Chaves Pix recebidas:", res)
    if (!res || !Array.isArray(res)) {
      console.error("Dados inválidos recebidos:", res)
      return
    }
    setKeys(res)
  }

  useEffect(() => {
    if (auth?.bankAccount?.id) fetchPixKeys()
  }, [auth?.bankAccount?.id])

  const handleOpen = (key = null) => {
    setEditingKey(key)
    setForm(key ? { pixKey: key.pixKey, keyType: key.keyType } : { pixKey: "", keyType: "CPF" })
    setOpen(true)
  }

  const handleClose = () => {
    setOpen(false)
    setEditingKey(null)
  }

  const handleChange = e => {
    setForm({ ...form, [e.target.name]: e.target.value })
  }

  const handleSubmit = async () => {
    const payload = {
      bankAccountId: auth.bankAccount.id,
      pixKey: form.pixKey,
      keyType: form.keyType,
    }

    try {
      if (editingKey) {
        await api.put(`/pix-keys/${editingKey.id}`, payload) // ou implemente PATCH se preferir
      } else {
        await api.post("/pix-keys", payload)
      }
      fetchPixKeys()
      handleClose()
    } catch (err) {
      console.error("Erro ao salvar chave Pix", err)
    }
  }

  const handleDelete = async (id) => {
    if (window.confirm("Deseja realmente excluir esta chave Pix?")) {
      await api.delete(`/pix-keys/${id}`)
      fetchPixKeys()
    }
  }

  return (
    <>
      <Typography variant="h6" gutterBottom>Minhas Chaves Pix</Typography>
      <Stack spacing={2}>
        {keys.map((key) => (
          <Box key={key.id} display="flex" alignItems="center" justifyContent="space-between" sx={{ p: 1, border: '1px solid #ccc', borderRadius: 2 }}>
            <Box>
              <Typography variant="subtitle1">{key.pixKey}</Typography>
              <Typography variant="caption">Tipo: {key.keyType}</Typography>
            </Box>
            <Box>
              <IconButton onClick={() => handleOpen(key)}><Edit /></IconButton>
              <IconButton onClick={() => handleDelete(key.id)}><Delete /></IconButton>
            </Box>
          </Box>
        ))}
        <Button startIcon={<Add />} variant="contained" onClick={() => handleOpen()}>
          Nova Chave Pix
        </Button>
      </Stack>

      <Dialog open={open} onClose={handleClose} fullWidth>
        <DialogTitle>{editingKey ? "Editar Chave Pix" : "Nova Chave Pix"}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth label="Chave" margin="normal"
            name="pixKey" value={form.pixKey} onChange={handleChange}
          />
          <TextField
            select fullWidth label="Tipo" margin="normal"
            name="keyType" value={form.keyType} onChange={handleChange}
          >
            {keyTypes.map((type) => (
              <MenuItem key={type} value={type}>{type}</MenuItem>
            ))}
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleClose}>Cancelar</Button>
          <Button onClick={handleSubmit} variant="contained">Salvar</Button>
        </DialogActions>
      </Dialog>
    </>
  )
}

export default PixKeyManager
