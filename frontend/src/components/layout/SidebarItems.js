"use client"
import { Link as RouterLink } from "react-router-dom"
import { List, ListItem, ListItemButton, ListItemIcon, ListItemText } from "@mui/material"
import DashboardIcon from "@mui/icons-material/Dashboard"
import BusinessIcon from "@mui/icons-material/Business"
import GroupIcon from "@mui/icons-material/Group"
import AccountBalanceWalletIcon from "@mui/icons-material/AccountBalanceWallet"
import CreditScoreIcon from "@mui/icons-material/CreditScore"
import ReceiptIcon from "@mui/icons-material/Receipt"
import SwapHorizIcon from "@mui/icons-material/SwapHoriz"
import AssignmentIcon from "@mui/icons-material/Assignment"
import { useAuth } from "../../services/keycloak"

const menuItems = {
  admin: [
    { text: "Dashboard", icon: <DashboardIcon />, link: "/dashboard" },
    { text: "Propostas", icon: <AssignmentIcon />, link: "/dashboard/propostas" },
    { text: "Nova Empresa", icon: <BusinessIcon />, link: "/dashboard/empresas/nova" },
  ],
  empresa: [
    { text: "Dashboard", icon: <DashboardIcon />, link: "/dashboard/company" },
    { text: "Funcionários", icon: <GroupIcon />, link: "/dashboard/funcionarios/novo" },
  ],
  cliente: [
    { text: "Minha Conta", icon: <AccountBalanceWalletIcon />, link: "/dashboard/client" },
    { text: "Transações", icon: <SwapHorizIcon />, link: "/dashboard/transactions" },
    { text: "Extrato", icon: <ReceiptIcon />, link: "/dashboard/extrato" },
    { text: "Empréstimos", icon: <CreditScoreIcon />, link: "/dashboard/credito" },
   
  ],
}

const SidebarItems = () => {
  const { roles } = useAuth()
  const getMenuItems = () => {
    if (roles.includes("admin")) return menuItems.admin
    if (roles.includes("company")) return menuItems.empresa
    if (roles.includes("client")) return menuItems.cliente
    return []
  }
  return (
    <List>
      {getMenuItems().map((item) => (
        <ListItem key={item.text} disablePadding>
          <ListItemButton component={RouterLink} to={item.link}>
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primary={item.text} />
          </ListItemButton>
        </ListItem>
      ))}
    </List>
  )
}

export default SidebarItems
