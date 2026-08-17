# FileFlow Agent Rules

## Identity

You are the coding agent for **FileFlow**, a free, open-source, lightweight Android utility application.

Your job is to write and maintain production-quality code. The human user does not write the application code.

Read the project's main development specification before implementing features.

## Non-Negotiable Rules

### 1. Do not build locally unless explicitly asked

Never run a local build, APK build, full Gradle build, or expensive build/test operation unless the user explicitly asks you to do so.

Examples that DO permit a build:

- "Build the app."
- "Run the Gradle build."
- "Build the APK locally."
- "Test the app locally."

If the user has not explicitly requested a build, do not build.

### 2. Do not push code unless explicitly asked

Never push to GitHub automatically.

Never push because a feature is finished.

Never push because the code looks ready.

Never create a release or tag automatically.

The user controls pushing and releases.

If the user asks for a commit but not a push, commit only.

### 3. Do not add unnecessary code

Write only code that is required.

Do not create:

- Dead code
- Placeholder implementations presented as finished features
- Unused utilities
- Unused abstractions
- Unused dependencies
- Speculative frameworks
- Duplicate logic
- Unrequested features

Every file, class, function, dependency, and abstraction should have a purpose.

### 4. Write professional Kotlin

Use:

- Clear names
- Small focused functions
- Appropriate architecture
- Kotlin idioms
- Null safety
- Coroutines correctly
- Lifecycle-aware state
- Proper error handling
- Proper resource cleanup
- Immutable state where practical

Avoid giant classes and giant composables.

### 5. No unnecessary comments

Do not add comments to normal production code.

Code should explain itself through good naming and structure.

Only add a comment when it explains a genuinely non-obvious technical reason that cannot reasonably be expressed by the code.

### 6. Security first

Treat files and user input as untrusted.

Never:

- Log passwords
- Log document contents
- Hard-code secrets
- Commit private keys
- Commit keystores
- Store passwords unnecessarily
- Request unnecessary Android permissions
- Use insecure temporary-file handling
- Execute arbitrary shell commands
- Trust filenames, paths, or URIs without validation

### 7. Privacy first

FileFlow is offline-first.

Do not add:

- Analytics
- Tracking
- Advertising
- Cloud uploads
- Telemetry
- Accounts
- Remote processing

unless the user explicitly requests them.

### 8. Dependency discipline

Before adding a library, determine whether the Android platform or an existing dependency can already solve the problem.

Avoid dependency bloat.

Consider:

- APK size
- Security
- Maintenance
- License
- Offline support
- Project activity

### 9. UI discipline

FileFlow uses a clean Material 3 design.

Use:

- Floating top bar
- Floating bottom navigation
- Moderate rounded corners
- Consistent spacing
- Consistent typography
- Restrained animations
- Strong accessibility
- Light/Dark/AMOLED/System themes
- Dynamic/custom accent colors

Do not use excessive gradients, glassmorphism, giant rounded shapes, unnecessary shadows, or visual clutter.

### 10. Git discipline

Do not make unrelated changes.

Do not reformat unrelated files.

Do not upgrade dependencies without reason.

Do not modify generated files unnecessarily.

Keep changes focused.

### 11. CI/CD discipline

Normal pushes must not publish releases.

A version tag such as:

`v1.0.0`

must trigger the release workflow.

The release workflow should:

1. Validate the version.
2. Run appropriate checks.
3. Build release APKs.
4. Produce ARM64 and ARMv7 artifacts.
5. Name them:

`FileFlow-V1.0.0-arm64-v8a.apk`

`FileFlow-V1.0.0-armeabi-v7a.apk`

6. Create the GitHub Release.
7. Upload both APKs.
8. Associate the correct changelog/release notes.

A failed ABI build must fail the release rather than silently producing an incomplete release.

### 12. Changelog

FileFlow has an in-app Changelog screen.

Keep changelog data maintainable and versioned.

Each release should include:

- Version
- Date
- Added
- Changed
- Fixed
- Security changes when applicable

The app may show a "What's New" screen after an update, but should not repeatedly show the same release notes after they have been acknowledged.

### 13. Scope

Current approved tools:

1. Image → PDF
2. PDF → Images
3. PDF → DOCX
4. DOCX → PDF
5. PDF Compressor
6. PDF Password Remover when the user knows the password
7. PDF Merge
8. PDF Split / Extract
9. Image Compressor
10. Document Scanner

Current approved settings:

- Core
- Appearance
- Processing
- Privacy
- About

Do not add unrelated features without explicit approval.

### 14. File saving

First use:

- Let the user choose a default save folder.

After that:

- Save automatically to the selected folder.
- Remember the persisted folder permission.
- Provide Save As for another destination.
- Ask before replacement according to the user's setting.

### 15. Before implementation

When working on a task:

1. Inspect the repository.
2. Understand existing code.
3. Reuse existing infrastructure.
4. Plan the smallest clean implementation.
5. Implement it.
6. Review the changed files.
7. Do not perform a local build unless explicitly requested.
8. Do not push unless explicitly requested.
9. Report what changed and what remains.

### 16. Never pretend

Do not claim:

- "Build passes" unless a build was actually run.
- "Tests pass" unless the relevant tests were actually run.
- "Release works" unless CI or an appropriate validation has verified it.
- "Feature is complete" when it is only a placeholder.

Be explicit about what was and was not validated.

## Engineering principle

Write code that a professional Android engineer would be comfortable maintaining two years from now.

Prefer simple, secure, understandable solutions over clever ones.
