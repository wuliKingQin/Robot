package com.wuliqinwang.android.robot.wework.constant

/**
 * @Description: 用来存放方法的名字
 * @Version: 1.0.0
 */
object MethodName {

    // DES: 构建文本消息的方法
    // DES: 全名：public static WwRichmessage.RichMessage buildTextualMessage(
    // CharSequence charSequence, SendExtraInfo sendExtraInfo)
    const val HQQ_METHOD_BUILD_TEXTUAL_MESSAGE = "buildTextualMessage"

    // DES: 消息对象里面的getInfo方法
    const val MSG_METHOD_GET_INFO = "getInfo"

    // DES: User对象里面的getInfo方法
    const val USER_METHOD_GET_INFO = "getInfo"

    // DES: 消息解析静态类方法
    const val HQI_METHOD_A = "a"
    const val HQI_METHOD_GET_INFO = "getInfo"

    // DES: 线程工具类里静态方法
    const val THREAD_UTILS_METHOD_A = "A"
    const val THREAD_UTILS_METHOD_RUN_ON_MAIN_THREAD = "runOnMainThread"
    const val THREAD_UTILS_METHOD_X = "x"
    const val THREAD_UTILS_METHOD_F = "f"

    // DES: API类的静态方法
    const val API_METHOD_BP = "bp"
    // DES: messager类的方法
    const val MESSAGER_METHOD_A = "a"

    // DES: 发送消息工具类里的静态方法
    const val HQQ_METHOD_TRANSFORM_IMAGE_MESSAGE_TYPE = "transformImageMessageType"
    // DES: 构建文件消息方法b
    const val HQQ_METHOD_BUILD_FILE_MESSAGE_B = "b"

    // DES: ExpressionManager类下面的静态方法Ez
    // DES: public static EmojiInfo m75051Ez(String str)
    const val EXPRESSION_MANAGER_METHOD_EZ = "Ez"
    // DES: BASE_EMOJI_MGR静态方法c
    // DES: public static WwRichmessage.EmotionMessage m61851c(EmojiInfo emojiInfo)
    const val BASE_EMOJI_MGR_METHOD_c = "c"

    // DES: 文件管理器，获取单列的方法
    const val FILE_D_MGR_GET_INSTANCE = "getInstance"
    const val FILE_D_MGR_ADD_DOWNLOAD_TASK = "addDownloadTask"
    // DES: 文件下载请求设置下载连接方法
    const val FILE_D_BUILDER_SET_DOWNLOAD_URL = "setDownloadURL"
    const val FILE_D_BUILDER_BUILD = "build"

    // DES: 会话列表方法
    const val CONVERSATION_LIST_ON_START = "onStart"
    const val CONVERSATION_LIST_UNREGISTER_EVENT = "unregistEvent"
    const val CONVERSATION_LIST_EEF = "eeF"
}