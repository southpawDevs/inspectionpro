package devs.southpaw.com.inspectionpro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

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

import java.util.Arrays;
import java.util.List;

import objects.User;

public class SplashScreenActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;

    FirebaseDatabase database;
    DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

                registerUserToRealtimeDatabase(user);

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

    void registerUserToRealtimeDatabase(final FirebaseUser firebaseUser){

        final String uid = firebaseUser.getUid();
        final String username = firebaseUser.getDisplayName();
        final String email = firebaseUser.getEmail();

        usersReference.addListenerForSingleValueEvent(new ValueEventListener() {

            Boolean admin;
            Boolean developer;

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(uid) == false){

                    Log.d("userEmail", email);
                    if (email == "inspectionpro.dev@gmail.com"){
                        admin = true;
                        developer = true;
                        User user = new User(uid, username, email, admin, developer);

                        usersReference.child(uid).setValue(user);
                    }else {

                        admin = false;
                        developer = false;
                        User user = new User(uid, username, email, admin, developer);

                        usersReference.child(uid).setValue(user);
                    }
                }
            }//end on data change

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //
            }
        });

    }

}
