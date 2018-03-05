package android.me.lj.criminalintent;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Administrator on 2018/3/5 0005.
 */

public class CrimeListFragment extends Fragment {

    private RecyclerView mCrimeRecyclerView;


    private class CrimeHolder extends RecyclerView.ViewHolder {



        public CrimeHolder(View itemView) {
            super(itemView);
        }
    }
}
