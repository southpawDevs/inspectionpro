package layout;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

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
    RecyclerView recyclerView;
    TextView completedCount;

    Boolean refreshing = false;

    int currentItemsCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_details);

        completedCount = (TextView) findViewById(R.id.count_text_view);

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


        recyclerView = (RecyclerView) findViewById(R.id.recycler_items);
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

        currentItemsCount = selectedInspection.getInspectionItems().size();

        //set title in action bar after deserializing data
        actionBar.setTitle(selectedInspection.getInspection_name());
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

            case android.R.id.home:
                Log.d("clicked", "action bar clicked");
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void handleItemCompletion(List<InspectionItem> items){

        int itemsCompletedCount = 0;
        for(int l=0; l<items.size(); l++){

            if (items.get(l).getItem_status() == 2){
                itemsCompletedCount += 1;
            }//end if
        }//end loop

        completedCount.setText(String.valueOf(itemsCompletedCount) + "/" + String.valueOf(items.size()) + " completed");
    }


    private void getItemsDataFromFireStore(final Boolean isRefreshing){

        refreshContainer.setRefreshing(true);

        final String FireStoreTAG = "firestoreTag";
        final String selectedInspectionID = selectedInspection.getInspection_id();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");

        CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
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

                handleItemCompletion(itemsData);

                refreshContainer.setRefreshing(false);

                if (isRefreshing == true){

                    refreshing = false;
                }else{

                }


            }
        });
    }
}
