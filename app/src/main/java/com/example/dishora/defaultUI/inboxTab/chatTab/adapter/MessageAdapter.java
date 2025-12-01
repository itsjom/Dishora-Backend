package com.example.dishora.defaultUI.inboxTab.chatTab.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.dishora.R;
import com.example.dishora.defaultUI.inboxTab.chatTab.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;

    private final List<ChatMessage> messageList;
    private final String currentUserId; // Needed to determine sent vs. received
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageAdapter(Context context, List<ChatMessage> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
        // You might need context later for loading images, etc.
    }

    // --- Determine View Type ---
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messageList.get(position);
        // Use the isSentByUser flag or compare senderId
        if (message.isSentByUser()) { // Or message.getSenderId().equals(currentUserId)
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    // --- Create ViewHolders ---
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SENT) {
            View view = inflater.inflate(R.layout.item_chat_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else { // VIEW_TYPE_RECEIVED
            View view = inflater.inflate(R.layout.item_chat_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }

    // --- Bind Data to ViewHolders ---
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messageList.get(position);
        String formattedTime = message.getTimestamp() != null ? timeFormat.format(message.getTimestamp()) : "";

        if (holder.getItemViewType() == VIEW_TYPE_SENT) {
            SentMessageViewHolder sentHolder = (SentMessageViewHolder) holder;
            sentHolder.tvMessageBody.setText(message.getMessageBody());
            sentHolder.tvTimestamp.setText(formattedTime);
        } else {
            ReceivedMessageViewHolder receivedHolder = (ReceivedMessageViewHolder) holder;
            receivedHolder.tvMessageBody.setText(message.getMessageBody());
            receivedHolder.tvTimestamp.setText(formattedTime);
            // Optional: Set sender name if group chat
            // receivedHolder.tvSenderName.setText(message.getSenderName()); // Need sender name in model
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // --- ViewHolder for SENT messages ---
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageBody;
        TextView tvTimestamp;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageBody = itemView.findViewById(R.id.tvMessageBody);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }

    // --- ViewHolder for RECEIVED messages ---
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessageBody;
        TextView tvTimestamp;
        // TextView tvSenderName; // Optional: For sender name in group chats

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessageBody = itemView.findViewById(R.id.tvMessageBody);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            // tvSenderName = itemView.findViewById(R.id.tvSenderName); // Optional
        }
    }

    // Helper method to add a new message and notify the adapter
    public void addMessage(ChatMessage message) {
        // 1. Add the message to the START of the list (position 0)
        //    This is correct because your RecyclerView is reversed.
        messageList.add(0, message);

        // 2. Notify that an item was inserted at position 0
        //    Your old code notified the wrong position, causing the bug.
        notifyItemInserted(0);
    }
}
