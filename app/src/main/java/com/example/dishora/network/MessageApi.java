package com.example.dishora.network;

// --- Models ---
import com.example.dishora.models.ChatMessageDto;
import com.example.dishora.models.ConversationDto;
import com.example.dishora.models.UnreadCountResponse;

// --- Other Imports ---
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
// No need for @Header, the authenticated client handles it

public interface MessageApi {

    // --- Messages ---

    // Get chat history between current user and another user
    @GET("messages/history/{recipientId}")
    Call<List<ChatMessageDto>> getChatHistory(@Path("recipientId") long recipientId);

    // Mark messages from a specific sender as read
    @POST("messages/markasread/{senderId}")
    Call<Void> markMessagesAsRead(@Path("senderId") long senderId);

    // FOR THE CUSTOMER UI
    @GET("messages/unread-count/customer")
    Call<UnreadCountResponse> getCustomerUnreadCount(); // For badge

    @GET("messages/conversations/customer")
    Call<List<ConversationDto>> getCustomerConversations(); // For inbox list

    // FOR THE VENDOR UI
    @GET("messages/unread-count/vendor")
    Call<UnreadCountResponse> getVendorUnreadCount(); // For badge

    @GET("messages/conversations/vendor")
    Call<List<ConversationDto>> getVendorConversations(); // For inbox list
}