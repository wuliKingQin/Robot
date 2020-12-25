package com.wuliqinwang.android.robot.wework

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

/**
 * @Description: 实现一个代理的回调处理类
 * @Version: 1.0.0
 */
class CallbackHandler(
    private val methodNameList: Array<String>,
    private var result: (methodName: String, args: Array<out Any>?) -> Any?
): InvocationHandler{
    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
        val name = method?.name ?: ""
        return when {
            methodNameList.contains(name) -> result(name, args)
            method?.returnType == String::class.java -> ""
            method?.returnType == Boolean::class.java -> false
            method?.returnType == Int::class.java -> 0
            method?.returnType == Float::class.java -> 0f
            method?.returnType == Double::class.java -> 0f
            method?.returnType == Long::class.java -> 0
            method?.returnType == Byte::class.java -> 0
            else -> null
        }
    }
}