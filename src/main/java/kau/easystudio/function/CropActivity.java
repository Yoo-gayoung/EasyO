package kau.easystudio.function;

/**
 * Created by sksk3 on 2016-05-16.
 */

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;


import org.mp4parser.Container;
import org.mp4parser.IsoFile;
import org.mp4parser.muxer.FileRandomAccessSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AppendTrack;
import org.mp4parser.muxer.tracks.ClippedTrack;
import org.mp4parser.support.Matrix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


import kau.easystudio.R;
import kau.easystudio.ui.SelfEditActivity;


public class CropActivity extends Activity {

    private static final String TAG = CropActivity.class.getSimpleName();
    private static String path;
    ActionBar mActionbar;
    private Context context;
    private double starttime;
    private double endtime;

    private AsyncTask<Void, Void, Void> trimmVideo;
    private String workingPath;

    private String croppath; // 잘라진 동영상의 새로운 경로


    private TextView textViewLeft, textViewRight;
    private VideoSliceSeekBar videoSliceSeekBar;
    private VideoView videoView;
    private View videoControlBtn;
    private View videoCutBtn;
    Double checkLeftSeek = -1.0;
    Double checkRightSeek = -1.0;
    int idf = -1;
    private double duration;
    Integer height=-1;
    Integer width=-1;
    Integer compareHeightWidth = -1;

    private ProgressDialog progressDialog;
    Bitmap bmp1, bmp2, bmp3, bmp4, bmp5, bmp6, bmp7, bmp8, bmp9, bmp10;
    Bitmap combinedBitmap;
    ImageView imvComvine;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActionbar=getActionBar();
        mActionbar.setLogo(R.mipmap.ic_launcher);
        mActionbar.setDisplayUseLogoEnabled(true);
        mActionbar.setDisplayShowHomeEnabled(true);
        mActionbar.setTitle(Html.fromHtml("<font color='#726f6e'> TRIM A CLIP </font>"));
        mActionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.whitepatternbackground2));


        Intent i = getIntent();
        path = i.getStringExtra("path"); //편집할 동영상의 경로를 가져옴
        Log.e("tag_path2", path);
        compareHeightWidth = getVideoSize(path);
        double duration = getVideoDuration(path);
        bmp1 = createThumbnails(path, 1,duration);
        bmp2 = createThumbnails(path, 2,duration);
        bmp3 = createThumbnails(path, 3,duration);
        bmp4 = createThumbnails(path, 4,duration);
        bmp5 = createThumbnails(path, 5,duration);
        bmp6 = createThumbnails(path, 6, duration);
        bmp7 = createThumbnails(path, 7, duration);
        bmp8 = createThumbnails(path, 8, duration);
        bmp9 = createThumbnails(path, 9, duration);
        bmp10 = createThumbnails(path, 10, duration);

        combinedBitmap = combineImages(bmp1, bmp2) ;
        combinedBitmap =combineImages(combinedBitmap,bmp3);
        combinedBitmap =combineImages(combinedBitmap,bmp4);
        combinedBitmap =combineImages(combinedBitmap,bmp5);
        combinedBitmap =combineImages(combinedBitmap,bmp6);
        combinedBitmap =combineImages(combinedBitmap,bmp7);
        combinedBitmap =combineImages(combinedBitmap,bmp8);
        combinedBitmap =combineImages(combinedBitmap,bmp9);
        combinedBitmap =combineImages(combinedBitmap,bmp10);

        if (compareHeightWidth == 1) {
            setContentView(R.layout.self_edit_portrait);
        } else {
            setContentView(R.layout.self_edit_landscape);
        }
        this.context = this;

        textViewLeft = (TextView) findViewById(R.id.left_pointer);
        textViewRight = (TextView) findViewById(R.id.right_pointer);

        imvComvine = (ImageView)findViewById(R.id.imageview);
        imvComvine.setImageBitmap(combinedBitmap);
        videoSliceSeekBar = (VideoSliceSeekBar) findViewById(R.id.seek_bar);

        videoView = (VideoView) findViewById(R.id.video_view);
        videoControlBtn = findViewById(R.id.video_control_btn);
        videoCutBtn = findViewById(R.id.cutButton);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);

        this.workingPath = Environment.getExternalStorageDirectory() + "/EasyO/temp";

        initVideoView();
    }


    private void initVideoView() {
        //final double initial_left= -1.0;
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(final MediaPlayer mp) {
                videoView.seekTo(10);
                videoSliceSeekBar.setSeekBarChangeListener(new VideoSliceSeekBar.SeekBarChangeListener() {
                    @Override
                    public void SeekBarValueChanged(double leftThumb, double rightThumb) {

                        textViewLeft.setText(getTimeForTrackFormat(leftThumb, true));
                        textViewRight.setText(getTimeForTrackFormat(rightThumb, true));
                        idf++;
                        if(videoView.isPlaying()){
                            videoView.pause();
                        }
                        if (idf > 0) {
                            if (checkLeftSeek != leftThumb) {
                                videoView.seekTo((int) leftThumb);
                                checkLeftSeek = leftThumb;
                            } else if (checkRightSeek != rightThumb) {
                                videoView.seekTo((int) rightThumb);
                                checkRightSeek = rightThumb;
                            }
                        }
                        if(idf>10){
                            idf=1;
                        }

                        Double leftThumb_test = leftThumb;
                        Double rightThumb_test = rightThumb;
                        Log.e("시크바left", leftThumb_test.toString());
                        Log.e("시크바right", rightThumb_test.toString());

                    }

                });
                videoSliceSeekBar.setMaxValue(mp.getDuration());
                videoSliceSeekBar.setLeftProgress(0);
                videoSliceSeekBar.setRightProgress(mp.getDuration());

                checkLeftSeek = videoSliceSeekBar.getLeftProgress();
                checkRightSeek = videoSliceSeekBar.getRightProgress();

                videoControlBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        performVideoViewClick();
                    }
                });

                videoCutBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Left progress : " + videoSliceSeekBar.getLeftProgress());
                        Log.d(TAG, "Right progress : " + videoSliceSeekBar.getRightProgress());
                        Log.d(TAG, "Total Duration : " + mp.getDuration());

                        starttime = videoSliceSeekBar.getLeftProgress() / 1000;
                        endtime = videoSliceSeekBar.getRightProgress() / 1000;

                        trimmVideo = new CropVideo(path, starttime, endtime).execute();

                    }

                });

            }
        });

        videoView.setVideoPath(path);


    }


    @Override
    protected void onStop() {
        if (trimmVideo != null)
            trimmVideo.cancel(true);
        super.onStop();
    }


    private class CropVideo extends AsyncTask<Void, Void, Void> {
        private String mediaPath;
        private double startTime1;
        private double endTime1;
        private double length;
        private ProgressDialog progressDialog;

        private CropVideo(String mediaPath, double startTime, double endTime) {
            this.mediaPath = mediaPath;
            this.startTime1 = startTime;
            this.endTime1 = endTime;
            this.length = endTime - startTime;
            Double test_startTime1 = startTime1;
            Double test_endTime1 = endTime1;
            Double test_legth = length;
            Log.e("test_start", test_startTime1.toString());
            Log.e("test_end", test_endTime1.toString());
            Log.e("test_length", test_legth.toString());
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(context, "Trimming videos", "Please wait...", true);
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            trimVideo();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            Intent intent = new Intent(CropActivity.this, SelfEditActivity.class);
            intent.putExtra("path", croppath);
            setResult(RESULT_OK, intent);
            finish();
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            super.onPostExecute(result);
        }

        private void trimVideo() {
            try {

                Movie movie = MovieCreator.build(mediaPath);
                List<Track> tracks = movie.getTracks();
                movie.setTracks(new LinkedList<Track>());

                boolean timeCorrected = false;

                for (Track track : tracks) {
                    if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                        if (timeCorrected) {
                            throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                        }
                        startTime1 = correctTimeToSyncSample(track, startTime1, false);
                        endTime1 = correctTimeToSyncSample(track, endTime1, true);
                        timeCorrected = true;
                    }
                }

                for (Track track : tracks) {
                    long currentSample = 0;
                    double currentTime = 0;
                    double lastTime = -1;
                    long startSample1 = -1;
                    long endSample1 = -1;

                    for (int i = 0; i < track.getSampleDurations().length; i++) {
                        long delta = track.getSampleDurations()[i];


                        if (currentTime > lastTime && currentTime <= startTime1) {
                            startSample1 = currentSample;
                        }
                        if (currentTime > lastTime && currentTime <= endTime1) {
                            endSample1 = currentSample;
                        }
                        lastTime = currentTime;
                        currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
                        currentSample++;
                    }
                    movie.addTrack(new AppendTrack(new ClippedTrack(track, startSample1, endSample1)));
                }
                Container out = new DefaultMp4Builder().build(movie);
                File storagePath = new File(workingPath);
                if(!storagePath.exists()){
                    storagePath.mkdirs();
                }
                long timestamp = new Date().getTime();
                String timestampS = "" + timestamp;

                File myMovie = new File(storagePath, String.format("output-%s-%f-%f.mp4", timestampS, startTime1 * 1000, length * 1000));

                croppath = myMovie.getPath();
                FileOutputStream fos = new FileOutputStream(myMovie);
                FileChannel fc = fos.getChannel();
                out.writeContainer(fc);

                fc.close();
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];

            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), currentSample + 1)] = currentTime;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;

        }
        double previous = 0;
        for (double timeOfSyncSample : timeOfSyncSamples) {
            if (timeOfSyncSample > cutHere) {
                if (next) {
                    return timeOfSyncSample;
                } else {
                    return previous;
                }
            }
            previous = timeOfSyncSample;
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }

    public static String getTimeForTrackFormat(double timeInMills, boolean display2DigitsInMinsSection) {
        int minutes = (int) (timeInMills / (60 * 1000));
        int seconds = (int) (timeInMills - minutes * 60 * 1000) / 1000;
        String result = display2DigitsInMinsSection && minutes < 10 ? "0" : "";
        result += minutes + ":";
        if (seconds < 10) {
            result += "0" + seconds;
        } else {
            result += seconds;
        }
        return result;
    }

    private void performVideoViewClick() {
        if (videoView.isPlaying()) {
            videoView.pause();
            videoSliceSeekBar.setSliceBlocked(false);
            videoSliceSeekBar.removeVideoStatusThumb();
        } else {
            videoView.seekTo((int) videoSliceSeekBar.getLeftProgress());
            videoView.start();
            videoSliceSeekBar.setSliceBlocked(false);
            videoSliceSeekBar.videoPlayingProgress((int) videoSliceSeekBar.getLeftProgress());
            videoStateObserver.startVideoProgressObserving();
        }
    }

    private StateObserver videoStateObserver = new StateObserver();

    private class StateObserver extends Handler {

        private boolean alreadyStarted = false;

        private void startVideoProgressObserving() {
            if (!alreadyStarted) {
                alreadyStarted = true;
                sendEmptyMessage(0);
            }
        }

        private Runnable observerWork = new Runnable() {
            @Override
            public void run() {
                startVideoProgressObserving();
            }
        };

        @Override
        public void handleMessage(Message msg) {
            alreadyStarted = false;
            videoSliceSeekBar.videoPlayingProgress(videoView.getCurrentPosition());
            if (videoView.isPlaying() && videoView.getCurrentPosition() < videoSliceSeekBar.getRightProgress()) {
                postDelayed(observerWork, 50);
            } else {

                if (videoView.isPlaying()) videoView.pause();

                videoSliceSeekBar.setSliceBlocked(false);
                videoSliceSeekBar.removeVideoStatusThumb();
            }
        }
    }

    public int getVideoSize(String path) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(path);
        Bitmap bmp = mmr.getFrameAtTime(1);
        height=bmp.getHeight();
        width=bmp.getWidth();
        Log.e("compare_height",height.toString());
        Log.e("compare_width",width.toString());
        if(height>width){
            return 1;
        }
        else return 2;
    }

    public Bitmap createThumbnails(String filepath, int identf,double duration) {
        int thumbCount = 10;
        double videoDuration = 0;
        double startTime = 0.000001;
        double endTime;
        double cutInterval;
        double usToSecParam = 1000000;
        videoDuration = duration;
        cutInterval = videoDuration / thumbCount;
        endTime = (int) videoDuration;
        double thumbTime = startTime;
        thumbTime = thumbTime + (identf * cutInterval);

        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filepath);
        Bitmap bmp;
        bmp = mmr.getFrameAtTime((long) thumbTime * (long) usToSecParam);
        bmp = ThumbnailUtils.extractThumbnail(bmp,40,80);
        return bmp;
    }
    public double getVideoDuration(String filepath) {
        try {
            String path=filepath;
            IsoFile isoFile = new IsoFile(path);
            double lengthInSeconds = (double) isoFile.getMovieBox().getMovieHeaderBox().getDuration()
                    / isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
            isoFile.close();
            return lengthInSeconds;
        } catch (IOException e) {
            return 0;
        }
    }
    public Bitmap combineImages(Bitmap c, Bitmap s) {
        Bitmap cs = null;
        int width, height = 0;
        if(c.getWidth() > s.getWidth()) {
            width = c.getWidth() + s.getWidth();
            height = c.getHeight();
        } else {
            width = s.getWidth() + s.getWidth();
            height = c.getHeight();
        }
        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas comboImage = new Canvas(cs);
        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s, c.getWidth(), 0f, null);
        return cs;
    }

}

