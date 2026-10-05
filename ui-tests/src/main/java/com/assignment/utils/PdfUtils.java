package com.assignment.utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Utility for parsing and extracting content from PDF documents (e.g. order receipts).
 */
public final class PdfUtils {

    private PdfUtils() {
    }

    /**
     * Extracts full plain text content from a given PDF file path.
     *
     * @param pdfPath path to the PDF file
     * @return extracted text content
     */
    public static String extractText(Path pdfPath) {
        return extractText(pdfPath.toFile());
    }

    /**
     * Extracts full plain text content from a given PDF file.
     *
     * @param pdfFile the PDF file
     * @return extracted text content
     */
    public static String extractText(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read PDF file: " + pdfFile.getAbsolutePath(), e);
        }
    }
}

