package com.estsoft.muvicam.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;

import com.estsoft.muvicam.R;

/**
 * Created by jaylim on 12/13/2016.
 */

public final class DialogFactory {

  public static Dialog createSimpleOkErrorDialog(Context context, String title, String message) {
    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setNeutralButton(R.string.dialog_action_ok, null);
    return alertDialog.create();
  }

  public static Dialog createSimpleOkErrorDialog(Context context,
                                                 @StringRes int titleResource,
                                                 @StringRes int messageResource) {

    return createSimpleOkErrorDialog(context,
        context.getString(titleResource),
        context.getString(messageResource));
  }

  public static Dialog createGenericErrorDialog(Context context, String message) {
    AlertDialog.Builder alertDialog = new AlertDialog.Builder(context)
        .setTitle(context.getString(R.string.dialog_error_title))
        .setMessage(message)
        .setNeutralButton(R.string.dialog_action_ok, null);
    return alertDialog.create();
  }

  public static Dialog createGenericErrorDialog(Context context, @StringRes int messageResource) {
    return createGenericErrorDialog(context, context.getString(messageResource));
  }

  public static ProgressDialog createProgressDialog(Context context, String message) {
    ProgressDialog progressDialog = new ProgressDialog(context);
    progressDialog.setMessage(message);
    return progressDialog;
  }

  public static ProgressDialog createProgressDialog(Context context,
                                                    @StringRes int messageResource) {
    return createProgressDialog(context, context.getString(messageResource));
  }


  // clickListener : ok = -1; cancel = -2;
  // @jaylim you can change this!
   public static AlertDialog createOkCancelDialog( Context context, String title, String message,
                                                  DialogInterface.OnClickListener clickListener) {
       AlertDialog.Builder builder = new AlertDialog.Builder(context);
       builder.setMessage(message);
       builder.setPositiveButton(context.getString(R.string.dialog_action_ok), clickListener);
       builder.setNegativeButton(context.getString(R.string.dialog_action_cancel), clickListener);

       AlertDialog alertDialog = builder.create();
       if (title != null
               && !title.equals("")
               && !title.equals(context.getString(R.string.dialog_empty_title)))
           alertDialog.setTitle( title );

    return alertDialog;
  }

    public static AlertDialog createOkCancelDialog( Context context,
                                                  @StringRes int titleResource,
                                                  @StringRes int messageResource,
                                                  DialogInterface.OnClickListener clickListener) {
    return createOkCancelDialog(context,
            context.getString(titleResource),
            context.getString(messageResource),
            clickListener);
    }


}

