package com.estsoft.muvigram.ui.common;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.estsoft.muvigram.R;

/**
 * Created by jaylim on 16/02/2017.
 */

public class UnsupportedDialogFragment extends DialogFragment {
  /**
   * To display the dialog fragment, this tag should might be provided
   * to add this fragment to transaction.
   */
  public static final String TAG = UnsupportedDialogFragment.class.getName();

  public static UnsupportedDialogFragment newInstance() {
    return new UnsupportedDialogFragment();
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = new AlertDialog.Builder(getActivity())
        .setView(R.layout.dialog_unsupported)
        .setPositiveButton(R.string.dialog_unsupported_quit_button, (d, i) -> getActivity().finishAffinity())
        .create();
    return dialog;
  }


}
