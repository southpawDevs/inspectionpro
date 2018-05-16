package devs.southpaw.com.inspectionpro;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.StringRes;
import android.text.format.DateFormat;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toolbar;

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

    public static String getStringDateAndTimeFromDate(Date inputDate) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTime(inputDate);

        String date = DateFormat.format("dd-MMM-yyyy hh:mm aa", cal).toString();
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

    public static IconicsDrawable getGMD(Context context, IIcon gmdIcon, int sizeDp, int paddingDp, int tintColor){

        IconicsDrawable icon = new IconicsDrawable(context)
                .icon(gmdIcon)
                .color(context.getResources().getColor(tintColor))
                .sizeDp(sizeDp)
                .paddingDp(paddingDp);

        return icon;
    }
    public static void setStatusAndActionBarPrimaryColor(Activity activity, android.support.v7.widget.Toolbar toolbar){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //orange 500
            window.setStatusBarColor(activity.getResources().getColor(R.color.colorPrimaryDark));
            toolbar.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
        }
    }

    public static void setStatusAndActionBarDeepOrangeColor(Activity activity, android.support.v7.widget.Toolbar toolbar){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            //orange 500
            window.setStatusBarColor(activity.getResources().getColor(R.color.colorDarkDeepOrange));
            toolbar.setBackgroundColor(activity.getResources().getColor(R.color.colorDeepOrange));
        }
    }
}
