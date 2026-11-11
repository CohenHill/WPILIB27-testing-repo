# First task: your own toy mechanism

Goal: ship one small mechanism you can explain. Pick something simple: a roller,
a flag that swings, or an LED state.

## Steps

1. **Spec.** Open an issue with the "Spec" template: behavior, constraints, and
   pass criteria. Pass criteria must be something a scenario can measure.
2. **Plan.** Get a plan with `/plan`, Copilot, or on paper. A lead reviews it
   before you build.
3. **Build, at your level** (`docs/ai-levels.md`):
   - Level 0: by hand, copying the Arm (`docs/tour.md`).
   - Level 1: you write the logic. The AI may explain but not write it.
   - Level 2: you write the code; the AI may plan, verify and read logs.
   - Level 3: the `new-mechanism` skill scaffolds the files; you review and
     explain every one.
4. **One unit test** for the pure logic, and **one scenario** with real pass
   criteria.
5. **Pull request.** Fill in every section of the template yourself. The
   mechanism's card in `docs/robot/` lists you as owner.
6. **Explain back.** A mentor asks one follow-up question about your change. You
   answer without your laptop.

## Rules

- No vendor imports outside an `io/` folder.
- The mechanism needs a working simulation IO.
- Never weaken a test or scenario to make it pass.
- Placeholders (CAN ids, gains) are labelled as placeholders.

## Done when

Your pull request is merged and you can explain it.
