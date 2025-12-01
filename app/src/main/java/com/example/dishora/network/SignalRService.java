package com.example.dishora.network;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.dishora.utils.SessionManager; // Your class to get the JWT token
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import io.reactivex.rxjava3.core.Single;

public class SignalRService {

    public static final String ACTION_NEW_MESSAGE = "com.example.dishora.NEW_MESSAGE_RECEIVED";

    private static SignalRService instance;
    private HubConnection hubConnection;
    private SessionManager sessionManager;
    private Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private Context appContext;

    // Listener interface to pass messages back to the UI (ChatActivity)
    public interface MessageListener {
        void onMessageReceived(String senderId, String message);
    }
    private MessageListener messageListener;

    // --- Singleton Setup ---
    private SignalRService(Context context) {
        this.appContext = context.getApplicationContext(); // ✅ 5. Store the context
        sessionManager = new SessionManager(context.getApplicationContext());
        // Define the hub connection but don't start it yet
        hubConnection = HubConnectionBuilder.create("http://dishora-mobile-env.eba-jym3ahey.ap-southeast-1.elasticbeanstalk.com/chathub") // ⚠️ REPLACE WITH YOUR URL
                .withAccessTokenProvider(Single.defer(() -> {
                    // This gets your JWT token right before connecting
                    return Single.just(sessionManager.getToken());
                })).build();
    }

    public static synchronized SignalRService getInstance(Context context) {
        if (instance == null) {
            instance = new SignalRService(context.getApplicationContext());
        }
        return instance;
    }

    // --- Connection Management ---
    public void startConnection(@Nullable Runnable onSuccess, @Nullable Runnable onError) {
        if (hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
            if (onSuccess != null) onSuccess.run();
            return;
        }

        // Register the "ReceiveMessage" handler BEFORE connecting
        // The name "ReceiveMessage" MUST match the name used in your C# ChatHub
        hubConnection.on("ReceiveMessage", (senderId, message) -> {
            Log.d("SignalR", "Message received from " + senderId);

            // ✅ 6. SEND A BROADCAST to notify the chat list
            Intent intent = new Intent(ACTION_NEW_MESSAGE);
            intent.putExtra("senderId", senderId);
            intent.putExtra("message", message);
            LocalBroadcastManager.getInstance(appContext).sendBroadcast(intent);

            if (messageListener != null) {
                // Post to the main thread to safely update the UI
                mainThreadHandler.post(() -> messageListener.onMessageReceived(senderId, message));
            }
        }, String.class, String.class); // Define the types of the incoming parameters

        // Start the connection
        hubConnection.start().doOnComplete(() -> {
            Log.d("SignalR", "Connection started successfully.");
            if (onSuccess != null) mainThreadHandler.post(onSuccess);
        }).doOnError(error -> {
            Log.e("SignalR", "Connection failed: " + error.getMessage());
            if (onError != null) mainThreadHandler.post(onError);
        }).subscribe();
    }

    public void stopConnection() {
        if (hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
            hubConnection.stop();
            hubConnection.remove("ReceiveMessage"); // Clean up handler
            Log.d("SignalR", "Connection stopped.");
        }
        messageListener = null;
    }

    // --- Sending Messages ---
    public void sendMessage(String recipientUserId, String message) {
        if (hubConnection.getConnectionState() != HubConnectionState.CONNECTED) {
            Log.e("SignalR", "Cannot send message, not connected.");
            return;
        }
        hubConnection.invoke("SendMessage", recipientUserId, message);
    }

    // --- Listener Setter ---
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }
}