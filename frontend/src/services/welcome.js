import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  DialogContentText,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Checkbox,
  Button,
  Typography,
  Box,
  Slide,
  FormControlLabel,
} from "@mui/material"
import ExpandMoreIcon from "@mui/icons-material/ExpandMore"
import { useState } from "react"

import axios from "axios"

const WelcomeDialog = ({userInfo}) => {
  
  const [termsAccepted, setTermsAccepted] = useState(false)
  const [loading, setLoading] = useState(false)

  const handleCreateAccount = async () => {
    try {
      setLoading(true)
      await axios.post("http://localhost:8082/bank-accounts", {
        customerId: userInfo?.id,
      })
      window.location.reload()
    } catch (err) {
      console.error("Erro ao criar conta:", err)
      alert("Erro ao criar conta. Tente novamente.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <Dialog
      fullScreen
      open={userInfo?.virtualAccountNumber === null}
      TransitionComponent={Slide}
      TransitionProps={{ direction: "up" }}
    >
      <DialogTitle sx={{ fontSize: "2rem", fontWeight: "bold", color: "#0d47a1" }}>
        👋 Bem-vindo(a), {userInfo?.name}!
      </DialogTitle>

      <DialogContent>
        <DialogContentText sx={{ mb: 2, fontSize: "1.2rem" }}>
          Você está acessando a plataforma da <strong>{userInfo?.company?.name}</strong>.
        </DialogContentText>

        <DialogContentText sx={{ mb: 3 }}>
          Para continuar utilizando o sistema e ter acesso aos serviços financeiros, você precisa criar sua conta bancária digital.
        </DialogContentText>

        <Accordion>
          <AccordionSummary expandIcon={<ExpandMoreIcon />}>
            <Typography fontWeight="bold">📄 Termos de Uso</Typography>
          </AccordionSummary>
          <AccordionDetails>
            <Typography variant="body2">
              Ao criar sua conta bancária digital, você concorda com os seguintes termos:
              <ul style={{ paddingLeft: "1rem" }}>
                <li>A conta será usada para recebimento de valores autorizados pela empresa conveniada.</li>
                <li>O acesso ao sistema está condicionado à criação desta conta.</li>
                <li>Seus dados estão protegidos conforme a LGPD.</li>
                <li>Você poderá solicitar encerramento da conta a qualquer momento.</li>
              </ul>
            </Typography>
          </AccordionDetails>
        </Accordion>

        <Box mt={2}>
          <FormControlLabel
            control={
              <Checkbox
                checked={termsAccepted}
                onChange={(e) => setTermsAccepted(e.target.checked)}
                color="primary"
              />
            }
            label="Li e aceito os termos de uso."
          />
        </Box>
      </DialogContent>

      <DialogActions sx={{ p: 3 }}>
        <Button
          onClick={handleCreateAccount}
          variant="contained"
          size="large"
          disabled={!termsAccepted || loading}
          sx={{
            fontWeight: "bold",
            borderRadius: 2,
            px: 4,
            py: 1,
            boxShadow: 3,
            transition: "0.3s",
            ":hover": {
              transform: "scale(1.05)",
            },
          }}
        >
          {loading ? "Criando Conta..." : "Criar Conta e Prosseguir"}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default WelcomeDialog
