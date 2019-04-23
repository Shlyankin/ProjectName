package com.example.user.projectname.AdapterPackage;

import java.util.HashMap;
import java.util.Map;

public class User {

    public String name;
    public int admin;


    public User() { }

    public User(int admin, String name) {
        this.admin = admin;
        this.name = name;
    }

    public User(String firstName, String secondName) {
        this.admin = 0;
        this.name = firstName + " " + secondName;
    }

    public String getName() {
        return name;
    }

    public int getAdmin() { return admin; }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", admin=" + admin +
                '}';
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("admin", 0);
        return result;
    }
}
