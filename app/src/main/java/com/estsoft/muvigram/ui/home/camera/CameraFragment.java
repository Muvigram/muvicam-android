package com.estsoft.muvigram.ui.home.camera;

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

import com.estsoft.muvigram.BuildConfig;
import com.estsoft.muvigram.R;
import com.estsoft.muvigram.model.Music;
import com.estsoft.muvigram.ui.common.UnsupportedDialogFragment;
import com.estsoft.muvigram.ui.home.HomeActivity;
import com.estsoft.muvigram.ui.home.camera.view.AlbumArtButton;
import com.estsoft.muvigram.ui.home.camera.view.LibraryThumbnailButton;
import com.estsoft.muvigram.ui.home.camera.view.ResizableTextureView;
import com.estsoft.muvigram.ui.home.camera.view.StackBar;
import com.estsoft.muvigram.ui.library.LibraryActivity;
import com.estsoft.muvigram.ui.share.ShareActivity;
import com.estsoft.muvigram.util.MusicPlayer;
import com.estsoft.muvigram.util.rx.RxUtil;
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

import static com.estsoft.muvigram.util.AnimationUtil.*;
import static com.estsoft.muvigram.util.AnimationUtil.getClickingAnimation;

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
    if (!isCompoundSessionReady || mMusic == null || !mVideoStack.isEmpty()) {
      return;
    }

    FragmentManager cfm = getChildFragmentManager();
    Fragment fragment = cfm.findFragmentById(R.id.camera_container_music_cut);

    if (fragment == null) {
      fragment = MusicCutFragment.newInstance(mMusic, mMusicPlayer.getOffset());
      final FragmentTransaction transaction = cfm.beginTransaction()
          .add(R.id.camera_container_music_cut, fragment);

      v.startAnimation(getClickingAnimation(getActivity(), new AnimationEndListener() {
        @Override public void onAnimationEnd(Animation animation) {
          transaction.commit();
          requestUiChange(UI_LOGIC_DURING_CUT_MUSIC);
        }
      }));
    }
  }

  @OnClick(R.id.camera_selfie_button)
  public void _shiftSelfieMode(View v) {
    if (!isCompoundSessionReady || !mVideoStack.isEmpty()) {
      return;
    }

    v.startAnimation(getClickingAnimation(getActivity(), new AnimationEndListener() {
      @Override public void onAnimationEnd(Animation animation) {
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
    if (!isCompoundSessionReady || mVideoStack.isEmpty() || mMusicPlayer.getRelativePosition() < 5000) {
      return;
    }
    v.startAnimation(getClickingAnimation(getActivity(), new AnimationEndListener() {
      @Override public void onAnimationEnd(Animation animation) {
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
    if (!isCompoundSessionReady || !mVideoStack.isEmpty()) {
      return;
    }
    v.startAnimation(getClickingAnimation(getActivity(), new AnimationEndListener() {
      @Override public void onAnimationEnd(Animation animation) {
        getActivity().startActivity(LibraryActivity.getIntent(getActivity()));
      }
    }));
  }

  // OnClick
  public void _deleteRecentVideo(View v) {
    if (!isCompoundSessionReady || mVideoStack.isEmpty()) {
      return;
    }
    v.startAnimation(getClickingAnimation(getActivity(), new AnimationEndListener() {
      @Override public void onAnimationEnd(Animation animation) {
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
      videoPaths[i++] = file.toString();
    }

    int[] videoOffsets = new int[mOffsetStack.size()];
    i = 0;
    for (Integer videoOffset : mOffsetStack.toArray(new Integer[mOffsetStack.size()])) {
      videoOffsets[i++] = videoOffset;
    }

    String mMusicPath = mMusic == null ? "" : mMusic.uri().toString();

    Intent intent = ShareActivity.getIntent(getContext(), videoPaths, videoOffsets,
        mMusicPath, mMusicPlayer.getOffset(), mMusicPlayer.getRelativePosition());

    getActivity().startActivity(intent);
  }

  // STEP - VIEW BINDING //////////////////////////////////////////////////////////////////////////

  private boolean isDown = false;
  private boolean isRecording = false;
  private boolean isOnStopRecording = false;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    startBackgroundThread();
    mMusicPlayer = new MusicPlayer(getActivity(), "silence_15_sec.mp3");
    mMusicPlayer.openPlayer();   // player
    mMusicPlayer.setUpPlayer();  //
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_home_camera, container, false);
    mUnbinder = ButterKnife.bind(this, view);

    final Observable<MotionEvent> sharedObservable = RxView.touches(mShootButton).share();
    // Start shooting
    sharedObservable
        .filter(e -> e.getAction() == MotionEvent.ACTION_DOWN)
        .filter(e -> mMusicPlayer.getRelativePosition() <= 14000)
        .filter(e -> !isRecording && isCompoundSessionReady)
        .filter(this::shootButtonDown)
        .debounce(400, TimeUnit.MILLISECONDS, Schedulers.newThread())
        .subscribe(
            this::requestRecording,
            e -> Timber.e(e, "m/onCreateView RxView.touches.ACTION_DOWN")
        );

    // Stop shooting
    sharedObservable
        .filter(e -> e.getAction() == MotionEvent.ACTION_UP ||
                     e.getAction() == MotionEvent.ACTION_OUTSIDE)
        .filter(e -> mMusicPlayer.getRelativePosition() <= 14000)
        .filter(e -> !isOnStopRecording)
        .filter(this::preventRecording)
        .subscribe(this::stopRecording,e -> Timber.e(e, "m/onCreateView RxView.touches.ACTION_UP"));

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

  public boolean shootButtonDown(MotionEvent motionEvent) {
    requestUiChange(UI_LOGIC_HOLD_SHOOT_BUTTON); // Release -> Hold
    isDown = true;
    return true;
  }

  public void requestRecording(MotionEvent motionEvent) {
    if (!isDown) return;
    startRecording();
  }

  public boolean preventRecording(MotionEvent motionEvent) {
    isDown = false;

    if (!isRecording) {
      requestUiChange(UI_LOGIC_SHOW_PERIPHERAL_BUTTONS);
      requestUiChange(UI_LOGIC_RELEASE_SHOOT_BUTTON);
    }
    return isRecording;
  }

  public void stopRecording(MotionEvent motionEvent) {
    mBackgroundHandler.post(this::delayStopRecording);
    isOnStopRecording = true;
  }

  public void stopRecording() {
    isDown = false;
    mBackgroundHandler.post(this::delayStopRecording);
    isOnStopRecording = true;
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

  private File mVideoFile;
  private int mVideoOffset;
  private Stack<File> mVideoStack = new Stack<>();
  private Stack<Integer> mOffsetStack = new Stack<>(); // TODO - extract from StackBar

  private void createVideoFile() {
    try {
      mVideoFile = File.createTempFile("muvigram", null, getContext().getCacheDir());
      if(BuildConfig.DEBUG) Timber.d("Create file %s\n", mVideoFile.toString());
    } catch (IOException e) {
      Timber.e(e, "m/createVideoFile");
    }
  }

  private void updateVideoOffset() {
    mVideoOffset = mMusicPlayer.getRelativePosition();
  }

  Subscription mStackBarSubscription;

  /**
   * This method has four interactions as follows:
   *   1. start music player's cold observable.
   *   2. subscribe the music player.
   *   3. stop recording with a proper delay.
   *   4. unsubscribe the music player.
   */
  public void setStackBarOnListen() {
    RxUtil.unsubscribe(mStackBarSubscription);
    mStackBarSubscription = mMusicPlayer.startSubscribePlayer()
        .subscribeOn(Schedulers.newThread())
        .observeOn(Schedulers.computation())
        .doOnSubscribe(() -> mUiThreadHandler.post(this::hideTrashbin))
        .map(millisec -> (millisec - mMusicPlayer.getOffset()))
        .filter(relMillisec -> {
          if (relMillisec > 15000) {
            stopRecording();
            return false;
          }
          return true;
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            mStackBar::updateStackBar,
            e -> Timber.e(e, "m/setStackBarOnListen"),
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
      Timber.d("Push the file into the stack %s [ms : %d]\n", mVideoStack.peek().toString(), mOffsetStack.peek());
    }
    if (!mVideoStack.isEmpty()) {
      mUiThreadHandler.post(this::updateTrashbin);
      requestUiChange(UI_LOGIC_DURING_SHOOTING);
    }

  }

  private void popVideoFile() {
    if (!mVideoStack.isEmpty()) {
      File oldVideo = mVideoStack.pop();
      int oldOffset = mOffsetStack.pop();
      Timber.d("Pop the file file the stack %s [ms : %d]\n", oldVideo, oldOffset);
    }
    if (mVideoStack.isEmpty()) {
      mUiThreadHandler.post(this::updateTrashbin);
      requestUiChange(UI_LOGIC_BEFORE_SHOOTING);
    }
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

    mStackTrashbinContainer.removeView(mStackTrashbin);
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
      Timber.e(e, "m/stopBackgroundThread");
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
      Timber.e(e, "m/openCamera e/CameraAccessException");
    } catch (InterruptedException e) {
      Timber.e(e, "m/openCamera e/InterruptedException");
      throw new RuntimeException("Interrupted while trying to lock camera open ing.", e);
    }
  }

  private void closeCamera() {
    closeExistingSession(); // session
    try {
      mCameraOpenCloseLock.acquire();
      if (null != mCameraDevice) {
        mCameraDevice.close();
        mCameraDevice = null;
      }
    } catch (InterruptedException e) {
      Timber.e(e, "m/closeCamera e/InterruptedException");
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

  private final static int HARDWARE_LEGACY = CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;

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
      if (mHardwareLevel == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY) {
//        UnsupportedDialogFragment dialogFragment = UnsupportedDialogFragment.newInstance();
//        dialogFragment.show(getFragmentManager(), UnsupportedDialogFragment.TAG);
//        dialogFragment.setCancelable(false);
        Timber.w("m/setupCameraOutputs Hardware level LEGACY detected");
      }


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

      Timber.d("[w : %d, h : %d]", w, h);
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
      Timber.e(e, "m/setUpCameraOutputs e/CameraAccessException");
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
        Timber.w("Invalid display rotation : %d\n", displayRotation);
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
      Timber.v("Image capture - %d : %d\n", option.getWidth(), option.getHeight());
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

      // WARN - There is not proper preview size
      StringBuilder feasibleSizes = new StringBuilder("");
      for (Size option : choices) {
        feasibleSizes.append(
            String.format(Locale.getDefault(),
                "[%d:%d]\n", option.getWidth(), option.getHeight())
        );
      }
      feasibleSizes.append(
          String.format(Locale.getDefault(),
              "Display Aspect Ratio [%d:%d]\n", aspectRatio.getWidth(), aspectRatio.getHeight())
      );
      Timber.w("Couldn't find any suitable video size.\n%s", feasibleSizes.toString());

      switch (baseDimension) {
        case BASE_DIMENSION_HEIGHT:
          return Collections.max(longerHeight, new SizeComparator());
        case BASE_DIMENSION_WIDTH:
          return Collections.max(longerWidth, new SizeComparator());
        default: // Unexpected case
          Timber.w("Invalid value for baseSide parameter.");
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
          Timber.v("Big enough - %d : %d", option.getWidth(), option.getHeight());
          bigEnough.add(option);
        } else {
          Timber.v("Not Big enough - %d : %d", option.getWidth(), option.getHeight());
          notBigEnough.add(option);
        }
      }
    }

    if (bigEnough.size() > 0) {
      return Collections.min(bigEnough, new SizeComparator());
    } else if (notBigEnough.size() > 0) {
      return Collections.max(notBigEnough, new SizeComparator());
    } else {

      // WARN - There is not proper preview size
      StringBuilder feasibleSizes = new StringBuilder("");
      for (Size option : choices) {
        feasibleSizes.append(
            String.format(Locale.getDefault(),
            "[%d:%d]\n", option.getWidth(), option.getHeight())
        );
      }
      feasibleSizes.append(
          String.format(Locale.getDefault(),
              "Aspect Ratio [%d:%d]\n", aspectRatio.getWidth(), aspectRatio.getHeight())
      );
      Timber.w("Couldn't find any suitable preview size.\n%s", feasibleSizes.toString());

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

  private void startPreviewing() {
    isCompoundSessionReady = false;
    createCompoundSession();
  }

  private void startRecording() {
    requestUiChange(UI_LOGIC_LOCK_SHOOT_BUTTON);
    requestUiChange(UI_LOGIC_HIDE_PERIPHERAL_BUTTONS);

    isRecording = true;
    updateVideoOffset();
    setStackBarOnListen();

    // media recorder
    startRecorder();
    mMusicPlayer.startPlayer();
  }

  private static final int FIXED_DELAY_LENGTH = 1000;

  private static final int MINIMUM_VIDEO_LENGTH =  1000;
  private static final int MAXIMUM_VIDEO_LENGTH = 15000;

  private void delayStopRecording() {
    mBackgroundHandler.post(() -> {
      if (Looper.myLooper() == null) {
        Looper.prepare();
      }

      int delayTime = delayTime(mMusicPlayer.getRelativePosition(), mVideoOffset);

      try {
        Thread.sleep(delayTime);
      } catch (InterruptedException e) {
        Timber.e(e, "m/delayStopRecording e/InterruptedException");
      } finally {
        // stop subscribing music player.
        mMusicPlayer.stopSubscribePlayer();
        mMusicPlayer.pausePlayer();

        requestUiChange(UI_LOGIC_SHOW_PERIPHERAL_BUTTONS);
        requestUiChange(UI_LOGIC_HOLD_SHOOT_BUTTON);
      }

      try {
        Thread.sleep(FIXED_DELAY_LENGTH - delayTime);
      } catch (InterruptedException e) {
        Timber.e(e, "m/delayStopRecording e/InterruptedException");
      } finally {
        requestUiChange(UI_LOGIC_RELEASE_SHOOT_BUTTON);
        if (mHardwareLevel == HARDWARE_LEGACY) {
          try {
            mSession.stopRepeating();
            mSession.abortCaptures();
          } catch (CameraAccessException e) {
            e.printStackTrace();
          }
        }
        stopRecorder();
        isRecording = false;
        startPreviewing();
        isOnStopRecording = false;
      }
    });
  }

  /**
   * This method calculate time length which should be delayed to ensure the minimum video length.
   *
   * @param musicCurTime Current time of music player, based on the offset time.
   * @param videoOffset  Offset time of current video in terms of music player's time.
   * @return To-be-delayed time which is always between 0 and 1000 (ms), inclusively.
   */
  private static int delayTime(int musicCurTime, int videoOffset) {
    if (musicCurTime < 14000) {
      return musicCurTime - videoOffset < MINIMUM_VIDEO_LENGTH ?
          MINIMUM_VIDEO_LENGTH - (musicCurTime - videoOffset) : 0;
    } else if (musicCurTime > 15000) {
      return 0;
    } else { // 14000 <= musicCurTime <= 15000
      return MAXIMUM_VIDEO_LENGTH - musicCurTime;
    }
  }

  // STEP - CREATE SESSION ////////////////////////////////////////////////////////////////////////

  private CameraCaptureSession mSession;

  private CaptureRequest.Builder mCaptureRequestBuilder;

  private boolean isCompoundSessionReady = false;

  private void createCompoundSession() {
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

      // Initialize captureRequestBuilder
      mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
      mCaptureRequestBuilder.addTarget(previewSurface);
      mCaptureRequestBuilder.addTarget(recorderSurface);
      mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

      closeExistingSession();

      mCameraDevice.createCaptureSession(surfaces, mCompoundSessionStateCallback, mBackgroundHandler);

    } catch (CameraAccessException e) {
      Timber.e(e, "m/createCompoundSession Exception when creating capture session for recording.");
    }
  }

  private CameraCaptureSession.StateCallback mCompoundSessionStateCallback =
      new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
          try {

            // Start preview
            session.setRepeatingRequest(mCaptureRequestBuilder.build(), null, mBackgroundHandler);
          } catch (CameraAccessException e) {
            Timber.e(e, "m/RecordSessionStateCallback#onConfigured");
            isCompoundSessionReady = false;
            // startPreviewing();
          }

          mSession = session;
          isCompoundSessionReady = true;
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
          Timber.w("compoundSessionStateCallback m/onConfigureFalied session failed");
        }
  };

  private void closeExistingSession() {
    if (null != mSession) {
      mSession.close();
      mSession = null;
    }

    isCompoundSessionReady = false;
  }

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
      Timber.e(e, "m/setUpRecorder");
      mRecorder.reset();
    }
  }

  public void startRecorder() {
    try {
      mRecorder.start();
    } catch (IllegalStateException e) {
      Timber.e(e, "m/startRecorder");
      mRecorder.reset();
      setUpRecorder();
      mRecorder.start();
    }
  }

  public void stopRecorder() {
    try {
      mRecorder.stop(); // Error-expected point
      pushVideoFile();
      mVideoFile = null;
      createVideoFile();

    } catch (IllegalStateException e) {
      Timber.e(e, "m/stopRecorder");
    } catch (RuntimeException e) {
      Timber.w(e, "m/stopRecorder Recording length was too short.");
    } finally {
      mRecorder.reset(); // If already stopped, reset recorder.
      setUpRecorder();
    }
  }

  // STEP - UI MAIN THREAD ////////////////////////////////////////////////////////////////////DONE

  /* FUNC - UI LOGIC */
  public static final int UI_LOGIC_SHOW_PERIPHERAL_BUTTONS = 0x001;
  public static final int UI_LOGIC_HIDE_PERIPHERAL_BUTTONS = 0x002;

  public static final int UI_LOGIC_SHOW_ALL_BUTTONS = 0x003;
  public static final int UI_LOGIC_HIDE_ALL_BUTTONS = 0x004;

  public static final int UI_LOGIC_MUSIC_UPDATE_COMPLETE = 0x005;

  public static final int UI_LOGIC_BEFORE_SHOOTING = 0x006;
  public static final int UI_LOGIC_DURING_SHOOTING = 0x007;
  public static final int UI_LOGIC_FINISH_SHOOTING = 0x008;

  public static final int UI_LOGIC_RELEASE_SHOOT_BUTTON = 0x009;
  public static final int UI_LOGIC_HOLD_SHOOT_BUTTON = 0x010;
  public static final int UI_LOGIC_LOCK_SHOOT_BUTTON = 0x011;

  public static final int UI_LOGIC_DURING_CUT_MUSIC = 0x012;
  public static final int UI_LOGIC_FINISH_CUT_MUSIC = 0x013;

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({
      UI_LOGIC_SHOW_PERIPHERAL_BUTTONS, UI_LOGIC_HIDE_PERIPHERAL_BUTTONS,
      UI_LOGIC_SHOW_ALL_BUTTONS, UI_LOGIC_HIDE_ALL_BUTTONS,

      UI_LOGIC_MUSIC_UPDATE_COMPLETE,

      UI_LOGIC_BEFORE_SHOOTING, UI_LOGIC_DURING_SHOOTING, UI_LOGIC_FINISH_SHOOTING,
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

    @Override
    public void handleMessage(Message msg) {
      int uiLogicRequest = msg.getData().getInt(UI_LOGIC);

      switch (uiLogicRequest) {
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
          if (!duringShootingVideo) {
            ((HomeActivity) getActivity()).enableScroll();
          }
          break;
        case UI_LOGIC_HOLD_SHOOT_BUTTON:
          mShootButton.setImageResource(R.drawable.camera_shoot_button_hold_70dp);
          mShootButton.startAnimation(getAnimation(getActivity(), R.anim.clicking_101));
          ((HomeActivity) getActivity()).disableScroll();
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
