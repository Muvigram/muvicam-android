package com.estsoft.muvigram.ui.common;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.estsoft.muvigram.ui.home.HomeActivity;

/**
 * Created by jaylim on 27/01/2017.
 */

public class StackRemoverActivity extends AppCompatActivity {

  public static Intent getIntent(Context packageContext) {
    Intent intent = new Intent(packageContext, StackRemoverActivity.class);
    return intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent homeIntent = HomeActivity.newIntent(this);
    startActivity(homeIntent);
    this.finish();
  }
}
