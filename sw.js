/* Cheditz PWA service worker — offline shell + agent push notifications */
const VERSION = 'cheditz-sw-v1';
const SHELL = [
  './',
  './index.html',
  './manifest.json',
  './favicon.png',
  './b_Enhance_the_image_to-removebg-preview.png'
];

self.addEventListener('install', (e) => {
  e.waitUntil(
    caches.open(VERSION)
      .then((c) => c.addAll(SHELL))
      .catch(() => undefined)
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(keys.filter((k) => k !== VERSION).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (e) => {
  const req = e.request;
  if (req.method !== 'GET') return;
  let url;
  try { url = new URL(req.url); } catch (_) { return; }
  if (url.origin !== self.location.origin) return;

  if (req.mode === 'navigate') {
    e.respondWith(
      fetch(req)
        .then((r) => {
          const copy = r.clone();
          caches.open(VERSION).then((c) => c.put(req, copy)).catch(() => undefined);
          return r;
        })
        .catch(() => caches.match(req).then((m) => m || caches.match('./index.html')))
    );
    return;
  }

  e.respondWith(
    caches.match(req).then((cached) => {
      const network = fetch(req)
        .then((r) => {
          if (r && r.status === 200 && r.type === 'basic') {
            const copy = r.clone();
            caches.open(VERSION).then((c) => c.put(req, copy)).catch(() => undefined);
          }
          return r;
        })
        .catch(() => cached);
      return cached || network;
    })
  );
});

self.addEventListener('push', (e) => {
  let data = {};
  if (e.data) {
    try { data = e.data.json(); } catch (_) { data = { body: e.data.text() }; }
  }
  const title = data.title || 'Cheditz Agent';
  const opts = {
    body: data.body || '',
    icon: './favicon.png',
    badge: './favicon.png',
    image: data.image || undefined,
    tag: data.tag || 'cheditz-alert',
    renotify: true,
    requireInteraction: !!data.requireInteraction,
    timestamp: data.timestamp || Date.now(),
    data: { url: data.url || './' }
  };
  e.waitUntil(self.registration.showNotification(title, opts));
});

self.addEventListener('notificationclick', (e) => {
  e.notification.close();
  const target = (e.notification.data && e.notification.data.url) || './';
  e.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((list) => {
      for (const c of list) {
        if ('focus' in c) return c.focus().then((w) => (w && w.url) ? w : c.navigate(target));
      }
      return self.clients.openWindow(target);
    })
  );
});

self.addEventListener('pushsubscriptionchange', (e) => {
  const key = 'BCumSzaHxMFHn2XPMXOGN2-h3kUKeSUO8P--fJExwEeHGP6IIDQj8GsMq6o4l9EsOD1n-gsNyiGC3MVQZLGUlD0';
  if (!key || !e.oldSubscription) return;
  e.waitUntil(
    self.registration.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: urlBase64ToUint8Array(key)
    }).then((sub) => {
      self.clients.matchAll({ includeUncontrolled: true }).then((list) => {
        list.forEach((c) => c.postMessage({ type: 'cheditz:resubscribe', subscription: sub }));
      });
    }).catch(() => undefined)
  );
});

function urlBase64ToUint8Array(base64String) {
  const padding = '='.repeat((4 - (base64String.length % 4)) % 4);
  const base64 = (base64String + padding).replace(/-/g, '+').replace(/_/g, '/');
  const raw = atob(base64);
  const output = new Uint8Array(raw.length);
  for (let i = 0; i < raw.length; i++) output[i] = raw.charCodeAt(i);
  return output;
}
