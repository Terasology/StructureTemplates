#  Structure Templates

## About this repository.

Structure Templates is a module for Terasology that allows for the creation of so called "structure templates".


## What are structure templates

Structure tempaltes describe a structure. This structure can then be placed multiple times.

The structure templates can either be obtained as item ingame and just be used for quicker building.

However structure templates can also be used exported to json files (terasology prefabs) and used by modules to generate for example a random dungeon with multiple rooms. The module [Gooey's Quest](https://github.com/Terasology/GooeysQuests) does for example exactly that. A single structure template can spawn a complete random dungeon as structure templates can also trigger the spawning of further structure templates.

Structure templates can not only spawn blocks but for example also entities. However the spawning of more than just blocks and chests with items need currently some manual extension of the json file. For example the dungeon generating structure templates of  [Gooey's Quest](https://github.com/Terasology/GooeysQuests) also spawn skeletons.

The structure template framework can also be easily extended to add further spawn actions and conditions.

## Getting started by editing an existng template ingame

It is possible to place structure templates in a edit mode. In that edit mode placeholders will be placed at locations where for example normally other further sturcture template spawning would have been triggered normally. To do this you first need to obtian the toolbox item ![with red toolbox icon](assets/textures/Toolbox16x16.png) via the command `give toolbox`. This item opens on activation a dialog which allows you to obtain items (Icon: ![with T icon](assets/textures/StructureTemplateOrigin.png)) that spawn structure templates in edit mode. However to actually see structure templates ingame you need to activate first a moduel like [Gooey's Quest](https://github.com/Terasology/GooeysQuests) that contains structure templates. 

Once you placed a structure template a white block with a black T (Icon: ![T](assets/textures/StructureTemplateOrigin.png)
) will  appear at the so called "origin" of the structure template.
This structure template orign block can be activated to open a interaction dialog with the structure template.

