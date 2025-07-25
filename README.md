
# Walk The Line

I Walk The Line! Can you beat minecraft when you are limited to only traveling on a single line?




## Commands

#### Enable mod

```http
  /WalkTheLine enable
```

Mod is disabled by default. To start using the mod simply run the `/WalkTheLine enable` command from chat.


Once enabled the mod will lock the player to either the X or Z axis. A rotating color line is displayed to show the boundary that the player is restricted to. Note this line currently becomes translucent / invisible when using shaders.

Overworld: It finds the closest stronghold from spawn and locks the player to the axis and coordinate that intersects the end portal frame.

Nether: When entering the Nether the mod will lock the player to the same axis as the overworld but to the coordinate that the player was placed on when entering the Nether.

End: The player is locked to 0 on the Z axis, making it intersect with the End Island Portal  


## Configuration

### config/walk-the-line.json

| Parameter             | Type       | Description                                                                 | Default          |
| :-------------------- | :--------- | :-------------------------------------------------------------------------- |:-----------------|
| `particleType`        | String     | Particle to use when using the particle line indicator                      | "electric_spark" |
| `coordinateTolerance` | Float      | Side to side distance from the line you can move before pushback is applied | 0.40             |
| `teleportTolerance`   | Integer    | Number of blocks away from the line before teleporting you back             | 2                |