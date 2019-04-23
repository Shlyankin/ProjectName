package com.example.user.projectname.AdapterPackage;

import java.util.HashMap;

/**
 * Created by user on 07.06.2018.
 */

public class CustomNote {
    private String date;
    private String text;

    public CustomNote(String date, String text) {
        this.date = date;
        this.text = text;
    }

    public HashMap toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("date", date);
        result.put("text", text);
        return result;
    }

    @Override
    public String toString() {
        return "Note{" +
                "date='" + date + '\'' +
                ", text='" + text + '\'' +
                '}';
    }

    public String getDate() {
        return date;
    }

    public String getText() {
        return text;
    }
}
