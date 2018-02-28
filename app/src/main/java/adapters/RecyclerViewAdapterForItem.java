package adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

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

    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

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

        //View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_layout, parent, false);

        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_row_menu, parent, false);

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

            String urlImage = inspectionItemsData.get(position).getItem_condition_photo();
            Glide.with(mContext).load(urlImage).centerCrop().into(holder.itemConditionImage);
        }

        if (inspectionItemsData.get(position).getItem_comments() == null){
            holder.itemComments.setVisibility(View.GONE);
        }else{
            holder.itemComments.setVisibility(View.VISIBLE);
            holder.itemComments.setText(inspectionItemsData.get(position).getItem_comments());
        }


        InspectionItem dataObject = inspectionItemsData.get(position);
        viewBinderHelper.bind(holder.swipeRevealLayout, dataObject.getItem_id());

    }

    @Override
    public int getItemCount() {
        if (inspectionItemsData.size() == 0){
            return 0;
        }else {
            return inspectionItemsData.size();
        }
    }

    public void saveStates(Bundle outState) {
        viewBinderHelper.saveStates(outState);
    }

    public void restoreStates(Bundle inState) {
        viewBinderHelper.restoreStates(inState);
    }

    // inner class to hold a reference to each item of RecyclerView
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView itemTitle;
        public TextView itemDescription;
        public ImageView itemConditionImage;
        public CardView cardStatus;
        public SwipeRevealLayout swipeRevealLayout;
        private View frontLayout;
        private View doneLayout;
        private IconicsImageView doneIV;

        private final Context context;

        public TextView itemComments;

        public ViewHolder(final View itemLayoutView) {
            super(itemLayoutView);

            //populate viewholder data accordingly
            swipeRevealLayout = itemLayoutView.findViewById(R.id.swipe_layout);
            frontLayout = itemLayoutView.findViewById(R.id.front_layout);
            doneLayout = itemLayoutView.findViewById(R.id.done_layout);
            itemTitle = itemLayoutView.findViewById(R.id.title_item);
            itemDescription = itemLayoutView.findViewById(R.id.description_item);
            itemComments = itemLayoutView.findViewById(R.id.comments_text_View);
            itemConditionImage = itemLayoutView.findViewById(R.id.condition_image_view);
            doneIV = itemLayoutView.findViewById(R.id.done_image_view);
            cardStatus = itemLayoutView.findViewById(R.id.status_card_view);

            IconicsDrawable drawable = new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_done)
                    .color(Color.WHITE)
                    .sizeDp(24);

            doneIV.setIcon(drawable);
            itemLayoutView.setOnClickListener(this);
            context = itemLayoutView.getContext();

            //add listener for quick done
            doneLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String id = inspectionItemsData.get(getAdapterPosition()).getItem_id();
                    updateDoneStatusToFirebase(id, 2);
                }
            });

            //add listener for front view because of the swipe layout library
            frontLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemLayoutView.setClickable(false);
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

                    context.startActivity(intentItemDetail);
                }
            });
        }

        @Override
        public void onClick(View v) {
           //click func are done above on front layout
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

    private void updateDoneStatusToFirebase(final String item_id, int status){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");
        CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
        final CollectionReference itemsColl = inspectionsColl.document(inspectionID).collection("items");

        //update firestore
        itemsColl.document(item_id).update("item_status", status)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(mContext, "status updated", Toast.LENGTH_SHORT).show();
                        viewBinderHelper.closeLayout(item_id);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(mContext, "fail to update status", Toast.LENGTH_SHORT).show();
                        viewBinderHelper.closeLayout(item_id);
                    }
                });
    }
}
