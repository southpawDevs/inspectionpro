package devs.southpaw.com.inspectionpro;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import objects.Inspection;
import objects.InspectionItem;

/**
 * Created by keith on 30/01/2018.
 */

public class RecyclerViewAdapterForItem extends RecyclerView.Adapter<RecyclerViewAdapterForItem.ViewHolder> {

    private List<InspectionItem> inspectionItemsData;

    final private RecyclerViewAdapterForItem.RecyclerItemClickListener mOnClickListener;

    private static int viewHolderCount;
    private int mNumberItems;

    public interface RecyclerItemClickListener {
        void onListItemClick(int clickedItemIndex);

    }

    public RecyclerViewAdapterForItem(List<InspectionItem> inspectionItemsData, RecyclerViewAdapterForItem.RecyclerItemClickListener mOnClickListener) {
        this.inspectionItemsData = inspectionItemsData;
        this.mOnClickListener = mOnClickListener;
    }

    @Override
    public RecyclerViewAdapterForItem.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_layout, parent, false);


        ViewHolder viewHolder = new ViewHolder(itemLayoutView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterForItem.ViewHolder holder, int position) {
        //set the title
        holder.inspectionTitle.setText(inspectionItemsData.get(position).getItemName());
        holder.inspectionDescription.setText(inspectionItemsData.get(position).getItemMethod());

        //handle status
        Log.d("item status", String.valueOf(inspectionItemsData.get(position).getItemStatus()));

        switch (inspectionItemsData.get(position).getItemStatus()){
            case 0:
                //new
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#eaeaea"));
                break;
            case 1:
                //on the way
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#F49A27"));

                break;
            case 2:
                //green
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#61f1a2"));
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (inspectionItemsData.size() == 0){
            return 0;
        }else {
            return inspectionItemsData.size();
        }
    }


    // inner class to hold a reference to each item of RecyclerView
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView inspectionTitle;
        public TextView inspectionDescription;
        //public TextView inspectionReady;
        public CardView cardStatus;

        private final Context context;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            //populate viewholder data accordingly
            inspectionTitle = itemLayoutView.findViewById(R.id.title_item);
            inspectionDescription = itemLayoutView.findViewById(R.id.description_item);

            cardStatus = itemLayoutView.findViewById(R.id.status_card_view);

            itemLayoutView.setOnClickListener(this);
            context = itemLayoutView.getContext();
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListItemClick(clickedPosition);
            Log.d("clicked", "tapped");

            InspectionItem selectedItem = inspectionItemsData.get(clickedPosition);

            // Serialization
            Gson gson = new Gson();
            String itemJson = gson.toJson(selectedItem);

            //intent when clicked
            Intent intentItemDetail =  new Intent(context, ItemDetailsActivity.class);
            intentItemDetail.putExtra("selected_item" , itemJson);

            //Log.d("merchant_name", inspectionItemsData.get(clickedPosition).getInspectionName());
            context.startActivity(intentItemDetail);
        }
    }
}
