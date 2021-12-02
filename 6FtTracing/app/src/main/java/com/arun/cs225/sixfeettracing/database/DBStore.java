package com.arun.cs225.sixfeettracing.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DBStore {
    private SharedPreferences sharedPreferences;

    private static final String KEY = "CS225_KEY";

    public DBStore(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void write(JSONObject obj) throws JSONException {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray existingObj = new JSONArray();
        if(sharedPreferences.contains(KEY)) {
            existingObj = new JSONArray(sharedPreferences.getString(KEY, null));
        }
        existingObj.put(obj);
        editor.putString(KEY, existingObj.toString());
        editor.commit();
    }

    public String read() {
        return sharedPreferences.getString(KEY, null);
    }

    public void flush() {
        sharedPreferences.edit().clear().apply();
    }

}
