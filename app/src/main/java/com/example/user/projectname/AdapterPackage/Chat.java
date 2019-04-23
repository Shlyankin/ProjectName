package com.example.user.projectname.AdapterPackage;

import java.util.HashMap;

/**
 * Created by user on 07.05.2018.
 */

public class Chat {
    private String chatName;
    private int countUsers;
    private String password;

    public Chat() {
        chatName = "";
        countUsers = 1;
        password = "";
    }

    public Chat(String chatName) {
        this.chatName = chatName;
        countUsers = 1;
        password = "";
    }

    public Chat(String chatName, String password) {
        this.chatName = chatName;
        countUsers = 1;
        this.password = password;
    }

    public String getChatName() {
        return chatName;
    }

    @Override
    public String toString() {
        return "Chat{" +
                "chatName='" + chatName + '\'' +
                ", countUsers=" + countUsers +
                ", password='" + password + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Chat chat = (Chat) o;

        return chatName != null ? chatName.equals(chat.chatName) : chat.chatName == null;
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("chatName", this.chatName);
        result.put("countUsers", this.countUsers);
        result.put("password", this.password);
        return result;
    }
}
