package kau.easystudio.ui;

import kau.easystudio.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class ProjectNamingDialog extends Activity {
    private Button nextBtn;
    private Button cancelBtn;
    private EditText projectNameEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_dialog_name);
        nextBtn = (Button) findViewById(R.id.next);
        nextBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                startSelectingProjectTypeDialog();
            }
        });
        cancelBtn = (Button) findViewById(R.id.cancel_0);
        cancelBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                cancelDialog();
            }
        });
        projectNameEdit = (EditText) findViewById(R.id.project_name_query);
        projectNameEdit.setText("Project");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
    }

    private void startSelectingProjectTypeDialog() {
        String newProjectName=projectNameEdit.getText().toString();
        Intent intent = new Intent();
        ComponentName componentName = new ComponentName(this, SelectingProjectTypeDialog.class );
        intent.setComponent(componentName);
        Bundle bundleData = new Bundle();
        bundleData.putString("PROJECT_NAME",newProjectName);
        intent.putExtra("PROJECT_NAME_DATA", bundleData);
        Log.e("재현태그", "1");
        startActivity(intent);
    }
    private void cancelDialog() {
        finish();
    }
}

