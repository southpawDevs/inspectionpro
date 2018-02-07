package layout;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

import devs.southpaw.com.inspectionpro.R;
import objects.Inspection;

public class InspectionAddActivity extends AppCompatActivity {

    private Button createButton;
    private EditText nameEditText;
    private EditText dayCountEditText;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_add);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        createButton = (Button) findViewById(R.id.create_inspection_button);
        nameEditText = (EditText) findViewById(R.id.name_edit_text);
        dayCountEditText = (EditText) findViewById(R.id.dayCount_edit_text);
        progressBar = (ProgressBar) findViewById(R.id.progressBarAdd);

        progressBar.setVisibility(View.INVISIBLE);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleConditionsToCreateInspection();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case android.R.id.home:
                Log.d("clicked", "action bar clicked");
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleConditionsToCreateInspection(){
        String title = nameEditText.getText().toString();
        String days = dayCountEditText.getText().toString();

        if (TextUtils.isEmpty(title)){
            Toast.makeText(getBaseContext(),"Please fill in title", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(days)){
            Toast.makeText(getBaseContext(),"Please fill in inspection days", Toast.LENGTH_SHORT).show();
        }else{
            createNewInspection();
        }
    }

    private void createNewInspection(){

        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");

        Date currentDate = new Date();

        String title = nameEditText.getText().toString();
        int daysCount = Integer.parseInt(dayCountEditText.getText().toString());

        Inspection newInspection = new Inspection(title, currentDate, user.getUid(), null, null, 0, 0, daysCount);
        devHousePropertyDoc.collection("inspections").add(newInspection)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        documentReference.update("inspection_id", documentReference.getId());
                        Log.d("Add Firestore", "DocumentSnapshot written with ID: " + documentReference.getId());
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getBaseContext(),"Inspection Added", Toast.LENGTH_SHORT).show();
                                finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Add Firestore", "Error adding document", e);
                        Toast.makeText(getBaseContext(),"Fail to add inspection", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

}
