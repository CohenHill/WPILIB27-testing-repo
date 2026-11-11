# M10 plan (brief): student onboarding

Mostly people work. Agents help write the materials; mentors run the
sessions. Assumes M4 and M5 are merged.

## Goal

Every student has one merged pull request they can explain, made with the
full loop: spec, plan, build, scenario, review.

## Materials (agent-drafted, mentor-reviewed)

- `docs/setup.md`: install WPILib 2027 on Windows and Mac, including that
  the Mac install lives in the hidden `~/.wpilib` folder; Git for Windows;
  Copilot in the WPILib VS Code; optional Claude Code.
- `docs/tour.md`: a one-session walk through the Arm, bottom to top: IO
  interface, sim IO, mechanism, command, test, scenario, replay.
- `docs/ai-levels.md` (from M5): how to pick and set a level, including
  level 0.
- `docs/first-task.md`: the toy-mechanism exercise below.
- Issue templates: spec (behavior, constraints, pass criteria) and bug.

## The exercise

Each student adds a small mechanism of their choice (roller, flag, LED
state) with the `new-mechanism` skill or by hand:

1. Write the spec as an issue.
2. Get a plan (`/plan`, Copilot, or on paper) and have a lead review it.
3. Implement at their level. Level 1 students write the logic themselves
   at marked spots.
4. Add one unit test and one scenario.
5. Open a PR; fill in the explain-back; the mechanism owner file gains
   their name.

## Mentor checklist

- [ ] Each student set their AI level.
- [ ] First weekly AI-off code walk held; one student explained the Arm.
- [ ] `CODEOWNERS` lists a student owner per mechanism.
- [ ] Collected what confused people; fixed the docs, not just the person.

## Done when

Every student has a merged PR and can answer one follow-up question about
it without their laptop.
