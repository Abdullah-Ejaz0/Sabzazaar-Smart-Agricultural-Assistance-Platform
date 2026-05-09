package com.example.sabzazaar.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.Locale;

public class LocaleHelper {

    public static Context setLocale(Context context, String language) {
        updateResources(context, language);
        return context;
    }

    public static Context setLocaleFromPreferences(Context context) {
        SharedPreferences sPref = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        String lang = sPref.getString("preferred_language", "en");
        return setLocale(context, lang);
    }

    private static void updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
}
