---
name: match-debrief
description: Write a one-page debrief of a match or sim run from its log. Use after a match, or when asked for a debrief, driver report or "how did that run go". Needs a .wpilog file.
---
1. Get the log. Use the path given. `./frc log pull` is BLOCKED (it needs a
   Systemcore), so if no path was given, ask the person to copy the log over.
   For a practice run in simulation, `./frc sim SimMatch` makes one and prints
   its path.
2. Events: `./frc log events <log>`. Note enables, mode changes, alerts,
   brownouts and loop overruns, with their times.
3. Mechanisms: for each mechanism with a numeric goal signal (today only the
   Arm), `./frc log track <log> Arm`. Report mean and max error and
   `fractionNearGoal`. For other mechanisms say there is no goal signal.
4. Driver: `./frc log driver-report <log>`. Report cycles and cycle times, idle
   time, time per zone. `timeToAlign` is null until an align command exists:
   say so rather than guessing.
5. Write one page of Markdown with these headings: **Summary** (three
   sentences), **Events**, **Mechanisms**, **Driver**, **Things to check**.
   Every number comes from a command above; quote the command next to it.
   Definitions of each metric are in `docs/metrics.md`.
6. Print it. Save it to `build/debrief/<log name>.md` only if asked.

Never fill in a number that no command printed. If a command fails or a signal
is missing, say which one.
