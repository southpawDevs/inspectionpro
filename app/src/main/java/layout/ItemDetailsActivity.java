package layout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import devs.southpaw.com.inspectionpro.R;
import objects.ActionItems;
import objects.InspectionItem;

public class ItemDetailsActivity extends AppCompatActivity {

    private TextView itemTitle;
    private TextView itemDescription;
    private TextView itemCondition;
    private TextView itemMethod;
    private EditText itemComments;
    private TextView itemQuestion;
    private Button addConditionImageButton;
    private Button updateCommentButton;
    private SimpleDraweeView snapshotImageView;
    InspectionItem selectedItem;
    String inspectionName;
    String inspectionID;
    private SimpleDraweeView itemImageView;

    private Button itemStatus0;
    private Button itemStatus1;
    private Button itemStatus2;

    private int REQUEST_CODE = 1880;

    String mCurrentPhotoPath;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "ITEMJPEG" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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
                Uri photoURI = FileProvider.getUriForFile(this,
                        "southpaw.dev.inspectionpro.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CODE);
            }
        }
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = snapshotImageView.getWidth();
        int targetH = snapshotImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        //int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        //bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        snapshotImageView.setImageBitmap(bitmap);
        snapshotImageView.setVisibility(View.VISIBLE);

        uploadImageToStorage(selectedItem.getItem_id());
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_item_details);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        itemTitle = (TextView) findViewById(R.id.item_title_text_view);
        itemDescription = (TextView) findViewById(R.id.item_description);
        itemMethod = (TextView) findViewById(R.id.item_method);
        itemCondition = (TextView) findViewById(R.id.item_condition);
        itemComments = (EditText) findViewById(R.id.item_comment_edit_text);
        itemQuestion = (TextView) findViewById(R.id.item_question_text_view);
        addConditionImageButton = (Button) findViewById(R.id.add_condition_image_button);
        snapshotImageView = (SimpleDraweeView) findViewById(R.id.snapshot_image_view);
        itemImageView = (SimpleDraweeView) findViewById(R.id.item_image_view);
        updateCommentButton = (Button) findViewById(R.id.update_comment_button);

        itemStatus0 = (Button) findViewById(R.id.item_status_0);
        itemStatus1 = (Button) findViewById(R.id.item_status_1);
        itemStatus2 = (Button) findViewById(R.id.item_status_2);

        snapshotImageView.setVisibility(View.GONE);
        snapshotImageView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                openWithUrl(selectedItem.getItem_condition_photo());
            }
        });

        itemImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWithUrl(selectedItem.getItem_photo());
            }
        });

        // Deserialization
        String itemJson= getIntent().getStringExtra("selected_item");
        inspectionName = getIntent().getStringExtra("inspection_name");
        inspectionID = getIntent().getStringExtra("inspection_id");
        Log.d("itemJson", itemJson);

        Gson gson = new Gson();
        selectedItem = gson.fromJson(itemJson, InspectionItem.class);

        populateScreenData();

        actionBar.setTitle(inspectionName + "/" + selectedItem.getItem_name());

        //add button listeners
        addConditionImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        IconicsDrawable cameraIcon = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_add_a_photo)
                .color(Color.WHITE)
                .backgroundColor(Color.GRAY)
                .sizeDp(8)
                .paddingDp(15);
        addConditionImageButton.setBackground(cameraIcon);

        updateCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadCommentsToFirebase(selectedItem.getItem_id(), itemComments.getText().toString());
            }
        });

        itemComments.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do something, e.g. set your TextView here via .setText()
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        itemStatus0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatusToFirestore(selectedItem.getItem_id(), 0);
            }
        });

        itemStatus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatusToFirestore(selectedItem.getItem_id(), 1);
            }
        });

        itemStatus2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatusToFirestore(selectedItem.getItem_id(), 2);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.items_detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            // action with ID action_refresh was selected
            case R.id.inspection_item_delete:

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");

                final CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
                final CollectionReference itemsColl = inspectionsColl.document(inspectionID).collection("items");

                deleteInspectionItem(itemsColl, selectedItem.getItem_id());

                break;

            case R.id.inspection_item_edit:

                //edit item here

                break;

            case android.R.id.home:
                Log.d("clicked", "action bar clicked");
                finish();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

           setPic();
        }
    }

    void populateScreenData(){
        String name = selectedItem.getItem_name();
        String description =  selectedItem.getItem_description();
        String method = selectedItem.getItem_method();
        String condition = selectedItem.getItem_condition();
        String comment = selectedItem.getItem_comments();
        String question = selectedItem.getItem_check_question();

        itemTitle.setText(name);
        itemDescription.setText("-" + description);
        itemMethod.setText("-" + method);
        itemCondition.setText("Condition: " + condition);
        itemComments.setText(comment);
        itemQuestion.setText(question);

        String urlImage = selectedItem.getItem_photo();

        if (urlImage != "" || urlImage != null){
            Glide.with(this).load(urlImage).fitCenter().into(itemImageView);
        }

        String conditionImageUrl = selectedItem.getItem_condition_photo();

        if (conditionImageUrl != null){
            Glide.with(this).load(conditionImageUrl).override(200,200).fitCenter().into(snapshotImageView);
            snapshotImageView.setVisibility(View.VISIBLE);
        }else{
            snapshotImageView.setVisibility(View.GONE);
        }
    }

    private void uploadImageToStorage(final String item_id){
        //initialize storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

        // Create a child reference
        // imagesRef now points to "images"
        StorageReference imagesItemRef = storageRef.child("oNJZmUlwxGxAymdyKoIV").child("images").child("temp_"+item_id);


        Uri file = Uri.fromFile(new File(mCurrentPhotoPath));

        UploadTask uploadTask2 = imagesItemRef.putFile(file);

        //UploadTask uploadTask = imagesItemRef.child(item_id).putBytes(data);
        uploadTask2.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getBaseContext(),"Fail to add item", Toast.LENGTH_SHORT).show();

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
                DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");

                CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
                final CollectionReference itemsColl = inspectionsColl.document(inspectionID).collection("items");

                //update firestore
                itemsColl.document(item_id).update("item_condition_photo", String.valueOf(downloadUrl));

                Toast.makeText(getBaseContext(),"condition image updated", Toast.LENGTH_SHORT).show();

                //createButton.setClickable(true);

                //progressBar.setVisibility(View.INVISIBLE);

                return;
            }
        });
    }

    private void uploadCommentsToFirebase(String item_id, String comments){

        if (comments != null || comments != "") {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");
            CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
            final CollectionReference itemsColl = inspectionsColl.document(inspectionID).collection("items");

            //update firestore
            itemsColl.document(item_id)
                    .update("item_comments", comments,
                            "item_reported_at", new Date(),
                            "item_reported_by", user.getEmail())
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideKeyboard();
                            itemComments.clearFocus();
                            Toast.makeText(getBaseContext(), "comments updated", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            itemComments.clearFocus();
                            hideKeyboard();
                            Toast.makeText(getBaseContext(), "fail to upload comment", Toast.LENGTH_SHORT).show();
                        }
                    });

        }else{
            Toast.makeText(this, "please fill in comments", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatusToFirestore(String item_id, int status){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");
        CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
        final CollectionReference itemsColl = inspectionsColl.document(inspectionID).collection("items");

        //update firestore inspection item
        itemsColl.document(item_id)
                .update("item_status", status,
                        "item_reported_at", new Date(),
                        "item_reported_by", user.getEmail()
                )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideKeyboard();
                        itemComments.clearFocus();
                        Toast.makeText(getBaseContext(), "status updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        itemComments.clearFocus();
                        hideKeyboard();
                        Toast.makeText(getBaseContext(), "fail to update status", Toast.LENGTH_SHORT).show();
                    }
                });

        //update firestore Action Items
        if (status == 1) {
            //red

            final CollectionReference actionItemsColl = devHousePropertyDoc.collection("actionItems");

            //initailize action item
            ActionItems actionItem = new ActionItems(item_id, selectedItem.getItem_name(), selectedItem.getItem_comments(), new Date(), user.getEmail(), selectedItem.getItem_condition_photo(), inspectionName, inspectionID);

            actionItemsColl.document(selectedItem.getItem_id()).set(actionItem);
            Toast.makeText(getBaseContext(), "added to action item", Toast.LENGTH_SHORT).show();
        }else{
            final CollectionReference actionItemsColl = devHousePropertyDoc.collection("actionItems");
            actionItemsColl.document(selectedItem.getItem_id()).delete();
            Toast.makeText(getBaseContext(), "removed from action item", Toast.LENGTH_SHORT).show();
        }

    }


    private void deleteInspectionItem(CollectionReference colRef, String itemID){
        colRef.document(itemID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Firestore inspection", "DocumentSnapshot successfully deleted!");
                        Toast.makeText(getBaseContext(), "Item deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Firestore inspection", "Error deleting document", e);
                        Toast.makeText(getBaseContext(), "Fail to delete item", Toast.LENGTH_SHORT).show();

                    }
                });

    }

    public void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    ///////open image viewer
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


}
