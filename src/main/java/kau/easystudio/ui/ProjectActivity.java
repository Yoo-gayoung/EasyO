package kau.easystudio.ui;
import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.media.ThumbnailUtils;
import android.text.Html;

import kau.easystudio.R;
import kau.easystudio.db.ProjectsDBManager;

public class ProjectActivity extends Activity {
    /* Called when the activity is first created. */
    ActionBar mActionbar;
    int newProjectType = -1; // -1 : default value(not changed), 0 :Self Editing, 1 :Ontouch Edting

    /*=================Database area=================*/
    public ProjectsDBManager mDbManager = null;
    String[] columns = new String[]{"project_id"};
    int projectCount=0; // variable to count the projects number.
    static final String     TABLE_PROJECTS  = "Projects";
    /*===============================================*/


    private GridView mGridView = null;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bar_menu_project, menu);
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

            case R.id.newprojectbutton: {
                makeNewProject();
                return true;
            }
            default: {
                return false;
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mDbManager = ProjectsDBManager.getInstance( this );
        Cursor dbc = mDbManager.query(TABLE_PROJECTS,columns, null, null, null, null, null);

        if (dbc != null) {
            int temp = 0;
            while (dbc.moveToNext()) {
                temp = dbc.getInt(0);
                if(projectCount<temp){
                    projectCount=temp;
                }
            }
        }
        dbc.close();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);

        mActionbar=getActionBar();
        mActionbar.setLogo(R.mipmap.ic_launcher);
        mActionbar.setDisplayUseLogoEnabled(true);
        mActionbar.setDisplayShowHomeEnabled(true);
        mActionbar.setTitle(Html.fromHtml("<font color='#726f6e'> PROJECTS </font>"));
        mActionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.whitepatternbackground2));
        mGridView = (GridView)findViewById(R.id.projectGridView);
        mGridView.setAdapter(new ImageAdapter(projectCount));
    }

    public void makeNewProject(){
        startProjectNamingDialog();
    }

    public void startProjectNamingDialog(){
        /* giving a textedit to name the project name. */
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(this, ProjectNamingDialog.class);
        intent.setComponent(componentName);
        startActivity(intent);
    }



    public class ImageAdapter extends BaseAdapter {
        int projectCnt;
        private Integer nonProjectThumbId=R.drawable.nonprojectimg2;

        public ImageAdapter(int projectCount){
            projectCnt=projectCount;
        }
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            if(projectCnt!=0) {
                Cursor c = managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                // return c.getCount(); // 여기 임시적으로 return 1로 줌 나중에 고쳐야지~
                return 1;
            }
            else{
                return 1;
            }
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;

            if (convertView == null) {
                imageView = new ImageView(mGridView.getContext());
                imageView.setLayoutParams(new GridView.LayoutParams(1500, 500));
            } else {
                imageView = (ImageView)convertView;
            }
            if(projectCnt!=0) {
                /*
                imageView.setImageResource(nonProjectThumbId);
                Cursor c = managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                //Log.v("태그", String.valueOf(position));
                c.moveToPosition(position);
                int dataIndex = c.getColumnIndex(MediaStore.Video.Media.DATA);
                String path = c.getString(dataIndex);

                Bitmap bmp = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
                bmp = Bitmap.createScaledBitmap(bmp, 1000, 256, false);

                imageView.setImageBitmap(bmp);*/
                /*이 부분도 고쳐주어야함*/
                imageView.setImageResource(nonProjectThumbId);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeNewProject();
                    }
                });
            }
            else {
                imageView.setImageResource(nonProjectThumbId);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        makeNewProject();
                    }
                });
            }
            return (View)imageView;
        }

    }

}