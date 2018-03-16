package android.me.lj.beatbox;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/13.
 */

public class BeatBox {

    private static final String TAG = "BeatBox";
    private static final String SOUNDS_FOLDER = "sample_sounds";

    private static final int MAX_SOUNDS = 5;

    private AssetManager mAssets;
    private List<Sound> mSounds = new ArrayList<>();

    private SoundPool mSoundPool;

    public BeatBox(Context context) {
        mAssets = context.getAssets();
        /**
         * 第一个参数指定同时播放多少个音频。这里指定了5个。
         * 已经播放了5个音频时，如果尝试再播第6个， SoundPool会停止播放原来的音频。
         *
         * 第二个参数确定音频流类型。 Android有很多不同的音频流，它们都有各自独立的音量控制选项。
         * 这就是调低音乐音量，闹钟音量却不受影响的原因。
         * 打开开发者文档，先找到AudioManager类中以AUDIO打头的常量，再看看其他控制选项。
         * STREAM_MUSIC是音乐和游戏常用的音量控制常量。
         *
         * 最后一个参数指定采样率转换品质。参考文档说这个参数不起作用，所以这里传入0。
         */
        mSoundPool = new SoundPool(MAX_SOUNDS, AudioManager.STREAM_MUSIC, 0);
        loadSounds();
    }

    public void play(Sound sound) {
        Integer soundId = sound.getSoundId();
        /**
         * 播放前，要检查并确保soundId不是null值。
         * Sound加载失败会出现null值的情况。
         * 检查通过后，就可以调用SoundPool.play(int, float, float, int, int, float)方法播放音频了。
         */
        if (soundId == null) {
            return;
        }
        /**
         * 这些参数依次是：音频ID、左音量、右音量、优先级（无效）、是否循环以及播放速率。
         * 我们需要最大音量和常速播放，所以传入值1.0。
         * 是否循环参数传入0，代表不循环。如果想无限循环，可以传入-1。
         */
        mSoundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    /**
     * 音频播放完毕，应调用SoundPool.release()方法释放SoundPool。
     */
    public void release() {
        mSoundPool.release();
    }

    private void loadSounds() {
        String[] soundNames;
        try {
            /**
             * AssetManager.list(String)方法能列出指定目录下的所有文件名。
             */
            soundNames = mAssets.list(SOUNDS_FOLDER);
            Log.i(TAG, "Found " + soundNames.length + " sounds");
        } catch (IOException ioe) {
            Log.e(TAG, "Could not list assets", ioe);
            return;
        }

        for (String filename : soundNames) {
            try {
                String assetPath = SOUNDS_FOLDER + "/" + filename;
                Sound sound = new Sound(assetPath);
                load(sound);
                mSounds.add(sound);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Could not load sound " + filename, e);
            }
        }
    }

    private void load(Sound sound) throws IOException {
        AssetFileDescriptor afd = mAssets.openFd(sound.getAssetPath());
        /**
         * 使用SoundPool加载音频文件。
         * 相比其他音频播放方法， SoundPool还有个快速响应的优势：指令刚一发出，它就立即开始播放，一点都不拖沓。
         * 不过反应快也要付出代价，那就是在播放前必须预先加载音频。 SoundPool加载的音频文件都有自己的Integer型ID。
         *
         * 调用mSoundPool.load(AssetFileDescriptor, int)方法可以把文件载入SoundPool待播。
         * 为了方便管理、重播或卸载音频文件， mSoundPool.load(...)方法会返回一个int型ID。这实际就是存储在mSoundId中的ID。
         */
        int soundId = mSoundPool.load(afd, 1);
        sound.setSoundId(soundId);
    }

    public List<Sound> getSounds() {
        return mSounds;
    }
}
