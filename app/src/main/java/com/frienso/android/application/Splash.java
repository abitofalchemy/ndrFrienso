package com.frienso.android.application;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.parse.Parse;
import com.parse.ParseUser;
import com.parse.ui.ParseLoginActivity;
import com.parse.ui.ParseLoginBuilder;


public class Splash extends Activity {

    private final int LOGIN_ACTIVITY = 1;
    private final int MAIN_ACTIVITY = 2;

    private final int SPLASH_DISPLAY_LENGTH = 1000;
    private Context mContext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);
        mContext = this;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Parse.initialize(mContext, "ocxutehzKxd4EvmeODaNDl8AwJPYajzTK06QYkzZ", "G70qPOHHCFiUFUwBqbUJvqb2Fel8BrxcjBjntQEc");
                ParseUser currentUser = ParseUser.getCurrentUser();
                if (currentUser != null) {
                    startMainActivity();
                } else {
                    //take the user to login screen
                    ParseLoginBuilder builder = new ParseLoginBuilder(mContext);
                    startActivityForResult(builder.build(), LOGIN_ACTIVITY);
                }
            }
        }, SPLASH_DISPLAY_LENGTH);
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivityForResult(intent, MAIN_ACTIVITY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == LOGIN_ACTIVITY) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.i("udayan", "Login results successful, start the main activity");
            } else {
                finish();
            }
        } else if (requestCode == MAIN_ACTIVITY) {
            finish();
        }
    }
}
