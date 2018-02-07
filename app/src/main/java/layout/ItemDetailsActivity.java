package layout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;

import devs.southpaw.com.inspectionpro.R;
import objects.InspectionItem;

public class ItemDetailsActivity extends AppCompatActivity {

    private TextView itemDescription;
    private TextView itemCondition;
    private TextView itemMethod;
    private Button submitButton;
    private ImageView snapshotImageView;
    InspectionItem selectedItem;
    String inspectionName;

    private int REQUEST_CODE = 1888;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        itemDescription = (TextView) findViewById(R.id.item_description);
        itemMethod = (TextView) findViewById(R.id.item_method);
        itemCondition = (TextView) findViewById(R.id.item_condition);
        submitButton = (Button) findViewById(R.id.submit_button);
        snapshotImageView = (ImageView) findViewById(R.id.snapshot_image_view);

        snapshotImageView.setVisibility(View.GONE);

        // Deserialization
        String itemJson= getIntent().getStringExtra("selected_item");
        inspectionName = getIntent().getStringExtra("inspection_name");
        Log.d("itemJson", itemJson);

        Gson gson = new Gson();
        selectedItem = gson.fromJson(itemJson, InspectionItem.class);

        populateScreenData();

        actionBar.setTitle(inspectionName + "/" + selectedItem.getItem_name());

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            snapshotImageView.setImageBitmap(photo);
            snapshotImageView.setVisibility(View.VISIBLE);
        }
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
        String name = selectedItem.getItem_name();
        String description =  selectedItem.getItem_description();
        String method = selectedItem.getItem_method();
        String condition = selectedItem.getItem_condition();

        itemDescription.setText("Title: " + description);
        itemMethod.setText("Method: " + method);
        itemCondition.setText("Condition: " + condition);
    }

}
