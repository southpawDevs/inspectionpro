package devs.southpaw.com.inspectionpro;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

/**
 * Created by keith on 08/03/2018.
 */

public class SharedPrefUtil {

    static SharedPreferences sharedPref;

    private SharedPrefUtil() {
        throw new AssertionError();
    }

    public static SharedPreferences getSharedPreferences(Context context){

        sharedPref = context.getSharedPreferences(
                context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        return sharedPref;
    }

    @NonNull
    public static String getPropertyID(Activity activity){
        sharedPref = getSharedPreferences(activity);
        String defaultValue = activity.getResources().getString(R.string.default_property_id);
        String propertyID = sharedPref.getString(activity.getString(R.string.property_id_key), defaultValue);

        return propertyID;
    }

    public static Boolean getAdminRights(Activity activity){
        sharedPref = getSharedPreferences(activity);
        Boolean defaultValue = false;
        Boolean adminRights = sharedPref.getBoolean(activity.getString(R.string.admin_rights_key), defaultValue);

        return adminRights;
    }

    public static void logOutRemoveSharedPref(Activity activity){
        SharedPreferences.Editor editor = sharedPref.edit().remove(activity.getString(R.string.property_id_key)).remove(activity.getString(R.string.admin_rights_key)).remove(activity.getString(R.string.developer_rights_key));
        editor.clear();
        editor.commit();
    }



}
