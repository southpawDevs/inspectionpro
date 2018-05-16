package objects;

import java.util.ArrayList;
import java.util.List;

public class Department {

    public String department_name;
    public String department_photo;
    public String department_color_hex;
    public String department_id;

    public Department() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Department(String department_name, String department_color_hex, String department_photo) {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public String getDepartment_id() {
        return department_id;
    }

    public String getDepartment_color_hex() {
        return department_color_hex;
    }

    public String getDepartment_name() {
        return department_name;
    }

    public String getDepartment_photo() {
        return department_photo;
    }


    public void setDepartment_color_hex(String department_color_hex) {
        this.department_color_hex = department_color_hex;
    }

    public void setDepartment_name(String department_name) {
        this.department_name = department_name;
    }

    public void setDepartment_photo(String department_photo) {
        this.department_photo = department_photo;
    }

    public void setDepartment_id(String department_id) {
        this.department_id = department_id;
    }
}
