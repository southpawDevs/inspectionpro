package adapters;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import devs.southpaw.com.inspectionpro.R;
import objects.Inspection;

/**
 * Created by keith on 27/02/2018.
 */

public class RecyclerViewAdapterForArchive extends RecyclerView.Adapter<RecyclerViewAdapterForArchive.ViewHolder>{


    private List<Inspection> inspectionsArchive;

    final private RecyclerItemClickListener mOnClickListener;

    public interface RecyclerItemClickListener {
        void onListItemClick(int clickedItemIndex);

    }

    public RecyclerViewAdapterForArchive(List<Inspection> inspectionsArchive) {
        this.inspectionsArchive = inspectionsArchive;
        mOnClickListener = null;
    }

    @Override
    public RecyclerViewAdapterForArchive.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_archive_layout, parent, false);


        ViewHolder viewHolder = new ViewHolder(itemLayoutView);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapterForArchive.ViewHolder holder, int position) {

        Inspection currentInspection = inspectionsArchive.get(position);

        holder.inspectionTitle.setText(currentInspection.getInspection_name());
    }

    @Override
    public int getItemCount() {
        if (inspectionsArchive.size() == 0){
            return 0;
        }else {
            return inspectionsArchive.size();
        }
    }

    // inner class to hold a reference to each item of RecyclerView
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView inspectionTitle;
        public TextView inspectionSubmitted;
        public CardView cardBackground;

        private final Context context;


        public ViewHolder(View itemLayoutView) {
            super(itemLayoutView);

            //populate viewholder data accordingly
            inspectionTitle = itemLayoutView.findViewById(R.id.title_inspection_archive);
            cardBackground = itemLayoutView.findViewById(R.id.card_view_archive);
            inspectionSubmitted = itemLayoutView.findViewById(R.id.submitted_inspection_archive);

            itemLayoutView.setOnClickListener(this);
            context = itemLayoutView.getContext();
        }

        @Override
        public void onClick(View v) {


        }
    }

}
