
import wave
import math
import struct
import random

def clamp(val):
    return max(-32767, min(32767, int(val)))

def generate_execute_sound(filename):
    # EXECUTE V2: Cyberpunk Engine Start
    # Structure: Deep Rumble + Digital Sweep + Metallic Lock
    sample_rate = 44100
    duration = 0.8
    num_samples = int(sample_rate * duration)
    
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(sample_rate)
        
        for i in range(num_samples):
            t = float(i) / sample_rate
            base_freq = 60 + (t * 40) 
            base_wave = (math.fmod(t * base_freq, 1.0) * 2 - 1) * 0.4
            
            sweep_freq = 400 * (1.5 ** (t * 10))
            if sweep_freq > 4000: sweep_freq = 4000
            sci_fi_wave = math.sin(2 * math.pi * sweep_freq * t * 0.5) * 0.3
            lfo = math.sin(2 * math.pi * 30 * t) 
            sci_fi_wave *= (0.8 + 0.2 * lfo)
            
            signal = base_wave + sci_fi_wave
            
            if t < 0.1: amp = t / 0.1
            elif t > duration - 0.1: amp = (duration - t) / 0.1
            else: amp = 1.0
            
            if t > duration - 0.05:
                click_freq = 1000
                click = math.sin(2 * math.pi * click_freq * t) * 0.5
                signal += click
            
            value = signal * 20000 * amp
            data = struct.pack('<h', clamp(value))
            wav_file.writeframes(data)

def generate_abort_sound(filename):
    # ABORT V3: Continuous Interference
    sample_rate = 44100
    duration = 1.0
    num_samples = int(sample_rate * duration)
    
    with wave.open(filename, 'w') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(sample_rate)
        
        for i in range(num_samples):
            t = float(i) / sample_rate
            freq1 = 150
            freq2 = 158
            wave1 = 1.0 if math.sin(2 * math.pi * freq1 * t) > 0 else -1.0
            wave2 = 1.0 if math.sin(2 * math.pi * freq2 * t) > 0 else -1.0
            chopper_rate = 15
            chopper = 1.0 if math.sin(2 * math.pi * chopper_rate * t) > 0 else 0.0
            
            signal = (wave1 + wave2) * 0.5 * chopper
            static = random.uniform(-0.3, 0.3) * chopper
            signal += static
            
            amp = 1.0
            if t > duration - 0.1:
                amp = (duration - t) / 0.1
                
            value = signal * 15000 * amp
            data = struct.pack('<h', clamp(value))
            wav_file.writeframes(data)

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
    generate_execute_sound("android-app/app/src/main/res/raw/sfx_execute.wav")
    generate_abort_sound("android-app/app/src/main/res/raw/sfx_abort.wav")
    generate_complete_sound("android-app/app/src/main/res/raw/sfx_complete.wav")
    generate_click_sound("android-app/app/src/main/res/raw/sfx_click.wav")
    print("ALL SFX Generated.")
