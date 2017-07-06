package kau.easystudio.ui;


import java.lang.String;
import java.util.ArrayList;
import java.util.Collections;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.media.MediaPlayer.OnPreparedListener;
import android.widget.VideoView;

import kau.easystudio.R;
import kau.easystudio.function.CropActivity;
import kau.easystudio.function.MergeActivity;
import kau.easystudio.ui.DragDropGridView.OnDropListener;
import kau.easystudio.ui.DragDropGridView.OnClickListener;

public class SelfEditActivity extends Activity {
    private static final String TAG = SelfEditActivity.class.getSimpleName();
    ActionBar mActionbar;
    private String path;

    private Bitmap thumbnail;
    private String croppath;
    private ArrayList<String> pathlist;
    private ArrayList<Bitmap> thumbnaillist;
    private ArrayList<String> mergelist;
    private DragDropGridView _itemList;
    protected AppListAdapter _adapter;
    private int i;

    private View mergeBtn;
    private VideoView videoView;
    private int videoposition = 0;
    private MediaController mediaController;
    private MediaController mediaController1;
    private int posi;

    private final int SELECT_MOVIE = 1;
    private final int CROP_MOVIE = 2;

    String projectName=null;
    int projectId=-1;
    int projectType=-1;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.video_action_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                // Returns to project picker if user clicks on the app icon in the action bar.
                final Intent intent = new Intent(this, SelfEditActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            }

            case R.id.menu_item_import_video: {
                final Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityGallery();
                return true;
            }

            default: {
                return false;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_editing_project);
        mActionbar=getActionBar();
        mActionbar.setLogo(R.mipmap.ic_launcher);
        mActionbar.setDisplayUseLogoEnabled(true);
        mActionbar.setDisplayShowHomeEnabled(true);
        mActionbar.setTitle(Html.fromHtml("<font color='#726f6e'> ADD CLIPS </font>"));
        mActionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.whitepatternbackground2));
        startActivityGallery();

        pathlist = new ArrayList<String>();
        thumbnaillist = new ArrayList<Bitmap>();
        mergelist = new ArrayList<String>();

        mergeBtn = findViewById(R.id.mergebutton);


    }




    public void onClick(View view) {
        switch(view.getId()){
            case R.id.mergebutton: //버튼을 누르면 동영상을 합성
                if(pathlist.isEmpty()){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SelfEditActivity.this);
                    alertDialogBuilder.setMessage("Please select one or more videos.");
                    alertDialogBuilder.setNegativeButton("OK", null);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.show();
                    break;

                }else{
                    for(i=0; i<pathlist.size(); i++){
                        mergelist.add(_adapter.getItemPath(i));
                    }

                    Intent intent = new Intent(this, MergeActivity.class);
                    Bundle b = new Bundle();
                    b.putStringArrayList("arraylist",mergelist);
                    intent.putExtras(b);
                    startActivity(intent);


                    overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
                    break;
                }
        }

    }

    private void startActivityGallery() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("video/*");
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try
        {
            startActivityForResult(i, SELECT_MOVIE);
        } catch (android.content.ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_MOVIE)
            {
                Uri uri = intent.getData();
                path = getPath(uri);

                Intent i = new Intent(SelfEditActivity.this, CropActivity.class);
                i.putExtra("path", path);

                startActivityForResult(i, CROP_MOVIE);
                overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            }

            if(requestCode == CROP_MOVIE){

                Bundle bundle = intent.getExtras();
                croppath=bundle.getString("path");
                thumbnail = ThumbnailUtils.createVideoThumbnail(croppath, MediaStore.Images.Thumbnails.MINI_KIND);
                pathlist.add(croppath);
                thumbnaillist.add(thumbnail);


                videoView = (VideoView) findViewById(R.id.videoView);

                if (mediaController == null) {
                    mediaController = new MediaController(SelfEditActivity.this);
                    mediaController.setAnchorView(videoView);
                    videoView.setMediaController(mediaController);
                }

                try {
                    videoView.setVideoPath(croppath);

                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }

                videoView.requestFocus();
                videoView.setOnPreparedListener(new OnPreparedListener() {

                    public void onPrepared(MediaPlayer mediaPlayer) {
                        videoView.seekTo(videoposition);
                        if (videoposition == 0) {
                            videoView.start();
                        }

                        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                            @Override
                            public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                mediaController.setAnchorView(videoView);
                            }
                        });
                    }
                });




            }

        }


        //GridView

        _itemList = (DragDropGridView) findViewById(R.id.list_apps);
        _adapter = new AppListAdapter(this);

        for(i=0; i< thumbnaillist.size(); i++){
            _adapter.addItem(1 + "", thumbnaillist.get(i), pathlist.get(i));
        } // adapter의 리스트에 썸네일과 경로를 추가
        _itemList.setAdapter(_adapter);
        _itemList.setOnDropListener(onDropListener);
        _itemList.setOnClickListener(onClickListener);

    }


    private OnDropListener onDropListener = new OnDropListener(){
        @Override
        public void drop(int from, int to){
            AppListItem item = _adapter.removeItemAt(from);
            _adapter.addItemAt(to, item);
            Collections.swap(pathlist, from, to);
            _adapter.notifyDataSetChanged();
            _itemList.invalidateViews();
        }
    };



    private OnClickListener onClickListener = new OnClickListener(){
        @Override
        public void Click(int position){
            posi = position;
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SelfEditActivity.this);

            //alertDialogBuilder.setTitle("*");
            alertDialogBuilder
                    .setMessage("Please select what to do.")
                    .setPositiveButton("Delete Movie", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            pathlist.remove(posi);
                            thumbnaillist.remove(posi);
                            _adapter.removeItemAt(posi);
                            _itemList.invalidateViews();

                        }
                    })
                    .setNegativeButton("Play Movie", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            videoView = (VideoView) findViewById(R.id.videoView);
                            if (mediaController1 == null) {
                                mediaController1 = new MediaController(SelfEditActivity.this);
                                mediaController1.setAnchorView(videoView);
                                videoView.setMediaController(mediaController1);
                            }
                            try {
                                videoView.setVideoPath(_adapter.getItemPath(posi));
                            } catch (Exception e) {
                                Log.e("Error", e.getMessage());
                                e.printStackTrace();
                            }


                            videoView.requestFocus();

                            videoView.setOnPreparedListener(new OnPreparedListener() {
                                public void onPrepared(MediaPlayer mediaPlayer) {

                                    videoView.seekTo(videoposition);
                                    if (videoposition == 0) {
                                        videoView.start();
                                    }

                                    mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                                        @Override
                                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                                            mediaController1.setAnchorView(videoView);
                                        }
                                    });
                                }
                            });


                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCanceledOnTouchOutside(true);
            alertDialog.show();

        }
    };



    public class AppListAdapter extends BaseAdapter
    {
        private ArrayList<AppListItem> _items = new ArrayList<AppListItem>();
        private LayoutInflater _inflater;
        private Context context;


        public AppListAdapter(Context $context)
        {
            context = $context;
            _inflater = LayoutInflater.from($context);
        }

        public void addItem(CharSequence $id, Bitmap $thumb, String $path)
        {
            AppListItem item = new AppListItem(getCount(), $id, $thumb, $path);
            _items.add(item);
        }

        public void addItemAt(int $index, AppListItem $item)
        {
            _items.add($index, $item);
        }


        /**
         * 아이콘 변경
         * @param $index 위치
         * @param $thumb 썸네일
         */
        public void setItemIcon(int $index, Bitmap $thumb)
        {
            _items.get($index)._thumb = $thumb;
            notifyDataSetChanged();
        }


        /**
         * 특정 위치의 아이템 지우기
         * @param $index 위치
         */
        public AppListItem removeItemAt(int $index)
        {
            return _items.remove($index);
        }


        /**
         * 항목들 전부 지우기
         */
        public void clear()
        {
            _items.clear();
        }


        @Override
        public int getCount()
        {
            return _items.size();
        }


        // 선택 위치의 object 가져오기
        @Override
        public Object getItem(int $index)
        {
            return _items.get($index);
        }


        // 선택 위치의 위치값 가져오기
        @Override
        public long getItemId(int $index)
        {
            return _items.get($index)._id;
        }


        // 선택 위치의 id 가져오기
        public CharSequence getItemAuId(int $index)
        {
            return _items.get($index)._auid;
        }


        // 선택 위치의 썸네일 가져오기
        public Bitmap getItemThumb(int $index)
        {
            return _items.get($index)._thumb;
        }


        // 선택 위치의 경로를 가져오기
        public String getItemPath(int $index)
        {
            return _items.get($index)._path;
        }



        @Override
        public View getView(int $index, View $convertView, ViewGroup $parent)
        {

            // 이미지 뷰 내용

            View view = _inflater.inflate(R.layout.list_item_app_list, null);
            ImageView icon = (ImageView) view.findViewById(R.id.item_icon);
            icon.setImageBitmap(_items.get($index)._thumb);
            icon.setScaleType(ImageView.ScaleType.CENTER_CROP);
            icon.setPadding(1, 1, 1, 1);
            view.setLayoutParams(new DragDropGridView.LayoutParams(250, 250));

            return view;
        }
    }




    private String getPath(Uri uri) //선택한 비디오의 경로를 얻는 클래스
    {
        String[] projection = { MediaStore.Video.Media.DATA};
        //미디어스토어를 통해 영상의 데이터를 받아, projection에 정보를 추가한다.
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        //query를 통해 정보를 읽어온다.
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst(); //cursor를 원하는 위치로 옮긴다
        return cursor.getString(column_index); //원하는 위치에 있는 미디어의 경로를 얻어온다.
    }
}