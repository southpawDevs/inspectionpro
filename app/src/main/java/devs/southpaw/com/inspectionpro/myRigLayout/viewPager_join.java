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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.zxing.WriterException;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.R;
import devs.southpaw.com.inspectionpro.SharedPrefUtil;
import devs.southpaw.com.inspectionpro.R;

public class viewPager_join extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ListView mListView;
    private TextView installationName;
    private ImageView qrImage;

    public viewPager_join() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment viewPager_join.
     */
    // TODO: Rename and change types and number of parameters
    public static viewPager_join newInstance(String param1, String param2) {
        viewPager_join fragment = new viewPager_join();
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
        View view = inflater.inflate(R.layout.fragment_view_pager_join, container, false);

        installationName = view.findViewById(R.id.installation_name_tv);
        qrImage = view.findViewById(R.id.qr_code_image_view);

        DocumentReference name = FirebaseUtil.getPropertyRefFromFirestore(getActivity());

        name.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.exists()) {

                        String name = document.getData().get("name").toString();
                        installationName.setText("Installation Name: " + name);

                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });

        generateQRCodeID();
        return view;
    }

    private void generateQRCodeID(){

        String propertyID = SharedPrefUtil.getPropertyID(getActivity());

        // Initializing the QR Encoder with your value to be encoded, type you required and Dimension
        int qrDimension = 800;
        QRGEncoder qrgEncoder = new QRGEncoder(propertyID.trim(), null, QRGContents.Type.TEXT, qrDimension);
        try {
            // Getting QR-Code as Bitmap
            Bitmap qrBitmap = qrgEncoder.encodeAsBitmap();
            // Setting Bitmap to ImageView
            qrImage.setImageBitmap(qrBitmap);
        } catch (WriterException e) {
            Log.v("QRGENERATOR", e.toString());
        }
    }
}
