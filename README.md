<div align="center">

  <img src="icon.png" alt="FileFlow Icon" width="128" height="128" style="border-radius: 28px;" />

  # FileFlow

  **Lightweight, 100% Offline and Privacy-First Document, Image, and Barcode Suite for Android**

  <p align="center">
    <a href="https://github.com/zer0k7/FileFlow/releases/latest">
      <img src="https://img.shields.io/badge/Download_APK-GitHub_Release-0284C7?style=for-the-badge&logo=github&logoColor=white" alt="Download APK" />
    </a>
    &nbsp;&nbsp;
    <a href="https://obtainium.imranr.dev/add?url=https://github.com/zer0k7/FileFlow">
      <img src="https://img.shields.io/badge/Get_it_on-Obtainium-7C3AED?style=for-the-badge&logo=android&logoColor=white" alt="Get on Obtainium" />
    </a>
  </p>

  <p align="center">
    <img src="https://img.shields.io/badge/Android-8.0+_(API_26+)-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Android Version" />
    <img src="https://img.shields.io/badge/Architecture-arm64--v8a-4F46E5?style=flat-square" alt="Architecture" />
    <img src="https://img.shields.io/badge/Interface-Material_3-0284C7?style=flat-square" alt="Material 3" />
    <img src="https://img.shields.io/badge/Privacy-100%25_Offline-16A34A?style=flat-square" alt="100% Offline" />
    <img src="https://img.shields.io/badge/License-Apache_2.0-2563EB?style=flat-square" alt="License" />
  </p>

</div>

---

## Overview

FileFlow is a modern, offline-first Android utility application designed for document conversion, PDF manipulation, photo optimization, metadata inspection, and barcode scanning.

Every operation runs completely on-device. No files or media are transmitted to external servers, no account registration is required, and no analytics or tracking dependencies are bundled.

> [!NOTE]
> FileFlow operates without internet dependencies for processing. Network connectivity is used exclusively to check for application updates when requested.

---

## Download and Installation

### Option 1: Standalone APK
Download the signed 64-bit release APK directly from GitHub Releases:
- [Download Latest Release](https://github.com/zer0k7/FileFlow/releases/latest)

### Option 2: Automated Tracking with Obtainium
Add FileFlow directly to Obtainium for background update notifications:

<div align="center">
  <a href="https://obtainium.imranr.dev/add?url=https://github.com/zer0k7/FileFlow">
    <img src="https://img.shields.io/badge/Add_to-Obtainium-7C3AED?style=for-the-badge&logo=android&logoColor=white" alt="Add to Obtainium" />
  </a>
</div>

### Option 3: In-App Auto-Updater
FileFlow includes an integrated release updater. You can check for updates directly from Settings and install updates with a single tap.

---

## Offline Tool Suites

FileFlow organizes its utilities into clean categories accessible from the main interface:

### 1. QR Code and Barcode Suite

| Tool | Capability | Input &rarr; Output |
|---|---|---|
| **QR and Barcode Scanner** | Offline decoding of QR codes, Code 128, Code 39, EAN-13, EAN-8, UPC-A, UPC-E, and Data Matrix from camera or image. Features smart action handlers for Wi-Fi credentials, UPI payments, vCard contacts, and web links. | `Camera / Image` &rarr; `Decoded Content / Intent` |
| **QR Code Generator** | Live interactive QR styling with custom foreground colors, error correction levels (L, M, Q, H), and multi-payload support (Wi-Fi, UPI, Contact, URL, Text). | `Parameters` &rarr; `High-Resolution PNG` |

### 2. Image and Photo Utilities

| Tool | Capability | Input &rarr; Output |
|---|---|---|
| **Image Format Converter** | Native conversion between JPG, PNG, WebP, and HEIC with configurable quality compression. | `Images` &rarr; `JPG / PNG / WebP` |
| **EXIF Metadata Stripper** | Complete inspection of camera hardware, exposure settings, timestamps, and GPS coordinates. Offers 100% clean metadata eradication via bitmap re-encoding or selective GPS stripping. | `Images` &rarr; `Sanitized Images` |
| **Image Resizer and Target KB** | Scale images by dimensions (px), percentage (10% to 200%), or strict target file size (e.g., Under 100 KB, 200 KB, 500 KB) using iterative binary search optimization. | `Images` &rarr; `Resized Images` |
| **Color Palette Extractor** | Extract dominant aesthetic colors with exact Hex codes, RGB values, and distribution percentages. Includes one-tap copying and exportable PNG palette cards. | `Image` &rarr; `Palette Card PNG` |
| **Image Compressor** | Downsampling, custom quality slider, and lossy/lossless compression modes. | `Images` &rarr; `Compressed Images` |

### 3. PDF Document Tools

| Tool | Capability | Input &rarr; Output |
|---|---|---|
| **Image to PDF** | Combine multi-page photos and scans into a PDF with custom paper sizes (A4, Letter, Legal) and auto-orientation. | `Images` &rarr; `PDF` |
| **PDF to Images** | Hardware-accelerated rendering and extraction of document pages into high-resolution JPG, PNG, or WebP formats. | `PDF` &rarr; `JPG / PNG / WebP` |
| **PDF to DOCX** | Paragraph extraction converting PDF layouts into editable Microsoft Word (.docx) documents. | `PDF` &rarr; `DOCX` |
| **DOCX to PDF** | Offline OpenXML rendering engine formatting Word documents into paginated PDF files. | `DOCX` &rarr; `PDF` |
| **PDF Compressor** | Multi-level raster and stream optimization (Extreme, Recommended, Light) to reduce file sizes. | `PDF` &rarr; `Compressed PDF` |
| **PDF Password Protect** | Encrypt and secure PDF files with 128-bit AES encryption. | `PDF` &rarr; `Encrypted PDF` |
| **PDF Password Remover** | Decrypt and remove passwords when known. Passwords are never retained or logged. | `Encrypted PDF` &rarr; `Unlocked PDF` |
| **PDF Merge** | Concatenate multiple PDF files into a single unified document. | `Multiple PDFs` &rarr; `Single PDF` |
| **PDF Split and Extract** | Extract specific page ranges or burst all pages into standalone documents. | `PDF` &rarr; `Split PDFs` |
| **PDF Page Rotate** | Rotate document pages clockwise by 90°, 180°, or 270°. | `PDF` &rarr; `Rotated PDF` |
| **Extract Text from PDF** | Export selectable text content into clean plain text documents. | `PDF` &rarr; `TXT` |
| **PDF Watermark** | Stamp custom text watermarks across pages with adjustable opacity. | `PDF` &rarr; `Watermarked PDF` |
| **PDF Metadata Editor** | Update title, author, subject, and keywords without altering document streams. | `PDF` &rarr; `Updated PDF` |
| **PDF Sign and Stamp** | Drawing canvas for signatures and predefined official status stamps. | `PDF` &rarr; `Signed PDF` |

### 4. Scanner and OCR

| Tool | Capability | Input &rarr; Output |
|---|---|---|
| **Document Scanner** | Perspective-enhanced document capture with Magic Color, B&W, and Grayscale filters. | `Camera / Images` &rarr; `PDF` |
| **OCR Text Extractor** | On-device machine learning text recognition from scanned documents and images. | `Images` &rarr; `Searchable PDF / TXT` |

### 5. Security and Threat Intelligence

| Tool | Capability | Input &rarr; Output |
|---|---|---|
| **VirusTotal & Malware Scanner** | Multi-engine malware detection for APKs, PDFs, images, archives, and files using VirusTotal (70+ Antivirus engines), MalwareBazaar by Abuse.ch (100% Free & Open), and Hybrid Analysis Falcon Sandbox. Includes streaming SHA-256/MD5 hashing and detailed threat scorecards. | `Any File` &rarr; `Security Report / Hashes` |

---

## Technical Specifications

| Parameter | Detail |
|---|---|
| **Target ABI** | `arm64-v8a` (Modern 64-bit Android) |
| **Minimum SDK** | Android 8.0 (API 26) |
| **Target SDK** | Android 15 (API 35) |
| **Architecture** | Kotlin, Jetpack Compose, Coroutines, StateFlow |
| **Storage Model** | Storage Access Framework (SAF) with persistent directory permissions |
| **Telemetry** | None (Zero network calls during file processing) |

---

## Privacy and Permissions

FileFlow requests only the permissions necessary for local operation:

| Permission | Justification |
|---|---|
| `INTERNET` | Used solely to check for GitHub release updates when triggered by the user. |
| `CAMERA` | Requested at runtime only when capturing photos inside Document Scanner or Barcode Scanner. |
| `VIBRATE` | Provides tactile haptic feedback for user interactions. |
| `REQUEST_INSTALL_PACKAGES` | Allows the in-app updater to pass downloaded APKs to the Android system package installer. |

---

## License

FileFlow is distributed under the [Apache License 2.0](LICENSE).
