package devs.southpaw.com.inspectionpro;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import layout.InspectionFragment;

public class MainActivity extends AppCompatActivity implements InspectionFragment.OnFragmentInteractionListener{

    private int mSelectedItem;
    private BottomNavigationView mBottomNav;
    private static final String SELECTED_ITEM = "arg_selected_item";

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBottomNav = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        mBottomNav.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        //load first fragment when start up
        MenuItem selectedItem;
        if (savedInstanceState != null) {
            mSelectedItem = savedInstanceState.getInt(SELECTED_ITEM, 0);
            //selectedItem = mBottomNav.getMenu().findItem(mSelectedItem);
            selectedItem = mBottomNav.getMenu().findItem(mSelectedItem);
        } else {
            //selectedItem = mBottomNav.getMenu().getItem(0);
            selectedItem = mBottomNav.getMenu().getItem(0);
        }
        selectFragment(selectedItem);
    }

    private void selectFragment(MenuItem item) {
        Fragment frag = null;
        // init corresponding fragment
        switch (item.getItemId()) {
            case R.id.navigation_inspection:
                frag = InspectionFragment.newInstance("title", "inspection");
                break;
            case R.id.navigation_action:
                frag = InspectionFragment.newInstance("title", "action");
                break;
            case R.id.navigation_archives:
                frag = InspectionFragment.newInstance("title", "archives");
                break;
        }

        // update selected item
        mSelectedItem = item.getItemId();

        updateToolbarText(item.getTitle());

        if (frag != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.container, frag, frag.getTag());
            transaction.commit();
        }
    }

    private void updateToolbarText(CharSequence text) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(text);
        }
    }
}
