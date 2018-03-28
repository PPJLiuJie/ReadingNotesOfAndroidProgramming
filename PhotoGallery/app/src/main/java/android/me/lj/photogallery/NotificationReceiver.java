package android.me.lj.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

/**
 * Created by Administrator on 2018/3/28.
 */

public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "received result: " + getResultCode());

        /**
         * 在PoolServer的showBackgroundNotification中发送有序广播时，
         * resultCode被设置为Activity.RESULT_OK，
         * 但是在VisibleFragment的mOnShowNotification的onReceive方法中，
         * resultCode被设置为Activity.RESULT_CANCEL
         */
        if (getResultCode() != Activity.RESULT_OK) {
            return;
        }

        int requestCode = intent.getIntExtra(PollService.REQUEST_CODE, 0);

        /**
         * 从当前context中取出一个NotificationManagerCompat实例
         */
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Notification notification = intent.getParcelableExtra(PollService.NOTIFICATION);

        /**
         * 贴出消息
         * 参数一：标识符，在整个应用中该标识符应该唯一。
         *        如果使用同一ID发送两条消息，则第二条消息会替换掉第一条消息。
         *        实际开发中，如果要在Notification中显示进度条并实时更新进度，或者要实现其他动态效果，通过这个标识符可以实现
         *
         * 参数二：Notification对象
         */
        notificationManager.notify(requestCode, notification);
    }
}
