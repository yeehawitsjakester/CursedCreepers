# Cursed Creepers

Shitpost creepers for Minecraft **1.7.10** (Forge 10.13.4.1614). Install on the server **and** every client.

## Creepers

- **Hallucination Creeper**: looks exactly like a creeper. 1% of naturally spawned creepers are secretly one. It
  notices you from 64 blocks away, but only if it can see you and has a way to actually get to you, and runs at you at
  24 blocks a second (about 4x a sprinting player). If you get somewhere it can't follow, it gives up after 3 seconds
  and leaves you alone for 10.
  - As it comes, a sound plays from where it is, stretched to end the instant it reaches you: slowed down and deeper for
    a long chase, sped up and higher for a short one. You hear it across its whole sight range; anyone else within 60
    blocks hears it too, fading with their distance from it, like proximity chat.
  - Only you can see it. It can't be hit, shot or pushed, makes no footsteps, and fades away after two minutes with
    nobody to chase.
  - When it reaches you it's gone. Usually that's all; one in five goes off instead: a bang and 1.5 hearts of damage,
    but no blocks broken.
  - Each time one reaches someone there's a 0.000001% (one in a hundred million) chance it crashes the server.

Ops can send one at a player from a distance, already after them:

```
/cursedcreeper spawn [distance] [player]
```

The distance defaults to 50 blocks and the player to yourself. It lands on ground in a random direction, as close to
the player's height as it can find. Only you get told.

Tune it in `config/cursedcreepers.cfg` (chase speed, sight range, vanish distance, replace chance, sound range, explode
chance and damage, crash chance, and an optional extra spawn weight). Keep the server's and
clients' configs the same.

## Sounds

`sound-src/` holds the original clip; `tools/make-sounds.sh` (needs ffmpeg) turns it into the three OGG copies the mod
plays (1x, 0.25x and 4x speed).

## Building

```
./gradlew build
```

The jar lands in `build/libs/`.
