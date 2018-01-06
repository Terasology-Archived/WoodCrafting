Woodcrafting
============

This module adds several items, blocks, crafting recipes, and workstation processes that are used in or produced using
the skill of Woodcrafting. This utilizes the components, events, systems, and UI elements present in the
`WorkstationCrafting` parent module. This module relies on the use of the `NeoTTA` world generator.

`Woodcrafting` is intended to be the Tier 1 material level of the `NeoThroughoutTheAges` (or NeoTTA) family of modules,
following Tier 0's NatureCrafting in `WorkstationCrafting`.

For a general walkthrough of how to progress through this module and craft items, please read `WALKTHROUGH.md` (TBA).

**Important Note:** If you wish to use the `extendedProcesses` branch of this module, make sure to switch to the
`extendedProcesses` branches of both `WorkstationCrafting` and `Workstation`. Otherwise, various features may break or
act unstable.

===

Naming convention:

* Append "Station" to the end of every workstation.
* Append "Process" to the end of every process name. Here, the convention followed is `[SkillLevel][StationName]Station`.
* Prepend "Recipe" to any crafting recipe name.

===

In order to add a new process to this module, do the following:

* Add the process definition under assets/prefabs/processDefinitions. Use the preexisting ones as templates.
* Add a static variable containing the name of the process in src/main/java/org/terasology/woodCrafting/Woodcrafting.java
* In src/main/java/org/terasology/woodCrafting/system/RegisterWoodcraftingRecipes.java, add this process to the process
factory (registerProcessFactory).

Now, you should be able to use this process in a workstation or recipe.