---
name: plan
description: Plan a change before writing any code. Use when the user types /plan or asks for a plan. Prints a plan and edits nothing.
argument-hint: "[task description]"
disable-model-invocation: true
context: fork
agent: planner
---
Plan this task: $ARGUMENTS

Follow your output format exactly. Do not edit anything. If the task is empty,
say "no task given" and stop.
