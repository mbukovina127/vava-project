package org.shippin.app.models;

public class User {


    private String name;
    private String role;
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public User(int id, String name, String role) {
        this.id= id;
        this.name = name;
        this.role = role;
    }

    public User(String name, String role) {

        this.name = name;
        this.role = role;
    }


}

