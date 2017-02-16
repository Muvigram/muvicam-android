package com.estsoft.muvigram.ui.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.estsoft.muvigram.R;

/**
 * Created by jaylim on 16/02/2017.
 */

public class ExitDialogFragment extends DialogFragment {

  /**
   * To display the dialog fragment, this tag should might be provided
   * to add this fragment to transaction.
   */
  public static final String TAG = ExitDialogFragment.class.getName();

  public static ExitDialogFragment newInstance() {
    return new ExitDialogFragment();
  }



  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    return new AlertDialog.Builder(getActivity())
        .setView(R.layout.dialog_exit)
        .setPositiveButton(android.R.string.yes, (dialog, id) -> getActivity().finishAffinity())
        .setNegativeButton(android.R.string.no, (dialog, id) -> dialog.dismiss())
        .create();
  }
}
