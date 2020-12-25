package com.wuliqinwang.android.robot.wework.constant

/**
 * @Description: 用于统一编写ClassName的字符串
 * @Version: 1.0.0
 */
object ClassName {
    // DES: 以下所有的参数都是针对企业微信版本是3.0.25，
    // 企业微信版本更新后，需要对下面某些参数进行更新，比如hmz或者hmy之类的

    // DES: hook企业微信的WwApplicationLike的onCreate方法
    const val WW_APPLICATION_LIKE = "com.tencent.wework.launch.WwApplicationLike"
    const val ON_CREATE = "onCreate"

    // DES: 用于操作会话的静态类cls, ConversationEngine.class
    const val CONVERSATION_ENGINE_CLASS = "hmz"
    // DES: hook企业微信IConversationObserver接口对应的实例对象
    const val CONVERSATION_OBSERVER_46 = "$CONVERSATION_ENGINE_CLASS\$46"
    const val ON_ADD_MESSAGES = "onAddMessages"
    const val ON_MESSAGE_STATE_CHANGE = "onMessageStateChange"
    const val ON_ADD_MEMBERS = "onAddMembers"
    const val ON_REMOVE_MEMBERS = "onRemoveMembers"
    const val CONVERSATION = "com.tencent.wework.foundation.model.Conversation"
    const val MESSAGE = "com.tencent.wework.foundation.model.Message"
    // DES: 用于构造会话的静态方法a ConversationEngine静态方法a
    const val CONVERSATION_ENGINE_METHOD_A = "a"
    // DES: 创建会话用户的回调接口
    const val COMMON_CONVERSATION_OPERATE_CALLBACK  = "com.tencent.wework.foundation.callback.ICommonConversationOperateCallback"
    // DES: 回调方法的名字
    // DES: public void onResult(int i, Conversation conversation, String str)
    const val CONVERSATION_CALLBACK_METHOD_NAME = "onResult"

    // DES: 会话的Item， ConversationItem.class
    const val CONVERSATION_ITEM_CLASS = "hnb"
    const val GET_CONVERSATION_ID = "getConvsationId"

    // DES: 发送消息的静态类以及方法 MessageManager.class
    const val MESSAGE_MANAGER_CLASS = "hqq"
    // DES: MessageManager的单列方法
    const val EIM = "eIm"
    // DES: 发送文本消息的方法
    const val SEND_TEXTUAL_MESSAGE = "sendTextualMessage"
    // DES: 发送图片的消息方法
    const val SEND_IMAGE_MESSAGE = "sendImageMessage"
    // DES: 发送所有消息的方法
    const val SEND_MESSAGE = "sendMessage"
    // DES: 发送消息回调接口
    const val SEND_MESSAGE_CALLBACK = "com.tencent.wework.foundation.callback.ISendMessageCallback"
    // DES: 回调方法名onProgress
    const val M_METHOD_ON_PROGRESS = "onProgress"
    // DES: 回调结果方法名
    const val M_METHOD_ON_RESULT = "onResult"
    // DES: 构建链接消息对象的方法名
    const val BUILD_LINK_MESSAGE = "buildLinkMessage"
    // DES: 内部消息class
    const val WW_MESSAGE_CLASS = "com.tencent.wework.foundation.model.pb.WwMessage\$Message"
    // DES: MessageNano消息类名
    const val MESSAGE_NANO_CLASS = "com.google.protobuf.nano.MessageNano"

    // DES: 构造发送文本方法需要用的属性以及方法名等
    const val METHOD_ECN = "ecn"
    const val METHOD_ECX = "ecx"
    const val METHOD_TO_BYTE_ARRAY = "toByteArray"
    const val METHOD_NEW_MESSAGE = "NewMessage"
    const val METHOD_GET_TIME_STAMP = "getTimestamp"
    const val METHOD_A = "a"
    const val METHOD_OB = "oB"
    const val METHOD_ECR = "ecr"
    const val METHOD_ECT = "ect"
    const val METHOD_ECM = "ecm"
    const val METHOD_ECS = "ecs"
    const val METHOD_ECO = "eco"
    const val METHOD_SET_INFO = "setInfo"
    const val FIELD_FLAG = "flag"
    const val FIELD_CONTENT_TYPE = "contentType"
    const val FIELD_CONTENT = "content"
    const val FIELD_EXTRA_CONTENT = "extraContent"
    const val FIELD_NNA = "nNA"

    // DES: 获取我的客户联系人信息
    // DES: com.tencent.wework.friends.controller.OutFriendListActivity
    // 类下面的updateListData()方法可查询以下信息
    // DES: 原始类名叫FriendDataHelper.class
    const val FRIEND_DATA_HELPER_CLASS = "guc"
    // DES: 获取联系人的接口回调
    const val FRIEND_DATA_HELPER_CALLBACK = "guc\$c"
    // DES: 获取联系人的接口返回方法
    const val FRIEND_DATA_HELPER_METHOD_A = "a"

    // DES: 用户对象class
    const val USER_CLASS = "com.tencent.wework.foundation.model.User"
    // DES: 用户对象的getInfo方法用于获取WwUser.User对象
    const val USER_METHOD_GET_INFO = "getInfo"
    // DES: 用来判断是否含有微信信息的方法hasWechatInfo
    const val USER_METHOD_HAS_WECHAT_INFO = "hasWechatInfo"
    // DES: 模块工具静态类
    const val MODULE_STATIC_CLASS = "com.tencent.wecomponent.MK"
    // DES: 获取对象模块的服务的静态方法
    const val MODULE_METHOD_SERVICE = "service"
    // DES: 账户接口对象class
    const val ACCOUNT_CLASS = "com.tencent.wework.login.api.IAccount"
    // DES: 账户接口里面的getLoginUser方法
    const val ACCOUNT_METHOD_GET_LOGIN_USER = "getLoginUser"
    // DES: 判断当前的账号是否登录
    const val ACCOUNT_METHOD_IS_LOGIN = "isCurrentProfileLogin"
    // DES: 这个类从ConversationEngineDefine编译过来
    const val CONVERSATION_ENGINE_DEFINE_CLASS = "hna\$c"
    // DES: 判断用户是否是登录用户
    const val CONVERSATION_ENGINE_DEFINE_METHOD_IS_SELF = "isSelf"

    // DES: 解析消息的静态类MessageItem.class
    const val MESSAGE_ITEM_CLASS = "hqi"

    // DES: 线程工具类
    // 如果有变化直接搜索ThreadUtils
    const val THREAD_UTILS_CLASS = "exw"

    // DES: 发送小程序的类
    const val JS_API_SHARE_APP_MESSAGE_BUNDLE_CLASS = "com.tencent.mm.plugin.appbrand.jsapi.override.JsApiShareAppMessageBundle"
    const val API_CLASS = "com.tencent.wework.api.API"
    const val MESSAGER_CLASS = "com.tencent.wework.api.account.Messager"
    const val APP_MESSAGE_CONTENT_CLASS = "com.tencent.mm.message.AppMessage\$Content"

    // DES: 表情管理器类ExpressionManager.class
    const val EXPRESSION_MANAGER_CLASS = "hob"

    // DES: 基类表情管理器 BaseEmojiMgr.class
    const val BASE_EMOJI_MGR_CLASS = "dil"

    // DES: 文件下载管理器
    const val FILE_DOWNLOAD_MANAGER_CLASS = "com.tencent.mm.plugin.downloader.model.FileDownloadManager"
    const val FILE_DOWNLOAD_REQUEST_BUILDER_CLASS = "com.tencent.mm.plugin.downloader.model.FileDownloadRequest\$Builder"
    const val FILE_DOWNLOAD_CALLBACK_CLASS = "com.tencent.mm.plugin.downloader.model.IFileDownloadCallback"

    // DES: 会话列表
    const val CONVERSATION_LIST_FRAGMENT_CLASS = "com.tencent.wework.msg.controller.ConversationListFragment"
}