package android.me.lj.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Created by Administrator on 2018/3/5 0005.
 */

public class CrimeListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new CrimeFragment();
    }
}
