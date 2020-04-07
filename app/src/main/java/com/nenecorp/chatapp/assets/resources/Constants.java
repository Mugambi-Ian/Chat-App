package com.nenecorp.chatapp.assets.resources;

import android.content.Context;
import android.content.SharedPreferences;

import com.nenecorp.chatapp.models.chats.ChatRoom;
import com.nenecorp.chatapp.models.user.User;

public class Constants {
    public static final String USERS = "Users";
    public static String ChatRooms = "ChatRooms";
    public static String Online = "Online", Offline = "Offline";
    public static String Requests = "Requests", Contacts = "Contacts";
    public static ChatRoom chatRoom;
    public static String Messages = "Messages";

    public static String getInboxId(User... users) {
        char _1 = users[0].userId.charAt(0);
        char _2 = users[1].userId.charAt(0);
        String chatId = users[0].userId + users[1].userId;
        if (_1 >= _2) {
            chatId = users[1].userId + users[0].userId;
        }
        return chatId;
    }

    public static void saveChatState(String chatId, boolean b, Context c) {
        SharedPreferences preferences = c.getSharedPreferences("Chat-State", Context.MODE_PRIVATE);
        preferences.edit().putBoolean(chatId, b).apply();
    }

    public static boolean chatState(String chatId, Context c) {
        SharedPreferences preferences = c.getSharedPreferences("Chat-State", Context.MODE_PRIVATE);
        return preferences.getBoolean(chatId, false);
    }

}
