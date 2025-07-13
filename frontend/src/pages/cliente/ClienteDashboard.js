import React from "react"
import {
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  CircularProgress,
  Box,
  IconButton,
} from "@mui/material"
import { Link as RouterLink } from "react-router-dom"
import AccountBalanceIcon from "@mui/icons-material/AccountBalance"
import VisibilityIcon from "@mui/icons-material/Visibility"
import VisibilityOffIcon from "@mui/icons-material/VisibilityOff"
import { useAuth } from "../../services/keycloak"
import { useBankAccount } from "../../hooks/useBankAccount"

const ClienteDashboard = () => {
  const auth = useAuth()
  const { userInfo } = auth
  const { currentAccount: bankAccount, loading } = useBankAccount()

  const [showBalance, setShowBalance] = React.useState(true)

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="60vh">
        <CircularProgress />
        <Typography sx={{ ml: 2 }}>Carregando informações...</Typography>
      </Box>
    )
  }

  return (
    <Box p={2}>
      <Typography variant="h4" gutterBottom fontWeight="bold">
        Olá, {userInfo?.name || "Cliente"}!
      </Typography>

      <Grid container spacing={3}>
        {/* Saldo em Conta */}
        <Grid item xs={12} md={6}>
          <Card sx={{ borderRadius: 3, boxShadow: 3, minWidth: 335 }}>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center">
                <Box>
                  <Typography color="text.secondary" variant="body2">
                    Saldo em Conta
                  </Typography>
                  <Typography variant="h4" fontWeight="bold">
                    {showBalance
                      ? `R$ ${Number(bankAccount?.balance || 0).toFixed(2)}`
                      : "R$ ••••••"}
                  </Typography>
                </Box>

                <IconButton onClick={() => setShowBalance(!showBalance)}>
                  {showBalance ? <VisibilityOffIcon /> : <VisibilityIcon />}
                </IconButton>
              </Box>

              <Typography variant="body2" sx={{ mt: 2 }}>
                Conta: <strong>{bankAccount?.accountNumber || "—"}</strong> | Agência:{" "}
                <strong>{bankAccount?.agencyNumber || "—"}</strong>
              </Typography>
            </CardContent>

            <CardActions sx={{ justifyContent: "flex-end", px: 2, pb: 2 }}>
              <Button
                component={RouterLink}
                to="/dashboard/extrato"
                size="small"
                variant="outlined"
              >
                Ver Extrato
              </Button>
              <Button
                component={RouterLink}
                to="/dashboard/transactions"
                size="small"
                variant="contained"
              >
                Fazer Pix
              </Button>
            </CardActions>
          </Card>
        </Grid>

        {/* Simulação de Crédito */}
        <Grid item xs={12} md={6}>
          <Card sx={{ borderRadius: 3, boxShadow: 3, height: "100%" }}>
            <CardContent>
              <Box display="flex" alignItems="center" mb={2}>
                <AccountBalanceIcon color="primary" sx={{ fontSize: 40, mr: 1 }} />
                <Typography variant="h6" fontWeight="bold">
                  Crédito Pré-Aprovado
                </Typography>
              </Box>
              <Typography variant="body2">
                Simule e contrate seu crédito consignado de forma rápida e segura.
              </Typography>
            </CardContent>

            <CardActions sx={{ justifyContent: "flex-end", px: 2, pb: 2 }}>
              <Button
                component={RouterLink}
                to="/dashboard/credito"
                variant="contained"
                fullWidth
              >
                Simular Agora
              </Button>
            </CardActions>
          </Card>
        </Grid>
      </Grid>
    </Box>
  )
}

export default ClienteDashboard
