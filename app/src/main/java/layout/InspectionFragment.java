package layout;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import devs.southpaw.com.inspectionpro.R;
import devs.southpaw.com.inspectionpro.RecyclerViewAdapterForInspection;
import objects.Inspection;
import objects.InspectionItem;


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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private LinearLayoutManager mLayoutManager;
    private RecyclerViewAdapterForInspection inspectionAdapter;
    RecyclerView recyclerView;

    private List<Inspection> inspectionsData = new ArrayList<>();

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
    public static InspectionFragment newInstance(String param1, String param2) {
        InspectionFragment fragment = new InspectionFragment();
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
        View view = inflater.inflate(R.layout.fragment_inspection, container, false);
        TextView mainText = (TextView) view.findViewById(R.id.mainTextView);

        recyclerView = view.findViewById(R.id.recycler_inspection);
        mLayoutManager = new LinearLayoutManager(this.getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        //populate data
        Inspection inspex1 = new Inspection("Pre Rig Up Drops Inspection Wireline Sheaves", "0001",1517288692,"keef", 1517288692, "null", 0);
        Inspection inspex2 = new Inspection("Inspection 2","0002", 1517294980, "", 1517294980, "null", 1);
        Inspection inspex3 = new Inspection(" Inspection 3","0003", 1517294980, "", 1517294980, "null", 2);

        InspectionItem item1 = new InspectionItem("i_01", "Sheave Hangar", "Suspended in Elevators", 0123, "Check for wear and tear and Elevator ID is sufficient for the size of the Hangar", "no comments", 1517294980, "user 1", 0);
        InspectionItem item2 = new InspectionItem("i_02", "Swivel", "Swivel Screws", 456, "Check all screws are made up on bottom of swivel", "fix it asap", 1517294980, "user 2", 1);
        InspectionItem item3 = new InspectionItem("i_03", "Sheave Pin", "Castle nut with split pin", 456, "Check Castle nut has been installed and split pin secured", "fix it asap", 1517294980, "user 3", 2);


        ArrayList<InspectionItem> itemsArray = new ArrayList<>();
        itemsArray.add(item1);
        itemsArray.add(item2);
        itemsArray.add(item3);

        inspex1.setInspectionItems(itemsArray);

        inspectionsData.add(inspex1);
        inspectionsData.add(inspex2);
        inspectionsData.add(inspex3);

        inspectionAdapter = new RecyclerViewAdapterForInspection(inspectionsData, this);

        recyclerView.setAdapter(inspectionAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        return view;
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        //item selection here
        Log.d("tapped2", "clicked2");
    }

    public interface OnFragmentInteractionListener {
        //void onFragmentInteraction(Uri uri);
    }
}
