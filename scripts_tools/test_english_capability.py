
import openai
import base64

# Config (User's Key)
API_KEY = "YOUR_API_KEY_HERE"
BASE_URL = "https://open.bigmodel.cn/api/paas/v4/"
MODEL = "autoglm-phone" # This maps to GLM-4V-9B

openai.api_key = API_KEY
openai.api_base = BASE_URL

# Powerful English System Prompt
SYSTEM_PROMPT = (
    "You are a mobile assistant.\n"
    "CRITICAL RULE: You MUST think and match elements in ENGLISH. "
    "Even if the screen is in Chinese, your reasoning (<think>) and answer (<answer>) MUST BE ENGLISH ONLY.\n"
    "Example Output:\n"
    "<think>The user wants to open WeChat. I see the icon labeled '微信' which is WeChat.</think>\n"
    "<answer>do(action='Launch', app='WeChat')</answer>"
)

# User Task (Chinese)
TASK = "打开微信"

messages = [
    {"role": "system", "content": SYSTEM_PROMPT},
    {"role": "user", "content": f"Task: {TASK} (Step 1). screenshot is black for test."}
]

print("Testing English adherence...")
try:
    response = openai.ChatCompletion.create(
        model=MODEL,
        messages=messages,
        max_tokens=300,
        temperature=0.1
    )
    res_content = response['choices'][0]['message']['content']
    print("\n--- Response ---")
    print(res_content)
    print("----------------")
    
    if "think" in res_content and "open" in res_content: # Simple check for English keywords
        print("Verdict: Model CAN output English.")
    else:
        print("Verdict: Model failed to output English.")
        
except Exception as e:
    print(f"Error: {e}")
