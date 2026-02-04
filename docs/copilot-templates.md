Copilot prompt templates and examples (Chronos)
=============================================

This document contains copy-ready prompt templates you can use with GitHub Copilot or other assistants when working on Chronos (Spring Boot 3, Java 17).

Short prompts
-------------
- "Create a MapStruct mapper `FacilityMapper` for `Facility` <-> `FacilityDTO` (componentModel=spring). Include imports and a small unit test verifying round-trip mapping."
- "Add `DatabaseTimeoutException` with an optional `Duration timeout` and map it in GlobalControllerAdvice to return 408 if timeout < 3 minutes, otherwise 503. Add two MockMvc tests."
- "Write a MockMvc standalone test for `FacilityControllerAdvice` to assert `ResourceNotFoundException` results in 404 with ErrorResponse containing `X-Correlation-Id`."

Detailed template (copy/paste and edit)
--------------------------------------
```
Goal: <one-sentence goal>
Location: com.example.chronos.<package>
Style: Spring Boot 3, Java 17, use jakarta.*, constructor injection
Tests: include JUnit5 tests (MockMvc for controllers)
Restrictions: No new dependencies, no secrets, follow repo conventions in copilot-instructions.md
```

Example usage
-------------
- Paste the Detailed template and replace placeholders to ask Copilot for focused, test-covered code.

Tips
----
- Keep prompts focused — one class or small feature per prompt.
- Ask for tests together with the code to reduce review cycles.
- If Copilot suggests large multi-file changes, split them into smaller prompts.
