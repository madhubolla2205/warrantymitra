# WarrantyMitra

**Is it still covered? Check before you pay for repair.**

WarrantyMitra keeps every bill, warranty and protection plan for an Indian household in one place, reminds you before cover runs out, and tells you what to do when something breaks.

- `web/` is the clickable web prototype (a single HTML page).
- `android/` is the native Android app.

## Android app

Kotlin + Jetpack Compose, Room for storage on the phone, WorkManager for daily reminders.

**Try it on your phone:** open the latest run of the *Android build* workflow under the Actions tab, download `warrantymitra-debug-apk`, unzip it and install `app-debug.apk` (Android will ask you to allow installing from this source).

**Build locally:** open the `android/` folder in Android Studio, or run `./gradlew assembleDebug` inside it.

### What it does
- Every item keeps the product, bill photo, price in ₹, GST invoice or order number, payment mode (UPI, EMI, card) and whose item it is.
- **Where you bought it:** shop name, address, city, PIN code and phone, or the website and seller for online orders. Tap "I'm at the shop" to save the shop's location from GPS.
- Maker's warranty and any extended or protection plan on one timeline, plus AMC or service due dates.
- Phone notifications at 9 AM on the reminder days you choose (30, 10, 7 or 1 days before cover ends) and a week before an AMC is due.
- **Share to add:** share an order SMS or email from Messages or Gmail (or a bill photo) to WarrantyMitra and the form fills itself. This avoids the SMS-reading permission, which Google Play only allows for default SMS apps.
- **Something broke?** Pick what happened and get a likely covered / unclear / likely not covered answer, then next steps: return window on Amazon or Flipkart, the brand's customer care, service centres near your PIN, documents to carry, and the National Consumer Helpline (1915) and e-Daakhil if a claim is refused.
- Repair or claim tracker, and a JSON backup to a file.

### Code layout
- `domain/`: plain Kotlin rules with unit tests (coverage dates, damage check, order text parser, reminders, ₹ formatting).
- `data/`: Room database and sample items.
- `reminders/`: the daily WorkManager job and notifications.
- `ui/`: Compose screens (list, add/edit, item detail).
