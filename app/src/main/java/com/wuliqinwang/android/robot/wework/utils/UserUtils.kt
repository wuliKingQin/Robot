package com.wuliqinwang.android.robot.wework.utils

import com.wuliqinwang.android.robot.callMethod
import com.wuliqinwang.android.robot.callStaticMethod
import com.wuliqinwang.android.robot.getAnyField
import com.wuliqinwang.android.robot.hook.Hook
import com.wuliqinwang.android.robot.tryCatch
import com.wuliqinwang.android.robot.wework.constant.ClassName
import com.wuliqinwang.android.robot.wework.constant.FieldName
import com.wuliqinwang.android.robot.wework.constant.MethodName
import java.util.concurrent.ConcurrentHashMap

/**
 * @Description: 用于封装用户操作的工具类
 * @Version: 1.0.0
 */
object UserUtils {

    // DES: 用于读取缓存的操作锁
    private val mLock = Any()
    // DES: 缓存联系人信息
    private val mUserMap = ConcurrentHashMap<Long, User>(100)

    // DES: 获取登录用户对象
    // DES: 返回对象为User实例
    fun getLoginUser(): Any? {
        return Hook.findClass(ClassName.MODULE_STATIC_CLASS)
            .callStaticMethod<Any>(
                ClassName.MODULE_METHOD_SERVICE,
                Hook.findClass(ClassName.ACCOUNT_CLASS)
            ).callMethod(ClassName.ACCOUNT_METHOD_GET_LOGIN_USER)
    }

    // DES: 用来判断企业微信是否登录
    fun isCurrentProfileLogin(): Boolean {
        return Hook.findClass(ClassName.MODULE_STATIC_CLASS)
            .callStaticMethod<Any>(
                ClassName.MODULE_METHOD_SERVICE,
                Hook.findClass(ClassName.ACCOUNT_CLASS)
            ).callMethod<Boolean>(ClassName.ACCOUNT_METHOD_IS_LOGIN) ?: false
    }

    // DES: 判断是否是自己
    fun isSelf(user: Any): Boolean {
        val innerIsSelf = Hook.findClass(ClassName.CONVERSATION_ENGINE_DEFINE_CLASS)
            .callStaticMethod<Any>(ClassName.CONVERSATION_ENGINE_DEFINE_METHOD_IS_SELF, user)
        return if(innerIsSelf is Boolean) {
            innerIsSelf
        } else {
            false
        }
    }

    // DES: 用来判断是否有微信信息
    fun hasWechatInfo(user: Any?): Boolean {
        val isHasWechatInfo = user.callMethod<Any>(ClassName.USER_METHOD_HAS_WECHAT_INFO)
        return if(isHasWechatInfo is Boolean) {
            isHasWechatInfo
        } else {
            false
        }
    }

    // DES: 用于初始话用户缓存信息
    private fun initCacheUser(result: (()->Unit)? = null) {
        // DES: 添加登录自己的用户
        addCacheUser(
            getLoginUser()
        )
        // DES: 添加我的客户下面的联系人信息到缓存
        ContactUtils.getMyClientContacts { contactMap ->
            synchronized(mLock) {
                contactMap?.forEach { contactEntry ->
                    (contactEntry.value as? List<*>)?.forEach { contactItem ->
                        addCacheUser(
                            contactItem.getAnyField<Any>(FieldName.CONTACT_FIELD_MUSER)
                        )
                    }
                }
                result?.invoke()
            }
        }
    }

    // DES: 添加用户信息到缓存
    private fun addCacheUser(realUser: Any?) {
        realUser?.let { user ->
            val outUer = User()
            user.callMethod<Any>(MethodName.USER_METHOD_GET_INFO)?.let { userInfo ->
                outUer.realUser = user
                outUer.avatorUrl = userInfo.getAnyField<String>(FieldName.USER_INFO_FIELD_AVATOR_URL)
                outUer.name = userInfo.getAnyField<String>(FieldName.USER_INFO_FIELD_NAME)
                outUer.remoteId = userInfo.getAnyField<Long>(FieldName.USER_INFO_FIELD_REMOTEID) ?: 0
                outUer.unionid = userInfo.getAnyField<String>(FieldName.USER_INFO_FIELD_UNIONID)
                mUserMap[outUer.remoteId] = outUer
            }
        }
    }

    // DES: 通过远程Id获取对象的用户信息
    fun getUserByRemoteId(remoteId: Long): User? {
        return mUserMap[remoteId]
    }

    // DES: 异步查询用户
    fun queryUsersWithAsync(remoteIds: List<String>?, result: (List<Any>) -> Unit) {
        val userList = ArrayList<Any>(remoteIds?.size ?: 0)
        if(!remoteIds.isNullOrEmpty()) {
            val remoteCacheIds = mUserMap.keys().toList().filter {
                it.toString() in remoteIds
            }
            when {
                remoteCacheIds.size == remoteIds.size -> {
                    remoteCacheIds.addTargetUserToList(userList)
                    result(userList)
                }
                remoteCacheIds.isNotEmpty() -> {
                    remoteCacheIds.addTargetUserToList(userList)
                    val remoteNoCacheIds = remoteIds.filter {
                        tryCatch {
                            it.toLong() !in remoteCacheIds
                        } ?: false
                    }
                    if(remoteNoCacheIds.isNotEmpty()) {
                        initCacheUser {
                            mUserMap.keys()
                                .toList().filter {
                                it.toString() in remoteNoCacheIds
                            }.addTargetUserToList(userList)
                            result(userList)
                        }
                    } else {
                        result(userList)
                    }
                }
                else -> {
                    initCacheUser {
                        mUserMap.keys()
                            .toList().filter {
                            it.toString() in remoteIds
                        }.addTargetUserToList(userList)
                        result(userList)
                    }
                }
            }
        } else {
            result(userList)
        }
    }

    // DES: 添加目标用户到列表
    private fun List<Long>?.addTargetUserToList(userList: ArrayList<Any>) {
        this ?: return
        forEach { remoteId ->
            mUserMap[remoteId]?.let { outUser ->
                outUser.realUser?.let { realUser ->
                    userList.add(realUser)
                }
            }
        }
    }

    // DES: 通过远程Id异步获取用户信息
    fun queryUserWithAsync(remoteId: Long, result: (User?)-> Unit){
        getUserWithAsync({
            getUserByRemoteId(
                remoteId
            )
        }, result)
    }

    // DES: 通过头像和用户名来获取已经存在的用户信息
    fun getUserByAvatarUrlAndName(avatarUrl: String?, name: String?): User? {
        return mUserMap.values.firstOrNull{ it.avatorUrl == avatarUrl
                && it.name == name }
    }

    // DES: 获取我的客户的第一个用户
    fun getFirstUser(result: (User?)-> Unit) {
        getUserWithAsync({
            mUserMap.values.toList().let {
                if (it.isNotEmpty()) {
                    it[1]
                } else {
                    null
                }
            }
        }, result)
    }

    // DES: 异步获取联系人信息
    fun queryUserWithAsync(avatarUrl: String?, name: String?, result: (User?) -> Unit) {
        getUserWithAsync({
            getUserByAvatarUrlAndName(
                avatarUrl,
                name
            )
        }, result)
    }

    // DES: 异步获取用户信息
    private fun getUserWithAsync(action: ()-> User?, result: (User?) -> Unit) {
        var outUser = action()
        if(outUser == null) {
            initCacheUser {
                outUser = action()
                result(outUser)
            }
        } else {
            result(outUser)
        }
    }
}

// DES: 自己封装的用户信息
data class User(
    // DES: 用户头像
    var avatorUrl: String? = null,
    // DES: 用户名
    var name: String? = null,
    // DES: 用户远程ID
    var remoteId: Long = 0L,
    // DES: 用户unionId
    var unionid: String? = null,
    // DES: 真实用户实例，可以使用它来创建会话
    var realUser: Any? = null
)