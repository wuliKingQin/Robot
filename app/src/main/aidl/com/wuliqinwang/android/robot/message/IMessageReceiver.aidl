// IMessageReceiver.aidl
package com.wuliqinwang.android.robot.message;

// Declare any non-default types here with import statements

interface IMessageReceiver {
     // 接收者名字，改名字必须是唯一的，将用于获取该接收器
     String getReceiverName();
     // 接收消息的方法
     void onReceiving(String senderName, in byte[] message);
}