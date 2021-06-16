package com.wang.monitor.fps.monitors;

import android.os.Looper;
import android.util.LongSparseArray;
import com.wang.monitor.fps.core.FpsConstants;
import cn.wang.log.core.WeLog;

/**
 * Created to : 主线程Handler卡顿检测。
 *
 * @author WANG
 * @date 2021/6/16
 */
public class MainThreadBlockMonitor extends LooperMonitor.LooperCallback implements Runnable {

    private long loopStartTime = 0L;
    private IoThread ioThread;
    protected boolean sample = false;
    private int blockThreadMillis;
    private int maxStackCount;
    private LongSparseArray<String> stackList = new LongSparseArray<>();

    public MainThreadBlockMonitor(int blockThreadMillis, int maxStackCount) {
        this.blockThreadMillis = blockThreadMillis;
        this.maxStackCount = maxStackCount;
        ioThread = new IoThread("IoThread");
        ioThread.start();
    }

    @Override
    public void dispatchLoopStart() {
        loopStartTime = System.currentTimeMillis();
        startDump();
    }

    @Override
    public void dispatchLoopEnd() {
        if (loopStartTime > 0L) {
            long end = System.currentTimeMillis() - loopStartTime;
            if (end >= blockThreadMillis) {
                ioThread.getIoHandler().post(printerStackRunnable);
            }
        }
        stopDump();
    }

    @Override
    public void run() {
        printStackTrace();
        if (sample) {
            //Looper开始之后，如果还没结束的话就隔300ms 抓一次堆栈信息。
            ioThread.getIoHandler().postDelayed(this, 300);
        }
    }

    private void printStackTrace() {
        WeLog.d("MainThreadBlockMonitor-> collectStack. collect count = "+stackList.size()+"  Thread is "+Thread.currentThread());
        StringBuilder stringBuilder = new StringBuilder();
        Thread thread = Looper.getMainLooper().getThread();
        for (StackTraceElement element : thread.getStackTrace()) {
            stringBuilder.append(element.toString())
                    .append(FpsConstants.SEPARATOR);
        }
        synchronized (stackList) {
            //超过了最大的堆栈次数就移除最早的一条。
            if (stackList.size() >= maxStackCount) {
                stackList.removeAt(0);
            }
            stackList.put(System.currentTimeMillis(), stringBuilder.toString());
        }
    }

    private void startDump() {
        if (sample) {
            return;
        }
        sample = true;
        ioThread.getIoHandler().removeCallbacks(this);
        ioThread.getIoHandler().postDelayed(this, getDelayTime());
    }

    private void stopDump() {
        if (!sample) {
            return;
        }
        sample = false;
        ioThread.getIoHandler().removeCallbacks(this);
    }


    private long getDelayTime() {
        return (long) (blockThreadMillis * 0.8);
    }

    private Runnable printerStackRunnable = new Runnable() {
        @Override
        public void run() {
            WeLog.d("MainThreadBlockMonitor-> printerStackToLogCat");
            StringBuilder temp = new StringBuilder();
            for (int i = 0; i < stackList.size(); i++) {
                temp.append(stackList.valueAt(i));
            }
            WeLog.e(temp.toString());
        }
    };
}
