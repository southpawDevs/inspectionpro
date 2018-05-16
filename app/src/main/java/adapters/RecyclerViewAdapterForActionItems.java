package adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import java.lang.annotation.Target;
import java.util.List;

import javax.sql.DataSource;

import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.UIUtil;
import devs.southpaw.com.inspectionpro.R;
import devs.southpaw.com.inspectionpro.actionItemsLayout.ActionItemsDetailActivity;
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

    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

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

        holder.reportIcon.setIcon(UIUtil.getGMD(mContext, GoogleMaterial.Icon.gmd_assignment,20,0,R.color.colorPrimaryWhite));

        String reportedDate = UIUtil.getStringDateFromDate(selecteditem.getItem_reported_at());
        holder.reportedAt.setText("Reported At: " + reportedDate);
        holder.reportedBy.setText("Reported By: " + selecteditem.getItem_reported_by());
        holder.reportedBy.setVisibility(View.GONE);

        StorageReference pathReference = FirebaseUtil.getStorageRef(mActivity);
        StorageReference imageLocation = pathReference.child("images/temp_" + selecteditem.getItem_existing_id());

        holder.descriptionActionItem.setText(selecteditem.getItem_report_description());

        holder.aiImageCard.setVisibility(View.GONE);
        holder.actionImageView.setVisibility(View.GONE);
        holder.progressBar.setVisibility(View.VISIBLE);

        imageLocation.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                String urlImage = uri.toString();

                holder.aiImageCard.setVisibility(View.VISIBLE);
                holder.actionImageView.setVisibility(View.VISIBLE);
                //holder.lp.setMargins(15,0,0,0);

               // holder.lp.addRule(RelativeLayout.END_OF, R.id.actionItemProgressBar);
              //  holder.descriptionActionItem.setLayoutParams(holder.lp);

                Glide.with(mContext).load(urlImage).override(500,300).centerCrop().listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFirstResource) {
                        holder.aiImageCard.setVisibility(View.GONE);
                        holder.actionImageView.setVisibility(View.GONE);
                        holder.progressBar.setVisibility(View.GONE);

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                       // holder.lp.addRule(RelativeLayout.END_OF, R.id.action_items_image_view);
                       // holder.descriptionActionItem.setLayoutParams(holder.lp);
                        holder.aiImageCard.setVisibility(View.VISIBLE);
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
                holder.aiImageCard.setVisibility(View.GONE);
                holder.actionImageView.setVisibility(View.GONE);
                //holder.lp.setMargins(0,0,0,0);
            }
        });

        ActionItems dataObject = actionItemsData.get(position);
        viewBinderHelper.bind(holder.swipeRevealLayout, dataObject.getItem_existing_id());
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
        public CardView aiImageCard;
        public ImageView actionImageView;
        public RelativeLayout.LayoutParams lp;
        public ProgressBar progressBar;
        private View frontLayout;
        private View doneLayout;
        public SwipeRevealLayout swipeRevealLayout;
        private final Context context;
        public IconicsImageView reportIcon;

        private IconicsImageView doneIV;

        public ViewHolder(final View itemLayoutView) {
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
            frontLayout = itemLayoutView.findViewById(R.id.front_layout_ai);
            doneLayout = itemLayoutView.findViewById(R.id.done_layout_ai);
           // lp = (RelativeLayout.LayoutParams) descriptionActionItem.getLayoutParams();
            swipeRevealLayout = itemLayoutView.findViewById(R.id.swipe_action_item_layout);
            aiImageCard = itemLayoutView.findViewById(R.id.ai_image_card_view);
            doneIV = itemLayoutView.findViewById(R.id.done_image_view);
            reportIcon = itemLayoutView.findViewById(R.id.report_icon);

            itemLayoutView.setOnClickListener(this);
            context = itemLayoutView.getContext();

            swipeRevealLayout.setEnabled(false);

//            IconicsDrawable drawableReport = new IconicsDrawable(mContext)
//                    .icon(GoogleMaterial.Icon.gmd_assignment)
//                    .color(itemLayoutView.getResources().getColor(R.color.colorPrimaryWhite))
//                    .sizeDp(20);
//
//            reportIcon.setIcon(drawableReport);
            //reportIcon.setVisibility(View.GONE);

            IconicsDrawable drawable = new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_done)
                    .color(Color.WHITE)
                    .sizeDp(24);
            doneIV.setIcon(drawable);

            //add listener for quick done
            doneLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
//                    InspectionItem selectedItem = inspectionItemsData.get(position);
//
//                    String id = selectedItem.getItem_id();
//
//                    if (selectedItem.getItem_status() != 2) {
//                        updateDoneStatusToFirebase(id, 2);
//                    }
//
//                    viewBinderHelper.closeLayout(id);
                }
            });

            frontLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemLayoutView.setClickable(false);

                    final ActionItems ai = actionItemsData.get(getAdapterPosition());

                    // Serialization
                    Gson gson = new Gson();
                    String itemJson = gson.toJson(ai);

                    Intent intentItemDetail = new Intent(context, ActionItemsDetailActivity.class);
                    intentItemDetail.putExtra("inspection_name", ai.getInspection_name());
                    intentItemDetail.putExtra("inspection_id", ai.getInspection_id());
                    intentItemDetail.putExtra("ai", itemJson);
                    context.startActivity(intentItemDetail);
                }
            });
        }

        @Override
        public void onClick(View v) {

            final ActionItems ai = actionItemsData.get(getAdapterPosition());

            // Serialization
            Gson gson = new Gson();
            String itemJson = gson.toJson(ai);

            Intent intentItemDetail = new Intent(context, ActionItemsDetailActivity.class);
            intentItemDetail.putExtra("inspection_name", ai.getInspection_name());
            intentItemDetail.putExtra("inspection_id", ai.getInspection_id());
            intentItemDetail.putExtra("ai", itemJson);
            context.startActivity(intentItemDetail);
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
