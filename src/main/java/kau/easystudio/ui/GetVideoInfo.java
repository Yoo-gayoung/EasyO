package kau.easystudio.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.*;

import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.util.SparseArray;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by jay on 2016. 10. 25..
 */

class GetVideoInfo {
    private static final int MUSIC_DURATION = 30 * 1;
    private static final int PROBSIZE = 4;
    private static final int FROM = 0;
    private static final int TO = 1;
    private static final boolean NONZERO = true;
    private static final boolean ZERO = false;
    private static final double FACTOR = 0.7;


    private String[] filePath;
    private int[] duration;
    private int[] intervalLength;

    private int[][] timeSection;
    private int[] intervalCnt;
    private float[][][][] prob;

    public int[][] numOfFaces;


    Context context;

    private ProgressDialog progressDialog = null;

    FragmentList[] fragmentLists;

    public GetVideoInfo(String[] path, Context context) {
        this.context = context;
        this.filePath = path;
    }

    public void setVideoMetadata() {
        int cnt = 0;
        setTimeSection(filePath);

        numOfFaces = new int[timeSection.length][];
        for (int i = 0; i < timeSection.length; i++) {
            cnt += intervalCnt[i];
            for (int j = 0; j < timeSection[i].length; j++) {
                numOfFaces[i] = new int[timeSection[i].length];
            }
        }

        double progress = 100.0 / (double)cnt;
        int progressTmp = 0;
        Bitmap bmp;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        FaceDetector detector = new FaceDetector.Builder(context.getApplicationContext())
                .setTrackingEnabled(false).setMode(FaceDetector.ACCURATE_MODE)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();
        Frame frame;
        SparseArray<Face> faces;
        for (int i = 0; i < numOfFaces.length; i++) {
            mmr.setDataSource(filePath[i]);
            for (int j = 0; j < numOfFaces[i].length; j++) {
                bmp = mmr.getFrameAtTime(timeSection[i][j] * 1000);
                frame = new Frame.Builder().setBitmap(bmp).build();
                faces = detector.detect(frame);
                numOfFaces[i][j] = faces.size();
                EditProcessActivity.progressDialog.setProgress((int) (progress * (progressTmp++)));
            }
        }
        detector.release(); // detecting 작업 완료 후, 메모리 해제
        try {
            fragmentLists = new FragmentList[timeSection.length];

            for (int i = 0; i < numOfFaces.length; i++) {
                fragmentLists[i] = new FragmentList(i);
                boolean[] isNonZero = new boolean[numOfFaces[i].length];
                for (int j = 0; j < isNonZero.length; j++) {
                    if (numOfFaces[i][j] == 0) {
                        isNonZero[j] = false;
                    } else isNonZero[j] = true;
                }
                boolean prev = isNonZero[0];
                int startIndex = 0;
                int endIndex = 0;
                for (int j = 1; j < isNonZero.length; j++) {
                    Log.e("prev vs isNonZero[j] : ", "isNonZero["+Integer.toString(j)+"] : " +
                            ""+Boolean.toString(prev)+" vs "+Boolean.toString(isNonZero[j]));
                    if (isNonZero[j] == prev) {
                        endIndex = j;
                    } else if (isNonZero[j] != prev) {
                        fragmentLists[i].add(startIndex, endIndex, prev);
                        startIndex = j;
                        endIndex = j;
                        prev = isNonZero[j];
                    }

                    if(j==isNonZero.length-1){
                        fragmentLists[i].add(startIndex, endIndex,prev);
                    }
                }
            }

            int [] faceDuration = new int[numOfFaces.length];
            for(int i=0;i<numOfFaces.length;i++){
                int sectionCnt = 0;
                for(int j=0;j<numOfFaces[i].length;j++){
                    if(numOfFaces[i][j]!=0){
                        sectionCnt++;
                    }
                }
                faceDuration[i] = sectionCnt;
            }
            double [] assignedTime;
            assignedTime = distribution(MUSIC_DURATION,duration,faceDuration);



            for(int i =0; i<fragmentLists.length;i++){
                ArrayList<Integer> fromNonZeroList = fragmentLists[i].getList(FROM,NONZERO);
                ArrayList<Integer> toNonZeroList = fragmentLists[i].getList(TO,NONZERO);
                ArrayList<Integer> fromZeroList = fragmentLists[i].getList(FROM,ZERO);
                ArrayList<Integer> toZeroList = fragmentLists[i].getList(TO,ZERO);
                assignedTime[i] = assignedTime[i] * 1000;

                int durationSum = 0;
                while(durationSum < assignedTime[i]){
                    if(!fromNonZeroList.isEmpty()){
                        int fromIndex,toIndex;
                        fromIndex = fromNonZeroList.get(0);
                        toIndex = toNonZeroList.get(0);
                        fromNonZeroList.remove(0);
                        toNonZeroList.remove(0);
                        int duration = timeSection[i][toIndex] - timeSection[i][fromIndex];
                        if(duration <= assignedTime[i] - durationSum){
                            fragmentLists[i].addToResultList(timeSection[i][fromIndex], timeSection[i][toIndex]);
                            durationSum += duration;
                        }
                        else if(duration > assignedTime[i] - durationSum){
                            int diff = (int)assignedTime[i] - (int)durationSum;
                            fragmentLists[i].addToResultList(timeSection[i][fromIndex],
                                    timeSection[i][fromIndex]+diff);
                            durationSum += diff;
                            break;
                        }
                    }
                    else if(fromNonZeroList.isEmpty()){
                        int fromIndex,toIndex;
                        fromIndex = fromZeroList.get(0);
                        toIndex = toZeroList.get(0);
                        fromZeroList.remove(0);
                        toZeroList.remove(0);
                        int duration = timeSection[i][toIndex] - timeSection[i][fromIndex];
                        if(duration <= assignedTime[i] - durationSum){
                            fragmentLists[i].addToResultList(timeSection[i][fromIndex], timeSection[i][toIndex]);
                            durationSum += duration;
                        }
                        else if(duration > assignedTime[i] - durationSum){
                            int diff = (int)assignedTime[i] - (int)durationSum;
                            fragmentLists[i].addToResultList(timeSection[i][fromIndex],
                                    timeSection[i][fromIndex]+diff);
                            durationSum += diff;
                            break;
                        }
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        EditProcessActivity.progressDialog.dismiss();
    }

    public void setTimeSection(String[] filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();

        timeSection = new int[filePath.length][];
        duration = new int[filePath.length];
        intervalCnt = new int[filePath.length];
        intervalLength = new int[filePath.length];

        for (int i = 0; i < filePath.length; i++) {
            mmr.setDataSource(filePath[i]);
            duration[i] = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            intervalCnt[i] = getIntervalCnt(duration[i]);
            intervalLength[i] = duration[i] / intervalCnt[i];
            timeSection[i] = new int[intervalCnt[i] + 1];
            timeSection[i][0] = 10;
            timeSection[i][intervalCnt[i]] = duration[i] - 10;

            for (int j = 1; j < intervalCnt[i]; j++) {
                timeSection[i][j] = timeSection[i][j - 1] + intervalLength[i];
            }

        }
    }

    private double [] distribution (int musicDuration, int [] videoDuration, int [] faceDuration){
        double [] result = new double [videoDuration.length];
        int totalVideoDuration = 0;
        int totalNumOfFaces = 0;
        double [] ratioVideoDuration = new double [videoDuration.length];
        double [] ratioNumOfFaces = new double [videoDuration.length];
        for(int i=0;i<videoDuration.length;i++){
            totalVideoDuration += videoDuration[i];
            totalNumOfFaces += faceDuration[i];
        }
        for(int i=0;i<ratioVideoDuration.length;i++){
            ratioVideoDuration[i] = (double)videoDuration[i] / (double)totalVideoDuration;
            ratioNumOfFaces[i] = (double)faceDuration[i] / (double)totalNumOfFaces;
            result[i] = musicDuration * (FACTOR * ratioVideoDuration[i] + (1-FACTOR) * ratioNumOfFaces[i]);
        }
        for(int i=0;i<result.length;i++){
            if(result[i] >= videoDuration[i]){
                for(int j=0;j<result.length;j++){
                    result[j] = musicDuration * ratioVideoDuration[j];
                }
                break;
            }
        }
        return result;

    }
    public FragmentList[] getFragmentLists(){
        return fragmentLists;
    }

    public int getIntervalCnt(int duration) {
        return (int) duration / 1000;
    }

    public int[] getVideoDuration() {
        return duration;
    }

}
