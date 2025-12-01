package com.example.dishora.utils.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.dishora.R;
import com.example.dishora.defaultUI.inboxTab.chatTab.model.ChatItem;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(ChatItem chatItem);
    }

    private List<ChatItem> chatList;
    private final OnChatClickListener clickListener;

    public ChatAdapter(List<ChatItem> chatList, OnChatClickListener clickListener) {
        this.chatList = chatList;
        this.clickListener = clickListener;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, message, time, badge;

        public ChatViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.chatImage);
            name = itemView.findViewById(R.id.chatName);
            message = itemView.findViewById(R.id.chatMessage);
            time = itemView.findViewById(R.id.chatTime);
            badge = itemView.findViewById(R.id.chatBadge);
        }
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatViewHolder holder, int position) {
        ChatItem item = chatList.get(position);
        holder.image.setImageResource(item.imageResId);
        holder.name.setText(item.name);
        holder.message.setText(item.message);
        holder.time.setText(item.time);

        // ✅ ADD THIS LOGIC
        try {
            int count = Integer.parseInt(item.getBadgeCount());
            if (count > 0) {
                holder.badge.setText(item.getBadgeCount());
                holder.badge.setVisibility(View.VISIBLE);
            } else {
                holder.badge.setVisibility(View.GONE);
            }
        } catch (NumberFormatException e) {
            holder.badge.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> clickListener.onChatClick(item));
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
}
