package adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.sectionedrecyclerview.SectionedRecyclerViewAdapter;
import com.afollestad.sectionedrecyclerview.SectionedViewHolder;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;

import java.util.ArrayList;
import java.util.List;

import devs.southpaw.com.inspectionpro.FirebaseUtil;
import devs.southpaw.com.inspectionpro.R;
import devs.southpaw.com.inspectionpro.UIUtil;
import objects.Department;
import objects.Members;


public class MainAdapter extends SectionedRecyclerViewAdapter<MainAdapter.MainVH> {

    List<Members> membersData = new ArrayList<>();
    List<Department> sectionData = new ArrayList<>();
    Context mContext;
    Activity mActivity;

    public  MainAdapter(List<Members> membersData,List<Department> sectionData, Context context, Activity activity){
        this.membersData = membersData;
        this.sectionData = sectionData;
        this.mContext = context;
        this.mActivity = activity;
    }

    @Override
    public int getSectionCount() {
//        return sectionData.size(); // number of sections, you would probably base this on a data set such as a map
        return sectionData.size();
    }

    @Override
    public int getItemCount(int sectionIndex) {
        // number of items in section, you could also pull this from a map of lists
        List<Members> filteredData = new ArrayList<>();
        List<Members> noDepartmentData = new ArrayList<>();

        if (TextUtils.equals(sectionData.get(sectionIndex).getDepartment_name(), "No Department")) {
            for (int i = 0; i < membersData.size(); i++) {
                if (TextUtils.equals(membersData.get(i).member_department_name, "") || TextUtils.equals(membersData.get(i).member_department_name, "No Department")) {
                    noDepartmentData.add(membersData.get(i));
                }
            }

            return noDepartmentData.size();
        }else {

            for (int i = 0; i < membersData.size(); i++) {

                if (TextUtils.equals(membersData.get(i).member_department_name, sectionData.get(sectionIndex).getDepartment_name())) {
                    filteredData.add(membersData.get(i));
                }
            }
            return filteredData.size();
        }

    }

    @Override
    public void onBindHeaderViewHolder(MainVH holder, int section, boolean expanded) {
        // Setup header view
        holder.title.setText(sectionData.get(section).getDepartment_name());
    }

    @Override
    public void onBindViewHolder(MainVH holder, int section, int relativePosition, int absolutePosition) {
        // Setup non-header view.
        // 'section' is section index.
        // 'relativePosition' is index in this section.
        // 'absolutePosition' is index out of all items, including headers and footers.
        // See sample project for a visual of how these indices work.

        List<Members> filteredData = new ArrayList<>();
        List<Members> noDepartmentData = new ArrayList<>();
        Members selectedMember;

        Log.d("Department", sectionData.get(section).department_name);
        if (TextUtils.equals(sectionData.get(section).getDepartment_name(), "No Department")) {
            for (int i = 0; i < membersData.size(); i++) {
                if (TextUtils.equals(membersData.get(i).member_department_name, "") || TextUtils.equals(membersData.get(i).member_department_name, "No Department")) {
                    noDepartmentData.add(membersData.get(i));
                }
            }
            selectedMember = noDepartmentData.get(relativePosition);
        } else {
            for (int i = 0; i < membersData.size(); i++) {
                if (TextUtils.equals(membersData.get(i).member_department_name, sectionData.get(section).getDepartment_name())) {
                    filteredData.add(membersData.get(i));
                }
            }
            selectedMember = filteredData.get(relativePosition);
        }

        holder.title2.setText(
                selectedMember.getMember_name());

        IconicsDrawable icon = UIUtil.getGMD(mContext, GoogleMaterial.Icon.gmd_account_circle,90,0,R.color.colorDarkGrey);
        Glide.with(mContext).load(selectedMember.getMember_profile_picture()).placeholder(icon).override(90,90).centerCrop().into(holder.image);
    }

    @Override
    public void onBindFooterViewHolder(MainVH holder, int section) {
        // Setup footer view, if footers are enabled (see the next section)
    }

    @Override
    public MainVH onCreateViewHolder(ViewGroup parent, int viewType) {
        // Change inflated layout based on type
        int layoutRes;
        switch(viewType) {
            case VIEW_TYPE_HEADER:
                layoutRes = R.layout.section_header_department;
                break;
            default:
                layoutRes = R.layout.section_item_department;
                break;
        }
        View v = LayoutInflater.from(parent.getContext())
                .inflate(layoutRes, parent, false);
        return new MainVH(v, this, mContext,mActivity,sectionData, membersData);
    }

    public static class MainVH extends SectionedViewHolder
            implements View.OnClickListener {

        final TextView title;
        final TextView title2;
        final ImageView image;
        final MainAdapter adapter;
        final Context mContext;
        final Activity mActivity;
        Toast toast;
        List<Department> sectionData = new ArrayList<>();
        List<Members> members = new ArrayList<>();

        public MainVH(View itemView, MainAdapter adapter, Context context, Activity activity, List<Department> arrayData, List<Members> members) {
            super(itemView);
            // Setup view holder. You'd want some views to be optional, e.g. the
            // header/footer will have views that normal item views do or do not have.
            itemView.setOnClickListener(this);

            this.title = itemView.findViewById(R.id.tvTitle);
            this.title2 = itemView.findViewById(R.id.tvItem);
            this.image = itemView.findViewById(R.id.imgItem);
            this.mContext = context;
            this.adapter = adapter;
            this.sectionData = arrayData;
            this.members = members;
            this.mActivity = activity;
        }

        @Override
        public void onClick(View view) {
            // SectionedViewHolder exposes methods such as:
            List<String> sectionString = new ArrayList<>();
            for (int i =0; i< sectionData.size(); i++){
                sectionString.add(sectionData.get(i).getDepartment_name());
            }

            if (isFooter()) {
                // ignore footer clicks
                return;
            }

            if (isHeader()) {
                adapter.toggleSectionExpanded(getRelativePosition().section());
            } else {
                if (toast != null) {
                    toast.cancel();
                }

                new MaterialDialog.Builder(mContext)
                        .title("Assign Department")
                        .items(sectionString)
                        .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                            @Override
                            public boolean onSelection(final MaterialDialog dialog, final View view, int which, CharSequence text) {

                                int section = getRelativePosition().section();
                                List<Members> filteredData = new ArrayList<>();
                                List<Members> noDepartmentData = new ArrayList<>();
                                final Members selectedMember;

                                //get selected Member
                                if (TextUtils.equals(sectionData.get(section).getDepartment_name(), "No Department")) {
                                    for (int i = 0; i <= members.size()-1; i++) {
                                        if (TextUtils.equals(members.get(i).member_department_name, "") || TextUtils.equals(members.get(i).member_department_name, "No Department")) {
                                            noDepartmentData.add(members.get(i));
                                        }
                                    }
                                    selectedMember = noDepartmentData.get(getRelativePosition().relativePos());
                                } else {
                                    for (int i = 0; i <= members.size()-1; i++) {
                                        if (TextUtils.equals(members.get(i).member_department_name, sectionData.get(section).getDepartment_name())) {
                                            filteredData.add(members.get(i));
                                        }
                                    }
                                    selectedMember = filteredData.get(getRelativePosition().relativePos());
                                }


                                toast = Toast.makeText(view.getContext(), "assigning " + selectedMember.getMember_name()+" to "+sectionData.get(dialog.getSelectedIndex()), Toast.LENGTH_SHORT);
                                toast.show();

                                DocumentReference docRef = FirebaseUtil.getMembersFromProperty(mActivity,selectedMember.getMember_id());

                                docRef.update("member_department_id", sectionData.get(dialog.getSelectedIndex()).department_id, "member_department_name", sectionData.get(dialog.getSelectedIndex()).department_name).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            toast = Toast.makeText(view.getContext(), "succesfully assigned " + selectedMember.getMember_name()+" to "+sectionData.get(dialog.getSelectedIndex()), Toast.LENGTH_SHORT);
                                            toast.show();
                                        }else{
                                            toast = Toast.makeText(view.getContext(), "fail to assign " + selectedMember.getMember_name()+" to "+sectionData.get(dialog.getSelectedIndex()), Toast.LENGTH_SHORT);
                                            toast.show();
                                        }
                                    }
                                });

                                return true;
                            }
                        })
                        .positiveText("Assign")
                        .show();
            }
        }
    }
}
