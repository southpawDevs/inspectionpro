package layout;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.R;
import adapters.RecyclerViewAdapterForItem;
import devs.southpaw.com.inspectionpro.SharedPrefUtil;
import objects.ActionItems;
import objects.Department;
import objects.Inspection;
import objects.InspectionItem;

public class InspectionDetailsActivity extends AppCompatActivity implements RecyclerViewAdapterForItem.RecyclerItemClickListener {

    public static final String EXTRA_NAME = "no_name";
    public Inspection selectedInspection;

    private List<InspectionItem> itemsData = new ArrayList<>();

    private LinearLayoutManager mLayoutManager;
    private RecyclerViewAdapterForItem itemsAdapter;
    private SwipeRefreshLayout refreshContainer;
    private Button submitInspectionButton;
    RecyclerView recyclerView;
    TextView completedCount;

    private MaterialDialog mProgressDialog;
    private MaterialDialog mDeterminateDialog;

    Boolean refreshing = false;

    private int currentItemsCount;
    private int completedItemsCount = 0;

    private String selectedItemId;
    private String photoPath;

    private String pid = "";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference devHousePropertyDoc;

    private Activity mActivity = this;

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            photoPath = intent.getStringExtra("photo_path");
            selectedItemId = intent.getStringExtra("item_id");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_details);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("snapshot_id"));

        pid = SharedPrefUtil.getPropertyID(this);
        devHousePropertyDoc = db.collection("properties").document(pid);

        completedCount = (TextView) findViewById(R.id.count_text_view);
        submitInspectionButton = (Button) findViewById(R.id.submit_inspection_button);

        mProgressDialog = new MaterialDialog.Builder(mActivity)
                .title("Uploading to archive")
                .content("please wait...")
                .progress(true, 0)
                .show();
        mProgressDialog.dismiss();

        //handle pull refreshing container
        refreshContainer = (SwipeRefreshLayout) findViewById(R.id.refresh_container_detail);
        refreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (itemsAdapter != null){
                    itemsAdapter.clear();
                }
                refreshing = true;
                getItemsDataFromFireStore(true);
            }
        });

        refreshContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //swipe to delete
//        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//            @Override
//            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
//                return false;
//            }
//
//            // [...]
//            @Override
//            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
//                //Remove swiped item from list and notify the RecyclerView
//            }
//        };
//
//        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_items);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(mLayoutManager);

        //itemTouchHelper.attachToRecyclerView(recyclerView);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Deserialization
        String inspectionJson= getIntent().getStringExtra("inspection");
        Log.d("inspectionJson", inspectionJson);

        Gson gson = new Gson();
        selectedInspection = gson.fromJson(inspectionJson, Inspection.class);

        //set title in action bar after deserializing data
        actionBar.setTitle(selectedInspection.getInspection_name());

        submitInspectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                submitInspection(completedItemsCount, currentItemsCount);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Boolean admin = SharedPrefUtil.getAdminRights(this);
        if(admin == true) {
            getMenuInflater().inflate(R.menu.items_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1880:
                if(resultCode == Activity.RESULT_OK) {
                    updateDoneStatusToFirebase(selectedItemId,2);
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (itemsAdapter != null){
            itemsAdapter.clear();
        }
        getItemsDataFromFireStore(true);
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {

    }

    //handle back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            // action with ID action_refresh was selected
            case R.id.inspection_item_create:
                Intent createIntent = new Intent(getApplicationContext(), InspectionItemAddActivity.class);
                createIntent.putExtra("inspection_id", selectedInspection.getInspection_id());

                ArrayList<String> ids = new ArrayList<>();
                for(int l=0; l<=itemsData.size() - 1; l++){
                    ids.add(itemsData.get(l).getItem_id());
                }

                createIntent.putExtra("inspection_items", ids);
                startActivity(createIntent);
                break;

            case R.id.inspection_delete:

                //delete inspection here
                final CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
                deleteInspection(inspectionsColl, selectedInspection.getInspection_id());

                break;

            case android.R.id.home:
                Log.d("clicked", "action bar clicked");
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void handleItemCompletion(List<InspectionItem> items){

        int itemsCompletedCount = 0;
        for(int l=0; l<items.size(); l++){

            if (items.get(l).getItem_status() != 0){
                itemsCompletedCount += 1;
            }//end if
        }//end loop

        completedCount.setText(String.valueOf(itemsCompletedCount) + "/" + String.valueOf(items.size()) + " inspected");
        completedItemsCount = itemsCompletedCount;
        currentItemsCount = items.size();
    }

    private void getItemsDataFromFireStore(final Boolean isRefreshing){

        refreshContainer.setRefreshing(true);

        final String FireStoreTAG = "firestoreTag";
        final String selectedInspectionID = selectedInspection.getInspection_id();

        final CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
        CollectionReference itemsColl = inspectionsColl.document(selectedInspectionID).collection("items");

        final List<InspectionItem> items = new ArrayList<>();

        itemsColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {

                        if (document != null) {
                            InspectionItem item = document.toObject(InspectionItem.class);
                            itemsData.add(item);
                        } else {
                            Log.d(FireStoreTAG, "No such document");
                            Toast.makeText(getBaseContext(),"Fail to retrieve", Toast.LENGTH_SHORT).show();
                        }
                    }

                } else {
                    Log.d(FireStoreTAG, "Error getting documents: ", task.getException());
                    Toast.makeText(getBaseContext(),"Couldn't refresh", Toast.LENGTH_SHORT).show();
                }

                //get department obj
                CollectionReference depColl = FirebaseUtil.getDepartmentsFromFirestore(mActivity);
                DocumentReference depDoc = depColl.document(selectedInspection.getInspection_department());

                depDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Department department = document.toObject(Department.class);

                                //handle recycler view
                                itemsAdapter = new RecyclerViewAdapterForItem(itemsData, getBaseContext(),mActivity, department, pid);
                                itemsAdapter.getInspectionNameToItemAdapter(selectedInspection.getInspection_name(), selectedInspectionID, selectedInspection.getInspection_department());
                                recyclerView.setAdapter(itemsAdapter);
                                recyclerView.setItemAnimator(new DefaultItemAnimator());


                            } else {
                                Department department = new Department();
//handle recycler view
                                itemsAdapter = new RecyclerViewAdapterForItem(itemsData, getBaseContext(),mActivity, department,pid);
                                itemsAdapter.getInspectionNameToItemAdapter(selectedInspection.getInspection_name(), selectedInspectionID, selectedInspection.getInspection_department());
                                recyclerView.setAdapter(itemsAdapter);
                                recyclerView.setItemAnimator(new DefaultItemAnimator());
                            }
                        }else{
                            Department department = new Department();
//handle recycler view
                            itemsAdapter = new RecyclerViewAdapterForItem(itemsData, getBaseContext(),mActivity, department,pid);
                            itemsAdapter.getInspectionNameToItemAdapter(selectedInspection.getInspection_name(), selectedInspectionID, selectedInspection.getInspection_department());
                            recyclerView.setAdapter(itemsAdapter);
                            recyclerView.setItemAnimator(new DefaultItemAnimator());
                        }
                    }
                });

                Department department = new Department();
//handle recycler view
                itemsAdapter = new RecyclerViewAdapterForItem(itemsData, getBaseContext(),mActivity, department,pid);
                itemsAdapter.getInspectionNameToItemAdapter(selectedInspection.getInspection_name(), selectedInspectionID, selectedInspection.getInspection_department());
                recyclerView.setAdapter(itemsAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());

                addSnapshotListener(inspectionsColl.document(selectedInspectionID));

                handleItemCompletion(itemsData);

                refreshContainer.setRefreshing(false);

                if (isRefreshing == true){

                    refreshing = false;
                }else{

                }
            }
        });
    }

    private void addSnapshotListener(DocumentReference docRef){
        //for realtime changes

        docRef.collection("items")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("Realtime Firestore", "Listen failed.", e);
                            return;
                        }

                        itemsData.clear();

                        List<InspectionItem> items = new ArrayList<>();
                        for (DocumentSnapshot doc : value) {
                            if (doc.get("item_name") != null) {
                                InspectionItem item = doc.toObject(InspectionItem.class);
                                itemsData.add(item);
                                itemsAdapter.notifyDataSetChanged();
                            }
                        }

                        handleItemCompletion(itemsData);
                    }
                });
    }

    private void deleteInspection(CollectionReference colRef, String inspectioniD){
        colRef.document(inspectioniD)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore inspection", "DocumentSnapshot successfully deleted!");
                        Toast.makeText(getBaseContext(), "Inspection Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore inspection", "Error deleting document", e);
                        Toast.makeText(getBaseContext(), "Fail to delete inspection", Toast.LENGTH_SHORT).show();

                    }
                });

    }

    private void submitInspection(int completedCount, int totalCount){

        if (completedCount == totalCount){

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            Inspection archive = new Inspection();
            archive = selectedInspection;
            archive.setInspection_submitted_at(new Date());
            archive.setInspection_submitted_by(user.getUid());
            archive.setInspection_submitted_by_name(user.getDisplayName());

            createInspectionArchive(archive, archive.getInspection_submitted_at(), user.getUid(), user.getDisplayName());
        }else{
            MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .title("Oops")
                    .content("Please ensure all remaining items are inspected")
                    .positiveText("Ok")
                    .show();
        }
    }

    private void createInspectionArchive(Inspection archiveInspection, final Date newDate, final String uid, final String name){
        final String selectedInspectionID = selectedInspection.getInspection_id();
        final CollectionReference archiveColl = devHousePropertyDoc.collection("archives");

        Map<String, Object> data = new HashMap<>();
        data.put("inspection_name", archiveInspection.getInspection_name());

        archiveInspection.setInspection_items(itemsData);

        //add data to archive
        archiveColl.document(selectedInspectionID).set(data);
        archiveColl.document(selectedInspectionID).collection("inspected_history").add(archiveInspection)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        //re-add firebase auto id to field
                        documentReference.update("inspection_id", documentReference.getId());
                        Log.d("Add Firestore", "DocumentSnapshot written with ID: " + documentReference.getId());

                        //refresh current inspection
                        //update last submitted in holding inspection
                        final CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
                        CollectionReference itemsColl = inspectionsColl.document(selectedInspectionID).collection("items");
                        inspectionsColl.document(selectedInspectionID).update(

                                "inspection_submitted_at", newDate,
                                "inspection_submitted_by", uid,
                                "inspection_submitted_by", name

                        );

                        //refresh inspection item value
                        for(int l=0; l<=itemsData.size()-1; l++){
                            final String currentItemID = itemsData.get(l).getItem_id();
                            final InspectionItem currentItem = itemsData.get(l);

                            final StorageReference storageRef = FirebaseUtil.getStorageRef(mActivity);
                            StorageReference imageLocation = storageRef.child("images/temp_" + currentItemID);

                            getTempImageAndAddImageToArchive(storageRef, imageLocation, selectedInspectionID, itemsColl, currentItemID,currentItem);

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.w("Add Firestore", "Error adding document", e);

                        Toast.makeText(getBaseContext(),"Fail to archived inspection", Toast.LENGTH_SHORT).show();
                        //progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    private void addActionItem(final String existingItemID, InspectionItem inspectionItem, final Uri uri){

        FirebaseUser user = FirebaseUtil.getFirebaseUser();
        final CollectionReference actionItemsColl = devHousePropertyDoc.collection("actionItems");

        //initailize action item
        final ActionItems actionItem = new ActionItems(null, existingItemID, inspectionItem.getItem_name(), inspectionItem.getItem_comments(), new Date(), user.getEmail(), null, selectedInspection.getInspection_name(), selectedInspection.getInspection_id(),true);

        final StorageReference storageRef = FirebaseUtil.getStorageRef(this);

        UploadTask uploadTask = storageRef.child("images/inspection_actionItems/"+existingItemID).putFile(uri);
        uploadTask
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
//for determinate progress
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        actionItem.setItem_reported_photo(task.getResult().getDownloadUrl().toString());
                        actionItemsColl.add(actionItem).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        }).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {

                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                String aiID = task.getResult().getId().toString();

                                actionItemsColl.document(aiID).update("ai_id", aiID).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getBaseContext(),"Added to action items", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void getTempImageAndAddImageToArchive(final StorageReference storageRef, StorageReference imageLocation, final String inspectionID, final CollectionReference itemsColl, final String currentItemID, final InspectionItem currentItem) {

        mProgressDialog.show();

        File localFile = null;

        try {
            localFile = File.createTempFile("tempArchive", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        final File finalLocalFile = localFile;
        imageLocation.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                //Glide.with(mActivity).load(finalLocalFile).into(testImageView);
                //upload the temp image to archive and action items
                UploadTask uploadTask = storageRef.child("images").child("inspection_archives").child(inspectionID).putFile(Uri.fromFile(finalLocalFile));

                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getBaseContext(),"Fail to upload image to archive", Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                        finish();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                        itemsColl.document(currentItemID).update(
                                "item_status", 0,
                                "item_condition_photo", null,
                                "item_comments", null

                        ).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getBaseContext(),"Inspection Submitted", Toast.LENGTH_SHORT).show();
                                mProgressDialog.dismiss();
                                finish();
                            }
                        });
                    }
                });

                if (currentItem.getItem_status() == 1) {
                    addActionItem(currentItemID, currentItem, (Uri.fromFile(finalLocalFile)));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(),"Fail to submit inspection", Toast.LENGTH_SHORT).show();
                mProgressDialog.dismiss();
                finish();
            }
        });

    }

    private void updateDoneStatusToFirebase(final String item_id, int status){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference devHousePropertyDoc = db.collection("properties").document(pid);
        final CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
        final CollectionReference itemsColl = inspectionsColl.document(selectedInspection.getInspection_id()).collection("items");

        //update firestore
        itemsColl.document(item_id).update("item_status", status)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        int pendingCount = 0;
                        for (int l=0; l<selectedInspection.getInspection_items_count();l++){
                            if (itemsData.get(l).getItem_status() == 0){
                                pendingCount += 1;
                            }
                        }

                        inspectionsColl.document(selectedInspection.getInspection_id()).update(
                                "inspection_pending_count", pendingCount
                        );

                        setPic();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(getBaseContext(), "fail to update status", Toast.LENGTH_SHORT).show();
                      //  viewBinderHelper.closeLayout(item_id);
                    }
                });
    }

    //uploading images
    private void setPic() {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;

        uploadImageToStorage(selectedItemId);
    }

    private void uploadImageToStorage(final String item_id){
        //initialize storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

        // Create a child reference
        // imagesRef now points to "images"
        StorageReference imagesItemRef = storageRef.child(pid).child("images").child("temp_"+item_id);


        Uri file = Uri.fromFile(new File(photoPath));

        UploadTask uploadTask2 = imagesItemRef.putFile(file);

        //UploadTask uploadTask = imagesItemRef.child(item_id).putBytes(data);
        uploadTask2.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(mActivity,"Fail to add item", Toast.LENGTH_SHORT).show();

                //createButton.setClickable(true);

                //progressBar.setVisibility(View.INVISIBLE);

                return;
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference devHousePropertyDoc = db.collection("properties").document(pid);

                CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
                final CollectionReference itemsColl = inspectionsColl.document(selectedInspection.getInspection_id()).collection("items");

                //update firestore
                itemsColl.document(item_id).update("item_condition_photo", String.valueOf(downloadUrl));

                Toast.makeText(mActivity,"condition image updated", Toast.LENGTH_SHORT).show();

                //createButton.setClickable(true);

                //progressBar.setVisibility(View.INVISIBLE);

                return;
            }
        })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        double progress = 100.0 * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        System.out.println("Upload is " + progress + "% done");
                        int totalBytes = (int) taskSnapshot.getTotalByteCount();
                        int currentprogress = (int) progress;
                    }
                });
    }



    private void showProgress(int currentProgress, int totalBytes) {

        boolean showMinMax = true;

        mDeterminateDialog = new MaterialDialog.Builder(getApplicationContext())
                .title("Uploading Image")
                .content("please wait")
                .progress(false, totalBytes, showMinMax)
                .show();

        // Loop until the dialog's progress value reaches the max (150)
        while (mDeterminateDialog.getCurrentProgress() != mDeterminateDialog.getMaxProgress()) {
            // If the progress dialog is cancelled (the user closes it before it's done), break the loop
            if (mDeterminateDialog.isCancelled()) break;
            // Wait 50 milliseconds to simulate doing work that requires progress
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
            // Increment the dialog's progress by 1 after sleeping for 50ms
            mDeterminateDialog.incrementProgress(1);
        }
    }
}
