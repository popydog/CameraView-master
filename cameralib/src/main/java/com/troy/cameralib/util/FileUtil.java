package com.troy.cameralib.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.media.ExifInterface;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Author: Troy
 * Date: 2017/8/30
 * Email: 810196673@qq.com
 * Des: FileUtil
 */

public class FileUtil {
    private static final  String TAG ="摄像机的参数";
    private static final File parentPath = Environment.getExternalStorageDirectory();
    private static   String storagePath = "";
    private static final String FOLDER_NAME = "CameraView";
    private static String path;

    /**
     * 保存Bitmap到SD卡
     * @param b
     * @param savePath
     */
    public static void saveBitmap(Bitmap b,String savePath){
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(savePath);
            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
            b.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            Log.i(TAG, "saveBitmap成功"+savePath);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.i(TAG, "saveBitmap:失败");
            e.printStackTrace();
        }
    }

    /**
     * 根据文件Uri获取文件路径
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath(final Context context,final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String path = null;
        if ( scheme == null )
            path = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            path = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        path = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return path;
    }

    /**
     * 获取照片被旋转的角度
     * @param data
     * @return
     */
    public static int getRotateDegree(byte[] data){
        // Find out if the picture needs rotating by looking at its Exif data
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(new ByteArrayInputStream(data));
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        int rotationDegrees = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotationDegrees = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotationDegrees = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotationDegrees = 270;
                break;
        }
        return rotationDegrees;
    }

    public static Bitmap rotateBitmap(int angle,Bitmap bitmap) {
        if(bitmap != null){
            int myWidth = bitmap.getWidth();
            int myHeight = bitmap.getHeight();
            //旋转图片 动作
            Matrix mMatrix = new Matrix();
            mMatrix.postRotate(angle);
            // 创建新的图片
            Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                    myWidth, myHeight, mMatrix, true);
            return resizedBitmap;
        }else{
            return bitmap;
        }
    }


    private static final String SD_PATH = "/sdcard/dskqxt/pic/";
    private static final String IN_PATH = "/dskqxt/pic/";

    public static String saveBitmap(Context context, Bitmap mBitmap) {
        String savePath;
        File filePic;
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            savePath = SD_PATH;
        } else {
            savePath = context.getApplicationContext().getFilesDir()
                    .getAbsolutePath()
                    + IN_PATH;
        }
        try {
            path = savePath + System.currentTimeMillis() + ".jpg";
            filePic = new File(path);
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
       // Log.i(TAG, "方法二saveBitmap成功"+path);

        return filePic.getAbsolutePath();
    }



    /**
     * 转为二值图像
     *
     * @param bmp
     *            原图bitmap
     * @param w
     *            转换后的宽
     * @param h
     *            转换后的高
     * @return
     */
    public static Bitmap convertToBMW(Bitmap bmp, int w, int h,int tmp) {

       // Bitmap bm = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(path), 502, 302, true);

        int width = bmp.getWidth(); // 获取位图的宽
        int height = bmp.getHeight(); // 获取位图的高
        int[] pixels = new int[width * height]; // 通过位图的大小创建像素点数组
        // 设定二值化的域值，默认值为100
        //tmp = 180;
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];
                // 分离三原色
                alpha = ((grey & 0xFF000000) >> 24);
                int red = ((grey & 0x00FF0000) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);
                if (red > tmp) {
                    red = 255;
                } else {
                    red = 0;
                }
                if (blue > tmp) {
                    blue = 255;
                } else {
                    blue = 0;
                }
                if (green > tmp) {
                    green = 255;
                } else {
                    green = 0;
                }
                pixels[width * i + j] = alpha << 24 | red << 16 | green << 8
                        | blue;
                if (pixels[width * i + j] == -1) {
                    pixels[width * i + j] = -1;
                } else {
                    pixels[width * i + j] = -16777216;
                }
            }
        }
        // 新建图片
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 设置图片数据
        newBmp.setPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, w, h);
        return resizeBmp;
    }


    public static int getPixColor(Bitmap src) {
        int pixelColor;
        pixelColor = src.getPixel(5, 5);
        return pixelColor;
    }


}
