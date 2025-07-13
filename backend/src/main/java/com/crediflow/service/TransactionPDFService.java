package com.crediflow.service;

import java.io.ByteArrayOutputStream;

import com.crediflow.entity.Transaction;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TransactionPDFService {

    public byte[] generateReceipt(Transaction tx) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            doc.add(new Paragraph("Comprovante de Transação", titleFont));
            doc.add(new Paragraph(" "));

            doc.add(new Paragraph("ID da Transação: " + tx.getId(), textFont));
            doc.add(new Paragraph("Data: " + tx.getCreatedAt().toString(), textFont));
            doc.add(new Paragraph("Tipo: " + tx.getType(), textFont));
            doc.add(new Paragraph("Valor: R$ " + tx.getAmount().toString(), textFont));
            doc.add(new Paragraph("Conta Pagadora (ID): " + (tx.getFromAccount() != null ? tx.getFromAccount().getId() : "N/A"), textFont));
            doc.add(new Paragraph("Conta Destino (ID): " + (tx.getToAccount() != null ? tx.getToAccount().getId() : "N/A"), textFont));
            doc.add(new Paragraph("Status: " + tx.getStatus(), textFont));
            doc.add(new Paragraph("Descrição: " + tx.getDescription(), textFont));

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar comprovante", e);
        }
    }
}

