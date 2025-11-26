package com.proyecto.travelia.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    public static boolean isLoggedIn(Context context) {
        return getUserEmail(context) != null;
    }

    public static String getUserEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constantes.PREFS_USUARIO, Context.MODE_PRIVATE);
        String email = prefs.getString(Constantes.KEY_EMAIL, null);
        if (email != null && email.trim().isEmpty()) {
            return null;
        }
        return email;
    }

    public static String getUserName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constantes.PREFS_USUARIO, Context.MODE_PRIVATE);
        String name = prefs.getString(Constantes.KEY_NAME, null);
        return name != null ? name : "Viajero";
    }
}
