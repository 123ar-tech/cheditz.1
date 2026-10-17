# Cheditz

Everything for [cheditz.online](https://cheditz.online) in one repo:

| Path | What |
|---|---|
| `index.html`, `admin.html`, `sw.js`, `manifest.json` | Live website |
| `app/` | **Cheditz Admin** — native Android app (Kotlin + Jetpack Compose + Material 3) |
| `functions/` | Cloud Function that pushes an FCM notification on every new order |
| `firestore.rules` | Active security rules (website-compatible) |
| `firestore.rules.strict` | Recommended rules after Firebase Auth migration |
| `firestore.indexes.json` | Composite index for order status queries |
| `releases/app-debug.apk` | Prebuilt debug APK |

## Android app (`app/`)

Manages the site backend directly through Firestore — live order list, status updates
(pending → processing → completed/cancelled), portfolio visibility, reviews, hero/content
copy, accent color (writes `cheditz/config.accent`, site theme reacts instantly), services
pricing, contact info, and Firebase Auth sign-in.

```bash
./gradlew assembleDebug      # APK at app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease    # signed release (create a keystore first)
```

Requires JDK 17 + Android SDK 34. `app/google-services.json` is already configured for
project `cheditz-dee25`.

## Push notifications (works on the free plan — no Blaze needed)

The app checks for new orders itself:

1. **While the app is open** — real-time Firestore listener, instant
2. **Background** — WorkManager syncs every 15 minutes and shows a local
   notification for each new pending order (survives app closed/reboot;
   tap opens the order detail screen)

`functions/index.js` contains an optional Cloud Function (`onNewOrder`) that pushes
the same notification through FCM within seconds — deploying it requires Firebase's
**Blaze plan** (free tier covers it), so it's ready whenever you want it:

```bash
firebase deploy --only functions
```

## Firestore

Current `firestore.rules` keeps the existing website flows working (they use client-side
password only). **Before switching to `firestore.rules.strict`**:

1. Firebase Console → Authentication → enable **Email/Password**
2. Create an admin user
3. Deploy: `firebase deploy --only firestore:rules`

Known issue: the site password lives in client-side JS and is readable by anyone — the
migration above is required for real security.

## Collections

- `cheditz_orders` — name, email, phone, service, budget, deadline, description, date, status, receiptNumber, createdAt
- `cheditz_testimonials` — name, role, rating, text, date
- `cheditz` / `config` — accent, works[], content{}, services[], contact{}, testimonials[]
- `cheditz_receipts`, `cheditz_push_subs`, `cheditz_agent_log` — website data
