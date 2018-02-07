package objects;

/**
 * Created by keith on 03/02/2018.
 */

public class User {

    public String uid;
    public String username;
    public String email;
    public Boolean admin_rights;
    public Boolean developer_rights;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String username, String email, Boolean admin_rights, Boolean developer_rights) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.admin_rights = admin_rights;
        this.developer_rights = developer_rights;
    }
}
