package android.me.lj.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.widget.Toast;

/**
 * Created by Administrator on 2018/3/27.
 */

public abstract class VisibleFragment extends Fragment {

    private static final String TAG = "VisibleFragment";

    @Override
    public void onStart() {
        super.onStart();
        /**
         * 要传入一个IntentFilter，必须先以代码的方式创建它。这里创建的IntentFilter同以下XML文件定义的filter是一样的：
         * <intent-filter>
         *      <action android:name="android.me.lj.photogallery.SHOW_NOTIFICATION" />
         * </intent-filter>
         *
         * 任何使用XML定义的IntentFilter均能以代码的方式定义。
         * 要在代码中配置IntentFilter，可以直接调用addCategory(String)、 addAction(String)和addDataPath(String)等方法。
         */
        IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotification, filter);
    }

    @Override
    public void onStop() {
        super.onStop();

        /**
         * 使用动态登记的broadcast receiver时，要记得事后清理。
         * 通常，如果在生命周期启动方法中登记了一个receiver，就应在相应的停止方法中调用Context.unregisterReceiver(BroadcastReceiver)方法。
         * 这里，我们在onStart()方法里登记，在onStop()方法里撤销登记。
         *
         * 同样，如果在onCreate(...)方法里登记，就应在onDestroy()里撤销登记。
         * 但是，设备旋转时， onCreate(...)和onDestroy()方法中的getActivity()方法会返回不同的值。
         * 因此，如果想在Fragment.onCreate(Bundle)和Fragment.onDestroy()方法中实现登记或撤销登记，
         * 应改用getActivity().getApplicationContext()方法。
         */

        getActivity().unregisterReceiver(mOnShowNotification);
    }

    private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getActivity(),
                    "Got a broadcast:" + intent.getAction(),
                    Toast.LENGTH_LONG)
                    .show();
        }
    };
}
