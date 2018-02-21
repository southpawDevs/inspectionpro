package adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.List;

import devs.southpaw.com.inspectionpro.R;
import layout.ItemDetailsActivity;
import objects.InspectionItem;

/**
 * Created by keith on 30/01/2018.
 */

public class RecyclerViewAdapterForItem extends RecyclerView.Adapter<RecyclerViewAdapterForItem.ViewHolder> {

    private List<InspectionItem> inspectionItemsData;
    String inspectionName;
    String inspectionID;

    final private RecyclerViewAdapterForItem.RecyclerItemClickListener mOnClickListener;
    private Context mContext;

    private static int viewHolderCount;
    private int mNumberItems;

    public RecyclerViewAdapterForItem(List<InspectionItem> itemsData, Context context) {
        this.inspectionItemsData = itemsData;
        mOnClickListener = null;
        mContext = context;
    }

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
        holder.itemTitle.setText(inspectionItemsData.get(position).getItem_name());
        holder.itemDescription.setText(inspectionItemsData.get(position).getItem_method());

        //handle status
        Log.d("item status", String.valueOf(inspectionItemsData.get(position).getItem_status()));

        switch (inspectionItemsData.get(position).getItem_status()){
            case 0:
                //new
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#eaeaea"));
                break;
            case 1:
                //alert
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#fa4048"));

                break;
            case 2:
                //green
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#61f1a2"));
                break;
        }

        if (inspectionItemsData.get(position).getItem_condition_photo() == null){
            holder.itemConditionImage.setVisibility(View.GONE);
        }else{
            holder.itemConditionImage.setVisibility(View.VISIBLE);
            ImageView iv = holder.itemConditionImage;
            String urlImage = inspectionItemsData.get(position).getItem_condition_photo();
            Picasso.with(mContext).load(urlImage).into(iv);
        }

        if (inspectionItemsData.get(position).getItem_comments() == null){
            holder.itemComments.setVisibility(View.GONE);
        }else{
            holder.itemComments.setVisibility(View.VISIBLE);
            holder.itemComments.setText(inspectionItemsData.get(position).getItem_comments());
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

        public TextView itemTitle;
        public TextView itemDescription;
        public ImageView itemConditionImage;
        public CardView cardStatus;

        private final Context context;

        public TextView itemComments;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            //populate viewholder data accordingly
            itemTitle = itemLayoutView.findViewById(R.id.title_item);
            itemDescription = itemLayoutView.findViewById(R.id.description_item);
            itemComments = itemLayoutView.findViewById(R.id.comments_text_View);
            itemConditionImage = itemLayoutView.findViewById(R.id.condition_image_view);

            cardStatus = itemLayoutView.findViewById(R.id.status_card_view);

            itemLayoutView.setOnClickListener(this);
            context = itemLayoutView.getContext();
        }

        @Override
        public void onClick(View v) {
            int clickedPosition = getAdapterPosition();
            //mOnClickListener.onListItemClick(clickedPosition);
            Log.d("clicked", "tapped");

            InspectionItem selectedItem = inspectionItemsData.get(clickedPosition);

            // Serialization
            Gson gson = new Gson();
            String itemJson = gson.toJson(selectedItem);

            //intent when clicked
            Intent intentItemDetail =  new Intent(context, ItemDetailsActivity.class);
            intentItemDetail.putExtra("selected_item" , itemJson);
            intentItemDetail.putExtra("inspection_name" , inspectionName);
            intentItemDetail.putExtra("inspection_id" , inspectionID);

            //Log.d("merchant_name", inspectionItemsData.get(clickedPosition).getInspectionName());
            context.startActivity(intentItemDetail);
        }
    }

    public void getInspectionNameToItemAdapter(String name, String id){
        inspectionName = name;
        inspectionID = id;
    }

    // Clean all elements of the recycler
    public void clear() {
        inspectionItemsData.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<InspectionItem> list) {
        inspectionItemsData.addAll(list);
        notifyDataSetChanged();
    }
}
