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
    private ThumbnailDownloadListener mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownload(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        /**
         * onLooperPrepared()是在Looper首次检查消息队列之前调用，所以该方法是创建Handler实现的好地方。
         *
         * Handler默认与当前线程的Looper相关联。
         * 查看源码可知，onLooperPrepared()方法是在Thread的run方法中被调用的。
         * 因此这个Handler会与子线程的Looper相关联。
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
     * 清除队列中的所有请求
     */
    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    /**
     * handleRequest()方法是下载执行的地方
     */
    private void handleRequest(final T target) {

        try {
            final String url = mRequestMap.get(target);
            if (url == null) {
                return;
            }
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            /**
             * 使用BitmapFactory把getUrlBytes(...)返回的字节数组转换为位图
             */
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");

            /**
             * 一般来说在工作线程中执行耗时任务，当任务完成时，会返回UI线程，一般是更新UI。
             * 这时有两种方法可以达到目的。
             * 一种是handler.sendMessage。发一个消息，再根据消息，执行相关任务代码。
             * 另一种是handler.post(r)。r是要执行的任务代码。意思就是说r的代码实际是在UI线程执行的。可以写更新UI的代码。（工作线程是不能更新UI的）
             * 以上内容摘自百度：https://zhidao.baidu.com/question/550652187.html
             */

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    /**
                     * 检查requestMap。这很有必要，因为RecyclerView会循环使用其视图。
                     * 某个(target, url)键值对被put进Map，下载完成后被remove，然后该键值对有可能再次被put进map，循环反复。
                     *
                     * 检查mHasQuit值。如果ThumbnailDownloader已经退出，运行任何回调方法可能都不太安全。
                     */
                    if (mRequestMap.get(target) != url || mHasQuit) {
                        return;
                    }
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error downloading image", e);
        }
    }
}
