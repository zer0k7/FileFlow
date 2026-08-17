# Contributing to FileFlow

Thank you for your interest in contributing to **FileFlow**!

## Development Guidelines

1. **Privacy & Offline First:** Core utilities must remain 100% offline. No analytics, tracking, or cloud APIs.
2. **Material 3 UI:** Maintain the floating top/bottom bar visual aesthetic, clean spacing, and accessible contrast.
3. **Clean Architecture:** Keep UI, state, viewmodels, engines, and storage logic decoupled.
4. **Code Quality:**
   - Small, focused Kotlin functions and composables.
   - Null safety, proper coroutine dispatchers, and resource cleanup.
   - No unnecessary comments or bloated dependencies.

## Workflow

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m "feat: Add your feature"`
4. Push to your fork and submit a Pull Request.
