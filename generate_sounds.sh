#!/bin/bash
# 生成猫叫声音频文件（WAV格式）
# 使用 Python 生成不同频率的正弦波模拟猫叫声

python3 << 'PYEOF'
import struct
import math
import os

def generate_tone(filename, base_freq, duration_ms, sample_rate=44100, 
                  freq_variation=0, amplitude=0.5, fade_in=True, fade_out=True):
    """生成一个简单的音调 WAV 文件"""
    num_samples = int(sample_rate * duration_ms / 1000)
    samples = []
    
    for i in range(num_samples):
        t = i / sample_rate
        # 频率变化（模拟猫叫的声调变化）
        freq = base_freq + freq_variation * math.sin(2 * math.pi * 3 * t)
        # 生成正弦波
        value = amplitude * math.sin(2 * math.pi * freq * t)
        
        # 淡入淡出
        if fade_in and i < num_samples * 0.1:
            value *= i / (num_samples * 0.1)
        if fade_out and i > num_samples * 0.7:
            value *= (num_samples - i) / (num_samples * 0.3)
        
        # 添加一些谐波让声音更丰富
        value += amplitude * 0.3 * math.sin(2 * math.pi * freq * 2 * t)
        value += amplitude * 0.15 * math.sin(2 * math.pi * freq * 3 * t)
        
        samples.append(max(-1.0, min(1.0, value)))
    
    # 写入 WAV 文件
    with open(filename, 'wb') as f:
        # WAV 头部
        data_size = num_samples * 2  # 16-bit mono
        f.write(b'RIFF')
        f.write(struct.pack('<I', 36 + data_size))
        f.write(b'WAVE')
        f.write(b'fmt ')
        f.write(struct.pack('<I', 16))  # chunk size
        f.write(struct.pack('<H', 1))   # PCM format
        f.write(struct.pack('<H', 1))   # mono
        f.write(struct.pack('<I', sample_rate))
        f.write(struct.pack('<I', sample_rate * 2))  # byte rate
        f.write(struct.pack('<H', 2))   # block align
        f.write(struct.pack('<H', 16))  # bits per sample
        f.write(b'data')
        f.write(struct.pack('<I', data_size))
        
        for sample in samples:
            f.write(struct.pack('<h', int(sample * 32767)))

output_dir = "/root/cat-tracker/android/app/src/main/res/raw"

# 猫叫声参数: (文件名, 基础频率Hz, 时长ms, 频率变化)
cat_sounds = [
    # 摸头 - 轻柔短促的 "喵"
    ("meow_pet", 800, 200, 200),
    # 挠下巴 - 舒服的 "呼噜" 
    ("meow_scratch", 600, 300, 400),
    # 撸肚子 - 低沉的 "呜"
    ("meow_belly", 500, 400, 300),
    # 喂食 - 开心的 "喵喵"
    ("meow_feed", 1000, 350, 500),
    # 零食 - 兴奋的 "咪"
    ("meow_snack", 1200, 250, 600),
    # 罐头 - 期待的 "喵呜"
    ("meow_can", 900, 450, 400),
    # 逗猫棒 - 欢快的 "喵喵喵"
    ("meow_play", 1100, 300, 700),
    # 毛线球 - 好奇的 "咪?"
    ("meow_ball", 950, 200, 350),
    # 激光笔 - 兴奋的 "喵！"
    ("meow_laser", 1300, 200, 800),
    # 洗澡 - 不开心的 "呜..."
    ("meow_bath", 400, 500, 100),
    # 梳毛 - 舒服的 "呼噜"
    ("meow_brush", 550, 350, 200),
    # 睡觉 - 安静的 "zzz"
    ("meow_sleep", 300, 600, 50),
    # 成就解锁 - 欢快的上升音
    ("sound_achievement", 800, 500, 1000),
    # 购买成功 - 叮咚
    ("sound_purchase", 1000, 300, 2000),
]

for name, freq, duration, variation in cat_sounds:
    filepath = os.path.join(output_dir, f"{name}.wav")
    generate_tone(filepath, freq, duration, freq_variation=variation)
    print(f"Generated: {filepath}")

print("All cat sounds generated!")
PYEOF
