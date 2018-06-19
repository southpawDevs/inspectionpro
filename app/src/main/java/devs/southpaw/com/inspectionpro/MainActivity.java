package devs.southpaw.com.inspectionpro;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.context.IconicsLayoutInflater2;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import devs.southpaw.com.inspectionpro.accountLayout.AccountFragment;
import devs.southpaw.com.inspectionpro.actionItemsLayout.ActionItemsFragment;
import devs.southpaw.com.inspectionpro.archiveLayout.ArchiveFragment;
import devs.southpaw.com.inspectionpro.myRigLayout.MyRigFragment;
import layout.InspectionAddActivity;
import layout.InspectionFragment;
import objects.Inspection;
import objects.User;

public class MainActivity extends AppCompatActivity implements InspectionFragment.OnFragmentInteractionListener, ArchiveFragment.OnFragmentInteractionListener, ActionItemsFragment.OnFragmentInteractionListener, AccountFragment.OnFragmentInteractionListener, MyRigFragment.OnFragmentInteractionListener{

    private int mSelectedItem;
    private BottomNavigationView mBottomNav;
    private Toolbar toolbar;
    private TextView titleToolbar;
    private static final String SELECTED_ITEM = "arg_selected_item";
    private int REQUEST_CODE = 100;
    public Drawer result;
    private AccountHeader headerResult;
    //if you want to update the items at a later time it is recommended to keep it in a variable
    PrimaryDrawerItem inspecItem = new PrimaryDrawerItem().withIdentifier(1).withName("Inspections").withIcon(GoogleMaterial.Icon.gmd_check);
    PrimaryDrawerItem myRigItem = new PrimaryDrawerItem().withIdentifier(3).withName("My Rig").withIcon(GoogleMaterial.Icon.gmd_home);
    PrimaryDrawerItem accountItem = new PrimaryDrawerItem().withIdentifier(2).withName("Account").withIcon(GoogleMaterial.Icon.gmd_person);
    PrimaryDrawerItem settingsItem = new PrimaryDrawerItem().withIdentifier(4).withName("Settings (developing)").withIcon(GoogleMaterial.Icon.gmd_settings);
    PrimaryDrawerItem helpItem = new PrimaryDrawerItem().withIdentifier(5).withName("Help & feedback (developing)").withIcon(GoogleMaterial.Icon.gmd_help);

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_inspection:
                    selectFragment(item);
                    return true;
                case R.id.navigation_action:
                    selectFragment(item);
                    return true;
                case R.id.navigation_archives:
                    selectFragment(item);
                    return true;
            }
            return false;
        }

    };

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mSelectedItem = mBottomNav.getSelectedItemId();
    }

    @Override
    public void onBackPressed() {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        finish();
                    }
                })
                .title("Quit InspecPro?")
                .content("Clicking \"OK\" will quit the application")
                .positiveText("OK")
                .negativeText("Cancel")
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.inspection_menu);
        mBottomNav.setSelectedItemId(mSelectedItem);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LayoutInflaterCompat.setFactory2(getLayoutInflater(), new IconicsLayoutInflater2(getDelegate()));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomNav = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        mBottomNav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        titleToolbar = (TextView) findViewById(R.id.title_toolbar);
        //create the drawer and remember the `Drawer` result object
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.inflateMenu(R.menu.inspection_menu);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.inspection_create: //Your task
                        Intent createIntent = new Intent(getApplicationContext(), InspectionAddActivity.class);
                        startActivity(createIntent);
                        return true;

                    default:return false;
                }
            }
        });


        //set drawer
        handleNavigationDrawer();

        //load first fragment when start up
        MenuItem selectedItem;
        if (savedInstanceState != null) {

            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM, 0);
            mBottomNav.setSelectedItemId(mSelectedItem);
        } else {
            //selectedItem = mBottomNav.getMenu().getItem(0);
            selectedItem = mBottomNav.getMenu().getItem(0);
            mBottomNav.setSelectedItemId(R.id.navigation_inspection);
            selectFragment(selectedItem);
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CODE);
        }
    }

    private void selectFragment(MenuItem item) {
        Boolean admin = SharedPrefUtil.getAdminRights(this);

        Fragment frag = null;
        // init corresponding fragment
        switch (item.getItemId()) {
            case R.id.navigation_inspection:
                frag = InspectionFragment.newInstance("title", "inspection", admin);

                UIUtil.setStatusAndActionBarPrimaryColor(this, toolbar);
                toolbar.getMenu().clear();
                if (admin == true) {
                    toolbar.inflateMenu(R.menu.inspection_menu);
                }
                break;
            case R.id.navigation_action:
                frag = ActionItemsFragment.newInstance("title", "action");

                //UIUtil.setStatusAndActionBarDeepOrangeColor(this, toolbar);
                toolbar.getMenu().clear();
                break;
            case R.id.navigation_archives:

                UIUtil.setStatusAndActionBarPrimaryColor(this, toolbar);
                toolbar.getMenu().clear();
                frag = ArchiveFragment.newInstance("title", "archives");
                break;
        }

        // update selected item
        mSelectedItem = item.getItemId();

        updateToolbarText(item.getTitle());

        if (frag != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, frag, frag.getTag());
            transaction.commit();
            mBottomNav.setVisibility(View.VISIBLE);
        }
    }

    private void selectFragmentFromDrawer(long id) {
        Fragment frag = null;
        // init corresponding fragment
        //handle USER AVAILABITY
        Boolean admin = SharedPrefUtil.getAdminRights(this);

        switch ((int) id){
            case 1:
                //main
                frag = InspectionFragment.newInstance("title", "inspection",admin);
                toolbar.getMenu().clear();
                updateToolbarText("Inspections");
                if (admin == true) {
                    toolbar.inflateMenu(R.menu.inspection_menu);
                }

                MenuItem firstItemTab = mBottomNav.getMenu().getItem(0);
                mBottomNav.setSelectedItemId(firstItemTab.getItemId());
                mBottomNav.setVisibility(View.VISIBLE);
                break;

            case 2:
                frag = AccountFragment.newInstance("title", "account");
                updateToolbarText("Account");
                toolbar.getMenu().clear();
                mBottomNav.setVisibility(View.GONE);
                break;

            case 3:
                //my rig
                frag = MyRigFragment.newInstance("title", "My Rig");
                toolbar.getMenu().clear();
                updateToolbarText("My Rig");
                mBottomNav.setVisibility(View.GONE);
                break;
            case 4:
                //main
                break;

            case 5:
                break;
        }

        if (frag != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, frag, frag.getTag());
            transaction.commit();
            result.closeDrawer();
        }
    }

    private void updateToolbarText(CharSequence text) {
        titleToolbar.setText(text);
    }

    public void handleNavigationDrawer(){
        final Activity activity = this;

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();

        //get profile pic
        CollectionReference usersColl = FirebaseUtil.getUsersFromFirestore();
        usersColl.document(user.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                if(documentSnapshot.exists()){
                    User userObj = documentSnapshot.toObject(User.class);
                    buildHeaderDrawer(user, userObj.getProfile_picture());

                }else{
                    buildHeaderDrawer(user, "");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                buildHeaderDrawer(user, "");
            }
        });
    }

    private void buildHeaderDrawer(final FirebaseUser user, final String imagePath){
        final Activity mActivity= this;

        new DrawerBuilder().withActivity(this).build();

        // Create the AccountHeader
        if (TextUtils.equals(imagePath,"")){
            IconicsDrawable icon = UIUtil.getGMD(getApplicationContext(),GoogleMaterial.Icon.gmd_account_circle, 100, 0, R.color.colorGrey);
            headerResult = new AccountHeaderBuilder()
                    .withActivity(mActivity)
                    .withHeaderBackground(R.color.colorPrimaryDark)
                    .withSelectionListEnabledForSingleProfile(false)
                    .addProfiles(
                            new ProfileDrawerItem().withName(user.getDisplayName()).withEmail(user.getEmail()).withIcon(user.getPhotoUrl())
                    )
                    .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                        @Override
                        public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                            return false;
                        }
                    })
                    .build();
        }else {
            DrawerImageLoader.init(new AbstractDrawerImageLoader() {
                @Override
                public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                    super.set(imageView, uri, placeholder);

                    Glide.with(getApplicationContext()).load(imagePath).into(imageView);
                }
            });

            headerResult = new AccountHeaderBuilder()
                    .withActivity(mActivity)
                    .withHeaderBackground(R.color.colorPrimaryDark)
                    .withSelectionListEnabledForSingleProfile(false)
                    .addProfiles(
                            new ProfileDrawerItem().withName(user.getDisplayName()).withEmail(user.getEmail()).withIcon(imagePath)
                    )
                    .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                        @Override
                        public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                            return false;
                        }
                    })
                    .build();
        }

        //handle USER AVAILABILITY
        Boolean admin = SharedPrefUtil.getAdminRights(this);
        if (admin == true){
            result = buildAdminDrawer(result);
        }else{
            result = buildNormalDrawer(result);
        }

        //the result object also allows you to add new items, remove items, add footer, sticky footer, ..
        result.addItem(new DividerDrawerItem());
        result.addStickyFooterItem(new PrimaryDrawerItem().withName("Log Out").withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                //log out
                signOutFromFirebaseAuth();
                return  true;
            }
        }));
    }

    private Drawer buildAdminDrawer(Drawer drawer){
        drawer = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withSliderBackgroundColor(Color.parseColor("#FFFFFF"))
                .addDrawerItems(
                        inspecItem,
                        myRigItem,
                        accountItem,
                        new DividerDrawerItem(),
                        settingsItem,
                        helpItem
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                // do something with the clicked item :D
                long id = drawerItem.getIdentifier();
                if (id == 1) {
                    //inspection fragment / main activity
                    selectFragmentFromDrawer(id);
                    return true;
                }else if (id == 2){
                    //account fragment
                    selectFragmentFromDrawer(id);
                    return  true;
                }else if (id == 3){
                    //my rig fragment (only for admin)
                    selectFragmentFromDrawer(id);
                    return  true;
                }else if (id == 4){
                    //settings fragment
                    return false;
                }else if (id == 5) {
                    //help and feedback fragment
                    return false;
                }else{
                    return false;
                }
            }
        })
                .build();
        return drawer;
    }

    private Drawer buildNormalDrawer(Drawer drawer){
        drawer = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withSliderBackgroundColor(Color.parseColor("#FFFFFF"))
                .addDrawerItems(
                        inspecItem,
                        accountItem,
                        new DividerDrawerItem(),
                        settingsItem,
                        helpItem
                ).withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        long id = drawerItem.getIdentifier();
                        if (id == 1) {
                            //inspection fragment / main activity
                            selectFragmentFromDrawer(id);
                            return true;
                        }else if (id == 2){
                            //account fragment
                            selectFragmentFromDrawer(id);
                            return  true;
                        }else if (id == 3){
                            //my rig fragment (only for admin)
                            selectFragmentFromDrawer(id);
                            return  true;
                        }else if (id == 4){
                            //settings fragment
                            return false;
                        }else if (id == 5) {
                            //help and feedback fragment
                            return false;
                        }else{
                            return false;
                        }
                    }
                })
                .build();

        return drawer;
    }

    private void signOutFromFirebaseAuth(){
        final Activity activity = this;
        AuthUI.getInstance().signOut(this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    SharedPrefUtil.logOutRemoveSharedPref(activity);

                    FirebaseAuth.getInstance().signOut();

                    Intent loginIntent = new Intent(getBaseContext(), SplashScreenActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                }
            }
        });
    }


    public static void tintMenuIcon(Context context, MenuItem item, @ColorRes int color) {
        Drawable normalDrawable = item.getIcon();
        Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable, context.getResources().getColor(color));

        item.setIcon(wrapDrawable);
    }

}
