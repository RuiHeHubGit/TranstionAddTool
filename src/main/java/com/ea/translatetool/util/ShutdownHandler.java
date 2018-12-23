package com.ea.translatetool.util;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by HeRui on 2018/12/23.
 */
public abstract class ShutdownHandler extends Thread{
    private static List<ShutdownHandler> handlerList;

    public synchronized static void addShutdownHandler(ShutdownHandler handler) {
        if(handlerList == null) {
            handlerList = new ArrayList<ShutdownHandler>();
        }
        if(handlerList.contains(handler)) {
            return;
        }
        handlerList.add(handler);
        Runtime.getRuntime().addShutdownHook(handler);
    }

    public synchronized static void removeShutdownHandler(ShutdownHandler handler) {
        if(handlerList == null) {
            handlerList = new ArrayList<ShutdownHandler>();
        }
        if(!handlerList.contains(handler)) {
            return;
        }
        handlerList.remove(handler);
        Runtime.getRuntime().addShutdownHook(handler);
    }

    @Override
    abstract public void run();
}