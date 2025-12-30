
import wave
import math
import struct
import random

def clamp(val):
    return max(-32767, min(32767, int(val)))

def generate_complete_sound(filename):
    # COMPLETE: Success Chime
    sample_rate = 44100
    duration = 1.2
    num_samples = int(sample_rate * duration)
    
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(sample_rate)
        
        notes = [1046.5, 1318.5, 1568.0]
        
        for i in range(num_samples):
            t = float(i) / sample_rate
            signal = 0
            for idx, freq in enumerate(notes):
                start_t = idx * 0.08
                if t >= start_t:
                    local_t = t - start_t
                    wave = math.sin(2 * math.pi * freq * local_t)
                    env = math.exp(-local_t * 5)
                    signal += wave * env * 0.3
            
            value = signal * 25000
            data = struct.pack('<h', clamp(value))
            wav_file.writeframes(data)

def generate_click_sound(filename):
    # CLICK: UI Menu Open
    sample_rate = 44100
    duration = 0.15
    num_samples = int(sample_rate * duration)
    
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(sample_rate)
        
        for i in range(num_samples):
            t = float(i) / sample_rate
            click_env = math.exp(-t * 200)
            click = random.uniform(-1, 1) * click_env
            thud_env = math.exp(-t * 50)
            thud = math.sin(2 * math.pi * 150 * t) * thud_env
            signal = click * 0.4 + thud * 0.6
            value = signal * 20000
            data = struct.pack('<h', clamp(value))
            wav_file.writeframes(data)

if __name__ == "__main__":
    generate_complete_sound("android-app/app/src/main/res/raw/sfx_complete.wav")
    generate_click_sound("android-app/app/src/main/res/raw/sfx_click.wav")
    print("Aux SFX Generated.")
