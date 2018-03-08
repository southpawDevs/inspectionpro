package adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;

import java.lang.annotation.Target;
import java.util.List;

import javax.sql.DataSource;

import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.UIUtil;
import devs.southpaw.com.inspectionpro.R;
import layout.ItemDetailsActivity;
import objects.ActionItems;
import objects.InspectionItem;

/**
 * Created by keith on 07/03/2018.
 */

public class RecyclerViewAdapterForActionItems extends RecyclerView.Adapter<RecyclerViewAdapterForActionItems.ViewHolder> {

    private List<ActionItems> actionItemsData;
    final private RecyclerViewAdapterForItem.RecyclerItemClickListener mOnClickListener;
    private Context mContext;
    private Activity mActivity;

    public interface RecyclerItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    public RecyclerViewAdapterForActionItems(List<ActionItems> itemsData, Context context, Activity activity) {
        this.actionItemsData = itemsData;
        mOnClickListener = null;
        mContext = context;
        mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_action_items_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        ActionItems selecteditem = actionItemsData.get(position);
        holder.titleActionItem.setText(selecteditem.getItem_name());
        holder.inspectionCategory.setText("Inspection: " + selecteditem.getInspection_name());

        String reportedDate = UIUtil.getStringDateFromDate(selecteditem.getItem_reported_at());
        holder.reportedAt.setText("Reported At: " + reportedDate);
        holder.reportedBy.setText("Reported By: " + selecteditem.getItem_reported_by());

        StorageReference pathReference = FirebaseUtil.getStorageRef(mActivity);
        StorageReference imageLocation = pathReference.child("images/temp_" + selecteditem.getItem_existing_id());

        holder.descriptionActionItem.setText(selecteditem.getItem_report_description());

        holder.actionImageView.setVisibility(View.GONE);
        holder.progressBar.setVisibility(View.VISIBLE);

        imageLocation.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                String urlImage = uri.toString();

                holder.actionImageView.setVisibility(View.VISIBLE);
                holder.lp.setMargins(10,0,0,0);

                Glide.with(mContext).load(urlImage).override(1000,700).fitCenter().listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.actionImageView.setVisibility(View.GONE);
                        holder.progressBar.setVisibility(View.GONE);

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        holder.actionImageView.setVisibility(View.VISIBLE);
                        holder.progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(holder.actionImageView);
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                holder.progressBar.setVisibility(View.GONE);
                holder.actionImageView.setVisibility(View.GONE);
                holder.lp.setMargins(0,0,0,0);
            }
        });


    }

    @Override
    public int getItemCount() {
        if (actionItemsData.size() == 0){
            return 0;
        }else {
            return actionItemsData.size();
        }
    }

    // inner class to hold a reference to each item of RecyclerView
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView titleActionItem;
        public TextView descriptionActionItem;
        public TextView reportedAt;
        public TextView reportedBy;
        public TextView inspectionCategory;
        public CardView cardBackground;
        public ImageView actionImageView;
        public LinearLayout.LayoutParams lp;
        public ProgressBar progressBar;

        private final Context context;

        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            //populate viewholder data accordingly
            titleActionItem = itemLayoutView.findViewById(R.id.title_action_item_tv);
            cardBackground = itemLayoutView.findViewById(R.id.card_view_action_items);
            reportedAt = itemLayoutView.findViewById(R.id.reported_at_tv);
            reportedBy = itemLayoutView.findViewById(R.id.reported_by_tv);
            inspectionCategory = itemLayoutView.findViewById(R.id.inspection_category_tv);
            actionImageView = itemLayoutView.findViewById(R.id.action_items_image_view);
            descriptionActionItem = itemLayoutView.findViewById(R.id.description_action_item_tv);
            progressBar = itemLayoutView.findViewById(R.id.actionItemProgressBar);

            lp = (LinearLayout.LayoutParams) descriptionActionItem.getLayoutParams();

            itemLayoutView.setOnClickListener(this);
            context = itemLayoutView.getContext();
        }

        @Override
        public void onClick(View v) {

            final ActionItems ai = actionItemsData.get(getAdapterPosition());

            DocumentReference itemRef = FirebaseUtil.getInspectionItem(mActivity, ai.getInspection_id(), ai.getItem_existing_id());

            itemRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.getResult() != null){
                        InspectionItem item = task.getResult().toObject(InspectionItem.class);

                        // Serialization
                        Gson gson = new Gson();
                        String itemJson = gson.toJson(item);

                        Intent intentItemDetail =  new Intent(context, ItemDetailsActivity.class);
                        intentItemDetail.putExtra("inspection_name" , ai.getInspection_name());
                        intentItemDetail.putExtra("inspection_id" , ai.getInspection_id());
                        intentItemDetail.putExtra("selected_item" , itemJson);
                        context.startActivity(intentItemDetail);
                    }else{
                        Toast.makeText(mContext, "Fail to retrieve item", Toast.LENGTH_SHORT).show();
                    }
                }
            });



        }
    }
//    public void getInspectionNameToItemAdapter(String name, String id){
//        inspectionName = name;
//        inspectionID = id;
    //   }

    // Clean all elements of the recycler
    public void clear() {
        actionItemsData.clear();
        notifyDataSetChanged();
    }
}
