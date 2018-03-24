package android.me.lj.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Administrator on 2018/3/23.
 */

public class ThumbnailDownload<T> extends HandlerThread {

    private static final String TAG = "ThumbnailDownload";
    private static final int MESSAGE_DOWNLOAD = 0;

    private boolean mHasQuit = false;

    /**
     * 新添加的mRequestHandler用来存储对Handler的引用。
     * 这个Handler负责在ThumbnailDownloader后台线程上管理下载请求消息队列。
     * 还负责从消息队列里取出并处理下载请求消息。
     *
     * 使用ThumbnailDownloader的mRequestHandler，我们已可以从主线程安排后台线程任务
     */
    private Handler mRequestHandler;

    /**
     * ConcurrentHashMap是一种线程安全的HashMap。
     */
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    /**
     * 来自于主线程的Handler
     */
    private Handler mResponseHandler;

    public ThumbnailDownload() {
        super(TAG);
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        /**
         * onLooperPrepared()是在Looper首次检查消息队列之前调用，所以该方法是创建Handler实现的好地方。
         */
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
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
        if (url == null) {
            mRequestMap.remove(target);
        } else {
            /**
             * 更新mRequestMap并把下载消息放到后台线程的消息队列中去
             */
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
        }
    }

    /**
     * handleRequest()方法是下载执行的地方
     */
    private void handleRequest(T target) {

        try {
            String url = mRequestMap.get(target);
            if (url == null) {
                return;
            }
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            /**
             * 使用BitmapFactory把getUrlBytes(...)返回的字节数组转换为位图
             */
            Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error downloading image", e);
        }
    }
}
