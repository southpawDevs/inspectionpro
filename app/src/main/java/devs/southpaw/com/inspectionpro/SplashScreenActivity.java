package devs.southpaw.com.inspectionpro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.decoder.SimpleProgressiveJpegConfig;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.URI;
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
            Intent mainActivityIntent = new Intent(this, MainActivity.class);
            mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(mainActivityIntent);
            finish();
        }else{
            // Choose authentication providers
            List<AuthUI.IdpConfig> providers = Arrays.asList(
                    new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                    new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build());

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

        if (requestCode == RC_SIGN_IN) {
            finish();
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                FirebaseUtil.savePropertyIDFromUser(getApplicationContext());

                String pid = SharedPrefUtil.getPropertyID(this);
                registerUserToFirestore(pid);

                //log in to main screen
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();

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

        FirebaseUtil.setUsersFromFirestore(registerUser);
    }


}
