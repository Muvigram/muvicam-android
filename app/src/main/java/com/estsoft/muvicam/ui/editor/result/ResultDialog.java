package com.estsoft.muvicam.ui.editor.result;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.ui.home.HomeActivity;

/**
 * Created by Administrator on 2017-01-22.
 */

public class ResultDialog extends Dialog {
    LinearLayout editorToHome;
    ImageView editorToHomeNo, editorToHomeYes;
    Context context;
    public ResultDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowManager.LayoutParams lpWindow = new WindowManager.LayoutParams();
        lpWindow.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        lpWindow.dimAmount = 0.8f;
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        getWindow().setAttributes(lpWindow);
        setContentView(R.layout.layout_dialog_editor_result_to_home);
        editorToHome = (LinearLayout) findViewById(R.id.editor_result_ask_to_home);
        editorToHomeYes = (ImageView) findViewById(R.id.editor_result_ask_to_home_yes);
        editorToHomeNo = (ImageView) findViewById(R.id.editor_result_ask_to_home_no);
        editorToHomeYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 context.startActivity(HomeActivity.newIntent(context));
            }
        });
        editorToHomeNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
