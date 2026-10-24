# Cursed Creepers

Shitpost creepers for Minecraft **1.7.10** (Forge 10.13.4.1614). Install on the server **and** every client.

## Creepers

- **Hallucination Creeper**: looks exactly like a creeper. It notices you from 64 blocks away (if it can see you) and
  runs at you at about 16 blocks a second, roughly 6x a normal creeper and 3x a sprinting player. As it comes, the
  player it's after hears a sound stretched to end the instant it reaches them, slowed down and deeper for a long
  chase, sped up and higher for a short one. Then it's gone: no explosion, no hit, no drops. Only the player it's
  chasing can see or hear it; it can't be hit, shot or pushed, makes no footsteps, and fades away after two minutes
  with nobody to chase. Spawns rarely in the dark like other monsters, or from its spawn egg (Miscellaneous tab).

Ops can send one at a player from a distance, already after them:

```
/cursedcreeper spawn [distance] [player]
```

The distance defaults to 50 blocks and the player to yourself. It lands on ground in a random direction, as close to
the player's height as it can find. Only you get told.

Tune it in `config/cursedcreepers.cfg` (speed, sight range, vanish distance, spawn weight). Keep the server's and
clients' configs the same.

## Sounds

`sound-src/` holds the original clip; `tools/make-sounds.sh` (needs ffmpeg) turns it into the three OGG copies the mod
plays (1x, 0.25x and 4x speed).

## Building

```
./gradlew build
```

The jar lands in `build/libs/`.
