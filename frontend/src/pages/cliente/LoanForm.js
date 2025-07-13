import React, { useState, useEffect } from "react"
import {
  Button,
  Paper,
  Typography,
  Box,
  Slider,
  Alert,
  CircularProgress,
  Tooltip,
  Divider,
  Fade,
} from "@mui/material"
import { useApi } from "../../services/api"
import { useAuth } from "../../services/keycloak"
import { useSnackbar } from "../../contexts/SnackbarContext"
import LoanList from "./LoanList"

const LoanForm = () => {
  const [value, setValue] = useState(500)
  const [term, setTerm] = useState(24)
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [submitted, setSubmitted] = useState(false)
  const [margin, setMargin] = useState(null)
  const [refresh, setRefresh] = useState(false)
  const api = useApi()
  const auth = useAuth()
  const { showSnackbar } = useSnackbar()

  useEffect(() => {
    const fetchMargin = async () => {
      try {
        const res = await api.get(`/loan-proposals/available-margin/${auth.userInfo.id}`)
        setMargin(parseFloat(res))
      } catch (err) {
        console.error("Erro ao buscar margem:", err)
        showSnackbar("Erro ao buscar margem disponível", "error")
      }
    }

    if (auth.userInfo?.id) fetchMargin()
  }, [auth.userInfo, api, showSnackbar])

  const handleSimulate = async (e) => {
    e.preventDefault()

    if (margin === 0) {
      showSnackbar("Não é possível simular: você não possui margem disponível.", "warning")
      return
    }

    setLoading(true)
    setResult(null)
    setSubmitted(false)

    const payload = {
      customerId: auth.userInfo.id,
      companyId: auth.userInfo.companyId,
      requestedAmount: value,
      availableLimit: margin,
      termInMonths: term,
      convenioType: "Prefeitura",
      monthlyInterestRate: 0.02,
      firstInstallmentDate: new Date().toISOString().split("T")[0],
      modoSimulacao: "VALOR_PARCELA",
    }

    try {
      const response = await api.post("/loan-proposals/simulate", payload)

      if (margin !== null && response.installmentValue > margin) {
        showSnackbar(
          `A parcela simulada (R$ ${response.installmentValue.toFixed(2)}) excede sua margem disponível (R$ ${margin.toFixed(2)})`,
          "warning"
        )
      }

      setResult(response)
      showSnackbar("Simulação realizada com sucesso!", "success")
    } catch (err) {
      console.error(err)
      showSnackbar("Erro ao simular proposta", "error")
    } finally {
      setLoading(false)
    }
  }

  const handleContract = async () => {
  if (!result) return

  const payload = {
    ...result,
    customerId: auth.userInfo.id,
    companyId: auth.userInfo.companyId,
    modoSimulacao: "VALOR_PARCELA",
    requestedAmount: value,
    termInMonths: term,
    monthlyInterestRate: 0.02,
    convenioType: "Prefeitura",
    availableLimit: margin,
    firstInstallmentDate: result.firstInstallmentDate,
  }

  try {
    await api.post("/loan-proposals", payload)
    showSnackbar("Proposta contratada com sucesso!", "success")

    // Resetar os dados do formulário
    setSubmitted(true)
    setResult(null)
    setValue(500)
    setTerm(24)

    // Atualizar margem
    const res = await api.get(`/loan-proposals/available-margin/${auth.userInfo.id}`)
    setMargin(parseFloat(res))
    setRefresh(!refresh) // Forçar atualização da lista de empréstimos
  } catch (err) {
    console.error(err)
    showSnackbar("Erro ao contratar proposta", "error")

  }
}


  const isAboveMargin =
    margin !== null &&
    result?.installmentValue &&
    parseFloat(result.installmentValue) > margin

  return (
    <Paper elevation={4} sx={{ p: { xs: 2, sm: 4 }, borderRadius: 4 }}>
      <Typography variant="h5" fontWeight="bold" gutterBottom>
        Simulação de Crédito Consignado
      </Typography>

      <Typography variant="body1" sx={{ mb: 1, color: "text.secondary" }}>
        Simule sua proposta de crédito de forma rápida e segura.
      </Typography>

      {submitted && (
        <Alert severity="success" sx={{ my: 2 }}>
          Proposta enviada com sucesso! Você será notificado sobre a assinatura da CCB.
        </Alert>
      )}

      {margin === 0 && (
        <Alert severity="warning" sx={{ my: 2 }}>
          Você não possui margem consignável disponível no momento.
          Não é possível contratar um novo empréstimo.
        </Alert>
      )}

      <Box component="form" onSubmit={handleSimulate} sx={{ mt: 3 }}>
        <Typography gutterBottom>Quanto deseja pagar por mês? R${value}</Typography>
        <Slider
          value={value}
          onChange={(e, newValue) => setValue(newValue)}
          step={50}
          min={100}
          max={margin || 1000}
          valueLabelDisplay="auto"
          sx={{ mb: 3 }}
          disabled={margin === 0}
        />

        <Typography gutterBottom>Prazo desejado (em meses): {term}</Typography>
        <Slider
          value={term}
          onChange={(e, newValue) => setTerm(newValue)}
          step={1}
          min={6}
          max={96}
          valueLabelDisplay="auto"
          sx={{ mb: 3 }}
          disabled={margin === 0}
        />

        <Tooltip
          title={
            margin !== null
              ? `Sua margem atual é de R$ ${margin?.toLocaleString("pt-BR", { minimumFractionDigits: 2 })}`
              : "Carregando sua margem..."
          }
          placement="top"
          arrow
        >
          <Box sx={{ mb: 2, color: "primary.main", fontWeight: 500 }}>
            Margem disponível:{" "}
            {margin !== null ? (
              <span>R$ {margin.toLocaleString("pt-BR", { minimumFractionDigits: 2 })}</span>
            ) : (
              <CircularProgress size={16} />
            )}
          </Box>
        </Tooltip>

        <Button
          type="submit"
          fullWidth
          variant="contained"
          size="large"
          sx={{ mt: 1 }}
          disabled={loading || margin === 0}
        >
          {loading ? <CircularProgress size={24} /> : "Simular"}
        </Button>
      </Box>

      {result && (
        <Fade in={true}>
          <Box
            sx={{
              mt: 5,
              p: 3,
              backgroundColor: "#fafafa",
              border: "1px solid #e0e0e0",
              borderRadius: 3,
            }}
          >
            <Typography variant="h6" fontWeight="bold" gutterBottom>
              Resultado da Simulação
            </Typography>

            <Divider sx={{ my: 2 }} />

            <Typography>
              Valor Financiado:{" "}
              <strong>
                R$ {result.financedAmount?.toLocaleString("pt-BR", { minimumFractionDigits: 2 })}
              </strong>
            </Typography>
            <Typography>
              Parcela Mensal:{" "}
              <strong>
                R$ {result.installmentValue?.toLocaleString("pt-BR", { minimumFractionDigits: 2 })}
              </strong>
            </Typography>
            <Typography>
              Total a Pagar:{" "}
              <strong>
                R$ {result.totalPayment?.toLocaleString("pt-BR", { minimumFractionDigits: 2 })}
              </strong>
            </Typography>
            <Typography>
              Prazo: <strong>{result.numberOfInstallments} meses</strong>
            </Typography>
            <Typography>
              1ª Parcela em: <strong>{result.firstInstallmentDate}</strong>
            </Typography>

            <Tooltip
              title={
                isAboveMargin
                  ? `A parcela excede a margem disponível (R$ ${margin?.toLocaleString("pt-BR")})`
                  : ""
              }
              placement="top"
              arrow
              disableHoverListener={!isAboveMargin}
            >
              <span style={{ display: "block" }}>
                <Button
                  fullWidth
                  variant="contained"
                  color="success"
                  sx={{ mt: 3 }}
                  onClick={handleContract}
                  disabled={isAboveMargin}
                >
                  Contratar Proposta
                </Button>
              </span>
            </Tooltip>
          </Box>
        </Fade>
      )}
      <br />
      <LoanList refresh={refresh} />
    </Paper>
  )
}

export default LoanForm
