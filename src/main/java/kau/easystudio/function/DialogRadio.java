package kau.easystudio.function;

/**
 * Created by sksk3 on 2016-09-20.
 */
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

public class DialogRadio extends DialogFragment{

    //인터페이스 객체
    AlertPositiveListener alertPositiveListener;
    MusicselListener musicselListener;
    NegativeListener negativeselListener;

    //인터페이스

    interface AlertPositiveListener {
        public void onPositiveClick(int position);
    }

    interface MusicselListener {
        public void onMusicClick(int position);
    }

    interface NegativeListener {
        public void musicstop();
    }

    public void onAttach(android.app.Activity activity) {
        super.onAttach(activity);
        try{
            alertPositiveListener = (AlertPositiveListener) activity;
            musicselListener = (MusicselListener) activity;
            negativeselListener = (NegativeListener) activity;
        }catch(ClassCastException e){
            throw new ClassCastException(activity.toString());
        }
    }

    OnClickListener positiveListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

            AlertDialog alert = (AlertDialog)dialog;
            int position = alert.getListView().getCheckedItemPosition();
            alertPositiveListener.onPositiveClick(position);
        }
    }; //확인 버튼을 누를 때 실행되는 리스너

    OnClickListener musicListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            AlertDialog alert = (AlertDialog)dialog;
            int position = alert.getListView().getCheckedItemPosition();
            musicselListener.onMusicClick(position);
        }
    }; //라디오 버튼을 누를 때 실행되는 리스너

    OnClickListener negativeListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            negativeselListener.musicstop();
        }
    }; //취소 버튼을 누를 때 실행되는 리스너



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) { // 다이얼로그 창

        Bundle bundle = getArguments();
        int position = bundle.getInt("position");
        int music = bundle.getInt("music");
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity());


        if(music == 1){
            b.setTitle("#Select Your Music");
            b.setSingleChoiceItems(MusicList.musiclist, position, musicListener);
        }


        b.setPositiveButton("OK",positiveListener);
        b.setNegativeButton("Cancel",negativeListener);

        AlertDialog d = b.create();
        return d;
    }
}
