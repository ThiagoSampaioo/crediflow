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

## 🖼️ Vídeo Apresentação e explicação de uso do keycloak 
https://www.linkedin.com/posts/thiago-sampaio-b62a3524b_crediflow-plataforma-de-cr%C3%A9dito-consignado-activity-7350329467908554752-Z3U_?utm_source=share&utm_medium=member_desktop&rcm=ACoAAD3pQzoBESIT_JgBiccswMH5ppN4jQs-18U
---

## 🖼️ Print do Sistema 
(ADMIN)

<img width="390" height="842" alt="image" src="https://github.com/user-attachments/assets/0af37668-472e-4939-b7df-1eb6a8db67cb" />
<img width="390" height="839" alt="image" src="https://github.com/user-attachments/assets/b109c2b2-c27f-44a1-815e-b686bfefc101" />
<img width="388" height="843" alt="image" src="https://github.com/user-attachments/assets/d8980a09-904e-4eaf-8b68-adf5326b8f17" />
<img width="392" height="842" alt="image" src="https://github.com/user-attachments/assets/bb48085c-6a57-4f82-b35b-6c4c8cd5100b" />
<img width="390" height="840" alt="image" src="https://github.com/user-attachments/assets/9b7fb22f-e68b-45e4-99ec-fb2244e1c114" />


(COMPANY)


<img width="390" height="846" alt="image" src="https://github.com/user-attachments/assets/d44247b7-152a-4104-bfc0-ffb1beca2951" />
<img width="388" height="846" alt="image" src="https://github.com/user-attachments/assets/e1a278ef-5e24-426d-91fa-beb9849b67a9" />
<img width="390" height="843" alt="image" src="https://github.com/user-attachments/assets/b9fdf769-1cba-452a-8ecb-0b22506b3e45" />


(CLIENT)


<img width="389" height="844" alt="image" src="https://github.com/user-attachments/assets/2f81c5cf-2d7c-4694-8afe-2252952e3c61" />
<img width="388" height="840" alt="image" src="https://github.com/user-attachments/assets/10202757-167b-4851-9f16-5c5f37bf60f3" />
<img width="391" height="839" alt="image" src="https://github.com/user-attachments/assets/bec9aa05-b5c0-4e76-bdb0-99872590f283" />
<img width="384" height="840" alt="image" src="https://github.com/user-attachments/assets/7059e6f2-1e0d-4af8-ad7d-e24c958cbd0c" />
<img width="390" height="843" alt="image" src="https://github.com/user-attachments/assets/3ef74787-3acf-437d-b779-3681cb4dc799" />
<img width="386" height="842" alt="image" src="https://github.com/user-attachments/assets/b71ef945-de36-4cdc-b9af-937fb4c2ad38" />


(CCB)


<img width="634" height="861" alt="image" src="https://github.com/user-attachments/assets/5304e69f-8449-4dec-a252-f476fc3d6183" />
<img width="635" height="844" alt="image" src="https://github.com/user-attachments/assets/77f7f108-016c-47aa-b968-265db478233c" />
<img width="637" height="856" alt="image" src="https://github.com/user-attachments/assets/a7509cd3-27d7-4784-a5d3-a7c2e1479217" />



---

## 📦 Estrutura do Projeto

```bash
crediflow/      |   docker-compose up -d
│
├── backend/              # Quarkus (Java)    |   ./mvnw compile quarkus:dev
├── frontend/             # React + MUI       |   npm install | npm start
├── README.md


MIT License

Copyright (c) 2025 Francisco Thiago Sampaio Sousa

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the “Software”), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell   
copies of the Software, and to permit persons to whom the Software is        
furnished to do so, subject to the following conditions:                     

The above copyright notice and this permission notice shall be included in   
all copies or substantial portions of the Software.                          

THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR   
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,     
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE  
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER       
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING      
FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
IN THE SOFTWARE.

