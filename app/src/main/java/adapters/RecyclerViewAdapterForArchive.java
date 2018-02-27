package adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import devs.southpaw.com.inspectionpro.R;
import layout.InspectionDetailsActivity;
import objects.Inspection;

/**
 * Created by keith on 27/02/2018.
 */

public class RecyclerViewAdapterForArchive extends RecyclerView.Adapter<RecyclerViewAdapterForInspection.ViewHolder>{


    @Override
    public RecyclerViewAdapterForInspection.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterForInspection.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    // inner class to hold a reference to each item of RecyclerView
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView inspectionTitle;
        public TextView inspectionLastChecked;
        public TextView inspectionReady;
        public TextView inspectionItemsCount;
        public CardView cardBackground;

        private final Context context;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            //populate viewholder data accordingly
            inspectionTitle = itemLayoutView.findViewById(R.id.title_inspection);
            inspectionLastChecked = itemLayoutView.findViewById(R.id.last_checked_inspection);
            inspectionReady =itemLayoutView.findViewById(R.id.ready_inspection);
            cardBackground = itemLayoutView.findViewById(R.id.card_view);
            inspectionItemsCount = itemLayoutView.findViewById(R.id.no_items_inspection);

            itemLayoutView.setOnClickListener(this);
            context = itemLayoutView.getContext();
        }

        @Override
        public void onClick(View v) {


        }
    }

}
