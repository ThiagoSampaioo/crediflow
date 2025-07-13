"use client"
import { AppBar as MuiAppBar, Toolbar, IconButton, Typography, Button } from "@mui/material"
import MenuIcon from "@mui/icons-material/Menu"
import { useAuth } from "../../services/keycloak"

const AppBar = ({ drawerWidth, handleDrawerToggle }) => {
  const { keycloak } = useAuth()

  return (
    <MuiAppBar
      position="fixed"
      sx={{
        width: { md: `calc(100% - ${drawerWidth}px)` },
        ml: { md: `${drawerWidth}px` },
      }}
    >
      <Toolbar>
        <IconButton
          color="inherit"
          aria-label="open drawer"
          edge="start"
          onClick={handleDrawerToggle}
          sx={{ mr: 2, display: { md: "none" } }}
        >
          <MenuIcon />
        </IconButton>
        <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
          CrediFlow
        </Typography>
        <Button
          color="inherit"
          onClick={async () => {
            // Apaga IndexedDB
            await indexedDB.databases().then((databases) => {
              databases.forEach((db) => {
                if (db.name) indexedDB.deleteDatabase(db.name);
              });
            });

            // Limpa o localStorage e sessionStorage se usar
            localStorage.clear();
            sessionStorage.clear();

            // Logout com redirecionamento forçado para URL base
            const redirectUri = window.location.origin;
            keycloak.logout({ redirectUri }); // redireciona e força reload
          }}
        >
          Sair
        </Button>

      </Toolbar>
    </MuiAppBar>
  )
}

export default AppBar
