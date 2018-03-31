package devs.southpaw.com.inspectionpro.actionItemsLayout;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import adapters.RecyclerViewAdapterForActionItems;
import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.R;
import objects.ActionItems;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ActionItemsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ActionItemsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActionItemsFragment extends Fragment implements RecyclerViewAdapterForActionItems.RecyclerItemClickListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private List<ActionItems> actionItemsData = new ArrayList<>();

    private LinearLayoutManager mLayoutManager;
    private RecyclerViewAdapterForActionItems itemsAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshContainer;
    private TextView pullTV;

    private Boolean refreshing = false;

    public ActionItemsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ActionItemsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ActionItemsFragment newInstance(String param1, String param2) {
        ActionItemsFragment fragment = new ActionItemsFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_action_items, container, false);

        pullTV = (TextView) view.findViewById(R.id.ai_pull_tv);

        //handle pull refreshing container
        refreshContainer = (SwipeRefreshLayout) view.findViewById(R.id.refresh_container_action_items);
        refreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshing = true;
                getActionItemsData();
            }
        });

        refreshContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //handle recycler view
        recyclerView = view.findViewById(R.id.recycler_action_items);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        getActionItemsData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        //getActionItemsData();
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
    public void onListItemClick(int clickedItemIndex) {

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

    private  void  getActionItemsData(){
        final Context context = getContext();
        refreshContainer.setRefreshing(true);
        itemsAdapter = new RecyclerViewAdapterForActionItems(actionItemsData, getContext(), getActivity());
        CollectionReference actionItemsColl = FirebaseUtil.getActionItems(getActivity());
        actionItemsColl.orderBy("item_reported_at", Query.Direction.DESCENDING).limit(40).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    if (task.getResult().isEmpty()){
                        refreshContainer.setRefreshing(false);
                        pullTV.setText("No Data");
                    }else{
                        pullTV.setText("pull to refresh");
                    }

                    List<ActionItems> tempArray = new ArrayList<>();
                    for (DocumentSnapshot document : task.getResult()) {

                        if (document != null) {
                            ActionItems item = document.toObject(ActionItems.class);
                            tempArray.add(item);
                            refreshContainer.setRefreshing(false);
                        } else {
                            refreshContainer.setRefreshing(false);
                            Log.d(FirebaseUtil.getFirestoreTag(), "No such document");
                            Toast.makeText(getActivity(), "Fail to retrieve", Toast.LENGTH_SHORT).show();
                        }
                    }

                    actionItemsData = tempArray;

                    itemsAdapter = new RecyclerViewAdapterForActionItems(actionItemsData, getActivity(), getActivity());
                    recyclerView.setAdapter(itemsAdapter);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());

                } else {
                    refreshContainer.setRefreshing(false);
                    Log.d(FirebaseUtil.getFirestoreTag(), "Error getting documents: ", task.getException());
                    Toast.makeText(getActivity(), "Couldn't refresh", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
