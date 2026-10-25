"""Drives the installed app on the emulator and saves screenshots to ./screens."""
import os, re, subprocess, time, xml.etree.ElementTree as ET

OUT = "screens"
os.makedirs(OUT, exist_ok=True)

def adb(*args, out=False):
    r = subprocess.run(["adb", *args], capture_output=True)
    return r.stdout if out else None

def shot(name):
    time.sleep(1.5)
    with open(f"{OUT}/{name}.png", "wb") as f:
        f.write(adb("exec-out", "screencap", "-p", out=True))
    print("saved", name)

def nodes():
    adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    xml = adb("exec-out", "cat", "/sdcard/ui.xml", out=True)
    return ET.fromstring(xml).iter("node")

def tap(text, contains=False, tries=6):
    for _ in range(tries):
        for n in nodes():
            t = n.get("text") or n.get("content-desc") or ""
            if (text in t) if contains else (t == text):
                x1, y1, x2, y2 = map(int, re.findall(r"\d+", n.get("bounds")))
                adb("shell", "input", "tap", str((x1 + x2) // 2), str((y1 + y2) // 2))
                time.sleep(1.2)
                return True
        scroll()
    print("not found:", text)
    return False

def scroll(dist=900):
    adb("shell", "input", "swipe", "540", "1900", "540", str(1900 - dist), "400")
    time.sleep(1)

adb("shell", "cmd", "uimode", "night", "no")
adb("shell", "pm", "grant", "com.warrantymitra", "android.permission.POST_NOTIFICATIONS")
adb("shell", "am", "start", "-n", "com.warrantymitra/.MainActivity")
time.sleep(5)
shot("01-empty")
tap("Try 4 sample items")
time.sleep(2)
shot("02-my-items")
tap("Galaxy S24 FE")
shot("03-item-coverage")
scroll(1100); shot("04-item-store-reminders")
tap("Dropped, cracked or broken")
time.sleep(1)
shot("05-damage-check")
scroll(1100); shot("06-next-steps")
scroll(1100); shot("07-claim-tracker")
adb("shell", "input", "keyevent", "4")
time.sleep(1.5)
tap("Front-load washing machine 7 kg")
shot("08-washing-machine")
adb("shell", "input", "keyevent", "4")
time.sleep(1.5)
tap("Add item")
shot("09-add-item")
scroll(1300); shot("10-add-item-purchase")
scroll(1100); shot("11-add-item-where-bought")
adb("shell", "cmd", "uimode", "night", "yes")
adb("shell", "input", "keyevent", "4")
time.sleep(3)
shot("12-dark-mode")
