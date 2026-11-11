# Driver dashboard (Elastic)

Elastic is the Elastic FRC driver dashboard.

The driver table is published by `DriverDashboard` under `/Telemetry/Driver/`
(`Zone`, `Alliance`, `HoldingPiece`, `ArmAtGoal`). It is separate from the
AdvantageKit log and only writes.

## The layout file

The layout is `src/main/deploy/elastic-layout.json`. It has to be in the root
of the deploy folder: Elastic downloads it from the robot over port 5800, which
`DriverDashboard` serves.

In Elastic: **File > Load Layout From Robot** (`Ctrl+D`), choose
`elastic-layout.json`, then Overwrite, Merge or Full Reload.

## Editing it

Best practice: change the layout in Elastic, **File > Export Layout**, and
replace `elastic-layout.json` with the export. Commit the new file and deploy.

## What is verified

- The file structure (`version`, `grid_size`, `tabs[].name`,
  `grid_layout.layouts` and `.containers`, and per widget `title`, `x`, `y`,
  `width`, `height`, `type`, `properties.topic`) was checked against Elastic's
  source code.
- **Not verified:** that Elastic accepts this exact file. The widget `type`
  names ("Text Display", "Boolean Box") and the grid size of 128 are from
  memory of Elastic's defaults, and nobody has opened the file in Elastic yet.
  If Elastic complains, export a layout from Elastic and compare.
