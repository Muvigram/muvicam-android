package com.estsoft.muvicam.ui.splash;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.estsoft.muvicam.R;

import timber.log.Timber;

/**
 * Created by jaylim on 24/01/2017.
 */

public class PermissionManager {

  private final String[] mPermissions;
  private final int mRequestCode;
  private final Context mContext;

  public PermissionManager(Context context, String[] permissions, int requestCode) {
    mContext = context;
    mPermissions = permissions;
    mRequestCode = requestCode;
  }

  public String[] getPermissions() {
    return mPermissions;
  }

  public int getRequestCode() {
    return mRequestCode;
  }

  public boolean hasPermissionsGranted() {
    for (String permission : mPermissions) {
      if (ContextCompat.checkSelfPermission(mContext, permission)
          != PackageManager.PERMISSION_GRANTED) {
        return false;
      }
    }
    return true;
  }

  public boolean shouldShowRequestPermissionsRationale() {
    for (String permission : mPermissions) {
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
          ((Activity) mContext).shouldShowRequestPermissionRationale(permission)) {
        return true;
      }
    }
    return false;
  }

  public SettingDialog createSettingDialog(String message) {
    return SettingDialog.newInstance(message);
  }

  public DetailDialog createDetailDialog(String message) {
    return DetailDialog.newInstance(message, mPermissions, mRequestCode);
  }

  public static class SettingDialog extends DialogFragment {
    private static final String ARG_MESSAGE = "detail_permissionDeniedMessage";

    public static SettingDialog newInstance(String message) {
      SettingDialog dialog = new SettingDialog();
      Bundle args = new Bundle();

      args.putString(ARG_MESSAGE, message);
      dialog.setArguments(args);
      return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      Activity activity = getActivity();
      return new AlertDialog.Builder(activity)
          .setMessage(getArguments().getString(ARG_MESSAGE))
          .setNegativeButton(R.string.splash_button_cancel, (dialogInterface, i) -> activity.finish())
          .setPositiveButton(R.string.splash_button_detail_setting, (dialogInterface, i) -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
          })
          .create();
    }
  }

  public static class DetailDialog extends DialogFragment {
    private static final String ARG_MESSAGE = "permissionRequest_explain";
    private static final String ARG_PERMISSIONS = "permissionRequest_permissions";
    private static final String ARG_REQUEST_CODE = "permissionRequest_request_code";

    public static DetailDialog newInstance(String message, String[] permissions, int requestCode) {
      DetailDialog dialog = new DetailDialog();

      Bundle args = new Bundle();
      args.putString(ARG_MESSAGE, message);
      args.putStringArray(ARG_PERMISSIONS, permissions);
      args.putInt(ARG_REQUEST_CODE, requestCode);

      dialog.setArguments(args);
      return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      final Activity activity = getActivity();

      String message = getArguments().getString(ARG_MESSAGE);
      String[] permissions = getArguments().getStringArray(ARG_PERMISSIONS);
      int requestCode = getArguments().getInt(ARG_REQUEST_CODE);

      if (permissions == null) {
        Timber.e("No permission error.");
        return new AlertDialog.Builder(activity)
            .setMessage("There was an error to checking permissions.")
            .setPositiveButton(R.string.splash_button_ok, (dialogInterface, i) -> activity.finish())
            .create();
      }

      return new AlertDialog.Builder(activity)
          .setMessage(message)
          .setPositiveButton(R.string.splash_button_ok, (dialog, i) ->
              requestPermissions(permissions, requestCode)
          )
          .setNegativeButton(R.string.splash_button_cancel, (dialog, i) -> {
              activity.finish();
          })
          .create();
    }
  }
}



