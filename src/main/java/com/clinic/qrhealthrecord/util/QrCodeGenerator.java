package com.clinic.qrhealthrecord.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class QrCodeGenerator {

    private static final int QR_IMAGE_SIZE = 300;

    /**
     * Generates a QR code image entirely in memory and returns it as raw PNG bytes.
     * Nothing is written to disk — this makes QR generation safe on hosts with
     * ephemeral/non-persistent filesystems (Render, Railway, etc.), since the
     * image is rebuilt fresh from the patient code every time it's requested.
     */
    public byte[] generateQrCodeBytes(String content) throws WriterException, IOException {

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, QR_IMAGE_SIZE, QR_IMAGE_SIZE);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return outputStream.toByteArray();
    }
}