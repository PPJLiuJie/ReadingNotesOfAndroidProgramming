package android.me.lj.criminalintent.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

/**
 * Created by Administrator on 2018/3/12.
 */

public class PictureUtil {

    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {

        /**
         * 读取磁盘上图像的尺寸
         */
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        /**
         * inSampleSize值很关键。它决定着缩略图像素的大小。
         * 假设这个值是1的话，就表明缩略图和原始照片的水平像素大小一样。
         * 如果是2的话，它们的水平像素比就是1∶2。
         * 因此， inSampleSize值为2时，缩略图的像素数就是原始文件的四分之一。
         */
        int inSampleSize = 1;

        /**
         * 算出需要缩放的尺寸
         */
        if (srcHeight > destHeight || srcWidth > destWidth) {
            float heightScale = srcHeight / destHeight;
            float widthScale = srcWidth / destWidth;

            inSampleSize = Math.round(heightScale > widthScale ? heightScale : widthScale);
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        // 读取并创建最终的Bitmap
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        /**
         * 先获取屏幕的尺寸，然后按照屏幕的尺寸来缩放位图
         */
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }

}
