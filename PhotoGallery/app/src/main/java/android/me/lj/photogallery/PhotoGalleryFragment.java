package android.me.lj.photogallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/20.
 */

public class PhotoGalleryFragment extends VisibleFragment {

    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    /**
     * ThumbnailDownloader的泛型参数支持任何对象，
     * 但在这里， PhotoHolder最合适，因为该视图是最终显示下载图片的地方。
     */
    private ThumbnailDownload<PhotoHolder> mThumbnailDownload;

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

        /**
         * 调用setHasOptionsMenu(true)方法让fragment接收菜单回调方法
         */
        setHasOptionsMenu(true);

        updateItems();

        /**
         * Handler默认与当前线程的Looper相关联。
         * 这个Handler是在onCreate(...)方法中创建的，所以它会与主线程的Looper相关联。
         */
        Handler responseHandler = new Handler();

        /**
         * ThumbnailDownloader的getLooper()方法是在start()方法之后调用的。
         * 这能保证线程就绪，避免潜在竞争（尽管极少发生）。
         * 因为getLooper()方法能执行成功，说明onLooperPrepared()方法肯定早已完成。
         * 这样，queueThumbnail()方法因Handler为空而调用失败的情况就能避免了。
         */

        mThumbnailDownload = new ThumbnailDownload<>(responseHandler);
        mThumbnailDownload.setThumbnailDownloadListener(new ThumbnailDownload.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                photoHolder.bindDrawable(drawable);
            }
        });
        mThumbnailDownload.start();
        mThumbnailDownload.getLooper();
        Log.i(TAG, "Background thread started");
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        /**
         * 如果用户旋转屏幕，因PhotoHolder视图的失效， ThumbnailDownloader可能会挂起。
         * 如果点击这些ImageView，就会发生异常。
         * 既然视图已销毁，应该清空mThumbnailDownloader。
         */
        mThumbnailDownload.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /**
         * 在onDestroy()方法内调用quit()方法结束线程。这非常关键。
         * 如不终止HandlerThread，它会一直运行下去，成为僵尸。
         */
        mThumbnailDownload.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "QueryTextSubmit: " + query);
                updateItems();
                QueryPreferences.setStoredQuery(getActivity(), query);
                /**
                 * 请求受理后，返回true
                 */
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "QueryTextChange: " + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 用户点击搜索按钮展开SearchView时，搜索文本框显示已保存的查询字符串。
                 */
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();
                return true;

            case R.id.menu_item_toggle_polling:
                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                /**
                 * 让GalleryActivity刷新工具栏菜单选项
                 */
                getActivity().invalidateOptionsMenu();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
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

    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);
            mItemImageView = itemView.findViewById(R.id.item_image_view);
            itemView.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
        }

        @Override
        public void onClick(View v) {
            Intent i = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoPageUri());
            startActivity(i);
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
            holder.bindGalleryItem(galleryItem);
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(placeholder);
            mThumbnailDownload.queueThumbnail(holder, galleryItem.getUrl());
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void,Void,List<GalleryItem>> {

        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            if (mQuery == null) {
                return new FlickrFetchr().fetchRecentPhotos();
            } else {
                return new FlickrFetchr().searchPhotos(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> items) {
            mItems = items;
            setupAdapter();
        }
    }
}
