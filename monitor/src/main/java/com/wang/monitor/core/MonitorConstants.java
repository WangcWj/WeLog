package com.wang.monitor.core;

/**
 * Created to :
 *
 * @author WANG
 * @date 2021/6/10
 */
public class MonitorConstants {

    public static final String CALLBACK_QUEUE = "mCallbackQueues";
    public static final String ADD_CALLBACK_LOCKED = "addCallbackLocked";
    public static final String DISPLAY_EVENT_RECEIVER = "mDisplayEventReceiver";
    public static final String FRAME_INTERVAL_NANOS = "mFrameIntervalNanos";
    public static final String LOOPER_LOGGING = "mLogging";
    public static final String LOOPER_START = ">>";
    public static final String LOOPER_END = "<<";
    public static final String SEPARATOR = "\r\n";


    public static final long DEFAULT_FRAME_DURATION = 16666667L;
    public static final int DEFAULT_BLOCK_THRESHOLD_MILLIS = 3000;
    public static final int APP_MONITOR_ID = 1;
    public static final int MAIN_THREAD_BLOCK_MONITOR_ID = 2;

    public static final int UN_INIT = 0;
    public static final int RESUME = 1;
    public static final int PAUSE = 2;

    /**
     * Callback type: Input callback.  Runs first.
     *
     * @hide
     */
    public static final int CALLBACK_INPUT = 0;

    /**
     * Callback type: Animation callback.  Runs before traversals.
     *
     * @hide
     */
    public static final int CALLBACK_ANIMATION = 1;

    /**
     * Callback type: Commit callback.  Handles post-draw operations for the frame.
     * Runs after traversal completes.
     *
     * @hide
     */
    public static final int CALLBACK_TRAVERSAL = 2;

}
