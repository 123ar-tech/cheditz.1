const functions = require("firebase-functions/v1");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();
const db = getFirestore();

// Push notification when a new order is created in cheditz_orders
exports.onNewOrder = functions.firestore.document("cheditz_orders/{orderId}").onCreate(async (snap, context) => {
  const order = snap.data() || {};
  const orderId = context.params.orderId;

  const title = "New Cheditz Order";
  const body = `New order from ${order.name || "customer"} for ${order.service || "a service"} (${order.budget || "budget TBD"})`;

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

  const results = [];

  // Topic broadcast (app auto-subscribes to cheditz_orders)
  try {
    results.push(await getMessaging().send({ ...payload, topic: "cheditz_orders" }));
  } catch (err) {
    console.error("Topic send failed:", err);
  }

  // Direct token send for registered devices (deduplicated)
  try {
    const tokensSnap = await db.collection("cheditz_fcm_tokens").limit(500).get();
    const tokens = tokensSnap.docs.map((d) => d.data().token).filter(Boolean);
    if (tokens.length > 0) {
      results.push(await getMessaging().sendEachForMulticast({ ...payload, tokens }));
    }
  } catch (err) {
    console.error("Token send failed:", err);
  }

  // Audit log
  await db.collection("cheditz_notification_log").add({
    orderId,
    title,
    body,
    sentAt: new Date().toISOString()
  });

  return null;
});

// Callable: register an admin device token
exports.registerToken = functions.https.onCall(async (data) => {
  const token = data && data.token;
  if (!token || typeof token !== "string") {
    throw new functions.https.HttpsError("invalid-argument", "token is required");
  }
  await db.collection("cheditz_fcm_tokens").doc(token.slice(0, 128)).set({
    token,
    updatedAt: new Date().toISOString()
  });
  return { ok: true };
});
