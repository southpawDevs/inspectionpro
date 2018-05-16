package layout;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.MainActivity;
import devs.southpaw.com.inspectionpro.R;
import adapters.RecyclerViewAdapterForInspection;
import devs.southpaw.com.inspectionpro.SharedPrefUtil;
import objects.Inspection;
import objects.InspectionItem;
import objects.User;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InspectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InspectionFragment extends Fragment implements RecyclerViewAdapterForInspection.RecyclerItemClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_ADMIN = "admin";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Boolean mParamAdmin;

    private LinearLayoutManager mLayoutManager;
    private RecyclerViewAdapterForInspection inspectionAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshContainer;

    private Boolean adminRights;

    private Boolean refreshing = false;

    public InspectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment InspectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static InspectionFragment newInstance(String param1, String param2, Boolean admin) {
        InspectionFragment fragment = new InspectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putBoolean(ARG_ADMIN, admin);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParamAdmin = getArguments().getBoolean(ARG_ADMIN);
        }


        FirebaseUser user = FirebaseUtil.getFirebaseUser();

        final Context context = getContext();
        DocumentReference member = FirebaseUtil.getMembersFromProperty(getActivity(), user.getUid());
        member.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    String depID = documentSnapshot.getString("member_department_id");
                    getInspectionDataFromFireStore(refreshing, depID);
                }else{
                    new MaterialDialog.Builder(context)
                            .title("Not Assigned To Department")
                            .content("You are not assigned to any department. Please contact admin to be assigned in a department")
                            .positiveText(R.string.ok)
                            .show();
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_inspection, container, false);
        TextView mainText = (TextView) view.findViewById(R.id.mainTextView);

        //handle pull refreshing container
        refreshContainer = (SwipeRefreshLayout) view.findViewById(R.id.refresh_container);
        refreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshing = true;

                FirebaseUser user = FirebaseUtil.getFirebaseUser();

                DocumentReference member = FirebaseUtil.getMembersFromProperty(getActivity(), user.getUid());
                member.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String depID = documentSnapshot.getString("department_id");
                            getInspectionDataFromFireStore(refreshing, depID);
                        }else{
//                            new MaterialDialog.Builder(getContext())
//                                    .title("Not Assigned To Department")
//                                    .content("You are not assigned to any department. Please contact admin to be assigned in a department")
//                                    .positiveText(R.string.ok)
//                                    .negativeText(R.string.cancel)
//                                    .show();
                        }
                    }
                });

            }
        });

        refreshContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //handle recycler view
        recyclerView = view.findViewById(R.id.recycler_inspection);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        //item selection here
        Log.d("tapped2", "clicked2");
    }

    public interface OnFragmentInteractionListener {
        //void onFragmentInteraction(Uri uri);
    }

    private void getInspectionDataFromFireStore(final Boolean isRefreshing, String departmentID){

        refreshContainer.setRefreshing(true);
        final String FireStoreTAG = "firestoreTag";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");

        CollectionReference inspectionsColl = devHousePropertyDoc.collection("inspections");

        if (mParamAdmin == true){

            final List<Inspection> inspections = new ArrayList<Inspection>();

            inspectionsColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {

                            if (document != null) {

                                Inspection inspection = document.toObject(Inspection.class);
                                inspection.setInspection_id(document.getId());

                                //sort according to overdue
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(inspection.getInspection_submitted_at());

                                calendar.add(Calendar.DATE, inspection.getInspection_days());

                                Date supposeToCheckDate = new Date(calendar.getTimeInMillis());
                                Date now = new Date();

                                double diff = supposeToCheckDate.getTime() - now.getTime();
                                double diffHours = diff / (60 * 60 * 1000);
                                int hours = (int) diffHours;

                                inspection.setInspection_to_be_dued(hours);
                                inspections.add(inspection);


                            } else {
                                Log.d(FireStoreTAG, "No such document");
                                Toast.makeText(getContext(),"Fail to retrieve", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Log.d(FireStoreTAG, "Error getting documents: ", task.getException());
                        Toast.makeText(getContext(),"Couldn't refresh", Toast.LENGTH_SHORT).show();
                    }


                    Collections.sort(inspections, new Comparator<Inspection>(){
                        public int compare(Inspection obj1, Inspection obj2) {
                            // ## Ascending order

                            return Integer.valueOf(obj1.getInspection_to_be_dued()).compareTo(obj2.getInspection_to_be_dued()); // To compare integer values

                            // ## Descending order
                            // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                            // return Integer.valueOf(obj2.empId).compareTo(obj1.empId); // To compare integer values
                        }
                    });

                    inspectionAdapter = new RecyclerViewAdapterForInspection(inspections, getContext(), getActivity());

                    recyclerView.setAdapter(inspectionAdapter);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());

                    refreshContainer.setRefreshing(false);
                    if (isRefreshing == true){

                        refreshing = false;
                    }else{

                    }
                }

            });
        }else{
            Query query = inspectionsColl.whereEqualTo("inspection_department", departmentID);

            final List<Inspection> inspections = new ArrayList<Inspection>();

            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {

                            if (document != null) {

                                Inspection inspection = document.toObject(Inspection.class);
                                inspection.setInspection_id(document.getId());

                                //sort according to overdue
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(inspection.getInspection_submitted_at());

                                calendar.add(Calendar.DATE, inspection.getInspection_days());

                                Date supposeToCheckDate = new Date(calendar.getTimeInMillis());
                                Date now = new Date();

                                double diff = supposeToCheckDate.getTime() - now.getTime();
                                double diffHours = diff / (60 * 60 * 1000);
                                int hours = (int) diffHours;

                                inspection.setInspection_to_be_dued(hours);
                                inspections.add(inspection);


                            } else {
                                Log.d(FireStoreTAG, "No such document");
                                Toast.makeText(getContext(),"Fail to retrieve", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Log.d(FireStoreTAG, "Error getting documents: ", task.getException());
                        Toast.makeText(getContext(),"Couldn't refresh", Toast.LENGTH_SHORT).show();
                    }


                    Collections.sort(inspections, new Comparator<Inspection>(){
                        public int compare(Inspection obj1, Inspection obj2) {
                            // ## Ascending order

                            return Integer.valueOf(obj1.getInspection_to_be_dued()).compareTo(obj2.getInspection_to_be_dued()); // To compare integer values

                            // ## Descending order
                            // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                            // return Integer.valueOf(obj2.empId).compareTo(obj1.empId); // To compare integer values
                        }
                    });

                    inspectionAdapter = new RecyclerViewAdapterForInspection(inspections, getContext(), getActivity());

                    recyclerView.setAdapter(inspectionAdapter);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());

                    refreshContainer.setRefreshing(false);
                    if (isRefreshing == true){

                        refreshing = false;
                    }else{

                    }
                }

            });
        }

    }
}
