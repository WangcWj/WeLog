package com.wang.monitor.fps.monitors;

import android.util.Log;
import com.wang.monitor.fps.interfaces.FpsFrameCallback;

import cn.wang.log.core.WeLog;

/**
 * Created to :
 *
 * @author WANG
 * @date 2021/6/14
 */
public class FpsFramesMonitor implements FpsFrameCallback {

    private final long skippedFramesThreshold = 24;

    @Override
    public void doFrame(long endTime, long intendedFrameTimeNs, long frameIntervalNanos) {
        long jitterNanos = endTime - intendedFrameTimeNs;
        if (jitterNanos >= frameIntervalNanos) {
            long skippedFrames = jitterNanos / frameIntervalNanos;
            WeLog.e("AppMonitor.skippedFrames" + skippedFrames);
            if (skippedFrames >= skippedFramesThreshold) {

            }
        }
    }
}
