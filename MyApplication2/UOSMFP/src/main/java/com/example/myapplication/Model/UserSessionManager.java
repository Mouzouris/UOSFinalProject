package com.example.myapplication.Model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.example.myapplication.Activities.LoginActivity;

import java.util.HashMap;

public class UserSessionManager{
    // Shared Preferences reference
    private SharedPreferences pref;
    // Editor reference for Shared preferences
    private Editor editor;
    // Context
    private Context _context;
    // Shared pref mode
    private int PRIVATE_MODE = 0;
    // Sharedpref file name
    private static final String PREFER_NAME = "AndroidExamplePref";
    // All Shared Preferences Keys
    private static final String IS_USER_LOGIN = "IsUserLoggedIn";
    // User name (make variable public to access from outside)
    public static final String KEY_NAME = "name";
    // Email address (make variable public to access from outside)
    public static final String KEY_EMAIL = "email";

    public static final String KEY_TYPE="type";

    public static final String KEY_APPROVED="approvedstatus";

    // Constructor
    public UserSessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREFER_NAME, PRIVATE_MODE);
        editor = pref.edit();

    }
        //Create login session
    public void createUserLoginSession(String name, String email,String approved){
        // Storing login value as TRUE
        editor.putBoolean(IS_USER_LOGIN, true);
        // Storing name in pref
        editor.putString(KEY_NAME, name);
        // Storing email in pref
        editor.putString(KEY_EMAIL, email);

        editor.putString(KEY_APPROVED, approved);
        // commit changes
        editor.commit();
    }
//    //Create login session
//    public void createUserLoginSession(String name, String email,String type){
//        // Storing login value as TRUE
//        editor.putBoolean(IS_USER_LOGIN, true);
//        // Storing name in pref
//        editor.putString(KEY_NAME, name);
//        // Storing email in pref
//        editor.putString(KEY_EMAIL, email);
//        // Storing type in pref
//        editor.putString(KEY_TYPE,type);
//        // commit changes
//        editor.commit();
//    }

    /**
     * Check login method will check user login status
     * If false it will redirect user to login page
     * Else do anything
     * */
    public boolean checkLogin(){
        // Check login status
        if(!this.isUserLoggedIn()){

            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, LoginActivity.class);

            // Closing all the Activities from stack
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);

            return true;
        }
        return false;
    }

    /**
     * Get stored session data
     * */
    public HashMap<String, String> getUserDetails(){

        //Use hashmap to store user credentials
        HashMap<String, String> user = new HashMap<String, String>();
        // user name
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        // user email id
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        // user type
        user.put(KEY_TYPE, pref.getString(KEY_TYPE, null));

        user.put(KEY_APPROVED, pref.getString(KEY_APPROVED, null));
        // return user
        return user;
    }

    /**
     * Clear session details
     * */
    public void logoutUser(){

        // Clearing all user data from Shared Preferences
        editor.clear();
        editor.commit();

        // After logout redirect user to Login Activity
        Intent i = new Intent(_context, LoginActivity.class);

        // Closing all the Activities
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add new Flag to start new Activity
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Staring Login Activity
        _context.startActivity(i);

    }
    public void updateSession(String type, String approved){
        // Storing type in pref
        editor.putString(KEY_TYPE,type);
        editor.putString(KEY_APPROVED,approved);
        // commit changes
        editor.commit();

    }


    // Check for login
    public boolean isUserLoggedIn(){
        return pref.getBoolean(IS_USER_LOGIN, false);
    }
    public boolean isUserInfoFull(){
        if(!pref.getString(KEY_TYPE,"").equals("") && (!pref.getString(KEY_APPROVED, "").equals("")))
            return true;
        return false;
    }
}
