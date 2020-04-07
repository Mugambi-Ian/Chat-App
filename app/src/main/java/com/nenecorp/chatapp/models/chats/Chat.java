package com.nenecorp.chatapp.models.chats;

import com.google.firebase.database.DataSnapshot;

import java.util.Calendar;

public class Chat {
    private String msgId;
    private String senderId;
    private String msgMessage;
    private String msgDate;
    private String msgTime;

    public Chat(String msgMessage, String senderId) {
        this.msgMessage = msgMessage;
        this.senderId = senderId;
        Calendar x = Calendar.getInstance();
        msgDate = cleanInt(x.get(Calendar.MONTH)) + "-" + cleanInt(x.get(Calendar.DAY_OF_MONTH)) + "-" + cleanInt(x.get(Calendar.YEAR));
        msgTime = cleanInt(x.get(Calendar.HOUR_OF_DAY)) + ":" + cleanInt(x.get(Calendar.MINUTE));
        msgId = cleanInt(x.get(Calendar.YEAR))
                + cleanInt(x.get(Calendar.MONTH))
                + cleanInt(x.get(Calendar.DAY_OF_MONTH))
                + cleanInt(x.get(Calendar.HOUR_OF_DAY))
                + cleanInt(x.get(Calendar.MINUTE))
                + cleanInt(x.get(Calendar.SECOND))
                + cleanInt(x.get(Calendar.MILLISECOND))
                + ranChar(5);
    }

    private String ranChar(int n) {
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            int index = (int) (AlphaNumericString.length() * Math.random());
            sb.append(AlphaNumericString.charAt(index));
        }
        return sb.toString();
    }

    private String cleanInt(int x) {
        if (x > 9) {
            return "0" + x;
        } else {
            return "" + x;
        }
    }

    public void setChatId(String msgId) {
        this.msgId = msgId;
    }

    public Chat(DataSnapshot snapshot) {
        msgId = snapshot.child("msgId").getValue(String.class);
        msgMessage = snapshot.child("msgMessage").getValue(String.class);
        senderId = snapshot.child("senderId").getValue(String.class);
        msgDate = snapshot.child("msgDate").getValue(String.class);
        msgTime = snapshot.child("msgTime").getValue(String.class);
    }

    public String getMsgDate() {
        return msgDate;
    }

    public String getMsgMessage() {
        return msgMessage;
    }

    public String getMsgTime() {
        return msgTime;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgMessage(String msgMessage) {
        this.msgMessage = msgMessage;
    }
}
