package android.me.lj.beatbox;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

/**
 * Created by Administrator on 2018/3/14.
 */

/**
 * 视图模型实现数据绑定的Observable接口。
 * 这个接口可以让绑定类在视图模型上设置监听器。
 * 这样，只要视图模型有变化，绑定类立即回接到回调。
 */
public class SoundViewModel extends BaseObservable{

    private Sound mSound;
    private BeatBox mBeatBox;

    public SoundViewModel(BeatBox beatBox) {
        mBeatBox = beatBox;
    }

    /**
     * 使用@Bindable注解视图模型里可绑定的属性
     */
    @Bindable
    public String getTitle() {
        return mSound.getName();
    }

    public Sound getSound() {
        return mSound;
    }

    public void setSound(Sound sound) {
        mSound = sound;
        /**
         * 每次可绑定的属性值改变时，就调用notifyChange()方法或notifyPropertyChanged(int)方法。
         * 就是通知绑定类，视图模型对象上所有可绑定属性都已更新。
         */
        notifyChange();
    }
}
