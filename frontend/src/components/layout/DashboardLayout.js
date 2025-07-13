"use client"

import { useState } from "react"
import { Outlet } from "react-router-dom"
import { Box, useTheme, useMediaQuery } from "@mui/material"
import AppBar from "./AppBar"
import Sidebar from "./Sidebar"
import Copyright from "../common/Copyright"

const DRAWER_WIDTH = 240

const DashboardLayout = () => {
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down("md"))
  const [mobileOpen, setMobileOpen] = useState(false)

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen)
  }

  return (
    <Box sx={{ display: "flex", minHeight: "100vh" }}>
      <AppBar drawerWidth={DRAWER_WIDTH} handleDrawerToggle={handleDrawerToggle} />
      <Sidebar drawerWidth={DRAWER_WIDTH} mobileOpen={mobileOpen} handleDrawerToggle={handleDrawerToggle} />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { md: `calc(100% - ${DRAWER_WIDTH}px)` },
          mt: "64px", // Altura do AppBar
          display: "flex",
          flexDirection: "column",
        }}
      >
        <Box sx={{ flexGrow: 1 }}>
          <Outlet /> {/* O conteúdo da página será renderizado aqui */}
        </Box>
        <Copyright sx={{ pt: 4 }} />
      </Box>
    </Box>
  )
}

export default DashboardLayout
