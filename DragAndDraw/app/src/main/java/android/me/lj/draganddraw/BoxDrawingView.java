package android.me.lj.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/29.
 */

public class BoxDrawingView extends View {

    private static final String TAG = "BoxDrawingView";

    private Box mCurrentBox;
    private List<Box> mBoxen = new ArrayList<>();

    private Paint mBoxPaint;
    private Paint mBackgroundPaint;

    /**
     * 这里之所以添加了两个构造方法，是因为视图可从代码或者布局文件实例化。
     * 从布局文件中实例化的视图会收到一个AttributeSet实例，该实例包含了XML布局文件中指定的XML属性。
     * 即使不打算使用构造方法，按习惯做法也应添加这两个构造方法。
     */

    /**
     * 使用代码创建View时使用该构造方法
     */
    public BoxDrawingView(Context context) {
        this(context, null);
    }

    /**
     * 使用inflate从XML中创建View时使用该构造方法
     */
    public BoxDrawingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(0x22ff0000);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xfff8efe0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 填充背景色
         */
        canvas.drawPaint(mBackgroundPaint);

        for (Box box : mBoxen) {
            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);
            /**
             * 绘制矩形框
             */
            canvas.drawRect(left, top, right, bottom, mBoxPaint);
        }
    }

    /**
     * getRawX()和getRawY()获取的是相对屏幕左上角的坐标
     * getX()和getY()获取的是相对于当前View左上角的坐标
     *
     * Point和PointF的区别：Point使用int类型，PointF使用float类型
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        PointF current = new PointF(event.getX(), event.getY());

        String action = "";
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                /**
                 * 任何时候，只要接收到ACTION_DOWN动作事件，
                 * 就以事件原始坐标新建Box对象并赋值给mCurrentBox，然后再添加到矩形框数组中。
                 */
                mCurrentBox = new Box(current);
                mBoxen.add(mCurrentBox);
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                /**
                 * 用户手指在屏幕上移动时， mCurrentBox.mCurrent会得到更新。
                 */
                if (mCurrentBox != null) {
                    mCurrentBox.setCurrent(current);
                    /**
                     * invalidate()方法会强制BoxDrawingView重新绘制自己。
                     * 并再次调用onDraw(Canvas)方法
                     */
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                /**
                 * 在取消触摸事件或用户手指离开屏幕时，清空mCurrentBox以结束屏幕绘制。
                 */
                mCurrentBox = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                mCurrentBox = null;
                break;
        }
        Log.i(TAG, action + " at x=" + current.x + ", y=" + current.y);
        return true;
    }
}
