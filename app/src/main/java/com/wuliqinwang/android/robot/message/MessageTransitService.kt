package com.wuliqinwang.android.robot.message

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.lang.NullPointerException
import java.util.concurrent.ConcurrentHashMap

/**
 * @Description: 用于消息的中转的服务类
 * @Version: 1.0.0
 */
class MessageTransitService: Service(){

    companion object {
        // DES: 用户存储大小，默认是4
        private const val USER_INITIAL_CAPACITY = 4
    }

    // DES: 用于存储用户
    private val mUsers by lazy {
        ConcurrentHashMap<String, IMessageReceiver>(USER_INITIAL_CAPACITY)
    }

    override fun onBind(intent: Intent?): IBinder = MessageHandler()


    // DES: 用于处理多个进程互相发送消息的中转处理器
    inner class MessageHandler: IMessageHandler.Stub() {

        override fun transitMessage(sender: String?, targetUser: String?, message: ByteArray?) {
            val receiver = mUsers[targetUser]
            receiver?.onReceiving(sender, message)
        }

        override fun registerMessageReceiver(receiver: IMessageReceiver?) {
            receiver ?: return
            if (receiver.receiverName.isNullOrEmpty()) {
                throw NullPointerException("The getReceiverName() method cannot return null")
            }
            Log.d("test===", "注册${receiver.receiverName}成功")
            mUsers[receiver.receiverName] = receiver
        }

        override fun unregisterMessageReceiver(receiver: IMessageReceiver?) {
            receiver ?: return
            mUsers.remove(receiver.receiverName)
        }
    }
}