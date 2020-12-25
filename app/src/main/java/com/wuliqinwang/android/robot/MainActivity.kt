package com.wuliqinwang.android.robot

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import com.wuliqinwang.android.robot.message.IMessageReceiver
import com.wuliqinwang.android.robot.message.MessageManger

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // DES: 注册消息接收器到中转服务
        MessageManger.registerReceiverToTransitService(this, MessageReceiver())

        findViewById<View>(R.id.send_message_btn)?.setOnClickListener {
            val messageContent = findViewById<EditText>(
                R.id.message_content_edt)?.text?.toString()?.trim() ?: ""
            // DES: 发送消息到企业微信，然后企业微信再随机选取一个用户发送消息
            MessageManger.sendMessage("WeWork001", messageContent)
        }
    }

    // DES: 接收企业微信发送来的消息
    class MessageReceiver: IMessageReceiver.Stub() {
        override fun onReceiving(senderName: String?, message: ByteArray?) {
            Log.d("test===", message.toString())
        }

        override fun getReceiverName(): String = "Robot002"
    }
}