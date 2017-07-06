package kau.easystudio.ui;

import kau.easystudio.R;
import kau.easystudio.function.DialogRadio;
import kau.easystudio.function.MergeMP4WithAAC;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Log;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.os.AsyncTask;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Hemann Kim on 2016-05-30.
 */
public class ImportMusicActivity extends Activity {

    private Context context;
    private ProgressDialog progressDialog;
    private MediaPlayer mp;
    private Button btn;
    private String mp3Path;
    private String mp4Path;
    private String output;
    private String path;
    private String audiopath;
    private MediaController mediaController;
    private AsyncTask<Void, Void, Void> music;
    private VideoView videoView;
    private String musicname;

    private Button Savebutton;
    private Button Homebutton;

    int sel_music;
    int position;
    private File file;
    int save=0;

    ActionBar mActionbar;
    private final int SELECT_AUDIO = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_importmusic);

        mActionbar=getActionBar();
        mActionbar.setLogo(R.mipmap.ic_launcher);
        mActionbar.setDisplayUseLogoEnabled(true);
        mActionbar.setDisplayShowHomeEnabled(true);
        mActionbar.setTitle(Html.fromHtml("<font color='#726f6e'> SAVE YOUR VIDEO </font>"));
        mActionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.whitepatternbackground2));

        View.OnClickListener listener = new View.OnClickListener() { //버튼이 클릭됐을 때
            @Override
            public void onClick(View v) {
                if(v.getId()==R.id.savebutton) {
                    videoView.pause();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ImportMusicActivity.this);
                    alertDialogBuilder.setMessage("SAVED.");
                    alertDialogBuilder.setNegativeButton("OK", null);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.show();
                    save=1;
                    galleryAddPic();

                }else if(v.getId()==R.id.homebutton){
                    if(save==0){

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ImportMusicActivity.this);
                        alertDialogBuilder
                                .setMessage("ARE YOU SURE YOU WANT TO LEAVE?.")
                                .setPositiveButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        videoView.pause();
                                        galleryAddPic();
                                        Intent intentHome = new Intent(ImportMusicActivity.this, MainActivity.class);
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
                                            fileDelete(output);
                                        }
                                        videoView.pause();
                                        Intent intentHome = new Intent(ImportMusicActivity.this, MainActivity.class);
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
                        Intent intentHome = new Intent(ImportMusicActivity.this, MainActivity.class);
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intentHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(intentHome);
                        finish();
                    }
                }
            }

        };



        Savebutton = (Button) findViewById(R.id.savebutton);
        Savebutton.setOnClickListener(listener);
        Homebutton = (Button) findViewById(R.id.homebutton);
        Homebutton.setOnClickListener(listener);

        Intent intent = getIntent();
        Bundle bundleData = intent.getBundleExtra("MERGEPATH_DATA");
        path = bundleData.getString("MERGEPATH");
        sel_music = bundleData.getInt("music");
        position = bundleData.getInt("position");

        if(sel_music==1){
            if(position==0){
                musicname="dubstep_aac";
            }else if(position==1) {
                musicname="enigmatic_aac";
            }else if(position==2){
                musicname="memories_aac";
            }else if(position==3){
                musicname="tenderness_aac";
            }else if(position==4){
                musicname="thejazzpiano_aac";
            }else if(position==5){
                musicname="energy_aac";
            }else if(position==6){
                musicname="funkyelement_aac";
            }else if(position==7){
                musicname="funnysong_aac";
            }else if(position==8){
                musicname="sunny_aac";
            }else if(position==9){
                musicname="dance_aac";
            }else if(position==10) {
                musicname="moose_aac";
            }else if(position==11) {
                musicname="popdance_aac";
            }else if(position==12) {
                musicname="anewbeginning_aac";
            }else if(position==13) {
                musicname="clearday_aac";
            }else if(position==14) {
                musicname="goinghigher_aac";
            }else if(position==15) {
                musicname="littleidea_aac";
            }else if(position==16) {
                musicname="brightwish_aac";
            }else if(position==17) {
                musicname="greenhills_aac";
            }else if(position==18) {
                musicname="happyboytheme_aac";
            }else if(position==19) {
                musicname="snappy_aac";
            }else if(position==20) {
                musicname="happyrock_aac";
            }else if(position==21) {
                musicname="instrumental_aac";
            }else if(position==22) {
                musicname="stopping_aac";
            }else if(position==23) {
                musicname="tarantula_aac";
            }
        }

        File audioFile;
        audioFile = parseRawAudioFile(musicname);

        File videofile = new File(path);

        mp4Path = videofile.getAbsolutePath();

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory() + "/EasyO");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("IMicMic", "failed to create directory");
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "easyo" + timeStamp + ".mp4");

        output = mediaFile.getAbsolutePath();

        try {
            MergeMP4WithAAC.mux(mp4Path,audioFile, output);
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileDelete(path);

        videoView = (VideoView) findViewById(R.id.videoView);
        if (mediaController == null) {
            mediaController = new MediaController(ImportMusicActivity.this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
        }
        try {
            videoView.setVideoPath(output);

        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        videoView.requestFocus();
        videoView.start();

    }

    public static void fileDelete(String deleteFileName) { //파일 삭제
        File I = new File(deleteFileName);
        I.delete();
    }


    public File parseRawAudioFile (String audioName) {
        int resId;
        InputStream inputStream = null;
        OutputStream outputStream =null;
        File file = null;
        try{
            resId = getResources().getIdentifier(audioName,"raw","kau.easystudio");
            inputStream = getResources().openRawResource(resId);
            file = File.createTempFile("temp",".aac");
            outputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return file;

    }

    private void galleryAddPic() { //갤러리 새로고침
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(output);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}