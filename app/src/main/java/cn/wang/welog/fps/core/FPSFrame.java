package cn.wang.welog.fps.core;

import android.os.Looper;
import android.util.Log;
import java.lang.reflect.Method;
import cn.wang.welog.fps.interfaces.FpsFrameCallback;
import cn.wang.welog.fps.monitors.FpsFramesMonitor;
import cn.wang.welog.fps.monitors.LooperMonitor;

/**
 * Created to :
 *
 * @author WANG
 * @date 2021/6/10
 */
public class FPSFrame implements Runnable {

    private FPSFrame() {

    }

    private static class Instance {
        private static FPSFrame fpsFrame = new FPSFrame();
    }

    public static FPSFrame getInstance() {
        return Instance.fpsFrame;
    }

    private ReflectChoreographer mReChoreographer;
    private LooperMonitor mLoopMonitor;
    private boolean mHaveInit = false;
    private boolean isVSyncFrame = false;
    private long mLooperStartTime = 0L;
    private FpsFrameCallback mFpsFrameCallback;

    public boolean init() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            return false;
        }
        if (mHaveInit) {
            return mHaveInit;
        }
        mReChoreographer = new ReflectChoreographer();
        mReChoreographer.init();
        mLoopMonitor = new LooperMonitor();
        mLoopMonitor.init(Looper.getMainLooper());
        mFpsFrameCallback = new FpsFramesMonitor();
        mLoopMonitor.addLoopListener(new LooperMonitor.LooperCallback() {

            @Override
            public void dispatchLoopStart() {
                getInstance().loopStart();
            }

            @Override
            public void dispatchLoopEnd() {
                getInstance().loopEnd();
            }

        });
        mHaveInit = true;
        return true;
    }

    public void onStart() {
        if (!mHaveInit) {
            return;
        }
        addFrameCallback(FpsConstants.CALLBACK_TRAVERSAL, this);
    }

    private void doFrameBegin() {
        this.isVSyncFrame = true;
    }

    @Override
    public void run() {
        doFrameBegin();
    }

    private void loopStart() {
        //
        mLooperStartTime = System.nanoTime();
    }

    private void loopEnd() {
        long endTime = System.nanoTime();
        if (isVSyncFrame) {
            Log.e("WANG", "FPSFrame.loopEnd");
            addFrameCallback(FpsConstants.CALLBACK_TRAVERSAL, this);
            if (null != mFpsFrameCallback) {
                mFpsFrameCallback.doFrame(endTime, mReChoreographer.getIntendedFrameTimeNs(mLooperStartTime), mReChoreographer.frameIntervalNanos);
            }
        }
        isVSyncFrame = false;
    }

    private void addFrameCallback(int type, Runnable runnable) {
        if (!mHaveInit) {
            return;
        }
        try {
            synchronized (mReChoreographer.callbackQueueLock) {
                Method method = mReChoreographer.addTraversalCallback;
                if (null != method) {
                    method.invoke(mReChoreographer.addTraversalQueue[type], -1L, runnable, null);
                }
            }
        } catch (Exception e) {
            Log.e("WANG", "FPSFrame.addFrameCallback" + e);
        }
    }

}
