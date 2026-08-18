#!/usr/bin/env python3
import json
import os
import sys
import hashlib

def get_sha256(filepath):
    if not os.path.exists(filepath):
        return "N/A"
    sha = hashlib.sha256()
    with open(filepath, "rb") as f:
        while chunk := f.read(8192):
            sha.update(chunk)
    return sha.hexdigest()

def get_filesize(filepath):
    if not os.path.exists(filepath):
        return "N/A"
    size_bytes = os.path.getsize(filepath)
    if size_bytes >= 1024 * 1024:
        return f"{size_bytes / (1024 * 1024):.2f} MB"
    elif size_bytes >= 1024:
        return f"{size_bytes / 1024:.2f} KB"
    return f"{size_bytes} B"

def main():
    version = os.environ.get("VERSION", "").lstrip("v")
    apk_path = os.environ.get("APK_DEST", "")
    aab_path = os.environ.get("AAB_DEST", "")
    changelog_path = os.path.join("app", "src", "main", "assets", "changelog.json")

    entry = None
    if os.path.exists(changelog_path):
        try:
            with open(changelog_path, "r", encoding="utf-8") as f:
                changelog_data = json.load(f)
                # Find matching version or default to first
                for item in changelog_data:
                    if item.get("version") == version:
                        entry = item
                        break
                if not entry and changelog_data:
                    entry = changelog_data[0]
        except Exception as e:
            print(f"Warning: Failed to load changelog: {e}", file=sys.stderr)

    apk_name = os.path.basename(apk_path) if apk_path else f"FileFlow-v{version}.apk"
    aab_name = os.path.basename(aab_path) if aab_path else f"FileFlow-v{version}.aab"
    apk_sha = get_sha256(apk_path)
    aab_sha = get_sha256(aab_path)
    apk_size = get_filesize(apk_path)
    aab_size = get_filesize(aab_path)

    release_date = entry.get("releaseDate", "Latest") if entry else "Latest"
    
    notes = []
    notes.append(f"## 📱 FileFlow v{version} ({release_date})\n")
    notes.append("> **100% Offline • Privacy-First • No Telemetry • Native Material 3**\n")

    if entry:
        added = entry.get("added", [])
        if added:
            notes.append("### ✨ What's New")
            for item in added:
                notes.append(f"- {item}")
            notes.append("")

        changed = entry.get("changed", [])
        if changed:
            notes.append("### ⚡ Improvements & Changes")
            for item in changed:
                notes.append(f"- {item}")
            notes.append("")

        fixed = entry.get("fixed", [])
        if fixed:
            notes.append("### 🐛 Fixes & Stability")
            for item in fixed:
                notes.append(f"- {item}")
            notes.append("")

        security = entry.get("security", [])
        if security:
            notes.append("### 🔒 Security & Privacy")
            for item in security:
                notes.append(f"- {item}")
            notes.append("")

    notes.append("### 📦 Package & Build Details")
    notes.append("| Attribute | Value |")
    notes.append("|---|---|")
    notes.append(f"| **Package Name** | `com.fileflow.app` |")
    notes.append(f"| **Target ABI** | `arm64-v8a` (Modern 64-bit Android) |")
    notes.append(f"| **Minimum Android** | Android 8.0+ (API 26+) |")
    notes.append(f"| **Target Android** | Android 15 (API 35) |")
    notes.append(f"| **APK Size** | `{apk_size}` |")
    notes.append(f"| **AAB Size** | `{aab_size}` |\n")

    notes.append("### 🔐 Checksums (SHA-256)")
    notes.append("```text")
    notes.append(f"{apk_sha}  {apk_name}")
    notes.append(f"{aab_sha}  {aab_name}")
    notes.append("```\n")

    output_content = "\n".join(notes)
    with open("release-notes.md", "w", encoding="utf-8") as f:
        f.write(output_content)

    print("Generated release-notes.md successfully:")
    print(output_content)

if __name__ == "__main__":
    main()
