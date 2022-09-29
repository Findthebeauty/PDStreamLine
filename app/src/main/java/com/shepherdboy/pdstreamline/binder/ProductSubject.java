package com.shepherdboy.pdstreamline.binder;

import java.util.HashMap;
/**
 * 全局商品管理，用于商品与布局的同步
 */
public class ProductSubject implements Subject{

    private final HashMap<Integer, Observer> observers = new HashMap<>();
    public static final int SYNC_PRODUCT = 999;
    @Override
    public void attach(Observer observer) {

        observers.put(((ProductObserver)observer).getActivityIndex(), observer);
    }

    @Override
    public void detach(Observer observer) {

        observers.remove(((ProductObserver)observer).getActivityIndex());
    }

    @Override
    public void notify(String message) {

        for (Observer observer : observers.values()) {

            observer.update(message);
        }
    }

    public HashMap<Integer, Observer> getObservers() {
        return observers;
    }
}
