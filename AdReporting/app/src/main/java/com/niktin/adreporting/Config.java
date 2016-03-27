package com.niktin.adreporting;

/**
 * Created by gpw on 3/20/2016.
 */
public class Config {
    public static final String DATA_URL = "http://www.learnhtml.provisor.in/android/credits/getData.php?email=";
    public static final String KEY_NAME = "name";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_VC = "vc";
    public static final String JSON_ARRAY = "result";
    //URL to our login.php file
    public static final String LOGIN_URL = "http://www.learnhtml.provisor.in/android_login_api/new_try/login.php";

    //Keys for email and password as defined in our $_POST['key'] in login.php
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PASSWORD = "password";

    //If server response is equal to this that means login is successful
    public static final String LOGIN_SUCCESS = "success";

    //Keys for Sharedpreferences
    //This would be the name of our shared preferences
    public static final String SHARED_PREF_NAME = "myloginapp";

    //This would be used to store the email of current logged in user
    public static final String EMAIL_SHARED_PREF = "email";

    //We will use this to store the boolean in sharedpreference to track user is loggedin or not
    public static final String LOGGEDIN_SHARED_PREF = "loggedin";

}