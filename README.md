<div align="center">
  <img src="icon.png" width="120" height="120" alt="FileFlow Logo" />
  <h1>FileFlow</h1>
  <p><strong>Fast, lightweight, privacy-first Android utility for document, PDF, and image operations.</strong></p>
  
  <p>
    <a href="#features">Features</a> •
    <a href="#core-tools">Core Tools</a> •
    <a href="#privacy--security">Privacy & Security</a> •
    <a href="#building-from-source">Building</a> •
    <a href="#architecture">Architecture</a> •
    <a href="#license">License</a>
  </p>
</div>

---

## Overview

**FileFlow** is a free and open-source Android application designed for common PDF, document, and image workflows. All file operations run locally on your device without server uploads, accounts, advertising, or telemetry.

## Core Tools

1. **Image → PDF**: Convert pictures and gallery photos into multi-page PDF documents with customizable page sizes (A4, Letter, Original) and orientations.
2. **PDF → Images**: Extract pages as high-resolution JPG, PNG, or WebP images.
3. **PDF → DOCX**: Convert PDF documents into editable Microsoft Word (.docx) documents offline.
4. **DOCX → PDF**: Render Word documents into clean, paginated PDF files.
5. **PDF Compressor**: Optimize PDF file size with Extreme, Recommended, and Light compression profiles.
6. **PDF Password Remover**: Decrypt and unlock password-protected PDF files when the password is known.
7. **PDF Merge**: Combine multiple PDF documents into a single document in any order.
8. **PDF Split / Page Extract**: Extract specific page ranges (e.g. `1-3, 5`) or burst into individual single-page documents.
9. **Image Compressor**: Reduce image file size with quality percentage sliders and format conversion.
10. **Document Scanner**: Capture documents, apply Magic Color, B&W Clean, and Grayscale filters, and export to PDF.

## Key Features

- ⚡ **100% Offline Processing**: Zero cloud dependencies or remote file transmissions.
- 🎨 **Modern Material 3 Design**: Features floating top bar, floating bottom navigation, and smooth micro-interactions.
- 🌓 **Themes & Accents**: System, Light, Dark, and AMOLED Black themes with 14+ accent colors and a custom hex picker.
- 📁 **Storage Access Framework (SAF)**: Persisted default folder selection, customizable filename templates, and collision prevention.
- 🔒 **Privacy First**: No telemetry, no tracking SDKs, no ad networks, no analytics.

## Technology Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose + Material 3
- **Architecture:** ViewModel + Coroutines + Flow
- **Data Persistence:** AndroidX DataStore Preferences
- **Document Engines:** Native Android `PdfRenderer` / `PdfDocument` & Apache PDFBox-Android

## Building from Source

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 35 (minSdk 26)

### Build Commands

```bash
# Clone the repository
git clone https://github.com/your-username/FileFlow.git
cd FileFlow

# Run tests
./gradlew test

# Build debug APK
./gradlew assembleDebug

# Build release APKs (produces arm64-v8a and armeabi-v7a splits)
./gradlew assembleRelease
```

## Releases & CI/CD

Release builds produce two optimized ABI APKs:
- `FileFlow-V<version>-arm64-v8a.apk`
- `FileFlow-V<version>-armeabi-v7a.apk`

Publishing is automated via GitHub Actions when a version tag such as `v1.0.0` is pushed.

## License

FileFlow is licensed under the [Apache License 2.0](LICENSE).
