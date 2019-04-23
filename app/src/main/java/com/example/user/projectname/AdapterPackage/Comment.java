package com.example.user.projectname.AdapterPackage;

import java.util.HashMap;

/**
 * Created by user on 16.04.2018.
 */

public class Comment {
    private String author;
    private String comment;

    public Comment() {};

    public Comment(String author, String comment) {
        this.author = author;
        this.comment = comment;
    }

    public String getAuthor() {
        return author;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "author='" + author + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("author", this.author);
        result.put("comment", this.comment);
        return result;
    }
}
