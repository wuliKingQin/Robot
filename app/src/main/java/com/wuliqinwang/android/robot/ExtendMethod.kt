package com.wuliqinwang.android.robot

import com.wuliqinwang.android.robot.hook.ClassLoaderUtils
import com.wuliqinwang.android.robot.hook.Hook
import com.wuliqinwang.android.robot.wework.CallbackHandler
import de.robv.android.xposed.XposedHelpers
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Array.newInstance
import java.lang.reflect.Proxy

/**
 * @Description: 用于对某些类的扩展集中的地方
 * @Version: 1.0.0
 */
//========================================扩展Any方法==========================

// DES: 获取任何对象的属性值
@Suppress("UNCHECKED_CAST")
fun <T> Any?.getAnyField(fieldName: String): T?{
    this ?: return null
    return XposedHelpers.getObjectField(this, fieldName) as? T
}

// DES: 获取静态类的某个属性值
@Suppress("UNCHECKED_CAST")
fun <T> Class<*>?.getStaticAnyField(fieldName: String): T? {
    this ?: return null
    return XposedHelpers.getStaticObjectField(this, fieldName) as? T
}

// DES: 设置某个属性的值
fun Any?.setAnyField(fieldName: String, value: Any?) {
    this ?: return
    XposedHelpers.setObjectField(this, fieldName, value)
}

// DES: 设置静态对象的属性值
fun Class<*>?.setStaticAnyField(fieldName: String, value: Any?) {
    this ?: return
    XposedHelpers.setStaticObjectField(this, fieldName, value)
}

// DES: 调用任何对象的某个方法
@Suppress("UNCHECKED_CAST")
fun <T> Any?.callMethod(methodName: String?, vararg args: Any?): T? {
    this ?: return null
    return XposedHelpers.callMethod(this, methodName, *args) as? T
}

// DES: 调勇静态类的方法
@Suppress("UNCHECKED_CAST")
fun <T> Class<*>?.callStaticMethod(methodName: String?, vararg args: Any?): T? {
    this ?: return null
    return XposedHelpers.callStaticMethod(this, methodName, *args) as? T
}

// DES: 使用静态类的类名去调用静态方法
fun <T> callStaticMethod(className: String, methodName: String?, vararg args: Any?): T? {
    return Hook.findClass(className).callStaticMethod(methodName, *args)
}

// DES: 扩展对于是Array<out Any>数组进行筛选掉指定的类型
inline fun <reified T> Array<out Any?>?.filterOutXX(): Array<out Any?>? {
    this ?: return null
    return this.filter { it !is T }.toTypedArray()
}

// DES: 异常捕获包装器
inline fun <T> tryCatch(noinline exceptionCallback: ((Throwable)->Boolean)? = null, action: ()->T): T? {
    return try {
        action()
    } catch (e: Throwable) {
        val errorInfo = e.stackTrace.joinToString { it.toString().plus("\n") }
        if(exceptionCallback?.invoke(e) == false) {
            e.printStackTrace()
        }
        null
    }
}

// =======================================对文件以及流的对象的扩展函数================================
// DES: 判断文件是否存在做的事情
inline fun File.exist(action: File.() -> Unit): File {
    tryCatch {
        if (exists()) {
            action()
        }
    }
    return this
}

// DES: 判断文件不存在做的事情
inline fun File.notExist(action: File.() -> Unit): File {
    tryCatch {
        if (!exists()) {
            action()
        }
    }
    return this
}

// DES: 将文件转换成文件输出流对象
fun File?.asOutputStream(): FileOutputStream? {
    this ?: return null
    return tryCatch {
        FileOutputStream(this)
    }
}

// DES: 将文件转换成文件输入流对象
fun File?.asInputStream(): FileInputStream? {
    this ?: return null
    return tryCatch {
        FileInputStream(this)
    }
}

// DES: 异常捕获包装器,带自动关闭流
inline fun <T: Closeable> T?.withTryCatch(action: T.() -> Unit) {
    this ?: return
    try {
        action()
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        tryCatch {
            close()
        }
    }
}

/**
 * @Description: 用于扩展某些类的扩展方法
 * @Author: 秦王
 * @Copyright: 浙江集商优选电子商务有限公司
 * @CreateDate: 2020/7/24 10:37
 * @Version: 1.0.0
 */

// DES: 创建某个需要代理的接口回调实例
fun Class<*>?.newCallbackInstance(
    resultCallback: (methodName: String, args: Array<out Any>?) -> Any?
): Any? {
    this ?: return null
    val methodNameList = ArrayList<String>(methods.size)
    methods.forEach {
        methodNameList.add(it.name)
    }
    return newCallbackInstance(methodNameList.toTypedArray(), resultCallback)
}

// DES: 创建代理接口实例
fun Class<*>?.newCallbackInstance(
    proxyMethodList: Array<String>,
    resultCallback: (methodName: String, args: Array<out Any>?) -> Any?
): Any? {
    return Proxy.newProxyInstance(
        ClassLoaderUtils.targetClassLoader,
        arrayOf(this),
        CallbackHandler(proxyMethodList, resultCallback)
    )
}

// DES: 获取字符数组的class
fun Class<*>?.arrayClass(length: Int = 0): Class<*>? {
    return this.arrayObj(length)?.let {
        it::class.java
    }
}

// DES: 创建一个Object数组对象
fun Class<*>?.arrayObj(length: Int): Any? {
    this ?: return null
    return newInstance(this, length)
}

// DES: 构建一个数组class
fun arrayClass(className: String?, length: Int = 0): Class<*>? {
    return arrayObj(className, length)?.let {
        it::class.java
    }
}

// DES: 创建一个Object数组对象
fun arrayObj(className: String?, length: Int = 0): Any? {
    return Hook.findClass(className)?.let { cls ->
        newInstance(cls, length)
    }
}

// DES: 向数组中添加元素
fun Any?.addElement(index: Int, element: Any?): Array<*>? {
    this ?: return null
    if(this is Array<*>) {
        if(index in 0 until size) {
            java.lang.reflect.Array.set(this, index, element)
        }
    }
    return this as? Array<*>
}

