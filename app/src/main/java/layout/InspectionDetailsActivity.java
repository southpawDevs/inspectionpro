package layout;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

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
    RecyclerView recyclerView;
    TextView completedCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_details);

        completedCount = (TextView) findViewById(R.id.count_text_view);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_items);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Deserialization
        String inspectionJson= getIntent().getStringExtra("inspection");
        Log.d("inspectionJson", inspectionJson);

        Gson gson = new Gson();
        selectedInspection = gson.fromJson(inspectionJson, Inspection.class);

        itemsData = selectedInspection.getInspectionItems();

        //handle recycler view
        itemsAdapter = new RecyclerViewAdapterForItem(itemsData, this);
        recyclerView.setAdapter(itemsAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        handleItemCompletion(itemsData);

        //set title in action bar after deserializing data
        actionBar.setTitle(selectedInspection.getInspectionName());
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

        if (id == android.R.id.home) {
            Log.d("clicked", "action bar clicked");
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void handleItemCompletion(List<InspectionItem> items){

        int itemsCompletedCount = 0;
        for(int l=0; l<items.size(); l++){

            if (items.get(l).getItemStatus() == 2){
                itemsCompletedCount += 1;
            }//end if


        }//end loop

        completedCount.setText(String.valueOf(itemsCompletedCount) + "/" + String.valueOf(items.size()) + "completed");
    }

}
