"""End-to-end scenarios on an emulator: drives the real app with adb and records PASS/FAIL with screenshots.

Writes results/results.md and results/*.png.
"""
import datetime as dt, os, re, subprocess, time, xml.etree.ElementTree as ET

PKG = "com.warrantymitra"
OUT = "results"
os.makedirs(OUT, exist_ok=True)
results = []
_log = open(f"{OUT}/log.txt", "w")
_print = print
def print(*a):
    _print(*a, flush=True)
    _log.write(" ".join(str(x) for x in a) + "\n"); _log.flush()
shot_no = [0]
today = dt.date.today()
ONLY = os.environ.get("ONLY", "")


def run(n):
    return not ONLY or n in ONLY.split(",")


def months_ago(d, months):
    y, m = divmod(d.year * 12 + d.month - 1 - months, 12)
    import calendar
    return dt.date(y, m + 1, min(d.day, calendar.monthrange(y, m + 1)[1]))


def adb(*args, out=False, check=False):
    r = subprocess.run(["adb", *args], capture_output=True)
    if check and r.returncode:
        print("adb failed:", args, r.stderr.decode())
    return r.stdout.decode(errors="replace") if out == "text" else (r.stdout if out else None)


def shot(name):
    time.sleep(1.2)
    shot_no[0] += 1
    fn = f"{shot_no[0]:02d}-{name}.png"
    with open(f"{OUT}/{fn}", "wb") as f:
        f.write(adb("exec-out", "screencap", "-p", out=True))
    return fn


def dump():
    for _ in range(4):
        adb("shell", "rm", "-f", "/sdcard/ui.xml")
        msg = adb("shell", "uiautomator", "dump", "/sdcard/ui.xml", out="text")
        if "ERROR" in msg:
            print("dump error:", msg.strip())
            time.sleep(1)
            continue
        raw = adb("exec-out", "cat", "/sdcard/ui.xml", out=True)
        try:
            return list(ET.fromstring(raw).iter("node"))
        except ET.ParseError:
            time.sleep(1)
    return []


def label(n):
    return (n.get("text") or "") + (n.get("content-desc") or "")


def find(text, contains=True, which=-1):
    ns = [n for n in dump() if ((text in label(n)) if contains else (label(n) == text))]
    return ns[which] if ns else None


def center(n):
    x1, y1, x2, y2 = map(int, re.findall(r"\d+", n.get("bounds")))
    return (x1 + x2) // 2, (y1 + y2) // 2


def swipe_up(dist=900):
    adb("shell", "input", "swipe", "540", "1900", "540", str(1900 - dist), "400")
    time.sleep(0.8)


def swipe_down(dist=900):
    adb("shell", "input", "swipe", "540", "700", "540", str(700 + dist), "400")
    time.sleep(0.8)


def to_top():
    for _ in range(3):
        swipe_down(1500)


def seek(text, contains=True, max_scrolls=8, which=-1):
    """Finds text on screen, else scrolls down from the current spot, then from the top. Returns the node or None."""
    for attempt in range(2):
        for i in range(max_scrolls + 1):
            n = find(text, contains, which)
            if n is not None:
                return n
            swipe_up()
        to_top()
    return None


def tap(text, contains=False, from_top=False, which=-1):
    if from_top:
        to_top()
    n = seek(text, contains, which=which)
    if n is None:
        print("could not find to tap:", text)
        return False
    x, y = center(n)
    print(f"tap '{text}' at {x},{y} bounds={n.get('bounds')} clickable={n.get('clickable')} class={n.get('class')}")
    adb("shell", "input", "tap", str(x), str(y))
    time.sleep(1.3)
    return True


def hide_keyboard():
    if "mInputShown=true" in adb("shell", "dumpsys", "input_method", out="text"):
        adb("shell", "input", "keyevent", "4")
        time.sleep(0.5)


def type_into(field_label, text):
    if not tap(field_label, contains=False):
        return False
    adb("shell", "input", "keyevent", "KEYCODE_MOVE_END")
    adb("shell", "input", "text", text.replace(" ", "%s").replace("'", ""))
    time.sleep(0.5)
    hide_keyboard()
    return True


def check(scenario, step, ok, screenshot=None, note=""):
    results.append((scenario, step, bool(ok), screenshot, note))
    print(("PASS" if ok else "FAIL"), scenario, "|", step, note)


def expect(scenario, step, text, from_top=True, name=None):
    if from_top:
        to_top()
    n = seek(text)
    if n is not None:
        y = center(n)[1]
        if y > 1900:  # bring it into the screenshot
            swipe_up(y - 1200)
    fn = shot(name or re.sub(r"\W+", "-", step.lower())[:40])
    check(scenario, step, n is not None, fn, "" if n is not None else f'"{text}" not on screen')
    return n is not None


def share_text(text):
    adb("shell", "am", "start", "-f", "0x10008000", "-a", "android.intent.action.SEND", "-t", "text/plain",
        "-n", f"{PKG}/.MainActivity", "--es", "android.intent.extra.TEXT", f"'{text}'")
    time.sleep(3)


def back():
    hide_keyboard()
    adb("shell", "input", "keyevent", "4")
    time.sleep(1.5)


def home_list():
    """Restarts the app on a fresh task so it opens on the item list."""
    hide_keyboard()
    adb("shell", "am", "start", "-f", "0x10008000", "-n", f"{PKG}/.MainActivity")
    time.sleep(2.5)


def save_item():
    hide_keyboard()
    return tap("Save item", contains=False)


def damage(problem):
    to_top()
    tap(problem, contains=False)
    time.sleep(1)


def open_add_form(S):
    if find("Add item", contains=False) is None:
        print("Add item not in the accessibility dump; tapping the floating button by position")
        adb("shell", "input", "tap", "880", "2215")
        time.sleep(1.5)
    else:
        tap("Add item", contains=False)
    ok = find("Add an item", contains=False) is not None
    if not ok:
        n = find("Add item", contains=False)
        print("Add item tap did not open the form; node now:", None if n is None else n.attrib)
    check(S, "Add item button opens the form", ok, shot("add-form"))
    return ok


# ---------------------------------------------------------------- setup
adb("shell", "cmd", "uimode", "night", "no")
adb("shell", "pm", "grant", PKG, "android.permission.POST_NOTIFICATIONS")
adb("shell", "pm", "grant", PKG, "android.permission.ACCESS_FINE_LOCATION")
adb("shell", "pm", "grant", PKG, "android.permission.ACCESS_COARSE_LOCATION")
adb("shell", "am", "start", "-n", f"{PKG}/.MainActivity")
time.sleep(5)

S = "0. Empty app"
tap("Add your first item", contains=False)
check(S, "'Add your first item' opens the form", find("Add an item", contains=False) is not None, shot("first-item-form"))
back()

# ---------------------------------------------------------------- S1: Amazon order SMS, warranty ends in 10 days
if run("1"):
    S = "1. Amazon order SMS shared into the app"
    d1 = months_ago(today, 12) + dt.timedelta(days=11)  # 12-month warranty ends today + 10
    share_text(f"Your Amazon.in order of boAt Rockerz 450 Bluetooth Headphones has been delivered. Order #408-7712345-1234567 placed on {d1:%d/%m/%Y}. Total Rs. 1,499.")
    check(S, "Form opens with a note that fields were filled", find("Filled") is not None, shot("amazon-sms-filled"))
    check(S, "Product name read from the SMS", find("boAt Rockerz 450 Bluetooth Headphones") is not None)
    check(S, "Order number read", seek("408-7712345-1234567") is not None)
    check(S, "Price ₹1499 read", seek("1499", contains=False) is not None, shot("amazon-price-date"))
    check(S, "Purchase date read", seek(f"{d1:%d %b %Y}") is not None, note=f"expected {d1:%d %b %Y}")
    type_into("Brand", "boAt")
    save_item()
    expect(S, "Saved item shows 'Ends in 10 days'", "Ends in 10 days", name="amazon-item-detail")
    expect(S, "Bought on Amazon, with orders link", "Open Amazon orders", name="amazon-store")
    damage("Stopped working on its own")
    expect(S, "Defect inside warranty: likely covered", "Likely covered by the maker's warranty", from_top=False, name="amazon-defect")
    damage("Dropped, cracked or broken")
    expect(S, "Drop without a plan: likely not covered", "Likely not covered", from_top=False, name="amazon-drop")
    expect(S, "Next steps: service centres near the delivery PIN or brand", "Find boAt customer care", from_top=False, name="amazon-next-steps")

# ---------------------------------------------------------------- S2: reminder notification 10 days before
if run("2"):
    S = "2. Reminder 10 days before cover ends"
    adb("shell", "am", "start", "-f", "0x10008000", "-n", f"{PKG}/.MainActivity", "-a", "com.warrantymitra.debug.RUN_REMINDERS")
    time.sleep(8)
    notif = adb("shell", "dumpsys", "notification", "--noredact", out="text")
    check(S, "Notification posted for the headphones", "cover ends in 10 days" in notif,
          note="" if "cover ends in 10 days" in notif else "no matching notification in dumpsys")
    adb("shell", "cmd", "statusbar", "expand-notifications")
    time.sleep(2)
    fn = shot("reminder-notification")
    check(S, "Notification visible in the shade", find("cover ends in 10 days") is not None, fn)
    n = find("cover ends in 10 days")
    if n is not None:
        x, y = center(n)
        adb("shell", "input", "tap", str(x), str(y))
        time.sleep(3)
        check(S, "Tapping the notification opens the item", find("boAt Rockerz 450 Bluetooth Headphones") is not None, shot("notification-opens-item"))
    else:
        adb("shell", "cmd", "statusbar", "collapse")
    home_list()

# ---------------------------------------------------------------- S3: Flipkart email with written date + protection plan
if run("3"):
    S = "3. Flipkart email with an extended plan"
    d2 = today - dt.timedelta(days=5)
    share_text(f"Delivered: Samsung 7 kg Front Load Washing Machine WW70 is delivered on {d2:%d %b %Y}. Order ID OD430112233445566 worth ₹32,490. Thank you for shopping on Flipkart")
    shot("flipkart-filled")
    check(S, "Price ₹32490 read", seek("32490", contains=False) is not None)
    check(S, "Written date read", seek(f"{d2:%d %b %Y}") is not None, note=f"expected {d2:%d %b %Y}")
    check(S, "Platform detected as Flipkart", seek("Flipkart", contains=False) is not None, shot("flipkart-platform"))
    type_into("Brand", "Samsung")
    type_into("Extended warranty or protection plan", "Flipkart Complete Appliance Protection")
    type_into("Plan length (months)", "24")
    tap("From purchase date")
    tap("Plan covers accidental or screen damage")
    save_item()
    expect(S, "Saved and covered", "Covered", name="flipkart-detail")
    expect(S, "Plan shown on the coverage timeline", "Flipkart Complete Appliance Protection", name="flipkart-timeline")
    damage("Dropped, cracked or broken")
    expect(S, "Drop with accidental cover: claim with plan", "Likely covered by your plan", from_top=False, name="flipkart-drop")
    expect(S, "Claim goes to the plan, maker's warranty still running", "not the brand", from_top=False, name="flipkart-claim-wording")
    expect(S, "Return window shown (bought 5 days ago)", "You bought this 5 days ago", from_top=False, name="flipkart-return-window")
    damage("Water or liquid damage")
    expect(S, "Water damage, plan has no liquid cover: not covered", "Likely not covered", from_top=False, name="flipkart-water")
    damage("Power cut or voltage fluctuation")
    expect(S, "Voltage fluctuation: unclear", "Unclear: often excluded", from_top=False, name="flipkart-voltage")
    home_list()

# ---------------------------------------------------------------- S4: expired product
if run("4"):
    S = "4. Product bought 3 years ago"
    d3 = months_ago(today, 36)
    share_text(f"Your order of Philips Mixer Grinder HL7756 has been delivered. Order #402-1112223-3334445 on {d3:%d/%m/%Y}. Total Rs. 3,299 Amazon")
    save_item()
    expect(S, "Shows Expired", "Expired", name="expired-detail")
    damage("Stopped working on its own")
    expect(S, "Defect after expiry: not covered", "Not covered: cover ended on", from_top=False, name="expired-defect")
    home_list()

# ---------------------------------------------------------------- S5: shop purchase with GPS location
if run("5"):
    S = "5. Bought in a shop, location saved by GPS"
    adb("emu", "geo", "fix", "78.3915", "17.4483")  # Madhapur, Hyderabad
    time.sleep(2)
    open_add_form(S)
    type_into("Product name *", "Voltas 1.5 ton split AC")
    type_into("Brand", "Voltas")
    type_into("Shop name", "Bajaj Electronics")
    type_into("Shop phone", "04023456789")
    adb("emu", "geo", "fix", "78.3915", "17.4483")
    tap("I'm at the shop: save its location")
    time.sleep(10)
    ok = find("Map pin saved") is not None
    check(S, "GPS position saved", ok, shot("shop-gps"))
    check(S, "City filled from the location", find("Hyderabad") is not None, note="geocoder may be unavailable on the emulator")
    plus = [label(n) for n in dump() if re.search(r"\b[23456789CFGHJMPQRVWX]{4,8}\+[23456789CFGHJMPQRVWX]{2,3}\b", label(n))]
    check(S, "Shop address is readable (no Plus Code like C9XR+8H5)", not plus, note=("found: " + plus[0]) if plus else "")
    pin = find("5000", contains=True)
    check(S, "PIN code filled from the location", pin is not None, note="" if pin is not None else "no 5000xx PIN on screen")
    save_item()
    expect(S, "Detail shows shop and Maps button", "Open shop in Maps", name="shop-detail")
    expect(S, "Shop phone has a call button", "Call shop", name="shop-call")
    damage("Water or liquid damage")
    expect(S, "Water damage: likely not covered", "Likely not covered", from_top=False, name="shop-water")
    expect(S, "Next steps offer calling the shop", "Call Bajaj Electronics", from_top=False, name="shop-next-steps")
    home_list()

# ---------------------------------------------------------------- S6: form checks
if run("6"):
    S = "6. Form checks"
    open_add_form(S)
    save_item()
    expect(S, "Empty product name is refused", "Add the product name to save.", from_top=False, name="empty-product")
    type_into("Product name *", "Test item")
    type_into("PIN code", "50008")
    save_item()
    expect(S, "5-digit PIN is refused", "A PIN code has 6 digits.", from_top=False, name="short-pin")
    home_list()

# ---------------------------------------------------------------- S7: list overview
if run("7"):
    S = "7. List and filters"
    to_top()
    missing = [t for t in ["boAt Rockerz", "Samsung 7 kg", "Philips Mixer", "Voltas"] if seek(t, max_scrolls=3) is None]
    check(S, "All items listed", not missing, shot("list"), note=("missing: " + ", ".join(missing)) if missing else "")
    to_top()
    tap("Ending soon", contains=False, which=0)
    check(S, "Ending soon filter shows the headphones only", find("boAt Rockerz") is not None and find("Philips Mixer") is None, shot("filter-ending-soon"))
    tap("Expired", contains=False, which=0)
    to_top()
    check(S, "Expired filter shows the mixer", find("Philips Mixer") is not None and find("Ends in 10 days") is None, shot("filter-expired"))
    tap("All", contains=False, which=0)

# ---------------------------------------------------------------- S8: bill photo shared from the gallery
if run("8"):
    S = "8. Bill photo shared into the app"
    adb("push", ".github/scripts/sample-bill.png", "/sdcard/Pictures/sample-bill.png")
    adb("shell", "content", "call", "--uri", "content://media", "--method", "scan_volume", "--arg", "external_primary")
    adb("shell", "am", "broadcast", "-a", "android.intent.action.MEDIA_SCANNER_SCAN_FILE", "-d", "file:///sdcard/Pictures/sample-bill.png")
    time.sleep(3)
    q = adb("shell", "content", "query", "--uri", "content://media/external/images/media", "--projection", "_id:_display_name", out="text")
    m = re.search(r"_id=(\d+), _display_name=sample-bill.png", q)
    if m:
        uri = f"content://media/external/images/media/{m.group(1)}"
        adb("shell", "am", "start", "-f", "0x10008000", "-a", "android.intent.action.SEND", "-t", "image/png", "-n", f"{PKG}/.MainActivity",
            "--eu", "android.intent.extra.STREAM", uri, "--grant-read-uri-permission")
        time.sleep(4)
        shot("bill-photo-shared")
        check(S, "Add form opens with the photo attached", seek("Photo saved") is not None, shot("bill-photo-saved"),
          note="" if find("Couldn't open the shared photo") is None else "app showed: couldn't open the shared photo")
    else:
        check(S, "Add form opens with the photo attached", False, note="could not register the test image with the gallery")

# ---------------------------------------------------------------- report
with open(f"{OUT}/results.md", "w") as f:
    passed = sum(1 for r in results if r[2])
    f.write(f"# Scenario results: {passed}/{len(results)} passed ({today})\n\n")
    cur = None
    for sc, step, ok, fn, note in results:
        if sc != cur:
            f.write(f"\n## {sc}\n\n")
            cur = sc
        f.write(f"- {'PASS' if ok else 'FAIL'} {step}{' (' + note + ')' if note else ''}{' [' + fn + ']' if fn else ''}\n")
print(open(f"{OUT}/results.md").read())
