import {
  Typography,
  Grid,
  Card,
  CardContent,
  CardActionArea,
  Box,
} from "@mui/material"
import PersonAddIcon from "@mui/icons-material/PersonAdd"
import AssignmentIcon from "@mui/icons-material/Assignment"
import BusinessIcon from "@mui/icons-material/Business"
import { Link as RouterLink } from "react-router-dom"

const EmpresaDashboard = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Bem-vindo ao painel da empresa 👋
      </Typography>


      <Grid container spacing={3} sx={{ mt: 1 }}>
        <Grid item xs={12} md={4}>
          <Card elevation={3}>
            <CardActionArea component={RouterLink} to="/dashboard/funcionarios/novo">
              <CardContent>
                <Box
                  sx={{
                    width: 56,
                    height: 56,
                    backgroundColor: "#1976d2",
                    borderRadius: "50%",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    mb: 2,
                  }}
                >
                  <PersonAddIcon sx={{ color: "#fff", fontSize: 32 }} />
                </Box>
                <Typography variant="h6">Cadastrar Funcionário</Typography>
                <Typography variant="body2" color="text.secondary">
                  Adicione um novo colaborador à sua empresa.
                </Typography>
              </CardContent>
            </CardActionArea>
          </Card>
        </Grid>
      </Grid>
    </Box>
  )
}

export default EmpresaDashboard
