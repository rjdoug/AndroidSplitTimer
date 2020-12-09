package com.example.mytimer;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class Preferences {
    public static String TIMER_SAVE_LOCATION = "timers_pref";
    public static String TIMER_ITEMS = "timer_items";
    public static String MAIN_TIMER= "main_timer";
    public static String LOG = "log_pref";
    public static String LOG_LAST_RESET = "last_reset";

    public static void saveString(String location, Context mContext, String item, String name) {
        SharedPreferences preferences = mContext.getSharedPreferences(location, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(name, item);
        editor.apply();
    }

    public static String loadString(String location, Context mContext, String name) {
        SharedPreferences preferences = mContext.getSharedPreferences(location, MODE_PRIVATE);
        return preferences.getString(name, null);
    }

    // save given array to preferences
    public static void saveArray(PreferenceStopwatch[] array, String location, Context mContext, String arrayName) {
        SharedPreferences preferences = mContext.getSharedPreferences(location, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        // Gson allows us to store objects in shared preferences through conversion
        Gson gson = new Gson();
        // Update array length - 'arrayName_size' acts as an index ie overwrites
        editor.putInt(arrayName +"_size", array.length);
        for (int i = 0; i < array.length; i++){
            // Add strings to map, using name of array plus index as name
            String json = gson.toJson(array[i]);
            editor.putString(arrayName + "_" + i, json);
        }
        editor.apply();
    }

    // load and return array
    public static ArrayList<PreferenceStopwatch> loadArray(String location,
                                                           Context mContext, String arrayName){
        SharedPreferences preferences = mContext.getSharedPreferences(location, MODE_PRIVATE);
        // get size of array
        int size = preferences.getInt(arrayName + "_size", 0);
        ArrayList<PreferenceStopwatch> items = new ArrayList<>();
        Gson gson = new Gson();
        // convert array to arrayList
        for(int i = 0; i < size; i++)
            items.add(gson.fromJson(preferences.getString(arrayName + "_" + i, null),
                    PreferenceStopwatch.class));

        return items;
    }

    // Resets array size stored in preferences to zero and calls clearPreference
    public static void clearArray(String arrayName, Context mContext, String location){
        SharedPreferences.Editor preferences = mContext.getSharedPreferences(location,
                MODE_PRIVATE).edit();
        preferences.putInt(arrayName +"_size", 0);
        preferences.apply();
        clearPreference(mContext, location);
    }

    // Clears preferences of given location
    public static void clearPreference(Context mContext, String location){
        SharedPreferences.Editor preferences = mContext.getSharedPreferences(location,
                MODE_PRIVATE).edit();
        preferences.clear();
        preferences.apply();
    }
}

