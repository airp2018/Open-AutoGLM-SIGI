# SIGI v3.0 Update Summary: The Economy of Agents

**Release Date:** 2026-01-04
**Codename:** "Bang Bang" (梆梆)

## 1. Agent Economy System (代币经济)
Introduced a virtual currency system to gamify the agent experience.
- **Token Symbol:** `₳` (Agent Coin).
- **Earnings:**
    - Standard Task Completion: `+1 ₳`.
    - Doomsday List Protocol Execution: `+3 ₳`.
- **Spending (Ransomware Logic):**
    - Users can now pay a "ransom" to bypass the Proton Lock time-freeze.
    - **Easy Mode Cost:** 66 ₳.
    - **Hardcore Mode Cost:** 188 ₳.
- **Balance UI:** Viewable in the new Asset Vault title and the Lockdown payment screen.

## 2. SIGI Asset Vault (资产金库)
Refactored the `Cyber Codex` to support a dual-view mode:
- **LORE MODE:** The classic text-based scroll of SIGI history.
- **VAULT MODE:** A new grid-based showcase for collected digital artifacts.
    - Accessible by clicking the diamond icon (`◈`) in the Codex.

## 3. Visual Artifacts (传奇资产)
Designed and implemented high-fidelity collectible banknotes with dynamic text overlay technology.

| Artifact | Mode | Visual | Note |
| :--- | :--- | :--- | :--- |
| **Master Note** | Hardcore | **Pink Dragon Uncle** | Face Value: 100. Text: "梆梆" (Bang Bang). Unlocks via Hardcore Proton Lock. |
| **Dream Note** | Easy | **Green Meme Cat** | Face Value: 50. Text: "蒸蚌" (Zheng Bang). Unlocks via Normal Proton Lock. |
| **Train Ticket** | Hardcore | **Freight Waybill** | Retro 2007 style. Unlocks via '12306' puzzle tasks. |

## 4. Technical Improvements
- **Grid Layout System:** Implemented dynamic grid population for the Vault.
- **Overlay Rendering:** Created a flexible XML-based overlay system (`dialog_artifact_detail.xml`) to correct/modify text on AI-generated assets without external editing.
- **Smart Formatting:** Automatic aspect ratio handling for banknote assets.

---
*“In the frozen time loop, currency is the only measure of freedom.”*
