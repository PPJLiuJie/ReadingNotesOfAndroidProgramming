package android.me.lj.beatbox;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Administrator on 2018/3/5.
 */

public abstract  class SingleFragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        /**
         因为使用了支持库及AppCompatActivity类， 所以这里调用了getSupportFragmentManager()
         方法。如果不考虑旧版本的兼容性问题，可直接继承Activity类并调用getFragmentManager()
         方法。
         */
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        /**
         这里为什么先获取fragment，然后对其判空？
             设备旋转或回收内存时， Android系统会销毁CrimeActivity，
             而后重建时，会调用CrimeActivity.onCreate(Bundle)方法。 activity被
             销毁时，它的FragmentManager会将fragment队列保存下来。这样， activity重建时，新的
             FragmentManager会首先获取保存的队列，然后重建fragment队列，从而恢复到原来的状态。
         */
        if (fragment == null) {
            fragment = createFragment();

            /**
             fragment事务被用来添加、移除、附加、分离或替换fragment队列中的fragment。这是使用
             fragment动态组装和重新组装用户界面的关键。 FragmentManager管理着fragment事务回退栈。

             add(...)方法是整个事务的核心，它有两个参数：容器视图资源ID和新创建的CrimeFragment。

             容器视图资源ID的作用有：
                1. 告诉FragmentManager， fragment视图应该出现在activity视图的什么位置；
                2. 唯一标识FragmentManager队列中的fragment。
                     如需从FragmentManager中获取CrimeFragment，使用容器视图资源ID就行了：
                     FragmentManager fm = getSupportFragmentManager();
                     Fragment fragment = fm.findFragmentById(R.id.fragment_container);
             */

            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    /**
     * 使用了@LayoutRes注解。这告诉Android Studio，任何时候，该实现方法都应该返回有效的布局资源ID。
     */
    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    protected abstract Fragment createFragment();
}
