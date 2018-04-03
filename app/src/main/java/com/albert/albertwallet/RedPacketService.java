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
import java.util.concurrent.locks.Lock;


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
                break;
        }
    }

    @Override
    public void onInterrupt() {

    }


    synchronized private void closeRedPack(AccessibilityNodeInfo rootNode){
        if (rootNode == null){
            return;
        }
        for (int j = 0; j < rootNode.getChildCount(); j++) {
            AccessibilityNodeInfo myinfo = rootNode.getChild(j);
            List<AccessibilityNodeInfo> list = myinfo.findAccessibilityNodeInfosByText("红包记录");
            if (list.size() > 0){
                Log.e("-----------", "找到RedPacket领取页面");
                AccessibilityNodeInfo iteminfo = list.get(0);
                AccessibilityNodeInfo parentInfo = iteminfo.getParent();
                doClose(parentInfo);
                break;
            }
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
                try{
                    Thread.sleep(200);
                }catch ( Exception e){
                    e.printStackTrace();
                }
                perforGlobalClick(myinfo);
                return;
            }
        }
    }


    private void openRedPack(AccessibilityNodeInfo rootNode){
        if (rootNode == null){
            return;
        }
        for (int j = 0; j < rootNode.getChildCount(); j++) {
            AccessibilityNodeInfo myinfo = rootNode.getChild(j);
            List<AccessibilityNodeInfo> list = myinfo.findAccessibilityNodeInfosByText("发了一个");
            if (list.size() > 0){
                Log.e("-----------", "找到RedPacket页面");
                AccessibilityNodeInfo iteminfo = list.get(0);
                AccessibilityNodeInfo parentInfo = iteminfo.getParent();
                findImg(parentInfo);
                break;
            }
        }
    }

    synchronized private void findImg(AccessibilityNodeInfo parentInfo){
        int size = parentInfo.getChildCount()-1;
        for (int j = size; j > 0; j--) {
            AccessibilityNodeInfo myinfo = parentInfo.getChild(j);
            if (!myinfo.getClassName().equals("android.widget.ImageView")) {
                findImg(myinfo);
            }else{
                Log.e("-----------", "找到打开RedPacket按钮");
                try{
                    Thread.sleep(200);
                }catch ( Exception e){
                    e.printStackTrace();
                }
                perforGlobalClick(myinfo);
                return;
            }
        }
    }

    /**
     * 遍历
     */
    synchronized private void checkData(AccessibilityNodeInfo rootNode) {
        if (rootNode == null){
            return;
        }
        for (int j = 0; j < rootNode.getChildCount(); j++) {
            AccessibilityNodeInfo myinfo = rootNode.getChild(j);
            if (!myinfo.getClassName().equals("org.telegram.messenger.support.widget.RecyclerView")) {
                checkData(myinfo);
            } else {
                List<AccessibilityNodeInfo> list = myinfo.findAccessibilityNodeInfosByText("领取红包");
                Log.e("-----------", "找到可领取的RedPacket "+list.size());
                if (list.size() > 0){
                    Log.e("-----------", "发现RedPacket");
                    AccessibilityNodeInfo iteminfo = list.get(0);
                    perforGlobalClick(iteminfo);
                    Log.e("-----------", "点击RedPacket");
                    return;
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

//                if (text != null){
//                    AccessibilityNodeInfo parent = node1.getParent();
//                    while (parent != null) {
//                        Log.e("-----------",""+text);
//                        if (parent.isClickable()) {
//                            //模拟点击
////                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                            //isOpenRP用于判断该红包是否点击过
//                            Log.e("-----------","找到红包");
//                            break;
//                        }
//                        parent = parent.getParent();
//                    }
//
//                }else{
//                    Log.e("-----------","text 为空");
//                }
//            }
//
//        }
//        if (rootNode != null) {
//            //获取所有聊天的线性布局
//            List<AccessibilityNodeInfo> listChatRecord = rootNode.findAccessibilityNodeInfosByViewId("com.telegram.btcchat:id/o");
//            if(listChatRecord.size()==0){
//                return;
//            }
            //获取最后一行聊天的线性布局（即是最新的那条消息）
//            AccessibilityNodeInfo finalNode = listChatRecord.get(listChatRecord.size() - 1);
            //获取聊天对象list（其实只有size为1）
//            List<AccessibilityNodeInfo> imageName = finalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/i_");
            //获取聊天信息list（其实只有size为1）
//            List<AccessibilityNodeInfo> record = finalNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ib");
//        }
//    }
//
//    /**
//     * 必须重写的方法：系统要中断此service返回的响应时会调用。在整个生命周期会被调用多次。
//     */
//    @Override
//    public void onInterrupt() {
//        Toast.makeText(this, "我快被终结了啊-----", Toast.LENGTH_SHORT).show();
//    }
//
//    /**
//     * 服务开始连接
//     */
//    @Override
//    protected void onServiceConnected() {
//        Toast.makeText(this, "服务已开启", Toast.LENGTH_SHORT).show();
//        super.onServiceConnected();
//    }
//
//    /**
//     * 服务断开
//     *
//     * @param
//     * @return
//     */
//    @Override
//    public boolean onUnbind(Intent intent) {
//        Toast.makeText(this, "服务已被关闭", Toast.LENGTH_SHORT).show();
//        return super.onUnbind(intent);
//    }
//}
//        switch (eventType) {
//            //界面跳转的监听
//            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
//                String className = event.getClassName().toString();
//                //判断是否是微信聊天界面
//                if (LAUCHER.equals(className)) {
//                    //获取当前聊天页面的根布局
//                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
//                    //开始找红包
//                    findRedPacket(rootNode);
//                }
//
//                //判断是否是显示‘开’的那个红包界面
//                if (LUCKEY_MONEY_RECEIVER.equals(className)) {
//                    AccessibilityNodeInfo rootNode = getRootInActiveWindow();
//                    //开始抢红包
//                    openRedPacket(rootNode);
//                }
//
//                //判断是否是红包领取后的详情界面
//                if (isOpenDetail && LUCKEY_MONEY_DETAIL.equals(className)) {
//
//                    isOpenDetail = false;
//                    //返回桌面
//                    back2Home();
//                    //如果之前是锁着屏幕的则重新锁回去
//                    release();
//                }
//                break;
//        }
//    }
//
//    /**
//     * 开始打开红包
//     */
//    private void openRedPacket(AccessibilityNodeInfo rootNode) {
//        for (int i = 0; i < rootNode.getChildCount(); i++) {
//            AccessibilityNodeInfo node = rootNode.getChild(i);
//            if ("android.widget.ImageView".equals(node.getClassName())) {
//                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//
//                isOpenDetail = true;
//            }
//            openRedPacket(node);
//        }
//    }
//
//    /**
//     * 遍历查找红包
//     */
//    private void findRedPacket(AccessibilityNodeInfo rootNode) {
//        if (rootNode != null) {
//            //从最后一行开始找起
//            for (int i = rootNode.getChildCount() - 1; i >= 0; i--) {
//                AccessibilityNodeInfo node = rootNode.getChild(i);
//                //如果node为空则跳过该节点
//                if (node == null) {
//                    continue;
//                }
//                CharSequence text = node.getText();
//                if (text != null && text.toString().equals("领取红包")) {
//                    AccessibilityNodeInfo parent = node.getParent();
//                    //while循环,遍历"领取红包"的各个父布局，直至找到可点击的为止
//                    while (parent != null) {
//                        if (parent.isClickable()) {
//                            //模拟点击
//                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
//                            //isOpenRP用于判断该红包是否点击过
//                            isOpenRP = true;
//
//                            break;
//                        }
//                        parent = parent.getParent();
//                    }
//                }
//                //判断是否已经打开过那个最新的红包了，是的话就跳出for循环，不是的话继续遍历
//                if (isOpenRP) {
//                    break;
//                } else {
//                    findRedPacket(node);
//                }
//
//            }
//        }
//    }
//
//    /**
//     * 服务连接
//     */
//    @Override
//    protected void onServiceConnected() {
//        Toast.makeText(this, "抢红包服务开启", Toast.LENGTH_SHORT).show();
//        super.onServiceConnected();
//    }
//
//    /**
//     * 必须重写的方法：系统要中断此service返回的响应时会调用。在整个生命周期会被调用多次。
//     */
//    @Override
//    public void onInterrupt() {
//        Toast.makeText(this, "我快被终结了啊-----", Toast.LENGTH_SHORT).show();
//    }
//
//    /**
//     * 服务断开
//     */
//    @Override
//    public boolean onUnbind(Intent intent) {
//        Toast.makeText(this, "抢红包服务已被关闭", Toast.LENGTH_SHORT).show();
//        return super.onUnbind(intent);
//    }
//
//    /**
//     * 返回桌面
//     */
//    private void back2Home() {
//        Intent home = new Intent(Intent.ACTION_MAIN);
//        home.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        home.addCategory(Intent.CATEGORY_HOME);
//        startActivity(home);
//    }
//
//    /**
//     * 判断是否处于亮屏状态
//     *
//     * @return true-亮屏，false-暗屏
//     */
//    private boolean isScreenOn() {
//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        isScreenOn = pm.isScreenOn();
//        Log.e("isScreenOn", isScreenOn + "");
//        return isScreenOn;
//    }
//
//    /**
//     * 解锁屏幕
//     */
//    private void wakeUpScreen() {
//
//        //获取电源管理器对象
//        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
//        //后面的参数|表示同时传入两个值，最后的是调试用的Tag
//        wakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "bright");
//
//        //点亮屏幕
//        wakeLock.acquire();
//
//        //得到键盘锁管理器
//        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
//        keyguardLock = km.newKeyguardLock("unlock");
//
//        //解锁
//        keyguardLock.disableKeyguard();
//    }
//
//    /**
//     * 释放keyguardLock和wakeLock
//     */
//    public void release() {
//        if (keyguardLock != null) {
//            keyguardLock.reenableKeyguard();
//            keyguardLock = null;
//        }
//        if (wakeLock != null) {
//            wakeLock.release();
//            wakeLock = null;
//        }
//    }

