package objects;

public class Members {

    public String member_id;
    public String member_name;
    public String member_email;
    public String member_department_id;
    public String member_department_name;
    public String member_profile_picture;


    public Members() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Members(String member_id, String member_name,String member_email,String member_department_id,String member_department_name) {
        this.member_department_id = member_department_id;
        this.member_id = member_id;
        this.member_department_name = member_department_name;
        this.member_name = member_name;
        this.member_email = member_email;
    }

    public String getMember_profile_picture() {
        return member_profile_picture;
    }

    public String getMember_department_id() {
        return member_department_id;
    }

    public String getMember_department_name() {
        return member_department_name;
    }

    public String getMember_email() {
        return member_email;
    }

    public String getMember_id() {
        return member_id;
    }

    public String getMember_name() {
        return member_name;
    }

    public void setMember_department_id(String member_department_id) {
        this.member_department_id = member_department_id;
    }

    public void setMember_department_name(String member_department_name) {
        this.member_department_name = member_department_name;
    }

    public void setMember_email(String member_email) {
        this.member_email = member_email;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }

    public void setMember_name(String member_name) {
        this.member_name = member_name;
    }

    public void setMember_profile_picture(String member_profile_picture) {
        this.member_profile_picture = member_profile_picture;
    }
}
