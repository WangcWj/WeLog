package com.wang.monitor.interfaces;

/**
 * Created to :
 *
 * @author cc.wang
 * @date 2021/6/17
 */
public interface Monitor {

    void monitorStart();

    void monitorPause();

    void monitorResume();

    void monitorQuit();
}
