package devs.southpaw.com.inspectionpro;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

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
                FirebaseUser user = FirebaseUtil.getFirebaseUser();
                CollectionReference userColl = FirebaseUtil.getUsersFromFirestore();
                userColl.document(user.getUid()).update("assigned_property",pid).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseUtil.registerPropertyToSharedPref(getBaseContext(), pid);
                        Intent splash = new Intent(activity, SplashScreenActivity.class);
                        splash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        splash.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(splash);
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
