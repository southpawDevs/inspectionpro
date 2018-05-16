package devs.southpaw.com.inspectionpro;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import objects.Members;

public class SignInActivity extends AppCompatActivity implements QRCodeReaderView.OnQRCodeReadListener {

    private static final int RC_SIGN_IN = 123;

    QRCodeReaderView qrReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        qrReader = (QRCodeReaderView) findViewById(R.id.qr_reader);
        qrReader.setOnQRCodeReadListener(this);

        // Use this function to enable/disable decoding
        qrReader.setQRDecodingEnabled(true);

        // Use this function to change the autofocus interval (default is 5 secs)
        qrReader.setAutofocusInterval(2000L);

        // Use this function to enable/disable Torch
        qrReader.setTorchEnabled(true);

        // Use this function to set back camera preview
        qrReader.setBackCamera();

    }

    @Override
    public void onQRCodeRead(final String text, PointF[] points) {

        final Activity activity = this;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("properties").document(text).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                final String pid = text;
                final FirebaseUser user = FirebaseUtil.getFirebaseUser();
                CollectionReference userColl = FirebaseUtil.getUsersFromFirestore();
                userColl.document(user.getUid()).update("assigned_property",pid).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseUtil.registerPropertyToSharedPref(getBaseContext(), pid);
                        final CollectionReference membersCollRef = FirebaseUtil.getPropertyRefFromFirestore(activity).collection("allMembers");

                        Members memberObj = new Members(user.getUid(), user.getDisplayName(), user.getEmail(), "","");
                        membersCollRef.document(user.getUid()).set(memberObj).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    SharedPreferences sharedPref = SharedPrefUtil.getSharedPreferences(getApplicationContext());
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString(getString(R.string.property_id_key), text);

                                    Intent mainActivityIntent = new Intent(activity, MainActivity.class);
                                    mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(mainActivityIntent);
                                    finish();
                                } else {

                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(activity, "Fail to join installation: "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity, "Invalid Installation", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        qrReader.startCamera();
    }

    @Override
    protected void onPause() {
        super.onPause();
        qrReader.stopCamera();
    }
}
