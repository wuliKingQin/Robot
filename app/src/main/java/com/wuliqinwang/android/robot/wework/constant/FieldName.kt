package com.wuliqinwang.android.robot.wework.constant

/**
 * @Description: 用来存放属性名的名字
 * @Version: 1.0.0
 */
object FieldName {

    // DES: 消息对象Message里面getInfo里面的属性
    const val MSG_INFO_FIELD_SENDER = "sender"
    const val MSG_INFO_FIELD_CONTENT_TYPE = "contentType"

    // DES: 联系人里面的mUser属性
    const val CONTACT_FIELD_MUSER = "mUser"

    // DES: 用户对象里面详细信息字段
    const val USER_INFO_FIELD_AVATOR_URL = "avatorUrl"
    const val USER_INFO_FIELD_NAME = "name"
    const val USER_INFO_FIELD_REMOTEID = "remoteId"
    const val USER_INFO_FIELD_UNIONID = "unionid"
    const val USER_INFO_FIELD_ACCTID = "acctid"
    const val USER_INFO_FIELD_MOBILE = "mobile"

    // DES: 分享小程序AppMessage需要的字段名
    const val APP_SHARE_FIELD_CONTENT = "content"
    const val APP_SHARE_FIELD_EXTRA = "extra"
    const val APP_SHARE_FIELD_THUMB_DATA = "thumbData"
    const val APP_SHARE_FIELD_USER_NAME = "username"
    // DES: AppMessage.Content类的字段名
    const val APP_SHARE_FIELD_TITLE = "title"
    const val APP_SHARE_FIELD_TYPE = "type"
    const val APP_SHARE_FIELD_APP_BRAND_USER_NAME = "appbrandUsername"
    const val APP_SHARE_FIELD_USER_APP_BRAND_PAGE_PATH = "appbrandPagepath"
    const val APP_SHARE_FIELD_APP_BRAND_APP_ID = "appbrandAppId"
    const val APP_SHARE_FIELD_APP_BRAND_PKG_TYPE = "appbrandPkgType"
    const val APP_SHARE_FIELD_APP_BRAND_VERSION = "appbrandVersion"
    const val APP_SHARE_FIELD_APP_BRAND_PKG_MD5 = "appbrandPkgMD5"
    const val APP_SHARE_FIELD_APP_BRAND_TYPE = "appbrandType"
    const val APP_SHARE_FIELD_URL = "url"
    const val APP_SHARE_FIELD_APP_BRAND_WE_APP_ICON_URL = "appbrandWeAappIconUrl"
    const val APP_SHARE_FIELD_PUBLISHER_ID = "publisherId"
    const val APP_SHARE_FIELD_SRC_USER_NAME = "srcUsername"
    const val APP_SHARE_FIELD_SRC_DISPLAY_NAME = "srcDisplayname"

    // DES: 文件对象的属性flags
    const val FILE_MESSAGE_FIELD_FLAGS = "flags"
}