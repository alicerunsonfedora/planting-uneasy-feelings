<img src="icon.png" align="right" alt="Planting Uneasy Feelings icon"/>

# Planting Uneasy Feelings

A game about inflicting a curse that uncontrollable grows.

When Claris discovers that Tom, her manager, has cut but budget for her team's latest project, she devises a scheme for
revenge; inflict a curse that turns everyone into trees. Are you able to inflict the curse and find the ones that will
spread it uncontrollably?

**Planting Uneasy Feelings** is a game created for the 36th Godot Wild Jam and is a showcase for what can be done with
the Kotlin/JVM module for Godot created by utopia-rise.

## Build from source

**Required Tools**:

- Godot 3.3.2 with the Kotlin/JVM plugin (see: https://godot-kotl.in)
- IntelliJ 2021.1.x (either CE or Ultimate works)
- Godot Kotlin/JVM plugin for IntelliJ

First, follow the instructions outlined in the [Setup process for Godot-Kotlin][gdksetup]
to set up the engine.

You will also need to download the export templates for the 0.2.0-3.3.2 release
and [follow the instructions on how to set this up][gdkexport].

[release]: https://github.com/utopia-rise/godot-kotlin-jvm/releases/tag/0.2.0-3.3.2

[gdksetup]: https://godot-kotl.in/en/stable/getting-started/requirements/

[gdkexport]: https://godot-kotl.in/en/stable/user-guide/exporting/

Open Godot-Kotlin and click "Build" to build the project. Restart Godot-Kotlin to refresh the editor with exported
variable changes.

> Note: You may need to reassign exported variables as necessary in the level files.

Run the following in the root of the project:

```bash
jlink --add-modules java.base,java.logging --output jre
```

Then follow the standard procedure for exporting the project for Windows, macOS, and Linux.