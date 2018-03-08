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
    public String profile_picture;
    public String assigned_property;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String username, String email, Boolean admin_rights, Boolean developer_rights, String profile_picture, String assigned_property) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.admin_rights = admin_rights;
        this.developer_rights = developer_rights;
        this.profile_picture = profile_picture;
        this.assigned_property = assigned_property;
    }

    public String getUsername() {
        return username;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public Boolean getDeveloper_rights() {
        return developer_rights;
    }

    public Boolean getAdmin_rights() {
        return admin_rights;
    }

    public String getAssigned_property() {
        return assigned_property;
    }

    public String getProfile_picture() {
        return profile_picture;
    }


}
