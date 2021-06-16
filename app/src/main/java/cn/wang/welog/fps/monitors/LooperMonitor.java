package cn.wang.welog.fps.monitors;

import android.os.Build;
import android.os.Looper;
import android.os.MessageQueue;
import android.util.Log;
import android.util.Printer;

import java.util.HashSet;

import cn.wang.welog.fps.core.FpsConstants;
import cn.wang.welog.fps.utils.ReflectUtils;

/**
 * Created to :
 *
 * @author WANG
 * @date 2021/6/10
 */
public class LooperMonitor implements MessageQueue.IdleHandler {

    private HashSet<LooperCallback> listeners = new HashSet<>();
    private InnerPrinter innerPrinter;
    public Looper mainLooper;
    private long lastCheckPrinterTime = 0L;


    public void addLoopListener(LooperCallback callback) {
        if (null != callback) {
            listeners.add(callback);
        }
    }

    public void removeLoopListener(LooperCallback callback) {
        if (null != callback) {
            listeners.add(callback);
        }
    }

    public void init(Looper looper) {
        mainLooper = looper;
        Printer orgP = null;
        try {
            orgP = ReflectUtils.get(looper.getClass(), FpsConstants.LOOPER_LOGGING, looper);
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
            Log.e("WANG", "LooperMonitor.init.Exception " + e);
        }
        looper.setMessageLogging(innerPrinter = new InnerPrinter(orgP));
        //添加IdleHandler避免设置的Printer失效。
        addIdleHandler();
    }

    void dispatchLooperLog(String log) {
        dispatcherToListener(log.startsWith(FpsConstants.LOOPER_START));
    }

    @Override
    public boolean queueIdle() {

        return true;
    }

    private void addIdleHandler() {
        handlerIdleHandler(false);
    }

    private void removeIdleHandler() {
        handlerIdleHandler(true);
    }

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

    class InnerPrinter implements Printer {

        private Printer origin;
        private boolean isHasChecked = false;
        private boolean isValid = false;

        public InnerPrinter(Printer origin) {
            this.origin = origin;
        }

        @Override
        public void println(String x) {
            if (null != origin) {
                origin.println(x);
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

        boolean isLoopStart = false;

        public abstract void dispatchLoopStart();

        public abstract void dispatchLoopEnd();

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
