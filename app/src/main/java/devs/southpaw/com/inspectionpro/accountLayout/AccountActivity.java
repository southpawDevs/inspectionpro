package devs.southpaw.com.inspectionpro.accountLayout;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import devs.southpaw.com.inspectionpro.MainActivity;
import devs.southpaw.com.inspectionpro.R;
import devs.southpaw.com.inspectionpro.SplashScreenActivity;

public class AccountActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        toolbar = (Toolbar) findViewById(R.id.toolbar_account);
        handleNavigationDrawer();

        toolbar.setTitle("Account");

        profilePic = (ImageView) findViewById(R.id.profile_image);

        IconicsDrawable profileDrawable = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_person)
                .color(Color.WHITE)
                .sizeDp(24);
        profilePic.setImageDrawable(profileDrawable);
    }

    private void handleNavigationDrawer(){
        new DrawerBuilder().withActivity(this).build();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Inspections");

        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.color.colorPrimaryDark)
                .withSelectionListEnabledForSingleProfile(true)
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

        Drawer result = new DrawerBuilder()
                .withAccountHeader(headerResult)
                .withActivity(this)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(
                        item1.withIcon(GoogleMaterial.Icon.gmd_home),
                        new PrimaryDrawerItem().withName("Account").withIcon(GoogleMaterial.Icon.gmd_person),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName("Settings").withIcon(GoogleMaterial.Icon.gmd_settings),
                        new PrimaryDrawerItem().withName("Help & feedback").withIcon(GoogleMaterial.Icon.gmd_help)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        if (position == 1) {
                            Intent mainIntent = new Intent(getBaseContext(), MainActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);
                            return  true;
                        }else if (position == 2){
                            //already in this activity
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

                Intent loginIntent = new Intent(getBaseContext(), SplashScreenActivity.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(loginIntent);

                return  true;
            }
        }));

    }
}
