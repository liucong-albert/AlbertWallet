package com.albert.albertwallet;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by liuc on 2018-04-02.
 * 主要内容：
 */


public class RedPacketService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        switch (eventType) {
            //每次在聊天界面中有新消息到来时都出触发该事件
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                //获取聊天信息
                checkData(rootNode);
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                openRedPack(rootNode);
                closeRedPack(rootNode);
//                findDown(rootNode);
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }

    synchronized private void findDown(AccessibilityNodeInfo rootNode){
        if (rootNode == null){
            return;
        }
        for (int j = 0; j < rootNode.getChildCount(); j++) {
            AccessibilityNodeInfo myinfo = rootNode.getChild(j);
//            List<AccessibilityNodeInfo> list = myinfo.findAccessibilityNodeInfosByText("红包记录");
            if (myinfo.getChildCount() == 6){
                Log.e("-----------", "出现下拉按钮");
                clickDown(myinfo);
                break;
            }else{
                findDown(myinfo);
            }
        }
    }

    private void clickDown(AccessibilityNodeInfo rootNode){
        if (rootNode == null){
            return;
        }
        for (int j = 0; j < rootNode.getChildCount(); j++) {
            AccessibilityNodeInfo myinfo = rootNode.getChild(j);
            if (j == 5){
                Log.e("-----------", ""+myinfo.getClassName());
                Log.e("-----------", "点击按钮");
                perforGlobalClick(myinfo);
                break;
            }else{
                findDown(myinfo);
            }
        }
    }


    synchronized private void closeRedPack(AccessibilityNodeInfo rootNode){
        if (rootNode == null){
            return;
        }
        List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByText("红包记录");
        if (list.size() > 0){
            long  time1 = System.currentTimeMillis();
            if (time1 - time3 <1000){
                return;
            }
            time3 = System.currentTimeMillis();
            Log.e("-----------", "找到RedPacket领取页面");
            AccessibilityNodeInfo iteminfo = list.get(0);
            AccessibilityNodeInfo parentInfo = iteminfo.getParent();
            doClose(parentInfo);
        }
    }

    synchronized private void doClose(AccessibilityNodeInfo parentInfo){
        int size = parentInfo.getChildCount();
        for (int j = 0; j <size; j++) {
            AccessibilityNodeInfo myinfo = parentInfo.getChild(j);
            if (!myinfo.getClassName().equals("android.widget.ImageView")) {
                findImg(myinfo);
            }else{
                Log.e("-----------", "找到关闭RedPacket按钮");
                perforGlobalClick(myinfo);
                return;
            }
        }
    }


    private void openRedPack(AccessibilityNodeInfo rootNode){
        if (rootNode == null){
            return;
        }
        List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByText("发了一个");
        if (list.size() > 0){
            long  time1 = System.currentTimeMillis();
            if (time1 - time2 <1000){
                return;
            }
            time2 = System.currentTimeMillis();
            Log.e("-----------", "打开RedPacket");
            AccessibilityNodeInfo iteminfo = list.get(0);
            AccessibilityNodeInfo parentInfo = iteminfo.getParent();
            findImg(parentInfo);
        }
    }

    synchronized private void findImg(AccessibilityNodeInfo parentInfo){
        int size = parentInfo.getChildCount()-1;
        for (int j = size; j > 0; j--) {
            AccessibilityNodeInfo myinfo = parentInfo.getChild(j);
            if (!myinfo.getClassName().equals("android.widget.ImageView")) {
                findImg(myinfo);
            }else{
                Log.e("-----------", "成功领取RedPacket");
                perforGlobalClick(myinfo);
                return;
            }
        }
    }

    /**
     * 遍历
     */
    long time = 0;
    long time2 = 0;
    long time3 = 0;
    synchronized private void checkData(AccessibilityNodeInfo rootNode) {
        if (rootNode == null){
            return;
        }
        List<AccessibilityNodeInfo> list = rootNode.findAccessibilityNodeInfosByText("领取红包");
        if (list.size() > 0){
            Log.e("-----------", "time = "+time);
            long  time1 = System.currentTimeMillis();
            if (time1 - time <1000){
                return;
            }
            time = System.currentTimeMillis();
            Log.e("-----------", "time  init = "+time);

            AccessibilityNodeInfo iteminfo = list.get(0);
            perforGlobalClick(iteminfo);
            Log.e("-----------", "发现RedPacket");
            return;
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
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
