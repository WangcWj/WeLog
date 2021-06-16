package cn.wang.welog;

import android.util.Log;
import android.view.Choreographer;

import java.util.concurrent.TimeUnit;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/5/28
 */
public class FrameDroppedMonitor implements Choreographer.FrameCallback {

    public static long lastFrameTimeNanos = 0;
    public static long currentFrameTimeNanos = 0;
    public static final float deviceRefreshRateInMs = 16.6f;
    protected static int count = 0;

    @Override
    public void doFrame(long frameTimeNanos) {
        if (lastFrameTimeNanos == 0) {
            lastFrameTimeNanos = frameTimeNanos;
            Choreographer.getInstance().postFrameCallback(this);
            return;
        }
        currentFrameTimeNanos = frameTimeNanos;
        //毫秒
        long value = (currentFrameTimeNanos - lastFrameTimeNanos) / 1000000;
        Log.e("cc.wang","FrameDroppedMonitor.doFrame.value "+value);
        if (value > deviceRefreshRateInMs) {
            int i = droppedCount(lastFrameTimeNanos, currentFrameTimeNanos, deviceRefreshRateInMs);
            Log.e("cc.wang", "MainActivity.doFrame.droopedCount" + i);
        }
        lastFrameTimeNanos = currentFrameTimeNanos;
        if(count <= 15) {
            try {
                Thread.sleep(170);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        count++;
        if(count >= 50){
            return;
        }
        Choreographer.getInstance().postFrameCallback(this);
    }

    public static int droppedCount(long start, long end, float devRef) {
        int count = 0;
        long diffNs = end - start;
        long diffMs = TimeUnit.MILLISECONDS.convert(diffNs, TimeUnit.NANOSECONDS);
        long dev = Math.round(devRef);
        if (diffMs > dev) {
            long droppedCount = (diffMs / dev);
            count = (int) droppedCount;
        }
        return count;
    }
}
