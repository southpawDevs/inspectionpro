package devs.southpaw.com.inspectionpro;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

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

    public static String getPropertyID(Activity activity){
        sharedPref = getSharedPreferences(activity);
        String defaultValue = activity.getResources().getString(R.string.default_property_id);
        String propertyID = sharedPref.getString(activity.getString(R.string.property_id_key), defaultValue);

        return propertyID;
    }

    public static void removePropertyID(Activity activity){
        SharedPreferences.Editor editor = sharedPref.edit().remove(activity.getString(R.string.property_id_key));
    }



}
