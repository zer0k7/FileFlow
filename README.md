<div align="center">

  <img src="icon.png" alt="FileFlow Icon" width="128" height="128" style="border-radius: 28px;" />

  # FileFlow

  **Lightweight, 100% Offline & Privacy-First Document & Image Suite for Android**

  <p align="center">
    <a href="https://github.com/zer0k7/FileFlow/releases/latest">
      <img src="https://img.shields.io/github/v/release/zer0k7/FileFlow?style=for-the-badge&label=Download%20APK&logo=android&logoColor=white&color=0284C7" alt="Download Latest APK" height="42" />
    </a>
    &nbsp;&nbsp;
    <a href="obtainium://add/https://github.com/zer0k7/FileFlow">
      <img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="42" />
    </a>
  </p>

  <p align="center">
    <img src="https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-3DDC84?style=flat-square&logo=android&logoColor=white" alt="Platform" />
    <img src="https://img.shields.io/badge/Target_SDK-35-0F172A?style=flat-square" alt="Target SDK" />
    <img src="https://img.shields.io/badge/Architecture-arm64--v8a-4F46E5?style=flat-square" alt="Architecture" />
    <img src="https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-0284C7?style=flat-square" alt="UI" />
    <img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=flat-square" alt="License" />
    <img src="https://img.shields.io/badge/Telemetry-Zero-16A34A?style=flat-square" alt="Zero Telemetry" />
  </p>

</div>

---

## ⚡ What is FileFlow?

**FileFlow** is a modern, privacy-respecting Android utility application for document conversion, PDF manipulation, image optimization, and document scanning.

All operations run **100% locally on your phone's hardware**. No documents or photos are ever uploaded to remote servers, no user accounts are required, and no tracking or telemetry libraries exist in the code.

---

## 📥 Download & Installation

### Option 1: Direct APK Download
Download the latest standalone `.apk` directly from GitHub Releases:
- 👉 **[Download Latest APK (GitHub Releases)](https://github.com/zer0k7/FileFlow/releases/latest)**

### Option 2: Automatic Updates with Obtainium
If you use [Obtainium](https://github.com/ImranR98/Obtainium), tap the button below to add FileFlow for seamless background update notifications:

<div align="center">
  <a href="obtainium://add/https://github.com/zer0k7/FileFlow">
    <img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="52" />
  </a>
</div>

### Option 3: Built-In In-App Auto Updater
FileFlow includes a built-in release updater. Once installed, the app can automatically detect newer GitHub releases, display full release notes, and install updates with a single tap.

---

## 🛠️ 14 Core Offline Tools

FileFlow includes 14 specialized processing engines:

### 📄 PDF Tools
| Tool | Description | Input &rarr; Output |
|---|---|---|
| **Image to PDF** | Combine single or multi-page photos and scans into a PDF with custom paper size (A4, Letter, Legal) and auto-orientation. | `Images` &rarr; `PDF` |
| **PDF to Images** | Hardware-accelerated extraction of PDF pages into high-resolution JPG, PNG, or WebP images. | `PDF` &rarr; `JPG / PNG / WebP` |
| **PDF to DOCX** | Offline paragraph extraction that converts PDF documents into editable Microsoft Word (.docx) format. | `PDF` &rarr; `DOCX` |
| **DOCX to PDF** | Pure offline OpenXML renderer that formats Word documents into clean, paginated PDFs. | `DOCX` &rarr; `PDF` |
| **PDF Compressor** | Multi-level raster and stream optimization (Extreme, Recommended, Light) to significantly shrink file sizes. | `PDF` &rarr; `Compressed PDF` |
| **PDF Password Protect** | Lock and encrypt any PDF using standard 128-bit AES encryption with your custom password. | `PDF` &rarr; `Encrypted PDF` |
| **PDF Password Remover** | Decrypt and remove owner/user passwords when known. Passwords are never logged or stored. | `Encrypted PDF` &rarr; `Unlocked PDF` |
| **PDF Merge** | Join multiple PDF documents together in any custom order. | `Multiple PDFs` &rarr; `Single PDF` |
| **PDF Split / Extract** | Extract specific page ranges (e.g. `1-3, 5, 8`) or burst all pages into standalone individual files. | `PDF` &rarr; `Split PDFs` |
| **PDF Page Rotate** | Rotate all document pages clockwise by 90°, 180°, or 270° to fix sideways or upside-down scans. | `PDF` &rarr; `Rotated PDF` |
| **Extract Text from PDF** | Extract all selectable plain text content into a clean `.txt` document without OCR bloat. | `PDF` &rarr; `TXT` |
| **PDF Watermark** | Stamp custom translucent text watermarks across all document pages with adjustable opacity. | `PDF` &rarr; `Watermarked PDF` |

### 🖼️ Image & Scanner Tools
| Tool | Description | Input &rarr; Output |
|---|---|---|
| **Image Compressor** | Downsample dimensions, adjust quality percentage, and convert formats (JPG, PNG, WebP). | `Images` &rarr; `Compressed Images` |
| **Document Scanner** | Capture physical documents with contrast enhancements, Magic Color, and B&amp;W document filters. | `Camera / Images` &rarr; `PDF` |

---

## ✨ Features & Highlights

- 🔒 **100% Offline by Design**: Works completely without internet connection. Zero tracking, zero telemetry, zero analytics.
- 🎨 **Modern Material 3 Interface**: Floating top and bottom navigation bars, balanced spacing, and high-contrast AMOLED Black theme.
- 🌈 **Dynamic & Custom Accents**: Choose from 14 curated color accents or generate Material 3 schemes with custom hex colors.
- 💾 **Storage Access Framework**: Choose your default save folder once; processed documents save directly to your storage without permissions clutter.
- ⚡ **In-App Release Updater**: Check for updates anytime from Settings or get notified when a new version is released.
- 📦 **Ultra Lightweight**: Optimized native 64-bit binaries without unnecessary cloud SDK bloat.

---

## 🔒 Privacy & Permissions

FileFlow requests only the absolute minimum permissions required for operation:

| Permission | Purpose |
|---|---|
| `INTERNET` | Only used when you check for app updates from GitHub Releases or download update APKs. No data is sent out. |
| `CAMERA` | Optional. Only requested when taking document photos directly inside the Document Scanner. |
| `REQUEST_INSTALL_PACKAGES` | Allows the in-app updater to prompt the native Android package installer when updating. |

---

## 📄 License

FileFlow is open-source software licensed under the **[Apache License 2.0](LICENSE)**.
