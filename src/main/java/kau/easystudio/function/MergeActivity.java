package kau.easystudio.function;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import kau.easystudio.function.DialogRadio.AlertPositiveListener;
import kau.easystudio.function.DialogRadio.MusicselListener;
import kau.easystudio.function.DialogRadio.NegativeListener;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;


import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AppendTrack;


import kau.easystudio.R;
import kau.easystudio.ui.ImportMusicActivity;
import kau.easystudio.ui.MainActivity;
import kau.easystudio.ui.ProjectActivity;

public class MergeActivity extends Activity  implements AlertPositiveListener,MusicselListener,NegativeListener { //implements OnClickListener
    public static final String TAG = "MP4PARSER";
    private Button Savebutton;
    private Button Musicbutton;
    private Button Homebutton;
    private Context context;
    private ArrayList<String> videosToMerge; //합성이 될 동영상 이름의 리스트
    private ArrayList<String> videospath; //합성이 될 동영상 경로 리스트
    private AsyncTask<String, Integer, String> mergeVideos; // 동영상 합성 작업
    private String workingPath; //동영상 작업 폴더

    private int i; //for 문

    private int videoposition = 0;
    private MediaController mediaController;
    private VideoView videoview;
    private String mergepath; //합성된 동영상의 경로
    private String savePath; // 합성된 동영상을 저장할 폴더 경로
    private String filePath;
    int save = 0;

    ActionBar mActionbar;
    int position = 0; // radio button 순서
    MediaPlayer mp;
    boolean isPlaying = false; //음악이 재생되고 있는지 확인하기 위해
    int music = 1;

    static final long[] ROTATE_270 = new long[]{0, -1, 1, 0, 0, 0, 1, 0, 0};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge);
        this.context = this;

        videosToMerge = new ArrayList<String>();


        mActionbar=getActionBar();
        mActionbar.setLogo(R.mipmap.ic_launcher);
        mActionbar.setDisplayUseLogoEnabled(true);
        mActionbar.setDisplayShowHomeEnabled(true);
        mActionbar.setTitle(Html.fromHtml("<font color='#726f6e'> IMPORT MUSIC </font>"));
        mActionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.whitepatternbackground2));


        OnClickListener listener = new OnClickListener() { //버튼이 클릭됐을 때
            @Override
            public void onClick(View v) {
                if(v.getId()==R.id.musicbtn) {
                    FragmentManager manager = getFragmentManager();
                    DialogRadio alert = new DialogRadio();
                    Bundle b = new Bundle();
                    b.putInt("music", music);
                    b.putInt("position",position);
                    alert.setArguments(b);
                    alert.show(manager, "alert_dialog_radio");
                }else if(v.getId()==R.id.savebtn){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MergeActivity.this);
                    alertDialogBuilder.setMessage("SAVED.");
                    alertDialogBuilder.setNegativeButton("OK", null);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.show();
                    save=1;
                }else if(v.getId()==R.id.homebtn){
                    if(save==0) {
                        fileDelete(mergepath);
                    }
                    Intent intentHome = new Intent(MergeActivity.this, MainActivity.class);
                    intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intentHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intentHome);
                    finish();
                }
            }

        };


        Musicbutton = (Button) findViewById(R.id.musicbtn);
        Musicbutton.setOnClickListener(listener);
        Savebutton = (Button) findViewById(R.id.savebtn);
        Savebutton.setOnClickListener(listener);
        Homebutton = (Button) findViewById(R.id.homebtn);
        Homebutton.setOnClickListener(listener);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        videospath = new ArrayList<String>();
        videospath = b.getStringArrayList("arraylist");
        // 인텐트로 넘어온 동영상 경로의 ArrayList 를 저장.


        for (i = 0; i < videospath.size(); i++) {
            String fileName = getFileName(videospath.get(i)) + ".mp4"; // 동영상 경로에서 동영상 이름만 추출
            videosToMerge.add(fileName); // 추출한 동영상 이름을 리스트에 저장
            Log.e("test", videosToMerge.get(i));
        }

        this.workingPath = Environment.getExternalStorageDirectory() + "/EasyO/temp";

        initializeObjects();
    }

    public static void fileDelete(String deleteFileName) { //파일 삭제
        File I = new File(deleteFileName);
        I.delete();
    }

    @Override
    public void onPositiveClick(int position) { //확인 버튼 누르면..
        this.position = position;

        if(isPlaying==true){
            mp.stop();
            isPlaying=false;
        } // 음악이 계속 재생된다면 멈춘다.

        Intent intent2 = new Intent(getApplicationContext(), ImportMusicActivity.class);
        Bundle bundleData = new Bundle();
        bundleData.putString("MERGEPATH", mergepath);
        bundleData.putInt("music", music);
        bundleData.putInt("position", position);
        intent2.putExtra("MERGEPATH_DATA", bundleData);
        startActivity(intent2); // 첫페이지로 인텐트를 날림
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

    }


    public void onMusicClick(int position){ // radio 버튼이 선택됐을 때,
        this.position = position;

        videoview.pause();

        if(isPlaying==true){
            mp.stop();
            isPlaying=false;
        }

        if(position==0){
            mp = MediaPlayer.create(getApplicationContext(), R.raw.dubstep);
            mp.start();
            isPlaying=true;
        }else if(position == 1){
            mp = MediaPlayer.create(getApplicationContext(), R.raw.enigmatic);
            mp.start();
            isPlaying=true;
        } else if(position==2){
            mp = MediaPlayer.create(getApplicationContext(), R.raw.memories);
            mp.start();
            isPlaying=true;
        }else if(position==3){
            mp = MediaPlayer.create(getApplicationContext(), R.raw.tenderness);
            mp.start();
            isPlaying=true;
        }else if(position==4){
            mp = MediaPlayer.create(getApplicationContext(), R.raw.thejazzpiano);
            mp.start();
            isPlaying=true;
        }else if(position==5){
            mp = MediaPlayer.create(getApplicationContext(), R.raw.energy);
            mp.start();
            isPlaying=true;
        }else if(position==6){
            mp = MediaPlayer.create(getApplicationContext(), R.raw.funkyelement);
            mp.start();
            isPlaying=true;
        }else if(position==7){
            mp = MediaPlayer.create(getApplicationContext(), R.raw.funnysong);
            mp.start();
            isPlaying=true;
        }else if(position==8){
            mp = MediaPlayer.create(getApplicationContext(), R.raw.sunny);
            mp.start();
            isPlaying=true;
        }else if(position==9){
            mp = MediaPlayer.create(getApplicationContext(), R.raw.dance);
            mp.start();
            isPlaying=true;
        }else if(position==10) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.moose);
            mp.start();
            isPlaying = true;
        }else if(position==11) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.popdance);
            mp.start();
            isPlaying = true;
        }else if(position==12) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.anewbeginning);
            mp.start();
            isPlaying = true;
        }else if(position==13) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.clearday);
            mp.start();
            isPlaying = true;
        }else if(position==14) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.goinghigher);
            mp.start();
            isPlaying = true;
        }else if(position==15) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.littleidea);
            mp.start();
            isPlaying = true;
        }else if(position==16) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.brightwish);
            mp.start();
            isPlaying = true;
        }else if(position==17) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.greenhills);
            mp.start();
            isPlaying = true;
        }else if(position==18) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.happyboytheme);
            mp.start();
            isPlaying = true;
        }else if(position==19) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.snappy);
            mp.start();
            isPlaying = true;
        }else if(position==20) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.happyrock);
            mp.start();
            isPlaying = true;
        }else if(position==21) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.instrumental);
            mp.start();
            isPlaying = true;
        }else if(position==22) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.stopping);
            mp.start();
            isPlaying = true;
        }else if(position==23) {
            mp = MediaPlayer.create(getApplicationContext(), R.raw.tarantula);
            mp.start();
            isPlaying = true;
        }
    }


    public void musicstop(){
        if(isPlaying==true){
            mp.stop();
            isPlaying=false;
        }
    } // 취소 버튼 누르면 꺼지기


    public String getFileName(String pathandname) { //동영상 경로에서 동영상 이름을 추출.

        int start = pathandname.lastIndexOf("/");
        int end = pathandname.lastIndexOf(".");
        if (start != -1 && end != -1) {
            return pathandname.substring(start + 1, end);
        } else {
            return null;
        }
    }


    @Override
    protected void onStop() {
        if (mergeVideos != null) {
            mergeVideos.cancel(true);
        }
        super.onStop();
    }

    private void initializeObjects() {
        mergeVideos = new MergeVideos(workingPath, videosToMerge).execute();
    }


    public void DeleteDir(String path) //중간 파일 폴더를 삭제하는 기능
    {
        File file = new File(path);
        File[] childFileList = file.listFiles();
        for (File childFile : childFileList) {
            if (childFile.isDirectory()) {
                DeleteDir(childFile.getAbsolutePath());     //하위 디렉토리 루프
            } else {
                childFile.delete();    //하위 파일삭제
            }
        }
        file.delete();    //root 삭제
    }


    private class MergeVideos extends AsyncTask<String, Integer, String> {


        private String workingPath;
        private ArrayList<String> videosToMerge;
        private ProgressDialog progressDialog;

        private MergeVideos(String workingPath, ArrayList<String> videosToMerge) {
            this.workingPath = workingPath;
            this.videosToMerge = videosToMerge;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(context, "Merging videos", "Please wait...", true);
        }

        ;

        @Override
        protected String doInBackground(String... params) {

            Integer count = videosToMerge.size();
            String[] videoUris = new String[count];
            try {
                for (int index = 0; index < count; index++) {
                    videoUris[index] = videospath.get(index);
                }
                List<Movie> inMovies = new ArrayList<Movie>();
                for (String videoUri : videoUris) {
                    inMovies.add(MovieCreator.build(videoUri));
                }
                List<Track> videoTracks = new LinkedList<Track>();
                List<Track> audioTracks = new LinkedList<Track>();

                for (Movie m : inMovies) {
                    for (Track t : m.getTracks()) {
                        if (t.getHandler().equals("soun")) {
                            audioTracks.add(t);
                        }
                        if (t.getHandler().equals("vide")) {
                            videoTracks.add(t);
                        }
                        if (t.getHandler().equals("")) {

                        }
                    }
                }

                Movie result = new Movie();
                if (audioTracks.size() > 0) {
                    result.addTrack(new AppendTrack(audioTracks
                            .toArray(new Track[audioTracks.size()])));
                }
                if (videoTracks.size() > 0) {
                    result.addTrack(new AppendTrack(videoTracks
                            .toArray(new Track[videoTracks.size()])));
                }
                Container out = new DefaultMp4Builder().build(result);

                Calendar cal = Calendar.getInstance();
                String timeStamp = Integer.toString(cal.get(Calendar.YEAR))+Integer.toString(cal.get(Calendar.MONTH)+1)+
                        Integer.toString(cal.get(Calendar.DAY_OF_MONTH))+Integer.toString(cal.get(Calendar.HOUR_OF_DAY))+
                        Integer.toString(cal.get(Calendar.MINUTE));
                String timeStampS = "" + timeStamp;


                savePath = Environment.getExternalStorageDirectory() + "/EasyO";

                File storagePath = new File(savePath);
                if(!storagePath.exists()){
                    storagePath.mkdirs();
                }

                File myMovie = new File(storagePath, String.format("easyo-%s.mp4", timeStampS));

                mergepath = myMovie.getPath();

                FileOutputStream fos = new FileOutputStream(myMovie);
                FileChannel fc = fos.getChannel();
                out.writeContainer(fc);

                fc.close();
                fos.close();
                DeleteDir(workingPath);


            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            mFileName += "/output.mp4";
            return mFileName;

        }


        @Override
        protected void onPostExecute(String value) {
            super.onPostExecute(value);
            progressDialog.dismiss();


            //동영상 합성이 완료되면, videoview로 완성된 동영상을 재생.

            videoview = (VideoView) findViewById(R.id.mergevideoview);

            // Set the media controller buttons
            if (mediaController == null) {
                mediaController = new MediaController(MergeActivity.this);
                // Set the videoView that acts as the anchor for the MediaController.
                mediaController.setAnchorView(videoview);
                // Set MediaController for VideoView
                videoview.setMediaController(mediaController);
            }

            try {
                videoview.setVideoPath(mergepath);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            videoview.requestFocus();


            // When the video file ready for playback.
            videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mediaPlayer) {
                    videoview.seekTo(videoposition);
                    if (videoposition == 0) {
                        videoview.start();
                    }

                    // When video Screen change size.
                    mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            // Re-Set the videoView that acts as the anchor for the MediaController
                            mediaController.setAnchorView(videoview);
                        }
                    });
                }
            });


        }

    }

}