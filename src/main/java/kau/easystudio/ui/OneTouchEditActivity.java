package kau.easystudio.ui;

import kau.easystudio.R;
import kau.easystudio.multiselectview.AlbumsModel;
import kau.easystudio.multiselectview.ShowAlbumImagesActivity;
import kau.easystudio.multiselectview.Utils;
import kau.easystudio.ui.AlertDialogRadio.AlertPositiveListener;
import kau.easystudio.ui.AlertDialogRadio.MusicselListener;
import kau.easystudio.ui.AlertDialogRadio.NegativeListener;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import java.util.ArrayList;

public class OneTouchEditActivity extends Activity implements AlertPositiveListener,MusicselListener,NegativeListener {

    MediaPlayer mp;
    boolean isPlaying = false; //음악이 재생되고 있는지 확인하기 위해
    int position = 0; // radio button 순서
    int selection; // 테마
    private ArrayList<AlbumsModel> albumsModels;
    ActionBar mActionbar;
    String music_name;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onetouch);

        mActionbar=getActionBar();
        mActionbar.setLogo(R.mipmap.ic_launcher);
        mActionbar.setDisplayUseLogoEnabled(true);
        mActionbar.setDisplayShowHomeEnabled(true);
        mActionbar.setTitle(Html.fromHtml("<font color='#726f6e'> MUSIC THEME </font>"));
        mActionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.whitepatternbackground2));


        OnClickListener listener = new OnClickListener() { //버튼이 클릭됐을 때
            @Override
            public void onClick(View v) {
                switch(v.getId()){
                    case R.id.calmdown :
                        selection = 1;
                        break;
                    case R.id.chillout :
                        selection = 2;
                        break;
                    case R.id.coffeebreak :
                        selection = 3;
                        break;
                    case R.id.dailylife :
                        selection = 4;
                        break;
                    case R.id.epicparty :
                        selection = 5;
                        break;
                    case R.id.roadtrip :
                        selection = 6;
                        break;

                    default : break;
                } // 각각 테마의 selection 지정

                FragmentManager manager = getFragmentManager();
                AlertDialogRadio alert = new AlertDialogRadio();

                Bundle b  = new Bundle();
                b.putInt("position", position);
                b.putInt("theme", selection);
                alert.setArguments(b);
                alert.show(manager, "alert_dialog_radio");
            }
        };

        ImageButton theme_1 = (ImageButton) findViewById(R.id.calmdown);
        theme_1.setOnClickListener(listener);

        ImageButton theme_2 = (ImageButton) findViewById(R.id.chillout);
        theme_2.setOnClickListener(listener);

        ImageButton theme_3 = (ImageButton) findViewById(R.id.coffeebreak);
        theme_3.setOnClickListener(listener);

        ImageButton theme_4 = (ImageButton) findViewById(R.id.dailylife);
        theme_4.setOnClickListener(listener);

        ImageButton theme_5 = (ImageButton) findViewById(R.id.epicparty);
        theme_5.setOnClickListener(listener);

        ImageButton theme_6 = (ImageButton) findViewById(R.id.roadtrip);
        theme_6.setOnClickListener(listener);

    }

    private ArrayList<AlbumsModel> getGalleryAlbumImages() {
        final String[] columns = { MediaStore.Video.Media.DATA, MediaStore.Video.Media._ID };
        final String orderBy = MediaStore.Video.Media.DATE_TAKEN;
        Cursor imagecursor = managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy + " DESC");
        albumsModels = Utils.getAllDirectoriesWithImages(imagecursor);
        return albumsModels;
    }


    @Override
    public void onPositiveClick(int position) { //확인 버튼 누르면..
        this.position = position;
        Intent intent = new Intent(OneTouchEditActivity.this, ShowAlbumImagesActivity.class);




        if(selection==1){ // calm down
            if(position==0){
                music_name="enigmatic";
            }else if(position==1){
                music_name="memories";
            }else if(position==2){
                music_name="tenderness";
            }else{
                music_name="thejazzpiano";
            }
        }else if(selection==2){ //chill out
            if(position==0){
                music_name="brightwish";
            }else if(position==1){
                music_name="greenhills";
            }else if(position==2){
                music_name="happyboytheme";
            }else{
                music_name="snappy";
            }
        }else if(selection==3) { //coffee break
            if(position==0){
                music_name="happyrock";
            }else if(position==1){
                music_name="instrumental";
            }else if(position==2){
                music_name="stopping";
            }else{
                music_name="tarantula";
            }
        }else if(selection==4){ //daily lift
            if(position==0){
                music_name="energy";
            }else if(position==1){
                music_name="funkyelement";
            }else if(position==2){
                music_name="funnysong";
            }else{
                music_name="sunny";
            }
        }else if(selection==5){ //epic party
            if (position == 0) {
                music_name="dance";
            } else if (position == 1) {
                music_name="dubstep";
            } else if (position == 2) {
                music_name="moose";
            } else {
                music_name="popdance";
            }
        }else { //road trip
            if(position==0){
                music_name="anewbeginning";
            }else if(position==1){
                music_name="clearday";
            }else if(position==2){
                music_name="goinghigher";
            }else{
                music_name="littleidea";
            }
        }


        intent.putExtra("musicname",music_name);
        intent.putExtra("position", position);
        intent.putExtra("selection", selection);
        intent.putExtra("albumsList", getGalleryAlbumImages());
        startActivity(intent);

        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        if(isPlaying==true){
            mp.stop();
            isPlaying=false;
        } // 음악이 계속 재생된다면 멈춘다.
    }

    public void onMusicClick(int position){ // radio 버튼이 선택됐을 때,
        this.position = position;


        //String path = "android.resource://" + getPackageName() + "/" + R.raw.video;

        if(isPlaying==true){
            mp.stop();
            isPlaying=false;
        }

        if(selection==1){ // calm down
            if(position==0){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.enigmatic);
                mp.start();
                isPlaying=true;
            }else if(position==1){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.memories);
                mp.start();
                isPlaying=true;
            }else if(position==2){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.tenderness);
                mp.start();
                isPlaying=true;
            }else{
                mp = MediaPlayer.create(getApplicationContext(), R.raw.thejazzpiano);
                mp.start();
                isPlaying=true;
            }
        }else if(selection==2){ //chill out
            if(position==0){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.brightwish);
                mp.start();
                isPlaying=true;
            }else if(position==1){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.greenhills);
                mp.start();
                isPlaying=true;
            }else if(position==2){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.happyboytheme);
                mp.start();
                isPlaying=true;
            }else{
                mp = MediaPlayer.create(getApplicationContext(), R.raw.snappy);
                mp.start();
                isPlaying=true;
            }
        }else if(selection==3) { //coffee break
            if(position==0){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.happyrock);
                mp.start();
                isPlaying=true;
            }else if(position==1){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.instrumental);
                mp.start();
                isPlaying=true;
            }else if(position==2){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.stopping);
                mp.start();
                isPlaying=true;
            }else{
                mp = MediaPlayer.create(getApplicationContext(), R.raw.tarantula);
                mp.start();
                isPlaying=true;
            }
        }else if(selection==4){ //daily lift
            if(position==0){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.energy);
                mp.start();
                isPlaying=true;
            }else if(position==1){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.funkyelement);
                mp.start();
                isPlaying=true;
            }else if(position==2){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.funnysong);
                mp.start();
                isPlaying=true;
            }else{
                mp = MediaPlayer.create(getApplicationContext(), R.raw.sunny);
                mp.start();
                isPlaying=true;
            }
        }else if(selection==5){ //epic party
            if (position == 0) {
                mp = MediaPlayer.create(getApplicationContext(), R.raw.dance);
                mp.start();
                isPlaying = true;
            } else if (position == 1) {
                mp = MediaPlayer.create(getApplicationContext(), R.raw.dubstep_aac);
                mp.start();
                isPlaying = true;
            } else if (position == 2) {
                mp = MediaPlayer.create(getApplicationContext(), R.raw.moose);
                mp.start();
                isPlaying = true;
            } else {
                mp = MediaPlayer.create(getApplicationContext(), R.raw.popdance);
                mp.start();
                isPlaying = true;
            }
        }else { //road trip
            if(position==0){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.anewbeginning);
                mp.start();
                isPlaying=true;
            }else if(position==1){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.clearday);
                mp.start();
                isPlaying=true;
            }else if(position==2){
                mp = MediaPlayer.create(getApplicationContext(), R.raw.goinghigher);
                mp.start();
                isPlaying=true;
            }else{
                mp = MediaPlayer.create(getApplicationContext(), R.raw.littleidea);
                mp.start();
                isPlaying=true;
            }
        }

    }

    public void musicstop(){
        if(isPlaying==true){
            mp.stop();
            isPlaying=false;
        }
    } // 취소 버튼 누르면 꺼지기
}