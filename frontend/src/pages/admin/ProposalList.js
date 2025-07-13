import { useState, useEffect } from "react";
import {
  Typography,
  Box,
  Paper,
  Chip,
  Button,
  Stack,
  Card,
  CardContent,
  CardActions,
  useMediaQuery,
  useTheme,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Divider,
} from "@mui/material";
import { DataGrid } from "@mui/x-data-grid";
import DownloadIcon from "@mui/icons-material/Download";
import { useApi } from "../../services/api";
import { useSnackbar } from "../../contexts/SnackbarContext";
import { useAuth } from "../../services/keycloak";
import BadgeIcon from "@mui/icons-material/Badge"
import axios from "axios";

const statusMap = {
  PENDING_SIGNATURE: { label: "Pendente Assinatura", color: "warning" },
  SIGNED: { label: "Assinada", color: "info" },
  UNDER_REVIEW: { label: "Em Análise", color: "secondary" },
  APPROVED: { label: "Aprovada", color: "primary" },
  PAID: { label: "Desembolso Efetuado", color: "success" },
  REJECTED: { label: "Rejeitada", color: "error" },
  CANCELED: { label: "Cancelada", color: "default" },
  SIMULATED: { label: "Simulada", color: "default" },
};

const ProposalList = () => {
  const [proposals, setProposals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedProposal, setSelectedProposal] = useState(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [totalCount, setTotalCount] = useState(0);

  const api = useApi();
  const { showSnackbar } = useSnackbar();
  const auth = useAuth();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

  useEffect(() => {
    fetchProposals();
  }, [page]);

  const fetchProposals = async () => {
    try {
      setLoading(true);
      const response = await api.get("/loan-proposals/paged", {
        params: { search: "", page, size: pageSize },
      });
      setProposals(response.items);
      setTotalCount(response.total);
    } catch (error) {
      showSnackbar("Erro ao carregar propostas: " + error.message, "error");
    } finally {
      setLoading(false);
    }
  };



  const gerarOuAssinarCCB = async (id, tipo) => {
    try {
      const response = await axios.get(
        `http://localhost:8082/loan-proposals/${id}/ccb-sign`,
        {
          responseType: "blob",
          headers: {
            Authorization: `Bearer ${auth.keycloak.token}`,
          },
        }
      );

      const blob = new Blob([response.data], { type: "application/pdf" });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.setAttribute("download", `ccb_${tipo}_${id}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();

      fetchProposals();
      showSnackbar("CCB processada com sucesso!", "success");
    } catch (error) {
      showSnackbar("Erro ao baixar CCB: " + error.message, "error");
    }
  };

  const handlePayProposal = async (id) => {
    try {
      await api.put(`/loan-proposals/${id}/pay`);
      showSnackbar("Proposta paga com sucesso!", "success");
      fetchProposals();
    } catch (error) {
      showSnackbar("Erro ao pagar proposta: " + error.message, "error");
    }
  };

  const handleCancelProposal = async (id) => {
    try {
      await api.put(`/loan-proposals/${id}/cancel`);
      showSnackbar("Proposta cancelada com sucesso!", "success");
      fetchProposals();
    } catch (error) {
      showSnackbar("Erro ao cancelar proposta: " + error.message, "error");
    }
  };

  const columns = [
    { field: "id", headerName: "ID", width: 90 },
    { field: "customerName", headerName: "Cliente", width: 200 },
    {
      field: "financedAmount",
      headerName: "Valor Solicitado",
      width: 150,
      valueFormatter: (params) => `R$ ${params?.toFixed(2) || "0.00"}`,
    },
    { field: "companyName", headerName: "Empresa", width: 200 },
    {
      field: "status",
      headerName: "Status",
      width: 180,
      renderCell: (params) => {
        const { label, color } = statusMap[params.value] || {
          label: "Desconhecido",
          color: "default",
        };
        return <Chip label={label} color={color} size="small" />;
      },
    },
    {
      field: "actions",
      headerName: "Ações",
      width: 400,
      renderCell: (params) => {
        const status = params.row.status;
        return (
          <Stack direction="row" spacing={1}>
            {status === "SIGNED" && (
              <>
                <Button
                  variant="outlined"
                  color="success"
                  size="small"
                  onClick={() => handlePayProposal(params.row.id)}
                >
                   Desembolsar
                </Button>
                <Button
                  variant="outlined"
                  color="error"
                  size="small"
                  onClick={() => handleCancelProposal(params.row.id)}
                >
                  Cancelar
                </Button>
              </>
            )}
            <Button
              variant="outlined"
              size="small"
              startIcon={<DownloadIcon />}
              onClick={() => gerarOuAssinarCCB(params.row.id, "ccb-download")}
            >
              Baixar CCB
            </Button>
          </Stack>
        );
      },
    },
  ];


  return (
    <Paper sx={{ p: 2 }}>
      <Typography variant="h5" gutterBottom>
        Gerenciamento de Propostas de Crédito
      </Typography>

      {isMobile ? (
        <>
          <Stack spacing={2} sx={{ mt: 2 }}>
            {proposals.map((proposal) => {
              const status = statusMap[proposal.status] || {
                label: proposal.status,
                color: "default",
              };
              return (
                <Card
                  key={proposal.id}
                  elevation={3}

                  sx={{ cursor: "pointer" }}
                >
                  <CardContent>
                    <Box display="flex" alignItems="center" gap={1}>
                      <BadgeIcon />
                      <Typography variant="h6">{proposal.customerName}</Typography>
                    </Box>

                    <Divider sx={{ my: 2 }} />

                    <Typography variant="body2" color="text.secondary">
                      Empresa: {proposal.companyName}
                    </Typography>
                    <Typography variant="body2" sx={{ mt: 1 }}>
                      Valor: <strong>R$ {proposal.financedAmount?.toFixed(2)}</strong>
                    </Typography>
                    <Typography variant="body2" sx={{ mt: 1 }}>
                      Parcelas: {proposal.installments?.length || 0}
                    </Typography>
                    <Chip
                      label={status.label}
                      color={status.color}
                      size="small"
                      sx={{ mt: 1 }}
                    />


                    <Box sx={{ mt: 1 }}>
                      {proposal.status === "SIGNED" && (
                        <>
                          <Divider sx={{ my: 2 }} />
                          <Button
                            variant="outlined"
                            size="small"
                            startIcon={<DownloadIcon />}
                            onClick={() => gerarOuAssinarCCB(proposal.id, "ccb-download")}
                          >
                            Baixar CCB
                          </Button>
                        </>
                      )}
                    </Box>


                    {proposal.status === "SIGNED" && (
                      <>
                        <Divider sx={{ my: 2 }} />
                        <Box sx={{ mt: 1, display: "flex", gap: 1 }}>
                          <Button
                            variant="outlined"
                            color="success"
                            size="small"
                            onClick={() => handlePayProposal(proposal.id)}
                          >
                            Desembolsar
                          </Button>
                          <Button
                            variant="outlined"
                            color="error"
                            size="small"
                            onClick={() => handleCancelProposal(proposal.id)}
                          >
                            Cancelar
                          </Button>
                        </Box>
                      </>
                    )}
                    <Divider sx={{ my: 2 }} />
                    <Button
                      variant="outlined"
                      size="small"
                      onClick={() => {
                        setSelectedProposal(proposal);
                        setModalOpen(true);
                      }}
                    >
                      Ver Detalhes
                    </Button>

                  </CardContent>
                </Card>
              );
            })}
          </Stack>
          <Stack direction="row" spacing={2} justifyContent="center" mt={2}>
            <Button
              variant="outlined"
              disabled={page === 0}
              onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
            >
              Anterior
            </Button>
            <Button
              variant="outlined"
              disabled={(page + 1) * pageSize >= totalCount}
              onClick={() => setPage((prev) => prev + 1)}
            >
              Próxima
            </Button>
          </Stack>
        </>
      ) : (
        <Box sx={{ height: 600, width: "100%", mt: 2 }}>
          <DataGrid
            rows={proposals}
            columns={columns}
            loading={loading}
            pageSize={pageSize}
            rowCount={totalCount}
            paginationMode="server"
            onPageChange={(newPage) => setPage(newPage)}
            rowsPerPageOptions={[10]}
            disableSelectionOnClick
            onRowClick={(params) => {
              setSelectedProposal(params.row);
              setModalOpen(true);
            }}
          />
        </Box>
      )}

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
                <strong>Status:</strong>{" "}
                <Chip
                  label={statusMap[selectedProposal.status]?.label || selectedProposal.status}
                  color={statusMap[selectedProposal.status]?.color || "default"}
                  size="small"
                />
              </Typography>
              {selectedProposal.status === "SIGNED" && (
                <Box sx={{ mt: 2 }}>
                  <Button
                    variant="outlined"
                    size="small"
                    startIcon={<DownloadIcon />}
                    onClick={() => gerarOuAssinarCCB(selectedProposal.id, "ccb-download")}
                  >
                    Baixar CCB Assinada
                  </Button>
                </Box>
              )}
              <Typography variant="subtitle1" gutterBottom sx={{ mt: 2 }}>
                <strong>Parcelas:</strong>
              </Typography>
              {isMobile ? (
                <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                  {selectedProposal.installments?.map((parcel) => (
                    <Card key={parcel.number} sx={{ mb: 2 }} variant="outlined">
                      <CardContent>
                        <Typography variant="body2">
                          <strong>Parcela #{parcel.number}</strong>
                        </Typography>
                        <Typography variant="body2">
                          Vencimento: {parcel.dueDate}
                        </Typography>
                        <Typography variant="body2">
                          Valor: R$ {parcel.value?.toFixed(2)}
                        </Typography>
                        <Typography variant="body2">
                          Pago: {parcel.paidValue != null ? `R$ ${parcel.paidValue.toFixed(2)}` : "—"}
                        </Typography>

                        <Typography variant="body2">
                          <Chip
                            label={parcel.paid ? "Paga" : "Em aberto"}
                            color={parcel.paid ? "success" : "default"}
                            size="small"
                            sx={{ mt: 1 }}
                          />
                        </Typography>
                      </CardContent>
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
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {selectedProposal.installments?.map((parcel) => (
                      <TableRow key={parcel.number}>
                        <TableCell>{parcel.number}</TableCell>
                        <TableCell>{parcel.dueDate}</TableCell>
                        <TableCell>R$ {parcel.value?.toFixed(2)}</TableCell>
                        <TableCell>
                          {parcel.paidValue != null ? `R$ ${parcel.paidValue.toFixed(2)}` : "—"}
                        </TableCell>
                        <TableCell>
                          <Chip
                            label={parcel.paid ? "Paga" : "Em aberto"}
                            color={parcel.paid ? "success" : "default"}
                            size="small"
                          />
                        </TableCell>
                      </TableRow>
                    ))}
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
    </Paper>
  );
};

export default ProposalList;
