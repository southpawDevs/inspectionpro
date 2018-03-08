package adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.facebook.imagepipeline.common.RotationOptions;

/**
 * Created by keith on 07/03/2018.
 */

public class SimpleRotationOptionsAdapter extends BaseAdapter {

    private final Entry[] SPINNER_ENTRIES = new Entry[]{
            new Entry(RotationOptions.disableRotation(), "disableRotation"),
            new Entry(RotationOptions.autoRotate(), "autoRotate"),
            new Entry(RotationOptions.autoRotateAtRenderTime(), "autoRotateAtRenderTime"),
            new Entry(RotationOptions.forceRotation(RotationOptions.NO_ROTATION), "NO_ROTATION"),
            new Entry(RotationOptions.forceRotation(RotationOptions.ROTATE_90), "ROTATE_90"),
            new Entry(RotationOptions.forceRotation(RotationOptions.ROTATE_180), "ROTATE_180"),
            new Entry(RotationOptions.forceRotation(RotationOptions.ROTATE_270), "ROTATE_270"),
    };

    @Override
    public int getCount() {
        return SPINNER_ENTRIES.length;
    }

    @Override
    public Object getItem(int position) {
        return SPINNER_ENTRIES[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());

        final View view = convertView != null
                ? convertView
                : layoutInflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);

        return view;
    }

    public class Entry {

        //final RotationOptions rotationOptions;
        final String description;
        public RotationOptions rotationOptions;

        private Entry(
                RotationOptions rotationOptions,
                String description) {
            this.rotationOptions = rotationOptions;
            this.description = description;
        }
    }
}
