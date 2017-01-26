package com.estsoft.muvicam.ui.home.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.estsoft.muvicam.R;
import com.estsoft.muvicam.model.Music;
import com.estsoft.muvicam.ui.home.HomeActivity;
import com.estsoft.muvicam.ui.library.LibraryActivity;
import com.estsoft.muvicam.ui.share.ShareActivity;
import com.estsoft.muvicam.util.FileUtil;
import com.estsoft.muvicam.util.MusicPlayer;
import com.estsoft.muvicam.util.RxUtil;
import com.jakewharton.rxbinding.view.RxView;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.estsoft.muvicam.util.AnimationUtil.*;
import static com.estsoft.muvicam.util.AnimationUtil.getClickingAnimation;

/**
 * Camera Fragment.
 * <p>
 * Created by jaylim on 12/12/2016.
 */
public class CameraFragment extends Fragment implements CameraMvpView {

  public static CameraFragment newInstance() {
    return new CameraFragment();
  }

  public static CameraFragment get(Fragment fragment) {
    return (CameraFragment) fragment.getParentFragment();
  }

  private Unbinder mUnbinder;

  @BindView(R.id.camera_shoot_button)             ImageButton mShootButton;
  @BindView(R.id.camera_music_button)             AlbumArtButton mMusicButton;
  @BindView(R.id.camera_library_button)           LibraryThumbnailButton mLibraryButton;
  @BindView(R.id.camera_selfie_button)            ImageButton mSelfieButton;
  @BindView(R.id.camera_cut_button)               ImageButton mCutButton;
  @BindView(R.id.camera_ok_button)                ImageButton mOkButton;
  @BindView(R.id.camera_texture_view)             ResizableTextureView mTextureView;
  @BindView(R.id.camera_container_music_cut)      FrameLayout mMusicCutContainer;
  @BindView(R.id.camera_base_line)                View mBaseLineView;
  @BindView(R.id.camera_stack_bar)                StackBar mStackBar;
  @BindView(R.id.camera_stack_trashbin_container) FrameLayout mStackTrashbinContainer;

  @OnClick(R.id.camera_cut_button)
  public void _musicCut(View v) {
    if (mMusic == null || !mVideoStack.isEmpty()) {
      return;
    }

    FragmentManager cfm = getChildFragmentManager();
    Fragment fragment = cfm.findFragmentById(R.id.camera_container_music_cut);

    if (fragment == null) {
      fragment = MusicCutFragment.newInstance(mMusic, mMusicPlayer.getOffset());
      final FragmentTransaction transaction = cfm.beginTransaction()
          .add(R.id.camera_container_music_cut, fragment);

      v.startAnimation(getClickingAnimation(getActivity(), new AnimationEndListener() {
        @Override
        public void onAnimationEnd(Animation animation) {
          transaction.commit();
          requestUiChange(UI_LOGIC_DURING_CUT_MUSIC);
        }
      }));
    }
  }

  @OnClick(R.id.camera_selfie_button)
  public void _shiftSelfieMode(View v) {
    if (!mVideoStack.isEmpty()) {
      return;
    }

    v.startAnimation(getClickingAnimation(getActivity(), new AnimationEndListener() {
      @Override
      public void onAnimationEnd(Animation animation) {
        closeCamera();
        closeRecorder();
        mSelfieMode = !mSelfieMode;
        openRecorder();
        openCamera();
      }
    }));
  }

  @OnClick(R.id.camera_ok_button)
  public void _completeVideo(View v) {
    if (mVideoStack.isEmpty() || mMusicPlayer.getRelativePosition() < 5000) {
      return;
    }
    v.startAnimation(getClickingAnimation(getActivity(), new AnimationEndListener() {
      @Override
      public void onAnimationEnd(Animation animation) {
        // saveAllFilesInStack();
        sendAllInformation();
        if (mVideoStack.isEmpty()) {
          requestUiChange(UI_LOGIC_BEFORE_SHOOTING);
        }
      }
    }));
  }

  @OnClick(R.id.camera_library_button)
  public void _goToLibrary(View v) {
    v.startAnimation(getClickingAnimation(getActivity(), new AnimationEndListener() {
      @Override
      public void onAnimationEnd(Animation animation) {
        getActivity().startActivity(LibraryActivity.newIntent(getActivity()));
      }
    }));
  }

  // OnClick
  public void _deleteRecentVideo(View v) {
    if (mVideoStack.isEmpty()) {
      return;
    }
    v.startAnimation(getClickingAnimation(getActivity(), new AnimationEndListener() {
      @Override
      public void onAnimationEnd(Animation animation) {
        hideTrashbin();
        popVideoFile();
        mMusicPlayer.rewindPlayer(mStackBar.deleteRecentRecord() + mMusicPlayer.getOffset());
        updateTrashbin();
      }
    }));
  }

  public void sendAllInformation() { // TODO -refactoring

    String[] videoPaths = new String[mVideoStack.size()];
    int i = 0;
    int j = 0;
    for (File file : mVideoStack.toArray(new File[mVideoStack.size()])) {
      String fileName = String.format(Locale.US, "%d_%d.mp4",
          System.currentTimeMillis(), j++);
      File newFile = new File(mDir, fileName);
      new Thread() {
        @Override
        public void run() {
          super.run();
          try {
            FileUtil.copyFile(file, newFile);
          } catch (IOException e) {
            e.printStackTrace();
          } finally {
            Toast.makeText(getActivity(), "SAVE : " + mDir.toString() + fileName, Toast.LENGTH_SHORT).show();
            Timber.e("Save the file $$ dir : %s, file : %s\n", mDir.toString(), fileName);
          }
        }
      }.run();
      videoPaths[i++] = newFile.toString();
    }

    int[] videoOffsets = new int[mOffsetStack.size()];
    i = 0;
    for (Integer videoOffset : mOffsetStack.toArray(new Integer[mOffsetStack.size()])) {
      videoOffsets[i++] = videoOffset;
    }

    String mMusicPath = mMusic == null ? "" : mMusic.uri().toString();

    Intent intent = ShareActivity.newIntent(getContext(), videoPaths, videoOffsets,
        mMusicPath, mMusicPlayer.getOffset(), mMusicPlayer.getRelativePosition());

    getActivity().startActivity(intent);
  }

  // STEP - VIEW BINDING //////////////////////////////////////////////////////////////////////////

  private boolean isDown = false;
  private boolean isSubscribed = false;
  private boolean isSessionCreated = false;
  private boolean isRecording = false;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setUpStorageDir();
    startBackgroundThread();
    mMusicPlayer = new MusicPlayer(getActivity(), "silence_15_sec.mp3");
    mMusicPlayer.openPlayer();   // player
    mMusicPlayer.setUpPlayer();  //
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_camera, container, false);
    mUnbinder = ButterKnife.bind(this, view);

    final Observable<MotionEvent> sharedObservable = RxView.touches(mShootButton).share();
    // Start shooting
    sharedObservable
        .filter(e -> isSessionPreviewReady)
        .filter(e -> e.getAction() == MotionEvent.ACTION_DOWN)
        .filter(this::holdButton)
        .filter(this::shootButtonDown)
        .debounce(400, TimeUnit.MILLISECONDS, Schedulers.newThread())
        .subscribe(this::startRecording);

    // Stop shooting
    sharedObservable
        .filter(e -> e.getAction() == MotionEvent.ACTION_UP
            || e.getAction() == MotionEvent.ACTION_OUTSIDE)
        // .delay(calculateDelay(), TimeUnit.MILLISECONDS)
        .filter(this::releaseButton)        // isDown           : true
        .filter(this::preventSubscribe)     // isDown           : false, isSubscribed     : true
        .filter(this::preventCreateSession) // isSubscribed     : false, isSessionCreated : true
        .filter(this::preventRecording)     // isSessionCreated : false, isRecording      : true
        .subscribe(this::stopRecording);

    return view;
  }



  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mLibraryButton.updateThumbnailButton();
    mStackBar.setTimeBound(15000, 5000);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    initTrashbinDrawable();
    createTrashbin();
  }

  @Override
  public void onStart() {
    super.onStart();
  }

  @Override
  public void onResume() {
    super.onResume();
    openRecorder();   // Open recorder
    if (mTextureView.isAvailable()) { // camera
      openCamera();
    } else {
      mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }
  }

  @Override
  public void onPause() {
    closeCamera();  // camera
    closeRecorder();
    super.onPause();
  }

  @Override
  public void onStop() {
    super.onStop();
  }

  @Override
  public void onDestroyView() {
    mVideoStack.clear();
    mOffsetStack.clear();
    mUnbinder.unbind();
    if (mUnbinder != null) {
      mUnbinder = null;
    }
    super.onDestroyView();
  }

  @Override
  public void onDestroy() {
    mMusicPlayer.closePlayer();  // player
    stopBackgroundThread(); // TODO
    super.onDestroy();
  }

  // STEP - VIDEO RECORDING BUTTON INTERACTION ////////////////////////////////////NEED REFACTORING

  public boolean holdButton(MotionEvent motionEvent) {
    requestUiChange(UI_LOGIC_HOLD_SHOOT_BUTTON);
    return true;
  }

  public boolean shootButtonDown(MotionEvent motionEvent) {
    if (isDown) throw new RuntimeException(); // Unexpected but, just defensive code
    Timber.i("mShootButton/ACTION_DOWN");

    isDown = true;
    return true;
  }

  public void startRecording(MotionEvent motionEvent) {
    if (isSubscribed) throw new RuntimeException();

    Timber.i("mShootButton/EVENT_SUBSCRIBED");
    if (!isDown) { // Prevented by method "preventSubscribe"
      return;
    }
    isSubscribed = true;
    startRecording();
  }

  public boolean releaseButton(MotionEvent motionEvent) {
    requestUiChange(UI_LOGIC_RELEASE_SHOOT_BUTTON);
    return isDown;
  }

  public boolean preventSubscribe(MotionEvent motionEvent) {
    if (!isDown) throw new RuntimeException(); // Unexpected but for defensive code
    isDown = false;
    return isSubscribed;
  }

  public boolean preventCreateSession(MotionEvent motionEvent) {
    if (!isSubscribed) throw new RuntimeException(); // Unexpected but for defensive code
    isSubscribed = false;
    return isSessionCreated;
  }

  public boolean preventRecording(MotionEvent motionEvent) {
    if (!isSessionCreated) throw new RuntimeException(); // Unexpected but for defensive code
    isSessionCreated = false;
    return isRecording;

  }

  public void stopRecording(MotionEvent motionEvent) {
    if (!isRecording) throw new RuntimeException(); // Unexpected but for defensive code
    delayStopRecording();
  }

  // STEP - MUSIC PLAYER //////////////////////////////////////////////////////////////////////DONE

  private Music mMusic = null;
  MusicPlayer mMusicPlayer;

  public void changeMusic(Music music) {
    mMusicPlayer.setOnCompleteSetupListener(mp -> {
      mMusicPlayer = mp;
      requestUiChange(UI_LOGIC_MUSIC_UPDATE_COMPLETE);
    });

    mMusic = music;
    mMusicPlayer.setMusicAsync(music.uri());
  }

  public void changeOffset(int offset) {
    mMusicPlayer.setOffset(offset);
  }

  // STEP - STORAGE DIR ///////////////////////////////////////////////////////////////////////////

  private File mDir;
  private File mVideoFile;
  private int mVideoOffset;
  private Stack<File> mVideoStack = new Stack<>();
  private Stack<Integer> mOffsetStack = new Stack<>(); // TODO - extract from StackBar

  private void setUpStorageDir() {
    File storageRoot = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
    File dir = new File(storageRoot.getAbsolutePath() + "/camera_video_test");
    if (dir.isDirectory() || dir.mkdirs()) {
      mDir = dir;
    } else {
      // TODO - How to solve this problem?
      Timber.e("%s%s\n",
          "There was problem to find out application-specified directory. ",
          "The picture will be downloaded into the application root directory.");
      mDir = getActivity().getExternalFilesDir(null);
    }
  }

  private void createVideoFile() {
    try {
      mVideoFile = File.createTempFile("muvicam", null, getContext().getCacheDir());
      Toast.makeText(getActivity(), "CREATE : " + mVideoFile.toString(), Toast.LENGTH_SHORT).show();
      Timber.e("Create file %s\n", mVideoFile.toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void updateVideoOffset() {
    mVideoOffset = mMusicPlayer.getRelativePosition();
  }

  Subscription mStackBarSubscription;

  public void setStackBarOnListen() {
    mStackBarSubscription = mMusicPlayer.startSubscribePlayer()
        .doOnSubscribe(() -> mUiThreadHandler.post(this::hideTrashbin))
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.newThread())
        .filter(millisec -> {
          if (!isRecording) {
            mMusicPlayer.stopSubscribePlayer();
          }
          return isRecording;
        })
        .map(millisec -> (millisec - mMusicPlayer.getOffset()))
        .filter(millisec -> {
          if (millisec >= 15000) {
            stopRecording();
            return false;
          }
          return true;
        })
        .subscribe(
            mStackBar::updateStackBar,
            Throwable::printStackTrace,
            () -> {
              mStackBar.recordOffset();
              RxUtil.unsubscribe(mStackBarSubscription);
            }
        );
  }

  private void pushVideoFile() {
    if (mVideoFile != null) {
      mVideoStack.push(mVideoFile);
      mOffsetStack.push(mVideoOffset);
//      Toast.makeText(getActivity(),
//          "PUSH : " + mVideoStack.peek().toString() + "[ms : " + mOffsetStack.peek() + "]",
//          Toast.LENGTH_SHORT).show();
//      Timber.e("Push the file into the stack %s [ms : %d]\n",
//          mVideoStack.peek().toString(), mOffsetStack.peek());
    }
    if (!mVideoStack.isEmpty()) {
      mUiThreadHandler.post(() -> updateTrashbin());
      requestUiChange(UI_LOGIC_DURING_SHOOTING);
    }

  }

  private void popVideoFile() {
    if (!mVideoStack.isEmpty()) {
      File oldVideo = mVideoStack.pop();
      int oldOffset = mOffsetStack.pop();
      Toast.makeText(getActivity(),
          "POP : " + oldVideo.toString() + "[ms : " + oldOffset + "]",
          Toast.LENGTH_SHORT).show();
      Timber.e("Pop the file file the stack %s [ms : %d]\n", oldVideo, oldOffset);
    }
    if (mVideoStack.isEmpty()) {
      mUiThreadHandler.post(() -> updateTrashbin());
      requestUiChange(UI_LOGIC_BEFORE_SHOOTING);
    }
  }

  //
  private void saveAllFilesInStack() {
    int i = 0;
    for (Object obj : mVideoStack.toArray()) {
      File file = (File) obj;
      String fileName = String.format(Locale.US, "%d_%d.mp4",
          System.currentTimeMillis(), i++);
      File newFile = new File(mDir, fileName);

      new Thread() {
        @Override
        public void run() {
          super.run();
          try {
            FileUtil.copyFile(file, newFile);
          } catch (IOException e) {
            e.printStackTrace();
          } finally {
            Toast.makeText(getActivity(), "SAVE : " + mDir.toString() + fileName, Toast.LENGTH_SHORT).show();
            Timber.e("Save the file $$ dir : %s, file : %s\n", mDir.toString(), fileName);
          }
        }
      }.run();

    }
    mVideoStack.clear();
  }

  // STEP - STACK TRASH BIN ///////////////////////////////////////////////////////////////////////

  ImageButton mStackTrashbin;

  private final static int LEFT   = 0;
  private final static int CENTER = 1;
  private final static int RIGHT  = 2;

  Drawable[] mTrashbinDrawable;

  private void initTrashbinDrawable() {
    mTrashbinDrawable = new Drawable[]{
        getResources().getDrawable(R.drawable.camera_trashbin_button_left_30dp, null),
        getResources().getDrawable(R.drawable.camera_trashbin_button_center_30dp, null),
        getResources().getDrawable(R.drawable.camera_trashbin_button_right_30dp, null)
    }; // TODO - what is the second parameter of 'getDrawable' which is called theme?
  }

  private void createTrashbin() {
    mStackTrashbin = new ImageButton(getActivity());
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      mStackTrashbin.setBackgroundColor(getResources().getColor(R.color.transparent, null));
    } else {
      mStackTrashbin.setBackgroundColor(getResources().getColor(R.color.transparent));
    }
    mStackTrashbin.setOnClickListener(this::_deleteRecentVideo);
  }

  private void updateTrashbin() {
    if (mVideoStack.isEmpty()) {
      hideTrashbin();
      return;
    }
    int pos = mStackBar.getPosition();
    int trashbinWidth = mTrashbinDrawable[CENTER].getIntrinsicWidth();
    int trashbinHeight = mTrashbinDrawable[CENTER].getIntrinsicHeight();
    int barWidth = mStackBar.getBarWidth();

    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(trashbinWidth, trashbinHeight);
    params.topMargin = 0;

    if (pos < trashbinWidth / 2 + 1) {
      mStackTrashbin.setImageDrawable(mTrashbinDrawable[LEFT]);
      params.leftMargin = pos;
    } else if (barWidth - pos < trashbinWidth / 2 + 1) {
      mStackTrashbin.setImageDrawable(mTrashbinDrawable[RIGHT]);
      params.leftMargin = pos - trashbinWidth;
    } else {
      mStackTrashbin.setImageDrawable(mTrashbinDrawable[CENTER]);
      params.leftMargin = pos - trashbinWidth / 2;
    }

    mStackTrashbinContainer.addView(mStackTrashbin, params);
  }

  private void hideTrashbin() {
    mStackTrashbinContainer.removeView(mStackTrashbin);
  }

  // STEP - TEXTURE VIEW //////////////////////////////////////////////////////////////////////////

  private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
      openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
      // Size changed
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
      return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
      // texture view updated
    }
  };

  // STEP - BACKGROUND THREAD /////////////////////////////////////////////////////////////////////

  private final static String CAMERA_BACKGROUND_HANDLER_THREAD = "CameraBackground";

  HandlerThread mBackgroundThread;

  Handler mBackgroundHandler;

  private void startBackgroundThread() {
    mBackgroundThread = new HandlerThread(CAMERA_BACKGROUND_HANDLER_THREAD);
    mBackgroundThread.start();
    mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
  }

  private void stopBackgroundThread() {
    mBackgroundThread.quitSafely();
    try {
      // Waits forever for this thread to die.
      mBackgroundThread.join();
      mBackgroundThread = null;
      mBackgroundHandler = null;
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  // STEP - OPEN/CLOSE CAMERA /////////////////////////////////////////////////////////////////////

  private Semaphore mCameraOpenCloseLock = new Semaphore(1);

  private String mCameraId;

  private CameraDevice mCameraDevice;

  private void openCamera() {

    createVideoFile();
    setUpCameraOutputs();
    setUpRecorder();

    Activity activity = getActivity();
    CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);

    try {
      if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
        throw new RuntimeException("Time out waiting to lock cameraopening");
      }

      //noinspection MissingPermission
      manager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);

    } catch (CameraAccessException e) {
      e.printStackTrace();
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while trying to lock camera open ing.", e);
    }
  }

  private void closeCamera() {
    closeSession(); // session
    try {
      mCameraOpenCloseLock.acquire();
      if (null != mCameraDevice) {
        mCameraDevice.close();
        mCameraDevice = null;
      }
    } catch (InterruptedException e) {
      throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
    } finally {
      mCameraOpenCloseLock.release();
    }
  }

  private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
    @Override
    public void onOpened(@NonNull CameraDevice camera) {
      mCameraDevice = camera;
      mCameraOpenCloseLock.release();
      startPreviewing();
    }

    @Override
    public void onDisconnected(@NonNull CameraDevice camera) {
      mCameraOpenCloseLock.release();
      camera.close();
      mCameraDevice = null;
      /* need validation */
    }

    @Override
    public void onError(@NonNull CameraDevice camera, int error) {
      mCameraOpenCloseLock.release();
      camera.close();
      mCameraDevice = null;
      /* need validation */
      Activity activity = getActivity();
      if (null != activity) {
        activity.finish();
      }
    }
  };

  // STEP - SETUP CAMERA OUTPUT ///////////////////////////////////////////////////////////////DONE

  private boolean mSelfieMode = false;

  private int mHardwareLevel;

  private int mSensorOrientation;

  private Size mVideoSize;
  private Size mPreviewSize;

  private final static int BASE_DIMENSION_WIDTH = 0;
  private final static int BASE_DIMENSION_HEIGHT = 1;

  @SuppressWarnings({"SuspiciousNameCombination", "ConstantConditions"})
  private void setUpCameraOutputs() {
    Activity activity = getActivity();
    CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
    try {
      String cameraId = selectCamera(mSelfieMode, manager);

      CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

      mHardwareLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);

      StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      if (map == null) {
        return;
      }

      int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
      mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

      boolean isOrthogonal = checkOrthogonality(displayRotation, mSensorOrientation);

      // get display size
      Point displaySize = new Point();
      activity.getWindowManager().getDefaultDisplay().getRealSize(displaySize);

      int w = displaySize.x;
      int h = displaySize.y;

      Timber.e("[w : %d, h : %d]", w, h);
      Size aspectRatio = !isOrthogonal ? new Size(w, h) : new Size(h, w);

      int baseDimension = !isOrthogonal ? BASE_DIMENSION_WIDTH : BASE_DIMENSION_HEIGHT;

      int baseLength = displaySize.x;

      mVideoSize = chooseVideoSize(map.getOutputSizes(SurfaceTexture.class), aspectRatio, baseLength, baseDimension);
      mPreviewSize = choosePreviewSize(map.getOutputSizes(SurfaceTexture.class), mVideoSize, baseLength, baseDimension);

      mTextureView.setAspectRatio(
          mPreviewSize.getHeight(), mPreviewSize.getWidth()
      );

      mCameraId = cameraId;

    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  private static String selectCamera(boolean isSelfieMode, CameraManager manager) throws CameraAccessException {
    for (String cameraId : manager.getCameraIdList()) {
      CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

      Integer expectedLensFacing = isSelfieMode ? CameraCharacteristics.LENS_FACING_FRONT
          : CameraCharacteristics.LENS_FACING_BACK;

      Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
      if (lensFacing == null || !Objects.equals(lensFacing, expectedLensFacing)) {
        continue;
      }

      return cameraId;
    }
    return null;
  }

  private static boolean checkOrthogonality(int displayRotation, int cameraSensorOrientation) {
    switch (displayRotation) {
      case Surface.ROTATION_0:
      case Surface.ROTATION_180:
        return cameraSensorOrientation == 90 || cameraSensorOrientation == 270;
      case Surface.ROTATION_90:
      case Surface.ROTATION_270:
        return cameraSensorOrientation == 0 || cameraSensorOrientation == 180;
      default:
        Timber.e("Invalid display rotation : %d\n", displayRotation);
        return false;
    }
  }

  private static Size chooseVideoSize(Size[] choices, Size aspectRatio, int baseLength, int baseDimension) {
    List<Size> feasibleSize = new ArrayList<>();
    List<Size> longerHeight = new ArrayList<>();
    List<Size> longerWidth = new ArrayList<>();

    int w = aspectRatio.getWidth();
    int h = aspectRatio.getHeight();

    for (Size option : choices) {
      Timber.e("Image capture - %d : %d\n", option.getWidth(), option.getHeight());
      if (option.getHeight() <= 1080 && option.getHeight() <= baseLength) {
        if (option.getHeight() == option.getWidth() * h / w) {
          feasibleSize.add(option);
        } else if (option.getHeight() > option.getWidth() * h / w) {
          longerHeight.add(option);
        } else {
          longerWidth.add(option);
        }
      }
    }

    if (feasibleSize.size() > 0) {
      return Collections.max(feasibleSize, new SizeComparator());
    } else {
      Timber.e("Couldn't find any suitable preview size.");
      switch (baseDimension) {
        case BASE_DIMENSION_HEIGHT:
          return Collections.max(longerHeight, new SizeComparator());
        case BASE_DIMENSION_WIDTH:
          return Collections.max(longerWidth, new SizeComparator());
        default:
          Timber.e("Invalid value for baseSide parameter.");
          return choices[choices.length - 1];
      }
    }
  }

  private static Size choosePreviewSize(Size[] choices, Size aspectRatio, int baseLength, int baseDimension) {

    // Collect the supported resolutions that are at least as big as the preview Surface
    List<Size> bigEnough = new ArrayList<>();
    // Collect the supported resolutions that are smaller than the preview Surface
    List<Size> notBigEnough = new ArrayList<>();

    // TODO - Ignore the case that there is no size which has same aspect ratio with image capture.

    int w = aspectRatio.getWidth();
    int h = aspectRatio.getHeight();

    for (Size option : choices) {
      int optionLength = (BASE_DIMENSION_WIDTH == baseDimension) ?
          option.getWidth() : option.getHeight();

      // Confirm feasibility : Maximum size && Aspect ratio
      if (option.getHeight() == option.getWidth() * h / w) {
        // Big enough
        if (optionLength >= baseLength) {
          Timber.e("Big enough - %d : %d\n", option.getWidth(), option.getHeight());
          bigEnough.add(option);
        } else {
          Timber.e("Not Big enough - %d : %d\n", option.getWidth(), option.getHeight());
          notBigEnough.add(option);
        }
      }
    }

    if (bigEnough.size() > 0) {
      return Collections.min(bigEnough, new SizeComparator());
    } else if (notBigEnough.size() > 0) {
      return Collections.max(notBigEnough, new SizeComparator());
    } else {
      Timber.e("Couldn't find any suitable preview size.");
      return choices[0];
    }
  }

  static class SizeComparator implements Comparator<Size> {
    @Override
    public int compare(Size lhs, Size rhs) {
      // We cast here to ensure the multiplications won't overflow
      return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
          (long) rhs.getWidth() * rhs.getHeight());
    }

  }

  // STEP - CONTROL PREVIEW ///////////////////////////////////////////////////////////////////////

  private CaptureRequest.Builder mCaptureRequestBuilder;
  private boolean isSessionPreviewReady = false;

  private void startPreviewing() {
    createPreviewSession();
  }

  private void startRecording() {
    isSessionPreviewReady = false;
    requestUiChange(UI_LOGIC_HIDE_PERIPHERAL_BUTTONS);
    requestUiChange(UI_LOGIC_LOCK_SHOOT_BUTTON);
    createRecordSession();
  }

  private void delayStopRecording() {
    Thread stopMusic = new Thread() {
      @Override
      public void run() {
        super.run();
        Looper.prepare();
        try {
          Thread.sleep(calculateDelay());
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          mMusicPlayer.pausePlayer();
          requestUiChange(UI_LOGIC_SHOW_PERIPHERAL_BUTTONS);
          requestUiChange(UI_LOGIC_HOLD_SHOOT_BUTTON);
          isRecording = false;
        }
      }
    };

    Thread stopRecording = new Thread() {
      @Override
      public void run() {
        super.run();
        Looper.prepare();
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        } finally {
          requestUiChange(UI_LOGIC_RELEASE_SHOOT_BUTTON);
          stopRecorder();
          startPreviewing();
        }
      }
    };

    stopMusic.start();
    stopRecording.start();
  }

  private int calculateDelay() {
    Timber.e("CUR : %d, STD : %d\n", mMusicPlayer.getRelativePosition(), mVideoOffset + 1000);
    if (mMusicPlayer.getRelativePosition() < 14000) {
      return mMusicPlayer.getRelativePosition() < (mVideoOffset + 1000) ?
          (mVideoOffset + 1000) - mMusicPlayer.getRelativePosition() : 0;
    } else {
      return 1000;
    }
  }

  private void stopRecording() {
    mMusicPlayer.pausePlayer();
    requestUiChange(UI_LOGIC_SHOW_PERIPHERAL_BUTTONS);
    requestUiChange(UI_LOGIC_RELEASE_SHOOT_BUTTON);
    stopRecorder();
    startPreviewing();
  }

  // STEP - CREATE SESSION ////////////////////////////////////////////////////////////////////////

  private CameraCaptureSession mSession;

  private void createPreviewSession() {
    try {
      // Surface texture
      SurfaceTexture texture = mTextureView.getSurfaceTexture();
      assert texture != null;
      texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
      Surface previewSurface = new Surface(texture);

      mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
      mCaptureRequestBuilder.addTarget(previewSurface);

      closeSession();
      mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
          mPreviewSessionStateCallback, mBackgroundHandler);

    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  private void createRecordSession() {
    try {
      // Surface texture
      SurfaceTexture texture = mTextureView.getSurfaceTexture();
      assert texture != null;
      texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
      Surface previewSurface = new Surface(texture);

      // Recorder surface
      Surface recorderSurface = mRecorder.getSurface();

      List<Surface> surfaces = new ArrayList<>();
      surfaces.add(previewSurface);
      surfaces.add(recorderSurface);

      if (!isSubscribed) { // Prevent by method "preventCreateSession"
        return;
      }
      isSessionCreated = true;

      mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
      mCaptureRequestBuilder.addTarget(previewSurface);
      mCaptureRequestBuilder.addTarget(recorderSurface);

      closeSession();
      mCameraDevice.createCaptureSession(surfaces, mRecordSessionStateCallback, mBackgroundHandler);
    } catch (CameraAccessException e) {
      Timber.e(e, "CameraAccessException from creating capture session for recording.");
    }
  }

  private void closeSession() {

    if (null != mSession) {
      mSession.close();
      mSession = null;
    }
  }

  CameraCaptureSession.StateCallback mPreviewSessionStateCallback = new CameraCaptureSession.StateCallback() {

    @Override
    public void onConfigured(@NonNull CameraCaptureSession previewSession) {
      isSessionPreviewReady = true;
      mSession = previewSession;
      // start capture request
      mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
      try {
        mSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
      } catch (CameraAccessException e) {
        e.printStackTrace();
      }
    }

    @Override
    public void onConfigureFailed(@NonNull CameraCaptureSession previewSession) {
    }
  };

  CameraCaptureSession.StateCallback mRecordSessionStateCallback = new CameraCaptureSession.StateCallback() {

    @Override
    public void onConfigured(@NonNull CameraCaptureSession recordSession) {
      mSession = recordSession;

      // start capture request
      mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
      try {
        mSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
      } catch (CameraAccessException e) {
        e.printStackTrace();
      }

      if (!isSessionCreated) { // Prevent by method "preventRecording"
        startPreviewing();
        return;
      }
      isRecording = true;

      updateVideoOffset();
      setStackBarOnListen();

      // media recorder
      startRecorder();
      mMusicPlayer.startPlayer();
    }

    @Override
    public void onConfigureFailed(@NonNull CameraCaptureSession recordSession) {
    }
  };

  // STEP - MEDIA RECORDER ////////////////////////////////////////////////////////////////////DONE

  private MediaRecorder mRecorder;

  public void openRecorder() {
    if (mRecorder == null) {
      mRecorder = new MediaRecorder();
    } else {
      mRecorder.reset();
    }
  }

  public void closeRecorder() {
    if (mRecorder != null) {
      mRecorder.release();
      mRecorder = null;
    }
  }

  public void setUpRecorder() {
    try {
      if (mMusic == null) {
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      }
      mRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
      mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
      mRecorder.setOutputFile(mVideoFile.getAbsolutePath());

      // Set media source detail
      if (mMusic == null) {
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mRecorder.setAudioSamplingRate(48000);
      }
      mRecorder.setVideoEncodingBitRate(10000000);
      mRecorder.setVideoFrameRate(30);
      mRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
      mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

      mRecorder.setOrientationHint(mSensorOrientation);
      mRecorder.prepare();

    } catch (IOException e) {
      mRecorder.reset();
      e.printStackTrace();
    }
  }

  public void startRecorder() {
    try {
      mRecorder.start();
    } catch (IllegalStateException e) {
      e.printStackTrace();
      mRecorder.reset();
      setUpRecorder();
    }
  }

  public void stopRecorder() {
    try {
      mRecorder.stop(); // TODO - Error-expected point
      pushVideoFile();
      mVideoFile = null;
      createVideoFile();

    } catch (IllegalStateException e) {
      e.printStackTrace();
    } catch (RuntimeException e) {
      Timber.e(e, "Recording length was too short : ");
    } finally {
      mRecorder.reset();
      setUpRecorder();
    }
  }

  public void resetRecorder() {
    mRecorder.reset();
  }


  // STEP - UI MAIN THREAD ////////////////////////////////////////////////////////////////////DONE

  /* FUNC - UI LOGIC */
  public static final int UI_LOGIC_RESTORE_UI_CONFIGURATION = 0x000;

  public static final int UI_LOGIC_SHOW_PERIPHERAL_BUTTONS = 0x001;
  public static final int UI_LOGIC_HIDE_PERIPHERAL_BUTTONS = 0x002;

  public static final int UI_LOGIC_SHOW_ALL_BUTTONS = 0x003;
  public static final int UI_LOGIC_HIDE_ALL_BUTTONS = 0x004;

  public static final int UI_LOGIC_MUSIC_UPDATE_COMPLETE = 0x005;

  public static final int UI_LOGIC_BEFORE_SHOOTING = 0x006;
  public static final int UI_LOGIC_DURING_SHOOTING = 0x007;

  public static final int UI_LOGIC_RELEASE_SHOOT_BUTTON = 0x008;
  public static final int UI_LOGIC_HOLD_SHOOT_BUTTON = 0x009;
  public static final int UI_LOGIC_LOCK_SHOOT_BUTTON = 0x010;

  public static final int UI_LOGIC_DURING_CUT_MUSIC = 0x011;
  public static final int UI_LOGIC_FINISH_CUT_MUSIC = 0x012;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({UI_LOGIC_RESTORE_UI_CONFIGURATION,
      UI_LOGIC_SHOW_PERIPHERAL_BUTTONS, UI_LOGIC_HIDE_PERIPHERAL_BUTTONS,
      UI_LOGIC_SHOW_ALL_BUTTONS, UI_LOGIC_HIDE_ALL_BUTTONS,
      UI_LOGIC_MUSIC_UPDATE_COMPLETE,
      UI_LOGIC_BEFORE_SHOOTING, UI_LOGIC_DURING_SHOOTING,
      UI_LOGIC_RELEASE_SHOOT_BUTTON, UI_LOGIC_HOLD_SHOOT_BUTTON, UI_LOGIC_LOCK_SHOOT_BUTTON,
      UI_LOGIC_DURING_CUT_MUSIC, UI_LOGIC_FINISH_CUT_MUSIC})
  public @interface UiLogic {
  }

  private static final String UI_LOGIC = "UiLogic";

  public void requestUiChange(@UiLogic int request) {
    // Request some action to Ui thread.
    Bundle bundle = new Bundle();
    bundle.putSerializable(UI_LOGIC, request);

    Message msg = Message.obtain();
    msg.setData(bundle);
    mUiThreadHandler.sendMessage(msg);
  }

  /* FUNC - UI Thread Handler */
  Handler mUiThreadHandler = new Handler(Looper.getMainLooper()) {

    private boolean duringShootingVideo = false;
    private boolean duringCuttingMusic = false;

    @Override
    public void handleMessage(Message msg) {
      int uiLogicRequest = msg.getData().getInt(UI_LOGIC);

      switch (uiLogicRequest) {
        case UI_LOGIC_RESTORE_UI_CONFIGURATION:
          if (duringCuttingMusic) {
            Timber.e("is during cutting music? true");
            ((HomeActivity) getActivity()).disableScroll();
            mShootButton.setVisibility(View.INVISIBLE);
            mBaseLineView.setVisibility(View.INVISIBLE);
            mCutButton.setVisibility(View.INVISIBLE);
            mSelfieButton.setVisibility(View.INVISIBLE);
            mOkButton.setVisibility(View.INVISIBLE);
            mMusicButton.setVisibility(View.INVISIBLE);
            mLibraryButton.setVisibility(View.INVISIBLE);
            break;
          } else {
            Timber.e("is during cutting music? false");
            if (duringShootingVideo) {
              duringShootingVideo = true;
              mCutButton.setImageResource(R.drawable.camera_cut_button_inactive_30dp);
              mSelfieButton.setImageResource(R.drawable.camera_selfie_button_inactive_30dp);
              mOkButton.setImageResource(R.drawable.camera_ok_button_active_30dp);
              ((HomeActivity) getActivity()).disableScroll();
              break;
            } else {
              if (mMusic != null) {
                mCutButton.setImageResource(R.drawable.camera_cut_button_active_30dp);
              } else {
                mCutButton.setImageResource(R.drawable.camera_cut_button_inactive_30dp);
              }
              mSelfieButton.setImageResource(R.drawable.camera_selfie_button_active_30dp);
              mOkButton.setImageResource(R.drawable.camera_ok_button_inactive_30dp);
              ((HomeActivity) getActivity()).enableScroll();
              break;
            }
          }
        case UI_LOGIC_BEFORE_SHOOTING:
          duringShootingVideo = false;
          if (mMusic != null) {
            mCutButton.setImageResource(R.drawable.camera_cut_button_active_30dp);
          } else {
            mCutButton.setImageResource(R.drawable.camera_cut_button_inactive_30dp);
          }
          mSelfieButton.setImageResource(R.drawable.camera_selfie_button_active_30dp);
          mOkButton.setImageResource(R.drawable.camera_ok_button_inactive_30dp);
          ((HomeActivity) getActivity()).enableScroll();
          break;
        case UI_LOGIC_DURING_SHOOTING:
          duringShootingVideo = true;
          mCutButton.setImageResource(R.drawable.camera_cut_button_inactive_30dp);
          mSelfieButton.setImageResource(R.drawable.camera_selfie_button_inactive_30dp);
          mSelfieButton.setImageResource(R.drawable.camera_selfie_button_inactive_30dp);
          if (mMusicPlayer.getRelativePosition() < 5000)
            mOkButton.setImageResource(R.drawable.camera_ok_button_inactive_30dp);
          else
            mOkButton.setImageResource(R.drawable.camera_ok_button_active_30dp);
          ((HomeActivity) getActivity()).disableScroll();
          break;
        case UI_LOGIC_FINISH_CUT_MUSIC:
          duringCuttingMusic = false;
          if (duringShootingVideo) {
            ((HomeActivity) getActivity()).disableScroll();
          } else {
            ((HomeActivity) getActivity()).enableScroll();
          }
          /*  */
        case UI_LOGIC_SHOW_ALL_BUTTONS:
          mShootButton.setVisibility(View.VISIBLE);
          mBaseLineView.setVisibility(View.VISIBLE);
          /*  */
        case UI_LOGIC_SHOW_PERIPHERAL_BUTTONS:
          mCutButton.setVisibility(View.VISIBLE);
          mSelfieButton.setVisibility(View.VISIBLE);
          mOkButton.setVisibility(View.VISIBLE);
          mMusicButton.setVisibility(View.VISIBLE);
          mLibraryButton.setVisibility(View.VISIBLE);
          break;
        case UI_LOGIC_DURING_CUT_MUSIC:
          duringCuttingMusic = true;
          ((HomeActivity) getActivity()).disableScroll();
          /*  */
        case UI_LOGIC_HIDE_ALL_BUTTONS:
          mShootButton.setVisibility(View.INVISIBLE);
          mBaseLineView.setVisibility(View.INVISIBLE);
          /*  */
        case UI_LOGIC_HIDE_PERIPHERAL_BUTTONS:
          mCutButton.setVisibility(View.INVISIBLE);
          mSelfieButton.setVisibility(View.INVISIBLE);
          mOkButton.setVisibility(View.INVISIBLE);
          mMusicButton.setVisibility(View.INVISIBLE);
          mLibraryButton.setVisibility(View.INVISIBLE);
          break;
        case UI_LOGIC_RELEASE_SHOOT_BUTTON:
          mShootButton.setImageResource(R.drawable.camera_shoot_button_release_70dp);
          mShootButton.startAnimation(getAnimation(getActivity(), R.anim.clicking_101));
          break;
        case UI_LOGIC_HOLD_SHOOT_BUTTON:
          mShootButton.setImageResource(R.drawable.camera_shoot_button_hold_70dp);
          mShootButton.startAnimation(getAnimation(getActivity(), R.anim.clicking_101));
          break;
        case UI_LOGIC_LOCK_SHOOT_BUTTON:
          mShootButton.setImageResource(R.drawable.camera_shoot_button_shooting_70dp);
          break;
        case UI_LOGIC_MUSIC_UPDATE_COMPLETE:
          mMusicButton.setAlbumArt(mMusic);
          mMusicButton.startAnimation(getAnimation(getActivity(), R.anim.rotating));
          if (mMusic != null) {
            mCutButton.setImageResource(R.drawable.camera_cut_button_active_30dp);
          } else {
            mCutButton.setImageResource(R.drawable.camera_cut_button_inactive_30dp);
          }
          break;
        default:
          // Nothing to do.
          break;
      }
    }
  };

}
