# Changelog

All notable changes to **FileFlow** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-08-17

### Added
- Image → PDF converter with custom page sizes (A4, Letter, Original) and orientations.
- PDF → Images extractor supporting JPG, PNG, and WebP output formats.
- Offline PDF → DOCX converter for extracting text layouts into Microsoft Word documents.
- DOCX → PDF converter rendering styled paragraphs and runs to PDF canvas.
- PDF Compressor with Extreme, Recommended, and Light compression profiles.
- PDF Password Remover for decrypting password-protected PDF files when the password is known.
- PDF Merge utility for combining multiple PDF files into one document.
- PDF Split & Extract tool supporting page ranges (e.g. `1-3, 5`) and individual page splitting.
- Image Compressor with adjustable quality slider and format conversions.
- Document Scanner with perspective enhancement and Magic Color, B&W Clean, and Grayscale filters.
- Material 3 user interface with floating top bar and floating bottom navigation bar.
- System, Light, Dark, and AMOLED Black themes.
- 14+ Accent color themes including custom color picker.
- Storage Access Framework (SAF) integration with default save folder persistence and conflict resolution.
- Processing History screen with file sizes, timestamps, and quick action buttons.
- In-app Changelog viewer and comprehensive Settings screen.

### Security
- 100% offline-first architecture: zero cloud uploads, zero telemetry, zero analytics.
- Passwords are never logged, persisted, or exported.
