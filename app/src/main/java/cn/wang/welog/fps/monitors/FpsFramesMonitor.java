package cn.wang.welog.fps.monitors;

import android.util.Log;
import cn.wang.welog.fps.interfaces.FpsFrameCallback;

/**
 * Created to :
 *
 * @author WANG
 * @date 2021/6/14
 */
public class FpsFramesMonitor implements FpsFrameCallback {

    private final long skippedFramesThreshold = 30;

    @Override
    public void doFrame(long endTime, long intendedFrameTimeNs, long frameIntervalNanos) {
        long jitterNanos = endTime - intendedFrameTimeNs;
        if (jitterNanos >= frameIntervalNanos) {
            long skippedFrames = jitterNanos / frameIntervalNanos;
            Log.e("WANG", "FPSFrame.skippedFrames" + skippedFrames);
            if (skippedFrames >= skippedFramesThreshold) {
                String stackTraceString = Log.getStackTraceString(new Throwable());
                Log.e("WANG","FpsFramesMonitor.skippedFrames:"+skippedFrames+"  log  is  "+stackTraceString);
            }
        }
    }
}
