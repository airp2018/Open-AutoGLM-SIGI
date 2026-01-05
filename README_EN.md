<p align="center">
  <img src="docs/screenshots/01_home.jpg" width="24%" />
  <img src="docs/screenshots/02_list.jpg" width="24%" />
  <img src="docs/screenshots/03_lockdown.jpg" width="24%" />
  <img src="docs/screenshots/04_settings.jpg" width="24%" />
</p>

[English](README_EN.md) | [简体中文](README.md)

# AutoGLM-SIGI: The Last Cyberphone

## 🌟 SIGI v3.2: "The Apology Alliance"

> **"Delay compensation makes AI feel more human, like a hardworking delivery rider, rather than just a tool."**

> **New Feature**: Introduced **Latency Compensation Mechanism**.
> When the Agent task execution exceeds **75 seconds**, the system automatically triggers "Delivery Delay Insurance" and issues an **"Apology Token"** to the user.

<p align="center">
  <img src="android-app/app/src/main/res/drawable/stamp_apology_cn.png" width="20%" />
  <img src="android-app/app/src/main/res/drawable/stamp_apology_in.png" width="20%" />
  <img src="android-app/app/src/main/res/drawable/stamp_apology_jp.png" width="20%" />
  <img src="android-app/app/src/main/res/drawable/stamp_apology_it.png" width="20%" />
</p>



## 🌟 SIGI v3.1: Agent Economic System Launched

> **SIGI v3.1: In version 3.1, we introduced the concept of "Agent Involvement in Virtual Economy" for the first time. By collaborating with the Agent, you can accumulate credits and issue two types of game tokens. In the future, these community tokens will be exchangeable for real creative services. Stay tuned.**

<p align="center">
  <img src="android-app/app/src/main/res/drawable/asset_banknote_hardcore.png" width="48%" />
  <img src="android-app/app/src/main/res/drawable/asset_banknote_cat.png" width="48%" />
</p>


> **"This is the way the world ends. Not with a bang but a whimper."**
> — *T.S. Eliot, The Hollow Men*

> **Cyber Game × AutoGLM: A Thought Experiment on the Future**
>
> **"Previewing the future of AI phones — their infinite possibilities, human hopes, and deep-seated fears."**
>
> If you are tired of cold efficiency tools, try this crazy idea: **Refactoring a GUI Agent into a cyber terminal from the apocalypse.**
> Breaking the boundary between "Tool" and "Game", reshaping interaction through narrative. This is not just an assistant, but a **micro cyber-theater** in your pocket.

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/airp2018/Open-AutoGLM-SIGI?color=00E676&label=SIGI%20Version&style=flat-square)](https://github.com/airp2018/Open-AutoGLM-SIGI/releases)
[![License](https://img.shields.io/github/license/airp2018/Open-AutoGLM-SIGI?color=blue&style=flat-square)](LICENSE)
[![GitHub repo size](https://img.shields.io/github/repo-size/airp2018/Open-AutoGLM-SIGI?style=flat-square)](https://github.com/airp2018/Open-AutoGLM-SIGI)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg?style=flat-square)](https://developer.android.com)
[![Vibe](https://img.shields.io/badge/Vibe-Coding-purple.svg?style=flat-square)](#creator)

---

## 🇺🇸 English Introduction

### 01. Zero-Cost AI Phone

**Stop paying $10,000 for hyping AI hardware. SIGI transforms your old Android into a top-tier AI terminal instantly.**

*   **Zero Cost**: Free to use if you have an Android phone.
*   **Zero Barrier**: No PC required, no Termux CLI. One-click install `.apk`, instantly becoming a LAM (Large Action Model) agent.
*   **Fully Auto**: It's your digital avatar. Watch it tap, swipe, and type like a ghost on your screen.

### 02. Narrative UI & Thought Experiment

We are not building a tool; we are building a **"Narrative UI"**.

**SIGILLUM MENTIS** is Latin for "Seal of the Mind" (Thought Stamp). In this era of information overload, we aim to imprint the mark of thought.

SIGI is inspired by **"The Three-Body Problem"** and **Classic Cyberpunk aesthetics**. It is not just an app, but a **"Reality Script Kill" (Immersive Mystery Game)**.

*   **Survival or Destruction?**
    *   🔴 **ACCELERATE**: Represents effective accelerationism. Hand over control and let the AI agent take over your life at full speed.
    *   🟢 **ABORT**: Represents humanity's last stand. Cut the connection and reclaim control from the machine.

*   **The Last Line of Defense / Proton Lockdown**:
    Every task execution broadcasts a signal to the universe. When the system overloads, the "Proton Lockdown" alarm will sound, and you will need to solve a puzzle within 3 minutes, like in an escape room, or be kicked out of cyberspace.

### 03. Interaction Philosophy: Emotional Consumption

We reject cold, utilitarian tools. **"He who captures emotions, captures the user."**

We predict that AI products will shift from function-driven to an era dominated by **Emotional Consumption**. SIGI provides a space for "mood consumption"—here, your phone is no longer just a tool, but a vessel for your emotions.

#### The Doomsday Protocols
We embedded narratives into the most basic command list. Each preset command is a micro-story fragment about love, parting, survival, and desire:
*   *"Oh, darling, open Weibo and post: Goodbye, my love"*
*   *"Winter is coming, buy 2 portions of spicy chicken wings"*
*   *"Open Trip.com... I don't want to die at home"*

#### Escape Room Aesthetic
*   **Atmosphere**: The interface uses `Terminal Green (#00E676)` paired with `Deep Space Black (#0A1929)`, simulating a secret terminal from the Cold War era.
*   **Metaphor**: The interaction on the settings page is a "secret door." Just like finding a mechanism in an escape room, you need to find clues for the key within the page to reveal the "Higher Dimensional" truth hidden beneath the surface.

### 04. Architecture Revolution

We refactored the open-source Open-AutoGLM with a **"Dimensional Strike"** level of reconstruction.

*   **Old Way (Competitors)**: Requires a PC + Termux + Python environment + complex dual-process communication. Your phone would overheat, lag, and crash easily.
*   **SIGI Way (This Solution)**: **Single-Process Hybrid Architecture**. The Python agent is directly embedded into the Android core.

| Feature | Legacy Way | SIGI (Next-Gen) |
| :--- | :--- | :--- |
| **Deployment Difficulty** | 🛑 Extremely Hard (Requires Termux/CLI) | ✅ **Extremely Simple (One-click APK install)** |
| **Response Speed** | 🐢 Slow (~500ms latency) | ⚡ **Light Speed (~3ms in-process direct)** |
| **No PC Required** | ❌ Must connect to PC for configuration | ✅ **Runs completely without a PC** |
| **Experience** | 📟 Dry command line | 🎮 **Immersive Cyberpunk gaming experience** |

### 05. Technical Architecture

#### Directory Structure
```bash
Open-AutoGLM-SIGI/
├── android-app/           # Android Host Project (Kotlin)
│   ├── app/src/main/python/   # 🟢 Python Agent Core
│   │   ├── agent_main.py      # Entry Point
│   │   └── android_helper.py  # Bridge Layer
│   └── app/src/main/java/     # 🟡 Android Native Layer
│       ├── AutoGLMAccessibilityService.kt # Perception & Action
│       └── MainActivity.kt    # UI Container
├── SIGI_Deployment_Kit/   # 🟣 Deployment Kit
└── docs/                  # Documentation
```

#### Hybrid Architecture Logic
SIGI uses a **Single-Process Hybrid Architecture**, embedding the Python VM directly into the Android Runtime via Chaquopy.
1.  **The Brain**: Python layer runs the AutoGLM Agent logic.
2.  **The Body**: Android native layer handles Screenshots and Accessibility Actions.
3.  **The Nerve**: Zero-latency JNI communication between layers.

### 06. The Creator

**SIGI is created by Yanqiao ([Weibo @颜桥](https://weibo.com/n/颜桥)).**

I am not a traditional "coder"; I am a **Storyteller** and **Creative Strategist**.

*   **Background**: **Top-tier CS Degree (985)** + multidisciplinary knowledge background.
*   **Identity**: **Novelist**. Works published in top literary journals such as *Harvest*, *People's Literature*, and *October*.
*   **Commercial**: Original stories were acquired by the producers of *Detective Chinatown* and Hunan Broadcasting System for high value.
*   **Crossover**: Provided creative strategy support for top brands like **BMW, HP, Chrysler, LV, and Burberry**.

> **"In the AI era, technology is no longer a barrier. Creative minds, empowered by Vibe Coding, will transform fresh ideas into tangible products. We light up your product with creative details."**

### 07. Install & Join

1.  Download **[SIGI_Deployment_Kit.zip](https://github.com/airp2018/Open-AutoGLM-SIGI/releases)** from Releases.
2.  Unzip it on your PC.
3.  Run `Install_Windows.bat` (First time only).
3.  **Unplug, and Accelerate.**

---

<!--
KEYWORDS & META TAGS FOR SEARCH ENGINE OPTIMIZATION
Topic: Android AI Agent, AutoGLM, LAM (Large Action Model)
Style: Cyberpunk, Sci-Fi, Three-Body Problem (三体), Digital Twin
Tech: Python, Accessibility Service, ADB, No-Root, Automation, RPA
-->
> **Tags**: `AutoGLM` `Android-Agent` `LAM` `Cyberpunk` `Three-Body-Problem` `Automation` `Python` `RPA` `No-Root` `AI-Assistant`
