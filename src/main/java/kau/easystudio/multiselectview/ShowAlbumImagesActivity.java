package kau.easystudio.multiselectview;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import kau.easystudio.R;
import kau.easystudio.ui.EditProcessActivity;

public class ShowAlbumImagesActivity extends Activity implements ShowAlbumImagesAdapter.ViewHolder.ClickListener {

    private RecyclerView mRecyclerView;
    private ShowAlbumImagesAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private TextView dur_tv;
    private GridView gridview;
    private Button btnSelection;
    private ArrayList<AlbumsModel> albumsModels;
    private int mPosition;

    private String tmppath;
    private Bitmap thumbnail;
    private static ArrayList<String> thumbnailpath;
    private ArrayList<String> videospath;

    private int position_theme;
    private int selection_theme;
    private String music_name;
    private int music_dur;
    private String[] uri;

    ActionBar mActionbar;
    public ArrayList<Uri> mShareImages = new ArrayList<Uri>();
    LinearLayout scrollview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_gallery_images);
        btnSelection = (Button) findViewById(R.id.btnShow);
        dur_tv = (TextView) findViewById(R.id.dur_tv);


        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        mActionbar=getActionBar();
        mActionbar.setLogo(R.mipmap.ic_launcher);
        mActionbar.setDisplayUseLogoEnabled(true);
        mActionbar.setDisplayShowHomeEnabled(true);
        mActionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.whitepatternbackground2));
        // mActionbar.setBackgroundDrawable(new ColorDrawable(0xFFF3691C));
        mActionbar.setTitle(Html.fromHtml("<font color='#726f6e'> VIDEO GALLERY </font>"));

        position_theme = b.getInt("position");
        selection_theme = b.getInt("selection");
        music_name = b.getString("musicname");

        scrollview = (LinearLayout) findViewById(R.id.scrollview);

        thumbnailpath = new ArrayList<String>(); //스크롤뷰에 들어갈 썸네일

        mPosition = (int) getIntent().getIntExtra("position", 0);
        albumsModels = (ArrayList<AlbumsModel>) getIntent().getSerializableExtra("albumsList");
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        mRecyclerView.setHasFixedSize(true);

        mAdapter = new ShowAlbumImagesAdapter(ShowAlbumImagesActivity.this, getAlbumImages(), this);

        mRecyclerView.setAdapter(mAdapter);

        StaggeredGridLayoutManager mLayoutManager;
        mLayoutManager = new StaggeredGridLayoutManager(2, 1);
        mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
        mLayoutManager.setOrientation(StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        int spanCount = 2; // 3 columns
        int spacing = 20; // 50px
        boolean includeEdge = true;
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, includeEdge));

        btnSelection.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btnShow:
                        Intent intent = new Intent(ShowAlbumImagesActivity.this, EditProcessActivity.class);

                        int cnt = mShareImages.size();

                        uri = new String[cnt];
                        for (int i = 0; i < cnt; i++) {
                            String tmp = getPath(mShareImages.get(i));
                            uri[i] = tmp;
                        }

                        String duration = "";
                        int sum = 0;
                        for (int i = 0; i < cnt; i++) {
                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(uri[i]);
                            duration = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                            Log.e("Duration tag", duration);
                            sum += Double.parseDouble(duration);

                        }
                        Log.e("Duration sum", Integer.toString(sum));

                        music_dur = 30000;

                        if (sum >= music_dur*2 ) {
                            intent.putExtra("uri", uri);
                            intent.putExtra("musicname",music_name);
                            startActivity(intent);

                            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                            startActivity(intent);
                            break;
                        } else {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ShowAlbumImagesActivity.this);
                            alertDialogBuilder.setMessage("# You need to make your total video length more than "+(music_dur*2)/1000+" sec.\n");
                            alertDialogBuilder.setNegativeButton("OK", null);
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            alertDialog.setCanceledOnTouchOutside(true);
                            alertDialog.show();
                        }

                }
            }
        });
    }

    private Uri getImageContentUri(File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = this.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Video.Media._ID},
                MediaStore.Video.Media.DATA + "=? ",
                new String[]{filePath}, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/video/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DATA, filePath);
                return this.getContentResolver().insert(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    private ArrayList<AlbumImages> getAlbumImages() {
        int k = albumsModels.size();
        Object [][] temp;
        temp = new Object[k][];
        int size [] = new int[k];
        for(int i=0;i<k;i++){
            temp[i]=albumsModels.get(i).folderImages.toArray();
            size[i] = temp[i].length;
        }
        ArrayList<AlbumImages> paths = new ArrayList<AlbumImages>();
        for(int i=0;i<k;i++){
            for(int j=0;j<size[i];j++){
                AlbumImages albumImages = new AlbumImages();
                albumImages.setAlbumImages((String)temp[i][j]);
                paths.add(albumImages);
            }
        }
        return paths;
    }

    @Override
    public void onItemClicked(int position) {
        toggleSelection(position);
    }

    @Override
    public boolean onItemLongClicked(int position) {
        toggleSelection(position);
        return true;
    }

    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        Uri uriPath = Uri.parse(mAdapter.getAlbumImagesList().get(position).getAlbumImages());
        String path = uriPath.getPath();
        File imageFile = new File(path);
        Uri uri = getImageContentUri(imageFile);
        tmppath = getPath(uri);
        thumbnail = ThumbnailUtils.createVideoThumbnail(tmppath, MediaStore.Images.Thumbnails.MINI_KIND);

        if (mAdapter.isSelected(position)) {
            mShareImages.add(uri);
        } else {
            mShareImages.remove(uri);
        }


        scrollview.removeAllViews();
        thumbnailpath.clear();

        ScrollView();

    }

    private String getPath(Uri uri) //선택한 비디오의 경로를 얻는 클래스
    {
        String[] projection = {MediaStore.Video.Media.DATA};
        //미디어스토어를 통해 영상의 데이터를 받아, projection에 정보를 추가한다.
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        //query를 통해 정보를 읽어온다.

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
        cursor.moveToFirst(); //cursor를 원하는 위치로 옮긴다
        return cursor.getString(column_index); //원하는 위치에 있는 미디어의 경로를 얻어온다.
    }

    public void ScrollView(){
        int cnt = mShareImages.size();
        for (int i = 0; i < cnt; i++) {
            String tmp = getPath(mShareImages.get(i));
            thumbnailpath.add(tmp);
        }
        for (int i = 0; i < cnt; i++) {
            scrollview.addView(insertPhoto(thumbnailpath.get(i)));
        }

        if(thumbnailpath.size()>=1) {
            String dur1 = "";
            MediaMetadataRetriever mmr1 = new MediaMetadataRetriever();
            mmr1.setDataSource(thumbnailpath.get(cnt - 1));
            dur1 = mmr1.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);

            double dur_sum = 0;
            String dur2 = "";
            for (int i = 0; i < cnt; i++) {
                MediaMetadataRetriever mmr2 = new MediaMetadataRetriever();
                mmr2.setDataSource(thumbnailpath.get(i));
                dur2 = mmr2.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                dur_sum += Double.parseDouble(dur2);
            }
            dur_tv.setText("# Selected video length is "+ Double.parseDouble(dur1)/1000+" sec. " +
                    "\n# The total length is "+ dur_sum/1000+" sec. ");
        }else{
            dur_tv.setText("");
        }

    }


    View insertPhoto(String path) {
        Bitmap bm = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setLayoutParams(new ViewGroup.LayoutParams(250, 250));
        layout.setGravity(Gravity.CENTER);

        ImageView imageView = new ImageView(getApplicationContext());
        imageView.setLayoutParams(new ViewGroup.LayoutParams(220, 220));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(bm);

        layout.addView(imageView);
        return layout;
    }



}