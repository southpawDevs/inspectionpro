package devs.southpaw.com.inspectionpro.archiveLayout;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import adapters.RecyclerViewAdapterForItem;
import devs.southpaw.com.inspectionpro.R;
import objects.Inspection;
import objects.InspectionItem;

public class ArchiveItemsActivity extends AppCompatActivity {

    public Inspection selectedInspection;

    private List<InspectionItem> itemsData = new ArrayList<>();

    private LinearLayoutManager mLayoutManager;
    private RecyclerViewAdapterForItem itemsAdapter;
    private SwipeRefreshLayout refreshContainer;
    private Button submitInspectionButton;
    RecyclerView recyclerView;
    TextView completedCount;

    Boolean refreshing = false;

    private int currentItemsCount;
    private int completedItemsCount = 0;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference devHousePropertyDoc = db.collection("properties").document("oNJZmUlwxGxAymdyKoIV");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_items);


    }
}
