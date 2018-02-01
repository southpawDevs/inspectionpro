package objects;

import android.media.Image;

/**
 * Created by keith on 30/01/2018.
 */

public class InspectionItem {

    private String itemID;
    private String itemDescription;
    private String itemMethod;
    private int itemPhoto;
    private String itemCondition;
    private String itemComments;
    private long itemModifiedAt;
    private String itemModifiedBy;
    private int itemStatus;
    private String inspectionID;
    private Image itemNewPhoto;

    //constructor
    public InspectionItem(String itemID, String itemDescription, String itemMethod, int itemPhoto, String itemCondition, String itemComments, long itemModifiedAt, String itemModifiedBy, int itemStatus) {
        this.itemID = itemID;
        this.itemDescription = itemDescription;
        this.itemMethod = itemMethod;
        this.itemCondition = itemCondition;
        this.itemPhoto = itemPhoto;
        this.itemComments = itemComments;
        this.itemModifiedAt = itemModifiedAt;
        this.itemModifiedBy = itemModifiedBy;
        this.itemStatus = itemStatus;
    }

    //getters
    public String getItemName() {
        return itemDescription;
    }

    public int getItemStatus() {
        return itemStatus;
    }

    public String getItemMethod() {
        return itemMethod;
    }

    public String getItemCondition() {
        return itemCondition;
    }

}
