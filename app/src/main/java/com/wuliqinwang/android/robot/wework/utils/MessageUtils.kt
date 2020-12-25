package com.wuliqinwang.android.robot.wework.utils

import android.content.Context
import com.wuliqinwang.android.robot.*
import com.wuliqinwang.android.robot.hook.Hook
import com.wuliqinwang.android.robot.wework.constant.ClassName
import com.wuliqinwang.android.robot.wework.constant.MethodName

/**
 * @Description: 用于发送消息发送的工具类
 * @Version: 1.0.0
 */
object MessageUtils {

    // DES: 发送状态其他造成的失败
    const val SEND_STATE_FAIL = -1

    // DES: 发送消息文本
    fun sendTextMessage(
        conversation: Any?,
        text: CharSequence?,
        sendCallback: ISendMessageCallback?
    ) {
        if (!text.isNullOrEmpty() && conversation != null) {
            val textMessage =
                buildTextualMessage(
                    text,
                    null
                )
            if (textMessage != null) {
                sendTextMessage(
                    conversation,
                    textMessage,
                    null,
                    sendCallback
                )
            } else {
                sendCallback?.onResult(SEND_STATE_FAIL, conversation, null)
            }
        } else {
            sendCallback?.onResult(SEND_STATE_FAIL, conversation, null)
        }
    }

    /**
     * DES:封装发送文本的消息方法，并带有回调方法
     * AUTHOR: 秦王
     * TIME: 2020/7/28 17:44
     * 构造的方法是public static boolean sendTextualMessage(
     * Context context, long j, WwRichmessage.RichMessage richMessage,
     * SendExtraInfo sendExtraInfo, ISendMessageCallback iSendMessageCallback)
     * @param conversation 会话对象
     * @param richMessage 富文本消息对象
     * @param sendExtraInfo 发送扩展信息
     * @param sendCallback 发送回调
     **/
    private fun sendTextMessage(
        conversation: Any,
        richMessage: Any,
        sendExtraInfo: Any?,
        sendCallback: ISendMessageCallback?
    ) {
        val message = Hook.findClass(ClassName.WW_MESSAGE_CLASS)?.newInstance()
        if (message == null) {
            sendCallback?.onResult(SEND_STATE_FAIL, conversation, null)
            return
        }
        var flag: Int
        if (sendExtraInfo != null && sendExtraInfo.callMethod<Boolean>(ClassName.METHOD_ECN) == true) {
            flag = message.getAnyField<Int>(ClassName.FIELD_FLAG) ?: 0
            message.setAnyField(ClassName.FIELD_FLAG, flag.or(2))
            if (sendExtraInfo.callMethod<Boolean>(ClassName.METHOD_ECX) == true) {
                flag = message.getAnyField<Int>(ClassName.FIELD_FLAG) ?: 0
                message.setAnyField(ClassName.FIELD_FLAG, flag.or(131072))
            }
        }
        message.setAnyField(ClassName.FIELD_CONTENT_TYPE, 0)
        val byteArrayMessage = Hook.findClass(ClassName.MESSAGE_NANO_CLASS)
            .callStaticMethod<ByteArray>(ClassName.METHOD_TO_BYTE_ARRAY, richMessage)
        message.setAnyField(ClassName.FIELD_CONTENT, byteArrayMessage)
        val newMessage = Hook.findClass(ClassName.MESSAGE)
            .callStaticMethod<Any>(ClassName.METHOD_NEW_MESSAGE)
        if (sendExtraInfo != null) {
            val timestamp = sendExtraInfo.callMethod<Int>(ClassName.METHOD_GET_TIME_STAMP) ?: 0
            if (timestamp > 0) {
                flag = message.getAnyField<Int>(ClassName.FIELD_FLAG) ?: 0
                message.setAnyField(ClassName.FIELD_FLAG, flag.or(128))
                flag = message.getAnyField<Int>(ClassName.FIELD_FLAG) ?: 0
                message.setAnyField(ClassName.FIELD_FLAG, flag.and(-65))
                val extraContext = Hook.findClass(ClassName.MESSAGE_MANAGER_CLASS)
                    .callStaticMethod<Any>(
                        ClassName.METHOD_A,
                        Hook.findClass(ClassName.MESSAGE_MANAGER_CLASS)
                            .callStaticMethod(ClassName.METHOD_OB, timestamp)
                    )
                message.setAnyField(ClassName.FIELD_EXTRA_CONTENT, extraContext)
            }
            if (sendExtraInfo.callMethod<Boolean>(ClassName.METHOD_ECO) == true) {
                flag = message.getAnyField<Int>(ClassName.FIELD_FLAG) ?: 0
                message.setAnyField(ClassName.FIELD_FLAG, flag.or(-2048))
            }
            if (sendExtraInfo.callMethod<Boolean>(ClassName.METHOD_ECR) == true) {
                flag = message.getAnyField<Int>(ClassName.FIELD_FLAG) ?: 0
                message.setAnyField(ClassName.FIELD_FLAG, flag.or(-512))
                val extraContext = Hook.findClass(ClassName.MESSAGE_MANAGER_CLASS)
                    .callStaticMethod<Any>(
                        ClassName.METHOD_A,
                        message,
                        sendExtraInfo.callMethod(ClassName.METHOD_ECT)
                    )
                message.setAnyField(ClassName.FIELD_EXTRA_CONTENT, extraContext)
            }
            if (sendExtraInfo.callMethod<Boolean>(ClassName.METHOD_ECM) == true) {
                flag = message.getAnyField<Int>(ClassName.FIELD_FLAG) ?: 0
                message.setAnyField(ClassName.FIELD_FLAG, flag.or(134217728))
            }
            if (sendExtraInfo.callMethod<Boolean>(ClassName.METHOD_ECS) == true) {
                val extraContext = Hook.findClass(ClassName.MESSAGE_MANAGER_CLASS)?.let {
                    it.callStaticMethod<Any>(
                        ClassName.METHOD_A,
                        message,
                        it.getStaticAnyField<Long>(ClassName.FIELD_NNA)
                    )
                }
                message.setAnyField(ClassName.FIELD_EXTRA_CONTENT, extraContext)
            }
        }
        newMessage.callMethod<Any>(ClassName.METHOD_SET_INFO, message)
        sendMessage(
            conversation,
            newMessage,
            sendExtraInfo,
            sendCallback
        )
    }

    // DES: 发送视频消息
    fun sendVideoMessage(
        conversation: Any?,
        videoUrl: String?,
        previewImgUrl: String?,
        sendCallback: ISendMessageCallback?
    ) {
        val videoMessage =
            buildVideoMessage(
                videoUrl,
                previewImgUrl
            )
        sendFileMessage(
            conversation,
            5,
            videoMessage,
            sendCallback
        )
    }

    // DES: 发送文件消息
    fun sendFileMessage(conversation: Any?, filePath: String?, sendCallback: ISendMessageCallback?) {
        val fileMessage =
            buildFileMessage(
                filePath,
                0,
                0,
                0,
                false
            )
        sendFileMessage(
            conversation,
            8,
            fileMessage,
            sendCallback
        )
    }

    // DES: 构建一个视频消息
    private fun buildVideoMessage(videoUrl: String?, previewImgUrl: String?): Any? {
        return Hook.findClass(ClassName.MESSAGE_MANAGER_CLASS)
            .callStaticMethod<Any>(
                "buildVideoMessage",
                videoUrl,
                previewImgUrl
            )
    }

    // DES: 构建表情消息
    private fun buildEmotionMessage(filePath: String?): Any? {
        val emojiInfo = Hook.findClass(ClassName.EXPRESSION_MANAGER_CLASS)
            .callStaticMethod<Any>(MethodName.EXPRESSION_MANAGER_METHOD_EZ, filePath)
        return Hook.findClass(ClassName.BASE_EMOJI_MGR_CLASS)
            .callStaticMethod(MethodName.BASE_EMOJI_MGR_METHOD_c, emojiInfo)
    }

    // DES: 构建一个文件消息
    private fun buildFileMessage(
        filePath: String?,
        width: Int,
        height: Int,
        voiceTime: Int,
        isHd: Boolean
    ): Any? {
        return Hook.findClass(ClassName.MESSAGE_MANAGER_CLASS)
            .callStaticMethod<Any>(
                MethodName.HQQ_METHOD_BUILD_FILE_MESSAGE_B,
                filePath,
                width,
                height,
                voiceTime,
                isHd
            )
    }

    // DES: 转换图片消息类型
    private fun transformImageMessageType(fileMessage: Any?): Int {
        return Hook.findClass(ClassName.MESSAGE_MANAGER_CLASS)
            .callStaticMethod(
                MethodName.HQQ_METHOD_TRANSFORM_IMAGE_MESSAGE_TYPE,
                fileMessage
            ) ?: 7
    }

    // DES: 封装发送文件的消息(包括视频，图片，文件等)
    private fun sendFileMessage(
        conversation: Any?,
        contentType: Int,
        fileMessage: Any?,
        sendCallback: ISendMessageCallback?
    ) {
        if(conversation != null) {
            val newMessage =
                buildNewMessage(
                    contentType,
                    fileMessage
                )
            sendMessage(
                conversation,
                newMessage,
                null,
                sendCallback
            )
        } else {
            sendCallback?.onResult(SEND_STATE_FAIL, null, fileMessage)
        }
    }

    // DES: 构建一个新的消息对象
    private fun buildNewMessage(contentType: Int, message: Any?): Any? {
        return Hook.findClass(ClassName.WW_MESSAGE_CLASS)
            ?.newInstance()?.let { innerMessage ->
                innerMessage.setAnyField(ClassName.FIELD_CONTENT_TYPE, contentType)
                innerMessage.setAnyField(
                    ClassName.FIELD_CONTENT, Hook.findClass(ClassName.MESSAGE_NANO_CLASS)
                        .callStaticMethod(ClassName.METHOD_TO_BYTE_ARRAY, message)
                )
                val newMessage = Hook.findClass(ClassName.MESSAGE)
                    .callStaticMethod<Any>(ClassName.METHOD_NEW_MESSAGE)
                newMessage.callMethod<Any>(ClassName.METHOD_SET_INFO, innerMessage)
                newMessage
            }
    }

    /**
     * DES: 封装发送任何消息的方法，并带有回调方法
     * AUTHOR: 秦王
     * TIME: 2020/7/28 17:19
     *
     * @param conversation 会话对象
     * @param message 消息对象
     * @param sendExtraInfo 发送扩展信息
     * @param sendCallback 发送回调
     **/
    private fun sendMessage(
        conversation: Any,
        message: Any?,
        sendExtraInfo: Any?,
        sendCallback: ISendMessageCallback?
    ) {
        val isSuccess =
            sendMessageWrapper { context ->
                val sendMessageCallback = Hook.findClass(ClassName.SEND_MESSAGE_CALLBACK)
                    .newCallbackInstance { methodName, args ->
                        sendCallback?.let { callback ->
                            args?.let { it ->
                                if (it.size >= 3) {
                                    if (methodName == ClassName.M_METHOD_ON_PROGRESS) {
                                        val progress = it[1] as? Long ?: 0L
                                        val allSize = it[2] as? Long ?: 0L
                                        callback.onProgress(it[0], progress, allSize)
                                    } else if (methodName == ClassName.M_METHOD_ON_RESULT) {
                                        val sendState = it[0] as? Int ?: -1
                                        callback.onResult(sendState, it[1], it[2])
                                    }
                                }
                            }
                        }
                    }
                // DES: 调用发送消息的方法
                Hook.findClass(ClassName.MESSAGE_MANAGER_CLASS)
                    .callStaticMethod<Any>(
                        ClassName.SEND_MESSAGE,
                        context,
                        conversation,
                        message,
                        sendExtraInfo,
                        sendMessageCallback
                    )
            }
        if (!isSuccess) {
            // DES: 发送失败的回调
            sendCallback?.onResult(SEND_STATE_FAIL, conversation, message)
        }
    }

    // DES: 用于包装发送消息方法
    private inline fun sendMessageWrapper(actionBlock: (Context) -> Unit): Boolean {
        return tryCatch {
            val context = ActivityUtils.getTopActivity()?.let {
                actionBlock(it)
                it
            }
            context != null
        } ?: false
    }

    /**
     * DES: 用于构建链接消息对象
     * AUTHOR: 秦王
     * TIME: 2020/7/28 17:31
     *
     * @param linkUrl 链接地址
     * @param title 标题
     * @param description 描述
     * @param imageUrl 图片地址
     * @param imageDat 图片字节数据
     * @return 返回链接消息对象
     **/
    fun buildLinkMessage(
        linkUrl: String,
        title: String,
        description: String,
        imageUrl: String,
        imageDat: ByteArray
    ): Any? {
        return Hook.findClass(ClassName.MESSAGE_MANAGER_CLASS)
            .callStaticMethod(
                ClassName.BUILD_LINK_MESSAGE,
                title,
                linkUrl,
                description,
                imageUrl,
                imageDat
            )
    }

    // DES: 构建文本消息的方法
    fun buildTextualMessage(text: CharSequence, sendExtraInfo: Any?): Any? {
        return Hook.findClass(ClassName.MESSAGE_MANAGER_CLASS)
            .callStaticMethod<Any>(
                MethodName.HQQ_METHOD_BUILD_TEXTUAL_MESSAGE,
                text,
                sendExtraInfo
            )
    }

    // DES: 解析文本类型的消息
    fun parseTextMessage(message: Any?): CharSequence? {
        return Hook.findClass(ClassName.MESSAGE_ITEM_CLASS)
            .callStaticMethod<CharSequence>(
                MethodName.HQI_METHOD_A,
                message.callMethod(MethodName.HQI_METHOD_GET_INFO),
                null
            )
    }

    // DES: 发送消息回调监听器
    interface ISendMessageCallback {
        // DES: 进度方法
        fun onProgress(message: Any?, progress: Long, allLength: Long) {
        }

        // DES: 发送成功或者失败的方法sendState 为0表示成功，否则失败
        fun onResult(sendState: Int, conversation: Any?, message: Any?)
    }
}

// DES: 微信发送到企业微信的消息类型
interface WxMsgType {
    companion object {
        // DES: 文本消息
        const val TEXT = 2

        // DES: 位置类型
        const val LOCATION = 6

        // DES: 收藏类型
        const val FAVORITES = 13

        // DES: 语音类型
        const val VOICE = 16

        // DES: 个人名片类型
        const val PERSONAL_BUSINESS_CARD = 41

        // DES: 图片类型
        const val IMAGE = 101

        // DES: 文件类型
        const val FILE = 102

        // DES: 视频类型
        const val VIDEO = 103

        // DES: 表情类型
        const val EXPRESSION = 104
    }
}

// DES: 企业微信发送消息里面的消息类型
interface WwMsgType {
    companion object {
        // DES: 文本类型
        const val TEXT = 0

        // DES: 位置类型
        const val LOCATION = 6

        // DES: 收藏类型
        const val FAVORITES = 13

        // DES: 语音类型
        const val VOICE = 9

        // DES: 个人名片类型
        const val PERSONAL_BUSINESS_CARD = 41

        // DES: 图片类型
        const val IMAGE = 7

        // DES: 文件类型
        const val FILE = 8

        // DES: 视频类型
        const val VIDEO = 103

        // DES: 表情类型
        const val EXPRESSION = 29
    }
}