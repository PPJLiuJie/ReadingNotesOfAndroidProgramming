package android.me.lj.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2018/3/26.
 */

public class PollService extends IntentService {

    private static final String TAG = "PollService";

    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);

    public static final String ACTION_SHOW_NOTIFICATION = "android.me.lj.photogallery.SHOW_NOTIFICATION";

    public static final String PERM_PRIVATE = "android.me.lj.photogallery.PRIVATE";

    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    public static Intent newIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    /**
     * 用于启停定时器
     * @param context
     * @param isOn true表示启动，false表示停止
     */
    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent intent = PollService.newIntent(context);

        /**
         * 创建一个用来启动PollService的PendingIntent
         *
         * getService有四个参数：
         * 参数一：Context
         * 参数二：用于区分PendingIntent来源的请求码
         * 参数三：待发送的Intent对象
         * 参数四：用于决定如何创建PendingIntent的标识符
         */
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            /**
             * setRepeating方法用于创建定时器，该方法有四个参数：
             * 参数一：描述定时器的时间基准常量
             * 参数二：定时器启动的时间
             * 参数三：定时器循环的时间间隔
             * 参数四：到时要发送的PendingIntent对象
             *
             * AlarmManager详解参见博客：https://blog.csdn.net/coder_pig/article/details/49423531
             */
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime(),
                    POLL_INTERVAL_MS,
                    pendingIntent);
        } else {
            /**
             * 调用AlarmManager.cancel(PendingIntent)方法取消定时器。
             * 通常，也需同步取消PendingIntent。
             */
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        /**
         * BroadcastReceiver需要知道定时器的启停状态，但却无法调用本类中的isServiceAlarmOn来判断启停状态，
         * 所以在这里将启停状态存入SharedPreference
         */
        QueryPreferences.setAlarmOn(context, isOn);
    }

    /**
     * 使用PendingIntent来管理定时器
     * 通过检查PendingIntent是否存在来确认定时器激活与否
     * @param context
     * @return
     */
    public static boolean isServiceAlarmOn(Context context) {
        Intent intent = PollService.newIntent(context);
        /**
         * PendingIntent.FLAG_NO_CREATE标志 表示如果PendingIntent不存在，则返回null，而不是创建它。
         */
        PendingIntent pendingIntent = PendingIntent
                .getService(context, 0, intent, PendingIntent.FLAG_NO_CREATE);
        /**
         * PendingIntent空值表示定时器还未设置
         */
        return pendingIntent != null;
    }

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        if (!isNetworkAvailableAndConnected()) {
            return;
        }

        Log.i(TAG, "Received an intent: " + intent);

        String query = QueryPreferences.getStoredQuery(this);
        String lastResultId = QueryPreferences.getLastResultId(this);

        List<GalleryItem> items;

        if (query == null) {
            items = new FlickrFetchr().fetchRecentPhotos();
        } else {
            items = new FlickrFetchr().searchPhotos(query);
        }

        if (items.size() == 0) {
            return;
        }

        String resultId = items.get(0).getId();

        if (resultId.equals(lastResultId)) {
            Log.i(TAG, "Got an old result: " + resultId);
        } else {
            Log.i(TAG, "Got a new result: " + resultId);

            Resources resources = getResources();
            Intent i = PhotoGalleryActivity.newIntent(this);
            PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

            Notification notification = new NotificationCompat.Builder(this)
//                    .setTicker(resources.getString(R.string.new_pictures_title))
                    .setTicker("HAHA")// ticker text
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)// 小图标
                    .setContentTitle(resources.getString(R.string.new_pictures_title))// 标题
                    .setContentText(resources.getString(R.string.new_pictures_text))// 内容
                    .setContentIntent(pi)// 用户点击消息时所触发的动作行为
                    .setAutoCancel(true)// 用户点击消息时，该消息就会从消息抽屉中删除
                    .build();

            /**
             * 有新的所搜结果，发送有序广播
             */
            showBackgroundNotification(0, notification);
        }

        /**
         * 存储最近一次获取结果的第一条数据的id
         */
        QueryPreferences.setLastResultId(this, resultId);
    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent intent = new Intent(ACTION_SHOW_NOTIFICATION);
        intent.putExtra(REQUEST_CODE, requestCode);
        intent.putExtra(NOTIFICATION, notification);

        /**
         * 发送有序广播
         * 参数一：Intent对象
         * 参数二：权限，即接受者所需要具备的权限。
         * 参数三：略
         * 参数四：略
         * 参数五：结果代码的初始值
         * 参数六：结果数据
         * 参数七：结果附加值
         */
        sendOrderedBroadcast(intent, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
    }

    /**
     * 检查网络是否可用
     */
    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = (cm.getActiveNetworkInfo() != null);
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }
}
