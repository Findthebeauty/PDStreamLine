package com.shepherdboy.pdstreamline.activities;

import android.app.Activity;

import java.util.Stack;

public class ActivityManager {

    private static volatile ActivityManager activityManager;

    private Stack<Activity> activities;

    private ActivityManager() {

        activities = new Stack<>();
    }

    public static synchronized ActivityManager getInstance(){

        if (activityManager == null) {

            synchronized (ActivityManager.class) {

                if (activityManager == null) {

                    activityManager = new ActivityManager();
                }
            }
        }

        return activityManager;
    }

    public void addActivity(Activity activity) {

        activities.add(activity);
    }

    public void removeCurrent() {

        Activity activity = activities.lastElement();
        activity.finish();
        activities.remove(activity);
    }

    public void remove(Activity activity) {

        if (activity == null) return;

        for (int i = activities.size() - 1; i >= 0; i--) {
            Activity current = activities.get(i);

            if (current.getClass().equals(activity.getClass())) {
                current.finish();
                activities.remove(i);
            }
        }
    }

    public void removeAll() {

        for (int i = activities.size() - 1; i >= 0; i--) {

            activities.remove(i).finish();
        }
    }

    public void removeAllExcept(Activity activity) {

        for (int i = activities.size() - 1; i >= 0; i--) {

            Activity current = activities.get(i);

            if (current.getClass().equals(activity.getClass()))
                continue;
            activities.remove(i).finish();
        }
    }
}
