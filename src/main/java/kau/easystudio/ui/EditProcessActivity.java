package kau.easystudio.ui;


import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.ImageButton;


import java.io.IOException;

import kau.easystudio.R;




public class EditProcessActivity extends Activity {
    static final int NUMIVL = 10;
    Context context = null;
    int [][] numOfFaces;
    int [][] timeSection;
    long start,end;
    private AsyncTask<Void, Void, Void> editProcess;
    public static ProgressDialog progressDialog;
    public static ProgressDialog progressDialog2;
    private String completedVideoName = "";


    ActionBar mActionbar;
    private long segmentFrom = 0;
    private long segmentTo = 0;

    GetVideoInfo getVideoInfo;

    String [] path;
    String audioName = "";
    String [] uriInString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_process);
        this.context = this;

        mActionbar=getActionBar();
        mActionbar.setLogo(R.mipmap.ic_launcher);
        mActionbar.setDisplayUseLogoEnabled(true);
        mActionbar.setDisplayShowHomeEnabled(true);
        mActionbar.setTitle(Html.fromHtml("<font color='#726f6e'> CREATING... </font>"));
        mActionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.whitepatternbackground2));
        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        path = b.getStringArray("uri");
        audioName = b.getString("musicname")+"_aac";
        editProcess = new EditProcess(path).execute();



    }

    private class EditProcess extends AsyncTask<Void,Void,Void>{
        String [] filepath;
        private EditProcess(String [] filepath){
            this.filepath = filepath;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(EditProcessActivity.this);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setMessage("Video Analyzing Process...");
            progressDialog2 = new ProgressDialog(EditProcessActivity.this);
            progressDialog2.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog2.setMessage("Crop & Merge Process...");
            progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    progressDialog2.show();
                }
            });
            progressDialog.show();

            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... params) {
            GetVideoInfo getVideoInfo = new GetVideoInfo(this.filepath,context);
            getVideoInfo.setVideoMetadata();
            int [] duration = getVideoInfo.getVideoDuration();
            FragmentList [] fragmentLists = getVideoInfo.getFragmentLists();
            int total = 0;
            int num = 0;
            for(int i = 0 ; i < fragmentLists.length;i++){
                for(int j = 0 ; j < fragmentLists[i].getResultFrom().size();j++){
                    num++;
                }
            }


            try {
                CropAndMerge cropAndMerge = new CropAndMerge(context ,filepath, audioName,fragmentLists, num);
                completedVideoName = cropAndMerge.getVideoName();
            }catch (IOException e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialog2.dismiss();
            Intent intent = new Intent(EditProcessActivity.this, OneTouchLastActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("VIDEONAME",completedVideoName);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

}
