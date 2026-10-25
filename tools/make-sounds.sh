#!/usr/bin/env bash
# Rebuild the lock-on sounds from sound-src/: mono OGG (Minecraft 1.7.10 can't play MP3, and only mono is positional)
# at 1x, 0.25x and 4x speed. These are resampled, so the pitch changes with the speed on purpose. LockOnSound picks one
# and stretches it further (0.5x-2x) to end when the creeper arrives. Update LockOnSound.BASE_SECONDS if the clip changes.
# Also the whisper: the same clip backwards, slowed, muffled and echoing, for hauntings; and bold.mp3 (Herobrine's line)
# and cave_fart.mp3 (an underground whisper) as they are.
set -euo pipefail
cd "$(dirname "$0")/.."
src=sound-src/brain-aneurysm-meme-sound-effect.mp3
out=src/main/resources/assets/cursedcreepers/sounds
rate=$(ffprobe -v error -select_streams a:0 -show_entries stream=sample_rate -of csv=p=0 "$src")
for v in lockon:1 lockon_slow:0.25 lockon_fast:4; do
    name=${v%%:*}; speed=${v##*:}
    ffmpeg -hide_banner -loglevel error -y -i "$src" -af "asetrate=$rate*$speed,aresample=44100" -ac 1 -c:a libvorbis -q:a 5 "$out/$name.ogg"
    echo "$name: $(ffprobe -v error -show_entries format=duration -of csv=p=0 "$out/$name.ogg")s"
done
ffmpeg -hide_banner -loglevel error -y -i "$src" \
    -af "areverse,asetrate=$rate*0.6,aresample=44100,lowpass=f=1400,aecho=0.8:0.6:180|420:0.35|0.2,volume=1.5" \
    -ac 1 -c:a libvorbis -q:a 5 "$out/whisper.ogg"
echo "whisper: $(ffprobe -v error -show_entries format=duration -of csv=p=0 "$out/whisper.ogg")s"
for name in bold cave_fart; do
    ffmpeg -hide_banner -loglevel error -y -i "sound-src/$name.mp3" -ar 44100 -ac 1 -c:a libvorbis -q:a 5 "$out/$name.ogg"
    echo "$name: $(ffprobe -v error -show_entries format=duration -of csv=p=0 "$out/$name.ogg")s"
done
