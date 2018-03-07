package android.me.lj.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by Administrator on 2018/3/5 0005.
 */

public class CrimeListFragment extends Fragment {

    /**
     * RecyclerView的任务仅限于回收和定位屏幕上的View
     * RecyclerView 自身不会创建视图，视图来自于ViewHolder，ViewHolder 引用着itemView
     * RecyclerView自己不创建ViewHolder。这个任务实际是由Adapter来完成的。
     */

    /**
     * RecyclerView需要显示视图对象时，就会去找它的Adapter。
     *
     * 首先，调用Adapter的getItemCount()方法， RecyclerView询问数组列表中包含多少个对象。
     *
     * 接着， RecyclerView调用Adapter的onCreateViewHolder(ViewGroup, int)方法创建ViewHolder及其要显示的视图。
     *
     * 最后， RecyclerView会传入ViewHolder及其位置，调用onBindViewHolder(ViewHolder, int)方法。Adapter会找到目标位置的数据并将其绑定到ViewHolder的视图上。
     *
     * 整个过程执行完毕， RecyclerView就能在屏幕上显示crime列表项了。
     * 需要注意的是，相对于onBindViewHolder(ViewHolder, int)方法， onCreateViewHolder(ViewGroup, int)方法的调用并不频繁。
     * 一旦有了够用的ViewHolder， RecyclerView就会停止调用onCreateViewHolder(...)方法。随后，它会回收利用旧的ViewHolder以节约时间和内存。
     */

    private RecyclerView mCrimeRecyclerView;

    private CrimeAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = view.findViewById(R.id.crime_recycler_view);

        /**
         * 没有LayoutManager的支持，不仅RecyclerView无法工作，还会导致应用崩溃。
         * 所以，RecyclerView视图创建完成后，就立即转交给了LayoutManager对象。
         * RecyclerView 类不会亲自摆放屏幕上的列表项。实际上，摆放的任务被委托给了LayoutManager。
         * 除了在屏幕上摆放列表项，LayoutManager还负责定义屏幕滚动行为。因此，没有LayoutManager，RecyclerView也就没法正常工作。
         */
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.getInstance(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }

    }

    /**
     * ViewHolder只做一件事：容纳View视图
     */
    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Crime mCrime;

        private TextView mTitleTextView;
        private TextView mDateTextView;
        private ImageView mSolvedImageView;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_crime, parent, false));
            itemView.setOnClickListener(this);

            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mSolvedImageView = itemView.findViewById(R.id.imageView);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss EEEE");
            // 设置时区为东八区
            format.setTimeZone(TimeZone.getTimeZone("GMT+8"));
            String dateStr = format.format(mCrime.getDate());

            mDateTextView.setText(dateStr);
            mSolvedImageView.setVisibility(mCrime.isSolved() ? View.VISIBLE : View.INVISIBLE);
        }

        @Override
        public void onClick(View view) {
            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
            // 从 fragment 中 启 动 activity 类 似 于 从 activity 中 启 动 activity
            startActivity(intent);
        }
    }

    /**
     * Adapter是一个控制器对象，从模型层获取数据，然后提供给RecyclerView显示，是沟通的桥梁。
     * Adapter负责：
     * 1.创建必要的ViewHolder
     * 2.绑定ViewHolder至模型层数据
     */
    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @NonNull
        @Override
        public CrimeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CrimeHolder(LayoutInflater.from(getActivity()), parent);
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            holder.bind(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }
    }
}
