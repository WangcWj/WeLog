package com.wang.monitor.monitors;

import com.wang.monitor.core.AppMonitor;
import com.wang.monitor.core.MonitorConstants;
import com.wang.monitor.core.ReflectChoreographer;
import com.wang.monitor.interfaces.FpsFrameCallback;
import com.wang.monitor.interfaces.Monitor;

import java.lang.reflect.Method;

import cn.wang.log.core.WeLog;

/**
 * Created to : 掉帧检测。
 *
 * @author WANG
 * @date 2021/6/14
 */
public class FpsFramesMonitor extends LooperMonitor.LooperCallback implements FpsFrameCallback, Runnable, Monitor {

    private final long skippedFramesThreshold = 24;
    private long mLooperStartTime = 0L;
    private boolean isVSyncFrame = false;
    private ReflectChoreographer mReChoreographer;

    public FpsFramesMonitor(ReflectChoreographer mReChoreographer) {
        this.mReChoreographer = mReChoreographer;
    }

    @Override
    public void doFrame(long endTime, long intendedFrameTimeNs, long frameIntervalNanos) {
        /*if(!AppMonitor.getInstance().isForeground()){
            return;
        }*/
        long jitterNanos = endTime - intendedFrameTimeNs;
        if (jitterNanos >= frameIntervalNanos) {
            long skippedFrames = jitterNanos / frameIntervalNanos;
            if (skippedFrames >= skippedFramesThreshold) {
                WeLog.d("skippedFrames: " + skippedFrames);
            }
        }
    }

    @Override
    public void monitorStart() {
        addFrameCallback(MonitorConstants.CALLBACK_TRAVERSAL, this);
    }

    @Override
    public void monitorPause() {
        isVSyncFrame = false;
    }

    @Override
    public void monitorResume() {

    }

    @Override
    public void monitorQuit() {

    }

    @Override
    public void dispatchLoopStart() {
        handlerStart();
    }

    private void handlerStart() {
        //
        mLooperStartTime = System.nanoTime();
    }

    @Override
    public void dispatchLoopEnd() {
        handlerEnd();
    }

    @Override
    public int callbackID() {
        return 0;
    }

    @Override
    public void run() {
        doFrameBegin();
    }

    /**
     *
     */
    private void doFrameBegin() {
        this.isVSyncFrame = true;
    }

    private void handlerEnd() {
        long endTime = System.nanoTime();
        boolean current = isVSyncFrame;
        isVSyncFrame = false;
        if (current) {
            WeLog.d("addFrameCallback");
            addFrameCallback(MonitorConstants.CALLBACK_TRAVERSAL, this);
            doFrame(endTime, mReChoreographer.getIntendedFrameTimeNs(mLooperStartTime), mReChoreographer.frameIntervalNanos);
        }
    }

    private void addFrameCallback(int type, Runnable runnable) {
        if (null == mReChoreographer) {
            return;
        }
        try {
            synchronized (mReChoreographer.getCallbackQueueLock()) {
                Method method = mReChoreographer.getAddTraversalCallback();
                if (null != method) {
                    method.invoke(mReChoreographer.getAddTraversalQueue()[type], -1L, runnable, null);
                }
            }
        } catch (Exception e) {
            WeLog.e("AppMonitor.addFrameCallback" + e);
        }
    }
}
