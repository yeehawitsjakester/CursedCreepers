# Cursed Creepers

Shitpost creepers for Minecraft **1.7.10** (Forge 10.13.4.1614). Install on the server **and** every client.

## Creepers

- **Hallucination Creeper**: looks exactly like a creeper. It notices you from 64 blocks away, runs at you about 2.4x
  as fast as a normal creeper, and the moment it reaches you it's gone. No explosion, no drops. Only the player it's
  chasing can see it; it can't be hit, shot or pushed, makes no footsteps, and fades away after two minutes with nobody
  to chase. Spawns rarely in the dark like other monsters, or from its spawn egg (Miscellaneous tab).

Ops can send one at a player from a distance, already after them:

```
/cursedcreeper spawn [distance] [player]
```

The distance defaults to 50 blocks and the player to yourself. It lands on ground in a random direction, as close to
the player's height as it can find. Only you get told.

Tune it in `config/cursedcreepers.cfg` (speed, sight range, vanish distance, spawn weight). Keep the server's and
clients' configs the same.

## Building

```
./gradlew build
```

The jar lands in `build/libs/`.
