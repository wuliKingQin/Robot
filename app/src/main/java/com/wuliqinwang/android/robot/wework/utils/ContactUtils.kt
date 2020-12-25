package com.wuliqinwang.android.robot.wework.utils

import com.wuliqinwang.android.robot.callStaticMethod
import com.wuliqinwang.android.robot.hook.Hook
import com.wuliqinwang.android.robot.newCallbackInstance


/**
 * @Description: 用于封装获取企业微信联系人的静态方法
 * @Version: 1.0.0
 */
object ContactUtils {

    // DES: 获取到我的客户下面的联系人
    fun getMyClientContacts(result: (contactMap: Map<*, *>?) -> Unit) {
        val callback = Hook.findClass("guc\$c")
            .newCallbackInstance{ _, args ->
                result(args?.get(1) as? Map<*, *>)
            }
        // DES: 我的客户群组Id是18
        val groupId = 18
        Hook.findClass("guc")
            .callStaticMethod<Any>(
                "a",
                groupId,
                callback
            )
    }
}