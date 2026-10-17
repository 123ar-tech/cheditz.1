const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();
const db = getFirestore();

// Push notification when a new order is created in cheditz_orders
exports.onNewOrder = onDocumentCreated("cheditz_orders/{orderId}", async (event) => {
  const snap = event.data;
  if (!snap) return;

  const order = snap.data();
  const orderId = event.params.orderId;

  const title = "New Cheditz Order";
  const body = `New order from ${order.name || "customer"} for ${order.service || "a service"} (${order.budget || "budget TBD"})`;

  // Subscribe all admin devices to topic "cheditz_orders" (done in app).
  // Also send to any device tokens stored in fcm_tokens collection.
  const tokensSnap = await db.collection("cheditz_fcm_tokens").limit(500).get();
  const tokens = tokensSnap.docs.map((d) => d.data().token).filter(Boolean);

  const payload = {
    notification: { title, body, sound: "default" },
    data: {
      orderId,
      name: order.name || "",
      service: order.service || "",
      budget: order.budget || "",
      screen: "order_detail"
    }
  };

  // Topic broadcast (covers all subscribed admin devices)
  const topicPayload = { ...payload, topic: "cheditz_orders" };
  const tasks = [getMessaging().send(topicPayload)];

  // Direct token send for registered devices (deduplicated)
  if (tokens.length > 0) {
    tasks.push(
      getMessaging().sendEachForMulticast({ ...payload, tokens })
    );
  }

  const results = await Promise.allSettled(tasks);
  results.forEach((r, i) => {
    if (r.status === "rejected") {
      console.error(`Send ${i} failed:`, r.reason);
    }
  });

  // Write an audit log entry
  await db.collection("cheditz_notification_log").add({
    orderId,
    title,
    body,
    sentAt: new Date().toISOString(),
    tokenCount: tokens.length
  });
});

// Optional: callable to register an admin device token
// Call from app with { token: "..." }
exports.registerToken = require("firebase-functions/v2/https").onCall(async (request) => {
  const token = request.data && request.data.token;
  if (!token || typeof token !== "string") {
    throw new Error("token is required");
  }
  // Store with a stable key so re-registration overwrites
  await db.collection("cheditz_fcm_tokens").doc(token.slice(0, 128)).set({
    token,
    updatedAt: new Date().toISOString()
  });
  return { ok: true };
});
