package android.me.lj.beatbox;

import android.databinding.DataBindingUtil;
import android.me.lj.beatbox.databinding.FragmentBeatBoxBinding;
import android.me.lj.beatbox.databinding.ListItemSoundBinding;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Administrator on 2018/3/13.
 */

public class BeatBoxFragment extends Fragment {

    private BeatBox mBeatBox;

    public static BeatBoxFragment newInstance() {
        return new BeatBoxFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * 为了应对设备配置变化， fragment有一个特殊方法可确保BeatBox实例不被销毁，这个方法就是retainInstance。
         *
         * fragment的retainInstance属性值默认为false，这表明其不会被保留。
         * 因此，设备旋转时fragment会随托管activity一起被销毁并重建。
         * 调用setRetainInstance(true)方法可保留fragment。
         * 已保留的fragment不会随activity一起被销毁。
         * 相反，它会一直保留，并在需要时原封不动地转给新的activity。
         *
         * 对于已保留的fragment实例，其全部实例变量（如mBeatBox）的值也会保持不变，因此可放心继续使用。
         */
        setRetainInstance(true);

        mBeatBox = new BeatBox(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /**
         * fragment_beat_box.xml已经有了一个叫FragmentBeatBoxBinding的绑定类。
         * 这就是要用来做数据绑定的类：现在，实例化视图层级结构时，不再使用LayoutInflater，而是实例化FragmentBeatBoxBinding类。
         * 在一个叫作getRoot()的getter方法里， FragmentBeatBoxBinding引用着布局视图结构，而且也会引用那些在布局文件里以android:id标签引用的其他视图。
         *
         * FragmentBeatBoxBinding类有两个引用： getRoot()和recyclerView，前者指整个布局，后者指RecyclerView.
         * 布局只有一个视图，所以两个引用都指向了同一个视图： RecyclerView。
         */

        FragmentBeatBoxBinding binding = DataBindingUtil.inflate(inflater,
                R.layout.fragment_beat_box, container, false);

        binding.recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        binding.recyclerView.setAdapter(new SoundAdapter(mBeatBox.getSounds()));

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBeatBox.release();
    }

    private class SoundHolder extends RecyclerView.ViewHolder {

        private ListItemSoundBinding mBinding;

        public SoundHolder(ListItemSoundBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
            mBinding.setViewModel(new SoundViewModel(mBeatBox));
        }

        public void bind(Sound sound) {
            mBinding.getViewModel().setSound(sound);
            mBinding.executePendingBindings();
        }
    }

    private class SoundAdapter extends RecyclerView.Adapter<SoundHolder> {

        private List<Sound> mSounds;

        public SoundAdapter(List<Sound> sounds) {
            mSounds = sounds;
        }

        @NonNull
        @Override
        public SoundHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            ListItemSoundBinding binding = DataBindingUtil.inflate(inflater, R.layout.list_item_sound, parent, false);

            return new SoundHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull SoundHolder holder, int position) {
            Sound sound = mSounds.get(position);
            holder.bind(sound);
        }

        @Override
        public int getItemCount() {
            return mSounds.size();
        }
    }
}
