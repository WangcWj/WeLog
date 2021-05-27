package cn.wang.log.dispatchers;

import android.util.Log;

import java.util.concurrent.ConcurrentLinkedQueue;

import cn.wang.log.core.LogMsg;
import cn.wang.log.utils.WeLogThreadUtils;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/5/13
 */
public class ThreadWorkerDispatcher implements Dispatcher {

    private Worker worker;

    public ThreadWorkerDispatcher() {
        worker = new Worker();
    }

    @Override
    public void dispatch(LogMsg message) {
        if (message.isSync) {
            message.run();
            return;
        }
        if (worker == null) {
            return;
        }
        Log.e("cc.wang", "ThreadPoolDispatcher.dispatch." + message.hashCode());
        if (worker.workQueue.offer(message)) {
            if (!worker.isStart()) {
                worker.start();
            } else {
                worker.notifyRun();
            }
        }
    }

    @Override
    public void shutDown() {
        if (worker != null) {
            worker.shutDown();
        }
    }

    static class Worker implements Runnable {

        private final Thread thread;
        private final Object lock = new Object();
        private boolean isWork;
        private volatile boolean isRun = true;
        private volatile boolean isStart;
        public ConcurrentLinkedQueue<Runnable> workQueue = new ConcurrentLinkedQueue<>();

        public Worker() {
            isWork = false;
            isStart = false;
            thread = WeLogThreadUtils.threadFactory("WeLog Thread:", false).newThread(this);
        }

        /**
         * 可能多线程调用。
         */
        private void notifyRun() {
            if (!isWork) {
                synchronized (lock) {
                    lock.notify();
                }
            }
        }

        public synchronized void start() {
            if (!isStart) {
                isStart = true;
                thread.start();
            }
        }

        public boolean isStart() {
            return isStart;
        }

        public void shutDown() {
            isRun = false;
            if (isStart) {
                notifyRun();
            }
            isStart = false;
        }

        @Override
        public void run() {
            while (isRun) {
                synchronized (lock) {
                    isWork = true;
                    //任务执行的过程中抛出异常，不要影响Worker线程。
                    try {
                        Runnable poll = workQueue.poll();
                        if (poll == null) {
                            isWork = false;
                            Log.e("cc.wang", "Worker.run.wait ");
                            lock.wait();
                            isWork = true;
                        } else {
                            poll.run();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("cc.wang", "Worker.run." + e);
                        isWork = false;
                    }
                }
            }
            Log.e("cc.wang", "Worker.run.Finish");
        }
    }

}
