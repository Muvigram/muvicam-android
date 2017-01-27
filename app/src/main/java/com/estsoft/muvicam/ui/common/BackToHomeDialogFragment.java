package com.estsoft.muvicam.ui.common;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.ui.base.BaseActivity;
import com.estsoft.muvicam.ui.home.HomeActivity;
import com.estsoft.muvicam.ui.share.ShareActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by estsoft on 2017-01-24.
 */

public class BackToHomeDialogFragment extends DialogFragment {
    public static final String TAG = "BackToHoment";
    private static final String ARG_MSG = "BackToHomeDialogFragment.arg_msg";

    public static BackToHomeDialogFragment newInstance( String msg ) {
        BackToHomeDialogFragment fragment = new BackToHomeDialogFragment();
        Bundle args = new Bundle();
        args.putString( ARG_MSG, msg );
        fragment.setArguments( args );
        return fragment;
    }

    private String mMassage;

//    @BindView(R.id.fragment_tohome_dialog_title)      TextView mTitle;
    @BindView(R.id.fragment_tohome_dialog_msg)      TextView mMsg;

    Unbinder mUnbinder;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mMassage = getArguments().getString(ARG_MSG, "MSG_EMPTY");

        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.fragment_dalog_back_home, null);
        mUnbinder = ButterKnife.bind(this, view);

//        mTitle.setText(getResources().getString(R.string.app_name));
        mMsg.setText( mMassage );

        AlertDialog.Builder builder = new AlertDialog.Builder( getActivity() )
                .setView(view)
                .setPositiveButton(R.string.dialog_action_ok, this::onPositive)
                .setNegativeButton(R.string.dialog_action_cancel, this::onNegative);

        Dialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.width = (int)px;
            dialog.getWindow().setAttributes( params );
        });


        return dialog;
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

    private void onPositive(DialogInterface dialogInterface, int i) {
        Intent intent = StackRemoverActivity.getIntent( getContext() );
        startActivity( intent );
    }

    private void onNegative(DialogInterface dialogInterface, int i) {
        dismiss();
    }
}
