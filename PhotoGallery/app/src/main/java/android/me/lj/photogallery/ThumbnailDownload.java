package android.me.lj.photogallery;

import android.os.HandlerThread;
import android.util.Log;

/**
 * Created by Administrator on 2018/3/23.
 */

public class ThumbnailDownload<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownload";

    private boolean mHasQuit = false;

    public ThumbnailDownload() {
        super(TAG);
    }

    /**
     * 线程退出通知方法
     */
    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);
    }
}
