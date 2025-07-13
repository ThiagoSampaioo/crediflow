// hooks/useBankAccount.js
import { useState, useEffect } from "react"
import { useAuth } from "../services/keycloak"

export const useBankAccount = () => {
  const { bankAccount, fetchBankAccount } = useAuth()
  const [currentAccount, setCurrentAccount] = useState(null)
  const [loading, setLoading] = useState(true)

  const refreshBankAccount = async () => {
    setLoading(true)
    await fetchBankAccount()
    setLoading(false)
  }

  useEffect(() => {
    if (bankAccount) {
      setCurrentAccount(bankAccount)
      setLoading(false)
    }
  }, [bankAccount])

  return { currentAccount, refreshBankAccount, loading }
}
