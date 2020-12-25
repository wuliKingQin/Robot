package com.wuliqinwang.android.robot.wework

import android.app.Application
import android.os.IBinder
import android.util.Log
import com.wuliqinwang.android.robot.hook.Hook
import com.wuliqinwang.android.robot.hook.IHook
import com.wuliqinwang.android.robot.hook.IHookProvider
import com.wuliqinwang.android.robot.message.IMessageReceiver
import com.wuliqinwang.android.robot.message.MessageManger
import com.wuliqinwang.android.robot.wework.utils.*
import de.robv.android.xposed.XC_MethodHook

/**
 * @Description: 用于集中处理企业微信需要配hook的方法
 * @Version: 1.0.0
 */
class WeworkHookProvider: IHookProvider{

    override fun onHooks(): ArrayList<IHook> = arrayListOf(
        mApplicationOnCreateHook
    )

    // DES: 对企业微信的application的onCreate方法进行Hook，
    // 方便拿到企业微信的全局环境，以及Activity的生命周期
    private val mApplicationOnCreateHook by lazy {
        Hook(
            "com.tencent.wework.launch.WwApplicationLike",
            "onCreate",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam?) {
                    param ?: return
                    if (ActivityUtils.globalContent == null) {
                        (param.thisObject as? Application)?.apply {
                            // DES: 保存全局上下文
                            ActivityUtils.globalContent = this
                            // DES: 注册Activity的生命周期回调，用来获取顶部界面上下文
                            registerActivityLifecycleCallbacks(ActivityLifecycleCallbackIml())
                            // DES: 用于后面绑定机器人提供的服务所用
                            applicationOnCreate(this)
                        }
                    }
                }
            })
    }

    // DES: 执行企业微信的Application的 onCreate方法的扩展
    private fun applicationOnCreate(context: Application) {
        MessageManger.registerReceiverToTransitService(context, MessageReceiver())
    }

    // DES: 消息接收器
    class MessageReceiver: IMessageReceiver.Stub() {
        override fun onReceiving(senderName: String?, message: ByteArray?) {
            MessageManger.sendMessage("Robot002", "收到消息")
            val messageContent = message?.toString(Charsets.UTF_8) ?: ""
            Log.d("test====", "$senderName send content: $messageContent")
            UserUtils.getFirstUser {
                Log.d("test====", "${it?.name}")
                ConversationUtils.createConversationByUser(it?.realUser) { id, conversation ->
                    Log.d("test====", "conversation id=${id}")
                    MessageUtils.sendTextMessage(conversation, messageContent, null)
                }
            }
        }

        override fun getReceiverName(): String = "WeWork001"
    }
}