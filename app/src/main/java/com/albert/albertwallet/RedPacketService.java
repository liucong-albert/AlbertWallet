package com.albert.albertwallet;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.List;


/**
 * Created by liuc on 2018-04-02.
 * 主要内容：
 */


public class RedPacketService extends AccessibilityService {

    private long time;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        switch (eventType) {
            //每次在聊天界面中有新消息到来时都出触发该事件
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                //获取聊天信息
                getWeChatLog(rootNode);
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                click(rootNode);
                close(rootNode);
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    private void close(AccessibilityNodeInfo rootNode){
        if (rootNode == null){
            return;
        }
        for (int j = 1; j < rootNode.getChildCount(); j++) {
            AccessibilityNodeInfo myinfo = rootNode.getChild(j);
            List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByText("红包记录");
            if (list.size() > 0){
                Log.e("demo", "找到红包领用界面 ");
                doClose(rootNode);
                break;
            }else{
                close(myinfo);
            }
        }
    }

    private void doClose(AccessibilityNodeInfo rootNode){
        for (int j = 0; j < rootNode.getChildCount(); j++) {
            AccessibilityNodeInfo myinfo = rootNode.getChild(j);
            if (!myinfo.getClassName().equals("android.widget.ImageView")) {
                doClose(myinfo);
            } else {
                Log.e("demo", "关闭页面 ");
                Log.e("demo", "关闭页面 "+myinfo.isClickable());
                myinfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }



    private void click(AccessibilityNodeInfo rootNode){
        if (rootNode == null){
            return;
        }
        for (int j = 1; j < rootNode.getChildCount(); j++) {
            AccessibilityNodeInfo myinfo = rootNode.getChild(j);
            if (!myinfo.getClassName().equals("android.widget.ImageView")) {
                click(myinfo);
            } else {
                long time1 = System.currentTimeMillis();
                if (time1 - time <500){
                    return;
                }
                time = System.currentTimeMillis();
                Log.e("demo", "点击红包 ");
                Log.e("demo", "className =  "+myinfo.getClassName());
                myinfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }


    /**
     * 遍历
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void getWeChatLog(AccessibilityNodeInfo rootNode) {
        CharSequence name = rootNode.getClassName();
        if (name.equals("org.telegram.messenger.support.widget.RecyclerView")){
            Log.e("demo", " 找到listview");
        }
        checkData(rootNode);
    }

    public void checkData(AccessibilityNodeInfo rootNode) {
        for (int j = 0; j < rootNode.getChildCount(); j++) {
            AccessibilityNodeInfo myinfo = rootNode.getChild(j);
            int size = myinfo.getChildCount();
            if (!myinfo.getClassName().equals("org.telegram.messenger.support.widget.RecyclerView")) {
                checkData(myinfo);
            } else {
                Log.e("----------", "找到啦！！！！！！！！！！！！！！！");
                Log.e("demo", "myinfo isClick:" + myinfo.isClickable());
                List<AccessibilityNodeInfo> list = myinfo.findAccessibilityNodeInfosByText("领取红包");
                for(int c = 0 ; c < list.size() ; c++){
                    Log.e("-----------", "发现红包");
                    AccessibilityNodeInfo iteminfo = list.get(c);
                    Log.e("-----------", "iteminfo "+iteminfo.isClickable());
                    perforGlobalClick(iteminfo);
                }
            }
        }
    }

    public static void perforGlobalClick(AccessibilityNodeInfo info) {
        Rect rect = new Rect();
        info.getBoundsInScreen(rect);
        perforGlobalClick(rect.centerX(), rect.centerY());
    }

    public static void perforGlobalClick(int x, int y) {
        execShellCmd("input tap " + x + " " + y);

    }
    /**
     * 执行shell命令
     *
     * @param cmd
     */
    public static void execShellCmd(String cmd) {

        try {
            // 申请获取root权限，这一步很重要，不然会没有作用
            Process process = Runtime.getRuntime().exec("su");
            // 获取输出流
            OutputStream outputStream = process.getOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
            dataOutputStream.writeBytes(cmd);
            dataOutputStream.flush();
            dataOutputStream.close();
            outputStream.close();
//            process.waitFor();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

