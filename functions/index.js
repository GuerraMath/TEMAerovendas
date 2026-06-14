// Caminho: functions/index.js
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");
const { onDocumentCreated } = require("firebase-functions/v2/firestore");

initializeApp();

exports.notifyNewContactRequest = onDocumentCreated("contact_requests/{id}", async (event) => {
  const data = event.data.data();
  const db = getFirestore();

  const tokenDoc = await db.collection("admin_config").doc("fcm").get();
  const token = tokenDoc.exists ? tokenDoc.get("token") : null;
  if (!token) return;

  let aircraftLabel = data.aircraftId;
  try {
    const aircraftDoc = await db.collection("aircraft").doc(data.aircraftId).get();
    if (aircraftDoc.exists) aircraftLabel = aircraftDoc.get("model") || aircraftLabel;
  } catch (e) {}

  await getMessaging().send({
    token,
    notification: {
      title: "Nova proposta - " + aircraftLabel,
      body: data.message || "Nova solicitação de contato recebida.",
    },
  });
});
