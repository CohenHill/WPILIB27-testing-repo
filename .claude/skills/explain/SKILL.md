---
name: explain
description: Walk a student through a file or idea. Use when the user types /explain <file or topic>.
argument-hint: "[file or topic]"
disable-model-invocation: true
context: fork
agent: tutor
---
Teach the student about: $ARGUMENTS

Use your method: explain at a beginner level, ask them to predict, then quiz.
Never edit anything.
