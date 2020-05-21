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

# Crafting Stations
This module further adds three crafting stations.

1. The Basic Woodworking Station
2. The Novice Woodworking Station
3. The Standard Woodworking Station (Same skin as Novice station)

There is also the Portable Basic Wood Station which allows for the crafting of the same recipes as the basic station. When placed as a block, it is identical to the basic station.

Items can also be crafted by hand, in fact, most of the recipes are crafted by hand.

# Crafting
At the stations, you can craft various recipes. Each successive station allows you to craft better and better items. The UI screen of the standard station looks like this:

![Standard Station UI](https://github.com/Steampunkery/WoodCrafting/blob/master/assets/textures/StandardWoodworking.png)

Each station has a slightly different UI, allowing for different inputs, tools, upgrades and outputs.

It is important to remember that Wood Crafting is a system. This means that you can make your own module that houses your recipes and crafting stations so you won't have to modify Wood Crafting.

## Getting started
You're probably wondering how to actually craft an item. Here's how you get started.

1. Whip out your pickaxe (Using a shovel will not result a stone, you must use a pick)
2. Start mining dirt until you have 3-5 stones and 2-3 flint
3. Press "n" to open the hand crafting menu
4. Click the craft button of one of the available items

# Recipes
Here is a reference to all the craftable items in this module:

[In-hand crafting items](https://github.com/Terasology/WoodCrafting/tree/master/assets/prefabs/recipe/hand)

[Basic Station crafting items](https://github.com/Terasology/WoodCrafting/tree/master/assets/prefabs/recipe/wood/basic)

[Novice Station crafting items](https://github.com/Terasology/WoodCrafting/tree/master/assets/prefabs/recipe/wood/novice)

[Standard Station crafting items](https://github.com/Terasology/WoodCrafting/tree/master/assets/prefabs/recipe/wood/standard)
