import android_helper
import openai
import json
import base64
import re
from io import BytesIO
import time
from datetime import datetime

# 获取当前日期
today = datetime.today()
weekday_names = ["星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"]
weekday = weekday_names[today.weekday()]
formatted_date = today.strftime("%Y年%m月%d日") + " " + weekday

# 系统提示词 - 使用原项目的完整 prompt
SYSTEM_PROMPT = (
    "今天的日期是: " + formatted_date + """
你是一个智能手机自动化助手。

【关键规则】
1. 🛑 **停止信号**：如果屏幕充满红色，或有红色边框/横幅 -> 输出 <answer>finish(message="收到停止信号")</answer>
2. 🚀 **启动应用**：如果当前界面是 "AutoGLM/PHONE"、"任务日志" 或 "桌面" -> **必须**输出 Launch 指令启动目标应用！例如：<answer>do(action="Launch", app="12306")</answer>。**严禁**在AutoGLM界面点击任何按钮！
3. ✅ **任务执行**：只在目标APP（如12306、美团）内进行点击和滑动操作。

输出格式：
<think>你的思考</think>
<answer>操作指令</answer>

操作指令集：
- do(action="Launch", app="xxx")
- do(action="Tap", element=[x,y])
- do(action="Type", text="xxx")
- do(action="Swipe", start=[x1,y1], end=[x2,y2])
- do(action="Back")
- do(action="Home")
- do(action="Wait")
- finish(message="xxx")

注意：输入文本后通常需要点击键盘上的搜索/回车键(坐标约[950,950])。

【特别规则：美团店内搜索】
在美团应用内（特别是在店铺内搜索商品时）：
1. ⚠️ **输入前必须点击**：在执行 `Type` 输入文字之前，**必须**先执行 `Tap` 点击输入框，确保键盘弹出且光标闪烁。这是输入成功的关键！
2. ✅ **优先点击"热门搜索"**：如果既有热门词又有搜索框，优先点击热门词（效率更高）。
3. 🔄 **输入失败处理**：如果 `Type` 后没有反应，请尝试再点击一次输入框，或者点击左上角返回。
4. 📏 **长屏幕修正**：对于底部弹窗按钮（如"加入购物车"），点击时请自觉将 Y 坐标上移 15%，例如目标在 Y=800，请输出 Y=680。
5. 🛒 **购物流程**：添加商品到购物车后，**必须关闭弹窗**（点击弹窗外或关闭按钮），然后点击页面底部的**"去结算"按钮**完成购买。不要重复打开商品弹窗！

"""
)

class SimplePhoneAgent:
    def __init__(self, api_key, base_url, model_name, language="Chinese"):
        self.api_key = api_key
        self.base_url = base_url
        self.model_name = model_name
        self.language = language
        
        # 配置 openai（旧版 SDK 0.28.1 的方式）
        openai.api_key = api_key
        openai.api_base = base_url
        
        # 🔥 使用用户配置的 API 模型与地址
        self.model_name = model_name if model_name else "autoglm-phone"
        self.url = self.base_url
        self.max_steps = 40  # 原始最大步数
        self.dynamic_max_steps = 40  # 动态最大步数（可被停止按钮修改）
        self.current_step = 0  # 当前执行到第几步
        self.history = []
        self.current_app = None  # 🔥 记录当前 App，用于分应用策略
        
        # 防死循环机制
        self.recent_actions = []  # 记录最近的动作
        self.max_repeat_count = 3  # 允许的最大重复次数
        
        # AutoGLM-Phone-9B 使用 1000x1000 归一化坐标系
        self.model_width = 1000
        self.model_height = 1000



    def _scale_coordinates(self, x, y):
        """Scale coordinates from model reference (1000x1000) to device actual resolution"""
        # 动态获取当前设备尺寸
        device_width, device_height = android_helper.get_display_metrics()
        
        scaled_x = int(x * device_width / self.model_width)
        scaled_y = int(y * device_height / self.model_height)
        
        return scaled_x, scaled_y

    def _parse_action(self, response_text):
        """解析 AI 返回的动作指令 - 支持 XML 格式"""
        response_text = response_text.strip()
        
        # 1. 尝试提取 <answer> 标签中的内容
        answer_match = re.search(r'<answer>(.*?)</answer>', response_text, re.DOTALL | re.IGNORECASE)
        if answer_match:
            action_str = answer_match.group(1).strip()
        else:
            action_str = response_text
        
        # 2. 解析 do(action="...", ...) 格式
        do_match = re.search(r'do\s*\(\s*action\s*=\s*["\'](\w+)["\']', action_str, re.IGNORECASE)
        if do_match:
            action_type = do_match.group(1).lower()
            
            if action_type == "launch":
                app_match = re.search(r'app\s*=\s*["\'](.+?)["\']', action_str)
                if app_match:
                    app_name = app_match.group(1)
                    self.current_app = app_name  # 🔥 更新当前 App
                    return ('launch', app_name)
            
            elif action_type == "tap":
                elem_match = re.search(r'element\s*=\s*\[(\d+)\s*,\s*(\d+)\]', action_str)
                if elem_match:
                    x, y = int(elem_match.group(1)), int(elem_match.group(2))
                    return ('tap', x, y)
            
            elif action_type == "type":
                text_match = re.search(r'text\s*=\s*["\'](.+?)["\']', action_str)
                if text_match:
                    return ('input', text_match.group(1))
            
            elif action_type == "swipe":
                start_match = re.search(r'start\s*=\s*\[(\d+)\s*,\s*(\d+)\]', action_str)
                end_match = re.search(r'end\s*=\s*\[(\d+)\s*,\s*(\d+)\]', action_str)
                if start_match and end_match:
                    x1, y1 = int(start_match.group(1)), int(start_match.group(2))
                    x2, y2 = int(end_match.group(1)), int(end_match.group(2))
                    return ('swipe', x1, y1, x2, y2)
            
            elif action_type == "back":
                return ('back',)
            
            elif action_type == "home":
                return ('home',)
                
            elif action_type == "wait":
                return ('wait',)
        
        # 3. 解析 finish(message="...") 格式
        finish_match = re.search(r'finish\s*\(\s*(?:message\s*=\s*)?["\'](.*?)["\']', action_str, re.IGNORECASE)
        if finish_match:
            return ('finish', finish_match.group(1)) # 提取消息
        
        # 简单的 finish 检查
        if "finish" in action_str.lower():
            return ('finish', "任务完成")
        
        return None
    
    def _check_repeated_action(self, action, log_callback):
        """
        检测是否重复执行相同的动作（防死循环）
        
        Args:
            action: 当前要执行的动作
            log_callback: 日志回调
            
        Returns:
            True 如果检测到重复，False 否则
        """
        if not action or action[0] in ['finish', 'wait', 'launch', 'back']:
            # finish/wait/launch/back 不参与重复检测
            return False
        
        # 将动作转换为字符串用于比较
        action_str = str(action)
        
        # 添加到最近动作列表
        self.recent_actions.append(action_str)
        
        # 只保留最近 10 个动作
        if len(self.recent_actions) > 10:
            self.recent_actions.pop(0)
        
        # 检查最近的动作是否有重复
        if len(self.recent_actions) >= self.max_repeat_count:
            # 检查最后 N 个动作是否完全相同
            last_n_actions = self.recent_actions[-self.max_repeat_count:]
            if len(set(last_n_actions)) == 1:
                log_callback.onLog(f"⚠️ 检测到重复动作 {self.max_repeat_count} 次: {action_str}")
                log_callback.onLog(f"💡 建议: AI 可能陷入死循环，需要改变策略")
                
                # 🔥 不再自动执行 go_back()，而是让 AI 自己决定
                # 清空最近动作历史，给 AI 一个"新开始"
                self.recent_actions.clear()
                
                return True
        
        return False
    
    def request_graceful_stop(self, buffer_steps=1):
        """
        请求优雅停止：不立即中断，而是让 AI 再执行 buffer_steps 步后停止
        
        Args:
            buffer_steps: 缓冲步数，默认 1 步
        """
        old_limit = self.dynamic_max_steps
        self.dynamic_max_steps = self.current_step + buffer_steps
        print(f"🛑 收到停止请求：当前第 {self.current_step} 步，将在第 {self.dynamic_max_steps} 步后停止（原限制: {old_limit}）")
        return self.dynamic_max_steps

    def run(self, task, log_callback):
        log_callback.onLog(f"[*] 开始执行任务: {task}")
        
        # 检查服务是否就绪
        log_callback.onLog("[>] 检查无障碍服务...")
        if not android_helper.is_ready():
            log_callback.onLog("[X] 无障碍服务未就绪")
            log_callback.onLog("请确保:")
            log_callback.onLog("1. 已开启无障碍权限")
            log_callback.onLog("2. HTTP 服务器正在运行 (端口 8080)")
            return False
        log_callback.onLog("[OK] 无障碍服务已就绪")
        
        # 重置停止标志
        android_helper.set_stop(False)
        
        # 初始化消息历史 (放在循环外)
        self.messages = [
            {"role": "system", "content": SYSTEM_PROMPT}
        ]
        
        # 重置动态步数限制
        self.dynamic_max_steps = self.max_steps
        
        for step in range(self.max_steps):
            self.current_step = step + 1
            
            # 🔥 STRICT STOP CHECK: If we exceeded the dynamic limit, stop immediately.
            if self.current_step > self.dynamic_max_steps:
                log_callback.onLog(f"\n[!] 用户请求停止，已在第 {self.current_step-1} 步优雅退出")
                break
            
            # 检查旧的停止标志（保留兼容性）
            if android_helper.should_stop():
                log_callback.onLog("\n[STOP] 任务已由用户终止")
                break

            log_callback.onLog(f"\n[#] 步骤 {self.current_step}/{self.dynamic_max_steps}")
            
            # 🔥 CRITICAL: Double Check Stop Signal BEFORE Heavy Operations (Screenshot)
            if self.current_step > self.dynamic_max_steps or android_helper.should_stop():
                log_callback.onLog("\n[STOP] 任务已由用户强制终止 (Pre-Screenshot)")
                break

            # 1. Take screenshot
            image = android_helper.take_screenshot()
            if image is None:
                log_callback.onLog("\n[X] 无法获取截图")
                return False
            
            # 记录截图大小
            log_callback.onLog(f"\n[IMG] 截图尺寸: {image.size}")
            
            # Encode image
            buffered = BytesIO()
            image.save(buffered, format="PNG")
            img_str = base64.b64encode(buffered.getvalue()).decode()
            
            # Construct prompt
            if step == 0:
                # 🚀 第一步强制引导：不管看到什么，强制 Launch
                # 提取可能的 APP 名称（简单的启发式：取任务的前5个字，或者直接让 AI 决定）
                # 这里我们用通用的强指令
                prompt = (
                    f"任务: {task} (Step 1)\n\n"
                    "⚠️【特殊阶段指令】⚠️\n"
                    "当前画面仅仅是自动化助手的控制台，并不是目标应用。\n"
                    "请完全忽略画面中的按钮（如'开始执行'）！\n"
                    "你的这一步操作**只能**是：使用 Launch 指令启动目标应用！\n"
                    "例如：do(action=\"Launch\", app=\"目标App名称\")"
                )
                log_callback.onLog("[i] 已注入第一步强制启动指令")
            else:
                prompt = f"任务: {task} (Step {step+1})"
            
            content = [
                {"type": "text", "text": prompt},
                {"type": "image_url", "image_url": {"url": f"data:image/png;base64,{img_str}"}}
            ]
            
            # 3. Call AI
            log_callback.onLog(f"[PKG] 图片大小: {len(img_str)} bytes (base64)") # Updated log message for base64 string length
            log_callback.onLog("[AI] 正在思考...")
            try:
                # 构造当前用户消息（直接使用上面构建的 content）
                current_user_message = {
                    "role": "user",
                    "content": content
                }
                
                # 临时构建用于发送的消息列表 (System + History + Current)
                # 注意：history 里的旧图片已经被去除了
                messages_to_send = self.messages + [current_user_message]
                
                log_callback.onLog(f"[API] 调用: {self.model_name}")
                
                # 带 429 智能重试机制的调用逻辑
                max_retries = 3
                response = None
                for attempt in range(max_retries):
                    try:
                        response = openai.ChatCompletion.create(
                            model=self.model_name,
                            messages=messages_to_send,
                            max_tokens=300,
                            temperature=0.1
                        )
                        break
                    except Exception as api_err:
                        err_str = str(api_err)
                        if "429" in err_str or "RATE_LIMIT" in err_str:
                            if attempt < max_retries - 1:
                                log_callback.onLog(f"[!] 触发每分钟频率限制 (429)，等待 12 秒后自动重试 ({attempt + 1}/{max_retries})...")
                                time.sleep(12)
                                continue
                        raise api_err
                
                content = response['choices'][0]['message']['content']
                log_callback.onLog(f"[<] AI 回复:\n{content[:200]}...\n")
                
                # === 关键：更新历史记忆 ===
                
                # 1. 将当前用户消息加入历史，但我们要【移除图片】以节省 Token
                # 我们只保留文本描述，告诉 AI "你在这个步骤看到了截图"
                text_only_content = [
                    {"type": "text", "text": f"任务: {task} (Step {step+1}) [Screenshot provided]"}
                ]
                self.messages.append({"role": "user", "content": text_only_content})
                
                # 2. 将 AI 的回复加入历史
                self.messages.append({"role": "assistant", "content": content})
                
                # 4. Parse and execute action
                
                # 4. Parse and execute action
                log_callback.onLog("[?] 解析动作...")
                action = self._parse_action(content)
                
                if not action:
                    log_callback.onLog(f"[!] 无法解析动作")
                    continue
                
                log_callback.onLog(f"[OK] 动作: {action[0]}")
                
                # 🔥 检测重复动作（防死循环）
                if self._check_repeated_action(action, log_callback):
                    # 检测到重复，向 AI 注入警告信息
                    warning_message = (
                        f"⚠️ 系统检测: 你已经连续 {self.max_repeat_count} 次执行相同的操作 {action}，"
                        "但页面没有变化。这说明当前操作无效。\n"
                        "请尝试：\n"
                        "1. 🛒 **如果在美团购物**：商品已添加到购物车后，不要重复打开商品弹窗！应该关闭弹窗，然后点击页面底部的\"去结算\"按钮\n"
                        "2. 点击不同的坐标位置（例如列表项的中心或下方）\n"
                        "3. ⚠️ 如果是点击弹窗按钮无效，尝试大幅降低 Y 坐标（例如 Y-100）\n"
                        "4. 使用 Swipe 滑动查看更多内容\n"
                        "5. 使用 Back 返回重新操作\n"
                        "6. 如果任务已完成，使用 finish() 结束"
                    )
                    self.messages.append({
                        "role": "user",
                        "content": [{"type": "text", "text": warning_message}]
                    })
                    log_callback.onLog("💡 已向 AI 注入防死循环警告")
                
                if action[0] == 'finish':
                    message = action[1] if len(action) > 1 else "任务已完成"
                    log_callback.onLog(f"[OK] {message}")
                    return True  # 🎯 Task successfully finished
                
                elif action[0] == 'launch':
                    # 🔥 检查停止标志（避免干扰用户）
                    if self.current_step >= self.dynamic_max_steps or android_helper.should_stop():
                        log_callback.onLog("[!] 检测到停止信号，跳过启动APP")
                        continue  # 跳过此动作，继续下一步
                    
                    _, app_name = action
                    self.current_app = app_name # 🔥 更新当前 App
                    log_callback.onLog(f"[APP] 正在启动: {app_name}")
                    
                    # 捕获 print 输出
                    import sys
                    from io import StringIO
                    old_stdout = sys.stdout
                    sys.stdout = StringIO()
                    
                    success = android_helper.launch_app(app_name)
                    
                    # 获取输出并显示
                    output = sys.stdout.getvalue()
                    sys.stdout = old_stdout
                    
                    if output:
                        for line in output.strip().split('\n'):
                            log_callback.onLog(f"  {line}")
                    
                    if success:
                        log_callback.onLog(f"[OK] 启动成功: {app_name}")
                    else:
                        log_callback.onLog(f"[X] 启动失败: {app_name}")
                
                elif action[0] == 'wait':
                    log_callback.onLog(f"[...] 等待页面加载...")
                    time.sleep(2)
                
                elif action[0] == 'tap':
                    # 🔥 检查停止标志（避免干扰用户）
                    if self.current_step >= self.dynamic_max_steps or android_helper.should_stop():
                        log_callback.onLog("[!] 检测到停止信号，跳过点击动作")
                        continue
                    
                    _, x, y = action
                    scaled_x, scaled_y = self._scale_coordinates(x, y)
                    android_helper.click(scaled_x, scaled_y)
                    log_callback.onLog(f"[TAP] 点击 ({x},{y}) -> ({scaled_x},{scaled_y})")
                    
                    # 🔧 智能等待：点击中下部时等待弹窗展开
                    # 弹窗触发按钮(选规格 Y≈240, 加入购物车 Y≈770)
                    if y > 200:
                        time.sleep(0.5)
                        log_callback.onLog(f"[...] 等待弹窗展开 (500ms)")
                
                elif action[0] == 'swipe':
                    # 🔥 检查停止标志（避免干扰用户）
                    if self.current_step >= self.dynamic_max_steps or android_helper.should_stop():
                        log_callback.onLog("[!] 检测到停止信号，跳过滑动动作")
                        continue
                    
                    _, x1, y1, x2, y2 = action
                    sx1, sy1 = self._scale_coordinates(x1, y1)
                    sx2, sy2 = self._scale_coordinates(x2, y2)
                    android_helper.swipe(sx1, sy1, sx2, sy2)
                    log_callback.onLog(f"[SWIPE] 滑动")
                
                elif action[0] == 'input':
                    # 🔥 检查停止标志（避免干扰用户）
                    if self.current_step >= self.dynamic_max_steps or android_helper.should_stop():
                        log_callback.onLog("[!] 检测到停止信号，跳过输入动作")
                        continue
                    
                    _, text = action
                    # 🔥 传递当前 App 名称，触发特殊策略
                    android_helper.input_text(text, app_name=self.current_app)
                    log_callback.onLog(f"[TYPE] 输入: {text}")
                
                elif action[0] == 'back':
                    # 🔥 检查停止标志（避免干扰用户）
                    if self.current_step >= self.dynamic_max_steps or android_helper.should_stop():
                        log_callback.onLog("[!] 检测到停止信号，跳过返回动作")
                        continue
                    
                    log_callback.onLog(f"[<-] 返回")
                    android_helper.go_back()
                
                elif action[0] == 'home':
                    # 🔥 检查停止标志（避免干扰用户）
                    if self.current_step >= self.dynamic_max_steps or android_helper.should_stop():
                        log_callback.onLog("[!] 检测到停止信号，跳过Home动作")
                        continue
                    
                    log_callback.onLog(f"[HOME] 主屏幕")
                    android_helper.go_home()
                
                time.sleep(2)
                
            except Exception as e:
                log_callback.onLog(f"[ERR] 错误: {str(e)}")
                import traceback
                log_callback.onLog(traceback.format_exc())
                return False
        
        # Loop ended without finish() - means stopped/aborted
        return False


# 全局 agent 实例（供 Kotlin 调用停止函数）
_current_agent = None

def run_task(api_key, base_url, model_name, task, log_callback, language="Chinese"):
    global _current_agent
    _current_agent = SimplePhoneAgent(api_key, base_url, model_name, language)
    result = _current_agent.run(task, log_callback)
    _current_agent = None  # 任务结束后清空
    return result  # Return True if finished, False if aborted

def stop_gracefully(buffer_steps=1):
    """
    优雅停止当前任务
    供 Kotlin 调用：android_helper.stop_gracefully()
    
    Args:
        buffer_steps: 缓冲步数，默认 1
    
    Returns:
        停止后的最大步数，如果没有正在运行的任务则返回 -1
    """
    global _current_agent
    if _current_agent:
        return _current_agent.request_graceful_stop(buffer_steps)
    else:
        print("⚠️ 没有正在运行的任务")
        return -1

