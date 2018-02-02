package objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by keith on 30/01/2018.
 */

public class Inspection{

    private String inspectionID;
    private String inspectionName;
    private String inspectionCreatedBy;
    private long inspectionCreatedAt;
    private String inspectionModifiedBy;
    private long inspectionModifiedAt;
    private int inspectionStatus;
    private ArrayList<InspectionItem> items = new ArrayList<>();

    //add department ( marine , drilling)

    //constructor
    public Inspection(String inspectionName, String inspectionID, long inspectionCreatedAt, String inspectionCreatedBy, long inspectionModifiedAt, String inspectionModifiedBy, int inspectionStatus) {
        this.inspectionName = inspectionName;
        this.inspectionID = inspectionID;
        this.inspectionCreatedAt = inspectionCreatedAt;
        this.inspectionCreatedBy = inspectionCreatedBy;
        this.inspectionModifiedAt = inspectionModifiedAt;
        this.inspectionModifiedBy = inspectionModifiedBy;
        this.inspectionStatus = inspectionStatus;
    }

    //getters
    public String getInspectionName() {
        return inspectionName;
    }

    public String getInpectionID() {
        return inspectionID;
    }

    public String getInspectionCreatedBy() {
        return inspectionCreatedBy;
    }

    public long getInspectionCreatedAt() {
        return inspectionCreatedAt;
    }

    public String getInspectionModifiedBy() {
        return inspectionModifiedBy;
    }

    public long getInspectionModifiedAt() {
        return inspectionModifiedAt;
    }

    public int getInspectionStatus() {
        return inspectionStatus;
    }

    //setters
    public void setInspectionName(String inspectionName) {
        this.inspectionName = inspectionName;
    }

    public void setInspectionID(String inspectionID) {
        this.inspectionID = inspectionID;
    }

    public void setInspectionCreatedBy(String inspectionCreatedBy) {
        this.inspectionCreatedBy = inspectionCreatedBy;
    }

    public void setInspectionCreatedAt(long inspectionCreatedAt) {
        this.inspectionCreatedAt = inspectionCreatedAt;
    }

    //get itemArray
    public ArrayList<InspectionItem> getInspectionItems() {
        return items;
    }

    //set itemArray
    public void setInspectionItems(ArrayList<InspectionItem> inspectionItems) {
        this.items = inspectionItems;
    }

}
