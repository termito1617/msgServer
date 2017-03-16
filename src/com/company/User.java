package com.company;
/**
 * Created by vlad on 22.02.2017.
 */
public class User {
    private int    id;
    private String login;
    private String passHash;
    private String name;
    private String lastName;

    public User(String s)  {
       initFromString(s);
    }
    public User(String login, String passHash, String name, String lastName, int id) {
        this.login = login;
        this.passHash = passHash;
        this.name = name;
        this.lastName = lastName;
        this.id = id;
    }
    public User() {
        id = 0;
        login    = "";
        passHash = "";
        name     = "";
        lastName = "";
    }

    public void setLogin(String login) {
        this.login = login;
    }
    public void setPassHash(String Password) {
        passHash = Password;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name= name;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void initFromString(String u) {
        String []user = u.split(" ");

        if (user.length != 4) {
            login    = "default";
            passHash = "default";
            name     = "default";
            lastName = "default";
        } else {
            login    = user[0];
            passHash = user[1];
            name     = user[2];
            lastName = user[3];
        }

    }
    public String getLogin() {
        return login;
    }
    public String getPassHash() {
        return passHash;
    }
    public String getName() {
        return name;
    }
    public String getLastName() {
        return lastName;
    }
    public int    getId() { return id; }

    public String toString() {
        return login + " " + passHash + " " + name + " " + lastName;
    }

}
