package com.example.dishora.defaultUI.inboxTab.chatTab;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.dishora.R;
import com.example.dishora.models.ConversationDto;
import com.example.dishora.network.ApiClient;
import com.example.dishora.network.MessageApi;
import com.example.dishora.network.SignalRService;
import com.example.dishora.utils.SessionManager;
import com.example.dishora.utils.chat.ChatAdapter;
import com.example.dishora.utils.chat.ChatActivity;
import com.example.dishora.defaultUI.inboxTab.chatTab.model.ChatItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerChatTabFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private List<ChatItem> chatList;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    // ✅ 3. Define the BroadcastReceiver
    private final BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Check if the broadcast is the one we're interested in
            if (intent != null && SignalRService.ACTION_NEW_MESSAGE.equals(intent.getAction())) {
                // A new message has arrived from someone. Refresh the list.
                // A simple Toast can help confirm it's working during testing.
                // Toast.makeText(getContext(), "New message received, refreshing list...", Toast.LENGTH_SHORT).show();
                loadConversations();
            }
        }
    };

    public CustomerChatTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_tab, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.chatRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatList = new ArrayList<>();
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
        // Load conversations when the fragment is first shown
        loadConversations();

        // ✅ 4. Register the receiver to start listening for new message alerts
        IntentFilter filter = new IntentFilter(SignalRService.ACTION_NEW_MESSAGE);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(newMessageReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        // ✅ 5. Unregister the receiver to stop listening and prevent memory leaks
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(newMessageReceiver);
    }

    private void loadConversations() {
        SessionManager sessionManager = new SessionManager(getContext());
        String authToken = "Bearer " + sessionManager.getToken();

        MessageApi api = ApiClient.getBackendClient(getContext()).create(MessageApi.class);
        api.getCustomerConversations().enqueue(new Callback<List<ConversationDto>>() {
            @Override
            public void onResponse(Call<List<ConversationDto>> call, Response<List<ConversationDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chatList.clear();
                    // Convert server DTOs to the UI's ChatItem model
                    for (ConversationDto dto : response.body()) {
                        chatList.add(new ChatItem(
                                String.valueOf(dto.getRecipientId()),
                                R.drawable.profileimg, // Replace with real image URL later
                                dto.getRecipientName(),
                                dto.getLastMessage(),
                                timeFormat.format(dto.getTimestamp()),
                                String.valueOf(dto.getUnreadCount())
                        ));
                    }
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load chats", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ConversationDto>> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}