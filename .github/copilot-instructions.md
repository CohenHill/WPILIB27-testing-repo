Read `AGENTS.md` in the repository root first; it is the source of truth.

Key facts: WPILib 2027 alpha (packages are `org.wpilib.*`), Java 25,
Commands v3 (not v2), AdvantageKit. Build with `./gradlew build`. Simulate
with `./gradlew simulateJava`. Never deploy or push. Never edit `vendordeps/`.
Do not write WPILib or vendor API calls from memory; check the docs linked
in `docs/reference/2027-notes.md`.
