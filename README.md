# FTB StoneBlock Companion

Companion mod to FTB StoneBlock 3, focused on adding team based custom dimensions, sluice like tools and lots of helper utilities.

> As with most FTB mods, this mod has been created with the sole purpose of adding, modifying and removing of features in Minecraft based on the requirements of the modpack it was created for.
>
> **We never** recommend using this kind of mod outside of the pack it was created for as it is very likely to create issues with mods it was not tested against.

## What's included

As of writing this, the mod does the following

### Dimensions

- Adds team based dimension, creating based on FTB Teams
  - Each team's dimension is based on a random name built using 3 random words joined by a `-`
- Adds a custom portal to access these dimensions
  - Upon entering a portal without a dimension already created for your team.
    - A team will be created
    - A screen will show to allow you to select the starting structure to generate in the new dimension
    - After selection, you will be teleported to the correct dimension and any use of the portal after this point will put you back to that dimension even if you're already in that dimension.
  - The portal is a creative based item meaning it must be placed by the pack creator or a structure file
- Custom overworld spawning logic that defines where a player is first spawned, a structure that can be spawned in by the system automatically upon overworld creation (based on KubeJS)
  - Players can not be damaged, par from the `OUT_OF_WORLD` damage source, in the overworld
- A completely unique `StoneBlock` dimension that builds out circular worlds from the spawn point based on a dynamic config provided by KubeJS.

### Tools

- Auto hammers
- Hammers
- Crook
- JEI / REI integration for Cauldron recipes
- FirePlow
  - This item allows the player to hold right click with the item in hand to create lava from stone

## KubeJS

As with most FTB Mods of this nature, mostly everything is configurable with KubeJS. We recommend you reference the scripts within FTB StoneBlock 3 if you wish to see how these configs can be modified if you're trying to change them on a server, for example.

## Support

For **Modpack** issues, please go here: https://go.ftb.team/support-modpack

For **Mod** issues, please go here: https://go.ftb.team/support-mod-issues

Just got a question? Check out our Discord: https://go.ftb.team/discord

## Licence

All Rights Reserved to Feed The Beast Ltd. Source code is `visiable source`, please see our [LICENSE.md](/LICENSE.md) for more information. Any Pull Requests made to this mod must have the CLA (Contributor Licence Agreement) signed and agreed to before the request will be considered. 
