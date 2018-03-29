package layout;

import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.R;
import objects.Department;
import objects.Inspection;

public class InspectionAddActivity extends AppCompatActivity {

    private Button createButton;
    private EditText nameEditText;
    private EditText dayCountEditText;
    private ProgressBar progressBar;
    private Spinner spinner;

    private String selectedDepartment;
    private String selectedDepartmentID;
    private ArrayList<String> departments = new ArrayList<>();
    private ArrayList<String> departmentsIDs = new ArrayList<>();

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
        spinner = (Spinner) findViewById(R.id.spinner);

        retrieveDepartments();

        progressBar.setVisibility(View.INVISIBLE);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(selectedDepartment)) {
                    Toast.makeText(getBaseContext(),"Please select department", Toast.LENGTH_SHORT).show();
                }else{
                    handleConditionsToCreateInspection();
                }
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

            Date currentDate = new Date();
            int daysCount = Integer.parseInt(dayCountEditText.getText().toString());

            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();

            //initialize newInspection Object
            Inspection newInspection = new Inspection(title, currentDate, user.getUid(), currentDate, user.getUid(), user.getUid(), user.getDisplayName(), currentDate,0, 0, daysCount,selectedDepartmentID);

            Log.d("Firebase User Auth", user.getUid());
            createNewInspection(newInspection);
        }
    }

    private void createNewInspection(Inspection object){

        progressBar.setVisibility(View.VISIBLE);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");

        devHousePropertyDoc.collection("inspections").add(object)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        //re-add firebase auto id to field
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

    private void retrieveDepartments(){
        CollectionReference coll = FirebaseUtil.getDepartmentsFromFirestore(this);

        coll.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {

                        if (document != null) {
                            //save department id & name
                            Department dep = document.toObject(Department.class);
                            String obj = document.getId();
                            departments.add(dep.getDepartment_name());
                            departmentsIDs.add(obj);
                        } else {

                            Toast.makeText(getBaseContext(),"Fail to retrieve departments", Toast.LENGTH_SHORT).show();
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>
                            (getBaseContext(), android.R.layout.simple_spinner_dropdown_item, departments);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedDepartment = departments.get(position);
                            selectedDepartmentID = departmentsIDs.get(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                } else {
                    Log.d("Firestore", "Error getting documents: ", task.getException());
                    Toast.makeText(getBaseContext(),"Couldn't refresh", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

}
