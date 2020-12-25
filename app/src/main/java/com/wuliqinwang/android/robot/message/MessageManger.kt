package com.wuliqinwang.android.robot.message

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log

/**
 * @Description: 用于其他进程绑定消息中转服务以及注册消息监听等，该类运行在其他进程
 * @Version: 1.0.0
 */
object MessageManger {

    // DES: 中转服务的所处的包名
    private const val TRANSIT_SERVICE_PACKAGE_NAME = "com.wuliqinwang.android.robot"

    // DES: 中转服务的类名
    private const val TRANSIT_SERVICE_NAME =
        "com.wuliqinwang.android.robot.message.MessageTransitService"

    // DES: 保存中转服务链接
    private val mTransitServiceConnection by lazy {
        TransitServiceConnection()
    }

    // DES: 发送字节消息
    fun sendMessage(target: String, message: ByteArray) {
        mTransitServiceConnection.sendMessage(target, message)
    }

    // DES: 发送字符串消息
    fun sendMessage(target: String, message: String) {
        sendMessage(target, message.toByteArray())
    }

    // DES: 注册接收器到中转服务
    fun registerReceiverToTransitService(context: Context, receiver: IMessageReceiver) {
        Log.d("test===", "开始注册${receiver.receiverName}接收器")
        mTransitServiceConnection.receiver = receiver
        bindTransitService(context)
    }

    // DES: 绑定中转服务
    private fun bindTransitService(context: Context) {
        context.bindService(Intent().apply {
            setClassName(
                TRANSIT_SERVICE_PACKAGE_NAME,
                TRANSIT_SERVICE_NAME
            )
            type = context.packageName
        }, mTransitServiceConnection, Context.BIND_AUTO_CREATE)
    }

    // DES: 中转服务绑定链接监听
    class TransitServiceConnection(var receiver: IMessageReceiver? = null) : ServiceConnection {
        // DES: 拿到消息处理器的代理类
        private var mMessageHandlerProxy: IMessageHandler? = null
        override fun onServiceDisconnected(name: ComponentName?) {
            // DES: 中断服务的时候将接收者注销掉
            mMessageHandlerProxy?.unregisterMessageReceiver(receiver)
            mMessageHandlerProxy = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            mMessageHandlerProxy = IMessageHandler.Stub.asInterface(service)
            // DES: 注册我们的消息接收器到中转服务
            mMessageHandlerProxy?.registerMessageReceiver(receiver)
        }

        // DES: 发送消息到目标用户
        fun sendMessage(target: String, message: ByteArray) {
            mMessageHandlerProxy?.transitMessage(receiver?.receiverName, target, message)
        }
    }
}