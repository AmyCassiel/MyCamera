package com.example.a3droplets.mycamera;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

public class MyAccessibilityService extends AccessibilityService {

    private static final CharSequence TASK_LIST_VIEW_CLASS_NAME =
            "com.example.android.apis.accessibility.TaskListView";
    private static final String LOG_TAG = "MyAccessibilityService/onAccessibilityEvent";
    /** 标记是否初始化按键 */
    private boolean mKeyOnTouch;
    private AccessibilityServiceInfo info;
    private Context context;

    public void onServiceConnected() {
        //设置此服务想要监听的事件类型，其他类型将不会传递到此服务
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED;
        //该服务运行在指定的应用中
        info.packageNames = new String[]{"com.example.a3droplets.mycamera"};
        //设置服务将提供的反馈类型
        info.feedbackType = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        info.notificationTimeout = 100;
        this.setServiceInfo(info);

    }
    //绑定后的回调

    public IBinder onBinder(Intent intent) {
        return new MyIBinder();
    }

    //代理人
    public class MyIBinder extends Binder {
        public void Change(){
            Toast.makeText(getApplicationContext(),"您已连接成功",Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 通过遍历视图的树和来处理可访问性事件收集信息。
     */
    @SuppressLint("LongLogTag")
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (!mKeyOnTouch) {
            Log.e(LOG_TAG, "button engine not ready.  Bailing out.");
            return;
        }

        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return;
        }

        // Grab the parent of the view that fired the event.
        AccessibilityNodeInfo rowNode = getListItemNodeInfo(source);
        if (rowNode == null) {
            return;
        }

        // Using this parent, get references to both child nodes, the label and the checkbox.
        AccessibilityNodeInfo labelNode = rowNode.getChild(0);
        if (labelNode == null) {
            rowNode.recycle();
            return;
        }

        AccessibilityNodeInfo completeNode = rowNode.getChild(1);
        if (completeNode == null) {
            rowNode.recycle();
            return;
        }

        // Determine what the task is and whether or not it's complete, based on the text inside the label, and the state of the check-box.
        if (rowNode.getChildCount() < 2 || !rowNode.getChild(1).isCheckable()) {
            rowNode.recycle();
            return;
        }

        //成为系统应用，然后自己开启这个服务
        String enabledServicesSetting = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        ComponentName selfComponentName = new ComponentName(
                context.getPackageName(),
                "xxx");
        String flattenToString = selfComponentName.flattenToString();
        //null 表示没有任何服务
        if (enabledServicesSetting == null){
            enabledServicesSetting=flattenToString;
        }else if(!enabledServicesSetting.contains(flattenToString)) {
            enabledServicesSetting = enabledServicesSetting +":"+ flattenToString;
        }
        Settings.Secure.putString(context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                flattenToString);
        Settings.Secure.putInt(context.getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED, 1);

    }
    private AccessibilityNodeInfo getListItemNodeInfo(AccessibilityNodeInfo source) {
        AccessibilityNodeInfo current = source;
        while (true) {
            AccessibilityNodeInfo parent = current.getParent();
            if (parent == null) {
                return null;
            }
            if (TASK_LIST_VIEW_CLASS_NAME.equals(parent.getClassName())) {
                return current;
            }
            // NOTE: Recycle the infos.
            AccessibilityNodeInfo oldCurrent = current;
            current = parent;
            oldCurrent.recycle();
        }
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mKeyOnTouch) {

        }
    }
}
