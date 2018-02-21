package layout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.ExifInterface;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import devs.southpaw.com.inspectionpro.R;
import objects.InspectionItem;

public class ItemDetailsActivity extends AppCompatActivity {

    private TextView itemDescription;
    private TextView itemCondition;
    private TextView itemMethod;
    private EditText itemComments;
    private Button addConditionImageButton;
    private Button updateCommentButton;
    private ImageView snapshotImageView;
    InspectionItem selectedItem;
    String inspectionName;
    String inspectionID;
    private  ImageView itemImageView;

    private Button itemStatus0;
    private Button itemStatus1;
    private Button itemStatus2;


    private int REQUEST_CODE = 1880;

    String mCurrentPhotoPath;

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

        itemDescription = (TextView) findViewById(R.id.item_description);
        itemMethod = (TextView) findViewById(R.id.item_method);
        itemCondition = (TextView) findViewById(R.id.item_condition);
        itemComments = (EditText) findViewById(R.id.item_comment_edit_text);
        addConditionImageButton = (Button) findViewById(R.id.add_condition_image_button);
        snapshotImageView = (ImageView) findViewById(R.id.snapshot_image_view);
        itemImageView = (ImageView) findViewById(R.id.item_image_view);
        updateCommentButton = (Button) findViewById(R.id.update_comment_button);

        itemStatus0 = (Button) findViewById(R.id.item_status_0);
        itemStatus1 = (Button) findViewById(R.id.item_status_1);
        itemStatus2 = (Button) findViewById(R.id.item_status_2);

        snapshotImageView.setVisibility(View.GONE);

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
                updateStatusToFirebase(selectedItem.getItem_id(), 0);
            }
        });

        itemStatus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatusToFirebase(selectedItem.getItem_id(), 1);
            }
        });

        itemStatus2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStatusToFirebase(selectedItem.getItem_id(), 2);
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

        itemDescription.setText("Title: " + description);
        itemMethod.setText("Method: " + method);
        itemCondition.setText("Condition: " + condition);
        itemComments.setText(comment);

        String urlImage = selectedItem.getItem_photo();

        if (urlImage != "" || urlImage != null){
            Picasso.with(this).load(urlImage).into(itemImageView);
        }

        String conditionImageUrl = selectedItem.getItem_condition_photo();

        if (conditionImageUrl != "" || conditionImageUrl != null){
            Picasso.with(this).load(conditionImageUrl).into(snapshotImageView);
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
        StorageReference imagesItemRef = storageRef.child("oNJZmUlwxGxAymdyKoIV").child("images").child("temp");


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
            itemsColl.document(item_id).update("item_comments", comments)
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

    private void updateStatusToFirebase(String item_id, int status){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");
        CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
        final CollectionReference itemsColl = inspectionsColl.document(inspectionID).collection("items");

        //update firestore
        itemsColl.document(item_id).update("item_status", status)
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

}
