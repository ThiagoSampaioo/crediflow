import { useState, useEffect } from "react"
import { DataGrid } from "@mui/x-data-grid"
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Paper,
  Typography,
  Box,
  Button,
  Chip,
  IconButton,
  Alert,
  Tooltip,
  Card,
  CardContent,
  useMediaQuery,
  Divider,
} from "@mui/material"
import { useTheme } from "@mui/material/styles"
import {
  Dialog as ConfirmDialog,
  DialogTitle as ConfirmTitle,
  DialogContent as ConfirmContent,
  DialogActions as ConfirmActions,
} from "@mui/material"
import BadgeIcon from "@mui/icons-material/Badge"
import { AttachMoney, Description, Download, Edit } from "@mui/icons-material"
import { useApi } from "../../services/api"
import { useSnackbar } from "../../contexts/SnackbarContext"
import axios from "axios"
import { useAuth } from "../../services/keycloak"
import { useBankAccount } from "../../hooks/useBankAccount"
import { Stack } from "@mui/system"

const statusMap = {
  PENDING_SIGNATURE: { label: "Pendente Assinatura", color: "warning" },
  SIGNED: { label: "Assinada", color: "info" },
  UNDER_REVIEW: { label: "Em Análise", color: "secondary" },
  APPROVED: { label: "Aprovada", color: "primary" },
  PAID: { label: "Desembolso Efetuado", color: "success" },
  REJECTED: { label: "Rejeitada", color: "error" },
  CANCELED: { label: "Cancelada", color: "default" },
  SIMULATED: { label: "Simulada", color: "default" },
}

const LoanList = ({ refresh }) => {
  const [proposals, setProposals] = useState([])
  const [loading, setLoading] = useState(true)
  const { currentAccount } = useBankAccount()
  const api = useApi()
  const auth = useAuth()
  const { showSnackbar } = useSnackbar()
  const [selectedProposal, setSelectedProposal] = useState(null)
  const [modalOpen, setModalOpen] = useState(false)
  const [payingInstallment, setPayingInstallment] = useState(null)
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)

  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"))

  const fetchProposals = async () => {
    setLoading(true)
    try {
      const data = await api.get("/loan-proposals/my-proposals", {
        params: { page, size: rowsPerPage },
      })
      const mapped = data.items.map((item) => ({
        ...item,
        id: item.id,
        customerName: item.customerName,
        amount: item.requestedAmount,
        status: item.status,
      }))
      setProposals(mapped)
      setPage(data.page)
      setRowsPerPage(data.size)
    } catch (error) {
      console.error("Erro ao carregar propostas:", error)
      showSnackbar("Erro ao carregar propostas: " + error.message, "error")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchProposals()
  }, [auth.userInfo, page, rowsPerPage, refresh])

  const handleRequestPay = (installment) => {
    setPayingInstallment(installment)
    setConfirmOpen(true)
  }

  const confirmPayInstallment = async () => {
    if (!payingInstallment) return

    const { number, id: installmentId } = payingInstallment
    const bankAccountId = currentAccount?.id

    try {
      const response = await axios.post(
        `http://localhost:8082/loan-proposals/installments-pay/${installmentId}/pay/${bankAccountId}/receipt`,
        {},
        {
          responseType: "blob",
          headers: { Authorization: `Bearer ${auth.keycloak.token}` },
        }
      )

      const blob = new Blob([response.data], { type: "application/pdf" })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement("a")
      link.href = url
      link.setAttribute("download", `comprovante_parcela_${installmentId}.pdf`)
      document.body.appendChild(link)
      link.click()
      link.remove()

      showSnackbar("Parcela paga e comprovante baixado com sucesso!", "success")

      setSelectedProposal((prev) => ({
        ...prev,
        installments: prev.installments.map((inst) =>
          inst.number === number ? { ...inst, paid: true, paidValue: inst.value } : inst
        ),
      }))
      fetchProposals()
    } catch (err) {
      console.error(err)
      showSnackbar("Erro ao pagar e baixar comprovante: " + err.message, "error")
    } finally {
      setConfirmOpen(false)
      setPayingInstallment(null)
    }
  }

  const gerarOuAssinarCCB = async (id, tipo) => {
    try {
      const response = await axios.get(
        `http://localhost:8082/loan-proposals/${id}/${tipo === "ccb" ? "ccb-sign" : tipo}`,
        {
          responseType: "blob",
          headers: { Authorization: `Bearer ${auth.keycloak.token}` },
        }
      )

      const blob = new Blob([response.data], { type: "application/pdf" })
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement("a")
      link.href = url
      link.setAttribute("download", `ccb_${tipo}_${id}.pdf`)
      document.body.appendChild(link)
      link.click()
      link.remove()

      fetchProposals()
      showSnackbar(`CCB ${tipo === "ccb-generate" ? "gerada" : tipo === "ccb-sign" ? "assinada" : "baixada"} com sucesso!`, "success")
    } catch (error) {
      showSnackbar("Erro ao processar CCB: " + error.message, "error")
    }
  }

  const columns = [
    { field: "id", headerName: "ID", width: 90 },
    { field: "customerName", headerName: "Cliente", width: 250 },
    {
      field: "financedAmount",
      headerName: "Valor Solicitado",
      width: 150,
      valueFormatter: (params) => `R$ ${params || "0.00"}`,
    },
    { field: "companyName", headerName: "Empresa", width: 250 },
    {
      field: "status",
      headerName: "Status",
      width: 180,
      renderCell: (params) => {
        const { label, color } = statusMap[params.value] || {
          label: "Desconhecido",
          color: "default",
        }
        return <Chip label={label} color={color} size="small" />
      },
    },
  ]

  return (
    <Paper sx={{ p: 2 }}>
      <Typography variant="h5" gutterBottom>
        Gerenciamento de Propostas de Crédito
      </Typography>

      <Box sx={{ width: "100%", mt: 2 }}>
        {isMobile ? (
          proposals.map((proposal) => (
            <Card
              key={proposal.id}
              variant="outlined"
              sx={{ mb: 2, p: 2, boxShadow: 2, borderRadius: 3 }}
            /* onClick={() => {
               setSelectedProposal(proposal)
               setModalOpen(true)
             }}*/
            >
              <CardContent>
                <Box display="flex" alignItems="center" gap={1}>
                  <BadgeIcon />
                  <Typography variant="h6">{proposal.customerName}</Typography>
                </Box>

                <Divider sx={{ my: 2 }} />

                <Typography variant="body2">
                  <strong>Empresa:</strong> {proposal.companyName}
                </Typography>
                <Typography variant="body2">
                  <strong>Valor:</strong> R$ {proposal.financedAmount?.toFixed(2)}
                </Typography>
                <Typography variant="body2">
                  <strong>Parcelas:</strong> {proposal.installments?.length || 0}
                </Typography>
                <Typography variant="body2">
                  <strong>Status:</strong>{" "}
                  <Chip
                    label={statusMap[proposal.status]?.label || "Desconhecido"}
                    color={statusMap[proposal.status]?.color || "default"}
                    size="small"
                    sx={{ mt: 1 }}
                  />
                </Typography>
                <Divider sx={{ my: 2 }} />
                <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
                  <Button variant="outlined" onClick={() => {
                    setSelectedProposal(proposal)
                    setModalOpen(true)
                  }}>
                    Ver Detalhes
                  </Button>
                </Box>
              </CardContent>


            </Card>
          ))
        ) : (
          <Box sx={{ height: 600 }}>
            <DataGrid
              rows={proposals}
              columns={columns}
              loading={loading}
              pageSize={10}
              rowsPerPageOptions={[10]}
              disableSelectionOnClick
              onRowClick={(params) => {
                setSelectedProposal(params.row)
                setModalOpen(true)
              }}
            />
          </Box>
        )}
      </Box>
      <Divider sx={{ my: 2 }} />
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
          disabled={(page + 1) * rowsPerPage >= proposals.length}
          onClick={() => setPage((prev) => prev + 1)}
        >
          Próxima
        </Button>
      </Stack>
      <Dialog open={modalOpen} onClose={() => setModalOpen(false)} fullWidth maxWidth="md" fullScreen={isMobile}>
        <DialogTitle>Detalhes da Proposta #{selectedProposal?.id}</DialogTitle>
        <DialogContent dividers>
          {selectedProposal ? (
            <>
              <Typography variant="subtitle1" gutterBottom>
                <strong>Cliente:</strong> {selectedProposal.customerName}
              </Typography>
              <Typography variant="subtitle1" gutterBottom>
                <strong>Empresa:</strong> {selectedProposal.companyName}
              </Typography>
              <Typography variant="subtitle1" gutterBottom>
                <strong>Valor Solicitado:</strong> R$ {selectedProposal.financedAmount?.toFixed(2)}
              </Typography>
              <Typography variant="subtitle1" gutterBottom>
                <strong>Parcelas:</strong> {selectedProposal.installments?.length || 0}
              </Typography>
              <Typography variant="subtitle1" gutterBottom>
                <strong>Status:</strong> {statusMap[selectedProposal.status]?.label || selectedProposal.status}
              </Typography>

              <Box sx={{ mt: 2, display: "flex", flexWrap: "wrap", gap: 1 }}>
                {selectedProposal.status === "SIMULATED" && (
                  <Button variant="outlined" startIcon={<Description />} onClick={() => gerarOuAssinarCCB(selectedProposal.id, "ccb-generate")}>
                    Gerar CCB
                  </Button>
                )}
                {selectedProposal.status === "PENDING_SIGNATURE" && (
                  <Button variant="outlined" startIcon={<Edit />} onClick={() => gerarOuAssinarCCB(selectedProposal.id, "ccb-sign")}>
                    Assinar CCB
                  </Button>
                )}
                {selectedProposal.status === "SIGNED" && (
                  <Button variant="outlined" startIcon={<Download />} onClick={() => gerarOuAssinarCCB(selectedProposal.id, "ccb")}>
                    Baixar CCB Assinada
                  </Button>
                )}
              </Box>

              {selectedProposal.status === "SIGNED" && (
                <Alert severity="info" sx={{ mt: 2 }}>
                  Proposta assinada e enviada para análise. Aguarde a aprovação.
                </Alert>
              )}

              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                <strong>Parcelas:</strong>
              </Typography>

              {isMobile ? (
                <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                  {selectedProposal.installments?.map((inst) => (
                    <Card key={inst.number} variant="outlined" sx={{ p: 2 }}>
                      <Typography variant="body2">
                        <strong>Parcela #{inst.number}</strong> — Vencimento: {inst.dueDate}
                      </Typography>
                      <Typography variant="body2">Valor: R$ {inst.value?.toFixed(2)}</Typography>
                      <Typography variant="body2">{inst.paid ? `Pago: R$ ${inst.paidValue?.toFixed(2)}` : "Não pago"}</Typography>
                      <Box mt={1}>
                        {!inst.paid && selectedProposal.status === "PAID" && (
                          <Tooltip title="Pagar Parcela">
                            <IconButton size="small" color="primary" onClick={() => handleRequestPay(inst)}>
                              <AttachMoney />
                            </IconButton>
                          </Tooltip>
                        )}
                      </Box>
                    </Card>
                  ))}
                </Box>
              ) : (
                <Table size="small">
                  <TableHead>
                    <TableRow>
                      <TableCell>#</TableCell>
                      <TableCell>Vencimento</TableCell>
                      <TableCell>Valor</TableCell>
                      <TableCell>Pago</TableCell>
                      <TableCell>Status</TableCell>
                      <TableCell>Ações</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {selectedProposal.installments?.map((inst) => {
                      const anterioresPagas = selectedProposal.installments
                        .filter((i) => i.number < inst.number)
                        .every((i) => i.paid)

                      return (
                        <TableRow key={inst.number}>
                          <TableCell>{inst.number}</TableCell>
                          <TableCell>{inst.dueDate}</TableCell>
                          <TableCell>R$ {inst.value?.toFixed(2)}</TableCell>
                          <TableCell>{inst.paidValue != null ? `R$ ${inst.paidValue.toFixed(2)}` : "—"}</TableCell>
                          <TableCell>
                            <Chip label={inst.paid ? "Paga" : "Em aberto"} color={inst.paid ? "success" : "default"} size="small" />
                          </TableCell>
                          <TableCell>
                            {!inst.paid && selectedProposal.status === "PAID" && (
                              anterioresPagas ? (
                                <Tooltip title="Pagar Parcela">
                                  <IconButton size="small" color="primary" onClick={() => handleRequestPay(inst)}>
                                    <AttachMoney />
                                  </IconButton>
                                </Tooltip>
                              ) : (
                                <Tooltip title="Pague as parcelas anteriores primeiro">
                                  <span>
                                    <IconButton size="small" disabled>
                                      <AttachMoney />
                                    </IconButton>
                                  </span>
                                </Tooltip>
                              )
                            )}
                          </TableCell>
                        </TableRow>
                      )
                    })}
                  </TableBody>
                </Table>
              )}
            </>
          ) : (
            <Typography>Carregando detalhes...</Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setModalOpen(false)}>Fechar</Button>
        </DialogActions>
      </Dialog>

      <ConfirmDialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <ConfirmTitle>Confirmar Pagamento</ConfirmTitle>
        <ConfirmContent>
          <Typography>
            Deseja realmente pagar a parcela <strong>#{payingInstallment?.number}</strong> no valor de
            <strong> R$ {payingInstallment?.value?.toFixed(2)}</strong>?
          </Typography>
        </ConfirmContent>
        <ConfirmActions>
          <Button onClick={() => setConfirmOpen(false)}>Cancelar</Button>
          <Button onClick={confirmPayInstallment} variant="contained" color="success">
            Confirmar e Baixar Recibo
          </Button>
        </ConfirmActions>
      </ConfirmDialog>
    </Paper>
  )
}

export default LoanList
