package devs.southpaw.com.inspectionpro.actionItemsLayout;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.mikepenz.materialize.util.UIUtils;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.R;
import devs.southpaw.com.inspectionpro.UIUtil;
import objects.ActionItems;

public class ActionItemsDetailActivity extends AppCompatActivity {

    private ActionItems selectedActionItem;
    private SimpleDraweeView itemImageView;
    private SimpleDraweeView conditionImageView;
    private TextView titleActionItem;
    private TextView inspectionCategory;
    private TextView summaryReportTextView;
    private TextView reportedByTextView;
    private TextView reportedAtTextView;
    private EditText summaryEditText;
    private Activity mActivity;

    private Button greenButton;
    private Button redButton;

    private CardView itemStatusGreen;
    private CardView itemStatusRed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_items_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.action_item_toolbar);
        toolbar.getMenu();
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mActivity = this;

        titleActionItem = (TextView) findViewById(R.id.ai_title_text_view);
        inspectionCategory = (TextView) findViewById(R.id.ai_inspection_category);
        summaryReportTextView = (TextView) findViewById(R.id.summary_report_text_view);
        reportedByTextView = (TextView) findViewById(R.id.ai_reported_by_text_view);
        reportedAtTextView = (TextView) findViewById(R.id.ai_reported_at_text_view);
        itemImageView = (SimpleDraweeView) findViewById(R.id.ai_detail_image_view);
        conditionImageView = (SimpleDraweeView) findViewById(R.id.ai_condition_image_view);
        summaryEditText = (EditText) findViewById(R.id.summary_checked_edit_text);

        greenButton = (Button) findViewById(R.id.green_button);
        redButton = (Button) findViewById(R.id.red_button);
        itemStatusGreen = (CardView) findViewById(R.id.action_item_status_check_green);
        itemStatusRed = (CardView) findViewById(R.id.action_item_status_check_red);

        UIUtil.setStatusAndActionBarDeepOrangeColor(this, toolbar);

        conditionImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWithUrl(selectedActionItem.getItem_reported_photo().toString());
            }
        });

        // Deserialization
        String aiJson= getIntent().getStringExtra("ai");
        Log.d("inspectionJson", aiJson);

        Gson gson = new Gson();
        selectedActionItem = gson.fromJson(aiJson, ActionItems.class);

        titleActionItem.setText(selectedActionItem.getItem_name());
        inspectionCategory.setText(selectedActionItem.getInspection_name());
        summaryReportTextView.setText(selectedActionItem.getItem_report_description());
        reportedByTextView.setText(selectedActionItem.getItem_reported_by());

        String date = UIUtil.getStringDateFromDate(selectedActionItem.getItem_reported_at());
        reportedAtTextView.setText(date);

        StorageReference storageRef = FirebaseUtil.getStorageRef(this);
        storageRef.child("images/inspection_items/"+selectedActionItem.getItem_existing_id()).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                    Log.d("URL", task.getResult().toString());
                    final Uri url = task.getResult();
                    itemImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openWithUrl(url.toString());
                        }
                    });

                Glide.with(mActivity).load(url).fitCenter().into(itemImageView);

            }
        });
        Glide.with(this).load(selectedActionItem.getItem_reported_photo()).override(600,400).centerCrop().into(conditionImageView);


        if (selectedActionItem.getItem_checked_reported_comments() == null){
            summaryEditText.setText("");
        }else{
            summaryEditText.setText(selectedActionItem.getItem_checked_reported_comments());
        }

        summaryEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do something, e.g. set your TextView here via .setText()
                    uploadCommentsToFirebase(selectedActionItem.getAi_id(), summaryEditText.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private void openWithUrl(String url){

        List<String> images = new ArrayList<>();

        images.add(url);

        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .setRotationOptions(RotationOptions.autoRotate())
                .build();

        ImageViewer imageViewer;
        imageViewer = new ImageViewer.Builder<>(this, images)
                .setStartPosition(0)
                .hideStatusBar(false)
                .allowZooming(true)
                .allowSwipeToDismiss(true)
                .setCustomImageRequestBuilder(ImageRequestBuilder.fromRequest(imageRequest))
                .build();

        imageViewer.show();
    }

    private void uploadCommentsToFirebase(String item_id, String comments){

        if (comments != null || comments != "") {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");
            CollectionReference actionItemColl = devHousePropertyDoc.collection("actionItems");
            final DocumentReference itemsColl = actionItemColl.document(selectedActionItem.getAi_id());

            //update firestore
            itemsColl
                    .update("item_checked_reported_comments", comments,
                            "item_checked_reported_at", new Date(),
                            "item_checked_reported_by", FirebaseUtil.getFirebaseUser().getEmail())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            summaryEditText.setCursorVisible(false);
                            hideKeyboard();
                            Toast.makeText(getBaseContext(), "report updated", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            summaryEditText.setCursorVisible(false);
                            hideKeyboard();
                            Toast.makeText(getBaseContext(), "fail to upload report", Toast.LENGTH_SHORT).show();
                        }
                    });

        }else{
            Toast.makeText(this, "please fill in comments", Toast.LENGTH_SHORT).show();
        }
    }

    public void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void updateAIStatusToFirestore(String ai_item_id, final Boolean status) {

        DocumentReference aiItemRef = FirebaseUtil.getActionItem(mActivity,ai_item_id);


    }
}
