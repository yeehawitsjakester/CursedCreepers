#!/usr/bin/env python3
"""Draws Herobrine's skin (64x32, the classic player layout) and the glowing eyes layer, from scratch, no libraries."""
import os
import random
import struct
import zlib

W, H = 64, 32
OUT = os.path.join(os.path.dirname(__file__), "..", "src/main/resources/assets/cursedcreepers/textures/entity")
random.seed(1337)  # same pixels every run

SKIN = (176, 122, 92)
SKIN_DARK = (150, 100, 74)
HAIR = (52, 34, 18)
SHIRT = (0, 160, 165)
PANTS = (54, 50, 140)
SHOES = (76, 76, 76)
EYE = (255, 255, 255)


def blank():
    return [[(0, 0, 0, 0)] * W for _ in range(H)]


def fill(img, x0, y0, x1, y1, color, jitter=8):
    for y in range(y0, y1):
        for x in range(x0, x1):
            j = random.randint(-jitter, jitter)
            img[y][x] = tuple(max(0, min(255, c + j)) for c in color) + (255,)


def box(img, u, v, w, h, d, color):
    """a model box's six faces at texture offset (u, v), size w x h x d, all one colour"""
    fill(img, u + d, v, u + d + w, v + d, color)  # top
    fill(img, u + d + w, v, u + d + w + w, v + d, color)  # bottom
    fill(img, u, v + d, u + d + w + d + w, v + d + h, color)  # right, front, left, back


def png(path, img):
    raw = b"".join(b"\0" + bytes(c for px in row for c in px) for row in img)
    chunk = lambda t, d: struct.pack(">I", len(d)) + t + d + struct.pack(">I", zlib.crc32(t + d) & 0xFFFFFFFF)
    with open(path, "wb") as f:
        f.write(b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", W, H, 8, 6, 0, 0, 0))
                + chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b""))


skin = blank()
# head (8x8x8 at 0,0): face skin, hair over the top, back and upper sides, a fringe across the brow
box(skin, 0, 0, 8, 8, 8, SKIN)
fill(skin, 8, 0, 16, 8, HAIR)  # top
fill(skin, 24, 8, 32, 16, HAIR)  # back
fill(skin, 0, 8, 8, 11, HAIR)  # right side
fill(skin, 16, 8, 24, 11, HAIR)  # left side
fill(skin, 8, 8, 16, 10, HAIR)  # fringe
fill(skin, 0, 11, 2, 13, HAIR)  # sideburns
fill(skin, 22, 11, 24, 13, HAIR)
fill(skin, 9, 13, 15, 14, SKIN_DARK, 4)  # nose shadow and stubble
fill(skin, 10, 14, 14, 15, (110, 70, 52), 4)  # mouth
for x in (9, 10, 13, 14):  # blank white eyes
    skin[12][x] = EYE + (255,)
# body (8x12x4 at 16,16), arms (4x12x4 at 40,16) with short sleeves, legs (4x12x4 at 0,16)
box(skin, 16, 16, 8, 12, 4, SHIRT)
box(skin, 40, 16, 4, 12, 4, SKIN)
fill(skin, 44, 16, 48, 20, SHIRT)  # shoulder
fill(skin, 40, 20, 56, 24, SHIRT)  # sleeve
box(skin, 0, 16, 4, 12, 4, PANTS)
fill(skin, 0, 29, 16, 32, SHOES)
fill(skin, 8, 16, 12, 20, SHOES)  # sole
png(os.path.join(OUT, "herobrine.png"), skin)

eyes = blank()
for x in (9, 10, 13, 14):
    eyes[12][x] = (255, 255, 255, 255)
png(os.path.join(OUT, "herobrine_eyes.png"), eyes)
print("wrote herobrine.png and herobrine_eyes.png")
