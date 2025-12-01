package com.example.dishora.vendorUI.inboxTab.chatTab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar; // Import this
import android.widget.TextView; // Import this
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.models.ConversationDto;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.MessageApi;
import com.example.dishora.network.SignalRService;
import com.example.dishora.utils.SessionManager;
import com.example.dishora.utils.chat.ChatActivity;
import com.example.dishora.defaultUI.inboxTab.chatTab.model.ChatItem;
import com.example.dishora.utils.chat.ChatAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VendorChatTabFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatItem> chatList;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    // Add these view variables
    private ProgressBar progressBar;
    private TextView textEmpty;

    // BroadcastReceiver to listen for new messages in real-time
    private final BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && SignalRService.ACTION_NEW_MESSAGE.equals(intent.getAction())) {
                // A new message has arrived, refresh the conversation list
                loadConversations();
            }
        }
    };

    public VendorChatTabFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vendor_chat_tab, container, false);
        recyclerView = view.findViewById(R.id.vendorChatRecyclerView);

        // Find the new views
        progressBar = view.findViewById(R.id.progressBar);
        textEmpty = view.findViewById(R.id.textEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatList = new ArrayList<>();
        // Set up the adapter with an empty list initially
        adapter = new ChatAdapter(chatList, chatItem -> {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("RECIPIENT_USER_ID", chatItem.getRecipientId());
            intent.putExtra("RECIPIENT_NAME", chatItem.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load conversations when the fragment becomes visible
        loadConversations();

        // Register the receiver to listen for real-time updates
        IntentFilter filter = new IntentFilter(SignalRService.ACTION_NEW_MESSAGE);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(newMessageReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // Unregister the receiver to prevent memory leaks
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(newMessageReceiver);
    }

    private void loadConversations() {
        // 1. Show loading, hide others
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        textEmpty.setVisibility(View.GONE);

        SessionManager sessionManager = new SessionManager(getContext());
        String authToken = "Bearer " + sessionManager.getToken();

        // Use the same MessagesApi as the customer side
        MessageApi api = ApiClient.getBackendClient(getContext()).create(MessageApi.class);
        api.getVendorConversations().enqueue(new Callback<List<ConversationDto>>() {
            @Override
            public void onResponse(Call<List<ConversationDto>> call, Response<List<ConversationDto>> response) {
                // 2. Hide loading
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    chatList.clear();
                    for (ConversationDto dto : response.body()) {
                        chatList.add(new ChatItem(
                                String.valueOf(dto.getRecipientId()),
                                R.drawable.profileimg, // Replace with real customer profile image later
                                dto.getRecipientName(),
                                dto.getLastMessage(),
                                timeFormat.format(dto.getTimestamp()),
                                String.valueOf(dto.getUnreadCount())
                        ));
                    }

                    // 3. Check if the list is empty
                    if (chatList.isEmpty()) {
                        textEmpty.setText("Your inbox is empty.");
                        textEmpty.setVisibility(View.VISIBLE);
                    } else {
                        adapter.notifyDataSetChanged();
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    // 3. Handle failure
                    Toast.makeText(getContext(), "Failed to load vendor chats", Toast.LENGTH_SHORT).show();
                    textEmpty.setText("Failed to load chats.");
                    textEmpty.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<ConversationDto>> call, Throwable t) {
                // 2. Hide loading
                progressBar.setVisibility(View.GONE);

                // 3. Handle failure
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                textEmpty.setText("A network error occurred.");
                textEmpty.setVisibility(View.VISIBLE);
            }
        });
    }
}