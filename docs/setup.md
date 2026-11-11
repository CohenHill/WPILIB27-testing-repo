# Setup

Do these in order. Ask a mentor if a step does not match what you see; then fix
this page.

## 1. Install the tools

1. **WPILib 2027** (the alpha version this repo uses; `AGENTS.md` and
   `build.gradle` name it). Run the installer and choose "Everything". It
   installs VS Code, the JDK, Gradle caches, AdvantageScope and Elastic.
   - **Windows:** the installer shows where it installs; keep the default.
   - **Mac:** WPILib keeps files in `~/wpilib/<year>` and in a hidden folder,
     `~/.wpilib`. In Finder press `Cmd+Shift+.` to show hidden folders. The
     Gradle build finds the JDK there; you normally never open it.
2. **Git.** Windows: install Git for Windows. Mac: run `git --version` in
   Terminal and accept the Xcode tools prompt if it appears.
3. **A GitHub account**, added to the team repository by a mentor.
4. **Optional AI tools** (see `docs/ai-levels.md` first):
   - GitHub Copilot in the WPILib VS Code (Extensions view, search "GitHub
     Copilot", sign in).
   - Claude Code, if your mentor says so.

## 2. Get the code

```
git clone https://github.com/CohenHill/WPILIB27-testing-repo.git
cd WPILIB27-testing-repo
```

Open the folder in the **WPILib VS Code** (not plain VS Code).

## 3. Check it works

In the terminal inside VS Code (Windows: use `.\gradlew.bat` and `.\frc.cmd`):

| Command | Expect |
| --- | --- |
| `./gradlew build` | `BUILD SUCCESSFUL` |
| `./frc sim --list` | a list of scenario names |
| `./frc sim ArmReachesGoal` | a line of JSON with `"pass":true` |

If `./gradlew build` says `invalid source release: 25`, Gradle is using the
wrong Java. Set `JAVA_HOME` to the WPILib 2027 JDK (the `./frc` wrapper does
this for you), or ask a mentor.

## 4. Set your AI level

Read `docs/ai-levels.md` and set `FRC_AI_LEVEL` in `.claude/settings.local.json`
if you use Claude Code. Level 0 (no AI) is a normal, respected choice.

## 5. Make your first branch

```
git checkout -b <area>/<short-description>
```

Read `CONTRIBUTING.md` before you commit. Never work on `main`.

## Where to look when stuck

- `AGENTS.md`: the rules and commands.
- `docs/tour.md`: how the Arm is built, top to bottom.
- `docs/reference/2027-notes.md`: where the 2027 documentation is.
