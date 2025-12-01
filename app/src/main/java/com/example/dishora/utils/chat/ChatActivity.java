package com.example.dishora.utils.chat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.defaultUI.inboxTab.chatTab.adapter.MessageAdapter;
import com.example.dishora.defaultUI.inboxTab.chatTab.model.ChatMessage;
import com.example.dishora.models.ChatMessageDto;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.MessageApi;
import com.example.dishora.network.SignalRService;
import com.example.dishora.utils.SessionManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity implements SignalRService.MessageListener {

    // --- UI Components ---
    private MaterialToolbar toolbar;
    private RecyclerView rvMessages;
    private EditText etMessageInput;
    private ImageButton btnSend;

    // --- Data & Adapter ---
    private MessageAdapter messageAdapter;
    private List<ChatMessage> chatMessages;

    // --- User Info ---
    private String currentUserId;
    private String recipientUserId;
    private String recipientName;

    // --- SignalR ---
    private SignalRService signalRService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // ✅ 1. Get an instance of the SignalR service
        signalRService = SignalRService.getInstance(this);
        // Set this activity to be the one that listens for new messages
        signalRService.setMessageListener(this);

        // **Step 1: Get User and Recipient Info**
        // You'll pass these values when you start this activity
        recipientUserId = getIntent().getStringExtra("RECIPIENT_USER_ID");
        recipientName = getIntent().getStringExtra("RECIPIENT_NAME");

        // Get the current logged-in user's ID
        SessionManager sessionManager = new SessionManager(this);
        currentUserId = String.valueOf(sessionManager.getUserId());

        // **Step 2: Initialize UI Components**
        toolbar = findViewById(R.id.toolbar);
        rvMessages = findViewById(R.id.rvMessages);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSend = findViewById(R.id.btnSend);

        // **Step 3: Set Up the Toolbar**
        toolbar.setTitle(recipientName != null ? recipientName : "Chat");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish()); // Handle back button click

        // **Step 4: Set Up the RecyclerView**
        setupRecyclerView();

        // **Step 5: Set Up Input Listeners**
        setupInputListeners();

        markMessagesAsRead();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ✅ 2. Start the SignalR connection when the screen becomes visible
        signalRService.startConnection(
                () -> runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Connected", Toast.LENGTH_SHORT).show()),
                () -> runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Connection failed", Toast.LENGTH_SHORT).show())
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        // ✅ 3. Stop the connection when the screen is no longer visible to save battery
        signalRService.stopConnection();
    }

    // ✅ 4. This method is now called BY SignalR when a new message arrives
    public void onMessageReceived(String senderId, String message) {
        // Make sure the message is from the person we are currently chatting with
        if (senderId.equals(recipientUserId)) {
            ChatMessage receivedMessage = new ChatMessage(
                    UUID.randomUUID().toString(),
                    senderId,
                    currentUserId,
                    message,
                    new Date(),
                    false // It was NOT sent by the current user
            );
            messageAdapter.addMessage(receivedMessage);
            rvMessages.scrollToPosition(0);
        }
    }

    private void setupRecyclerView() {
        chatMessages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, chatMessages, currentUserId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true); // This makes the list stack from the bottom

        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(messageAdapter);

        loadChatHistory();
    }

    private void sendMessage() {
        String messageBody = etMessageInput.getText().toString().trim();
        if (messageBody.isEmpty()) {
            return;
        }

        // ✅ 5. Use the service to send the message to the backend
        signalRService.sendMessage(recipientUserId, messageBody);

        // Add the message to our own UI right away for a smooth experience
        ChatMessage sentMessage = new ChatMessage(
                UUID.randomUUID().toString(),
                currentUserId,
                recipientUserId,
                messageBody,
                new Date(),
                true // It WAS sent by the current user
        );
        messageAdapter.addMessage(sentMessage);
        rvMessages.scrollToPosition(0);

        etMessageInput.setText(""); // Clear the input field
    }

    private void setupInputListeners() {
        // Enable/disable the send button based on whether there's text
        etMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable the button only if the input is not empty
                btnSend.setEnabled(!s.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle the send button click
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void loadChatHistory() {
        if (recipientUserId == null) return;

        // Get the auth token
        SessionManager sessionManager = new SessionManager(this);
        String authToken = "Bearer " + sessionManager.getToken();

        // Create the API client
        MessageApi api = ApiClient.getBackendClient(this).create(MessageApi.class);
        Call<List<ChatMessageDto>> call = api.getChatHistory(Long.parseLong(recipientUserId));

        call.enqueue(new Callback<List<ChatMessageDto>>() {
            @Override
            public void onResponse(Call<List<ChatMessageDto>> call, Response<List<ChatMessageDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chatMessages.clear();
                    // Convert the DTOs from the server into the ChatMessage model your adapter uses
                    for (ChatMessageDto dto : response.body()) {
                        ChatMessage message = new ChatMessage(
                                String.valueOf(dto.getMessageId()),
                                String.valueOf(dto.getSenderId()),
                                recipientUserId, // Can be either sender or receiver
                                dto.getMessageText(),
                                dto.getSentAt(),
                                String.valueOf(dto.getSenderId()).equals(currentUserId)
                        );
                        chatMessages.add(message);
                    }
                    // The API returns messages oldest-to-newest, but the reversed RecyclerView
                    // needs the list to be newest-to-oldest to display correctly.
                    Collections.reverse(chatMessages);
                    messageAdapter.notifyDataSetChanged();
                    rvMessages.scrollToPosition(0); // Scroll to the bottom
                } else {
                    Toast.makeText(ChatActivity.this, "Failed to load chat history.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ChatMessageDto>> call, Throwable t) {
                Toast.makeText(ChatActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void markMessagesAsRead() {
        if (recipientUserId == null) return;

        SessionManager sessionManager = new SessionManager(this);
        String authToken = "Bearer " + sessionManager.getToken();

        MessageApi api = ApiClient.getBackendClient(this).create(MessageApi.class);
        api.markMessagesAsRead(Long.parseLong(recipientUserId)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("ChatActivity", "Messages marked as read.");
                } else {
                    Log.e("ChatActivity", "Failed to mark messages as read.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("ChatActivity", "Network error marking messages as read: " + t.getMessage());
            }
        });
    }
}