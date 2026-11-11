# M9 plan (brief): context controls, driver metrics, dashboard

Branch: `ops/context-and-metrics`. Assumes M6 is merged; uses M7 and M8
where present. Refresh and expand with Cohen before editing.

## Goal

Controls that depend on where the robot is and what it holds; a driver
report from any match log; a debrief after every match; a versioned
dashboard layout.

## Deliverables

Context:

- `RobotContext.java`: zone (from pose and a zones file), held piece,
  alliance; exposed as Commands v3 triggers and recorded as outputs.
- `src/main/deploy/zones.json` (placeholder polygons until kickoff) and a
  pure, unit-tested point-in-zone function.
- `Controls.java`: at least one binding whose action depends on zone.
- Scenario: the same button press in two zones produces two behaviors.

Metrics (`frc log ...`):

- `events`: enable and mode changes, alerts, brownouts, loop overruns.
- `track <mechanism>`: goal against measured, with error metrics.
- `driver-report`: cycle count and time, time to align, idle time, time
  per zone. Define each metric in `docs/metrics.md` before coding it.
- `pull`: fetch the newest log from the Systemcore (BLOCKED without one).

Operations:

- `match-debrief` skill: pull, `events`, `track` for each mechanism,
  `driver-report`, one-page Markdown report.
- `dashboard/elastic-layout.json` and a small driver-facing NetworkTables
  table separate from the AdvantageKit stream. Elastic means the Elastic
  FRC dashboard.
- `logs/golden/` with one sim "match" log; CI replays it and fails on
  unexplained divergence.

## Verify first

How Elastic 2027 loads a layout file; 2027 Telemetry API for the driver
table; how alerts and loop overruns appear in an AdvantageKit log.

## Done when

`frc log driver-report` produces a report from one scenario-driven "match"
log, and the golden-log replay runs in CI.
