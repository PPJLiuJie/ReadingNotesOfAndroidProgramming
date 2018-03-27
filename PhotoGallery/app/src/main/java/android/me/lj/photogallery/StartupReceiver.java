package android.me.lj.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Administrator on 2018/3/27.
 */

public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupReceiver";

    /**
     * broadcast receiver的生命非常短暂，因而难以有所作为。
     * 例如，我们无法使用任何异步API或登记任何监听器，因为一旦onReceive(Context, Intent)方法运行完， receiver就不存在了。
     * onReceive(Context, Intent)方法同样运行在主线程上，因此不能在该方法内做一些费时费力的事情，如网络连接或数据的永久存储等。
     *
     * 然而，这并不代表receiver一无用处。一些便利型任务就非常适合它，比如启动activity或服务（不需要等返回结果），以及系统重启后重置定时运行的定时器。
     */

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

        boolean isOn = QueryPreferences.isAlarmOn(context);
        PollService.setServiceAlarm(context, isOn);
    }
}
