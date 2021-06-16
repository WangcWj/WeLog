package com.wang.monitor.fps.core;

import android.os.Looper;
import java.lang.reflect.Method;
import com.wang.monitor.fps.interfaces.FpsFrameCallback;
import com.wang.monitor.fps.monitors.FpsFramesMonitor;
import com.wang.monitor.fps.monitors.LooperMonitor;
import com.wang.monitor.fps.monitors.MainThreadBlockMonitor;
import cn.wang.log.core.WeLog;

/**
 * Created to :
 *
 * @author WANG
 * @date 2021/6/10
 */
public class AppMonitor implements Runnable {

    private AppMonitor() {

    }

    private static class Instance {
        private static AppMonitor appMonitor = new AppMonitor();
    }

    public static AppMonitor getInstance() {
        return Instance.appMonitor;
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
        mLoopMonitor.addLoopListener(new MainThreadBlockMonitor(1000,50));
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
        WeLog.d("AppMonitor-> onStart");
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
            WeLog.d("AppMonitor-> loopEnd");
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
            WeLog.e("AppMonitor.addFrameCallback" + e);
        }
    }

}
