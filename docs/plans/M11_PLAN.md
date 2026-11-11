# M11 plan (brief): kickoff readiness

Branch: `platform/beta-upgrade`. Run when WPILib 2027 Beta 1 and matching
vendor libraries are out. Refresh and expand with Cohen before editing.

## Goal

The template runs on the beta, the scenarios prove it, and the platform
layer is frozen before kickoff.

## Phase 0: human

- [ ] Install the WPILib beta; update the Systemcore image and Driver
      Station to matching versions.
- [ ] Update vendor libraries one at a time through VS Code, in this
      order: AdvantageKit, Commands v3, Phoenix 6 (with firmware),
      PhotonLib, PathPlannerLib, YAMS if kept. Commit each separately.

## Upgrade procedure (agent, `upgrade-wpilib` skill)

After each vendor-library commit:

1. Read that library's changelog and migration notes. List breaking
   changes that touch this repo.
2. `./gradlew build`. Fix compile errors using `api-check`; never guess.
3. `frc test`, then every scenario, then the golden-log replay.
4. Report PASS, FAIL or BLOCKED per scenario before moving on.

If a library has no beta-compatible release, stop. Record it in
`ROADMAP.md` and decide with Cohen whether to wait or use the fallback
named in that library's milestone plan.

## Also in this milestone

- Update the "2027 is not what you remember" table and
  `docs/reference/2027-notes.md` for anything the beta changed.
- `docs/game/` template: field, scoring, zones, priorities. Empty until
  kickoff.
- Freeze: add `CODEOWNERS` rules so `lib/`, `Robot.java`, `RunProfile.java`
  and the tools source set need mentor review.
- Tag the repo as the season template.

## Hardware (BLOCKED without the robot)

- [ ] Deploy to the Systemcore on the beta image.
- [ ] USB logging works; pull and replay a real log.

## Done when

Every scenario and the golden-log replay pass on the beta, and the tag
exists.
