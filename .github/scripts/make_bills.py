"""Makes the bill photos the photo-reading scenario uses: printed bills photographed on a table the way people
actually do it, tilted, unevenly lit, faded thermal paper, creases and JPEG noise. Names and numbers are made up.

python3 .github/scripts/make_bills.py  ->  .github/scripts/bills/*.jpg
"""
import os, random

import numpy as np
from PIL import Image, ImageDraw, ImageFilter, ImageFont, ImageEnhance

OUT = os.path.join(os.path.dirname(__file__), "bills")
os.makedirs(OUT, exist_ok=True)
FONTS = "/usr/share/fonts/truetype"
MONO = f"{FONTS}/dejavu/DejaVuSansMono.ttf"
SANS = f"{FONTS}/liberation/LiberationSans-Regular.ttf"
SANS_B = f"{FONTS}/liberation/LiberationSans-Bold.ttf"
random.seed(7)


def paper(w, h, tone=(246, 244, 236)):
    img = Image.new("RGB", (w, h), tone)
    d = ImageDraw.Draw(img)
    for _ in range(w * h // 300):  # paper grain
        x, y = random.randrange(w), random.randrange(h)
        g = random.randint(-10, 6)
        d.point((x, y), tuple(max(0, min(255, c + g)) for c in tone))
    return img


def write(img, lines, font, x0, y0, gap, fill=(25, 25, 25), fade=0.0):
    d = ImageDraw.Draw(img)
    y = y0
    for line in lines:
        if isinstance(line, tuple):
            text, f = line
        else:
            text, f = line, font
        col = tuple(int(c + (235 - c) * fade * random.uniform(0.6, 1.0)) for c in fill)
        d.text((x0, y), text, font=f, fill=col)
        y += gap
    return y


def photograph(bill, name, angle, table=(92, 70, 52), light=0.35, crease=False, blur=0.6, quality=72, size=(1200, 1600)):
    if crease:
        d = ImageDraw.Draw(bill)
        for frac in (0.33, 0.66):
            y = int(bill.height * frac)
            d.line((0, y, bill.width, y + random.randint(-12, 12)), fill=(205, 203, 196), width=3)
    bg = Image.new("RGB", size, table)
    d = ImageDraw.Draw(bg)
    for i in range(0, size[1], 6):  # wood grain
        shade = random.randint(-10, 10)
        d.line((0, i, size[0], i + random.randint(-3, 3)), fill=tuple(max(0, c + shade) for c in table), width=3)
    scale = min((size[0] * 0.86) / bill.width, (size[1] * 0.86) / bill.height)
    bill = bill.resize((int(bill.width * scale), int(bill.height * scale)), Image.LANCZOS)
    rot = bill.convert("RGBA").rotate(angle, expand=True, resample=Image.BICUBIC)
    bg.paste(rot, ((size[0] - rot.width) // 2, (size[1] - rot.height) // 2), rot)
    # uneven light: brighter top-left, phone shadow towards the bottom-right
    w, h = size
    yy, xx = np.mgrid[0:h, 0:w]
    dim = np.clip((xx / w * 0.55 + yy / h * 0.45) ** 1.6 * light, 0, 1)[..., None]
    bg = Image.fromarray((np.asarray(bg, dtype=np.float32) * (1 - dim)).astype(np.uint8))
    bg = bg.filter(ImageFilter.GaussianBlur(blur))
    bg = ImageEnhance.Contrast(bg).enhance(0.92)
    bg.save(os.path.join(OUT, name), quality=quality)


# 1. Local shop GST bill on A5 paper, ruled table, stamp and signature
b = paper(1000, 1300)
d = ImageDraw.Draw(b)
big, mid, small = ImageFont.truetype(SANS_B, 40), ImageFont.truetype(SANS, 28), ImageFont.truetype(SANS, 26)
d.text((500, 50), "SRI VENKATESWARA ELECTRONICS", font=big, fill=(20, 20, 90), anchor="mt")
write(b, ["D.No 6-3-249, Main Road, Ameerpet, Hyderabad - 500016", "Ph: 040-23741234    GSTIN: 36ABCPV1234K1ZQ"], small, 120, 110, 36)
d.text((500, 200), "TAX INVOICE", font=ImageFont.truetype(SANS_B, 32), fill=(20, 20, 20), anchor="mt")
write(b, ["Inv No. SVE/1087", "To: Mr. Suresh"], mid, 60, 260, 40)
d.text((700, 260), "Date: 02/09/25", font=mid, fill=(25, 25, 25))
for y in (360, 410, 620):
    d.line((50, y, 950, y), fill=(60, 60, 60), width=2)
for x in (50, 110, 560, 660, 720, 840, 950):
    d.line((x, 360, x, 620), fill=(60, 60, 60), width=2)
for x, t in ((60, "S.No"), (120, "Particulars"), (570, "HSN"), (668, "Qty"), (730, "Rate"), (850, "Amount")):
    d.text((x, 372), t, font=small, fill=(20, 20, 20))
for x, t in ((70, "1"), (120, "Bajaj Rex 500W Mixer Grinder"), (570, "8509"), (680, "1"), (726, "2711.86"), (846, "2711.86")):
    d.text((x, 425), t, font=small, fill=(25, 25, 25))
for y, t, v in ((500, "CGST 9%", "244.07"), (540, "SGST 9%", "244.07")):
    d.text((120, y), t, font=small, fill=(25, 25, 25)); d.text((846, y), v, font=small, fill=(25, 25, 25))
d.text((560, 650), "Grand Total", font=ImageFont.truetype(SANS_B, 30), fill=(20, 20, 20))
d.text((830, 650), "3200.00", font=ImageFont.truetype(SANS_B, 30), fill=(20, 20, 20))
write(b, ["Rupees Three Thousand Two Hundred Only", "", "Warranty: 1 Year from date of bill", "E.&O.E.   Goods once sold will not be taken back."], small, 60, 720, 40)
d.ellipse((620, 960, 860, 1120), outline=(40, 70, 170), width=5)
d.text((740, 1040), "PAID", font=ImageFont.truetype(SANS_B, 44), fill=(40, 70, 170), anchor="mm")
d.line([(120, 1150), (170, 1110), (210, 1160), (260, 1100), (320, 1150)], fill=(30, 30, 120), width=3)
write(b, ["Customer Signature", "", "", "For Sri Venkateswara Electronics"], small, 60, 1170, 30)
photograph(b, "local-gst-bill.jpg", angle=-4, light=0.3)

# 2. Croma thermal receipt, narrow, faded
b = paper(560, 1500, tone=(250, 250, 247))
f = ImageFont.truetype(MONO, 22)
lines = [
    ("        CROMA", ImageFont.truetype(MONO, 34)), "   Infiniti Retail Limited", "  Croma - Inorbit Mall, Madhapur",
    "      Hyderabad - 500081", "   GSTIN: 36AACCV1726H1ZJ", "", "         TAX INVOICE", "",
    "Bill No: C412/2526/018834", "Date: 05/10/2025 18:42", "Cust: RAVI  Mob: 98xxxxxx21", "-" * 38,
    "Description           Qty   Amount", "-" * 38, "SONY WH-1000XM5 BLK      1  29990.00", "  HSN 85183000", "-" * 38,
    "Total Qty: 1", "Net Amount :          29,990.00", "Paid by: HDFC CREDIT CARD", "                      29,990.00", "-" * 38,
    "Warranty: 1 Year from invoice date", "", " Thank you for shopping at Croma", "   www.croma.com  1800 572 7662",
]
write(b, lines, f, 18, 30, 40, fill=(45, 45, 45), fade=0.45)
photograph(b, "croma-thermal-receipt.jpg", angle=3, table=(180, 180, 176), light=0.25, crease=True, blur=0.8)

# 3. Reliance Digital printed invoice, darker room
b = paper(1000, 1100)
f = ImageFont.truetype(SANS, 26)
lines = [
    ("Reliance Digital", ImageFont.truetype(SANS_B, 46)), "Reliance Retail Ltd", "Store: RD Kukatpally, Hyderabad 500072", "",
    ("Tax Invoice", ImageFont.truetype(SANS_B, 32)), "Invoice No : 2510RD4471000129", "Invoice Dt : 14-Sep-2025", "",
    "Description                                                          Qty      Net Amt",
    "Samsung 253L 3 Star Frost Free Refrigerator RT28C3053S8      1    24490.00", "",
    "Total Amount Payable : Rs 24,490.00", "", "Warranty: 1 Year on product, 20 Years on compressor",
]
write(b, lines, f, 50, 50, 52)
photograph(b, "reliance-digital-invoice.jpg", angle=-2, light=0.5, quality=60)

# 4. Vijay Sales thermal slip, crumpled and low contrast
b = paper(520, 1050, tone=(240, 238, 228))
f = ImageFont.truetype(MONO, 22)
lines = [
    ("   VIJAY SALES", ImageFont.truetype(MONO, 34)), "  Ameerpet Hyd 500016", "", "INVOICE NO: VS-AMP-25-77812", "DT:20/08/2025",
    "-" * 36, "HAIER 1.5T 3S INV AC HSU18C", "        1    36,490.00", "-" * 36, "TOTAL           36,490.00", "",
    "Thank you. Visit again", "1 Yr warranty, 10 Yr on compressor",
]
write(b, lines, f, 16, 30, 44, fill=(70, 70, 70), fade=0.35)
photograph(b, "vijay-sales-slip.jpg", angle=6, table=(70, 75, 85), light=0.4, crease=True, blur=1.0, quality=55)
print("written to", OUT)
