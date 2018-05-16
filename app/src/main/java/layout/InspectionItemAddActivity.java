package layout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;

import devs.southpaw.com.inspectionpro.R;
import objects.InspectionItem;

import static android.widget.Toast.LENGTH_SHORT;

public class InspectionItemAddActivity extends AppCompatActivity {

    private EditText itemName;
    private EditText itemDescription;
    private EditText itemMethod;
    private EditText itemCondition;
    private EditText itemQuestion;

    private Button createButton;
    String selectedInspectionID;
    int itemsCount;
    ArrayList<String> items = new ArrayList<>();

    ImageView item_image_view;
    Button addImage_button;
    ProgressBar progressBar;

    Uri selectedImagePath;

    public static final int pickedImage = 0123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_item_add);

        itemName = (EditText) findViewById(R.id.item_name_edit_text);
        itemDescription = (EditText) findViewById(R.id.item_description_edit_text);
        itemMethod = (EditText) findViewById(R.id.item_method_edit_text);
        itemCondition = (EditText) findViewById(R.id.item_condition_edit_text);
        itemQuestion = (EditText) findViewById(R.id.item_question_edit_text);
        createButton = (Button) findViewById(R.id.item_create_button);
        addImage_button = (Button) findViewById(R.id.item_add_image_button);
        item_image_view = (ImageView) findViewById(R.id.item_image_view);
        progressBar = (ProgressBar) findViewById(R.id._item_progress_bar);

        hideImage();
        progressBar.setVisibility(View.INVISIBLE);

        // Deserialization
        selectedInspectionID = getIntent().getStringExtra("inspection_id");
        items = getIntent().getStringArrayListExtra("inspection_items");
        itemsCount = items.size();

        Date currentDate = new Date();

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createButton.setClickable(false);
                handleConditionsToCreateInspectionItem();
            }
        });


        addImage_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), pickedImage);
            }
        });

    }

    //photo picked from gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == pickedImage) {
            //TODO: action
            Uri imageUri = data.getData();
            selectedImagePath = data.getData();
            item_image_view.setImageURI(imageUri);
            showImage();
        }
    }


    private void handleConditionsToCreateInspectionItem(){
        String title = itemName.getText().toString();
        String description = itemDescription.getText().toString();
        String method = itemMethod.getText().toString();
        String condition = itemCondition.getText().toString();
        String question = itemQuestion.getText().toString();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(getBaseContext(),"Please fill in title", Toast.LENGTH_SHORT).show();
            createButton.setClickable(true);
        }else if (TextUtils.isEmpty(description)){
            Toast.makeText(getBaseContext(),"Please fill in description", Toast.LENGTH_SHORT).show();
            createButton.setClickable(true);
        }else if (TextUtils.isEmpty(method)){
            Toast.makeText(getBaseContext(),"Please fill in method", Toast.LENGTH_SHORT).show();
            createButton.setClickable(true);
        }else if (TextUtils.isEmpty(condition)){
            Toast.makeText(getBaseContext(),"Please fill in condition", Toast.LENGTH_SHORT).show();
            createButton.setClickable(true);
        }else if (TextUtils.isEmpty(question)){
            Toast.makeText(getBaseContext(),"Please fill in question", Toast.LENGTH_SHORT).show();
            createButton.setClickable(true);
        }else if (item_image_view.getDrawable() == null){
            Toast.makeText(getBaseContext(),"Please add an image", Toast.LENGTH_SHORT).show();
            createButton.setClickable(true);
        }else{

            Date currentDate = new Date();

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            InspectionItem item = new InspectionItem(title, description, method, condition, null, null,null,null, null, null, 0, null,question);

            createNewItem(item);
        }
    }

    private void createNewItem(InspectionItem object){

        progressBar.setVisibility(View.VISIBLE);

        //initiliaze firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");

        final CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");
        final CollectionReference itemsColl = inspectionsColl.document(selectedInspectionID).collection("items");

        itemsColl.add(object)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        String item_id = String.valueOf(documentReference.getId());

                        //update item id
                        documentReference.update("item_id", documentReference.getId());

                        //add array
                        items.add(item_id);
                        itemsCount += 1;
                        inspectionsColl.document(selectedInspectionID).update("inspection_items",items);
                        inspectionsColl.document(selectedInspectionID).update("inspection_items_count",itemsCount);

                        Log.d("Add Firestore", "DocumentSnapshot written with ID: " + documentReference.getId());

                        //progressBar.setVisibility(View.INVISIBLE);

                        uploadImageToStorage2(documentReference, item_id);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Log.w("Add Firestore", "Error adding document", e);

                        Toast.makeText(getBaseContext(),"Fail to add item", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void uploadImageToStorage(final DocumentReference docRef, final String item_id){
        //initialize storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

        // Create a child reference
        // imagesRef now points to "images"
        StorageReference imagesItemRef = storageRef.child("oNJZmUlwxGxAymdyKoIV").child("images").child("inspection_items");

        //store image to storage
        // Get the data from an ImageView as bytes
        item_image_view.setDrawingCacheEnabled(true);
        item_image_view.buildDrawingCache();
        Bitmap bitmap = item_image_view.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = imagesItemRef.child(item_id).putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getBaseContext(),"Fail to add item", Toast.LENGTH_SHORT).show();

                createButton.setClickable(true);

                progressBar.setVisibility(View.INVISIBLE);

                return;
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                docRef.update("item_id", item_id);
                docRef.update("item_photo", String.valueOf(downloadUrl));

                Toast.makeText(getBaseContext(),"Item Added", Toast.LENGTH_SHORT).show();

                createButton.setClickable(true);

                progressBar.setVisibility(View.INVISIBLE);

                finish();

                return;
            }
        });
    }

    private void uploadImageToStorage2(final DocumentReference docRef, final String item_id){
        //initialize storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

        // Create a child reference
        // imagesRef now points to "images"
        StorageReference imagesItemRef = storageRef.child("oNJZmUlwxGxAymdyKoIV").child("images").child("inspection_items");

        //store image to storage

        UploadTask uploadTask = imagesItemRef.child(item_id).putFile(selectedImagePath);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getBaseContext(),"Fail to add item", Toast.LENGTH_SHORT).show();

                createButton.setClickable(true);

                progressBar.setVisibility(View.INVISIBLE);

                return;
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                //taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                docRef.update("item_id", item_id);
                docRef.update("item_photo", String.valueOf(downloadUrl));

                Toast.makeText(getBaseContext(),"Item Added", Toast.LENGTH_SHORT).show();

                createButton.setClickable(true);

                progressBar.setVisibility(View.INVISIBLE);

                finish();

                return;
            }
        });
    }

    //buttons
    private void hideImage(){
        item_image_view.setVisibility(View.GONE);
        addImage_button.setVisibility(View.VISIBLE);
    }

    private void showImage(){
        item_image_view.setVisibility(View.VISIBLE);
        addImage_button.setVisibility(View.GONE);
    }


}
