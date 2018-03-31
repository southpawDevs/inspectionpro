package devs.southpaw.com.inspectionpro;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.List;

import objects.User;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;

    FirebaseDatabase database;
    DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
                .setProgressiveJpegConfig(new SimpleProgressiveJpegConfig())
                .setResizeAndRotateEnabledForNetwork(true)
                .setDownsampleEnabled(true)
                .build();

        Fresco.initialize(this, config);

        super.onCreate(savedInstanceState);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");

        if (user != null) {
            // User is signed in (getCurrentUser() will be null if not signed in)

            //redirect page
            final Activity mActivity = this;

            String prefPID = SharedPrefUtil.getPropertyID(this);

            //if already stored PID in shared preferences
            if (TextUtils.isEmpty(prefPID)){

                //get PID from firestore
                CollectionReference users = FirebaseUtil.getUsersFromFirestore();
                users.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {
                            Intent mainIntent = new Intent(mActivity, MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            //startActivity(mainIntent);
                            FirebaseUtil.saveDataToSharedPrefAndIntent(documentSnapshot,getBaseContext(),mainIntent);
                            finish();
                        } else {
                            registerUserToFirestore("");
                            Intent signInIntent = new Intent(mActivity, SignInActivity.class);
                            signInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            FirebaseUtil.saveDataToSharedPrefAndIntent(documentSnapshot,getBaseContext(),signInIntent);
                            finish();
                        }
                    }
                });
            }else {
                CollectionReference users = FirebaseUtil.getUsersFromFirestore();
                users.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Intent mainActivityIntent = new Intent(mActivity, MainActivity.class);
                        mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        FirebaseUtil.saveDataToSharedPrefAndIntent(documentSnapshot,getBaseContext(),mainActivityIntent);
                        finish();
                    }
                });
            }

        }else{
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());

// Create and launch sign-in intent
            startActivityForResult(
                    AuthUI.getInstance(FirebaseApp.initializeApp(this))
                            .createSignInIntentBuilder()
                            .setAvailableProviders(providers)
                            .setTheme(R.style.AppThemeFirebaseAuth)
                            .setLogo(R.drawable.splash_screen_logo)
                            .build(),
                    RC_SIGN_IN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        final Activity mActivity = this;

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                CollectionReference users = FirebaseUtil.getUsersFromFirestore();
                final DocumentReference docRef = users.document(user.getUid());
                docRef.get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {
                            String pid = documentSnapshot.getString("assigned_property");

                            if(TextUtils.isEmpty(pid)){
                                Intent signInIntent = new Intent(mActivity, SignInActivity.class);
                                signInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                FirebaseUtil.saveDataToSharedPrefAndIntent(documentSnapshot,getBaseContext(),signInIntent);
                                finish();
                            }else {
                                Intent mainActivityIntent = new Intent(mActivity, MainActivity.class);
                                mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                FirebaseUtil.saveDataToSharedPrefAndIntent(documentSnapshot,getBaseContext(),mainActivityIntent);
                                finish();
                            }
                        } else {
                            registerUserToFirestore("");
                            Intent signInIntent = new Intent(mActivity, SignInActivity.class);
                            signInIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(signInIntent);
                            finish();
                        }
                    }
                });

            } else {
                // Sign in failed, check response for error code
                // ...
                Log.d("fail sign in", "fail sign in");
            }
        }
    }

    void registerUserToFirestore(String pid){
        FirebaseUser user = FirebaseUtil.getFirebaseUser();
        final String uid = user.getUid();
        final String username = user.getDisplayName();
        final String email = user.getEmail();
        final String photoUrl;

        Boolean admin = false;
        Boolean developer = false;
        if (email == "inspectionpro.dev@gmail.com"){
            admin = true;
            developer = true;
        }else{
            admin = false;
            developer = false;
        }

        if (user.getPhotoUrl() != null){
            photoUrl = String.valueOf(user.getPhotoUrl());
        }else{
            photoUrl = "";
        }

        User registerUser = new User(uid, username, email, admin, developer, photoUrl, pid);

        FirebaseUtil.setUsersFromFirestore(registerUser,getBaseContext());
    }
}
