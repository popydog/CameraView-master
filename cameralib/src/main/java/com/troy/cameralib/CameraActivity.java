package com.troy.cameralib;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.cameraview.AspectRatio;
import com.google.android.cameraview.CameraView;
import com.troy.cameralib.util.DisplayUtil;
import com.troy.cameralib.util.FileUtil;
import com.troy.cameralib.view.MaskView;

import java.math.BigDecimal;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Author: Troy
 * Date: 2017/8/30
 * Email: 810196673@qq.com
 * Des: CameraActivity
 */

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "摄像机的参数";
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private static final String FRAGMENT_DIALOG = "dialog";

    //控件View
    private CameraView mCameraView;
    private MaskView viewMask;
    private ImageButton ibtCapture;
    private ImageView ivReturn;
    private TextView lighttext;



    private float ratio; //高宽比
    private float cameraRatio; // 相机高宽比

    private int mCameraWidth;
    private int mCameraHeight;
    private Uri imageUri;
    private String imagePath;
    private int leftRight;
    private int topBottom;
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private ImageView biankuang, showpic;
    private ExecutorService mCachedThreadPool;


    private Handler mHandler1;
    private Runnable mRunnable;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        setupViews(getIntent());
        initView();
        mCachedThreadPool = Executors.newCachedThreadPool();
        mCameraView.addCallback(mCallback);
        mCameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraView.setAutoFocus(false);
                mCameraView.setAutoFocus(true);
                Log.d(TAG, "自动对焦" + mCameraView.getAutoFocus());

            }
        });
        mHandler1 = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {

                mCameraView.takePicture();
                mHandler1.postDelayed(this, 500);
            }
        };
        mHandler1.postDelayed(mRunnable, 500);//每两秒执行一次runnable.
    }


    private void setupViews(@NonNull Intent mIntent) {
        leftRight = mIntent.getIntExtra(EasyCamera.EXTRA_MARGIN_BY_WIDTH, 0);
        topBottom = mIntent.getIntExtra(EasyCamera.EXTRA_MARGIN_BY_HEIGHT, 0);
        ratio = mIntent.getFloatExtra(EasyCamera.EXTRA_VIEW_RATIO, 1f);
        tmp = mIntent.getIntExtra(EasyCamera.EXTRA_TMP, 100);
        imageUri = mIntent.getParcelableExtra(EasyCamera.EXTRA_OUTPUT_URI);
        imagePath = FileUtil.getRealFilePath(this, imageUri);
    }

    private void initView() {
        mCameraView = (CameraView) findViewById(R.id.camera_view);
        mCameraView.setAutoFocus(true);
        mCameraView.setFlash(0);
        viewMask = (MaskView) findViewById(R.id.view_mask);
        biankuang = (ImageView) findViewById(R.id.biankuang);
        showpic = (ImageView) findViewById(R.id.showpic);
        ibtCapture = (ImageButton) findViewById(R.id.ibt_capture);
        ivReturn = (ImageView) findViewById(R.id.iv_return);
        lighttext = (TextView) findViewById(R.id.lighttext);
        ibtCapture.setClickable(true);
        ibtCapture.setOnClickListener(this);
        ivReturn.setOnClickListener(this);
        showpic.setOnClickListener(this);
        AspectRatio currentRatio = mCameraView.getAspectRatio();
        cameraRatio = currentRatio.toFloat();
        mCameraWidth = (int) DisplayUtil.getScreenWidth(this);
        mCameraHeight = (int) (mCameraWidth * cameraRatio);
           Log.i("摄像机的参数:mCameraView的大小",
        "mCameraWidth:"+mCameraWidth+"mCameraHeight:"+mCameraHeight+"cameraRatio:"+cameraRatio);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.width = mCameraWidth;
        layoutParams.height = mCameraHeight;

        viewMask.setLayoutParams(layoutParams);




        if (viewMask != null) {
            double 宽精度 = div(mCameraWidth, 1080, 5);
            x1 = (int) (宽精度 * 270);
            y1 = (int) (div(mCameraHeight, 1440, 5) * 653);
            x2 = (int) (宽精度 * 865);
            y2 = (int) (div(mCameraHeight, 1440, 5) * 735);
            Rect screenCenterRect = DisplayUtil.setMaskRect(x1, y1, x2, y2);
            viewMask.setCenterRect(screenCenterRect);

            ViewGroup.MarginLayoutParams margin9 = new ViewGroup.MarginLayoutParams(
                    biankuang.getLayoutParams());
            margin9.setMargins((int) (30 * 宽精度), (int) (200 * 宽精度), 0, 0);//在左边距400像素，顶边距10像素的位置显示图片
            RelativeLayout.LayoutParams layoutParams9 = new RelativeLayout.LayoutParams(margin9);
            layoutParams9.height = (int) (630 * 宽精度);//设置图片的高度
            layoutParams9.width = (int) (1000 * 宽精度); //设置图片的宽度
            biankuang.setLayoutParams(layoutParams9);
        }


//        ViewGroup.MarginLayoutParams margin9 = new ViewGroup.MarginLayoutParams(
//                biankuang.getLayoutParams());
//        margin9.setMargins(40, 268, 0, 0);//在左边距400像素，顶边距10像素的位置显示图片
//        RelativeLayout.LayoutParams layoutParams9 = new RelativeLayout.LayoutParams(margin9);
//        layoutParams9.height = 844;//设置图片的高度
//        layoutParams9.width = 1340; //设置图片的宽度
//        biankuang.setLayoutParams(layoutParams9);

    }


    @Override
    protected void onResume() {
        super.onResume();
        checkPermission();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    private int tmp = 100;
    private int 存储照片的宽;
    private int 存储照片的高;
    private CameraView.Callback mCallback = new CameraView.Callback() {

        @Override
        public void onCameraOpened(CameraView cameraView) {
            super.onCameraOpened(cameraView);
            Log.d(TAG, "onCameraOpened");
        }

        @Override
        public void onCameraClosed(CameraView cameraView) {
            super.onCameraClosed(cameraView);
            Log.d(TAG, "onCameraClosed");
        }

        @Override
        public void onPictureTaken(CameraView cameraView, final byte[] data) {
            Log.d(TAG, "onPictureTaken " + data.length);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    Log.d("google_lenve_fb", "run: " + Thread.currentThread().getName());

                    Bitmap bitmap = null;
                    int degree; //图片被旋转的角度
                    if (data != null) {
                        bitmap = BitmapFactory.decodeByteArray(data, 0,
                                data.length);//data是字节数据，将其解析成类图
                    }
                    //保存图片到sdcard
                    if (bitmap != null) {
                        degree = FileUtil.getRotateDegree(data);
                        if (degree != 0) {
                            //如果图片被系统旋转了，就旋转过来
                            bitmap = FileUtil.rotateBitmap(degree, bitmap);
                        }





                        Log.i("摄像机的参数:生成的照片的大小",
                                "宽:" + bitmap.getWidth() + "高:" + bitmap.getHeight());
                        Log.i("摄像机的参数:在生成的照片上截取的矩形的大小", "x:" + (int) (div(bitmap.getWidth(),
                                mCameraWidth,
                                3) * x1) + "y:" + (int) (div(bitmap.getHeight(), mCameraHeight, 3))
                                * y1
                                + "width:" + (int) ((x2 - x1) * div(bitmap.getWidth(), mCameraWidth,
                                3))
                                + "height:" + (int) ((y2 - y1) * div(bitmap.getHeight(),
                                mCameraHeight,
                                3)));

                        Bitmap rectBitmap = Bitmap.createBitmap
                                (bitmap, (int) (div(bitmap.getWidth(), mCameraWidth, 5) * x1),
                                        (int) (div(bitmap.getHeight(), mCameraHeight, 5) * y1),
                                        (int) ((x2 - x1) * div(bitmap.getWidth(), mCameraWidth, 5)),
                                        (int) ((y2 - y1) * div(bitmap.getHeight(), mCameraHeight,
                                                5)));


                        int imageWidth = rectBitmap.getWidth();
                        int imageHeight = rectBitmap.getHeight();
                        final int gilight = FileUtil.getPicHilight(CameraActivity.this, rectBitmap,
                                imageWidth,
                                imageHeight);
                        存储照片的宽 = 530;
                        存储照片的高 = 78;
                        if (gilight >= 170 && gilight < 200) {
                            tmp = 110;
                        } else if (gilight >= 130 && gilight < 170) {
                            tmp = 100;
                        } else if (gilight >= 100 && gilight < 130) {
                            tmp = 80;
                        } else if (gilight >= 80 && gilight < 100) {
                            tmp = 60;
                        } else if (gilight >= 60 && gilight < 80) {
                            tmp = 40;
                        } else if (gilight >= 30 && gilight < 60) {
                            tmp = 30;
                        } else if (gilight >= 200) {
                            tmp = 130;
                        }
                        final Bitmap finalimg = FileUtil.convertToBMW(rectBitmap, 存储照片的宽, 存储照片的高, tmp);
                        // Bitmap finalimg = FileUtil.getBinaryzationBitmap(rectBitmap, 存储照片的宽,
                        // 存储照片的高);
                        //FileUtil.saveBitmap(finalimg, imagePath);
                        String realpath = FileUtil.saveBitmap(CameraActivity.this, finalimg);
                        setResultUri(realpath, imageWidth, imageHeight);
                        Log.i("摄像机的参数:生成的照片的uri", "imageUri:" + imageUri);
                        Log.i("摄像机的参数:生成的照片的真实路径", "imagePath:" + imagePath);
                        Log.i("摄像机的参数:生成的照片能查看到的真实路径", "realpath:" + realpath);
                        Log.i("摄像机的参数:生成的照片亮度", "gilight:" + gilight);

                        CameraActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                lighttext.setText("图片亮度数值为:" + gilight + "\n二值化力度为:" + tmp);
                                ibtCapture.setClickable(true);
                                showpic.setVisibility(View.VISIBLE);
                                showpic.setImageBitmap(finalimg);
                            }
                        });
                        if (!bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                        if (!rectBitmap.isRecycled()) {
                            rectBitmap.recycle();
                        }


                    }
                }
            };

            mCachedThreadPool.execute(runnable);
        }

    };

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.ibt_capture) {

            mCameraView.takePicture();
        } else if (i == R.id.iv_return) {
            CameraActivity.this.finish();
        } else if (i == R.id.showpic) {
            showpic.setVisibility(View.GONE);
        }
    }


    /**
     * @param uri         图片Uri
     * @param imageWidth  图片宽
     * @param imageHeight 图片高
     */
    protected void setResultUri(Uri uri, int imageWidth, int imageHeight) {
        setResult(RESULT_OK, new Intent()
                .putExtra(EasyCamera.EXTRA_OUTPUT_URI, uri)
                .putExtra(EasyCamera.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
                .putExtra(EasyCamera.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
        );
    }

    /**
     * @param url         图片url
     * @param imageWidth  图片宽
     * @param imageHeight 图片高
     */
    protected void setResultUri(String url, int imageWidth, int imageHeight) {
        setResult(RESULT_OK, new Intent()
                .putExtra(EasyCamera.EXTRA_OUTPUT_URI, url)
                .putExtra(EasyCamera.EXTRA_OUTPUT_IMAGE_WIDTH, imageWidth)
                .putExtra(EasyCamera.EXTRA_OUTPUT_IMAGE_HEIGHT, imageHeight)
        );
    }


    /********************************** 以下是权限检查部分 ********************************/
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            mCameraView.start();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ConfirmationDialogFragment
                    .newInstance(R.string.camera_permission_confirmation,
                            new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION,
                            R.string.camera_permission_not_granted)
                    .show(getSupportFragmentManager(), FRAGMENT_DIALOG);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        }
    }


    public static class ConfirmationDialogFragment extends DialogFragment {

        private static final String ARG_MESSAGE = "message";
        private static final String ARG_PERMISSIONS = "permissions";
        private static final String ARG_REQUEST_CODE = "request_code";
        private static final String ARG_NOT_GRANTED_MESSAGE = "not_granted_message";

        public static ConfirmationDialogFragment newInstance(@StringRes int message,
                String[] permissions, int requestCode, @StringRes int notGrantedMessage) {
            ConfirmationDialogFragment fragment = new ConfirmationDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_MESSAGE, message);
            args.putStringArray(ARG_PERMISSIONS, permissions);
            args.putInt(ARG_REQUEST_CODE, requestCode);
            args.putInt(ARG_NOT_GRANTED_MESSAGE, notGrantedMessage);
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle args = getArguments();
            return new AlertDialog.Builder(getActivity())
                    .setMessage(args.getInt(ARG_MESSAGE))
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String[] permissions = args.getStringArray(ARG_PERMISSIONS);
                                    if (permissions == null) {
                                        throw new IllegalArgumentException();
                                    }
                                    ActivityCompat.requestPermissions(getActivity(),
                                            permissions, args.getInt(ARG_REQUEST_CODE));
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(getActivity(),
                                            args.getInt(ARG_NOT_GRANTED_MESSAGE),
                                            Toast.LENGTH_SHORT).show();
                                }
                            })
                    .create();
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                @NonNull int[] grantResults) {
            switch (requestCode) {
                case REQUEST_CAMERA_PERMISSION:
                    if (permissions.length != 1 || grantResults.length != 1) {
                        throw new RuntimeException(getString(R.string.error_camera_permission));
                    }
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getActivity(), R.string.camera_permission_not_granted,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }

    }


    public static double div(double v1, double v2, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException(
                    "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    /******************************** 以上是权限部分 ********************************/


    public static void setLayout(View view, int x, int y) {
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(
                view.getLayoutParams());
        margin.setMargins(x, y, x + margin.width, y + margin.height);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
        view.setLayoutParams(layoutParams);
    }

    /*
    * 设置控件所在的位置X，并且不改变宽高，
    * X为绝对位置，此时Y可能归0
    */
    public static void setLayoutX(View view, int x) {
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(
                view.getLayoutParams());
        margin.setMargins(x, margin.topMargin, x + margin.width, margin.bottomMargin);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
        view.setLayoutParams(layoutParams);
    }

    /*
     * 设置控件所在的位置Y，并且不改变宽高，
     * Y为绝对位置，此时X可能归0
     */
    public static void setLayoutY(View view, int y) {
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(
                view.getLayoutParams());
        margin.setMargins(margin.leftMargin, y, margin.rightMargin, y + margin.height);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(margin);
        view.setLayoutParams(layoutParams);
    }
}
