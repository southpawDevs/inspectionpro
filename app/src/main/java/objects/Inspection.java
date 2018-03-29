package objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by keith on 30/01/2018.
 */

public class Inspection{

    private String inspection_id;  //id is created to hold id data temporarily
    private String inspection_name;
    private String inspection_created_by;
    private Date inspection_created_at;
    private String inspection_modified_by;
    private Date inspection_modified_at;
    private String inspection_submitted_by;
    private String inspection_submitted_by_name;
    private Date inspection_submitted_at;
    private int inspection_status;
    private int inspection_items_count;
    private int inspection_pending_count;
    private int inspection_days;
    private List<InspectionItem> inspection_items_object = new ArrayList<>();
    private int inspection_to_be_dued;
    private String inspection_department;
    //add department ( marine , drilling)

    public Inspection(){

    }

    //constructor
    public Inspection(String inspectionName, Date inspection_created_at, String inspectionCreatedBy, Date inspectionModifiedAt, String inspectionModifiedBy, String inspection_submitted_by, String inspection_submitted_by_name, Date inspection_submitted_at ,int inspectionStatus, int inspection_items_count, int inspection_days, String inspection_department) {
        this.inspection_name = inspectionName;
        this.inspection_created_at = inspection_created_at;
        this.inspection_created_by = inspectionCreatedBy;
        this.inspection_modified_at = inspectionModifiedAt;
        this.inspection_modified_by = inspectionModifiedBy;
        this.inspection_status = inspectionStatus;
        this.inspection_items_count = inspection_items_count;
        this.inspection_days = inspection_days;
        this.inspection_submitted_at = inspection_submitted_at;
        this.inspection_submitted_by = inspection_submitted_by;
        this.inspection_department = inspection_department;
    }

    //getters
    public String getInspection_name() {
        return inspection_name;
    }

    public String getInspection_id() {
        return inspection_id;
    }

    public int getInspection_days() {
        return inspection_days;
    }

    public String getInspection_created_by() {
        return inspection_created_by;
    }

    public Date getInspection_created_at() {
        return inspection_created_at;
    }

    public String getInspection_submitted_by() {
        return inspection_submitted_by;
    }

    public String getInspection_submitted_by_name() {
        return inspection_submitted_by_name;
    }

    public Date getInspection_submitted_at() {
        return inspection_submitted_at;
    }

    public String getInspection_modified_by() {
        return inspection_modified_by;
    }

    public Date getInspection_modified_at() {
        return inspection_modified_at;
    }

    public int getInspection_status() {
        return inspection_status;
    }

    public int getInspection_items_count(){return inspection_items_count;}

    public int getInspection_to_be_dued() {
        return inspection_to_be_dued;
    }

    public int getInspection_pending_count() {
        return inspection_pending_count;
    }

    public String getInspection_department() {
        return inspection_department;
    }

    //get itemArray
    public List<InspectionItem> getInspection_items_object() {
        return inspection_items_object;
    }

    //setters
    public void setInspection_name(String inspectionName) {
        this.inspection_name = inspectionName;
    }

    public void setInspection_id(String inspection_id) {
        this.inspection_id = inspection_id;
    }

    public void setInspection_created_by(String inspectionCreatedBy) {
        this.inspection_created_by = inspectionCreatedBy;
    }

    public void setInspection_created_at(Date inspection_created_at) {
        this.inspection_created_at = inspection_created_at;
    }

    public void setInspection_submitted_at(Date inspection_submitted_at) {
        this.inspection_submitted_at = inspection_submitted_at;
    }

    public void setInspection_submitted_by(String inspection_submitted_by) {
        this.inspection_submitted_by = inspection_submitted_by;
    }

    public void setInspection_submitted_by_name(String inspection_submitted_by_name) {
        this.inspection_submitted_by_name = inspection_submitted_by_name;
    }

    public void setInspection_items(List<InspectionItem> inspection_items_object) {
        this.inspection_items_object = inspection_items_object;
    }

    public void setInspection_to_be_dued(int inspection_to_be_dued) {
        this.inspection_to_be_dued = inspection_to_be_dued;
    }

    public void setInspection_pending_count(int inspection_pending_count) {
        this.inspection_pending_count = inspection_pending_count;
    }

    public void setInspection_days(int inspection_days) {
        this.inspection_days = inspection_days;
    }

    public void setInspection_items_count(int inspection_items_count) {
        this.inspection_items_count = inspection_items_count;
    }

    public void setInspection_items_object(List<InspectionItem> inspection_items_object) {
        this.inspection_items_object = inspection_items_object;
    }

    public void setInspection_modified_at(Date inspection_modified_at) {
        this.inspection_modified_at = inspection_modified_at;
    }

    public void setInspection_modified_by(String inspection_modified_by) {
        this.inspection_modified_by = inspection_modified_by;
    }

    public void setInspection_status(int inspection_status) {
        this.inspection_status = inspection_status;
    }

    public void setInspection_department(String inspection_department) {
        this.inspection_department = inspection_department;
    }
}
