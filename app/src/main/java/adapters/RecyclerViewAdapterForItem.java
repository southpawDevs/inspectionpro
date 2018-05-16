package adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.R;
import devs.southpaw.com.inspectionpro.UIUtil;
import layout.ItemDetailsActivity;
import objects.Department;
import objects.InspectionItem;

/**
 * Created by keith on 30/01/2018.
 */

public class RecyclerViewAdapterForItem extends RecyclerView.Adapter<RecyclerViewAdapterForItem.ViewHolder> {

    private List<InspectionItem> inspectionItemsData;
    String inspectionName;
    String inspectionID;
    String inspectionDepartmentID;
    Department departmentObject;

    final private RecyclerViewAdapterForItem.RecyclerItemClickListener mOnClickListener;
    private Context mContext;
    private Activity mActivity;

    private static int viewHolderCount;
    private int mNumberItems;

    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();
    private int REQUEST_CODE = 1880;
    String mCurrentPhotoPath;

    private String pid = "";

    public RecyclerViewAdapterForItem(List<InspectionItem> itemsData, Context context, Activity activity, String pid2) {
        this.inspectionItemsData = itemsData;
        mOnClickListener = null;
        mContext = context;
        mActivity = activity;
        //departmentObject = department;
        pid = pid2;
    }

    public interface RecyclerItemClickListener {
        void onListItemClick(int clickedItemIndex);

    }


    @Override
    public RecyclerViewAdapterForItem.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_layout, parent, false);

        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_item_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(itemLayoutView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterForItem.ViewHolder holder, int position) {

        InspectionItem dataObject = inspectionItemsData.get(position);

        //set the title
        holder.itemTitle.setText(dataObject.getItem_name());
        holder.itemDescription.setText(dataObject.getItem_description());
        holder.itemMethod.setText(dataObject.getItem_method());
        //handle status
        Log.d("item status", String.valueOf(dataObject.getItem_status()));

        switch (dataObject.getItem_status()){
            case 0:
                //new
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#eaeaea"));
                break;
            case 1:
                //red alert
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#f8f72d35"));
                break;
            case 2:
                //green
                holder.cardStatus.setCardBackgroundColor(Color.parseColor("#61f1a2"));
                break;
        }

        if (dataObject.getItem_condition_photo() == null){
            IconicsDrawable placeholder = UIUtil.getGMD(mContext,GoogleMaterial.Icon.gmd_wallpaper, 50,14, R.color.colorPrimaryWhite);
            holder.itemConditionImage.setImageDrawable(placeholder);
        }else{
            String urlImage = dataObject.getItem_condition_photo();
            Glide.with(mContext).load(urlImage).override(100,80).centerCrop().into(holder.itemConditionImage);
        }

        if (dataObject.getItem_comments() == null){
            holder.itemComments.setVisibility(View.GONE);
        }else{
            //hide comments for now
            holder.itemComments.setVisibility(View.GONE);
            holder.itemComments.setText(dataObject.getItem_comments());
        }

//        if(TextUtils.isEmpty(departmentObject.getDepartment_name())){
            holder.departmentTV.setVisibility(View.GONE);
//        }else{
//
//            holder.departmentCV.setCardBackgroundColor(Color.parseColor(departmentObject.getDepartment_color_hex()));
//            holder.departmentTV.setVisibility(View.VISIBLE);
//            holder.departmentTV.setText(departmentObject.getDepartment_name());
//        }

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
        public TextView itemMethod;
        public ImageView itemConditionImage;
        public CardView cardStatus;
        public TextView departmentTV;
        public CardView departmentCV;
        public SwipeRevealLayout swipeRevealLayout;
        private View frontLayout;
        private View doneLayout;
        private IconicsImageView doneIV;
        private IconicsImageView snapshotIcon;
        private IconicsImageView methodIcon;

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
            itemMethod = itemLayoutView.findViewById(R.id.method_text_view);
            itemConditionImage = itemLayoutView.findViewById(R.id.condition_image_view);
            doneIV = itemLayoutView.findViewById(R.id.done_image_view);
            cardStatus = itemLayoutView.findViewById(R.id.status_card_view);
            methodIcon = itemLayoutView.findViewById(R.id.items_method_icon);
            snapshotIcon = itemLayoutView.findViewById(R.id.snapshot_image_view);
            departmentTV = itemLayoutView.findViewById(R.id.department_text_view);
            departmentCV = itemLayoutView.findViewById(R.id.department_card_view);

            methodIcon.setIcon(UIUtil.getGMD(mContext, GoogleMaterial.Icon.gmd_build, 20,2, R.color.colorDarkGrey));

            IconicsDrawable drawable = new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_done)
                    .color(Color.WHITE)
                    .sizeDp(24);
            snapshotIcon.setIcon(UIUtil.getGMD(mContext, GoogleMaterial.Icon.gmd_camera, 24,0,R.color.colorPrimaryWhite));

            doneIV.setIcon(drawable);
            itemLayoutView.setOnClickListener(this);
            context = itemLayoutView.getContext();

            //add listener for quick done
            doneIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    InspectionItem selectedItem = inspectionItemsData.get(position);

                    String id = selectedItem.getItem_id();

                    if (selectedItem.getItem_status() != 2) {
                        updateDoneStatusToFirebase(id, 2);
                    }

                    viewBinderHelper.closeLayout(id);
                }
            });

            //add listener for quick done
            snapshotIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    InspectionItem selectedItem = inspectionItemsData.get(position);

                    String id = selectedItem.getItem_id();

                    dispatchTakePictureIntent();

                    String item_id = selectedItem.getItem_id();
                    Intent intent = new Intent("snapshot_id");
                    intent.putExtra("photo_path", mCurrentPhotoPath);
                    intent.putExtra("item_id",item_id);
                    intent.putExtra("item_name",selectedItem.getItem_name());
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                    viewBinderHelper.closeLayout(id);
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

                    int inspectedCount = 0;
                    for (int l=0; l<inspectionItemsData.size();l++){
                        if (inspectionItemsData.get(l).getItem_status() == 0){
                            inspectedCount += 1;
                        }
                    }

                    //intent when clicked
                    Intent intentItemDetail =  new Intent(context, ItemDetailsActivity.class);
                    intentItemDetail.putExtra("selected_item" , itemJson);
                    intentItemDetail.putExtra("inspection_name" , inspectionName);
                    intentItemDetail.putExtra("inspection_id" , inspectionID);
                    intentItemDetail.putExtra("inspected_count" , inspectedCount);
                    intentItemDetail.putExtra("total_items_count" , inspectionItemsData.size());
                    context.startActivity(intentItemDetail);
                }
            });
        }

        @Override
        public void onClick(View v) {
           //click func are done above on front layout
        }
    }

    public void getInspectionNameToItemAdapter(String name, String id, String department){
        inspectionName = name;
        inspectionID = id;
        inspectionDepartmentID = department;
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
        DocumentReference devHousePropertyDoc = db.collection("properties").document(pid);
        final CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
        final CollectionReference itemsColl = inspectionsColl.document(inspectionID).collection("items");

        //update firestore
        itemsColl.document(item_id).update("item_status", status)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        int pendingCount = 0;
                        for (int l=0; l<inspectionItemsData.size();l++){
                            if (inspectionItemsData.get(l).getItem_status() != 0){
                                pendingCount += 1;
                            }
                        }

                        inspectionsColl.document(inspectionID).update(
                                "inspection_pending_count", pendingCount
                        );

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

    //image snapshot function


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(mActivity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("Camera", "error creating file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(mContext,
                        "southpaw.dev.inspectionpro.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                mActivity.startActivityForResult(takePictureIntent, REQUEST_CODE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "ITEMJPEG" + timeStamp + "_";
        File storageDir = mActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
