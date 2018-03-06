package android.me.lj.criminalintent;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2018/3/5 0005.
 */

public class CrimeLab {

    /**
     * 应用能在内存里活多久，单例就能活多久。因此将对象列表保存在单例里的话，就能随时获
     * 取crime数据，不管activity和fragment的生命周期怎么变化。
     * 虽然CrimeLab单例不是数据持久保存的好方案，
     * 但它确实能保证仅拥有一份crime数据，并且能让控制器层类间的数据传递更容易。
     */

    private List<Crime> mCrimes;
    private static CrimeLab sCrimeLab;

    private CrimeLab(Context context) {
        mCrimes = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Crime crime = new Crime();
            crime.setTitle("Crime #" + i);
            crime.setSolved(i % 2 == 0);
            mCrimes.add(crime);
        }
    }

    public static CrimeLab getInstance(Context context) {
        if (sCrimeLab == null) {
            sCrimeLab = new CrimeLab(context);
        }
        return sCrimeLab;
    }

    public List<Crime> getCrimes() {
        return mCrimes;
    }

    public Crime getCrime(UUID id) {
        for (Crime crime : mCrimes) {
            if (crime.getId().equals(id)) {
                return crime;
            }
        }
        return null;
    }

}
