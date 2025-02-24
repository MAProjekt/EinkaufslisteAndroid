package com.fhswf.einkaufslisteandroid.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.fhswf.einkaufslisteandroid.MainActivity;
import com.fhswf.einkaufslisteandroid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

//https://github.com/MrHKMY/Pocketful/blob/master/BudgetWiser/app/src/main/java/com/mindscape/pocketful/FirebaseMessageReceiver.java

/**
 * Empfängt und verarbeitet FCM Push-Benachrichtigungen.
 * Zeigt eine Benachrichtigung an, wenn eine neue Nachricht empfangen wird.
 */
public class FirebaseMessageReceiver extends FirebaseMessagingService {

    /**
     * Wird aufgerufen, wenn eine neue Push-Benachrichtigung empfangen wird.
     * @param remoteMessage Empfangene Nachricht.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("DEBUG", "Empfangene Push-Nachricht: " + remoteMessage.getData());

        if (remoteMessage.getData().size() > 0) {
            showNotification(remoteMessage.getData().get("title"), remoteMessage.getData().get("message"));
        }
    }


    /**
     * Erstellt und zeigt eine Benachrichtigung an, wenn eine neue Nachricht empfangen wird.
     * @param title Titel der Benachrichtigung.
     * @param message Nachricht, die in der Nenachrichtigung angezeigt wird.
     */
    public void showNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        String channel_id = "notification_channel";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id)
                .setSmallIcon(R.drawable.login_icon_app)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channel_id, "App Notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(0, builder.build());
    }
}
