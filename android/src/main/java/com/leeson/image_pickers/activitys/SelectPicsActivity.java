package com.leeson.image_pickers.activitys;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.leeson.image_pickers.AppPath;
import com.leeson.image_pickers.R;
import com.leeson.image_pickers.utils.CommonUtils;
import com.leeson.image_pickers.utils.GlideEngine;
import com.leeson.image_pickers.utils.ImageCompressUtils;
import com.leeson.image_pickers.utils.PictureStyleUtil;
import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.tools.AndroidQTransformUtils;
import com.luck.picture.lib.tools.MediaUtils;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.luck.picture.lib.tools.SdkVersionUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


/**
 * Created by lisen on 2018-09-11.
 * 只选择多张图片，
 *
 * @author lisen < 453354858@qq.com >
 */
@SuppressWarnings("all")
public class SelectPicsActivity extends BaseActivity {

    private static final int WRITE_SDCARD = 101;

    public static final String GALLERY_MODE = "GALLERY_MODE";
    public static final String UI_COLOR = "UI_COLOR";
    public static final String SHOW_GIF = "SHOW_GIF";
    public static final String SHOW_CAMERA = "SHOW_CAMERA";
    public static final String ENABLE_CROP = "ENABLE_CROP";
    public static final String WIDTH = "WIDTH";
    public static final String HEIGHT = "HEIGHT";
    public static final String COMPRESS_SIZE = "COMPRESS_SIZE";

    public static final String SELECT_COUNT = "SELECT_COUNT";//可选择的数量

    public static final String COMPRESS_PATHS = "COMPRESS_PATHS";//压缩的画
    public static final String CAMERA_MIME_TYPE = "CAMERA_MIME_TYPE";//直接调用拍照或拍视频时有效
    public static final String CAMERA_CAPTURE_MAX_TIME = "CAMERA_CAPTURE_MAX_TIME";//视频最大拍摄时间
    public static final String VIDEO_SELECT_MAX_TIME = "VIDEO_SELECT_MAX_TIME";//相册中可选视频的最大时间
    private Number compressSize;
    private int compressCount = 0;
    private String mode;
    private Map<String, Number> uiColor;
    private Number selectCount;
    private boolean showGif;
    private boolean showCamera;
    private boolean enableCrop;
    private Number width;
    private Number height;
    private String mimeType;
    private int cameraCaptureMaxTime;
    private int videoSelectMaxTime;

    @Override
    public void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_pics);
        mode = getIntent().getStringExtra(GALLERY_MODE);
        uiColor = (Map<String, Number>) getIntent().getSerializableExtra(UI_COLOR);

        selectCount = getIntent().getIntExtra(SELECT_COUNT, 9);
        showGif = getIntent().getBooleanExtra(SHOW_GIF, true);
        showCamera = getIntent().getBooleanExtra(SHOW_CAMERA, false);
        enableCrop = getIntent().getBooleanExtra(ENABLE_CROP, false);
        width = getIntent().getIntExtra(WIDTH, 1);
        height = getIntent().getIntExtra(HEIGHT, 1);
        compressSize = getIntent().getIntExtra(COMPRESS_SIZE, 500);
        mimeType = getIntent().getStringExtra(CAMERA_MIME_TYPE);
        cameraCaptureMaxTime = getIntent().getIntExtra(CAMERA_CAPTURE_MAX_TIME, 60 * 5);
        videoSelectMaxTime = getIntent().getIntExtra(VIDEO_SELECT_MAX_TIME, 60 * 5);
        startSel();
    }

    private void startSel() {
        PictureStyleUtil pictureStyleUtil = new PictureStyleUtil(this);

        //添加图片
        PictureSelector pictureSelector = PictureSelector.create(this);
        PictureSelectionModel pictureSelectionModel = null;
        if (mimeType != null) {
            //直接调用拍照或拍视频时
            if ("photo".equals(mimeType)) {
                pictureSelectionModel = pictureSelector.openCamera(PictureMimeType.ofImage());
                if (SdkVersionUtils.checkedAndroid_Q()) {
                    pictureSelectionModel.imageFormat(PictureMimeType.PNG_Q);
                } else {
                    pictureSelectionModel.imageFormat(PictureMimeType.PNG);
                }
            } else {
                pictureSelectionModel = pictureSelector.openCamera(PictureMimeType.ofVideo());
                pictureSelectionModel.imageFormat(PictureMimeType.MIME_TYPE_VIDEO);
                pictureSelectionModel.closeAndroidQChangeVideoWH(true);
                pictureSelectionModel.videoMaxSecond(videoSelectMaxTime);

            }
        } else {
            //从相册中选择
            pictureSelectionModel = pictureSelector.openGallery("image".equals(mode) ? PictureMimeType.ofImage() : PictureMimeType.ofVideo());
            if ("image".equals(mode)) {
                if (SdkVersionUtils.checkedAndroid_Q()) {
                    pictureSelectionModel.imageFormat(PictureMimeType.PNG_Q);
                } else {
                    pictureSelectionModel.imageFormat(PictureMimeType.PNG);
                }

            } else {
                pictureSelectionModel.imageFormat(PictureMimeType.MIME_TYPE_VIDEO);
                pictureSelectionModel.closeAndroidQChangeVideoWH(true);
                pictureSelectionModel.videoMaxSecond(videoSelectMaxTime);
            }
        }

        pictureSelectionModel
                .loadImageEngine(GlideEngine.createGlideEngine())
                .isOpenStyleNumComplete(true)
                .isOpenStyleCheckNumMode(true)

//                .imageFormat(
//                        SdkVersionUtils.checkedAndroid_Q()
//                        ? ("video".equals(mimeType)  ? PictureMimeType.MIME_TYPE_VIDEO  : PictureMimeType.PNG_Q.toLowerCase() )
//                        : ("video".equals(mimeType) ? PictureMimeType.MIME_TYPE_VIDEO : PictureMimeType.PNG.toLowerCase()))

                .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .setPictureStyle(pictureStyleUtil.getStyle(uiColor))
                .setPictureCropStyle(pictureStyleUtil.getCropStyle(uiColor))
                .isPageStrategy(true)
//                .imageFormat(PictureMimeType.PNG.toLowerCase())// 拍照保存图片格式后缀,默认jpeg
                .isCamera(showCamera)
                .isGif(showGif)
                .maxSelectNum(enableCrop ? 1 : selectCount.intValue())
                .withAspectRatio(width.intValue(), height.intValue())
                .recordVideoSecond(cameraCaptureMaxTime)
                .imageSpanCount(4)// 每行显示个数 int
                .synOrAsy(false)
                .selectionMode(enableCrop || selectCount.intValue() == 1 ? PictureConfig.SINGLE : PictureConfig.MULTIPLE)// 多选 or 单选 PictureConfig.MULTIPLE or PictureConfig.SINGLE
                .isSingleDirectReturn(false)// 单选模式下是否直接返回
                .previewImage(true)// 是否可预览图片 true or false
                .enableCrop(enableCrop)// 是否裁剪 true or false
                .circleDimmedLayer(false)
                .showCropFrame(true)
                .showCropGrid(true)
                .hideBottomControls(true)
                .freeStyleCropEnabled(false)
                .cutOutQuality(90)
                .minimumCompressSize(100)
                .compressSavePath(ImageCompressUtils.getPath(getBaseContext()))//压缩图片保存地址
                .forResult(PictureConfig.CHOOSE_REQUEST);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片、视频、音频选择结果回调
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    List<String> paths = new ArrayList<>();
                    for (int i = 0; i < selectList.size(); i++) {
                        LocalMedia localMedia = selectList.get(i);
                        if (localMedia.isCut()) {//2.5.9 android Q 裁剪没问题
                            // 因为这个lib中 gif裁剪有问题，所以gif裁剪过就不使用裁剪地址，使用原gif地址
                            if (Build.VERSION.SDK_INT >= 29) {
                                if (localMedia.getPath() != null && localMedia.getAndroidQToPath() != null && localMedia.getAndroidQToPath().endsWith(".gif")) {
                                    String path = PictureFileUtils.getPath(getApplicationContext(), Uri.parse(localMedia.getPath()));
                                    paths.add(path);
                                } else {
                                    paths.add(localMedia.getCutPath());
                                }
                            } else {
                                if (localMedia.getPath() != null && localMedia.getPath().endsWith(".gif")) {
                                    paths.add(localMedia.getPath());
                                } else {
                                    paths.add(localMedia.getCutPath());
                                }
                            }
                        } else {
                            if (Build.VERSION.SDK_INT >= 29) {
                                //图片选择库 2.5.9 有bug，要这样处理
                               /* String AndroidQToPath = AndroidQTransformUtils.copyPathToAndroidQ(SelectPicsActivity.this, localMedia.getPath(), localMedia.getWidth(), localMedia.getHeight(), localMedia.getMimeType(), localMedia.getRealPath().substring(localMedia.getRealPath().lastIndexOf("/") + 1));
                                localMedia.setAndroidQToPath(AndroidQToPath);
                                paths.add(localMedia.getAndroidQToPath());*/
                                if(localMedia.getMimeType().startsWith("video")){
                                    String path = localMedia.getPath();
                                    String realPath = PictureMimeType.isContent(path) && !localMedia.isCut() && !localMedia.isCompressed() ? Uri.parse(path).getPath() : path;
                                    paths.add(realPath);
                                }else{
                                    paths.add(localMedia.getRealPath());
                                }
                            } else {
                                paths.add(localMedia.getPath());
                            }
                        }
                    }

                    if (mimeType != null) {
                        //直接调用拍照或拍视频时
                        if ("photo".equals(mimeType)) {
                            lubanCompress(paths);
                        } else {
                            resolveVideoPath(paths, selectList);
                        }
                    } else {
                        if ("image".equals(mode)) {
                            //如果选择的是图片就压缩
                            lubanCompress(paths);
                        } else {
                            resolveVideoPath(paths, selectList);
                        }
                    }
                    break;
                case WRITE_SDCARD:


                    break;
            }
        } else {
            finish();
        }
    }

    private void resolveVideoPath(List<String> paths, List<LocalMedia> selectList) {
        List<Map<String, Object>> thumbPaths = new ArrayList<>();
        for (int i = 0; i < paths.size(); i++) {
            String path = paths.get(i);
            LocalMedia localMedia = selectList.get(i);
            Bitmap bitmap = null;
            bitmap = ThumbnailUtils.createVideoThumbnail(localMedia.getRealPath(), MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
            String thumbPath = CommonUtils.saveBitmap(this, new AppPath(this).getAppImgDirPath(false), bitmap);
            Log.e("CMW","thumbPath----------->"+ thumbPath);
            Map<String, Object> map = new HashMap<>();
            map.put("thumbPath", thumbPath);
            map.put("path", localMedia.getRealPath());
            Log.e("image_pickers", "videoDuration--->" + localMedia.getDuration() + "   " + "videoWidth--->" + localMedia.getWidth() + "   " + "videoHeight--->" + localMedia.getHeight() + "   " + "videoSize--->" + localMedia.getSize());
            map.put("videoDuration", localMedia.getDuration());
            int orientation = -1;
        /*    if (PictureMimeType.isContent(path)) {
                orientation = MediaUtils.getVideoOrientationForUri(getApplicationContext(), Uri.parse(path));
            } else {
                orientation = MediaUtils.getVideoOrientationForUrl(path);
            }*/
            map.put("videoWidth", localMedia.getWidth());
            map.put("videoHeight", localMedia.getHeight());
            map.put("videoSize", localMedia.getSize());
            map.put("videoOrientation", orientation);
            thumbPaths.add(map);
        }
        Intent intent = new Intent();
        intent.putExtra(COMPRESS_PATHS, (Serializable) thumbPaths);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void lubanCompress(final List<String> paths) {
        final List<Map<String, String>> lubanCompressPaths = new ArrayList<>();
        for (String path : paths) {
            Map<String, String> map = new HashMap<>();
            map.put("thumbPath", path);
            map.put("path", path);
            lubanCompressPaths.add(map);
        }
        compressFinish(paths, lubanCompressPaths);
    }

    private void compressFinish(List<String> paths, List<Map<String, String>> compressPaths) {
        Intent intent = new Intent();
        intent.putExtra(COMPRESS_PATHS, (Serializable) compressPaths);
        setResult(RESULT_OK, intent);
        finish();
    }
}
