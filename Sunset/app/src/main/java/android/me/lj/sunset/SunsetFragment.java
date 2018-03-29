package android.me.lj.sunset;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by Administrator on 2018/3/29.
 */

public class SunsetFragment extends Fragment {

    private View mSceneView;
    private View mSunView;
    private View mSkyView;

    private int mBlueSkyColor;
    private int mSunsetSkyColor;
    private int mNightSkyColor;

    public static SunsetFragment newInstance() {
        return new SunsetFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sunset, container, false);

        mSceneView = view;
        mSunView = view.findViewById(R.id.sun);
        mSkyView = view.findViewById(R.id.sky);

        Resources resources = getResources();
        mBlueSkyColor = resources.getColor(R.color.blue_sky);
        mSunsetSkyColor = resources.getColor(R.color.sunset_sky);
        mNightSkyColor = resources.getColor(R.color.night_sky);

        mSceneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAnimation();
            }
        });

        return view;
    }

    private void startAnimation() {

        /**
         * View视图类的getTop、getBottom、getLeft、getRight方法获取的是其相对于父视图的位置
         */
        float sunYStart = mSunView.getTop();
        /**
         * mSkyView的高度，即为mSkyView的底部位置
         */
        float sunYEnd = mSkyView.getHeight();

        /**
         * ObjectAnimator是个属性动画制作对象。
         * 要获得某种动画效果，传统方式是设法在屏幕上移动视图，而属性动画制作对象却另辟蹊径：
                 以一组不同的参数值反复调用属性设置方法。
                 ObjectAnimator.ofFloat(mSunView, "y", 0, 1)
                 新建ObjectAnimator一旦启动，就会以从0开始递增的参数值反复调用mSunView.
                 setY(float)方法：
                 mSunView.setY(0);
                 mSunView.setY(0.02);
                 mSunView.setY(0.04);
                 mSunView.setY(0.06);
                 mSunView.setY(0.08);
                 ...
                 直到调用mSunView.setY(1)为止。
         */
        ObjectAnimator heightAnimator = ObjectAnimator
                .ofFloat(mSunView, "y", sunYStart, sunYEnd)
                .setDuration(3000);

        /**
         * 假设太阳一开始静止于天空，在进入落下的动画时，应该有个加速过程。
         * 使用一个AccelerateInterpolator对象实现太阳加速落下的特效。
         */
        heightAnimator.setInterpolator(new AccelerateInterpolator());

        ObjectAnimator sunsetSkyAnimator = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", mBlueSkyColor, mSunsetSkyColor)
                .setDuration(3000);
        /**
         * 颜色int数值并不是个简单的数字。它实际是由四个较小数字转换而来。
         * 因此，只有知道颜色的组成奥秘， ObjectAnimator对象才能合理地确定蓝色和橘黄色之间的中间值。
         *
         * TypeEvaluator能帮助ObjectAnimator对象精确地计算开始到结束间的递增值。
         * Android提供的这个TypeEvaluator子类叫作ArgbEvaluator，
         */
        sunsetSkyAnimator.setEvaluator(new ArgbEvaluator());

        ObjectAnimator nightSkyAnimator = ObjectAnimator
                .ofInt(mSkyView, "backgroundColor", mSunsetSkyColor, mNightSkyColor)
                .setDuration(1500);
        nightSkyAnimator.setEvaluator(new ArgbEvaluator());

        /**
         * AnimatorSet就是可以放在一起执行的动画集。
         * play和with表示同时执行heightAnimator和sunsetSkyAnimator，
         * before表示在nightSkyAnimator之前执行heightAnimator
         *
         * 在实际开发中，可能会用到更复杂的动画集。这也没问题，需要的话，可以多次调用play(Animator)方法。
         */
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .play(heightAnimator)
                .with(sunsetSkyAnimator)
                .before(nightSkyAnimator);
        animatorSet.start();
    }
}
