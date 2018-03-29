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
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.FileProvider;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.R;
import devs.southpaw.com.inspectionpro.SharedPrefUtil;
import devs.southpaw.com.inspectionpro.UIUtil;
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
    private SimpleDraweeView snapshotImageView;
    InspectionItem selectedItem;
    String inspectionName;
    String inspectionID;
    int pendingCount;
    private SimpleDraweeView itemImageView;
    private ConstraintLayout constraintLayout;
    private Activity mActivity;
    MaterialDialog mDialog;

    private Button itemStatus0;
    private Button itemStatus1;
    private Button itemStatus2;

    private CardView itemStatusSelect0;
    private CardView itemStatusSelect1;
    private CardView itemStatusSelect2;

    private MaterialDialog mProgressDialog;

    ImageView methodIcon;
    ImageView conditionIcon;
    IconicsImageView conditionImageIcon;
    IconicsImageView commentIcon;


    private InputMethodManager imm;

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

        mActivity = this;
        mProgressDialog = new MaterialDialog.Builder(mActivity)
                .title("Uploading image")
                .content("please wait...")
                .progress(true, 0)
                .show();
        mProgressDialog.dismiss();

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

        itemStatus0 = (Button) findViewById(R.id.item_status_0);
        itemStatus1 = (Button) findViewById(R.id.item_status_1);
        itemStatus2 = (Button) findViewById(R.id.item_status_2);

        itemStatusSelect0 = (CardView) findViewById(R.id.item_status_check_0);
        itemStatusSelect1 = (CardView) findViewById(R.id.item_status_check_1);
        itemStatusSelect2 = (CardView) findViewById(R.id.item_status_check_2);

        IconicsDrawable icon= UIUtil.getGMD(this,GoogleMaterial.Icon.gmd_build, 20,2, R.color.colorDarkGrey);
        IconicsDrawable iconSearch= UIUtil.getGMD(this,GoogleMaterial.Icon.gmd_search, 20,2, R.color.colorDarkGrey);

        methodIcon = (ImageView) findViewById(R.id.method_icon);
        methodIcon.setImageDrawable(icon);

        conditionImageIcon = (IconicsImageView) findViewById(R.id.condition_image_icon);
        conditionImageIcon.setIcon(UIUtil.getGMD(this, GoogleMaterial.Icon.gmd_photo, 20,0, R.color.colorPrimaryDark));

        commentIcon = (IconicsImageView) findViewById(R.id.comment_image_icon);
        commentIcon.setIcon(UIUtil.getGMD(this, GoogleMaterial.Icon.gmd_comment,20,0,R.color.primary_dark));

        conditionIcon = (ImageView) findViewById(R.id.condition_icon);
        conditionIcon.setImageDrawable(iconSearch);

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
        pendingCount = getIntent().getIntExtra("inspected_count",0);
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

        itemComments.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // do something, e.g. set your TextView here via .setText()
                    uploadCommentsToFirebase(selectedItem.getItem_id(), itemComments.getText().toString());
                    return true;
                }
                return false;
            }
        });

        itemStatus0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedItem.getItem_status() == 0) {
                    Toast.makeText(getBaseContext(), "already unchecked", Toast.LENGTH_SHORT).show();
                }else {
                    updateStatusToFirestore(selectedItem.getItem_id(), 0, itemComments.getText().toString());
                }
            }
        });

        itemStatus1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedItem.getItem_status() == 1) {
                    Toast.makeText(getBaseContext(), "already red", Toast.LENGTH_SHORT).show();
                }else {
                    updateStatusToFirestore(selectedItem.getItem_id(), 1, itemComments.getText().toString());
                }
            }
        });

        itemStatus2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedItem.getItem_status() == 2) {
                    Toast.makeText(getBaseContext(), "already green", Toast.LENGTH_SHORT).show();
                }else {
                    updateStatusToFirestore(selectedItem.getItem_id(), 2, itemComments.getText().toString
                            ());
                }
            }
        });

//set status selection
        switch (selectedItem.getItem_status()){
            case 0 :
                itemStatusSelect0.setVisibility(View.VISIBLE);
                itemStatusSelect1.setVisibility(View.INVISIBLE);
                itemStatusSelect2.setVisibility(View.INVISIBLE);
                break;
            case 1 :
                itemStatusSelect1.setVisibility(View.VISIBLE);
                itemStatusSelect0.setVisibility(View.INVISIBLE);
                itemStatusSelect2.setVisibility(View.INVISIBLE);
                break;
            case 2 :
                itemStatusSelect2.setVisibility(View.VISIBLE);
                itemStatusSelect1.setVisibility(View.INVISIBLE);
                itemStatusSelect0.setVisibility(View.INVISIBLE);
                break;
            default:break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Boolean admin = SharedPrefUtil.getAdminRights(this);
        if(admin == true) {
            getMenuInflater().inflate(R.menu.items_detail_menu, menu);
        }
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

        itemComments.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                itemComments.setCursorVisible(true);
                return false;
            }
        });

        itemTitle.setText(name);
        itemDescription.setText(description);
        itemMethod.setText(method);
        itemCondition.setText(condition);
        itemComments.setText(comment);
        itemQuestion.setText(question);

        String urlImage = selectedItem.getItem_photo();

        if (urlImage != "" || urlImage != null){
            Glide.with(this).load(urlImage).fitCenter().into(itemImageView);
        }

        String conditionImageUrl = selectedItem.getItem_condition_photo();

        if (conditionImageUrl != null){
            Glide.with(this).load(conditionImageUrl).centerCrop().into(snapshotImageView);
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
        })
        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                double progress = 100.0 * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                System.out.println("Upload is " + progress + "% done");
                int totalBytes = (int) taskSnapshot.getTotalByteCount();
                int currentprogress = (int) progress;

                //showProgress(currentprogress, totalBytes );
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
                            itemComments.setCursorVisible(true);
                           itemComments.setFocusable(false);

                            Toast.makeText(getBaseContext(), "comments updated", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            itemComments.setCursorVisible(true);
                            hideKeyboard();
                            Toast.makeText(getBaseContext(), "fail to upload comment", Toast.LENGTH_SHORT).show();
                        }
                    });

        }else{
            Toast.makeText(this, "please fill in comments", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStatusToFirestore(String item_id, final int status, String comments){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");
        CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
        final CollectionReference itemsColl = inspectionsColl.document(inspectionID).collection("items");

        //update firestore inspection item
        itemsColl.document(item_id)
                .update("item_status", status,
                        //"item_comments", comments,
                        "item_reported_at", new Date(),
                        "item_reported_by", user.getEmail()
                )
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideKeyboard();
                        itemComments.setCursorVisible(true);

                        Toast.makeText(getBaseContext(), "status updated", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        itemComments.setCursorVisible(true);
                        hideKeyboard();
                        Toast.makeText(getBaseContext(), "fail to update status", Toast.LENGTH_SHORT).show();
                    }
                });

        //update inspected count
        if (selectedItem.getItem_status() == 0) {

            if (status != 0) {
                //if current status is zero and is updating to other int (pending should decrease by 1)
                inspectionsColl.document(inspectionID).update(
                        "inspection_pending_count", pendingCount -= 1
                );
            } else {

            }

        }else{
            //if current status is either 1 or 2 and is updating to other int
            if (status == 0) {
                //(pending should increase by 1 after revert to normal state)
                inspectionsColl.document(inspectionID).update(
                        "inspection_pending_count", pendingCount += 1
                );
            } else {

            }
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

    private void showProgress(int currentProgress, int totalBytes){

        boolean showMinMax = true;

        mDialog = new MaterialDialog.Builder(getApplicationContext())
                .title("Uploading Image")
                .content("please wait")
                .progress(false, totalBytes, showMinMax)
                .show();

        // Loop until the dialog's progress value reaches the max (150)
        while (mDialog.getCurrentProgress() != mDialog.getMaxProgress()) {
            // If the progress dialog is cancelled (the user closes it before it's done), break the loop
            if (mDialog.isCancelled()) break;
            // Wait 50 milliseconds to simulate doing work that requires progress
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                break;
            }
            // Increment the dialog's progress by 1 after sleeping for 50ms
           mDialog.incrementProgress(1);
        }

// When the loop exits, set the dialog content to a string that equals "Done"
        mDialog.setContent("Done");
    }


}
