package Model.Domain;

import java.io.Serializable;

public class User implements Serializable {
    private int ID;
    private String username;
    private String avatar;

    public User(String username) {
        this.username = username;
    }

    public User(int ID, String username, String avatar){
        this.ID = ID;
        this.username = username;
        this.avatar = avatar;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getUsername() {
        return username;
    }
}
