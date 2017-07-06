package kau.easystudio.function;

import org.mp4parser.Container;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.tracks.ClippedTrack;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AppendTrack;
import org.mp4parser.muxer.tracks.AACTrackImpl;
import org.mp4parser.IsoFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MergeMP4WithAAC {
    public static boolean mux(String mp4File, File audioFile, String outputFile) throws IOException {
        Movie video;
        List<Track> videoTracks = new LinkedList<Track>();
        Movie result = new Movie();

        AACTrackImpl mp3Track = new AACTrackImpl((new FileDataSourceImpl(audioFile)));

        try {
            video = new MovieCreator().build(mp4File);
            for (Track t : video.getTracks()) {
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


        IsoFile isoFile = new IsoFile(mp4File);
        double lengthInSeconds = (double)
                isoFile.getMovieBox().getMovieHeaderBox().getDuration() /
                isoFile.getMovieBox().getMovieHeaderBox().getTimescale();

        Track track = (Track) video.getTracks().get(0);

        Track audioTrack = (Track) mp3Track;

        double startTime1 = 0;
        double endTime1 = lengthInSeconds;

        //video.addTrack(mp3Track);
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

        ClippedTrack clippedrAacTrack = new ClippedTrack(mp3Track, startSample1, endSample1);


        result.addTrack(clippedrAacTrack);

        if (videoTracks.size() > 0) {
            result.addTrack(new AppendTrack(videoTracks
                    .toArray(new Track[videoTracks.size()])));
        }

        Container out = new DefaultMp4Builder().build(result);

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        BufferedWritableFileByteChannel byteBufferByteChannel = new BufferedWritableFileByteChannel(fos);
        try {
            out.writeContainer(byteBufferByteChannel);
            byteBufferByteChannel.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0;
        for (int i = 0; i < track.getSampleDurations().length; i++) {
            long delta = track.getSampleDurations()[i];

            if (Arrays.binarySearch(track.getSyncSamples(), currentSample + 1) >= 0) {
                // samples always start with 1 but we start with zero therefore +1
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
}