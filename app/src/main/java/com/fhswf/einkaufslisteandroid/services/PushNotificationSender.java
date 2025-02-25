package com.fhswf.einkaufslisteandroid.services;

//Quelle:
//https://rollout.com/integration-guides/firebase-admin-sdk/sdk/step-by-step-guide-to-building-a-firebase-admin-sdk-api-integration-in-java

import android.content.Context;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;

import okhttp3.*;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Dient zum Senden von Push-Benachrichtigungen über FCM.
 */
public class PushNotificationSender {
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/einkaufsliste-3508a/messages:send";
    private static final String FIREBASE_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";

    // ExecutorService mit einem festen Thread-Pool von 3 Threads
    private static final ExecutorService executorService = Executors.newFixedThreadPool(3);

    /**
     * Sendet eine Push-Benachrichtigung an ein bestimmtes Gerät über FCM.
     * @param context Kontext, um auf Ressourcen zuzugreifen.
     * @param fcmToken FCM-Token des Empfängers.
     * @param title Titel der Benachrichtigung.
     * @param body Inhalt der Benachrichtigung.
     */
    public static void sendPushNotification(Context context, String fcmToken, String title, String body) {

        executorService.execute(() -> {
            try {
                // Nachricht wird hier erstellt
                JSONObject message = new JSONObject();
                message.put("token", fcmToken);

                JSONObject notification = new JSONObject();
                notification.put("title", title);  // Titel der Nachricht
                notification.put("body", body);    // Nachrichtentext

                message.put("notification", notification);

                JSONObject jsonRequest = new JSONObject();
                jsonRequest.put("message", message);

                RequestBody requestBody = RequestBody.create(
                        jsonRequest.toString(), MediaType.get("application/json"));

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(FCM_URL)
                        .addHeader("Authorization", "Bearer " + getAccessToken(context))
                        .addHeader("Content-Type", "application/json")
                        .post(requestBody)
                        .build();

                //ZUm Debuggen
                Response response = client.newCall(request).execute();
                Log.d("DEBUG", "code: " + response.code());
                Log.d("DEBUG", "body: " + response.body().string());

            } catch (Exception e) {
                Log.e("ERROR", "Fehler beim Senden der Push-Nachricht: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Ruft ein Firebase Access Token ab, um FCM-Nachrichten zu authentifizieren.
     * @param context Kontext, um auf die JSON-Datei mit den Firebase-Anmeldedaten zuzgreifen.
     * @return Access Token als String
     */
    private static String getAccessToken(Context context) {
        try {
            // Datei aus dem assets-Ordner laden
            InputStream inputStream = context.getAssets().open("einkaufsliste-3508a-firebase-adminsdk-rz597-6c02809d6b.json");

            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(inputStream)
                    .createScoped(Collections.singletonList(FIREBASE_SCOPE));
            credentials.refreshIfExpired();
            String token = credentials.getAccessToken().getTokenValue();
            Log.d("DEBUG", "Firebase Access Token erhalten: " + token);
            return token;
        } catch (Exception e) {
            Log.e("ERROR", "Fehler beim Abrufen des Firebase Access Tokens: " + e.getMessage(), e);
            return null;
        }
    }
}
