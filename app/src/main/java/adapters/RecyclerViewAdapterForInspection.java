package adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.gson.Gson;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.R;
import devs.southpaw.com.inspectionpro.UIUtil;
import layout.InspectionDetailsActivity;
import objects.Department;
import objects.Inspection;

/**
 * Created by keith on 30/01/2018.
 */

public class RecyclerViewAdapterForInspection extends RecyclerView.Adapter<RecyclerViewAdapterForInspection.ViewHolder> {

    private List<Inspection> inspectionsData;
    Context mContext;
    Activity mActivity;

    final private RecyclerItemClickListener mOnClickListener;

    public interface RecyclerItemClickListener {
        void onListItemClick(int clickedItemIndex);

    }

    public RecyclerViewAdapterForInspection(List<Inspection> inspectionsData, Context context, Activity activity) {
        this.inspectionsData = inspectionsData;
        mOnClickListener = null;
        mContext = context;
        mActivity = activity;
    }

    public RecyclerViewAdapterForInspection(List<Inspection> inspectionsData, RecyclerItemClickListener mOnClickListener) {
        this.inspectionsData = inspectionsData;
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    public RecyclerViewAdapterForInspection.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_inspection_layout, parent, false);


        ViewHolder viewHolder = new ViewHolder(itemLayoutView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewAdapterForInspection.ViewHolder holder, int position) {

        Inspection currentInspection = inspectionsData.get(position);

        //set the title
        holder.inspectionTitle.setText(currentInspection.getInspection_name());

        //set card background color
        switch (currentInspection.getInspection_status()){
            case 0:
                //new
                holder.cardBackground.setCardBackgroundColor(Color.parseColor("#f7f7f7"));
                holder.inspectionReady.setVisibility(View.GONE);
                break;
            case 1:
                //alert
                //holder.cardBackground.setCardBackgroundColor(Color.parseColor("#fa4048"));
                holder.inspectionReady.setVisibility(View.GONE);
                break;
            case 2:
                //green
                holder.cardBackground.setCardBackgroundColor(Color.parseColor("#61f1a2"));
                holder.inspectionReady.setVisibility(View.VISIBLE);
                holder.inspectionReady.setText("Ready To Submit");
                break;
        }

        //set inspection due date
        String lastChecked = "";
        String supposeToCheck = "";
        String today = "";
        Double hoursRemaining = 0.0;
        String dueInText = "";

        if (currentInspection.getInspection_submitted_at() != null) {
            Date lastCheckedDate = currentInspection.getInspection_submitted_at();
            int inspectionDays = currentInspection.getInspection_days();

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(lastCheckedDate);

            calendar.add(Calendar.DATE, inspectionDays);

            Date supposeToCheckDate = new Date(calendar.getTimeInMillis());
            Date todayDate = new Date();

            //strings
            lastChecked = getStringDateFromDate(currentInspection.getInspection_submitted_at());
            supposeToCheck = getStringDateFromDate(supposeToCheckDate);
            today = getStringDateFromDate(todayDate);

            if (supposeToCheckDate.after(todayDate)){
                hoursRemaining = getHoursRemaining(supposeToCheckDate);

                dueInText = handleDueInText(hoursRemaining);
                holder.inspectionTitle.setTextColor(Color.parseColor("#333333"));
                holder.inspectionLastChecked.setTextColor(Color.parseColor("#333333"));
                holder.inspectionOverdue.setTextColor(Color.parseColor("#333333"));
                holder.inspectionItemsCount.setTextColor(Color.parseColor("#333333"));

                holder.countIcon.setIcon(UIUtil.getGMD(mContext, GoogleMaterial.Icon.gmd_search,15,0,R.color.colorDarkGrey));

                holder.lastIcon.setIcon(UIUtil.getGMD(mContext, GoogleMaterial.Icon.gmd_alarm_on,15,0,R.color.colorDarkGrey));

                holder.timeIcon.setIcon(UIUtil.getGMD(mContext, GoogleMaterial.Icon.gmd_schedule,15,0,R.color.colorDarkGrey));
                holder.cardBackground.setBackgroundResource(R.drawable.gradient_normal_bg);
            }else{
                holder.cardBackground.setBackgroundResource(R.drawable.gradient_red_bg);
                holder.inspectionReady.setVisibility(View.GONE);

//                holder.inspectionTitle.setTextColor(R.color.colorPrimaryWhite);
//                holder.inspectionLastChecked.setTextColor(R.color.colorPrimaryWhite);
//                holder.inspectionItemsCount.setTextColor(R.color.colorPrimaryWhite);
                hoursRemaining = getHoursOverdue(supposeToCheckDate);
                dueInText = handleOverdueByText(hoursRemaining);
            }

        }

        holder.inspectionLastChecked.setText("last checked: " + lastChecked);
        holder.inspectionOverdue.setText(dueInText);

        //set item count
        String checkedCounts  = String.valueOf(currentInspection.getInspection_checked_count());
        String totalCounts  = String.valueOf(currentInspection.getInspection_items_count());
        holder.inspectionItemsCount.setText(checkedCounts+"/"+totalCounts + " items to inspect");

        //get department obj
        CollectionReference depColl = FirebaseUtil.getDepartmentsFromFirestore(mActivity);
        DocumentReference depDoc = depColl.document(currentInspection.getInspection_department());

        depDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Department department = document.toObject(Department.class);

                        holder.inspectionDepartment.setText(department.getDepartment_name());
                        holder.inspectionDepartment.setBackgroundColor(Color.parseColor(department.getDepartment_color_hex()));
                        holder.inspectionDepartment.setVisibility(View.VISIBLE);
                    } else {
                        holder.inspectionDepartment.setVisibility(View.GONE);
                    }
                }else{
                    holder.inspectionDepartment.setVisibility(View.GONE);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        if (inspectionsData.size() == 0){
            return 0;
        }else {
            return inspectionsData.size();
        }
    }

    // inner class to hold a reference to each item of RecyclerView
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView inspectionTitle;
        public TextView inspectionLastChecked;
        public TextView inspectionOverdue;
        public TextView inspectionReady;
        public TextView inspectionItemsCount;
        public TextView inspectionDepartment;
        public CardView cardBackground;
        public IconicsImageView timeIcon;
        public IconicsImageView lastIcon;
        public IconicsImageView countIcon;

        private final Context context;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            //populate viewholder data accordingly
            inspectionTitle = itemLayoutView.findViewById(R.id.title_inspection);
            inspectionLastChecked = itemLayoutView.findViewById(R.id.last_checked_inspection);
            inspectionReady =itemLayoutView.findViewById(R.id.ready_inspection);
            cardBackground = itemLayoutView.findViewById(R.id.card_view);
            inspectionItemsCount = itemLayoutView.findViewById(R.id.no_items_inspection);
            timeIcon = itemLayoutView.findViewById(R.id.inspection_time_icon);
            lastIcon = itemLayoutView.findViewById(R.id.inspection_checked_icon);
            inspectionOverdue = itemLayoutView.findViewById(R.id.overdue_inspection_text_view);
            countIcon = itemLayoutView.findViewById(R.id.count_icon);
            inspectionDepartment = itemLayoutView.findViewById(R.id.department_inspection);

            countIcon.setIcon(UIUtil.getGMD(mContext, GoogleMaterial.Icon.gmd_search,15,0,R.color.colorPrimaryWhite));

            lastIcon.setIcon(UIUtil.getGMD(mContext, GoogleMaterial.Icon.gmd_alarm_on,15,0,R.color.colorPrimaryWhite));


            timeIcon.setIcon(UIUtil.getGMD(mContext, GoogleMaterial.Icon.gmd_schedule,15,0,R.color.colorPrimaryWhite));

            itemLayoutView.setOnClickListener(this);
            context = itemLayoutView.getContext();
        }

        @Override
        public void onClick(View v) {

            int clickedPosition = getAdapterPosition();
//            mOnClickListener.onListItemClick(clickedPosition);
            Log.d("clicked", "tapped");
            v.setClickable(false);

            Inspection selectedInspection = inspectionsData.get(clickedPosition);

            // Serialization
            Gson gson = new Gson();
            String inspectionJson = gson.toJson(selectedInspection);

            //intent when clicked
            Intent intentInspectionDetail =  new Intent(context, InspectionDetailsActivity.class);
            intentInspectionDetail.putExtra(InspectionDetailsActivity.EXTRA_NAME , inspectionsData.get(clickedPosition).getInspection_name());
            intentInspectionDetail.putExtra("inspection", inspectionJson);

            Log.d("inspection data", inspectionsData.get(clickedPosition).getInspection_name());
            context.startActivity(intentInspectionDetail);
        }
    }

    //helper & formatting
    private String getStringDateFromDate(Date inputDate) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTime(inputDate);

        String date = DateFormat.format("dd-MMM-yyyy", cal).toString();
        return date;
    }

    private double getHoursRemaining(Date supposeToCheck){

        Date now = new Date();

        double diff = supposeToCheck.getTime() - now.getTime();
        double diffHours = diff / (60 * 60 * 1000);

        return diffHours;
    }

    private double getHoursOverdue(Date supposeToCheck){

        Date now = new Date();

        double diff = now.getTime() - supposeToCheck.getTime();
        double diffHours = diff / (60 * 60 * 1000);

        return diffHours;
    }

    private String handleDueInText(double hoursRemaining){

        String dueIn = "";

        if (hoursRemaining <= 1){

            double hour = Math.round(hoursRemaining);

            dueIn =  dueIn.format("%.0f", hour) + " hour";

        }else if(hoursRemaining < 24){

            double hours = Math.round(hoursRemaining);

            dueIn =  dueIn.format("%.0f", hours) + " hours";

        }else{

            double days = Math.round(hoursRemaining / 24);

            if (days == 1){
                dueIn = dueIn.format("%.0f", days) + " day";
            }else{
                dueIn = dueIn.format("%.0f", days) + " days";
            }
        }

        return "Due in: " + dueIn;
    }

    private String handleOverdueByText(double hoursOverdue){

        String overdueBy = "";

        if (hoursOverdue > 24){

            double days = Math.round(hoursOverdue / 24);

            if (days == 1){
                overdueBy = overdueBy.format("%.0f", days) + " day";
            }else{
                overdueBy = overdueBy.format("%.0f", days) + " days";
            }
        }else if(hoursOverdue >= 2){

            double hours = Math.round(hoursOverdue);

            overdueBy = overdueBy.format("%.0f", hours) + " hours";

        }else if (hoursOverdue >= 1){

           double hour = Math.round(hoursOverdue);

            overdueBy = overdueBy.format("%.0f", hour) + " hour";
        }

        return "Overdue by: " + overdueBy;
    }


}
