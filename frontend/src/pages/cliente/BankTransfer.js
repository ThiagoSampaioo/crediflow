// Aqui está a versão melhorada com foco em aparência (UI), experiência do usuário (UX) e responsividade:

import React from "react"
import {
  TextField,
  Button,
  Paper,
  Typography,
  Box,
  Alert,
  ToggleButtonGroup,
  ToggleButton,
  Fade,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  useMediaQuery,
  useTheme,
} from "@mui/material"
import ExpandMoreIcon from "@mui/icons-material/ExpandMore"
import AccountBalanceIcon from "@mui/icons-material/AccountBalance"
import QrCodeIcon from "@mui/icons-material/QrCode"
import SavingsIcon from "@mui/icons-material/Savings"
import { useAuth } from "../../services/keycloak"
import { useApi } from "../../services/api"
import { useBankAccount } from "../../hooks/useBankAccount"
import PixKeyManager from "./PixKeyManager"

const BankTransfer = () => {
  const auth = useAuth()
  const api = useApi()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"))
  const { userInfo } = auth
  const { currentAccount: bankAccount, refreshBankAccount, loading } = useBankAccount()

  const [method, setMethod] = React.useState("account")
  const [form, setForm] = React.useState({
    agencyNumber: "",
    accountNumber: "",
    pixKey: "",
    amount: "",
    acceptedTerms: false,
  })
  const [success, setSuccess] = React.useState(false)
  const [error, setError] = React.useState(null)
  const [confirmOpen, setConfirmOpen] = React.useState(false)
  const [recipientData, setRecipientData] = React.useState(null)
  const [pendingTransaction, setPendingTransaction] = React.useState(null)

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value })

  const handleMethodChange = (_, newMethod) => {
    if (newMethod) {
      setMethod(newMethod)
      setForm({ agencyNumber: "", accountNumber: "", pixKey: "", amount: "", acceptedTerms: false })
      setSuccess(false)
      setError(null)
    }
  }

  const gerarDescricaoAutomatica = (method, form, recipientData) => {
    const valorFormatado = `R$ ${parseFloat(form.amount).toFixed(2)}`
    switch (method) {
      case "account":
        return `Transferência de ${valorFormatado} para a agência ${recipientData.agencyNumber} e conta ${recipientData.accountNumber} de ${recipientData.name}`
      case "pix":
        return `Transferência de ${valorFormatado} via chave Pix \"${form.pixKey}\" para ${recipientData.name}`
      case "deposit":
        return `Depósito de ${valorFormatado} na sua conta`
      default:
        return ""
    }
  }

  const handlePreSubmit = async (e) => {
    e.preventDefault()
    setSuccess(false)
    setError(null)

    const parsedAmount = parseFloat(form.amount)
    if (!form.acceptedTerms) return setError("Você precisa aceitar os termos de uso.")
    if (isNaN(parsedAmount) || parsedAmount <= 0) return setError("Informe um valor válido maior que zero.")
    if ((method === "account" || method === "pix") && parsedAmount > bankAccount.balance)
      return setError(`Saldo insuficiente. Seu saldo é R$ ${bankAccount.balance.toFixed(2)}`)

    try {
      let res
      if (method === "account") {
        res = await api.get(`/bank-accounts/account-info/agency`, {
          params: { agency: form.agencyNumber, account: form.accountNumber },
        })
      } else if (method === "pix") {
        res = await api.get(`/bank-accounts/account-info/pix`, {
          params: { key: form.pixKey },
        })
      } else {
        const description = gerarDescricaoAutomatica("deposit", form, null)
        setPendingTransaction({ amount: parsedAmount, description })
        return handleSubmit(parsedAmount, description)
      }

      const description = gerarDescricaoAutomatica(method, form, res)
      setRecipientData(res)
      setPendingTransaction({ amount: parsedAmount, description })
      setConfirmOpen(true)
    } catch {
      setError("Destinatário não encontrado.")
    }
  }

  const handleSubmit = async (amountOverride, descriptionOverride) => {
    try {
      const amount = amountOverride || parseFloat(form.amount)
      const description = descriptionOverride || pendingTransaction?.description
      let res

      if (method === "account") {
        res = await api.post("/transactions/transfer/agency", {
          amount, description, fromAccountId: bankAccount?.id, agencyNumber: form.agencyNumber, accountNumber: form.accountNumber,
        })
      } else if (method === "pix") {
        res = await api.post("/transactions/pix", {
          amount, description, fromAccountId: bankAccount?.id, pixKey: form.pixKey,
        })
      } else if (method === "deposit") {
        res = await api.post("/transactions/deposit/agency", {
          amount, description, agencyNumber: userInfo?.virtualAgencyNumber, accountNumber: userInfo?.virtualAccountNumber,
        })
      }

      setSuccess(true)
      await refreshBankAccount()
      setForm({ agencyNumber: "", accountNumber: "", pixKey: "", amount: "", acceptedTerms: false })
    } catch (err) {
      setError(err.response?.data || "Erro desconhecido")
    }
  }

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" height="50vh">
        <CircularProgress />
        <Typography sx={{ ml: 2 }}>Carregando informações...</Typography>
      </Box>
    )
  }

  return (
    <Paper sx={{ p: 4, borderRadius: 4, boxShadow: 4, maxWidth: 800, mx: "auto", mt: 4 }}>
      <Typography variant="h5" fontWeight={600} gutterBottom>
        Operações Bancárias
      </Typography>

      <Typography variant="body1" sx={{ mb: 2 }}>
        Saldo atual: <strong>R$ {bankAccount?.balance?.toFixed(2)}</strong>
      </Typography>

      <ToggleButtonGroup
        value={method}
        exclusive
        onChange={handleMethodChange}
        fullWidth
        orientation={isMobile ? "vertical" : "horizontal"}
        sx={{ mb: 3 }}
      >
        <ToggleButton value="account"><AccountBalanceIcon sx={{ mr: 1 }} /> Agência + Conta</ToggleButton>
        <ToggleButton value="pix"><QrCodeIcon sx={{ mr: 1 }} /> Chave Pix</ToggleButton>
        <ToggleButton value="deposit"><SavingsIcon sx={{ mr: 1 }} /> Depósito</ToggleButton>
      </ToggleButtonGroup>

      {success && <Alert severity="success" sx={{ mb: 2 }}>Transação realizada com sucesso!</Alert>}
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Box component="form" onSubmit={handlePreSubmit}>
        <Fade in={method === "account"} unmountOnExit>
          <Box>
            <TextField fullWidth margin="normal" label="Agência" name="agencyNumber" value={form.agencyNumber} onChange={handleChange} />
            <TextField fullWidth margin="normal" label="Conta" name="accountNumber" value={form.accountNumber} onChange={handleChange} />
          </Box>
        </Fade>

        <Fade in={method === "pix"} unmountOnExit>
          <Box>
            <PixKeyManager />
            <TextField fullWidth margin="normal" label="Chave Pix" name="pixKey" value={form.pixKey} onChange={handleChange} />
          </Box>
        </Fade>

        {method === "deposit" && (
          <Alert severity="info" sx={{ my: 2 }}>
            Você está prestes a depositar em sua própria conta ({userInfo?.virtualAgencyNumber} / {userInfo?.virtualAccountNumber})
          </Alert>
        )}

        <TextField
          fullWidth
          margin="normal"
          label="Valor"
          name="amount"
          type="number"
          inputProps={{ min: 0.01, step: "0.01" }}
          value={form.amount}
          onChange={handleChange}
        />

        <Accordion sx={{ mt: 2 }}>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography>Termos de Uso</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Typography variant="body2" color="text.secondary">
              Esta plataforma é uma simulação. Nenhuma transação financeira real é realizada.
            </Typography>
          </AccordionDetails>
        </Accordion>

        <Box display="flex" alignItems="center" gap={1} mt={2}>
          <input
            type="checkbox"
            checked={form.acceptedTerms}
            onChange={(e) => setForm({ ...form, acceptedTerms: e.target.checked })}
          />
          <Typography variant="body2">Li e aceito os termos de uso</Typography>
        </Box>

        <Button type="submit" variant="contained" fullWidth size="large" sx={{ mt: 3, py: 1.5 }} 
        //se for transferencia via pix e não tiver chave, desabilita o botão
          disabled={(method === "pix" && !form.pixKey) || (method === "account" && (!form.agencyNumber || !form.accountNumber)) || !form.amount || !form.acceptedTerms}>
          Confirmar Transação
        </Button>
      </Box>

      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>Confirmar Dados</DialogTitle>
        <DialogContent dividers>
          {recipientData ? (
            <Box>
              <Typography><strong>Nome:</strong> {recipientData.name}</Typography>
              <Typography><strong>Agência:</strong> {recipientData.agencyNumber}</Typography>
              <Typography><strong>Conta:</strong> {recipientData.accountNumber}</Typography>
              <Typography><strong>Valor:</strong> R$ {pendingTransaction?.amount?.toFixed(2)}</Typography>
              <Typography><strong>Descrição:</strong> {pendingTransaction?.description}</Typography>
            </Box>
          ) : (
            <Typography color="error">Dados não encontrados.</Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)} color="inherit">Cancelar</Button>
          <Button onClick={() => {
            setConfirmOpen(false)
            handleSubmit(pendingTransaction.amount, pendingTransaction.description)
          }} variant="contained" color="primary">
            Confirmar
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  )
}

export default BankTransfer