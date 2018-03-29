package adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import devs.southpaw.com.inspectionpro.R;
import objects.Department;

public class ListViewAdapterForDepartments extends BaseAdapter{

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Department> mDataSource;

    private TextView departmentTitle;
    private CardView cardView;

    public ListViewAdapterForDepartments(Context context, List<Department> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDataSource.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Department department = mDataSource.get(position);

        View rowView = mInflater.inflate(R.layout.listview_departments, parent, false);

        departmentTitle = (TextView) rowView.findViewById(R.id.department_list_name_tv);
        cardView = (CardView) rowView.findViewById(R.id.department_cv);

        departmentTitle.setText(department.getDepartment_name());
        cardView.setCardBackgroundColor(Color.parseColor(department.getDepartment_color_hex()));

        return rowView;
    }
}
