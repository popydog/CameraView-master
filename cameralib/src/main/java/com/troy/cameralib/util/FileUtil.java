package com.troy.cameralib.util;

import android.app.Activity;
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
        if (!newBmp.isRecycled()) {
            newBmp.recycle();
        }
        return resizeBmp;
    }

    public static Bitmap convertGreyImg(Bitmap img,int w, int h) {
        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高

        int []pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for(int i = 0; i < height; i++)  {
            for(int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey  & 0x00FF0000 ) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(result, w, h);
        if (!result.isRecycled()) {
            result.recycle();
        }
        return resizeBmp;
    }

    public static  int [] convertGreyImg(Bitmap img) {
        int width = img.getWidth();         //获取位图的宽
        int height = img.getHeight();       //获取位图的高

        int []pixels = new int[width * height]; //通过位图的大小创建像素点数组

        img.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for(int i = 0; i < height; i++)  {
            for(int j = 0; j < width; j++) {
                int grey = pixels[width * i + j];

                int red = ((grey  & 0x00FF0000 ) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);

                grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }

        return pixels;
    }



    /**
     * 对图片进行二值化处理
     *
     * @param bm
     *            原始图片
     * @return 二值化处理后的图片
     */
    public static Bitmap getBinaryzationBitmap(Bitmap bm, int w, int h) {
        // 获取图片的宽和高
        int width = bm.getWidth();
        int height = bm.getHeight();


        // 新建图片
        Bitmap newBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // 设置图片数据

        // 遍历原始图像像素,并进行二值化处理
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                // 得到当前的像素值
                int pixel = bm.getPixel(i, j);
                // 得到Alpha通道的值
                int alpha = pixel & 0xFF000000;
                // 得到Red的值
                int red = (pixel & 0x00FF0000) >> 16;
                // 得到Green的值
                int green = (pixel & 0x0000FF00) >> 8;
                // 得到Blue的值
                int blue = pixel & 0x000000FF;
                // 通过加权平均算法,计算出最佳像素值
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                // 对图像设置黑白图
                if (gray <= 95) {
                    gray = 0;
                } else {
                    gray = 255;
                }
                // 得到新的像素值
                int newPiexl = alpha | (gray << 16) | (gray << 8) | gray;
                // 赋予新图像的像素
                newBmp.setPixel(i, j, newPiexl);
            }
        }


        Bitmap resizeBmp = ThumbnailUtils.extractThumbnail(newBmp, w, h);
        return resizeBmp;

    }


    public static int getPixColor(Bitmap src) {
        int pixelColor;
        pixelColor = src.getPixel(5, 5);
        return pixelColor;
    }


    public static int getPicHilight(Activity activity, Bitmap bitmap, int imageWidth,
            int imageHeight){


        Log.i("摄像机的参数:生成的照片的宽", "imageWidth:" + imageWidth);
        Log.i("摄像机的参数:生成的照片的高", "imageHeight:" + imageHeight);

        int x[] = { 0, imageWidth/10, imageWidth/9, imageWidth/8, imageWidth/7, imageWidth/6, imageWidth/5, imageWidth/4, imageWidth/3, imageWidth/2, imageWidth-1 };
        int y[] = { 0, imageHeight/10, imageHeight/9, imageHeight/8, imageHeight/7, imageHeight/6, imageHeight/5, imageHeight/4, imageHeight/3, imageHeight/2, imageHeight-1 };



        int r;
        int g;
        int b;
        int number = 0;
        double bright = 0;
        Integer localTemp;
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < y.length; j++) {
                number++;
                localTemp = (Integer) bitmap.getPixel(x[i], y[j]);
                r = (localTemp | 0xff00ffff) >> 16 & 0x00ff;
                g = (localTemp | 0xffff00ff) >> 8 & 0x0000ff;
                b = (localTemp | 0xffffff00) & 0x0000ff;

                bright = bright + 0.299 * r + 0.587 * g + 0.114 * b;
            }
        }

       return (int)(bright / number);
    }


}
