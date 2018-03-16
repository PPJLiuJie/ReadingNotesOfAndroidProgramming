package android.me.lj.beatbox;

/**
 * Created by Administrator on 2018/3/13 0013.
 */

public class Sound {

    private String mAssetPath;
    private String mName;
    private Integer mSoundId;

    public Sound(String assetPath) {
        mAssetPath = assetPath;
        /**
         * 为了有效显示声音文件名，在构造方法中对其做一下处理。
         * 首先使用String.split(String)方法分离出文件名，再使用String.replace(String, String)方法删除.wav后缀。
         */
        String components[] = assetPath.split("/");
        String filename = components[components.length - 1];
        mName = filename.replace(".wav", "");
    }

    public String getAssetPath() {
        return mAssetPath;
    }

    public String getName() {
        return mName;
    }

    public Integer getSoundId() {
        return mSoundId;
    }

    public void setSoundId(Integer soundId) {
        mSoundId = soundId;
    }
}
