package devs.southpaw.com.inspectionpro.archiveLayout;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import adapters.RecyclerViewAdapterForArchive;
import adapters.RecyclerViewAdapterForInspection;
import devs.southpaw.com.inspectionpro.R;
import devs.southpaw.com.inspectionpro.SharedPrefUtil;
import objects.Inspection;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ArchiveFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ArchiveFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ArchiveFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private RecyclerView recyclerView;
    private LinearLayoutManager mLayoutManager;
    private RecyclerViewAdapterForArchive archiveAdapter;
    private SwipeRefreshLayout refreshContainer;
    private Boolean refreshing = false;

    private String pid;

    public ArchiveFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ArchiveFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ArchiveFragment newInstance(String param1, String param2) {
        ArchiveFragment fragment = new ArchiveFragment();
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

        pid = SharedPrefUtil.getPropertyID(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_archive, container, false);

        //handle pull refreshing container
        refreshContainer = (SwipeRefreshLayout) view.findViewById(R.id.refresh_container_archive);
        refreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshing = true;
                getArchiveDataFromFireStore(refreshing);
            }
        });

        refreshContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        recyclerView = view.findViewById(R.id.recycler_archive);
        mLayoutManager = new GridLayoutManager(this.getActivity(), 2);
        recyclerView.setLayoutManager(mLayoutManager);

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            //mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        getArchiveDataFromFireStore(true);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        //void onFragmentInteraction(Uri uri);
    }

    private void getArchiveDataFromFireStore(final Boolean isRefreshing){

        refreshContainer.setRefreshing(true);
        final String FireStoreTAG = "firestoreTag";

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference devHousePropertyDoc = db.collection("properties").document(pid);

        CollectionReference archivesColl = devHousePropertyDoc.collection("archives");

        final List<Inspection> inspections = new ArrayList<Inspection>();

        archivesColl.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {

                        if (document != null) {
                            Inspection inspectionArchive = document.toObject(Inspection.class);
                            inspectionArchive.setInspection_id(document.getId());
                            inspections.add(inspectionArchive);
                            if (inspections.size() != 0) {
                                Log.d(FireStoreTAG, inspectionArchive.getInspection_name());
                            }else{
                                Log.d(FireStoreTAG, inspectionArchive.getInspection_name());
                            }
                        } else {
                            Log.d(FireStoreTAG, "No such document");
                            Toast.makeText(getContext(),"Fail to retrieve", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Log.d(FireStoreTAG, "Error getting documents: ", task.getException());
                    Toast.makeText(getContext(),"Couldn't refresh", Toast.LENGTH_SHORT).show();
                }

                archiveAdapter = new RecyclerViewAdapterForArchive(inspections);

                recyclerView.setAdapter(archiveAdapter);
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
