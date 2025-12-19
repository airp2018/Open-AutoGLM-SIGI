from PIL import Image
import os

# 源图片路径
source_image = r"C:\Users\YANQIAO\Documents\Augment\Open-AutoGLM-Hybrid-main\android-app\app\src\main\res\sigillum_icon.jpg"
base_path = r"C:\Users\YANQIAO\Documents\Augment\Open-AutoGLM-Hybrid-main\android-app\app\src\main\res"

# 打开源图片
img = Image.open(source_image)

# 转换为 RGBA（支持透明度）
if img.mode != 'RGBA':
    img = img.convert('RGBA')

# 不同尺寸的图标
sizes = {
    'mipmap-mdpi': 48,
    'mipmap-hdpi': 72,
    'mipmap-xhdpi': 96,
    'mipmap-xxhdpi': 144,
    'mipmap-xxxhdpi': 192
}

# 生成各种尺寸
for folder, size in sizes.items():
    # 调整大小
    resized = img.resize((size, size), Image.Resampling.LANCZOS)
    
    # 保存为 PNG
    output_path = os.path.join(base_path, folder, 'ic_launcher.png')
    resized.save(output_path, 'PNG')
    print(f"已生成: {output_path}")
    
    # 同时保存为 round 版本
    output_path_round = os.path.join(base_path, folder, 'ic_launcher_round.png')
    resized.save(output_path_round, 'PNG')
    print(f"已生成: {output_path_round}")

print("\n✅ 所有图标已成功生成！")
