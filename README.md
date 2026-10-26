# Cursed Creepers

Shitpost creepers for Minecraft **1.7.10** (Forge 10.13.4.1614). Install on the server **and** every client.

## Creepers

- **Hallucination Creeper**: looks exactly like a creeper. 1% of naturally spawned creepers are secretly one. It
  notices you from 64 blocks away, but only if it can see you and has a way to actually get to you, and runs at you at
  96 blocks a second (about 17x a sprinting player). If you get somewhere it can't follow, it gives up after 3 seconds
  and leaves you alone for 10.
  - As it comes, a sound plays from where it is, stretched to end the instant it reaches you: slowed down and deeper for
    a long chase, sped up and higher for a short one. You hear it across its whole sight range; anyone else within 60
    blocks hears it too, fading with their distance from it, like proximity chat.
  - Only you can see it. It can't be hit, shot or pushed, makes no footsteps, and fades away after two minutes with
    nobody to chase.
  - When it reaches you it's gone. Usually that's all; one in five goes off instead: a bang and 1.5 hearts of damage,
    but no blocks broken.
  - Each time one reaches someone there's a 0.000001% (one in a hundred million) chance it crashes the server.

## Hauntings

Every 10 minutes or so (on average, per player), one player gets something nobody else notices:

- **A whisper**: a sound from somewhere behind them, played to them alone, quiet enough to always sound far off. A
  backwards, echoing version of the lock-on sound, "Bold... and stupid", a creeper hissing right behind them, a zombie
  banging on a door, a door opening, a bow firing, an enderman's stare. Underground (no daylight reaching them at all)
  half of them are a quiet, echoing fart instead.
- **Herobrine** (2 in 5, when there are trees around): standing beside a trunk or under the leaves 20-40 blocks off, off
  to one side where they'd only catch him out of the corner of their eye, staring at them with glowing white eyes. Only
  they can see him. The moment their crosshair comes near him, or they come within 12 blocks, he's gone: sometimes
  silently, sometimes with a rustle in the leaves, sometimes with "Bold... and stupid" from where he stood. He leaves
  on his own after 90 seconds and is never saved with the world.

## Commands

Ops only; only you get told.

```
/cursedcreeper spawn [distance] [player]     a Hallucination Creeper, already after them
/cursedcreeper watcher [player]              Herobrine in the trees at the edge of their sight
/cursedcreeper whisper [player] [sound]      a whisper (a random one, or any sound name)
```

`spawn` defaults to 50 blocks and the player to yourself. It lands on ground in a random direction, as close to the
player's height as it can find, somewhere it can actually reach them from.

## Config

`config/cursedcreepers.cfg`:

- `hallucination_creeper`: chase speed, sight range, vanish distance, replace chance, sound range, explode chance and
  damage, and an optional extra spawn weight. Keep the server's and clients' the same.
- `haunting` (server only): how often, how many are Herobrine, his distances, vanish angle and distance, how long he
  stays, whether he needs trees, and the whisper sounds as `name,minDistance,maxDistance[,volume][,cave]`. Any sound
  name works, including ones a resource pack adds.
- `general`: the crash chance.

## Sounds and skin

`sound-src/` holds the original clips; `tools/make-sounds.sh` (needs ffmpeg) turns them into the OGGs the mod plays: the
lock-on sound at 1x, 0.25x and 4x speed and the backwards whisper, all from the meme clip, plus `bold` and `cave_fart`
as they are. `tools/make-skin.py` draws Herobrine's skin and his eyes layer.

## Building

```
./gradlew build
```

The jar lands in `build/libs/`.
