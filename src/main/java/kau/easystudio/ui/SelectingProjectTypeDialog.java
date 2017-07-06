package kau.easystudio.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;

import kau.easystudio.R;
import kau.easystudio.db.ProjectsDBManager;


public class SelectingProjectTypeDialog extends Activity {

    static final String TABLE_PROJECTS = "Projects";
    static final String TABLE_CLIP = "ClipTable";
    private Button finishBtn;
    private Button cancelBtn;
    RadioGroup projectTypeRadio;
    public ProjectsDBManager mDbManager = null;
    String[] columns = new String[]{"project_id"};

    int newProjectId = 0;
    String newProjectName = null;
    int newProjectType = -1; // -1 : default value(not changed), 0 :Self Editing, 1 :Ontouch Edting

    int projectCount = 0;

    public static final int TYPE_FOR_SELF_EDITING = 0;
    public static final int TYPE_FOR_EASY_EDITING = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog_type);
        Intent intent = getIntent();
        Bundle bundleData = intent.getBundleExtra("PROJECT_NAME_DATA");
        newProjectName = bundleData.getString("PROJECT_NAME");
        projectTypeRadio = (RadioGroup) findViewById(R.id.radio_project_type);

        /****************************************************/

        finishBtn = (Button) findViewById(R.id.finish);
        finishBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startEditingProjectActivity();
            }
        });
        cancelBtn = (Button) findViewById(R.id.cancel_1);
        cancelBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                cancelDialog();
            }
        });
        /****************************************************/

    }

    private void newProjectDbProcess() {
        mDbManager = ProjectsDBManager.getInstance(this);
        Cursor dbc = mDbManager.query(TABLE_PROJECTS, columns, null, null, null, null, null);

        if (dbc != null) {
            int temp = 0;
            while (dbc.moveToNext()) {
                temp = dbc.getInt(0);
                if (projectCount < temp) {
                    projectCount = temp;
                }
            }
        }
        newProjectId = projectCount + 1;
        ContentValues addRowValue = new ContentValues();
        addRowValue.put("project_name", newProjectName);
        addRowValue.put("project_type", newProjectType);
        mDbManager.insert(addRowValue, TABLE_PROJECTS);

       // mDbManager.makeNewProcessTable(TABLE_CLIP, newProjectId);
        // 동영상 클립정보에 대한 테이블의 이름을 Cliptable[projectID]로 생성,ex)두번째 프로젝트의 경우 ClipTable의 이름은 ClipTable2

        /*
        ContentValues addRowValue1= new ContentValues();
        addRowValue1.put("clip_sequence",1);
        addRowValue1.put("media_address","ABC");
        addRowValue1.put("clip_start_time", 1);
        addRowValue1.put("clip_end_time", 1);
        mDbManager.insert(addRowValue1, "Cliptable1");
        String [] columns1 = new String[]{"clip_id","clip_sequence","media_address"};
        dbc = mDbManager.query("Cliptable1",columns1, null, null, null, null, null);
        Integer temporary;

        if(dbc != null){
            while(dbc.moveToNext()){
                temporary= dbc.getInt(0);
                String abc =dbc.getString(2);
                Log.e("재현태그10",temporary.toString());
                Log.e("재현태그20",abc);
            }
        }*/
        dbc.close();
    }

    private void startEditingProjectActivity() {
        if (projectTypeRadio.getCheckedRadioButtonId() == R.id.radio_self) {
            newProjectType = TYPE_FOR_SELF_EDITING;
            newProjectDbProcess();
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName(this, SelfEditActivity.class);
            intent.setComponent(componentName);
            Bundle bundleData = new Bundle();
            bundleData.putInt("PROJECT_ID", newProjectId);
            bundleData.putString("PROJECT_NAME", newProjectName);
            bundleData.putInt("PROJECT_TYPE", newProjectType);
            intent.putExtra("NEW_PROJECT_DATA", bundleData);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left,android.R.anim.slide_out_right);
        } else if (projectTypeRadio.getCheckedRadioButtonId() == R.id.radio_onetouch) {
            //go to easy editing
            //need to be coded
            Intent intent = new Intent();
            ComponentName componentName = new ComponentName(this, OneTouchEditActivity.class);
            intent.setComponent(componentName);
            startActivity(intent);
        }
    }

    private void cancelDialog() {
        finish();
    }
}