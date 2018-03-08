package objects;

import android.media.Image;

import java.util.Date;

/**
 * Created by keith on 30/01/2018.
 */

public class InspectionItem {

    private String item_id;
    private String item_name;
    private String item_description;
    private String item_method;
    private String item_photo;
    private String item_condition;
    private String item_comments;
    private String item_check_question;
    private Date item_modified_at;
    private String item_modified_by;
    private Date item_reported_at;
    private String item_reported_by;
    private int item_status;
    private String inspection_id;
    private String item_condition_photo;

    public InspectionItem(){

    }

    //constructor
    public InspectionItem(String itemName, String itemDescription, String itemMethod, String itemCondition, String itemPhoto, String itemComments, Date itemModifiedAt, String itemModifiedBy, Date itemReportedAt, String itemReportedBy, int itemStatus, String itemConditionPhoto, String itemCheckQuestion) {
        this.item_name = itemName;
        this.item_description = itemDescription;
        this.item_method = itemMethod;
        this.item_condition = itemCondition;
        this.item_photo = itemPhoto;
        this.item_comments = itemComments;
        this.item_modified_at = itemModifiedAt;
        this.item_modified_by = itemModifiedBy;
        this.item_reported_at = itemReportedAt;
        this.item_reported_by = itemReportedBy;
        this.item_status = itemStatus;
        this.item_condition_photo = itemConditionPhoto;
        this.item_check_question = itemCheckQuestion;
    }

    //getters
    public String getItem_id() {
        return item_id;
    }

    public String getItem_name() {
        return item_name;
    }

    public String getItem_description(){
        return item_description;
    }

    public int getItem_status() {
        return item_status;
    }

    public String getItem_method() {
        return item_method;
    }

    public String getItem_condition() {
        return item_condition;
    }

    public String getItem_comments() {
        return item_comments;
    }

    public String getItem_photo() {
        return item_photo;
    }

    public String getItem_modified_by() {
        return item_modified_by;
    }

    public Date getItem_modified_at() {
        return item_modified_at;
    }

    public String getItem_reported_by() {
        return item_reported_by;
    }

    public Date getItem_reported_at() {
        return item_reported_at;
    }

    public String getItem_condition_photo(){return  item_condition_photo;}

    public String getItem_check_question(){return  item_check_question;}


    //setters
    public void setItem_name(String itemName) {
        this.item_name = itemName;
    }

}
