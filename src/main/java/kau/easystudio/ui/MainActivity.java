package kau.easystudio.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ImageButton;
import android.graphics.Typeface;

import kau.easystudio.R;




public class MainActivity extends Activity {

    ActionBar mActionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mActionbar=getActionBar();
        mActionbar.setLogo(R.mipmap.ic_launcher);
        mActionbar.setDisplayUseLogoEnabled(true);
        mActionbar.setDisplayShowHomeEnabled(true);
        mActionbar.setTitle(Html.fromHtml("<font color='#726f6e'> VIDEO EDIT TYPE </font>"));
        mActionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.whitepatternbackground2));


        ImageButton selfbtn = (ImageButton) findViewById(R.id.selfedit);
        selfbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelfEditActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        ImageButton onetouchbtn = (ImageButton) findViewById(R.id.onetouch);
        onetouchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OneTouchEditActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }
}
