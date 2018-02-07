package objects;

import android.media.Image;

/**
 * Created by keith on 30/01/2018.
 */

public class InspectionItem {

    private String item_id;
    private String item_name;
    private String item_description;
    private String item_method;
    private int item_photo;
    private String item_condition;
    private String item_comments;
    private long item_modified_at;
    private String item_modified_by;
    private int item_status;
    private String inspection_id;
    private Image item_new_photo;

    public InspectionItem(){

    }

    //constructor
    public InspectionItem(String itemID, String itemDescription, String itemMethod, int itemPhoto, String itemCondition, String itemComments, long itemModifiedAt, String itemModifiedBy, int itemStatus) {
        this.item_id = itemID;
        this.item_description = itemDescription;
        this.item_method = itemMethod;
        this.item_condition = itemCondition;
        this.item_photo = itemPhoto;
        this.item_comments = itemComments;
        this.item_modified_at = itemModifiedAt;
        this.item_modified_by = itemModifiedBy;
        this.item_status = itemStatus;
    }

    //getters
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

}
