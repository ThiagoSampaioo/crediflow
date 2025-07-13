# 💳 CrediFlow – Plataforma de Crédito Consignado

O **CrediFlow** é um sistema completo de gestão de crédito consignado desenvolvido como projeto de portfólio. Ele simula uma instituição financeira com funcionalidades de abertura de conta bancária, simulação de crédito, geração de propostas, assinatura de CCB e controle de parcelas e transações.

O sistema oferece uma jornada digital moderna com três perfis de acesso:

- **Admin:** Cadastra empresas conveniadas e acompanha propostas.
- **Empresa:** Gerencia seus funcionários.
- **Cliente (Funcionário):** Simula crédito, assina contratos, visualiza parcelas e movimenta a conta.

---

## 🔧 Tecnologias Utilizadas

| Camada         | Tecnologia                                                  |
|----------------|-------------------------------------------------------------|
| Frontend       | React, Material UI, Axios, React Router                     |
| Backend        | Java 17, Quarkus, Hibernate (JPA), MySQL, JWT, OpenAPI      |
| Autenticação   | Keycloak (controle de roles: admin, empresa, cliente)       |
| Outros         | Docker, Multitenancy via `tenant_code`, Swagger             |

---

## 🖼️ Vídeo Apresentação e explicação de uso

---

## 🖼️ Print do Sistema 

---

## 📦 Estrutura do Projeto

```bash
crediflow/      |   docker-compose up -d
│
├── backend/              # Quarkus (Java)    |   ./mvnw compile quarkus:dev
├── frontend/             # React + MUI       |   npm install | npm start
├── README.md
