import requests
import base64
from io import BytesIO
from PIL import Image, ImageDraw
import subprocess

# HTTP 服务器地址
HELPER_URL = "http://localhost:8080"
LAST_CLICK_POS = (None, None)  # 记录上一次点击的位置，用于夺回焦点

# APP 包名映射
# APP 包名映射
APP_PACKAGES = {
    # 购物/外卖
    "淘宝": "com.taobao.taobao",
    "淘宝闪购": "com.taobao.taobao", # 淘宝内功能
    "京东": "com.jingdong.app.mall",
    "京东秒送": "com.jingdong.pdj", # 京东到家
    "闲鱼": "com.taobao.idlefish",
    "拼多多": "com.xunmeng.pinduoduo",
    "美团": "com.sankuai.meituan",
    "饿了么": "me.ele",
    "肯德基": "com.yum.kfc.brand",
    "大众点评": "com.dianping.v1",

    # 社交/通讯/办公
    "微信": "com.tencent.mm",
    "QQ": "com.tencent.mobileqq",
    "QQ邮箱": "com.tencent.androidqqmail",
    "微博": "com.sina.weibo",
    "小红书": "com.xingin.xhs",
    "知乎": "com.zhihu.android",
    "豆瓣": "com.douban.frodo",
    "飞书": "com.ss.android.lark",
    "豆包": "com.larus.nova",

    # 视频/直播/短剧
    "抖音": "com.ss.android.ugc.aweme",
    "快手": "com.smile.gifmaker",
    "B站": "tv.danmaku.bili",
    "哔哩哔哩": "tv.danmaku.bili",
    "优酷视频": "com.youku.phone",
    "爱奇艺": "com.qiyi.video",
    "腾讯视频": "com.tencent.qqlive",
    "芒果TV": "com.hunantv.imgo.activity",
    "红果短剧": "com.xs.fm",

    # 音乐/音频
    "网易云音乐": "com.netease.cloudmusic",
    "QQ音乐": "com.tencent.qqmusic",
    "汽水音乐": "com.luna.music",
    "喜马拉雅": "com.ximalaya.ting.android",

    # 出行/旅游/地图
    "高德地图": "com.autonavi.minimap",
    "百度地图": "com.baidu.BaiduMap",
    "滴滴出行": "com.sdu.didi.psnger",
    "携程": "ctrip.android.view",
    "携程旅行": "ctrip.android.view",
    "去哪儿": "com.Qunar",
    "去哪儿旅行": "com.Qunar",
    "12306": "com.MobileTicket",
    "铁路12306": "com.MobileTicket",

    # 阅读/资讯
    "今日头条": "com.ss.android.article.news",
    "腾讯新闻": "com.tencent.news",
    "七猫免费小说": "com.kmxs.reader",
    "番茄小说": "com.dragon.read",
    "番茄免费小说": "com.dragon.read",
    "微信读书": "com.tencent.weread",
    
    # 金融/房产
    "支付宝": "com.eg.android.AlipayGphone",
    "同花顺": "com.hexin.plat.android",
    "招商银行": "cmb.pb",
    "贝壳找房": "com.lianjia.beike",
    "安居客": "com.anjuke.android.app",

    # 运动/健康/女性
    "Keep": "com.gotokeep.keep",
    "美柚": "com.seeyouyima.xch",

    # 游戏
    "崩坏：星穹铁道": "com.miHoYo.hkrpg",
    "恋与深空": "com.papegames.lysk.cn",

    # 工具/浏览器
    "有道词典": "com.youdao.dict",
    "百度网盘": "com.baidu.netdisk",
    "夸克": "com.quark.browser",
    "Chrome": "com.android.chrome",
    "Firefox": "org.mozilla.firefox",
    "设置": "com.android.settings",
}

# 全局控制标志
STOP_FLAG = False

def set_stop(stop):
    """设置停止标志"""
    global STOP_FLAG
    STOP_FLAG = stop
    print(f"Stop flag set to: {stop}")

def should_stop():
    """检查是否应该停止"""
    return STOP_FLAG

def is_ready():
    """检查服务是否就绪"""
    try:
        response = requests.get(f"{HELPER_URL}/status", timeout=2)
        if response.status_code == 200:
            data = response.json()
            return data.get('accessibility_enabled', False)
        return False
    except:
        return False

# 全局屏幕尺寸 (默认值，会被截图更新)
SCREEN_WIDTH = 1080
SCREEN_HEIGHT = 2400
SCREENSHOT_COUNTER = 0  # 🔧 调试：截图计数器

def take_screenshot():
    """截取屏幕"""
    global SCREEN_WIDTH, SCREEN_HEIGHT
    try:
        response = requests.get(f"{HELPER_URL}/screenshot", timeout=10)
        if response.status_code == 200:
            data = response.json()
            if data.get('success'):
                img_data = base64.b64decode(data['image'])
                image = Image.open(BytesIO(img_data))
                
                # 更新屏幕尺寸
                if image.width > 0 and image.height > 0:
                    SCREEN_WIDTH = image.width
                    SCREEN_HEIGHT = image.height
                    
                # 🔧 调试：保存截图到手机存储用于对比
                global SCREENSHOT_COUNTER
                SCREENSHOT_COUNTER += 1
                try:
                    save_path = f"/sdcard/apk_screenshot_{SCREENSHOT_COUNTER}.png"
                    image.save(save_path, "PNG")
                    print(f"🔧 [DEBUG] 截图已保存: {save_path}")
                except Exception as save_err:
                    print(f"🔧 [DEBUG] 保存截图失败: {save_err}")
                    
                return image
        return None
    except Exception as e:
        print(f"截图失败: {e}")
        return None

def tap(x, y):
    """点击屏幕"""
    global LAST_CLICK_POS
    LAST_CLICK_POS = (x, y)  # 记录位置
    try:
        response = requests.post(
            f"{HELPER_URL}/tap",
            json={'x': int(x), 'y': int(y)},
            timeout=5
        )
        if response.status_code == 200:
            data = response.json()
            return data.get('success', False)
        return False
    except Exception as e:
        print(f"点击失败: {e}")
        return False

# 保持兼容性别名
click = tap


def swipe(x1, y1, x2, y2, duration=500):
    """滑动屏幕"""
    try:
        response = requests.post(
            f"{HELPER_URL}/swipe",
            json={
                'x1': int(x1),
                'y1': int(y1),
                'x2': int(x2),
                'y2': int(y2),
                'duration': int(duration)
            },
            timeout=10
        )
        if response.status_code == 200:
            data = response.json()
            return data.get('success', False)
        return False
    except Exception as e:
        print(f"滑动失败: {e}")
        return False

def input_text_via_adb_keyboard(text, send_enter=False):
    """使用 ADB Keyboard 输入文本（通过内部 API）
    Args:
        text: 要输入的文本
        send_enter: Boolean, 是否在输入后发送回车/发送键
    """
    import base64
    import time
    
    try:
        print(f"🔄 使用 ADB Keyboard 输入: {text} (Send Enter: {send_enter})")
        
        # 1. 切换到 ADB Keyboard（通过 HTTP 请求 Kotlin 端）
        response = requests.post(
            f"{HELPER_URL}/switch_ime",
            json={'ime': 'com.android.adbkeyboard/.AdbIME'},
            timeout=3
        )
        
        if response.status_code != 200 or not response.json().get('success'):
            print(f"❌ 切换输入法失败")
            return False
        
        print(f"✅ 已切换到 ADB Keyboard")
        time.sleep(2.0)  # 给系统时间绑定
        
        # 🟢 [已移除] 危险逻辑：不要重新点击上一次的位置
        # 原因：页面切换后（如从列表进入聊天），上一次点击的坐标可能对应的是聊天记录（如视频），导致误触
        # if LAST_CLICK_POS[0] is not None:
        #    print(f"🎯 正在重新点击位置 {LAST_CLICK_POS} 以夺回焦点...")
        #    tap(LAST_CLICK_POS[0], LAST_CLICK_POS[1])
        #    time.sleep(0.5)
            
        # 2. 清空输入框
        requests.post(
            f"{HELPER_URL}/adb_broadcast",
            json={'action': 'ADB_CLEAR_TEXT'},
            timeout=3
        )
        time.sleep(0.3)
        
        # 3. 输入文本（Base64 编码支持中文）
        encoded_text = base64.b64encode(text.encode('utf-8')).decode('utf-8')
        requests.post(
            f"{HELPER_URL}/adb_broadcast",
            json={
                'action': 'ADB_INPUT_B64',
                'extras': {'msg': encoded_text}
            },
            timeout=3
        )
        print(f"⌨️ 已输入: {text}")
        time.sleep(0.5)

        # 🔥 4. 发送回车/发送键 (针对微信等)
        if send_enter:
            print(f"🚀 发送回车指令 (Code 66)...")
            requests.post(
                f"{HELPER_URL}/adb_broadcast",
                json={
                    'action': 'ADB_EDITOR_CODE',
                    'extras': {'code': '66'} 
                },
                timeout=3
            )
            time.sleep(0.5)
        
        # 5. 恢复原有输入法（通过 HTTP 请求）
        requests.post(
            f"{HELPER_URL}/restore_ime",
            timeout=3
        )
        print(f"🔙 已恢复原输入法")
        
        return True
        
    except Exception as e:
        print(f"❌ ADB Keyboard 输入失败: {e}")
        return False

def input_text(text, app_name=None):
    """输入文本（优先使用 AccessibilityService，失败时使用 ADB Keyboard）
    Args:
        text: 输入文本
        app_name: 当前 App 名称，用于特殊策略
    """
    
    # 🔥🔥🔥 策略路由：微信强制使用 ADB + Enter 🔥🔥🔥
    if app_name and "微信" in app_name:
        print(f"⚡ [策略] 检测到微信，启动混合双打模式...")
        
        # 0. 先尝试用无障碍服务“摸一下”输入框，目的是获取焦点 (Focus & Tap)
        # 即使它输入失败也没关系，关键是它会尝试点击输入框，确保键盘弹出
        try:
            print("👉 [预热] 尝试通过无障碍服务获取输入框焦点...")
            requests.post(f"{HELPER_URL}/input", json={'text': ''}, timeout=2)
        except Exception as e:
            print(f"⚠️ 预热聚焦失败(非致命): {e}")
            
        # 1. 然后执行 ADB 强力输入 + 回车
        return input_text_via_adb_keyboard(text, send_enter=True)
        
    try:
        # 1️⃣ 优先尝试 AccessibilityService
        response = requests.post(
            f"{HELPER_URL}/input",
            json={'text': str(text)},
            timeout=5
        )
        
        if response.status_code == 200:
            data = response.json()
            success = data.get('success', False)
            
            if success:
                print(f"✅ AccessibilityService 输入成功")
                return True
            else:
                print(f"⚠️ AccessibilityService 输入失败，尝试 ADB Keyboard...")
                # 2️⃣ 备选方案：使用 ADB Keyboard
                return input_text_via_adb_keyboard(text)
        else:
            print(f"⚠️ HTTP 请求失败，尝试 ADB Keyboard...")
            return input_text_via_adb_keyboard(text)
            
    except Exception as e:
        print(f"⚠️ AccessibilityService 异常: {e}，尝试 ADB Keyboard...")
        # 3️⃣ 异常时也使用 ADB Keyboard
        return input_text_via_adb_keyboard(text)

def launch_app(app_name):
    """启动应用 - 通过 HTTP 请求"""
    print(f"[launch_app] 尝试启动: {app_name}")
    package = APP_PACKAGES.get(app_name, "")
    if not package:
        print(f"[launch_app] 错误: 未知应用 {app_name}")
        print(f"[launch_app] 可用应用: {list(APP_PACKAGES.keys())}")
        return False
    
    print(f"[launch_app] 包名: {package}")
    print(f"[launch_app] 请求 URL: {HELPER_URL}/launch")
    
    try:
        response = requests.post(
            f"{HELPER_URL}/launch",
            json={'package': package},
            timeout=5
        )
        print(f"[launch_app] HTTP 状态码: {response.status_code}")
        print(f"[launch_app] 响应内容: {response.text}")
        
        if response.status_code == 200:
            data = response.json()
            success = data.get('success', False)
            print(f"[launch_app] 结果: {success}")
            return success
        return False
    except Exception as e:
        print(f"[launch_app] 异常: {type(e).__name__}: {e}")
        import traceback
        traceback.print_exc()
        return False

def go_back():
    """返回操作"""
    try:
        response = requests.post(
            f"{HELPER_URL}/action",
            json={'action': 'back'},
            timeout=5
        )
        if response.status_code == 200:
            return response.json().get('success', False)
        return False
    except Exception as e:
        print(f"Back 失败: {e}")
        return False

def go_home():
    """主屏幕操作"""
    try:
        response = requests.post(
            f"{HELPER_URL}/action",
            json={'action': 'home'},
            timeout=5
        )
        if response.status_code == 200:
            return response.json().get('success', False)
        return False
    except Exception as e:
        print(f"Home 失败: {e}")
        return False

def get_display_metrics():
    """获取屏幕尺寸"""
    return SCREEN_WIDTH, SCREEN_HEIGHT
