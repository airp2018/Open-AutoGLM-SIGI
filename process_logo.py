from PIL import Image
import os

def process_image(input_path, output_path):
    print(f"Processing: {input_path}")
    
    # 1. 打开图片并转为 RGBA
    img = Image.open(input_path).convert("RGBA")
    data = img.getdata()
    
    # 2. 抠图：将黑色背景转为透明
    # 阈值：亮度小于 30 的像素都视为背景
    new_data = []
    for item in data:
        # item 是 (R, G, B, A)
        # 如果 RGB 都很低（接近黑），则变透明
        if item[0] < 30 and item[1] < 30 and item[2] < 30:
            new_data.append((0, 0, 0, 0))  # 完全透明
        else:
            new_data.append(item)  # 保留原样（绿色像素）
            
    img.putdata(new_data)
    
    # 3. 自动剪裁（Crop）：去掉四周的透明区域
    bbox = img.getbbox()
    if bbox:
        img = img.crop(bbox)
        print(f"Cropped to: {bbox}")
    
    # 4. 保存
    img.save(output_path, "PNG")
    print(f"Saved transparent image to: {output_path}")

# 执行处理
source_file = r"C:\Users\YANQIAO\Documents\Augment\Open-AutoGLM-Hybrid-main\android-app\app\src\main\res\drawable\title_sigillum_pixel.png"
process_image(source_file, source_file)
