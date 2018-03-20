package android.me.lj.photogallery;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/20.
 */

public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";
    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 我们保留了fragment。
         * 这样即使设备旋转，也不会重复创建新的AsyncTask去获取JSON数据。
         */
        setRetainInstance(true);
        new FetchItemsTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView = view.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        /**
         * setupAdapter()方法会自动配置RecyclerView的adapter。
         * 应在onCreateView(...)方法中调用该方法，这样每次因设备旋转重新生成RecyclerView时，可重新为其配置对应的adapter。
         * 另外，每次模型层对象发生变化时，也应及时调用该方法。
         */
        setupAdapter();

        return view;
    }

    private void setupAdapter() {
        /**
         * 配置adapter前，应检查isAdded()的返回值是否为true。
         * 该检查确认fragment已与目标activity相关联，从而保证getActivity()方法返回结果非空。
         *
         * fragment可脱离任何activity而独立存在。在这之前，所有的方法调用都是由系统框架的回调方法驱动的，所以不会出现这种情况。
         * 本例中，如果fragment在接收回调指令，则它必然关联着某个activity；如它单独存在，也就不会收到回调。
         *
         * 既然在用AsyncTask，说明正在从后台进程触发回调指令。
         * 因而不能确定fragment是否关联着activity。
         * 那就必须确认fragment是否仍与activity关联。
         * 如果没有关联，依赖于activity的操作（如创建PhotoAdapter，进而还会使用托管activity作为context来创建TextView）就会失败。
         * 所以，设置adapter之前，你需要确认isAdded()方法返回值。
         */
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = itemView.findViewById(R.id.item_image_view);
        }

        public void bindGalleryItem(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_gallery, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            Drawable plackholder = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindGalleryItem(plackholder);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>> {
        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            return new FlickrFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }
}
