package adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import devs.southpaw.com.inspectionpro.R;
import objects.Inspection;
import objects.InspectionItem;

/**
 * Created by keith on 05/03/2018.
 */

public class RecyclerViewAdapterForArchiveItems extends RecyclerView.Adapter<RecyclerViewAdapterForArchiveItems.ViewHolder> {

    private List<Inspection> inspectionArchiveData;

    final private RecyclerViewAdapterForArchiveItems.RecyclerItemClickListener mOnClickListener;

    public interface RecyclerItemClickListener {
        void onListItemClick(int clickedItemIndex);

    }

    public RecyclerViewAdapterForArchiveItems(List<Inspection> inspectionArchiveData) {
        this.inspectionArchiveData = inspectionArchiveData;
        mOnClickListener = null;
    }

    @Override
    public RecyclerViewAdapterForArchiveItems.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_archive_items_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterForArchiveItems.ViewHolder holder, int position) {

        Inspection currentInspection = inspectionArchiveData.get(position);

        String submittedDate = getStringDateFromDate(currentInspection.getInspection_submitted_at());
        String submittedBy = currentInspection.getInspection_submitted_by_name();

        holder.inspectionSubmittedAt.setText("Submitted at: " + submittedDate);
        holder.inspectionSubmittedBy.setText("Submitted by: " + submittedBy);

        List<InspectionItem> items = currentInspection.getInspection_items_object();

        int totalRed = 0;
        int totalGreen = 0;

        for (int l = 0; l < items.size(); l++){
            if (items.get(l).getItem_status() == 1){
                totalRed += 1;
            }else{
                totalGreen += 1;
            }
        }

        if (totalRed != 0) {
            holder.itemSummary.setText(totalRed + " action items were found.");
        }else{
            holder.itemSummary.setText("No action items were found. Inspection is safe");
        }
    }

    @Override
    public int getItemCount() {
        if (inspectionArchiveData.size() == 0){
            return 0;
        }else {
            return inspectionArchiveData.size();
        }
    }

    // inner class to hold a reference to each item of RecyclerView
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView itemSummary;
        public TextView inspectionSubmittedAt;
        public TextView inspectionSubmittedBy;
        public CardView cardBackground;

        private final Context context;


        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            //populate viewholder data accordingly
            itemSummary = itemLayoutView.findViewById(R.id.item_summary);
            cardBackground = itemLayoutView.findViewById(R.id.card_view_archive_items);
            inspectionSubmittedAt = itemLayoutView.findViewById(R.id.submitted_at_inspection_archive);
            inspectionSubmittedBy = itemLayoutView.findViewById(R.id.submitted_by_inspection_archive);

            itemLayoutView.setOnClickListener(this);
            context = itemLayoutView.getContext();
        }

        @Override
        public void onClick(View v) {


        }
    }

//    public void getInspectionNameToItemAdapter(String name, String id){
//        inspectionName = name;
//        inspectionID = id;
 //   }

    // Clean all elements of the recycler
    public void clear() {
        inspectionArchiveData.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Inspection> list) {
        inspectionArchiveData.addAll(list);
        notifyDataSetChanged();
    }

    //helper & formatting
    private String getStringDateFromDate(Date inputDate) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTime(inputDate);

        String date = DateFormat.format("dd-MMM-yyyy", cal).toString();
        return date;
    }
}
