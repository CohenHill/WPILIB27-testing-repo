# Contributing

## Branches

- Never commit to `main`. Work on a branch and open a pull request.
- Name branches `<area>/<short-description>`, for example
  `intake/add-roller-sim` or `docs/setup-guide`.

## Commits

- Subject line: imperative mood, capitalized, no period, 72 characters or
  fewer. "Add roller sim", not "added roller sim" or "roller stuff".
- If the change is to one mechanism, name it in the subject.
- Body (optional for small changes): why the change was made, not what the
  diff already shows.
- One logical change per commit.

## Pull requests

1. Make sure `./gradlew build` passes on your machine.
2. Open the pull request and fill in every section of the template in your
   own words.
3. A reviewer must approve and CI must pass before merging.
4. A person merges. AI tools never merge, push or deploy.

## Using AI tools

AI tools are allowed and optional. Whoever opens the pull request must be
able to explain every line in it. If you cannot explain it, do not submit
it. Mark AI-assisted commits with a co-author trailer.

## Tests

New logic needs a unit test in `src/test/java`. Never delete, skip or
weaken a test to make the build pass.
