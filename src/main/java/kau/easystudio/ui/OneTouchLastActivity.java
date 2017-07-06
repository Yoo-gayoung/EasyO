package kau.easystudio.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

import kau.easystudio.R;

public class OneTouchLastActivity extends Activity {

    ActionBar mActionbar;
    private Button Savebutton;
    private Button Homebutton;
    private MediaController mediaController;
    private VideoView videoView;
    int save=0;
    String videoName = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_touch_last);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        videoName = bundle.getString("VIDEONAME");
        Log.e("videoName",videoName);
        mActionbar=getActionBar();
        mActionbar.setLogo(R.mipmap.ic_launcher);
        mActionbar.setDisplayUseLogoEnabled(true);
        mActionbar.setDisplayShowHomeEnabled(true);
        mActionbar.setTitle(Html.fromHtml("<font color='#726f6e'> SAVE YOUR VIDEO </font>"));
        mActionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.whitepatternbackground2));


        View.OnClickListener listener = new View.OnClickListener() { //버튼이 클릭됐을 때
            @Override
            public void onClick(View v) {
                if(v.getId()==R.id.savebutton_last) {
                    videoView.pause();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(OneTouchLastActivity.this);
                    alertDialogBuilder.setMessage("SAVED");
                    alertDialogBuilder.setNegativeButton("OK", null);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.show();
                    save=1;
                    galleryAddPic();

                }else if(v.getId()==R.id.homebutton_last){
                    if(save==0){


                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(OneTouchLastActivity.this);
                        alertDialogBuilder
                                .setMessage("ARE YOU SURE YOU WANT TO LEAVE?")
                                .setPositiveButton("NO", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        galleryAddPic();
                                        Intent intentHome = new Intent(OneTouchLastActivity.this, MainActivity.class);
                                        intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intentHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intentHome);
                                        finish();
                                    }
                                })
                                .setNegativeButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(save==0) {
                                            fileDelete(videoName);
                                        }
                                        videoView.pause();
                                        Intent intentHome = new Intent(OneTouchLastActivity.this, MainActivity.class);
                                        intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intentHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(intentHome);
                                        finish();
                                    }
                                });

                        AlertDialog alertDialog = alertDialogBuilder.create();
                        alertDialog.setCanceledOnTouchOutside(true);
                        alertDialog.show();
                    }else{
                        Intent intentHome = new Intent(OneTouchLastActivity.this, MainActivity.class);
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intentHome);
                        finish();
                    }
                }
            }

        };

        Savebutton = (Button) findViewById(R.id.savebutton_last);
        Savebutton.setOnClickListener(listener);
        Homebutton = (Button) findViewById(R.id.homebutton_last);
        Homebutton.setOnClickListener(listener);



        videoView = (VideoView) findViewById(R.id.videoview_last);
        if (mediaController == null) {
            mediaController = new MediaController(OneTouchLastActivity.this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
        }
        try {
            videoView.setVideoPath(videoName);

        } catch (Exception e) {
            e.printStackTrace();
        }

        videoView.requestFocus();
        videoView.start();





    }
    public static void fileDelete(String deleteFileName) { //파일 삭제
        File file = new File(deleteFileName);
        file.delete();
    }

    private void galleryAddPic() { //갤러리 새로고침
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(videoName);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


}