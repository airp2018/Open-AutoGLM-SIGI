
import base64

# Simple synthesized SFX (WAV headers + minimal PCM data) converted to Base64
# to avoid library dependency issues.

# CLICK: Short mechanical tick
B64_CLICK = "UklGRjQAAABXQVZFZm10IBAAAAABAAEAQB8AAEAfAAABAAgAZGF0YQAAAAA=" # Placeholder tiny wav
# Okay, generating real PCM data via bytes is verbose. 
# Re-trying the pure wave library approach but with ABSOLUTE SIMPLEST PATH logic.
# The previous error was likely due to complex path handling. I will write to current dir then move.

import wave, math, struct, random, os, shutil

def clamp(val): return max(-32767, min(32767, int(val)))

def gen_click():
    with wave.open("click_temp.wav", 'w') as f:
        f.setnchannels(1); f.setsampwidth(2); f.setframerate(44100)
        for i in range(int(44100*0.1)):
            val = random.uniform(-1,1) * math.exp(-i/500) * 20000
            f.writeframes(struct.pack('<h', clamp(val)))
            
def gen_complete():
    with wave.open("complete_temp.wav", 'w') as f:
        f.setnchannels(1); f.setsampwidth(2); f.setframerate(44100)
        for i in range(int(44100*1.0)):
            t = i/44100.0
            sig = 0
            for note in [1046, 1318, 1568]: # C6 E6 G6
                if t > [1046, 1318, 1568].index(note)*0.1:
                    local_t = t - [1046, 1318, 1568].index(note)*0.1
                    sig += math.sin(2*math.pi*note*local_t) * math.exp(-local_t*5) * 0.3
            f.writeframes(struct.pack('<h', clamp(sig*25000)))

if __name__ == "__main__":
    try:
        gen_click()
        gen_complete()
        print("Generated locally.")
        
        # Move manually
        target_dir = "android-app/app/src/main/res/raw/"
        shutil.move("click_temp.wav", os.path.join(target_dir, "sfx_click.wav"))
        shutil.move("complete_temp.wav", os.path.join(target_dir, "sfx_complete.wav"))
        print("Moved successfully.")
    except Exception as e:
        print(f"Error: {e}")
