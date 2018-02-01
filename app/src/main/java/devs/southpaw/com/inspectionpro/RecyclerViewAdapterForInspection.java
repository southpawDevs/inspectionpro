package devs.southpaw.com.inspectionpro;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import objects.Inspection;

/**
 * Created by keith on 30/01/2018.
 */

public class RecyclerViewAdapterForInspection extends RecyclerView.Adapter<RecyclerViewAdapterForInspection.ViewHolder> {

    private List<Inspection> inspectionsData;

    private static int viewHolderCount;
    private int mNumberItems;

    final private RecyclerItemClickListener mOnClickListener;

    public interface RecyclerItemClickListener {
        void onListItemClick(int clickedItemIndex);

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

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(RecyclerViewAdapterForInspection.ViewHolder holder, int position) {

        //set the title
        holder.inspectionTitle.setText(inspectionsData.get(position).getInspectionName());

        //set last modified
        String dateString = getDate(inspectionsData.get(position).getInspectionModifiedAt());
        holder.inspectionLastChecked.setText("Last checked: " + dateString);

        //set card background color
        switch (inspectionsData.get(position).getInspectionStatus()){
            case 0:
                //new
                holder.cardBackground.setCardBackgroundColor(Color.parseColor("#eaeaea"));
                holder.inspectionReady.setVisibility(View.GONE);
                break;
            case 1:
                //on the way
                holder.cardBackground.setCardBackgroundColor(Color.parseColor("#F49A27"));
                holder.inspectionReady.setVisibility(View.GONE);
                break;
            case 2:
                //green
                holder.cardBackground.setCardBackgroundColor(Color.parseColor("#61f1a2"));
                holder.inspectionReady.setVisibility(View.VISIBLE);
                holder.inspectionReady.setText("Ready To Submit");
                break;
        }

        //set inspection status details
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
        public TextView inspectionReady;
        public CardView cardBackground;

        private final Context context;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            //populate viewholder data accordingly
            inspectionTitle = itemLayoutView.findViewById(R.id.title_inspection);
            inspectionLastChecked = itemLayoutView.findViewById(R.id.last_checked_inspection);
            inspectionReady =itemLayoutView.findViewById(R.id.ready_inspection);
            cardBackground = itemLayoutView.findViewById(R.id.card_view);

            itemLayoutView.setOnClickListener(this);
            context = itemLayoutView.getContext();
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
            Log.d("clicked", "tapped");

            Inspection selectedInspection = inspectionsData.get(clickedPosition);

            // Serialization
            Gson gson = new Gson();
            String inspectionJson = gson.toJson(selectedInspection);

            //intent when clicked
            Intent intentShopDetail =  new Intent(context, InspectionDetailsActivity.class);
            intentShopDetail.putExtra(InspectionDetailsActivity.EXTRA_NAME , inspectionsData.get(clickedPosition).getInspectionName());
            intentShopDetail.putExtra("inspection", inspectionJson);

            Log.d("merchant_name", inspectionsData.get(clickedPosition).getInspectionName());
            context.startActivity(intentShopDetail);
        }
    }

    //helper & formatting
    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time * 1000);
        String date = DateFormat.format("dd-MMM-yyyy", cal).toString();
        return date;
    }

}
