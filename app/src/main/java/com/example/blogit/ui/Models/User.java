package com.example.blogit.ui.Models;

public class User {

    public String name;
    public String email;
    public String bio;
    public String website;
    public String dob;
    public String photo;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name,String email) {
        this.name=name;
        this.email=email;
    }

    public User(String name,String bio,String website,String dob,String photo) {
        this.name=name;
        this.bio=bio;
        this.website=website;
        this.dob=dob;
        this.photo =photo;
    }

    public String getName() {
        return name;
    }
    public String getEmail(){
        return email;
    }

    public String getPhoto() {
        return photo;
    }
}
