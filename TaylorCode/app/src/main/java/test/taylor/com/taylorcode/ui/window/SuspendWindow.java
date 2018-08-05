package test.taylor.com.taylorcode.ui.window;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * suspending window in app,it shows throughout the whole app
 */
public class SuspendWindow {
    /**
     * the content view of window
     */
    private View windowView;
    /**
     * the layout param for windowView
     */
    private WindowManager.LayoutParams layoutParam;
    private Context context;
    /**
     * show or dismiss the window according to the app lifecycle
     */
    private AppStatusListener appStatusListener;
    /**
     * this list records the activities which shows this window
     */
    private List<Class> whiteList;
    /**
     * if true,whiteList will be used to depend which activity could show window
     * if false,all activities in app is allow to show window
     */
    private boolean enableWhileList;

    private static volatile SuspendWindow INSTANCE;

    public static SuspendWindow getInstance() {
        if (INSTANCE == null) {
            synchronized (SuspendWindow.class) {
                if (INSTANCE == null) {
                    //in case of memory leak for singleton
                    INSTANCE = new SuspendWindow();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context) {
        this.context = context.getApplicationContext();
    }

    private SuspendWindow() {
        appStatusListener = new AppStatusListener();
        whiteList = new ArrayList<>();
    }

    public AppStatusListener getAppStatusListener() {
        return appStatusListener;
    }

    private void show(Context context) {
        if (context == null) {
            return;
        }
        this.context = context;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return;
        }
        if (windowView == null) {
            windowView = generateDefaultWindowView();
        }
        if (layoutParam == null) {
            layoutParam = generateDefaultLayoutParam();
        }
        //in case of "IllegalStateException :has already been added to the window manager."
        if (windowView.getParent() == null) {
            windowManager.addView(windowView, layoutParam);
        }
        windowView.setVisibility(View.VISIBLE);
    }


    private WindowManager.LayoutParams generateDefaultLayoutParam() {
        if (context == null) {
            return new WindowManager.LayoutParams();
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;//this is the key point let window be above all activity
//        layoutParams.token = getWindow().getDecorView().getWindowToken();
        layoutParams.format = PixelFormat.TRANSLUCENT;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.x = 0;
        layoutParams.y = screenHeight / 3;
        return layoutParams;
    }

    private View generateDefaultWindowView() {
        TextView tv = new TextView(context);
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(layoutParams);
        tv.setText("window view");
        tv.setTextColor(Color.parseColor("#00ff00"));
        tv.setTextSize(30);
        return tv;
    }

    private void dismiss() {
        if (context == null) {
            return;
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.removeView(windowView);
        }
    }

    public void setWhiteList(List<Class> whiteList) {
        enableWhileList = true;
        this.whiteList = whiteList;
    }

    public SuspendWindow setView(View view) {
        this.windowView = view;
        return this;
    }

    public SuspendWindow setLayoutParam(WindowManager.LayoutParams layoutParam) {
        this.layoutParam = layoutParam;
        return this;
    }

    /**
     * the listener control the timing of showing or dismissing the window
     */
    private class AppStatusListener implements Application.ActivityLifecycleCallbacks {

        private int foregroundActivityCount = 0;
        private boolean isConfigurationChange = false;


        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (isConfigurationChange) {
                isConfigurationChange = false;
                return;
            }

            //show the window when app is in foreground again
            if (enableWhileList) {
                if (whiteList.contains(activity.getClass())) {
                    show(activity.getApplicationContext());
                } else {
                    dismiss();
                }
            } else {
                show(activity.getApplicationContext());
            }
            foregroundActivityCount++;
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            if (activity.isChangingConfigurations()) {
                isConfigurationChange = true;
                return;
            }
            foregroundActivityCount--;
            if (foregroundActivityCount == 0) {
                //dismiss the window when app is in background
                dismiss();
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }
}
