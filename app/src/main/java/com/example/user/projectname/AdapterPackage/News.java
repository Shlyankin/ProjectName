package com.example.user.projectname.AdapterPackage;

import java.util.HashMap;

public class News {
    private String id;
    private String name;
    private String category;
    private String date;
    private String about;
    private String author;
    private String uri;
    private String storagePath;
    private boolean subscribe;


    public News(String id, String name, String date, String category, String about, String author, String storagePath, String uri, boolean subscribe) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.date = date;
        this.about = about;
        this.author = author;
        this.storagePath = storagePath;
        this.uri = uri;
        this.subscribe = subscribe;
    }

    /*public News(String id, String name, String date, String category, String about, String author) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.date = date;
        this.about = about;
        this.author = author;
    }*/

    public News() {
        this.id = "";
        this.name = "";
        this.category = "";
        this.date = "";
        this.about = "";
        this.author = "";
        this.storagePath = "";
        this.uri = "";
    }

    public News(News someNews) {
        this.id = someNews.id;
       this.name = someNews.name;
       this.category = someNews.category;
       this.date = someNews.date;
       this.about = someNews.about;
    }

    public void setSubscribe(boolean subscribe) { this.subscribe = subscribe; }

    public boolean getSubscribe() { return subscribe; }

    public String getId() { return  id; }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public String getAbout() { return about; }

    public String getDate() { return date; }

    public String getStoragePath() { return storagePath; }

    public String getUri() { return uri; }

    @Override
    public String toString() {
        return "News{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", date='" + date + '\'' +
                ", about='" + about + '\'' +
                ", author='" + author + '\'' +
                ", uri='" + uri + '\'' +
                ", storagePath='" + storagePath + '\'' +
                '}';
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("id", this.id);
        result.put("name", this.name);
        result.put("category", this.category);
        result.put("date", this.date);
        result.put("about", this.about);
        result.put("author", this.author);
        result.put("uri", this.uri);
        result.put("storagePath", this.storagePath);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        News tmpNews = (News)obj;
        if (
        this.id.equals(tmpNews.id) &&
        this.name.equals(tmpNews.name) &&
        this.category.equals(tmpNews.category) &&
        this.date.equals(tmpNews.date) &&
        this.about.equals(tmpNews.about) &&
        this.author.equals(tmpNews.author) &&
        this.storagePath.equals(tmpNews.storagePath) &&
        this.uri.equals(tmpNews.uri)
                ) return true;
        else return false;
    }
}
