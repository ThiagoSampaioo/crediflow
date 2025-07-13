"use client"
import { Box, Drawer, Toolbar, Divider } from "@mui/material"
import SidebarItems from "./SidebarItems"

const Sidebar = ({ drawerWidth, mobileOpen, handleDrawerToggle }) => {
  const drawerContent = (
    <div>
      <Toolbar />
      <Divider />
      <SidebarItems />
    </div>
  )

  return (
    <Box component="nav" sx={{ width: { md: drawerWidth }, flexShrink: { md: 0 } }} aria-label="mailbox folders">
      {/* Drawer para mobile */}
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={handleDrawerToggle}
        ModalProps={{
          keepMounted: true, // Better open performance on mobile.
        }}
        sx={{
          display: { xs: "block", md: "none" },
          "& .MuiDrawer-paper": { boxSizing: "border-box", width: drawerWidth },
        }}
      >
        {drawerContent}
      </Drawer>
      {/* Drawer para desktop */}
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: "none", md: "block" },
          "& .MuiDrawer-paper": { boxSizing: "border-box", width: drawerWidth },
        }}
        open
      >
        {drawerContent}
      </Drawer>
    </Box>
  )
}

export default Sidebar
