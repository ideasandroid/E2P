package com.ideasandroid.e2p;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class E2PActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = Prefs.get(this);
        if (prefs.getString("deviceRegistrationID", null) == null) {
            startActivity(new Intent(this, SetupActivity.class));
            finish();
        }else{
        	setContentView(R.layout.setup_complete);
        	TextView textView = (TextView) findViewById(R.id.setup_complete_text);
            textView.setText(Html.fromHtml(getString((R.string.setup_complete_text))));
            Button backButton = (Button) findViewById(R.id.back);
            Button finishButton = (Button) findViewById(R.id.finish);
            backButton.setVisibility(View.GONE);
            finishButton.setVisibility(View.GONE);
        }
    }
}