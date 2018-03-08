package devs.southpaw.com.inspectionpro;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.StringRes;
import android.text.format.DateFormat;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by keith on 07/03/2018.
 */

public final class UIUtil {

    private UIUtil() {
        throw new AssertionError();
    }

    public static String getStringDateFromDate(Date inputDate) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTime(inputDate);

        String date = DateFormat.format("dd-MMM-yyyy", cal).toString();
        return date;
    }

    public static IconicsDrawable getGMD(Context context, IIcon gmdIcon, int sizeDp, int paddingDp){

        IconicsDrawable icon = new IconicsDrawable(context)
                .icon(gmdIcon)
                .color(Color.WHITE)
                .backgroundColor(Color.GRAY)
                .sizeDp(sizeDp)
                .paddingDp(paddingDp);

        return icon;
    }
}
