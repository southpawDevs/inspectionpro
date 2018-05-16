package devs.southpaw.com.inspectionpro.accountLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.UploadTask;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;

import java.io.File;
import java.io.IOException;
import java.security.Key;
import java.text.SimpleDateFormat;
import java.util.Date;

import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.MainActivity;
import devs.southpaw.com.inspectionpro.R;
import objects.User;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AccountFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AccountFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AccountFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private EditText usernameET;
    private TextView emailTV;
    private TextView uid;
    private ImageView profilePicIV;
    private MaterialDialog mDialog;

    private Drawer result;

    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user = auth.getCurrentUser();

    FirebaseDatabase database;
    DatabaseReference usersReference;

    public AccountFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AccountFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AccountFragment newInstance(String param1, String param2) {
        AccountFragment fragment = new AccountFragment();
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

        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference("users");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        usernameET = (EditText) view.findViewById(R.id.username_edit_text);
        emailTV = (TextView) view.findViewById(R.id.email_text_view);
        profilePicIV = (ImageView) view.findViewById(R.id.profile_image);
        uid = (TextView) view.findViewById(R.id.uid_text_view);

        usernameET.setText(user.getDisplayName());
        emailTV.setText(user.getEmail());
        uid.setText(user.getUid());

        CollectionReference usersColl = FirebaseUtil.getUsersFromFirestore();
        usersColl.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                String path = "";
                if(documentSnapshot.exists()){
                    User userObj = documentSnapshot.toObject(User.class);
                    path = userObj.getProfile_picture();
                }
                Uri imagePath = Uri.parse(path);
                Glide.with(getActivity()).load(imagePath).centerCrop().into(profilePicIV);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                IconicsDrawable userIcon = new IconicsDrawable(getActivity())
                        .icon(GoogleMaterial.Icon.gmd_photo_camera)
                        .color(Color.parseColor("#303F9F"))
                        .sizeDp(80)
                        .paddingDp(18);
                profilePicIV.setImageDrawable(userIcon);
            }
        });

        usernameET.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                updateUsername(usernameET.getText().toString());
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                usernameET.setCursorVisible(false);
                return true;
            }

        });

        profilePicIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickImageDialog();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == pickedImage) {
            //TODO: action
            Uri imageUri = data.getData();
            profilePicIV.setImageURI(imageUri);
            updateProfileImage(imageUri);
        } else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            Uri uriImage = Uri.fromFile(new File(mCurrentPhotoPath));
            profilePicIV.setImageURI(uriImage);
            updateProfileImage(uriImage);
        }

        mDialog.dismiss();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
           // mListener.onFragmentInteraction(uri);
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
      //  void onFragmentInteraction(Uri uri);
    }

    private void updateUsername(String newName){

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("Firebase User", "User profile updated.");
                            Toast.makeText(getActivity(),"Username updated", Toast.LENGTH_SHORT).show();
                            MainActivity main = new MainActivity();
//                            ProfileDrawerItem itemProfile = new ProfileDrawerItem().withName(user.getDisplayName()).withEmail(user.getEmail()).withIcon(user.getPhotoUrl());
//                            main.result.updateItem(itemProfile);
                        }
                    }
                });
    }

    public void updateProfileImage(Uri filePathUrl){

        String path = "users/"+user.getUid()+"/"+user.getUid();
        Boolean uploadingImage = FirebaseUtil.uploadImageToStorageProperty(String.valueOf(filePathUrl), path , getActivity(), "profile_image", profilePicIV);

    }

    //choose photo

    String mCurrentPhotoPath;
    private int REQUEST_CODE = 1990;
    public static final int pickedImage = 1234;


    private void showPickImageDialog() {
        final MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(new MaterialSimpleListAdapter.Callback() {
            @Override
            public void onMaterialListItemSelected(MaterialDialog dialog, int index, MaterialSimpleListItem item) {
                // TODO
                if (index == 0 ){
                    dispatchTakePictureIntent();
                }else{
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto, pickedImage);

                    //choose from file manager
//                   Intent intent = new Intent();
//                                intent.setType("image/*");
//                                intent.setAction(Intent.ACTION_GET_CONTENT);
//                                startActivityForResult(Intent.createChooser(intent, "Select Picture"), pickedImage);
                }
            }
        });

        IconicsDrawable cameraIcon = new IconicsDrawable(getActivity())
                .icon(GoogleMaterial.Icon.gmd_photo_camera)
                .color(Color.WHITE)
                .sizeDp(11);

        IconicsDrawable galleryIcon = new IconicsDrawable(getActivity())
                .icon(GoogleMaterial.Icon.gmd_photo_album)
                .color(Color.WHITE)
                .sizeDp(11);


        adapter.add(new MaterialSimpleListItem.Builder(getActivity())
                .content("Open camera")
                .icon(cameraIcon)
                .iconPaddingDp(7)
                .backgroundColor(Color.parseColor("#ff4081"))
                .build());

        adapter.add(new MaterialSimpleListItem.Builder(getActivity())
                .content("Open gallery")
                .icon(galleryIcon)
                .iconPaddingDp(7)
                .backgroundColor(Color.parseColor("#F49A27"))
                .build());

        mDialog = new MaterialDialog.Builder(getActivity())
                .title("Pick an option:")
                .adapter(adapter, null)
                .show();
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "ProfilePicJPEG" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("Camera", "error creating file");
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "southpaw.dev.inspectionpro.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CODE);
            }
        }
    }
}
