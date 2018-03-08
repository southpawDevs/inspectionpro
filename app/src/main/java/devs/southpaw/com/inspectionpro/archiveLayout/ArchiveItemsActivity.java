package devs.southpaw.com.inspectionpro.archiveLayout;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import adapters.RecyclerViewAdapterForArchiveItems;
import adapters.RecyclerViewAdapterForItem;
import devs.southpaw.com.inspectionpro.R;
import layout.InspectionItemAddActivity;
import objects.Inspection;
import objects.InspectionItem;

public class ArchiveItemsActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "no_name";

    public Inspection selectedInspection;

    private List<Inspection> inspectionHistory = new ArrayList<>();

    private LinearLayoutManager mLayoutManager;
    private RecyclerViewAdapterForArchiveItems itemsAdapter;
    private SwipeRefreshLayout refreshContainer;
    RecyclerView recyclerView;
    TextView completedCount;

    Boolean refreshing = false;

    private int currentItemsCount;
    private int completedItemsCount = 0;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_items);

        //handle pull refreshing container
        refreshContainer = (SwipeRefreshLayout) findViewById(R.id.refresh_container_detail);
        refreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (itemsAdapter != null){
                    itemsAdapter.clear();
                }
                refreshing = true;
                getArchiveDataFromFireStore(true);
            }
        });

        refreshContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_archive_items);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setAutoMeasureEnabled(true);
        recyclerView.setLayoutManager(mLayoutManager);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Deserialization
        String inspectionJson= getIntent().getStringExtra("inspection");
        Log.d("inspectionJson", inspectionJson);

        Gson gson = new Gson();
        selectedInspection = gson.fromJson(inspectionJson, Inspection.class);

        //set title in action bar after deserializing data
        actionBar.setTitle(selectedInspection.getInspection_name());
    }

    @Override
    protected void onResume() {
        super.onResume();
        getArchiveDataFromFireStore(true);
    }

    //handle back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                Log.d("clicked", "action bar clicked");
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getArchiveDataFromFireStore(final Boolean isRefreshing){

        refreshContainer.setRefreshing(true);

        final String FireStoreTAG = "firestoreTag";
        final String selectedInspectionID = selectedInspection.getInspection_id();

        final CollectionReference archivesColl = devHousePropertyDoc.collection("archives");
        CollectionReference itemsColl = archivesColl.document(selectedInspectionID).collection("inspected_history");

        //final List<Inspection> inspectionsHistory = new ArrayList<>();

        itemsColl.orderBy("inspection_submitted_at", Query.Direction.DESCENDING).limit(40).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {

                        if (document != null) {
                            Inspection item = document.toObject(Inspection.class);
                            inspectionHistory.add(item);
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
                itemsAdapter = new RecyclerViewAdapterForArchiveItems(inspectionHistory);
                //itemsAdapter.getInspectionNameToItemAdapter(selectedInspection.getInspection_name(), selectedInspectionID);
                recyclerView.setAdapter(itemsAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());

                //addSnapshotListener(archivesColl.document(selectedInspectionID));

                refreshContainer.setRefreshing(false);

                if (isRefreshing == true){

                    refreshing = false;
                }else{

                }
            }
        });
    }
}