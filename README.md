# Sabzazaar-Smart-Agricultural-Assistance-Platform

Sabzazaar is a farmer-friendly app that uses AI and computer vision to detect crop diseases from leaf photos and provide clear, practical advice. It includes treatment steps, fertilizer tips, weather advisories, soil guidance, and expert support, built for simple, offline-first use in rural farming communities.

## Backend OCR Requirements (Soil Report Parsing)

Install these Python packages for local OCR:

- pytesseract
- pdf2image
- pdfplumber
- Pillow
- opencv-python (optional, improves OCR)

System dependencies (Windows):

- Tesseract OCR (add to PATH)
- Poppler (for PDF to image conversion, add to PATH)

Soil report upload field name:

- "soil report"
