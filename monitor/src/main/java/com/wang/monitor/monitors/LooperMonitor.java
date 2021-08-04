package com.wang.monitor.monitors;

import android.os.Build;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.SystemClock;
import android.util.Printer;

import java.util.ArrayList;
import java.util.List;

import com.wang.monitor.core.MonitorConstants;
import com.wang.monitor.interfaces.Monitor;
import com.wang.monitor.utils.ReflectUtils;

import cn.wang.log.core.WeLog;

/**
 * Created to : 通过反射设置Looper中的{@link Printer}，通过系统日志来监控Handler消息的执行时间。
 * 该类只是负责将Looper处理Handler的事件发送出去，具体监听的话要使用{@link #addLoopListener(LooperCallback)}来处理。
 *
 * @author WANG
 * @date 2021/6/10
 */
public class LooperMonitor implements MessageQueue.IdleHandler, Monitor {

    private final List<LooperCallback> listeners = new ArrayList<>();
    private InnerPrinter innerPrinter;
    public Looper mainLooper;
    private long lastCheckPrinterTime = 0L;
    private static final long CHECK_TIME = 60 * 1000L;

    /**
     * 添加回调。
     *
     * @param callback
     */
    public void addLoopListener(LooperCallback callback) {
        if (null != callback) {
            if (!listeners.contains(callback)) {
                listeners.add(callback);
            }
        }
    }

    public void removeLoopListener(LooperCallback callback) {
        if (null != callback) {
            listeners.remove(callback);
        }
    }

    /**
     * 开始工作。
     */
    @Override
    public void monitorStart() {
        init(Looper.getMainLooper());
    }

    @Override
    public void monitorPause() {
        if (null != innerPrinter) {
            innerPrinter.pause(true);
            resetCallBackState();
            mainLooper.setMessageLogging(innerPrinter.origin);
        }

    }

    @Override
    public void monitorResume() {
        if (null != innerPrinter) {
            innerPrinter.pause(false);
            mainLooper.setMessageLogging(innerPrinter);
        }
    }

    /**
     * 监控退出。
     */
    @Override
    public void monitorQuit() {
        if (innerPrinter != null) {
            mainLooper.setMessageLogging(innerPrinter.origin);
            removeIdleHandler();
            mainLooper = null;
            innerPrinter = null;
        }
        listeners.clear();
        WeLog.d("LooperMonitor quit!");
    }

    /**
     * 1.通过反射修改Looper中的Printer。
     * 2.增加一个监视器，防止自定义的Printer被挤掉，但一般来说没什么必要。
     *
     * @param looper
     */
    public void init(Looper looper) {
        mainLooper = looper;
        resetLooperPrinter();
        //添加IdleHandler避免设置的Printer失效。
        //addIdleHandler();
        WeLog.d("LooperMonitor start!");
    }

    /**
     * 反射设置Looper中的Logger。
     */
    private void resetLooperPrinter() {
       /* if (null == mainLooper) {
            removeIdleHandler();
            return;
        }*/
        Printer orgP = null;
        try {
            orgP = ReflectUtils.get(mainLooper.getClass(), MonitorConstants.LOOPER_LOGGING, mainLooper);
            if (null != innerPrinter && orgP == innerPrinter) {
                return;
            }
            //可能Looper是其它的ClassLoader加载的。参考腾讯的框架。
            if (orgP != null && innerPrinter != null) {
                if (orgP.getClass().getName().equals(innerPrinter.getClass().getName())) {
                    return;
                }
            }
        } catch (Exception e) {
            WeLog.e("LooperMonitor.init.Exception" + e);
        }
        mainLooper.setMessageLogging(innerPrinter = new InnerPrinter(orgP));
    }

    void dispatchLooperLog(String log) {
        dispatcherToListener(log.startsWith(MonitorConstants.LOOPER_START));
    }

    /**
     * 隔一分钟检查一次Printer是否还正常的工作。
     *
     * @return
     */
    @Override
    public boolean queueIdle() {
        if (SystemClock.uptimeMillis() - lastCheckPrinterTime >= CHECK_TIME) {
            resetLooperPrinter();
            lastCheckPrinterTime = SystemClock.uptimeMillis();
        }
        return true;
    }

    private void addIdleHandler() {
        handlerIdleHandler(false);
    }

    private void removeIdleHandler() {
        handlerIdleHandler(true);
    }

    /**
     * 类似于一个定时心跳。当主线程空闲的时候检查自定义的Printer是否被取代了。
     *
     * @param remove
     */
    private synchronized void handlerIdleHandler(boolean remove) {
        if (null == mainLooper) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (remove) {
                mainLooper.getQueue().removeIdleHandler(this);
            } else {
                mainLooper.getQueue().addIdleHandler(this);
            }
        } else {
            try {
                MessageQueue queue = ReflectUtils.get(mainLooper.getClass(), "mQueue", mainLooper);
                if (remove) {
                    queue.removeIdleHandler(this);
                } else {
                    queue.addIdleHandler(this);
                }
            } catch (Exception e) {

            }
        }
    }

    private void resetCallBackState() {
        for (LooperCallback callback : listeners) {
            callback.isLoopStart = false;
        }
    }

    /**
     * 将Handler开始执行跟执行之后的事件发送出去。
     *
     * @param isStart true Handler消息开始执行。false Handler消息执行结束。
     */
    private void dispatcherToListener(boolean isStart) {
        for (LooperCallback callback : listeners) {
            if (isStart) {
                if (!callback.isLoopStart) {
                    callback.loopStart();
                }
            } else {
                if (callback.isLoopStart) {
                    callback.loopEnd();
                }
            }
        }
    }

    /**
     * 自定义的Printer。
     */
    class InnerPrinter implements Printer {

        private Printer origin;
        private boolean isHasChecked = false;
        private boolean isValid = false;
        private boolean isPause = false;

        public InnerPrinter(Printer origin) {
            this.origin = origin;
        }

        public void pause(boolean pause) {
            isPause = pause;
        }

        @Override
        public void println(String x) {
            if (null != origin) {
                origin.println(x);
            }
            if (isPause) {
                return;
            }
            if (!isHasChecked) {
                isHasChecked = true;
                isValid = x.charAt(0) == '>' || x.charAt(0) == '<';
                if (!isValid) {
                    //
                }
            }

            if (isValid) {
                dispatchLooperLog(x);
            }
        }
    }

    public abstract static class LooperCallback {

        public boolean isLoopStart = false;

        public abstract void dispatchLoopStart();

        public abstract void dispatchLoopEnd();

        public abstract int callbackID();

        void loopStart() {
            if (!isLoopStart) {
                dispatchLoopStart();
            }
            isLoopStart = true;
        }

        void loopEnd() {
            if (isLoopStart) {
                dispatchLoopEnd();
            }
            isLoopStart = false;
        }

    }

}
