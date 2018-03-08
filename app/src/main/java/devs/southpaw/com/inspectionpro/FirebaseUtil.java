package devs.southpaw.com.inspectionpro;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import adapters.RecyclerViewAdapterForActionItems;
import objects.ActionItems;
import objects.InspectionItem;
import objects.User;

/**
 * Created by keith on 08/03/2018.
 */

public class FirebaseUtil {

    static String TAG_Firestore = "Firestore";

    static FirebaseAuth auth = FirebaseAuth.getInstance();
    static FirebaseUser user = auth.getCurrentUser();
    static FirebaseDatabase firebaseDatabase;
    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static FirebaseStorage storage = FirebaseStorage.getInstance();
    static StorageReference storageRef = storage.getReference();

    static String assignedPropertyID;

    static Boolean imageUploadSuccess;

    private FirebaseUtil() {
        throw new AssertionError();
    }


    //Firebase Auth User
    public static FirebaseUser getFirebaseUser(){
        return user;
    }

    public static void savePropertyIDFromUser(final Context context){

        db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Log.d(TAG_Firestore, "DocumentSnapshot data: " + document.getData());

                        User user = document.toObject(User.class);
                        assignedPropertyID = user.getAssigned_property();

                        SharedPreferences sharedPref = SharedPrefUtil.getSharedPreferences(context);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(context.getString(R.string.property_id_key), assignedPropertyID);
                        editor.commit();
                    } else {
                        Log.d(TAG_Firestore, "No such document");
                    }
                } else {
                    Log.d(TAG_Firestore, "get failed with ", task.getException());
                }
            }
        });
    }

    //FIRESTORE
    public static String getFirestoreTag(){
        return TAG_Firestore;
    }

    public static DocumentReference getPropertyRefFromFirestore(Activity activity){
        String propertyID = SharedPrefUtil.getPropertyID(activity);
        DocumentReference propertyRef = db.collection("properties").document(propertyID);

        return propertyRef;
    }


    public static CollectionReference getUsersFromFirestore(){

        CollectionReference usersRef = db.collection("users");
        return usersRef;
    }

    public static void setUsersFromFirestore(User userObject){

        getUsersFromFirestore().document(user.getUid()).set(userObject)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG_Firestore, "DocumentSnapshot successfully written!");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG_Firestore, "Error writing document", e);
                    }
                });
    }

    public static String getStringDateFromDate(Date inputDate) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTime(inputDate);

        String date = DateFormat.format("dd-MMM-yyyy", cal).toString();
        return date;
    }

    public static CollectionReference getActionItems(final Activity activity){

        final CollectionReference actionItemsColl = getPropertyRefFromFirestore(activity).collection("actionItems");

        return  actionItemsColl;
    }

    public static CollectionReference getInspections(final Activity activity){

        final CollectionReference inspectionsColl = getPropertyRefFromFirestore(activity).collection("inspections");

        return  inspectionsColl;
    }



    //FIREBASE STORAGE

    public static StorageReference getStorageRef(Activity activity){
        return storageRef.child(SharedPrefUtil.getPropertyID(activity));
    }

    public static Boolean uploadImageToStorageProperty(String imageFilePath, final String childPathAfterImages, final Activity activity, final String toastCategory){

        StorageReference storagePath;

        String propertyID = SharedPrefUtil.getPropertyID(activity);

        storagePath = storageRef.child(propertyID).child("images");

        storagePath.child(childPathAfterImages);

        Uri file = Uri.fromFile(new File(imageFilePath));

        UploadTask uploadTask = storagePath.putFile(file);

        uploadTask
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(activity,"Fail to add " + toastCategory + exception.getLocalizedMessage() , Toast.LENGTH_LONG).show();
                    imageUploadSuccess = false;
                }
            })

            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //taskSnapshot.getMetadata() contains file metadata such as size, content-type, and     download URL.

                    imageUploadSuccess = true;

                    Uri downloadUrl = taskSnapshot.getDownloadUrl();

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setPhotoUri(downloadUrl)
                            .build();

                    //update Firebase Auth User
                    user.updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("Firebase User", "User profile updated.");
                                        Toast.makeText(activity,toastCategory + " updated", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                }
            });

        return imageUploadSuccess;
    }

}
