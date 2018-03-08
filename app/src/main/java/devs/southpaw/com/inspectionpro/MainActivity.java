package devs.southpaw.com.inspectionpro;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
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

import devs.southpaw.com.inspectionpro.accountLayout.AccountFragment;
import devs.southpaw.com.inspectionpro.actionItemsLayout.ActionItemsFragment;
import devs.southpaw.com.inspectionpro.archiveLayout.ArchiveFragment;
import layout.InspectionAddActivity;
import layout.InspectionFragment;
import objects.Inspection;

public class MainActivity extends AppCompatActivity implements InspectionFragment.OnFragmentInteractionListener, ArchiveFragment.OnFragmentInteractionListener, ActionItemsFragment.OnFragmentInteractionListener, AccountFragment.OnFragmentInteractionListener{

    private int mSelectedItem;
    private BottomNavigationView mBottomNav;
    private Toolbar toolbar;
    private static final String SELECTED_ITEM = "arg_selected_item";
    private int REQUEST_CODE = 100;
    public Drawer result;

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
    protected void onResume() {
        super.onResume();
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
            mBottomNav.setSelectedItemId(0);
            selectFragment(selectedItem);
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CODE);
        }
    }

    private void selectFragment(MenuItem item) {
        Fragment frag = null;
        // init corresponding fragment
        switch (item.getItemId()) {
            case R.id.navigation_inspection:
                frag = InspectionFragment.newInstance("title", "inspection");
                toolbar.inflateMenu(R.menu.inspection_menu);
                break;
            case R.id.navigation_action:
                frag = ActionItemsFragment.newInstance("title", "action");
                toolbar.getMenu().clear();
                break;
            case R.id.navigation_archives:
                toolbar.getMenu().clear();
                frag = ArchiveFragment.newInstance("title", "archives");
                break;
        }

        // update selected item
        mSelectedItem = item.getItemId();

        updateToolbarText(item.getTitle());
        updateToolbarTitle(item.getTitle());

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

        switch ((int) id){
            case 1:
                //main
                frag = InspectionFragment.newInstance("title", "inspection");
                toolbar.inflateMenu(R.menu.inspection_menu);
                mBottomNav.setVisibility(View.VISIBLE);
                break;

            case 2:
                frag = AccountFragment.newInstance("title", "account");
                toolbar.getMenu().clear();
                mBottomNav.setVisibility(View.GONE);
                break;

            case 3:
                //my rig
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

    private void updateToolbarTitle(CharSequence text) {
        TextView title = (TextView) findViewById(R.id.title_toolbar);
        title.setText(text);
    }

    private void updateToolbarText(CharSequence text) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(text);
        }
    }

    public void handleNavigationDrawer(){
        final Activity activity = this;
        new DrawerBuilder().withActivity(this).build();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Inspections");


        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.colorPrimaryDark)
                .withSelectionListEnabledForSingleProfile(false)
                .addProfiles(
                        new ProfileDrawerItem().withName(user.getDisplayName()).withEmail(user.getEmail()).withIcon(user.getPhotoUrl())
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return true;
                    }
                })
                .build();

        result = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withSliderBackgroundColor(Color.parseColor("#FFFFFF"))
                .addDrawerItems(
                        item1.withIcon(GoogleMaterial.Icon.gmd_check),
                        new PrimaryDrawerItem().withIdentifier(3).withName("My Rig (developing)").withIcon(GoogleMaterial.Icon.gmd_home),
                        new PrimaryDrawerItem().withIdentifier(2).withName("Account").withIcon(GoogleMaterial.Icon.gmd_person),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withIdentifier(4).withName("Settings (developing)").withIcon(GoogleMaterial.Icon.gmd_settings),
                        new PrimaryDrawerItem().withIdentifier(5).withName("Help & feedback (developing)").withIcon(GoogleMaterial.Icon.gmd_help)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
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

         //to update only the name, badge, icon you can also use one of the quick methods
        //result.updateName(1, "A name");

        //the result object also allows you to add new items, remove items, add footer, sticky footer, ..
        result.addItem(new DividerDrawerItem());
        result.addStickyFooterItem(new PrimaryDrawerItem().withName("Log Out").withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
            @Override
            public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                //log out
                FirebaseAuth.getInstance().signOut();

                SharedPrefUtil.removePropertyID(activity);

                Intent loginIntent = new Intent(getBaseContext(), SplashScreenActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);

                return  true;
            }
        }));

    }


    public static void tintMenuIcon(Context context, MenuItem item, @ColorRes int color) {
        Drawable normalDrawable = item.getIcon();
        Drawable wrapDrawable = DrawableCompat.wrap(normalDrawable);
        DrawableCompat.setTint(wrapDrawable, context.getResources().getColor(color));

        item.setIcon(wrapDrawable);
    }

}
