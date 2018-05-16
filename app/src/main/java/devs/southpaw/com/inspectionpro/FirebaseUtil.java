package devs.southpaw.com.inspectionpro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
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

public class FirebaseUtil extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIIDService";

    static String TAG_Firestore = "Firestore";

    static FirebaseAuth auth = FirebaseAuth.getInstance();
    static FirebaseDatabase firebaseDatabase;
    static FirebaseFirestore db = FirebaseFirestore.getInstance();
    static FirebaseStorage storage = FirebaseStorage.getInstance();
    static StorageReference storageRef = storage.getReference();

    static String assignedPropertyID;
    static Boolean adminRights;
    static Boolean develeporRights;

    static Boolean imageUploadSuccess;

    private FirebaseUtil() {
        throw new AssertionError();
    }

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    //Firebase Auth User
    public static FirebaseUser getFirebaseUser(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user;
    }

    public static void saveDataToSharedPref(final Context context){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {
                        Log.d(TAG_Firestore, "DocumentSnapshot data: " + document.getData());

                        User user = document.toObject(User.class);
                        assignedPropertyID = user.getAssigned_property();
                        adminRights = user.getAdmin_rights();
                        develeporRights = user.getDeveloper_rights();

                        SharedPreferences sharedPref = SharedPrefUtil.getSharedPreferences(context);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(context.getString(R.string.property_id_key), assignedPropertyID);
                        editor.putBoolean(context.getString(R.string.admin_rights_key), adminRights);
                        editor.putBoolean(context.getString(R.string.developer_rights_key), develeporRights);
                        editor.apply();
                    } else {
                        Log.d(TAG_Firestore, "No such document");
                    }
                } else {
                    Log.d(TAG_Firestore, "get failed with ", task.getException());
                }
            }
        });
    }

    public static void saveDataToSharedPrefAndIntent(DocumentSnapshot documentRef, final Context context, final Intent intent){

        User user = documentRef.toObject(User.class);
        assignedPropertyID = user.getAssigned_property();
        adminRights = user.getAdmin_rights();
        develeporRights = user.getDeveloper_rights();

        SharedPreferences sharedPref = SharedPrefUtil.getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.property_id_key), assignedPropertyID);
        editor.putBoolean(context.getString(R.string.admin_rights_key), adminRights);
        editor.putBoolean(context.getString(R.string.developer_rights_key), develeporRights);
        editor.apply();

        context.startActivity(intent);
    }

    public static void registerPropertyToSharedPref(final Context context, String pid){

        SharedPreferences sharedPref = SharedPrefUtil.getSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(context.getString(R.string.property_id_key), pid);
        editor.commit();
    }

    //FIRESTORE
    public static String getFirestoreTag(){
        return TAG_Firestore;
    }

    public static DocumentReference getPropertyRefFromFirestore(Activity activity){
        String propertyID = SharedPrefUtil.getPropertyID(activity);

        Log.d("PROPERTY_SP", propertyID);
        DocumentReference propertyRef = db.collection("properties").document(propertyID);
        return propertyRef;
    }

    public static CollectionReference getDepartmentsFromFirestore(Activity activity){

        CollectionReference departmentRef = getPropertyRefFromFirestore(activity).collection("departments");
        return departmentRef;
    }

    public static CollectionReference getMembersFromFirestore(Activity activity){

        CollectionReference departmentRef = getPropertyRefFromFirestore(activity).collection("allMembers");
        return departmentRef;
    }

    public static CollectionReference getUsersFromFirestore(){

        CollectionReference usersRef = db.collection("users");
        return usersRef;
    }

    public static void setUsersFromFirestore(User userObject, final Context context){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        getUsersFromFirestore().document(user.getUid()).set(userObject)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG_Firestore, "DocumentSnapshot successfully written!");
                FirebaseUtil.saveDataToSharedPref(context);
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

    public static DocumentReference getInspectionItem(final Activity activity, String inspectionID, String itemID){

        final CollectionReference inspectionsColl = getPropertyRefFromFirestore(activity).collection("inspections");

        return  inspectionsColl.document(inspectionID).collection("items").document(itemID);
    }

    public static DocumentReference getActionItem(final Activity activity, String aiID){

        final CollectionReference inspectionsColl = getPropertyRefFromFirestore(activity).collection("actionItems");

        return  inspectionsColl.document(aiID);
    }

    public static DocumentReference getMembersFromProperty(final Activity activity, String uid){

        final DocumentReference membersColl = getPropertyRefFromFirestore(activity).collection("allMembers").document(uid);

        return  membersColl;
    }


    //FIREBASE STORAGE
    public static StorageReference getStorageRef(Activity activity){
        return storageRef.child(SharedPrefUtil.getPropertyID(activity));
    }

    public static Boolean uploadImageToStorageProperty(String imageFilePath, final String childPathAfterImages, final Activity activity, final String toastCategory, ImageView imageView){

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        StorageReference storagePath;

        String propertyID = SharedPrefUtil.getPropertyID(activity);

        Uri file = Uri.fromFile(new File(imageFilePath));

        storagePath = storageRef.child(propertyID).child("images/"+childPathAfterImages+file.getLastPathSegment());

        imageView.setDrawingCacheEnabled(true);
        imageView.buildDrawingCache();
        Bitmap bitmap = imageView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storagePath.putBytes(data);

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
