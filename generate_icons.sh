#!/bin/bash
# Generate cat-themed app icons using Python
python3 << 'PYEOF'
from PIL import Image, ImageDraw, ImageFont
import os
import math

# Cat face colors
BG_COLOR = (255, 138, 101)  # CatOrange
DARK_COLOR = (230, 74, 25)  # CatOrangeDark
LIGHT_COLOR = (255, 171, 145)  # CatOrangeLight
CREAM = (255, 243, 224)
WHITE = (255, 255, 255)
DARK_BROWN = (62, 39, 35)

def draw_cat_face(draw, cx, cy, size):
    """Draw a cute cat face"""
    r = size // 2
    
    # Ears (triangles)
    ear_size = size // 3
    # Left ear
    draw.polygon([
        (cx - r + 5, cy - r + 10),
        (cx - r - 5, cy - r - ear_size),
        (cx - r + ear_size, cy - r + 5)
    ], fill=DARK_BROWN, outline=DARK_COLOR)
    # Right ear
    draw.polygon([
        (cx + r - 5, cy - r + 10),
        (cx + r + 5, cy - r - ear_size),
        (cx + r - ear_size, cy - r + 5)
    ], fill=DARK_BROWN, outline=DARK_COLOR)
    
    # Inner ears
    inner_ear = ear_size - 8
    draw.polygon([
        (cx - r + 10, cy - r + 12),
        (cx - r + 2, cy - r - inner_ear + 5),
        (cx - r + ear_size - 3, cy - r + 10)
    ], fill=(248, 187, 208))
    draw.polygon([
        (cx + r - 10, cy - r + 12),
        (cx + r - 2, cy - r - inner_ear + 5),
        (cx + r - ear_size + 3, cy - r + 10)
    ], fill=(248, 187, 208))
    
    # Face circle
    face_r = r - 2
    draw.ellipse([cx - face_r, cy - face_r + 5, cx + face_r, cy + face_r + 5], fill=CREAM, outline=DARK_BROWN, width=2)
    
    # Eyes
    eye_y = cy - size // 10
    eye_spacing = size // 3
    eye_r = size // 10
    
    # Left eye
    draw.ellipse([cx - eye_spacing - eye_r, eye_y - eye_r, cx - eye_spacing + eye_r, eye_y + eye_r], fill=WHITE)
    draw.ellipse([cx - eye_spacing - eye_r//2, eye_y - eye_r//2, cx - eye_spacing + eye_r//2, eye_y + eye_r//2], fill=DARK_BROWN)
    draw.ellipse([cx - eye_spacing - eye_r//4, eye_y - eye_r//4 - 1, cx - eye_spacing + eye_r//4, eye_y + eye_r//4 - 1], fill=WHITE)
    
    # Right eye
    draw.ellipse([cx + eye_spacing - eye_r, eye_y - eye_r, cx + eye_spacing + eye_r, eye_y + eye_r], fill=WHITE)
    draw.ellipse([cx + eye_spacing - eye_r//2, eye_y - eye_r//2, cx + eye_spacing + eye_r//2, eye_y + eye_r//2], fill=DARK_BROWN)
    draw.ellipse([cx + eye_spacing - eye_r//4, eye_y - eye_r//4 - 1, cx + eye_spacing + eye_r//4, eye_y + eye_r//4 - 1], fill=WHITE)
    
    # Nose
    nose_y = cy + size // 12
    draw.polygon([
        (cx - 4, nose_y - 3),
        (cx + 4, nose_y - 3),
        (cx, nose_y + 3)
    ], fill=(248, 187, 208))
    
    # Mouth
    mouth_y = nose_y + 6
    draw.arc([cx - 8, mouth_y - 4, cx, mouth_y + 4], 0, 180, fill=DARK_BROWN, width=1)
    draw.arc([cx, mouth_y - 4, cx + 8, mouth_y + 4], 0, 180, fill=DARK_BROWN, width=1)
    
    # Whiskers
    whisker_y = nose_y
    # Left whiskers
    draw.line([(cx - 15, whisker_y - 5), (cx - size//2 + 5, whisker_y - 3)], fill=DARK_BROWN, width=1)
    draw.line([(cx - 15, whisker_y), (cx - size//2 + 5, whisker_y + 1)], fill=DARK_BROWN, width=1)
    draw.line([(cx - 15, whisker_y + 5), (cx - size//2 + 5, whisker_y + 6)], fill=DARK_BROWN, width=1)
    # Right whiskers
    draw.line([(cx + 15, whisker_y - 5), (cx + size//2 - 5, whisker_y - 3)], fill=DARK_BROWN, width=1)
    draw.line([(cx + 15, whisker_y), (cx + size//2 - 5, whisker_y + 1)], fill=DARK_BROWN, width=1)
    draw.line([(cx + 15, whisker_y + 5), (cx + size//2 - 5, whisker_y + 6)], fill=DARK_BROWN, width=1)

def create_icon(size, output_path):
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Background with rounded corners
    margin = 2
    draw.rounded_rectangle(
        [margin, margin, size - margin, size - margin],
        radius=size // 5,
        fill=BG_COLOR
    )
    
    # Draw cat face
    draw_cat_face(draw, size // 2, size // 2 + size // 20, size * 7 // 10)
    
    img.save(output_path, 'PNG')
    print(f"Created: {output_path} ({size}x{size})")

# Generate icons for all densities
base_path = "/root/cat-tracker/android/app/src/main/res"
sizes = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}

for folder, size in sizes.items():
    path = f"{base_path}/{folder}"
    os.makedirs(path, exist_ok=True)
    create_icon(size, f"{path}/ic_launcher.png")
    create_icon(size, f"{path}/ic_launcher_round.png")

print("All icons generated!")
PYEOF
