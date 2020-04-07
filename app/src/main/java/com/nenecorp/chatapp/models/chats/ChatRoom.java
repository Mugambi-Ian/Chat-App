package com.nenecorp.chatapp.models.chats;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.nenecorp.chatapp.assets.resources.Constants;
import com.nenecorp.chatapp.models.DbSocket;
import com.nenecorp.chatapp.models.user.User;

import java.util.ArrayList;
import java.util.HashMap;

public class ChatRoom {
    private String chatId;
    private ArrayList<String> chatMembers;
    private HashMap<String, Chat> chatMessages;
    private DataSnapshot sp;

    public ChatRoom(DataSnapshot dataSnapshot) {
        sp = dataSnapshot;
        chatId = dataSnapshot.child("chatId").getValue(String.class);
        chatMembers = loadChartMembers(dataSnapshot.child("chatMembers"));
        chatMessages = loadChartMessages(dataSnapshot.child("Messages"));
    }

    private HashMap<String, Chat> loadChartMessages(DataSnapshot chats) {
        HashMap<String, Chat> chatArrayList = new HashMap<String, Chat>();
        for (DataSnapshot chat : chats.getChildren()) {
            Chat c = new Chat(chat);
            chat.getRef().keepSynced(true);
            chatArrayList.put(c.getMsgId(), c);
        }
        return chatArrayList;
    }

    public ChatRoom() {
    }

    private ArrayList<String> loadChartMembers(DataSnapshot chatMembers) {
        ArrayList<String> strings = new ArrayList<>();
        for (DataSnapshot s : chatMembers.getChildren()) {
            strings.add(s.getValue(String.class));
        }
        return strings;
    }


    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public ArrayList<String> getChatMembers() {
        return chatMembers;
    }

    public void setChatMembers(ArrayList<String> chatMembers) {
        this.chatMembers = chatMembers;
        if (chatMessages == null) {
            chatMessages = new HashMap<>();
        }
    }

    public ArrayList<Chat> getChatMessagesList() {
        ArrayList<Chat> chats = new ArrayList<>();
        for (String l : chatMessages.keySet()) {
            chats.add(chatMessages.get(l));
        }
        return chats;
    }

    private HashMap<String, Chat> chartMessages() {
        HashMap<String, Chat> h = new HashMap<>();
        for (String i : chatMessages.keySet()) {
            Chat chat = chatMessages.get(i);
            h.put(chat.getMsgId(), chat);
        }
        return h;
    }

    private HashMap<String, String> chatMembers() {
        HashMap<String, String> h = new HashMap<>();
        for (String m : chatMembers) {
            h.put(m, m);
        }
        return h;
    }

    public void sendMessage(String msgContent) {
        String userId = DbSocket.dbHandler.appUser.userId;
        Chat x = new Chat(msgContent, userId);
        DatabaseReference ch = sp.child(Constants.Messages).getRef().push();
        x.setChatId(ch.getKey());
        ch.setValue(x);
    }

    public void translateMessages(final TranslateInterface translateInterface, final Context context, final User... users) {
        Toast.makeText(context, "Translating", Toast.LENGTH_SHORT).show();
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(FirebaseTranslateLanguage.languageForLanguageCode(users[1].userLang))
                        .setTargetLanguage(FirebaseTranslateLanguage.languageForLanguageCode(users[0].userLang))
                        .build();
        final FirebaseTranslator translator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                final int[] a = new int[1];
                                for (String x : chatMessages.keySet()) {
                                    final Chat c = chatMessages.get(x);
                                    if (!c.getSenderId().equals(users[0].userId)) {
                                        translator.translate(c.getMsgMessage())
                                                .addOnSuccessListener(
                                                        new OnSuccessListener<String>() {
                                                            @Override
                                                            public void onSuccess(@NonNull String translatedText) {
                                                                c.setMsgMessage(translatedText);
                                                                chatMessages.put(c.getMsgId(), c);
                                                                a[0]++;
                                                                if (a[0] >= chatMessages.size()) {
                                                                    translateInterface.onComplete(ChatRoom.this);
                                                                }
                                                            }
                                                        })
                                                .addOnFailureListener(
                                                        new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                a[0]++;
                                                                if (a[0] >= chatMessages.size()) {
                                                                    translateInterface.onComplete(ChatRoom.this);
                                                                }

                                                            }
                                                        });
                                    } else {
                                        a[0]++;
                                        if (a[0] >= chatMessages.size()) {
                                            translateInterface.onComplete(ChatRoom.this);
                                        }
                                    }
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        });
    }

    public interface TranslateInterface {
        void onComplete(ChatRoom x);
    }
}
