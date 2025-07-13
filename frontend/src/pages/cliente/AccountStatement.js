import React, { useEffect, useState } from "react"
import {
  Paper,
  Typography,
  List,
  ListItem,
  ListItemText,
  Divider,
  Pagination,
  CircularProgress,
  Box,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Chip,
  Stack,
  Avatar,
  useMediaQuery,
  useTheme,
} from "@mui/material"
import { useBankAccount } from "../../hooks/useBankAccount"
import { useApi } from "../../services/api"
import { format } from "date-fns"
import { ptBR } from "date-fns/locale"
import PaymentIcon from "@mui/icons-material/Payment"
import SwapHorizIcon from "@mui/icons-material/SwapHoriz"
import AccountBalanceWalletIcon from "@mui/icons-material/AccountBalanceWallet"
import MonetizationOnIcon from "@mui/icons-material/MonetizationOn"
import AttachMoneyIcon from "@mui/icons-material/AttachMoney"

const typeIcons = {
  PIX: <SwapHorizIcon />,
  DEPOSIT: <AccountBalanceWalletIcon />,
  TRANSFER: <SwapHorizIcon />,
  LOAN_DISBURSEMENT: <AttachMoneyIcon />,
  BILL_PAYMENT: <PaymentIcon />,
  LOAN_INSTALLMENT_PAYMENT: <MonetizationOnIcon />,
  PAYMENT: <PaymentIcon />,
}

const statusLabels = {
  COMPLETED: { label: "Concluído", color: "success" },
  PENDING: { label: "Pendente", color: "warning" },
  FAILED: { label: "Falhou", color: "error" },
}

const AccountStatement = () => {
  const { currentAccount } = useBankAccount()
  const api = useApi()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"))

  const [transactions, setTransactions] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)
  const [loading, setLoading] = useState(false)
  const [type, setType] = useState("")
  const [status, setStatus] = useState("")

  const loadData = async () => {
    if (!currentAccount) return
    setLoading(true)
    try {
      const res = await api.get(`/transactions/by-account/${currentAccount.id}`, {
        params: {
          type: type || undefined,
          status: status || undefined,
          page,
          size: 10,
        },
      })
      setTransactions(res.items)
      setTotalPages(Math.ceil(res.total / res.size))
    } catch (err) {
      console.error("Erro ao carregar extrato:", err)
    }
    setLoading(false)
  }

  useEffect(() => {
    loadData()
  }, [currentAccount, page, type, status])

  const handlePageChange = (_, value) => {
    setPage(value - 1)
  }

  return (
    <Paper sx={{ p: isMobile ? 2 : 4, borderRadius: 4, boxShadow: 6, mt: 4, maxWidth: 900, mx: "auto" }}>
      <Typography variant="h5" fontWeight={600} gutterBottom>
        Extrato Bancário
      </Typography>

      <Box sx={{ display: "flex", gap: 2, mb: 2, flexDirection: isMobile ? "column" : "row" }}>
        <FormControl fullWidth>
          <InputLabel>Tipo</InputLabel>
          <Select value={type} label="Tipo" onChange={(e) => setType(e.target.value)}>
            <MenuItem value="">Todos</MenuItem>
            {Object.keys(typeIcons).map((key) => (
              <MenuItem key={key} value={key}>
                {key.replaceAll("_", " ").toUpperCase()}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      {loading ? (
        <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
          <CircularProgress />
        </Box>
      ) : (
        <List>
          {transactions.map((tx) => {
            const isCredit =
              (!tx.toAccountId || tx.toAccountId === currentAccount.id) &&
              tx.type !== "LOAN_INSTALLMENT_PAYMENT"

            const color = isCredit ? "success.main" : "error.main"
            const sign = isCredit ? "+" : "-"

            return (
              <React.Fragment key={tx.id}>
                <ListItem alignItems="flex-start" sx={{ px: 0 }}>
                  <Avatar sx={{ bgcolor: "primary.main", mr: 2 }}>
                    {typeIcons[tx.type] || <AttachMoneyIcon />}
                  </Avatar>

                  <Box sx={{ flexGrow: 1 }}>

                      <Stack direction="column" justifyContent="space-between" alignItems="left" flexWrap="wrap">
                        <Typography fontWeight={600} fontSize={isMobile ? 14 : 16}>
                          {tx.description || tx.type.replaceAll("_", " ")}
                        </Typography>
                        
                        <Typography variant="body2" color="text.secondary">
                          {format(new Date(tx.createdAt), "dd 'de' MMMM 'às' HH:mm", { locale: ptBR })}
                        </Typography>
                      </Stack>

                   </Box>
                   <Box sx={{ flexShrink: 0, textAlign: "right", ml: 2 }}>

                      <Stack direction="column" justifyContent="space-between" alignItems="right" flexWrap="wrap">

                        <Typography color={color} fontWeight={700} fontSize={isMobile ? 14 : 16}>
                          {sign} R$ {parseFloat(tx.amount).toFixed(2).replace(".", ",")}
                        </Typography>
                        <Chip
                          label={statusLabels[tx.status]?.label || tx.status}
                          color={statusLabels[tx.status]?.color || "default"}
                          size="small"
                          sx={{ mt: isMobile ? 1 : 0 }}
                          />
                          
                      </Stack>
                   </Box>
                </ListItem>
                <Divider sx={{ my: 1 }} />
              </React.Fragment>
            )
          })}
        </List>
      )}

      <Box sx={{ display: "flex", justifyContent: "center", mt: 3 }}>
        <Pagination
          count={totalPages}
          page={page + 1}
          onChange={handlePageChange}
          color="primary"
          shape="rounded"
        />
      </Box>
    </Paper>
  )
}

export default AccountStatement
