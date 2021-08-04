package com.wang.monitor.monitors;

import android.os.Handler;
import android.os.Looper;

import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;

/**
 * Created to : 仿写HandlerThread。
 *
 * @author WANG
 * @date 2021/6/16
 */
public class IoThread extends Thread {

    private Handler mIoHandler;
    Looper mIoLooper;
    private Set<Runnable> mTask = new HashSet<>(5);

    private IoThread(@NonNull String name) {
        super(name);
    }

    private static ThreadLocal<IoThread> ioThreadThreadLocal = new ThreadLocal<>();

    public static IoThread getIoThread() {
        if (ioThreadThreadLocal.get() == null) {
            ioThreadThreadLocal.set(new IoThread("IoThread"));
        }
        return ioThreadThreadLocal.get();
    }

    @Override
    public void run() {
        super.run();
        Looper.prepare();
        synchronized (this) {
            mIoLooper = Looper.myLooper();
            notifyAll();
        }
        runTask();
        Looper.loop();
    }

    public void addTask(Runnable runnable) {
        mTask.add(runnable);
    }

    public void removeTask(Runnable runnable) {
        mTask.remove(runnable);
    }

    public void runTask() {
        if (mTask.size() > 0) {
            for (Runnable task : mTask) {
                task.run();
            }
        }
    }

    public Looper getIoLooper() {
        if (!isAlive()) {
            return null;
        }
        synchronized (this) {
            while (isAlive() && mIoLooper == null) {
                try {
                    wait();
                } catch (InterruptedException e) {

                }
            }
        }
        return mIoLooper;
    }

    public Handler getIoHandler() {
        if (null == mIoHandler) {
            mIoHandler = new Handler(getIoLooper());
        }
        return mIoHandler;
    }

    /**
     * 立马终止，不需要处理消息队列中的消息。
     */
    public void quite() {
        Looper ioLooper = getIoLooper();
        if (null != ioLooper) {
            ioLooper.quit();
        }
        ioThreadThreadLocal.remove();
    }
}
