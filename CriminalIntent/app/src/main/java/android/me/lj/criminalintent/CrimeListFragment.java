package android.me.lj.criminalintent;

import android.content.Intent;
import android.me.lj.criminalintent.utils.DateFormatUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

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

    private boolean mSubTitleVisible;
    private static final String SAVED_SUBTITLE_VISIBLE = "subtitle";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * Fragment.onCreateOptionsMenu(Menu, MenuInflater)方法是由FragmentManager负责调用的。
         * 因此，当activity接收到操作系统的onCreateOptionsMenu(...)方法回调请求时，我们必须明确告诉FragmentManager：
         *      其管理的fragment应接收onCreateOptionsMenu(...)方法的调用指令。要通知FragmentManager，需调用以下方法：
         *      public void setHasOptionsMenu(boolean hasMenu)
         */
        setHasOptionsMenu(true);
    }

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

        /**
         * 这里配合onSaveInstanceState(...)方法，解决屏幕旋转导致子标题消失的问题。
         */
        if (savedInstanceState != null) {
            mSubTitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubTitleVisible);
    }

    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.getInstance(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }

        // 更新子标题
        updateSubtitle();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        /**
         * 调用MenuInflater.inflate(int, Menu)方法并传入菜单文件的资源ID，将布局文件中定义的菜单项目填充到Menu实例中。
         */
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subTitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubTitleVisible) {
            subTitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subTitleItem.setTitle(R.string.show_subtitle);
        }
    }

    /**
     * 用户点击菜单中的菜单项时， fragment会收到onOptionsItemSelected(MenuItem)方法的回调请求。
     * 传入该方法的参数是一个描述用户选择的MenuItem实例。
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
         * 菜单通常包含多个菜单项。通过检查菜单项ID，可确定被选中的是哪个菜单项，然后作出相应的响应。
         * 这个ID实际就是在菜单定义文件中赋予菜单项的资源ID。
         */
        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.getInstance(getActivity()).addCrime(crime);
                Intent intent = CrimePagerActivity.newIntent(getActivity(), crime.getId());
                startActivity(intent);
                return true;

            case R.id.show_subtitle:
                mSubTitleVisible = !mSubTitleVisible;
                /**
                 * Declare that the options menu has changed, so should be recreated.
                 * The onCreateOptionsMenu(Menu)} method will be called the next
                 * time it needs to be displayed.
                 */
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;

            default:
                return super.onOptionsItemSelected(item);
            /**
             * onOptionsItemSelected(MenuItem)方法返回的是布尔值。
             * 一旦完成菜单项事件处理，该方法应返回true值以表明任务已完成。
             * 另外，默认case表达式中，如果菜单项ID不存在，超类版本方法会被调用。
             */
        }
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.getInstance(getActivity());
        int crimeCount = crimeLab.getCrimes().size();
        /**
         * getString(int resId, Object...formatArgs)方法接受字符串资源中占位符的替换值，updateSubtitle()用它生成子标题字符串。
         */
//        String subTitle = getString(R.string.subtitle_format, crimeCount);

//        if (crimeCount == 1 || crimeCount == 0) {
//            subTitle = subTitle.substring(0, subTitle.length() - 1);
//        }
        // 第十三章第七节：复数字符串资源
        String subTitle = getResources().getQuantityString(R.plurals.subtitle_plurals, crimeCount, crimeCount);

        if (!mSubTitleVisible) {
            subTitle = null;
        }

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subTitle);
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

            mDateTextView.setText(DateFormatUtil.format(mCrime.getDate()));
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

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }
    }
}
