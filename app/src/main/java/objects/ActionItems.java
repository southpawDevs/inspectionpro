package objects;

import android.text.BoringLayout;

import java.util.Date;

/**
 * Created by keith on 07/03/2018.
 */

public class ActionItems {

    private String item_existing_id;
    private String item_name;
    private String ai_id;
    private String item_report_description;
    private Date item_reported_at;
    private String item_reported_by;
    private String item_checked_reported_comments;
    private Date item_checked_reported_at;
    private String item_checked_reported_by;
    private String inspection_id;
    private String inspection_name;
    private String item_reported_photo;
    private Boolean ai_rectified_status;

    public ActionItems(){

    }

    //constructor
    public ActionItems(String ai_id, String itemId, String itemName, String itemReportDescription, Date itemReportedAt, String itemReportedBy, String itemReportedPhoto, String inspection_name, String inspection_id, Boolean rectified_status) {
        this.item_existing_id = itemId;
        this.item_name = itemName;
        this.item_report_description = itemReportDescription;
        this.item_reported_at = itemReportedAt;
        this.item_reported_by = itemReportedBy;
        this.item_reported_photo = itemReportedPhoto;
        this.inspection_id = inspection_id;
        this.inspection_name = inspection_name;
        this.ai_rectified_status = rectified_status;
    }


    //getters


    public Boolean getAi_rectified_status() {
        return ai_rectified_status;
    }

    public Date getItem_checked_reported_at() {
        return item_checked_reported_at;
    }

    public String getItem_checked_reported_comments() {
        return item_checked_reported_comments;
    }

    public String getItem_checked_reported_by() {
        return item_checked_reported_by;
    }

    public String getAi_id() {
        return ai_id;
    }

    public Date getItem_reported_at() {
        return item_reported_at;
    }

    public String getInspection_id() {
        return inspection_id;
    }

    public String getInspection_name() {
        return inspection_name;
    }

    public String getItem_existing_id() {
        return item_existing_id;
    }

    public String getItem_name() {
        return item_name;
    }

    public String getItem_report_description() {
        return item_report_description;
    }

    public String getItem_reported_by() {
        return item_reported_by;
    }

    public String getItem_reported_photo() {
        return item_reported_photo;
    }


    //setters
    public void setInspection_id(String inspection_id) {
        this.inspection_id = inspection_id;
    }

    public void setInspection_name(String inspection_name) {
        this.inspection_name = inspection_name;
    }

    public void setItem_existing_id(String item_existing_id) {
        this.item_existing_id = item_existing_id;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public void setItem_report_description(String item_report_description) {
        this.item_report_description = item_report_description;
    }

    public void setItem_reported_at(Date item_reported_at) {
        this.item_reported_at = item_reported_at;
    }

    public void setItem_reported_by(String item_reported_by) {
        this.item_reported_by = item_reported_by;
    }

    public void setItem_reported_photo(String item_reported_photo) {
        this.item_reported_photo = item_reported_photo;
    }

    public void setAi_id(String ai_id) {
        this.ai_id = ai_id;
    }

    public void setItem_checked_reported_at(Date item_checked_reported_at) {
        this.item_checked_reported_at = item_checked_reported_at;
    }

    public void setItem_checked_reported_by(String item_checked_reported_by) {
        this.item_checked_reported_by = item_checked_reported_by;
    }

    public void setItem_checked_reported_comments(String item_checked_reported_comments) {
        this.item_checked_reported_comments = item_checked_reported_comments;
    }

    public void setAi_rectified_status(Boolean ai_rectified_status) {
        this.ai_rectified_status = ai_rectified_status;
    }
}
