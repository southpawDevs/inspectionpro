package devs.southpaw.com.inspectionpro.myRigLayout;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.zxing.WriterException;

import java.util.ArrayList;
import java.util.List;

import adapters.ListViewAdapterForDepartments;
import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.R;
import devs.southpaw.com.inspectionpro.SharedPrefUtil;
import objects.Department;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link viewpager_department.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link viewpager_department#newInstance} factory method to
 * create an instance of this fragment.
 */
public class viewpager_department extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ListView mListView;

    List<Department> departmentList = new ArrayList<>();

    public viewpager_department() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment viewpager_department.
     */
    // TODO: Rename and change types and number of parameters
    public static viewpager_department newInstance(String param1, String param2) {
        viewpager_department fragment = new viewpager_department();
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
        View view = inflater.inflate(R.layout.fragment_viewpager_department, container, false);

        mListView = (ListView) view.findViewById(R.id.rig_list_view);

        CollectionReference coll = FirebaseUtil.getDepartmentsFromFirestore(getActivity());

        coll.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (DocumentSnapshot document : task.getResult()) {

                        if (document != null) {
                            Department obj = document.toObject(Department.class);
                            departmentList.add(obj);
                        } else {

                            Toast.makeText(getActivity(),"Fail to retrieve departments", Toast.LENGTH_SHORT).show();
                        }
                    }

                    ListViewAdapterForDepartments adapter = new ListViewAdapterForDepartments(getContext(), departmentList);
                    mListView.setAdapter(adapter);

                } else {
                    Log.d("Firestore", "Error getting documents: ", task.getException());
                    Toast.makeText(getActivity(),"Couldn't refresh", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}
