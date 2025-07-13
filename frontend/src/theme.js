import { createTheme } from "@mui/material/styles"

const theme = createTheme({
  palette: {
    primary: { main: "#00529B" },
    secondary: { main: "#D62828" },
    background: { default: "#F0F2F5", paper: "#FFFFFF" },
  },
  typography: {
    fontFamily: "Roboto, sans-serif",
    h4: { fontWeight: 700 },
    h5: { fontWeight: 600 },
  },
  components: {
    MuiButton: { styleOverrides: { root: { borderRadius: 8, textTransform: "none", fontWeight: 600 } } },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 12,
          boxShadow: "rgba(0, 0, 0, 0.05) 0px 6px 24px 0px, rgba(0, 0, 0, 0.08) 0px 0px 0px 1px;",
        },
      },
    },
    MuiDataGrid: { styleOverrides: { root: { border: "none" } } },
  },
})

export default theme
