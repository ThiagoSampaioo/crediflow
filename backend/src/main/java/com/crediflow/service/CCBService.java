package com.crediflow.service;

import com.crediflow.entity.LoanInstallment;
import com.crediflow.entity.LoanProposal;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.crediflow.enums.LoanProposalStatus;
import com.crediflow.exception.BadRequestException;

import jakarta.enterprise.context.ApplicationScoped;

import jakarta.transaction.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@ApplicationScoped
public class CCBService {

    private Paragraph lineSpacing(float antes, float depois) {
        LineSeparator line = new LineSeparator();
        Chunk chunk = new Chunk(line);
        Paragraph paragraph = new Paragraph(chunk);
        paragraph.setSpacingBefore(antes);
        paragraph.setSpacingAfter(depois);
        return paragraph;
    }

    private String gerarCodigoAleatorio() {
        StringBuilder codigo = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            int digito = (int) (Math.random() * 10);
            codigo.append(digito);
        }
        return codigo.toString();
    }

    private void addInfoBlock(PdfPTable table, String label, String value, Font boldFont, Font normalFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, boldFont));
        PdfPCell valueCell = new PdfPCell(new Phrase(value, normalFont));
        labelCell.setBackgroundColor(new Color(240, 240, 240));
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void adicionarTextoInicial(Document doc, Font normalFont, Font sectionTitleFont) throws DocumentException {
        Paragraph p = new Paragraph();
        p.setFont(normalFont);
        p.setSpacingAfter(12);
        p.add("Pela presente CÉDULA DE CRÉDITO BANCÁRIO, doravante designada simplesmente como CÉDULA, o EMITENTE se compromete, de forma irrevogável e irretratável, a pagar ao CREDOR, ou à sua ordem, o valor líquido, certo e exigível correspondente ao Valor do Crédito acordado entre as partes, acrescido dos juros remuneratórios convencionados, capitalizados conforme a periodicidade definida, bem como dos encargos moratórios e despesas previstas nesta CÉDULA.\n\n");
        p.add("O pagamento será efetuado por meio da liquidação das prestações nos prazos e condições estabelecidos no quadro resumo e demais cláusulas contratuais.");
        doc.add(p);

        doc.add(new Paragraph("Cláusulas Complementares", sectionTitleFont));
        doc.add(lineSpacing(5, 10));
        doc.add(new Paragraph(
                "1. Antecipação de Pagamento\nO EMITENTE poderá, a qualquer tempo, realizar o pagamento antecipado do saldo devedor, total ou parcial, com redução proporcional dos juros e encargos, conforme previsto na legislação vigente.",
                normalFont));
        doc.add(new Paragraph(
                "2. Inadimplemento e Vencimento Antecipado\nO não pagamento de qualquer parcela no prazo estipulado, bem como o descumprimento de quaisquer obrigações pactuadas, ensejará o vencimento antecipado da dívida, autorizando o CREDOR a exigir o saldo devedor integral, acrescido de encargos, multa contratual e demais cominações legais.",
                normalFont));
        doc.add(new Paragraph(
                "3. Título Executivo Extrajudicial\nEsta CÉDULA constitui título executivo extrajudicial, nos termos do artigo 784, inciso III, do Código de Processo Civil, podendo ser utilizada para fins de cobrança judicial em caso de inadimplemento.",
                normalFont));
        doc.add(new Paragraph(
                "4. Tratamento de Dados Pessoais (LGPD)\nO EMITENTE declara estar ciente de que seus dados pessoais serão tratados pelo CREDOR nos termos da Lei nº 13.709/2018 (Lei Geral de Proteção de Dados - LGPD), exclusivamente para fins relacionados à presente operação de crédito.",
                normalFont));
        doc.add(new Paragraph(
                "5. Foro\nFica eleito o foro da comarca da sede do CREDOR como competente para dirimir quaisquer controvérsias decorrentes desta CÉDULA, com renúncia expressa a qualquer outro, por mais privilegiado que seja.",
                normalFont));
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
    }

    private void adicionarDisposicoesFinais(Document doc, Font normalFont, Font sectionTitleFont, LoanProposal proposal)
            throws DocumentException {
        doc.add(new Paragraph("Disposições Finais", sectionTitleFont));
        doc.add(lineSpacing(-12, 5));
        doc.add(new Paragraph(
                "O EMITENTE declara e reconhece, para todos os fins de direito, que a formalização desta CÉDULA DE CRÉDITO BANCÁRIO por meio eletrônico possui validade jurídica plena, equiparando sua assinatura digital ou eletrônica à assinatura física.",
                normalFont));
        doc.add(new Paragraph(
                "O EMITENTE está ciente de que tem o direito de desistir da operação no prazo de até 7 (sete) dias corridos a contar do recebimento dos valores, devendo, nesse caso, solicitar o cancelamento diretamente à instituição credora, responsabilizando-se pela devolução integral dos valores recebidos e pelos encargos eventualmente incidentes.",
                normalFont));
        doc.add(new Paragraph(
                "Esta CÉDULA é regida pelas disposições aplicáveis da legislação brasileira, em especial a Lei nº 10.931/2004, sendo considerada título executivo extrajudicial, certo, líquido e exigível. O valor contratado, os juros, os encargos e as condições de pagamento encontram-se detalhados no corpo deste documento e no respectivo cronograma de parcelas.",
                normalFont));
        doc.add(new Paragraph(
                "O EMITENTE declara ter ciência prévia sobre o Custo Efetivo Total (CET), taxas aplicadas, bem como das implicações em caso de inadimplemento. Em caso de atraso no pagamento, o CREDOR poderá considerar a dívida vencida antecipadamente, com incidência de encargos legais e possibilidade de cobrança judicial ou extrajudicial.",
                normalFont));
        doc.add(Chunk.NEWLINE);

        // Declaração
        doc.add(new Paragraph("Declaração", sectionTitleFont));
        doc.add(lineSpacing(-12, 5));
        doc.add(new Paragraph("Eu, " + proposal.getCustomer().getName() + ", CPF: " + proposal.getCustomer().getCpf()
                + ", declaro ter ciência e concordância com os termos desta CCB.", normalFont));
        doc.add(Chunk.NEWLINE);
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Assinatura do Cliente: ___________________________________   Data: ___/___/______",
                normalFont));
        doc.add(Chunk.NEWLINE);
        doc.add(new Paragraph("Código CCB: " + proposal.getCcb(), normalFont));
    }

    @Transactional
    public byte[] gerarPdfCCB(LoanProposal proposal) {
        try {
            if (proposal == null || proposal.getCustomer() == null || proposal.getCompany() == null) {
                throw new BadRequestException("Proposta ou dados incompletos");
            }
            String codigoCCB = gerarCodigoAleatorio();
            proposal.setCcb(codigoCCB);
            proposal.setStatus(LoanProposalStatus.PENDING_SIGNATURE);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(0, 70, 130));
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            Paragraph title = new Paragraph("CÉDULA DE CRÉDITO BANCÁRIO (CCB) - Nº " + codigoCCB, titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(20);
            doc.add(title);

            // Adiciona o texto inicial
            adicionarTextoInicial(doc, normalFont, sectionTitleFont);

            // Bloco: Dados do Emitente
            Paragraph emitenteTitle = new Paragraph("Dados do Emitente (Cliente)", sectionTitleFont);
            emitenteTitle.setSpacingAfter(5);
            doc.add(emitenteTitle);
            PdfPTable clienteTable = new PdfPTable(2);
            clienteTable.setWidthPercentage(100);
            clienteTable.setSpacingBefore(5);
            clienteTable.setWidths(new int[] { 1, 3 });
            addInfoBlock(clienteTable, "Nome:", proposal.getCustomer().getName() + " (\"EMITENTE\")", boldFont,
                    normalFont);
            addInfoBlock(clienteTable, "CPF:", proposal.getCustomer().getCpf(), boldFont, normalFont);
            addInfoBlock(clienteTable, "Endereço:", proposal.getCustomer().getStreet() + ", "
                    + proposal.getCustomer().getStreetNumber() + " - " + proposal.getCustomer().getNeighborhood(),
                    boldFont, normalFont);
            addInfoBlock(clienteTable, "Cidade:", proposal.getCustomer().getCity(), boldFont, normalFont);
            addInfoBlock(clienteTable, "UF:", proposal.getCustomer().getState(), boldFont, normalFont);
            addInfoBlock(clienteTable, "CEP:", proposal.getCustomer().getZipCode(), boldFont, normalFont);
            addInfoBlock(clienteTable, "Nacionalidade:", "Brasileiro", boldFont, normalFont);
            addInfoBlock(clienteTable, "Data de Nascimento:",
                    proposal.getCustomer().getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), boldFont,
                    normalFont);
            doc.add(clienteTable);
            doc.add(new Paragraph("Doravante denominado apenas EMITENTE.", normalFont));
            doc.add(Chunk.NEWLINE);

            // Bloco: Informações Bancárias
            Paragraph bancoTitle = new Paragraph("Informações Bancárias", sectionTitleFont);
            bancoTitle.setSpacingAfter(5);
            doc.add(bancoTitle);
            PdfPTable bancoTable = new PdfPTable(2);
            bancoTable.setWidthPercentage(100);
            bancoTable.setSpacingBefore(5);
            bancoTable.setWidths(new int[] { 1, 3 });
            addInfoBlock(bancoTable, "Banco:", "CrediFlow Bank", boldFont, normalFont);
            addInfoBlock(bancoTable, "Agência:", proposal.getCustomer().getVirtualAccount().getAgencyNumber(), boldFont,
                    normalFont);
            addInfoBlock(bancoTable, "Conta:", proposal.getCustomer().getVirtualAccount().getAccountNumber(), boldFont,
                    normalFont);
            addInfoBlock(bancoTable, "CPF:", proposal.getCustomer().getCpf(), boldFont, normalFont);
            addInfoBlock(bancoTable, "Tipo de Conta:", "Conta Corrente", boldFont, normalFont);
            addInfoBlock(bancoTable, "Chave Pix:",
                    proposal.getCustomer().getVirtualAccount().getPixKeys().isEmpty() ? "Não Informada"
                            : proposal.getCustomer().getVirtualAccount().getPixKeys().get(0).getKey(),
                    boldFont, normalFont);
            addInfoBlock(bancoTable, "Chave Pix Tipo:",
                    proposal.getCustomer().getVirtualAccount().getPixKeys().isEmpty() ? "Não Informado"
                            : proposal.getCustomer().getVirtualAccount().getPixKeys().get(0).getKeyType().name(),
                    boldFont, normalFont);
            doc.add(bancoTable);
            doc.add(Chunk.NEWLINE);

            // Bloco: Informações da Proposta
            Paragraph propostaTitle = new Paragraph("Informações da Proposta", sectionTitleFont);
            propostaTitle.setSpacingAfter(5);
            doc.add(propostaTitle);
            PdfPTable propostaTable = new PdfPTable(2);
            propostaTable.setWidthPercentage(100);
            propostaTable.setSpacingBefore(5);
            propostaTable.setWidths(new int[] { 1, 3 });
            addInfoBlock(propostaTable, "Cod. da Proposta:", proposal.getId().toString(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Data da Proposta:",
                    proposal.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), boldFont, normalFont);
            addInfoBlock(propostaTable, "Valor Solicitado:", "R$ " + proposal.getRequestedAmount(), boldFont,
                    normalFont);
            addInfoBlock(propostaTable, "Valor da Parcela:", "R$ " + proposal.getInstallmentValue(), boldFont,
                    normalFont);
            addInfoBlock(propostaTable, "Prazo:", proposal.getTermInMonths() + " meses", boldFont, normalFont);
            addInfoBlock(propostaTable, "Taxa de Juros:",
                    proposal.getMonthlyRate().multiply(new BigDecimal("100")) + "%", boldFont, normalFont);
            addInfoBlock(propostaTable, "Data da 1ª Parcela:",
                    proposal.getFirstInstallmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), boldFont,
                    normalFont);
            addInfoBlock(propostaTable, "IOF Total:", "R$ " + proposal.getIofTotal(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Taxa de Juros Efetiva Mensal:",
                    proposal.getEffectiveMonthlyRate().multiply(new BigDecimal("100")) + "%", boldFont, normalFont);
            addInfoBlock(propostaTable, "Taxa de Juros Efetiva Anual:",
                    proposal.getEffectiveAnnualRate().multiply(new BigDecimal("100")) + "%", boldFont, normalFont);
            addInfoBlock(propostaTable, "Taxa de Juros Contratada Anual:",
                    proposal.getContractedAnnualRate().multiply(new BigDecimal("100")) + "%", boldFont, normalFont);
            addInfoBlock(propostaTable, "Tipo de Produto:", proposal.getProductType(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Fundo:", proposal.getFund(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Tipo de Parcela:", proposal.getInstallmentType(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Valor Total do Empréstimo:", "R$ " + proposal.getTotalPayment(), boldFont,
                    normalFont);
            addInfoBlock(propostaTable, "Data de Liberação:",
                    proposal.getDisbursementDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), boldFont,
                    normalFont);
            doc.add(propostaTable);
            doc.add(Chunk.NEWLINE);

            // Tabela de parcelas
            doc.add(new Paragraph("Cronograma de Parcelas", sectionTitleFont));
            doc.add(lineSpacing(-12, 5));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new int[] { 1, 3, 3, 3 });

            Stream.of("Nº", "Vencimento", "Valor", "Saldo").forEach(header -> {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setBackgroundColor(new Color(230, 230, 250));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            });

            for (LoanInstallment i : proposal.getInstallments()) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(i.getNumber()), normalFont)));
                table.addCell(new PdfPCell(
                        new Phrase(i.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont)));
                table.addCell(new PdfPCell(new Phrase("R$ " + i.getValue(), normalFont)));
                table.addCell(new PdfPCell(new Phrase("R$ " + i.getBalance(), normalFont)));
            }

            doc.add(table);
            doc.add(Chunk.NEWLINE);

            // Adiciona as disposições finais
            adicionarDisposicoesFinais(doc, normalFont, sectionTitleFont, proposal);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar CCB", e);
        }
    }

    // Método para assinar a CCB que vai ser a mesma ccb gerada no PDF com um codigo
    // aleatório simulando uma assinarura digital
    @Transactional
    public byte[] signCCB(LoanProposal proposal) {
        try {
            // Verifica se a proposta e a CCB são válidas

            if (proposal == null || proposal.getCcb() == null || proposal.getCcb().isEmpty()) {
                throw new BadRequestException("Proposta ou CCB inválida");
            }
            if (proposal.getCcb() == null || proposal.getCcb().isEmpty() || proposal.getCcb().equals("N/A")) {
                throw new BadRequestException("CCB não gerada ou inválida");
            }

         
            String codigoCCB = proposal.getCcb();
            if (proposal.getStatus() == LoanProposalStatus.PENDING_SIGNATURE) {
                proposal.setStatus(LoanProposalStatus.SIGNED);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(0, 70, 130));
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            Paragraph title = new Paragraph("CÉDULA DE CRÉDITO BANCÁRIO (CCB) - Nº " + codigoCCB, titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(20);
            doc.add(title);

            // Adiciona o texto inicial
            adicionarTextoInicial(doc, normalFont, sectionTitleFont);

            // Bloco: Dados do Emitente
            Paragraph emitenteTitle = new Paragraph("Dados do Emitente (Cliente)", sectionTitleFont);
            emitenteTitle.setSpacingAfter(5);
            doc.add(emitenteTitle);
            PdfPTable clienteTable = new PdfPTable(2);
            clienteTable.setWidthPercentage(100);
            clienteTable.setSpacingBefore(5);
            clienteTable.setWidths(new int[] { 1, 3 });
            addInfoBlock(clienteTable, "Nome:", proposal.getCustomer().getName() + " (\"EMITENTE\")", boldFont,
                    normalFont);
            addInfoBlock(clienteTable, "CPF:", proposal.getCustomer().getCpf(), boldFont, normalFont);
            addInfoBlock(clienteTable, "Endereço:", proposal.getCustomer().getStreet() + ", "
                    + proposal.getCustomer().getStreetNumber() + " - " + proposal.getCustomer().getNeighborhood(),
                    boldFont, normalFont);
            addInfoBlock(clienteTable, "Cidade:", proposal.getCustomer().getCity(), boldFont, normalFont);
            addInfoBlock(clienteTable, "UF:", proposal.getCustomer().getState(), boldFont, normalFont);
            addInfoBlock(clienteTable, "CEP:", proposal.getCustomer().getZipCode(), boldFont, normalFont);
            addInfoBlock(clienteTable, "Nacionalidade:", "Brasileiro", boldFont, normalFont);
            addInfoBlock(clienteTable, "Data de Nascimento:",
                    proposal.getCustomer().getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), boldFont,
                    normalFont);
            doc.add(clienteTable);
            doc.add(new Paragraph("Doravante denominado apenas EMITENTE.", normalFont));
            doc.add(Chunk.NEWLINE);

            // Bloco: Informações Bancárias
            Paragraph bancoTitle = new Paragraph("Informações Bancárias", sectionTitleFont);
            bancoTitle.setSpacingAfter(5);
            doc.add(bancoTitle);
            PdfPTable bancoTable = new PdfPTable(2);
            bancoTable.setWidthPercentage(100);
            bancoTable.setSpacingBefore(5);
            bancoTable.setWidths(new int[] { 1, 3 });
            addInfoBlock(bancoTable, "Banco:", "CrediFlow Bank", boldFont, normalFont);
            addInfoBlock(bancoTable, "Agência:", proposal.getCustomer().getVirtualAccount().getAgencyNumber(), boldFont,
                    normalFont);
            addInfoBlock(bancoTable, "Conta:", proposal.getCustomer().getVirtualAccount().getAccountNumber(), boldFont,
                    normalFont);
            addInfoBlock(bancoTable, "CPF:", proposal.getCustomer().getCpf(), boldFont, normalFont);
            addInfoBlock(bancoTable, "Tipo de Conta:", "Conta Corrente", boldFont, normalFont);
            addInfoBlock(bancoTable, "Chave Pix:",
                    proposal.getCustomer().getVirtualAccount().getPixKeys().isEmpty() ? "Não Informada"
                            : proposal.getCustomer().getVirtualAccount().getPixKeys().get(0).getKey(),
                    boldFont, normalFont);
            addInfoBlock(bancoTable, "Chave Pix Tipo:",
                    proposal.getCustomer().getVirtualAccount().getPixKeys().isEmpty() ? "Não Informado"
                            : proposal.getCustomer().getVirtualAccount().getPixKeys().get(0).getKeyType().name(),
                    boldFont, normalFont);
            doc.add(bancoTable);
            doc.add(Chunk.NEWLINE);

            // Bloco: Informações da Proposta
            Paragraph propostaTitle = new Paragraph("Informações da Proposta", sectionTitleFont);
            propostaTitle.setSpacingAfter(5);
            doc.add(propostaTitle);
            PdfPTable propostaTable = new PdfPTable(2);
            propostaTable.setWidthPercentage(100);
            propostaTable.setSpacingBefore(5);
            propostaTable.setWidths(new int[] { 1, 3 });
            addInfoBlock(propostaTable, "Cod. da Proposta:", proposal.getId().toString(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Data da Proposta:",
                    proposal.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), boldFont, normalFont);
            addInfoBlock(propostaTable, "Valor Solicitado:", "R$ " + proposal.getRequestedAmount(), boldFont,
                    normalFont);
            addInfoBlock(propostaTable, "Valor da Parcela:", "R$ " + proposal.getInstallmentValue(), boldFont,
                    normalFont);
            addInfoBlock(propostaTable, "Prazo:", proposal.getTermInMonths() + " meses", boldFont, normalFont);
            addInfoBlock(propostaTable, "Taxa de Juros:",
                    proposal.getMonthlyRate().multiply(new BigDecimal("100")) + "%", boldFont, normalFont);
            addInfoBlock(propostaTable, "Data da 1ª Parcela:",
                    proposal.getFirstInstallmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), boldFont,
                    normalFont);
            addInfoBlock(propostaTable, "IOF Total:", "R$ " + proposal.getIofTotal(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Taxa de Juros Efetiva Mensal:",
                    proposal.getEffectiveMonthlyRate().multiply(new BigDecimal("100")) + "%", boldFont, normalFont);
            addInfoBlock(propostaTable, "Taxa de Juros Efetiva Anual:",
                    proposal.getEffectiveAnnualRate().multiply(new BigDecimal("100")) + "%", boldFont, normalFont);
            addInfoBlock(propostaTable, "Taxa de Juros Contratada Anual:",
                    proposal.getContractedAnnualRate().multiply(new BigDecimal("100")) + "%", boldFont, normalFont);
            addInfoBlock(propostaTable, "Tipo de Produto:", proposal.getProductType(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Fundo:", proposal.getFund(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Tipo de Parcela:", proposal.getInstallmentType(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Valor Total do Empréstimo:", "R$ " + proposal.getTotalPayment(), boldFont,
                    normalFont);
            addInfoBlock(propostaTable, "Data de Liberação:",
                    proposal.getDisbursementDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), boldFont,
                    normalFont);
            doc.add(propostaTable);
            doc.add(Chunk.NEWLINE);

            // Tabela de parcelas
            doc.add(new Paragraph("Cronograma de Parcelas", sectionTitleFont));
            doc.add(lineSpacing(-12, 5));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new int[] { 1, 3, 3, 3 });

            Stream.of("Nº", "Vencimento", "Valor", "Saldo").forEach(header -> {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setBackgroundColor(new Color(230, 230, 250));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            });

            for (LoanInstallment i : proposal.getInstallments()) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(i.getNumber()), normalFont)));
                table.addCell(new PdfPCell(
                        new Phrase(i.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont)));
                table.addCell(new PdfPCell(new Phrase("R$ " + i.getValue(), normalFont)));
                table.addCell(new PdfPCell(new Phrase("R$ " + i.getBalance(), normalFont)));
            }

            doc.add(table);
            doc.add(Chunk.NEWLINE);

            // Adiciona as disposições finais
            adicionarDisposicoesFinais(doc, normalFont, sectionTitleFont, proposal);
            // simulação de assinatura digital
            doc.add(new Paragraph("Assinatura Digital: e4eb8c9f-4d3a-4b2e-8c1f-5d3e7f8b9c0d (Simulação)", normalFont));
            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar CCB", e);
        }
    }

    public byte[] getCCBDetails(LoanProposal proposal) {
        try {
            // Verifica se a proposta e a CCB são válidas

            if (proposal == null || proposal.getCcb() == null || proposal.getCcb().isEmpty()) {
                throw new BadRequestException("Proposta ou CCB inválida");
            }
            if (proposal.getCcb() == null || proposal.getCcb().isEmpty() || proposal.getCcb().equals("N/A")) {
                throw new BadRequestException("CCB não gerada ou inválida");
            }

          
            String codigoCCB = proposal.getCcb();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.BLACK);
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(0, 70, 130));
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.DARK_GRAY);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            Paragraph title = new Paragraph("CÉDULA DE CRÉDITO BANCÁRIO (CCB) - Nº " + codigoCCB, titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            title.setSpacingAfter(20);
            doc.add(title);

            // Adiciona o texto inicial
            adicionarTextoInicial(doc, normalFont, sectionTitleFont);

            // Bloco: Dados do Emitente
            Paragraph emitenteTitle = new Paragraph("Dados do Emitente (Cliente)", sectionTitleFont);
            emitenteTitle.setSpacingAfter(5);
            doc.add(emitenteTitle);
            PdfPTable clienteTable = new PdfPTable(2);
            clienteTable.setWidthPercentage(100);
            clienteTable.setSpacingBefore(5);
            clienteTable.setWidths(new int[] { 1, 3 });
            addInfoBlock(clienteTable, "Nome:", proposal.getCustomer().getName() + " (\"EMITENTE\")", boldFont,
                    normalFont);
            addInfoBlock(clienteTable, "CPF:", proposal.getCustomer().getCpf(), boldFont, normalFont);
            addInfoBlock(clienteTable, "Endereço:", proposal.getCustomer().getStreet() + ", "
                    + proposal.getCustomer().getStreetNumber() + " - " + proposal.getCustomer().getNeighborhood(),
                    boldFont, normalFont);
            addInfoBlock(clienteTable, "Cidade:", proposal.getCustomer().getCity(), boldFont, normalFont);
            addInfoBlock(clienteTable, "UF:", proposal.getCustomer().getState(), boldFont, normalFont);
            addInfoBlock(clienteTable, "CEP:", proposal.getCustomer().getZipCode(), boldFont, normalFont);
            addInfoBlock(clienteTable, "Nacionalidade:", "Brasileiro", boldFont, normalFont);
            addInfoBlock(clienteTable, "Data de Nascimento:",
                    proposal.getCustomer().getBirthDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), boldFont,
                    normalFont);
            doc.add(clienteTable);
            doc.add(new Paragraph("Doravante denominado apenas EMITENTE.", normalFont));
            doc.add(Chunk.NEWLINE);

            // Bloco: Informações Bancárias
            Paragraph bancoTitle = new Paragraph("Informações Bancárias", sectionTitleFont);
            bancoTitle.setSpacingAfter(5);
            doc.add(bancoTitle);
            PdfPTable bancoTable = new PdfPTable(2);
            bancoTable.setWidthPercentage(100);
            bancoTable.setSpacingBefore(5);
            bancoTable.setWidths(new int[] { 1, 3 });
            addInfoBlock(bancoTable, "Banco:", "CrediFlow Bank", boldFont, normalFont);
            addInfoBlock(bancoTable, "Agência:", proposal.getCustomer().getVirtualAccount().getAgencyNumber(), boldFont,
                    normalFont);
            addInfoBlock(bancoTable, "Conta:", proposal.getCustomer().getVirtualAccount().getAccountNumber(), boldFont,
                    normalFont);
            addInfoBlock(bancoTable, "CPF:", proposal.getCustomer().getCpf(), boldFont, normalFont);
            addInfoBlock(bancoTable, "Tipo de Conta:", "Conta Corrente", boldFont, normalFont);
            addInfoBlock(bancoTable, "Chave Pix Tipo:",
                    proposal.getCustomer().getVirtualAccount().getPixKeys().isEmpty() ? "Não Informado"
                            : proposal.getCustomer().getVirtualAccount().getPixKeys().get(0).getKeyType().name(),
                    boldFont, normalFont);
            addInfoBlock(bancoTable, "Chave Pix Tipo:",
                    proposal.getCustomer().getVirtualAccount().getPixKeys().isEmpty() ? "Não Informado"
                            : proposal.getCustomer().getVirtualAccount().getPixKeys().get(0).getKeyType().name(),
                    boldFont, normalFont);
            doc.add(bancoTable);
            doc.add(Chunk.NEWLINE);

            // Bloco: Informações da Proposta
            Paragraph propostaTitle = new Paragraph("Informações da Proposta", sectionTitleFont);
            propostaTitle.setSpacingAfter(5);
            doc.add(propostaTitle);
            PdfPTable propostaTable = new PdfPTable(2);
            propostaTable.setWidthPercentage(100);
            propostaTable.setSpacingBefore(5);
            propostaTable.setWidths(new int[] { 1, 3 });
            addInfoBlock(propostaTable, "Cod. da Proposta:", proposal.getId().toString(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Data da Proposta:",
                    proposal.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), boldFont, normalFont);
            addInfoBlock(propostaTable, "Valor Solicitado:", "R$ " + proposal.getRequestedAmount(), boldFont,
                    normalFont);
            addInfoBlock(propostaTable, "Valor da Parcela:", "R$ " + proposal.getInstallmentValue(), boldFont,
                    normalFont);
            addInfoBlock(propostaTable, "Prazo:", proposal.getTermInMonths() + " meses", boldFont, normalFont);
            addInfoBlock(propostaTable, "Taxa de Juros:",
                    proposal.getMonthlyRate().multiply(new BigDecimal("100")) + "%", boldFont, normalFont);
            addInfoBlock(propostaTable, "Data da 1ª Parcela:",
                    proposal.getFirstInstallmentDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), boldFont,
                    normalFont);
            addInfoBlock(propostaTable, "IOF Total:", "R$ " + proposal.getIofTotal(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Taxa de Juros Efetiva Mensal:",
                    proposal.getEffectiveMonthlyRate().multiply(new BigDecimal("100")) + "%", boldFont, normalFont);
            addInfoBlock(propostaTable, "Taxa de Juros Efetiva Anual:",
                    proposal.getEffectiveAnnualRate().multiply(new BigDecimal("100")) + "%", boldFont, normalFont);
            addInfoBlock(propostaTable, "Taxa de Juros Contratada Anual:",
                    proposal.getContractedAnnualRate().multiply(new BigDecimal("100")) + "%", boldFont, normalFont);
            addInfoBlock(propostaTable, "Tipo de Produto:", proposal.getProductType(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Fundo:", proposal.getFund(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Tipo de Parcela:", proposal.getInstallmentType(), boldFont, normalFont);
            addInfoBlock(propostaTable, "Valor Total do Empréstimo:", "R$ " + proposal.getTotalPayment(), boldFont,
                    normalFont);
            addInfoBlock(propostaTable, "Data de Liberação:",
                    proposal.getDisbursementDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), boldFont,
                    normalFont);
            doc.add(propostaTable);
            doc.add(Chunk.NEWLINE);

            // Tabela de parcelas
            doc.add(new Paragraph("Cronograma de Parcelas", sectionTitleFont));
            doc.add(lineSpacing(-12, 5));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new int[] { 1, 3, 3, 3 });

            Stream.of("Nº", "Vencimento", "Valor", "Saldo").forEach(header -> {
                PdfPCell cell = new PdfPCell(new Phrase(header, boldFont));
                cell.setBackgroundColor(new Color(230, 230, 250));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cell);
            });

            for (LoanInstallment i : proposal.getInstallments()) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(i.getNumber()), normalFont)));
                table.addCell(new PdfPCell(
                        new Phrase(i.getDueDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), normalFont)));
                table.addCell(new PdfPCell(new Phrase("R$ " + i.getValue(), normalFont)));
                table.addCell(new PdfPCell(new Phrase("R$ " + i.getBalance(), normalFont)));
            }

            doc.add(table);
            doc.add(Chunk.NEWLINE);

            // Adiciona as disposições finais
            adicionarDisposicoesFinais(doc, normalFont, sectionTitleFont, proposal);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar CCB", e);
        }
    }

}
