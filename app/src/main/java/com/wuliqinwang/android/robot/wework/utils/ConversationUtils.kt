package com.wuliqinwang.android.robot.wework.utils

import com.wuliqinwang.android.robot.*
import com.wuliqinwang.android.robot.hook.Hook
import com.wuliqinwang.android.robot.wework.constant.ClassName


/**
 * @Description: 用于封装企业微信创建会话Id的工具类
 * @Version: 1.0.0
 */
object ConversationUtils {


    // DES: 发送企微消息的轮询时间ms
    const val SEND_WW_MSG_TASK_INTERVAL_TIME_MS = 50L

    // DES: 通过用户对象来创建用户Id
    @JvmStatic
    inline fun createConversationByUser(
        user: Any?,
        crossinline callback: (conversationId: Long, conversation: Any?) -> Unit
    ) {
        val userArr: Array<*>?
        var userArr2: Array<*>? = null
        if (user == null || user.callMethod<Any>(ClassName.USER_METHOD_GET_INFO) == null) {
            userArr = null
            UserUtils.getLoginUser()
                ?.let { loginUser ->
                userArr2 = arrayObj(ClassName.USER_CLASS, length = 1)
                    .addElement(0, loginUser)
            }
        } else if (UserUtils.isSelf(user)) {
            userArr = null
            userArr2 = arrayObj(ClassName.USER_CLASS, length = 1)
                .addElement(0, user)
        } else if (UserUtils.hasWechatInfo(
                user
            )
        ) {
            userArr = arrayObj(ClassName.USER_CLASS, length = 1)
                .addElement(0, user)
            UserUtils.getLoginUser()
                ?.let { loginUser ->
                userArr2 = arrayObj(ClassName.USER_CLASS, length = 1)
                    .addElement(0, loginUser)
            }
        } else {
            userArr = null
            UserUtils.getLoginUser()
                ?.let { loginUser ->
                userArr2 = arrayObj(ClassName.USER_CLASS, length = 2)
                    .addElement(0, user)
                    .addElement(1, loginUser)
            }
        }
        createConversation(
            userArr,
            userArr2,
            callback
        )
    }

    // DES: 创建群发会话
    fun createConversation(userList: Array<*>, callback: (conversationId: Long, conversation: Any?) -> Unit) {
        var tempUserList: Array<*>? = userList
        UserUtils.getLoginUser()
            ?.let { loginUser ->
            tempUserList = arrayObj(ClassName.USER_CLASS, length = userList.size + 1)
                .addElement(userList.size, loginUser)
        }
        createConversation(
            null,
            tempUserList,
            callback
        )
    }

    // DES: 用于创建会话的方法，再发信息的时候需要调用该方法
    @JvmStatic
    inline fun createConversation(
        userArr: Array<*>?,
        userArr2: Array<*>?,
        crossinline callback: (conversationId: Long, conversation: Any?) -> Unit
    ) {
        val conversationCallback = Hook.findClass(ClassName.COMMON_CONVERSATION_OPERATE_CALLBACK)
            .newCallbackInstance{ _, args ->
                val conversationObj = args?.get(1)
                callback.invoke(
                    getConversationId(
                        conversationObj
                    ), conversationObj)
            }
        Hook.findClass(ClassName.CONVERSATION_ENGINE_CLASS)
            .callStaticMethod<Any>(
                ClassName.CONVERSATION_ENGINE_METHOD_A,
                "",
                userArr2,
                userArr,
                0L,
                null,
                null,
                conversationCallback
            )
    }

    // DES: 使用会话对象来获取会话Id
    @JvmStatic
    fun getConversationId(conversationObj: Any?): Long {
        return Hook.findClass(ClassName.CONVERSATION_ITEM_CLASS)
            .callStaticMethod(
                ClassName.GET_CONVERSATION_ID,
                conversationObj
            ) as? Long ?: 0
    }
}