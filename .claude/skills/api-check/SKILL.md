---
name: api-check
description: Verify a WPILib 2027, Commands v3 or vendor API before writing a call to it. Use whenever adding or changing code that calls org.wpilib, org.littletonrobotics or a vendor library.
---
1. Read the pinned version: `build.gradle` (GradleRIO) or the library's
   file in `vendordeps/`.
2. Open the class: its source in the Gradle cache or on GitHub at the
   matching tag, or the docs linked in `docs/reference/2027-notes.md`.
3. Confirm the package, class name, method name and argument types.
4. Only then write the call. In your report, name the source you checked.

If you cannot find the class or method, stop and say so. Do not substitute
a 2025 or 2026 API.
