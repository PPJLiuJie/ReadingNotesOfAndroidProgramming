package me.android.liujie.nerdlauncher;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Administrator on 2018/3/18 0018.
 */

public class NerdLauncherFragment extends Fragment {
    private static final String TAG = "NerdLauncherFragment";
    private RecyclerView mRecyclerView;

    public static NerdLauncherFragment newInstance() {
        return new NerdLauncherFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nerd_launcher, container, false);
        mRecyclerView = view.findViewById(R.id.app_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();

        return view;
    }

    private void setupAdapter() {

        /**
         * 创建一个隐式intent并从PackageManager那里获取匹配它的所有activity。
         */
        Intent startupIntent = new Intent(Intent.ACTION_MAIN);
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        /**
         * 在CriminalIntent应用中，为使用隐式intent发送crime报告，我们先创建隐式intent，再将其封装在选择器intent中，最后调用startActivity(Intent)方法发送给操作系统：
         * Intent i = new Intent(Intent.ACTION_SEND);
         * ... // Create and put intent extras
         * i = Intent.createChooser(i, getString(R.string.send_report));
         * startActivity(i);
         *
         * 这里没有使用上述处理方式，是不是很费解？
         * 原因很简单： MAIN/LAUNCHER intent过滤器(AndroidManifest.xml中activity结点的<intent-filter>结点)可能无法与通过startActivity(...)方法发送的MAIN/LAUNCHER隐式intent相匹配。
         * 事实上， startActivity(Intent)方法意味着“启动匹配隐式intent的默认activity”，而不是想当然的“启动匹配隐式intent的activity”。
         * 调用startActivity(Intent)方法（或startActivity-ForResult(...)方法）发送隐式intent时，操作系统会悄悄为目标intent添加Intent.CATEGORY_DEFAULT类别。
         *
         * 因此，如果希望intent过滤器匹配startActivity(...)方法发送的隐式intent，就必须在对应的intent过滤器中包含DEFAULT类别。
         * 定义了MAIN/LAUNCHER intent过滤器的activity是应用的主要入口点。
         * 它只负责做好作为应用主要入口点要处理的工作。
         * 它通常不关心自己是否为默认的主要入口点，所以可以不包含CATEGORY_DEFAULT类别。
         *
         * MAIN/LAUNCHER intent过滤器并不一定包含CATEGORY_DEFAULT类别，因此不能保证可以与startActivity(...)方法发送的隐式intent匹配。
         * 所以，我们转而使用intent直接向PackageManager查询带有MAIN/LAUNCHER intent过滤器的activity。
         */


        /**
         * 使用PackageManager获取所有可启动主activity。
         * 可启动主activity都带有包含MAIN操作和LAUNCHER类别的intent过滤器。
         */
        final PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(startupIntent, 0);
        /**
         * 对ResolveInfo对象中的activity标签按首字母排序
         */
        Collections.sort(activities, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo a, ResolveInfo b) {
                return String.CASE_INSENSITIVE_ORDER.compare(
                        a.loadLabel(pm).toString(),
                        b.loadLabel(pm).toString());
            }
        });

        Log.i(TAG, "Found " + activities.size() + " activities.");

        mRecyclerView.setAdapter(new ActivityAdapter(activities));
    }

    private class ActivityHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ResolveInfo mResolveInfo;
        private TextView mNameTextView;

        public ActivityHolder(View itemView) {
            super(itemView);
            mNameTextView = (TextView) itemView;
            mNameTextView.setOnClickListener(this);
        }

        public void bindActivity(ResolveInfo resolveInfo) {
            mResolveInfo = resolveInfo;
            PackageManager pm = getActivity().getPackageManager();
            String appName = mResolveInfo.loadLabel(pm).toString();
            mNameTextView.setText(appName);
        }

        @Override
        public void onClick(View view) {
            /**
             * 要创建启动activity的显式intent，需要从ResolveInfo对象中获取activity的包名与类名。
             */
            ActivityInfo activityInfo = mResolveInfo.activityInfo;
            /**
             * 使用包名和类名创建显式intent
             * 作为显式intent的一部分，我们还发送了ACTION_MAIN操作。
             */
            Intent intent = new Intent(Intent.ACTION_MAIN)
                    .setClassName(activityInfo.applicationInfo.packageName, activityInfo.name)
                    // 为了在启动新activity时启动新任务，需要为intent添加一个标志:FLAG_ACTIVITY_NEW_TASK
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityHolder> {

        private final List<ResolveInfo> mActivitys;

        public ActivityAdapter(List<ResolveInfo> activitys) {
            mActivitys = activitys;
        }

        @Override
        public ActivityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ActivityHolder(view);
        }

        @Override
        public void onBindViewHolder(ActivityHolder holder, int position) {
            holder.bindActivity(mActivitys.get(position));
        }

        @Override
        public int getItemCount() {
            return mActivitys.size();
        }
    }
}
