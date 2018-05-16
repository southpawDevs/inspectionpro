package devs.southpaw.com.inspectionpro.myRigLayout;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.sectionedrecyclerview.ItemCoord;
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.afollestad.sectionedrecyclerview.SectionedViewHolder;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import adapters.MainAdapter;
import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.R;
import objects.Department;
import objects.Members;

public class viewpager_members extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private MainAdapter sectionAdapter;
    RecyclerView recyclerView;
    SwipeRefreshLayout refreshContainer;

    public viewpager_members() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment view_pager_members.
     */
    // TODO: Rename and change types and number of parameters
    public static viewpager_members newInstance(String param1, String param2) {
        viewpager_members fragment = new viewpager_members();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_viewpager_members, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.members_departmemt_rv);
        refreshContainer = (SwipeRefreshLayout) view.findViewById(R.id.members_refresh);

        refreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getMembers();
            }
        });

        refreshContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        getMembers();

        return view;
    }

    private void getMembers(){
        final List<Members> membersData = new ArrayList<>();
        final List<Department> departmentData = new ArrayList<>();

        final CollectionReference departmentRef = FirebaseUtil.getDepartmentsFromFirestore(getActivity());
       final CollectionReference memberRef = FirebaseUtil.getMembersFromFirestore(getActivity());

       refreshContainer.setRefreshing(true);
        departmentRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Department obj = new Department("", "", "");

                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Log.d("Firestore", document.getId() + " => " + document.getData());
                    Department departmentObj = document.toObject(Department.class);

                    if (TextUtils.equals(departmentObj.getDepartment_name(),"No Department")){
                        obj = departmentObj;
                    }else{
                        departmentData.add(departmentObj);
                    }
                }

                departmentData.add(obj);
                memberRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("Firestore", document.getId() + " => " + document.getData());
                                final Members memberObj = document.toObject(Members.class);

                                DocumentReference userDoc = FirebaseUtil.getUsersFromFirestore().document(memberObj.getMember_id());
                                userDoc.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String imagePath = documentSnapshot.getString("profile_picture");
                                        memberObj.setMember_profile_picture(imagePath);
                                        membersData.add(memberObj);
                                        sectionAdapter.expandAllSections();
                                        sectionAdapter.notifyDataSetChanged();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String imagePath = "";
                                        memberObj.setMember_profile_picture(imagePath);
                                        membersData.add(memberObj);
                                        sectionAdapter.expandAllSections();
                                        sectionAdapter.notifyDataSetChanged();
                                    }
                                });
                            }

                            sectionAdapter = new MainAdapter(membersData,departmentData, getContext(),getActivity());
                            GridLayoutManager manager =
                                    new GridLayoutManager(getContext(), 4);

                            sectionAdapter.setLayoutManager(manager);
                            sectionAdapter.shouldShowHeadersForEmptySections(true);
                            sectionAdapter.shouldShowFooters(false);
                            sectionAdapter.expandAllSections();
                            recyclerView.setLayoutManager(manager);
                            recyclerView.setAdapter(sectionAdapter);

                            refreshContainer.setRefreshing(false);
                        } else {
                            Log.d(FirebaseUtil.getFirestoreTag(), "Error getting documents: ", task.getException());
                            refreshContainer.setRefreshing(false);
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                refreshContainer.setRefreshing(false);
            }
        });

    }
}
