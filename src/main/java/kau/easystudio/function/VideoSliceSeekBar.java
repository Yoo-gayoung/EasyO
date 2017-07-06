package kau.easystudio.function;

import android.content.Context;
import android.graphics.*;
import android.media.MediaMetadataRetriever;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import org.mp4parser.IsoFile;

import java.io.IOException;

import kau.easystudio.R;

public class VideoSliceSeekBar extends ImageView {

    private static final String TAG = VideoSliceSeekBar.class.getSimpleName();

    private static final int SELECT_THUMB_LEFT = 1;
    private static final int SELECT_THUMB_RIGHT = 2;
    private static final int SELECT_THUMB_NON = 0;

    Bitmap combinedBitmap;
    private String videoPath=null;
    //params
    private Bitmap thumbSlice = BitmapFactory.decodeResource(getResources(), R.drawable.minus1);
    private Bitmap thumbCurrentVideoPosition = BitmapFactory.decodeResource(getResources(), R.drawable.minus_thumb);
    private int progressMinDiff = 1; //percentage
    private int progressMaxDiff = 100; //percentage
    private int progressColor = getResources().getColor(R.color.progress);
    private int secondaryProgressColor = getResources().getColor(R.color.secprogress);
    //private int progressHalfHeight = 17;
    private int progressHalfHeight = getResources().getDimensionPixelOffset(R.dimen.progress_height);
    private int thumbPadding = getResources().getDimensionPixelOffset(R.dimen.thumb_margin);
    private int maxValue = 100;


    private int progressMinDiffPixels;
    private int progressMaxDiffPixels;
    private int thumbSliceLeftX, thumbSliceRightX, thumbCurrentVideoPositionX;
    private double thumbSliceLeftValue, thumbSliceRightValue;
    private int thumbSliceY, thumbCurrentVideoPositionY;
    private Paint paint = new Paint();
    private Paint paintThumb = new Paint();
    private int selectedThumb;
    private int thumbSliceHalfWidth, thumbCurrentVideoPositionHalfWidth;
    private int thumbSliceTenthWidth;
    private SeekBarChangeListener scl;
    private int progressTop;
    private int progressBottom;
    Integer test;
    private boolean blocked;
    private boolean isVideoStatusDisplay;

    public VideoSliceSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public VideoSliceSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoSliceSeekBar(Context context) {
        super(context);
    }


    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        init();
    }

    private void init() {
        if (thumbSlice.getHeight() > getHeight())
            getLayoutParams().height = thumbSlice.getHeight();

        thumbSliceY = (getHeight() / 2) - (thumbSlice.getHeight() / 2);
        thumbCurrentVideoPositionY = (getHeight() / 2) - (thumbCurrentVideoPosition.getHeight() / 2);

        thumbSliceHalfWidth = thumbSlice.getWidth() / 2;
        thumbSliceTenthWidth = thumbSlice.getWidth() / 10000;
        thumbCurrentVideoPositionHalfWidth = thumbCurrentVideoPosition.getWidth() / 2;
        if (thumbSliceLeftX == 0 || thumbSliceRightX == 0) {
            thumbSliceLeftX = thumbPadding;
            thumbSliceRightX = getWidth() - thumbPadding;
        }
        progressMinDiffPixels = calculateCorrds(progressMinDiff) - 2 * thumbPadding;
        progressMaxDiffPixels = calculateCorrds(progressMaxDiff) - 2 * thumbPadding;
        progressTop = getHeight() / 2 - progressHalfHeight;
        progressBottom = getHeight() / 2 + progressHalfHeight;
        invalidate();
    }
    public void setSeekBarChangeListener(SeekBarChangeListener scl) {
        this.scl = scl;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF rect;


        //generate and draw progress
        paint.setColor(progressColor);
        rect = new RectF(thumbPadding, progressTop, thumbSliceLeftX, progressBottom);
        //canvas.drawRect(rect, paint);
        canvas.drawRoundRect(rect,0,0,paint);
        rect = new RectF(thumbSliceRightX, progressTop, getWidth() - thumbPadding, progressBottom);
        canvas.drawRoundRect(rect,0,0,paint);
        //canvas.drawRect(rect, paint);
        //generate and draw secondary progress
        paint.setColor(secondaryProgressColor);
        rect = new RectF(thumbSliceLeftX, progressTop, thumbSliceRightX, progressBottom);
        //canvas.drawRect(rect, paint);
        canvas.drawRoundRect(rect,0,0,paint);

        if (!blocked) {
            //generate and draw thumbs pointer
            canvas.drawBitmap(thumbSlice, thumbSliceLeftX - thumbSliceHalfWidth, thumbSliceY, paintThumb);
            canvas.drawBitmap(thumbSlice, thumbSliceRightX - thumbSliceHalfWidth, thumbSliceY, paintThumb);
        }
        if (isVideoStatusDisplay) {
            //generate and draw video thump pointer
            canvas.drawBitmap(thumbCurrentVideoPosition, thumbCurrentVideoPositionX - thumbCurrentVideoPositionHalfWidth,
                    thumbCurrentVideoPositionY, paintThumb);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!blocked) {
            int mx = (int) event.getX();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mx >= thumbSliceLeftX -  thumbSliceTenthWidth
                            && mx <= thumbSliceLeftX +  thumbSliceTenthWidth || mx < thumbSliceLeftX -  thumbSliceTenthWidth) {
                        selectedThumb = SELECT_THUMB_LEFT;
                    } else if (mx >= thumbSliceRightX -  thumbSliceTenthWidth
                            && mx <= thumbSliceRightX +  thumbSliceTenthWidth|| mx > thumbSliceRightX +  thumbSliceTenthWidth) {
                        selectedThumb = SELECT_THUMB_RIGHT;
                    } else if (mx - thumbSliceLeftX +  thumbSliceTenthWidth < thumbSliceRightX -  thumbSliceTenthWidth - mx) {
                        selectedThumb = SELECT_THUMB_LEFT;
                    } else if (mx - thumbSliceLeftX +  thumbSliceTenthWidth > thumbSliceRightX -  thumbSliceTenthWidth - mx) {
                        selectedThumb = SELECT_THUMB_RIGHT;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if ((mx <= thumbSliceLeftX +  thumbSliceTenthWidth + progressMinDiffPixels && selectedThumb == SELECT_THUMB_RIGHT) ||
                            (mx >= thumbSliceRightX -  thumbSliceTenthWidth- progressMinDiffPixels && selectedThumb == SELECT_THUMB_LEFT)) {
                        selectedThumb = SELECT_THUMB_NON;
                    }

                    if ((mx >= thumbSliceLeftX +  thumbSliceTenthWidth + progressMaxDiffPixels && selectedThumb == SELECT_THUMB_RIGHT) ||
                            (mx <= thumbSliceRightX -  thumbSliceTenthWidth - progressMaxDiffPixels && selectedThumb == SELECT_THUMB_LEFT)) {
                        selectedThumb = SELECT_THUMB_NON;
                    }

                    if (selectedThumb == SELECT_THUMB_LEFT) {
                        thumbSliceLeftX = mx;
                    } else if (selectedThumb == SELECT_THUMB_RIGHT) {
                        thumbSliceRightX = mx;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    selectedThumb = SELECT_THUMB_NON;
                    break;
            }
            notifySeekBarValueChanged();
        }
        return true;
    }

    private void notifySeekBarValueChanged() {
        if (thumbSliceLeftX < thumbPadding)
            thumbSliceLeftX = thumbPadding;

        if (thumbSliceRightX < thumbPadding)
            thumbSliceRightX = thumbPadding;

        if (thumbSliceLeftX > getWidth() - thumbPadding)
            thumbSliceLeftX = getWidth() - thumbPadding;

        if (thumbSliceRightX > getWidth() - thumbPadding)
            thumbSliceRightX = getWidth() - thumbPadding;

        invalidate();
        if (scl != null) {
            calculateThumbValue();
            scl.SeekBarValueChanged(thumbSliceLeftValue, thumbSliceRightValue);
        }
    }

    private void calculateThumbValue() {
        thumbSliceLeftValue = (maxValue * (thumbSliceLeftX - thumbPadding)) / (getWidth() - 2 * thumbPadding);
        thumbSliceRightValue = (maxValue * (thumbSliceRightX - thumbPadding)) / (getWidth() - 2 * thumbPadding);
    }


    private int calculateCorrds(int progress) {
        int width = getWidth();
        return (int) (((width - 2d * thumbPadding) / maxValue) * progress) + thumbPadding;
    }

    public void setLeftProgress(int progress) {
        if (progress < thumbSliceRightValue - progressMinDiff
                && progress > thumbSliceRightValue - progressMaxDiff) {
            thumbSliceLeftX = calculateCorrds(progress);
        }
        notifySeekBarValueChanged();
    }

    public void setRightProgress(int progress) {
        Log.d("VideoSliceSeekBar : ",  "" + progress);
        if (progress > thumbSliceLeftValue + progressMinDiff) {
            Log.d("VideoSliceSeekBar : ",  "Actualizo slice Right: " + (thumbSliceLeftValue + progressMaxDiff));
            thumbSliceRightX = calculateCorrds(progress);
        }
        notifySeekBarValueChanged();
    }

    public double getLeftProgress() {
        return thumbSliceLeftValue;
    }

    public double getRightProgress() {
        return thumbSliceRightValue;
    }

    public void setProgress(int leftProgress, int rightProgress) {
        if (rightProgress - leftProgress > progressMinDiff) {
            thumbSliceLeftX = calculateCorrds(leftProgress);
            thumbSliceRightX = calculateCorrds(rightProgress);
        }
        notifySeekBarValueChanged();
    }

    public void videoPlayingProgress(int progress) {
        isVideoStatusDisplay = true;
        thumbCurrentVideoPositionX = calculateCorrds(progress);
        invalidate();
    }

    public void removeVideoStatusThumb() {
        isVideoStatusDisplay = false;
        invalidate();
    }

    public void setSliceBlocked(boolean isBLock) {
        blocked = isBLock;
        invalidate();
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public void setProgressMinDiff(int progressMinDiff) {
        this.progressMinDiff = progressMinDiff;
        progressMinDiffPixels = calculateCorrds((progressMinDiff/100) * maxValue);
    }

    public void setProgressMaxDiff(int progressMaxDiff) {
        this.progressMaxDiff = progressMaxDiff;
        int progressTotal = (progressMaxDiff * maxValue) / 100;
        progressMaxDiffPixels = calculateCorrds(progressTotal);
    }

    public void setProgressHeight(int progressHeight) {
        this.progressHalfHeight = progressHalfHeight / 2;
        invalidate();
    }

    public void setProgressColor(int progressColor) {
        this.progressColor = progressColor;
        invalidate();
    }

    public void setSecondaryProgressColor(int secondaryProgressColor) {
        this.secondaryProgressColor = secondaryProgressColor;
        invalidate();
    }

    public void setThumbSlice(Bitmap thumbSlice) {
        this.thumbSlice = thumbSlice;
        init();
    }

    public void setThumbCurrentVideoPosition(Bitmap thumbCurrentVideoPosition) {
        this.thumbCurrentVideoPosition = thumbCurrentVideoPosition;
        init();
    }

    public void setThumbPadding(int thumbPadding) {
        this.thumbPadding = thumbPadding;
        invalidate();
    }

    public interface SeekBarChangeListener {

        void SeekBarValueChanged(double leftThumb, double rightThumb);
    }
    public void setVideoPath(String path){
        videoPath = path;
    }


}
