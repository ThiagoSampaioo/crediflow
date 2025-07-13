import { Typography, Grid, Card, CardContent, CardActionArea, Box } from "@mui/material"
import { Link as RouterLink } from "react-router-dom"
import BusinessIcon from "@mui/icons-material/Business"
import AssignmentIcon from "@mui/icons-material/Assignment"

const features = [
  {
    title: "Propostas",
    description: "Aprove ou reprove solicitações de crédito.",
    icon: <AssignmentIcon sx={{ fontSize: 32, color: "#fff" }} />,
    color: "#1976d2",
    link: "/dashboard/propostas"
  },
  {
    title: "Empresas",
    description: "Cadastre e edite empresas conveniadas.",
    icon: <BusinessIcon sx={{ fontSize: 32, color: "#fff" }} />,
    color: "#9c27b0",
    link: "/dashboard/empresas/nova"
  }
]

const AdminDashboard = () => {
  return (
    <>
      <Typography variant="h4" component="h1" gutterBottom>
        Olá, administrador 👋
      </Typography>
      <Typography variant="subtitle1" gutterBottom>
        O que você deseja fazer hoje?
      </Typography>

      <Grid container spacing={3} sx={{ mt: 1 }}>
        {features.map((item, index) => (
          <Grid item xs={12} md={6} key={index}>
            <Card
              elevation={3}
              sx={{
                transition: "0.3s",
                "&:hover": {
                  transform: "scale(1.02)",
                  boxShadow: 6
                }
              }}
            >
              <CardActionArea component={RouterLink} to={item.link}>
                <CardContent>
                  <Box
                    sx={{
                      width: 56,
                      height: 56,
                      borderRadius: "50%",
                      backgroundColor: item.color,
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      mb: 2
                    }}
                  >
                    {item.icon}
                  </Box>
                  <Typography variant="h6" component="div">
                    {item.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {item.description}
                  </Typography>
                </CardContent>
              </CardActionArea>
            </Card>
          </Grid>
        ))}
      </Grid>
    </>
  )
}

export default AdminDashboard
