package layout;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.R;
import adapters.RecyclerViewAdapterForItem;
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

    Boolean refreshing = false;

    private int currentItemsCount;
    private int completedItemsCount = 0;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");

    private Activity mActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_details);

        completedCount = (TextView) findViewById(R.id.count_text_view);
        submitInspectionButton = (Button) findViewById(R.id.submit_inspection_button);

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
        getMenuInflater().inflate(R.menu.items_menu, menu);
        return super.onCreateOptionsMenu(menu);
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

                //handle recycler view
                itemsAdapter = new RecyclerViewAdapterForItem(itemsData, getBaseContext());
                itemsAdapter.getInspectionNameToItemAdapter(selectedInspection.getInspection_name(), selectedInspectionID);
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
            Toast.makeText(getBaseContext(), "Please inspect the remaining items", Toast.LENGTH_SHORT).show();
        }
    }

    private void createInspectionArchive(Inspection archiveInspection, final Date newDate, final String uid, final String name){
        final String selectedInspectionID = selectedInspection.getInspection_id();
        final CollectionReference archiveColl = devHousePropertyDoc.collection("archives");

        Map<String, Object> data = new HashMap<>();
        data.put("inspection_name", archiveInspection.getInspection_name());

        archiveInspection.setInspection_items(itemsData);

        archiveColl.document(selectedInspectionID).set(data);
        archiveColl.document(selectedInspectionID).collection("inspected_history").add(archiveInspection)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        //re-add firebase auto id to field
                        documentReference.update("inspection_id", documentReference.getId());

                        Log.d("Add Firestore", "DocumentSnapshot written with ID: " + documentReference.getId());

                        //progressBar.setVisibility(View.INVISIBLE);

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
                            String currentItemID = itemsData.get(l).getItem_id();

                            if (itemsData.get(l).getItem_status() == 1){
                                itemsColl.document(currentItemID).update(
                                        "item_condition_photo", null

                                );
                            }else {
                                itemsColl.document(currentItemID).update(
                                        "item_status", 0,
                                        "item_condition_photo", null

                                );
                            }

                            final StorageReference storageRef = FirebaseUtil.getStorageRef(mActivity);
                            StorageReference imageLocation = storageRef.child("images/temp_" + currentItemID);
                            imageLocation.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    UploadTask uploadTask = storageRef.child("images").child("inspection_archives").child(selectedInspectionID).putFile(uri);
                                    // Register observers to listen for when the download is done or if it fails
                                    uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle unsuccessful uploads
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                        }
                                    });


                                }
                            });

                            Toast.makeText(getBaseContext(),"Inspection Submitted", Toast.LENGTH_SHORT).show();
                            finish();
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


}
