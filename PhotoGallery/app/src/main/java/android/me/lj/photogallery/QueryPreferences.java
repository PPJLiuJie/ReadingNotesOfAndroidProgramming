package android.me.lj.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Administrator on 2018/3/26.
 */

public class QueryPreferences {

    /**
     * 要获得SharedPreferences定制实例，可使用Context.getSharedPreferences (String,int)方法。
     * 然而，在实际开发中，我们并不关心SharedPreferences实例具体是什么样，只要它能共享于整个应用就可以了。
     * 这种情况下，最好使用PreferenceManager.getDefaultSharedPreferences(Context)方法，
     * 该方法会返回具有私有权限和默认名称的实例（仅在当前应用内可用）。
     */

    private static final String PREF_SEARCH_QUERY = "searchQuery";

    public static String getStoredQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);
    }

    public static void setStoredQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }

}
