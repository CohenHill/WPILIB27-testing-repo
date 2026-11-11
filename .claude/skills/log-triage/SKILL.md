---
name: log-triage
description: Investigate a named symptom in a log with bounded queries, for example "arm never reaches goal" or "current spike". Use when given a log and a symptom.
---
1. Get the symptom and the log path. If either is missing, ask.
2. Find the signals: `./frc log keys <log> <prefix>` with the narrowest prefix
   (for example `Arm/`). Never list the whole log.
3. Measure: `./frc log stats <log> <key> --from <s> --to <s>` over a window.
   Start wide (whole run), then narrow to where the symptom happens. Numbers
   and booleans only.
4. If the log is a replay, check recorded vs replayed with
   `./frc log diff <log> <key>`.
5. Answer with one short finding and the numbers behind it (queries and
   values). If the logged signals cannot answer, say what would be needed.

Output is capped at 50 items per query; use `--limit` only when needed. Never
dump a raw series or guess a cause without a number.
