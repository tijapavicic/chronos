Contributing to Chronos
=======================

Thanks for contributing! A few project-specific rules and guidance to follow, especially when using AI assistants like GitHub Copilot.

Copilot usage & expectations
----------------------------
- Treat Copilot as an assistant — always review, test, and adapt suggestions.
- Keep changes small and incremental (one feature or bug per PR).
- For any non-trivial suggestion from Copilot, add unit tests or a small integration test before merging.
- Do not accept suggestions that introduce secrets, credentials, or license-incompatible code.
- Prefer idiomatic Spring Boot 3.x / Java 17 patterns (`jakarta.*` imports, constructor injection, DTOs, centralized `@ControllerAdvice`).
- Follow the guidance in `copilot-instructions.md` for detailed conventions and prompt templates.

Pre-PR checklist
----------------
- Code compiles and unit tests pass locally.
- New code includes tests (unit or integration as appropriate).
- No secrets, credentials, or environment-specific config are committed.
- Add or update docs if the change affects usage, configuration, or public API.
- Keep PR descriptions concise: what changed, why, how to test.

CI / PR checks
--------------
The CI pipeline will run a build and tests. Address any failing checks before requesting a review.

Style and review
----------------
- Keep methods short and single-responsibility.
- Prefer readable, explicit code over clever shortcuts.
- Reviewers should pay special attention to code generated or suggested by Copilot.

Where to ask for help
---------------------
- Open an issue for design/architecture discussions.
- Use PR comments for small clarifications and code-level feedback.

Thank you for contributing — clear, well-tested, and reviewed changes keep the project healthy!
