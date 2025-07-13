"use client"
import { useAuth } from "../services/keycloak"
import { Button, Container, Typography, Box, Paper } from "@mui/material"
import LockOpenIcon from "@mui/icons-material/LockOpen"

const Login = () => {
  const { keycloak } = useAuth()
const { authenticated, roles: userRoles, isReady } = useAuth()
  // A lógica de redirecionamento já é tratada pelo `onLoad: 'login-required'`
  // Esta página pode servir como um fallback ou uma tela de boas-vindas
  // antes do redirecionamento do Keycloak.
  return (
    <Container component="main" maxWidth="xs">
      <Paper
        elevation={6}
        sx={{
          marginTop: 8,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          padding: 4,
        }}
      >
        <LockOpenIcon sx={{ fontSize: 60, mb: 2, color: "primary.main" }} />
{ keycloak && keycloak.authenticated ?
          <>
        <Typography component="h1" variant="h5">
          Você já está autenticado {keycloak.tokenParsed?.preferred_username || "usuário"}! sua role é: {keycloak.realmAccess?.roles.join(", ")}
        </Typography>
        <Box sx={{ mt: 3 }}>
          <Typography variant="body1" align="center">
           {keycloak.tokenParsed?.tenant_code ? `Seu Tenant Code é: ${keycloak.tokenParsed.tenant_code}` : "Você não possui um Tenant Code definido."}
          </Typography>
        </Box>
        <Box sx={{ mt: 3 }}>
          <Typography variant="body1" align="center">
           {keycloak.tokenParsed?.email ? `Seu email é: ${keycloak.tokenParsed.email}` : "Você não possui um email definido."}
          </Typography>
        </Box>
        
        <Box sx={{ mt: 3 }}>
          <Typography variant="body1" align="center">
           {useAuth().roles.length > 0 ? `Suas roles são: ${useAuth().roles.join(", ")}` : "Você não possui roles definidas."}
          </Typography>
        </Box>
        <Box sx={{ mt: 3 }}>
          <Typography variant="body1" align="center">
           {userRoles.length > 0 ? `Suas roles são: ${userRoles.join(", ")}` : "Você não possui roles definidas."}
          </Typography>
        </Box>
        <Box sx={{ mt: 3 }}>
          <Typography variant="body1" align="center">
            Você está autenticado como {keycloak.tokenParsed?.preferred_username || "usuário"} 
          </Typography>
          <Typography variant="body1" align="center">
            Você está autenticado {authenticated ? "com sucesso" : "sem sucesso"}
          </Typography>
        </Box>
        <Box sx={{ mt: 3 }}>
          <Typography variant="body1" align="center">
            {isReady ? "A autenticação está pronta" : "A autenticação ainda não está pronta"}
          </Typography>
        </Box>
        <Box sx={{ mt: 3 }}>
          <Typography variant="body1" align="center">
            Você será redirecionado para o Dashboard.
          </Typography>
        </Box>
        <Button 
          fullWidth
          variant="contained"
          sx={{ mt: 3, mb: 2 }}
          onClick={() => window.location.href = "/dashboard"}
        >
          
          ir para o Dashboard
        </Button>
        </>
:

        <>        <Typography component="h1" variant="h5">
          Bem-vindo ao CrediFlow 
        </Typography>
        
        <Box sx={{ mt: 3 }}>
          <Typography variant="body1" align="center">
            Você será redirecionado para a página de login.
          </Typography>
          <Button fullWidth variant="contained" sx={{ mt: 3, mb: 2 }} onClick={() => keycloak && keycloak.login()}>
            Ir para o Login
          </Button>
        </Box>
        </>}

      </Paper>
    </Container>
  )
}

export default Login
