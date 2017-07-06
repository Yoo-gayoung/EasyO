package kau.easystudio.ui;

import android.content.Context;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.mp4parser.Container;
import org.mp4parser.IsoFile;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AACTrackImpl;
import org.mp4parser.muxer.tracks.AppendTrack;
import org.mp4parser.muxer.tracks.ClippedTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import kau.easystudio.R;

/**
 * Created by jay on 2016. 10. 26..
 */

public class CropAndMerge {
    public static final int EXTRA_HIGH = 90;

    /*********/
    private int c=3;

    private String[] videoPath;
    private int[][] timeSection;
    private int[][] numOfFaces;
    private int musicDuration;
    private int[] videoDuration;
    private Movie[] mov;
    String saveDirectoryPath;
    private String completedVideoName = "";

    int index = 0;

    public CropAndMerge(Context context, String[]videoPath, String audioName ,FragmentList [] fragmentLists, int num)
            throws IOException {
        this.videoPath = videoPath;
        mov = new Movie[num];
        for(int i=0 ; i < fragmentLists.length;i++){
            for(int j=0;j<fragmentLists[i].getResultFrom().size();j++){
                double startTime = ((double)fragmentLists[i].getResultFrom().get(j))/1000;
                double endTime = ((double)fragmentLists[i].getResultTO().get(j))/1000;
                mov[index] = crop(videoPath[i],startTime,endTime);
                index++;
            }
        }
        String tempFilePath = merge(mov);
        completedVideoName = addAudio(context,tempFilePath,audioName);

    }

    private Movie crop(String videoPath, double startTime, double endTime) {
        Movie movie = new Movie();
        try {
            movie = MovieCreator.build(videoPath);

            List<Track> tracks = movie.getTracks();
            movie.setTracks(new LinkedList<Track>());
            boolean timeCorrected = false;

            for (Track track : tracks) {
                if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                    if (timeCorrected) {
                        throw new RuntimeException("The startTime has already been corrected by another" +
                                " track with SyncSample. Not Supported.");
                    }
                    startTime = correctTimeToSyncSample(track, startTime, false);
                    endTime = correctTimeToSyncSample(track, endTime, true);
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


                    if (currentTime > lastTime && currentTime <= startTime) {
                        startSample1 = currentSample;
                    }
                    if (currentTime > lastTime && currentTime <= endTime) {
                        endSample1 = currentSample;
                    }
                    lastTime = currentTime;
                    currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
                    currentSample++;
                }
                movie.addTrack(new AppendTrack(new ClippedTrack(track, startSample1, endSample1)));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return movie;

    }

    private String merge(Movie movies[]) {
        String tempFilePath = "";
        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();
        int cnt = 0;
        for (Movie m : movies) {
            for (Track t : m.getTracks()) {
                cnt++;
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
        try {
            if (audioTracks.size() > 0) {
                result.addTrack(new AppendTrack(audioTracks
                        .toArray(new Track[audioTracks.size()])));
            }
            if (videoTracks.size() > 0) {
                result.addTrack(new AppendTrack(videoTracks
                        .toArray(new Track[videoTracks.size()])));
            }

            Container out = new DefaultMp4Builder().build(result);
            saveDirectoryPath = Environment.getExternalStorageDirectory() + "/EasyO/temp";
            File saveFilePath = new File(saveDirectoryPath);
            if(!saveFilePath.exists()){
                saveFilePath.mkdirs();
            }
            File myMovie = new File(saveFilePath, "mergetemp.mp4");

            FileOutputStream fos = new FileOutputStream(myMovie);
            FileChannel fc = fos.getChannel();
            out.writeContainer(fc);
            fc.close();
            fos.close();
            tempFilePath = myMovie.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return tempFilePath;
    }
    private String addAudio(Context context,String tempVideoPath, String audioName) throws IOException {
        Calendar cal = Calendar.getInstance();
        String timeStamp = Integer.toString(cal.get(Calendar.YEAR))+Integer.toString(cal.get(Calendar.MONTH)+1)+
                Integer.toString(cal.get(Calendar.DAY_OF_MONTH))+Integer.toString(cal.get(Calendar.HOUR_OF_DAY))+
                Integer.toString(cal.get(Calendar.MINUTE));
        String outPath = Environment.getExternalStorageDirectory() + "/EasyO/easyo"+timeStamp+".mp4";
        Movie video = null;
        Movie result = new Movie();
        List<Track> videoTracks = new LinkedList<Track>();
        File audioFile = parseRawAudioFile(context,audioName);

        AACTrackImpl aacTrack = new AACTrackImpl((new FileDataSourceImpl(audioFile)));


        try{
            video = new MovieCreator().build(tempVideoPath);
            for(Track t : video.getTracks()){
                if(t.getHandler().equals("vide")){
                    videoTracks.add(t);
                }
            }
        } catch (RuntimeException e){
            e.printStackTrace();
        }

        IsoFile isoFile = new IsoFile(tempVideoPath);
        double lengthInSeconds = (double)
                isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                isoFile.getMovieBox().getMovieHeaderBox().getTimescale();

        Track track = (Track) video.getTracks().get(0);

        Track audioTrack = (Track) aacTrack;

        double startTime1 = 0;
        double endTime1 = lengthInSeconds;
        boolean timeCorrected = false;

        if (audioTrack.getSyncSamples() != null && audioTrack.getSyncSamples().length > 0) {
            if (timeCorrected) {

                throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
            }
            startTime1 = correctTimeToSyncSample(audioTrack, startTime1, false);
            endTime1 = correctTimeToSyncSample(audioTrack, endTime1, true);
            timeCorrected = true;
        }

        long currentSample = 0;
        double currentTime = 0;
        double lastTime = -1;
        long startSample1 = -1;
        long endSample1 = -1;
        for (int i = 0; i < audioTrack.getSampleDurations().length; i++) {
            long delta = audioTrack.getSampleDurations()[i];


            if (currentTime > lastTime && currentTime <= startTime1) {
                // current sample is still before the new starttime
                startSample1 = currentSample;
            }
            if (currentTime > lastTime && currentTime <= endTime1) {
                // current sample is after the new start time and still before the new endtime
                endSample1 = currentSample;
            }

            lastTime = currentTime;
            currentTime += (double) delta / (double) audioTrack.getTrackMetaData().getTimescale();
            currentSample++;
        }

        ClippedTrack clippedAACTrack = new ClippedTrack(aacTrack,startSample1,endSample1);

        result.addTrack(clippedAACTrack);
        if (videoTracks.size() > 0) {
            result.addTrack(new AppendTrack(videoTracks
                    .toArray(new Track[videoTracks.size()])));
        }
        Container out = new DefaultMp4Builder().build(result);
        FileOutputStream fos = new FileOutputStream(outPath);
        BufferedWritableFileByteChannel byteBufferByteChannel = new BufferedWritableFileByteChannel(fos);
        try {
            out.writeContainer(byteBufferByteChannel);
            byteBufferByteChannel.close();
            fos.close();
            deleteDir(saveDirectoryPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outPath;
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
    private File parseRawAudioFile (Context context, String audioName) {
        int resId;
        InputStream inputStream = null;
        OutputStream outputStream =null;
        File file = null;
        try{
            resId = context.getResources().getIdentifier(audioName,"raw","kau.easystudio");
            inputStream = context.getResources().openRawResource(resId);
            file = File.createTempFile("temp",".aac");
            outputStream = new FileOutputStream(file);
            IOUtils.copy(inputStream,outputStream);
            inputStream.close();
            outputStream.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return file;

    }

    private static class BufferedWritableFileByteChannel implements WritableByteChannel {
        private static final int BUFFER_CAPACITY = 1000000;

        private boolean isOpen = true;
        private final OutputStream outputStream;
        private final ByteBuffer byteBuffer;
        private final byte[] rawBuffer = new byte[BUFFER_CAPACITY];

        private BufferedWritableFileByteChannel(OutputStream outputStream) {
            this.outputStream = outputStream;
            this.byteBuffer = ByteBuffer.wrap(rawBuffer);
        }

        @Override
        public int write(ByteBuffer inputBuffer) throws IOException {
            int inputBytes = inputBuffer.remaining();

            if (inputBytes > byteBuffer.remaining()) {
                dumpToFile();
                byteBuffer.clear();

                if (inputBytes > byteBuffer.remaining()) {
                    throw new BufferOverflowException();
                }
            }

            byteBuffer.put(inputBuffer);

            return inputBytes;
        }

        @Override
        public boolean isOpen() {
            return isOpen;
        }

        @Override
        public void close() throws IOException {
            dumpToFile();
            isOpen = false;
        }

        private void dumpToFile() {
            try {
                outputStream.write(rawBuffer, 0, byteBuffer.position());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void deleteDir(String path) //중간 파일 폴더를 삭제하는 기능
    {
        File file = new File(path);
        File[] childFileList = file.listFiles();
        for (File childFile : childFileList) {
            if (childFile.isDirectory()) {
                deleteDir(childFile.getAbsolutePath());     //하위 디렉토리 루프
            } else {
                childFile.delete();    //하위 파일삭제
            }
        }
        file.delete();    //root 삭제
    }
    public String getVideoName () { return completedVideoName; }
}
