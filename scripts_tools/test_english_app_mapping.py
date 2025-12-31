
import openai
from datetime import datetime

# Config
API_KEY = "YOUR_API_KEY_HERE"
BASE_URL = "https://open.bigmodel.cn/api/paas/v4/"
MODEL = "autoglm-phone"

openai.api_key = API_KEY
openai.api_base = BASE_URL

# The SAME English Prompt we just added
DATE_STR = datetime.today().strftime("%Y-%m-%d")
SYSTEM_PROMPT_EN = (
    f"Today is: {DATE_STR}\n"
    "You are an intelligent mobile automation assistant.\n\n"
    "OUTPUT FORMAT:\n"
    "<think>Reasoning in English</think>\n"
    "<answer>Command</answer>\n\n"
    "COMMANDS:\n"
    "- do(action=\"Launch\", app=\"Name\")\n"
)

# Test Cases
tasks = [
    "Open WeChat",
    "Launch RED (Xiaohongshu)",
    "Order food on Meituan",
    "Open Douyin"
]

print("=== Testing English Intent Understanding ===")

for task in tasks:
    print(f"\n[Task]: {task}")
    messages = [
        {"role": "system", "content": SYSTEM_PROMPT_EN},
        {"role": "user", "content": f"Task: {task} (Step 1). The screen shows desktop icons: [微信] [小红书] [美团] [抖音]."}
    ]
    
    try:
        response = openai.ChatCompletion.create(
            model=MODEL,
            messages=messages,
            max_tokens=200,
            temperature=0.1
        )
        content = response['choices'][0]['message']['content']
        print(f"[AI Response]:\n{content}")
        
        # Analyze match
        if "<think>" in content and "do(" in content:
            # Check if thought is English
            # Simple heuristic: check for common English words vs Chinese characters
            is_english_think = len([c for c in content.split("</think>")[0] if u'\u4e00' <= c <= u'\u9fff']) < 5
            print(f"> Thought is English? {is_english_think}")
            
            # Extract App Name
            import re
            m = re.search(r'app=["\'](.+?)["\']', content)
            if m:
                app_name = m.group(1)
                print(f"> Target App Name: '{app_name}'")
            else:
                print("> No app name found.")
        else:
            print("> Format Invalid.")
            
    except Exception as e:
        print(f"Error: {e}")
