package devs.southpaw.com.inspectionpro;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.Gson;

import objects.Inspection;
import objects.InspectionItem;

public class ItemDetailsActivity extends AppCompatActivity {

    private TextView itemDescription;
    private TextView itemCondition;
    private TextView itemMethod;

    InspectionItem selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        itemDescription = (TextView) findViewById(R.id.item_description);
        itemMethod = (TextView) findViewById(R.id.item_method);
        itemCondition = (TextView) findViewById(R.id.item_condition);

        // Deserialization
        String itemJson= getIntent().getStringExtra("selected_item");
        Log.d("itemJson", itemJson);

        Gson gson = new Gson();
        selectedItem = gson.fromJson(itemJson, InspectionItem.class);

        populateScreenData();

        actionBar.setTitle(selectedItem.getItemName());
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

    void populateScreenData(){

        String description =  selectedItem.getItemName();
        String method = selectedItem.getItemMethod();
        String condition = selectedItem.getItemCondition();

        itemDescription.setText("Title: " + description);
        itemMethod.setText("Method: " + method);
        itemCondition.setText("Condition: " + condition);
    }

}
