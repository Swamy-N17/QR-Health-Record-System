package com.clinic.qrhealthrecord.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Component;

import com.clinic.qrhealthrecord.entity.Patient;

@Component
public class HealthCardGenerator {

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 720;
    private static final int QR_SIZE = 300;

    private final QrCodeGenerator qrCodeGenerator;

    public HealthCardGenerator(QrCodeGenerator qrCodeGenerator) {
        this.qrCodeGenerator = qrCodeGenerator;
    }

    public byte[] generateHealthCard(Patient patient) throws Exception {
        BufferedImage card = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = card.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(new Color(239, 248, 247));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Main card
        g.setColor(Color.WHITE);
        g.fillRoundRect(55, 45, WIDTH - 110, HEIGHT - 90, 28, 28);
        g.setColor(new Color(195, 225, 222));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(55, 45, WIDTH - 110, HEIGHT - 90, 28, 28);

        // Heading
        g.setColor(new Color(13, 148, 136));
        g.fillRoundRect(90, 88, 8, 58, 4, 4);
        g.setColor(new Color(24, 59, 69));
        g.setFont(new Font("SansSerif", Font.BOLD, 34));
        g.drawString("QR HEALTH RECORD", 120, 125);

        g.setColor(new Color(124, 146, 151));
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        g.drawString("Patient Health Card", 122, 151);

        // Divider
        g.setColor(new Color(220, 235, 234));
        g.fillRect(90, 178, WIDTH - 180, 2);

        // Left information area
        int x = 120;
        int y = 225;
        int labelWidth = 145;
        int rowGap = 45;

        drawField(g, "Name", safe(patient.getFullName()), x, y, labelWidth);
        drawField(g, "Patient Code", safe(patient.getPatientCode()), x, y += rowGap, labelWidth);
        drawField(g, "Age", patient.getAge() == null ? "Not provided" : patient.getAge() + " years", x, y += rowGap, labelWidth);
        drawField(g, "Date of Birth", formatDate(patient.getDateOfBirth()), x, y += rowGap, labelWidth);
        drawField(g, "Gender", safe(patient.getGender()), x, y += rowGap, labelWidth);
        drawField(g, "Blood Group", safe(patient.getBloodGroup()), x, y += rowGap, labelWidth);
        drawField(g, "Phone", safe(patient.getPhoneNumber()), x, y += rowGap, labelWidth);
        drawField(g, "Emergency", safe(patient.getEmergencyContact()), x, y += rowGap, labelWidth);

        // Address gets a little more room because it can be long.
        int addressY = y + rowGap;
        g.setColor(new Color(112, 133, 139));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString("Address", x, addressY);
        g.setColor(new Color(52, 78, 86));
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        drawWrappedText(g, safe(patient.getAddress()), x + labelWidth, addressY, 390, 21, 2);

        // QR area
        int qrPanelX = 790;
        int qrPanelY = 220;
        int qrPanelSize = 330;

        g.setColor(new Color(241, 250, 249));
        g.fillRoundRect(qrPanelX, qrPanelY, qrPanelSize, qrPanelSize + 55, 20, 20);
        g.setColor(new Color(198, 225, 222));
        g.drawRoundRect(qrPanelX, qrPanelY, qrPanelSize, qrPanelSize + 55, 20, 20);

        byte[] qrBytes = qrCodeGenerator.generateQrCodeBytes(patient.getPatientCode());
        BufferedImage qr = ImageIO.read(new java.io.ByteArrayInputStream(qrBytes));
        g.drawImage(qr, qrPanelX + 15, qrPanelY + 15, QR_SIZE, QR_SIZE, null);

        g.setColor(new Color(24, 59, 69));
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        String code = safe(patient.getPatientCode());
        int codeWidth = g.getFontMetrics().stringWidth(code);
        g.drawString(code, qrPanelX + (qrPanelSize - codeWidth) / 2, qrPanelY + 350);

        g.setColor(new Color(128, 148, 153));
        g.setFont(new Font("SansSerif", Font.PLAIN, 12));
        String hint = "Scan to identify patient";
        int hintWidth = g.getFontMetrics().stringWidth(hint);
        g.drawString(hint, qrPanelX + (qrPanelSize - hintWidth) / 2, qrPanelY + 372);

        g.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(card, "PNG", output);
        return output.toByteArray();
    }

    private void drawField(Graphics2D g, String label, String value, int x, int y, int labelWidth) {
        g.setColor(new Color(112, 133, 139));
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        g.drawString(label, x, y);

        g.setColor(new Color(52, 78, 86));
        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        drawWrappedText(g, value, x + labelWidth, y, 390, 20, 1);
    }

    private void drawWrappedText(Graphics2D g, String text, int x, int y, int maxWidth, int lineHeight, int maxLines) {
        if (text == null || text.isBlank()) {
            text = "Not provided";
        }

        String[] words = text.trim().split("\\s+");
        StringBuilder line = new StringBuilder();
        int lines = 0;

        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (g.getFontMetrics().stringWidth(candidate) <= maxWidth) {
                line.setLength(0);
                line.append(candidate);
            } else {
                if (lines >= maxLines) {
                    return;
                }
                g.drawString(line.toString(), x, y + lines * lineHeight);
                lines++;
                line.setLength(0);
                line.append(word);
            }
        }

        if (lines < maxLines && line.length() > 0) {
            g.drawString(line.toString(), x, y + lines * lineHeight);
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "Not provided" : value;
    }

    private String formatDate(LocalDate date) {
        return date == null ? "Not provided" : date.toString();
    }
}
