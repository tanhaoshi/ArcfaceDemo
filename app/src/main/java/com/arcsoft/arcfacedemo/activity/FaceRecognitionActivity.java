package com.arcsoft.arcfacedemo.activity;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Toast;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.adapter.MainRcAdapter;
import com.arcsoft.arcfacedemo.adapter.MainRcListAdapter;
import com.arcsoft.arcfacedemo.db.dao.RegisterInfo;
import com.arcsoft.arcfacedemo.faceserver.CompareResult;
import com.arcsoft.arcfacedemo.faceserver.CompareRgResult;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.model.DrawInfo;
import com.arcsoft.arcfacedemo.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.DrawHelper;
import com.arcsoft.arcfacedemo.util.camera.CameraHelper;
import com.arcsoft.arcfacedemo.util.camera.CameraListener;
import com.arcsoft.arcfacedemo.util.face.FaceHelper;
import com.arcsoft.arcfacedemo.util.face.FaceListener;
import com.arcsoft.arcfacedemo.util.face.RequestFeatureStatus;
import com.arcsoft.arcfacedemo.widget.FaceRectView;
import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.VersionInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;

public class FaceRecognitionActivity extends AppCompatActivity implements ViewTreeObserver.OnGlobalLayoutListener{

    public static final String TAG = "FaceRecognitionActivity";

    private Unbinder mUnbinder;

    @BindView(R.id.texture_preview)
    TextureView mTextureView;
    @BindView(R.id.face_rect_view)
    FaceRectView mFaceRectView;
    @BindView(R.id.main_rv)
    RecyclerView main_rv;
    @BindView(R.id.main_rv_list)
    RecyclerView mRvList;

    private ConcurrentHashMap<Integer, Integer> requestFeatureStatusMap = new ConcurrentHashMap<>();
    private List<RegisterInfo> mRegisterInfoList = Collections.synchronizedList(new LinkedList<RegisterInfo>());
    private List<RegisterInfo> mInfos = new LinkedList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_recognition);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        FaceServer.getInstance().initRegister(this);

        mUnbinder = ButterKnife.bind(this);

        mTextureView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
    }

    private MainRcAdapter mMainRcAdapter;
    private MainRcListAdapter mMainRcListAdapter;

    private void initView(){
        main_rv.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL,false));
        main_rv.setItemAnimator(new SlideInRightAnimator());
        main_rv.getItemAnimator().setAddDuration(400);

        mMainRcAdapter = new MainRcAdapter(mRegisterInfoList,this);
        main_rv.setAdapter(mMainRcAdapter);

        mRvList.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        mMainRcListAdapter = new MainRcListAdapter(mInfos,this);
        mRvList.setAdapter(mMainRcListAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mUnbinder != null) mUnbinder.unbind();
    }

    @Override
    public void onGlobalLayout() {
        mTextureView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if(!checkPermissions(NEEDED_PERMISSIONS)){
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        }else{
            initEngine();
            initCamera();
        }
    }

    private FaceEngine faceEngine;
    private int afCode = -1;
    private static final int MAX_DETECT_NUM = 10;

    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE
    };

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;

    private CameraHelper cameraHelper;

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                initEngine();
                initCamera();
                if(cameraHelper != null) cameraHelper.start();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initEngine(){
        faceEngine = new FaceEngine();
        afCode = faceEngine.init(this, FaceEngine.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(this),
                16, MAX_DETECT_NUM, FaceEngine.ASF_FACE_RECOGNITION | FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_LIVENESS);
        VersionInfo versionInfo = new VersionInfo();
        faceEngine.getVersion(versionInfo);
        Log.i(TAG, "initEngine:  init: " + afCode + "  version:" + versionInfo);

        if (afCode != ErrorInfo.MOK) {
            Toast.makeText(this, getString(R.string.init_failed, afCode), Toast.LENGTH_SHORT).show();
        }
    }

    private Integer cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private Camera.Size previewSize;
    private DrawHelper drawHelper;
    private FaceHelper faceHelper;

    private void initCamera(){
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final FaceListener faceListener = new FaceListener() {
            @Override
            public void onFail(Exception e) {
                Log.e(TAG, "onFail: " + e.getMessage());
            }

            @Override
            public void onFaceFeatureInfoGet(@Nullable FaceFeature faceFeature, Integer requestId) {
                Log.i(TAG,"requestId : " + requestId);
                if(faceFeature != null){
                    Log.i(TAG,"face : " + requestId);
                    searchFace(faceFeature,requestId);
                }else{
                    requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                }
            }
        };

        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                previewSize = camera.getParameters().getPreviewSize();
                drawHelper = new DrawHelper(previewSize.width, previewSize.height,
                        mTextureView.getWidth(),
                        mTextureView.getHeight(), displayOrientation
                        , cameraId, isMirror);

                faceHelper = new FaceHelper.Builder()
                        .faceEngine(faceEngine)
                        .frThreadNum(10)
                        .previewSize(previewSize)
                        .faceListener(faceListener)
                        .currentTrackId(ConfigUtil.getTrackId(FaceRecognitionActivity.this.getApplicationContext()))
                        .build();
            }

            @Override
            public void onPreview(final byte[] data, Camera camera) {
                if(mFaceRectView != null) mFaceRectView.clearFaceInfo();
                final List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(data);
                if(facePreviewInfoList != null && mFaceRectView != null && drawHelper != null){
                    List<DrawInfo> drawInfoList = new ArrayList<>();
                    for(int i=0; i<facePreviewInfoList.size(); i++){
                        String name = faceHelper.getName(facePreviewInfoList.get(i).getTrackId());
                        drawInfoList.add(new DrawInfo(facePreviewInfoList.get(i).getFaceInfo().getRect(),
                                GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE, LivenessInfo.UNKNOWN,name == null ?
                                String.valueOf(facePreviewInfoList.get(i).getTrackId()) : name));
                    }
                    drawHelper.draw(mFaceRectView,drawInfoList);
                }
                clearLeftFace(facePreviewInfoList);

                if (facePreviewInfoList != null && facePreviewInfoList.size() > 0 && previewSize != null) {

                    for (int i = 0; i < facePreviewInfoList.size(); i++) {
                        /**
                         * 对于每个人脸，若状态为空或者为失败，则请求FR（可根据需要添加其他判断以限制FR次数），
                         * FR回传的人脸特征结果在{@link FaceListener#onFaceFeatureInfoGet(FaceFeature, Integer)}中回传
                         */
                        if (requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == null
                                || requestFeatureStatusMap.get(facePreviewInfoList.get(i).getTrackId()) == RequestFeatureStatus.FAILED) {

                            requestFeatureStatusMap.put(facePreviewInfoList.get(i).getTrackId(), RequestFeatureStatus.SEARCHING);

                            faceHelper.requestFaceFeature(data, facePreviewInfoList.get(i).getFaceInfo(), previewSize.width, previewSize.height,
                                    FaceEngine.CP_PAF_NV21, facePreviewInfoList.get(i).getTrackId());

                        }
                    }
                }
            }

            @Override
            public void onCameraClosed() {

            }

            @Override
            public void onCameraError(Exception e) {

            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (drawHelper != null) {
                    drawHelper.setCameraDisplayOrientation(displayOrientation);
                }
            }
        };

        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(mTextureView.getMeasuredWidth(),mTextureView.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(cameraID != null ? cameraID : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(mTextureView)
                .cameraListener(cameraListener)
                .build();

        cameraHelper.init();
    }

    private static final float SIMILAR_THRESHOLD = 0.8F;
    volatile boolean isRemove = false;

    private void searchFace(final FaceFeature faceFeature,final Integer requestId){
        Observable.create(new ObservableOnSubscribe<CompareRgResult>() {
            @Override
            public void subscribe(ObservableEmitter<CompareRgResult> emitter) {
                CompareRgResult compareResult = FaceServer.getInstance().compareFace(faceFeature);
                if(compareResult == null){
                    emitter.onError(null);
                }else{
                    emitter.onNext(compareResult);
                }
            }
        }).subscribeOn(Schedulers.io())
          .unsubscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe(new Observer<CompareRgResult>() {
              @Override
              public void onSubscribe(Disposable d) {

              }

              @Override
              public void onNext(CompareRgResult compareResult) {
                  if (compareResult == null || compareResult.getRegisterInfo().getName() == null) {
                      requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                      faceHelper.addName(requestId, "VISITOR " + requestId);
                      Log.i(TAG,"compare result is null " + requestId);
                      return;
                  }

                  if (compareResult.getSimilar() > SIMILAR_THRESHOLD) {
                      requestFeatureStatusMap.put(requestId, RequestFeatureStatus.SUCCEED);
                      faceHelper.addName(requestId, compareResult.getRegisterInfo().getName());
                      Log.i(TAG,"user name : " + compareResult.getRegisterInfo().getName());
                      synchronized (this){
                          mInfos.add(compareResult.getRegisterInfo());
                          mMainRcListAdapter.add(mInfos.size(),mInfos);
                          if(mInfos.size() -1 != 0){
                              mRvList.scrollToPosition(mInfos.size() -1);
                              LinearLayoutManager linearLayoutManager =
                                      (LinearLayoutManager)mRvList.getLayoutManager();
                              linearLayoutManager.scrollToPositionWithOffset(mInfos.size() -1 ,0);
                          }
                          if(mRegisterInfoList.size() > 3){
                              isRemove = true;
                              try{
                                  mRegisterInfoList.remove(0);
                                  mMainRcAdapter.remove(0,mRegisterInfoList);
                              }finally {
                                  mRegisterInfoList.add(compareResult.getRegisterInfo());
                                  mMainRcAdapter.add(mRegisterInfoList.size(),mRegisterInfoList);
                                  initiativeDismiss(mRegisterInfoList);
                                  isRemove = false;
                                  return;
                              }
                          }
                          if(!isRemove){
                              mRegisterInfoList.add(compareResult.getRegisterInfo());
                              mMainRcAdapter.add(mRegisterInfoList.size(),mRegisterInfoList);
                              autoDismissItem(mRegisterInfoList);
                          }

                      }
                  } else {
                      requestFeatureStatusMap.put(requestId, RequestFeatureStatus.FAILED);
                      faceHelper.addName(requestId, "VISITOR " + requestId);
                      Log.i(TAG,"recognition fail id : " + requestId);
                  }
              }

              @Override
              public void onError(Throwable e) {

              }

              @Override
              public void onComplete() {

              }
          });
    }

    private void autoDismissItem(final List<RegisterInfo> infoList){
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) {
                if(infoList.size() > 0){
                    emitter.onNext(true);
                }
            }
        }).delay(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if(aBoolean){
                            try{
                                infoList.remove(0);
                            }finally {
                                mMainRcAdapter.remove(0,infoList);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG,"error auto : " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void initiativeDismiss(final List<RegisterInfo> infoList){
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) {
                if(infoList.size() > 0){
                    emitter.onNext(true);
                }
            }
        }).delay(5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if(aBoolean){
                            try{
                                infoList.remove(0);
                            }finally {
                                mMainRcAdapter.remove(0,infoList);
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.i(TAG,"error initiative : " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList){
        Set<Integer> keySet = requestFeatureStatusMap.keySet();
        if (facePreviewInfoList == null || facePreviewInfoList.size() == 0) {
            requestFeatureStatusMap.clear();
            return;
        }

        for (Integer integer : keySet) {
            boolean contained = false;
            for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                if (facePreviewInfo.getTrackId() == integer) {
                    contained = true;
                    break;
                }
            }
            if (!contained) {
                requestFeatureStatusMap.remove(integer);
            }
        }
    }


}
