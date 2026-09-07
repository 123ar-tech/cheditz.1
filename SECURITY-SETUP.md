# Required Firebase activation

These files remove the embedded password and replace the browser-only admin login with Firebase Authentication. **Uploading the HTML alone does not secure your database. These rules have not been deployed to your Firebase project.** Admin login will require the setup below; the old password will no longer work.

1. In the Firebase console for your existing project, open Firestore → Rules. Back up the existing rules and review/publish `firestore.rules`. The supplied rules cover only the collections used by these HTML files and deny all other paths. Review any other applications using this project first. Orders become private; public visitors can submit validated orders and reviews but cannot edit or delete them. Reviews remain public, as in the original design.
2. In Authentication → Sign-in method, enable Email/Password. Set a password policy of at least 12 characters. Under Users, create your administrator account with a new, unique password. Treat the old embedded password as exposed and change it anywhere else you reused it.
3. Copy that user's UID. In Firestore, create collection `adminUsers`, document ID equal to the UID, with the boolean field `enabled: true`. Only do this in the trusted Firebase console. Browser clients cannot grant themselves this permission. Do not create a public registration path that grants admin access.
4. In `cheditz/config`, delete the old `password` field and any fields outside `accent`, `works`, `content`, `services`, `contact`, `testimonials`. Keep the existing design/content fields. The rules deliberately block public reads of a legacy config containing a password until it is cleaned. Remove password copies from old backups and published HTML too. Review public config content for any other sensitive information.
5. Upload the new `index.html` and `admin.html`, keeping the original logo and favicon files beside them. Add your real website domain to Firebase Authentication's authorized domains. Serve over HTTPS and sign in with the new email/password. Never upload service account keys or private backend credentials.
6. In Google Cloud credentials, review the existing Firebase web API key. Restrict it to the APIs needed by this Firebase app, following the official API-key guidance below. Check any website restrictions against all production/preview domains and test Authentication and Firestore after changing them. If that key was also used with non-Firebase or paid Google APIs, move those uses to separate restricted keys and rotate exposed secret credentials. The Firebase web key remains visible by design; it does not authorize database access.
7. Enable and monitor Firebase App Check for Firestore, then enforce it after registering all real clients and verifying traffic. Public forms otherwise remain susceptible to automated submissions. For strict submission rate limits, add a trusted backend endpoint with server-side limits; the browser's rate limiter is not a security boundary.

## Verify before going live

Use Firestore's Rules Playground or Emulator to check: anonymous order reads fail; anonymous config writes fail; ordinary authenticated users cannot read orders or write configuration; users cannot create their own `adminUsers` document; an enabled admin can read orders and update configuration; valid public submissions succeed; extra fields and invalid submissions fail. Verify password changes and sign-out with your real admin account. Setting `sessionStorage.cheditz_admin = '1'` must not grant access. Removing an admin's allowlist entry must make subsequent protected requests fail.

The browser changes were checked locally with simulated authentication, not with a live Firebase account. The rules require deployment and live/emulator validation. Existing server endpoints, hosting headers, storage-bucket rules, and account permissions were not supplied or audited.

## What changed

- Removed hardcoded passwords and config-based password changes from both pages.
- Firebase sign-in now verifies admin membership before subscribing to orders or showing the dashboard. Database rules enforce permissions independently of the interface.
- Orders are kept in admin-page memory rather than persistent browser storage; listeners and caches are cleared on logout. The old browser session flag no longer grants access.
- Saved/exported configuration only includes public content fields.
- Public pages no longer write website configuration or delete reviews. Failed order delivery no longer shows a success receipt.
- The previous mobile background improvements and asset references are retained.

Sources: [Firebase security checklist](https://firebase.google.com/support/guides/security-checklist), [Firebase API keys](https://firebase.google.com/docs/projects/api-keys), [Firebase rules basics](https://firebase.google.com/docs/rules/basics).
