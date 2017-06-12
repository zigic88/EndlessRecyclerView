package com.zigic.githubuser.utils;

import android.content.Context;

/**
 * Created by zigic on 12/06/17.
 */

public class Utils {

    public final static String getResString(Context context, int stringId) {
        return context.getApplicationContext().getResources().getString(stringId);
    }

}
