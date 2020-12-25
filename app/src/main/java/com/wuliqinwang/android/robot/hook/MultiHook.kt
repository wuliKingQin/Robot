package com.wuliqinwang.android.robot.hook

import java.util.*
import kotlin.NoSuchElementException

/**
 * @Description: 用于进行多个方法hook的类，比如一个类下面的多个方法
 * @Version: 1.0.0
 */
open class MultiHook(
    // DES: 类名
    className: String?,
    // DES: 用于寻找类的ClassLoader
    classLoader: ClassLoader?,
    // DES: 用于添加hook的方法
    private var addHookMethods: MultiHook.()-> Unit
): Hook(className, classLoader){

    // DES: 只需要提供类名的构造器
    constructor(
        // DES: 类名
        className: String?,
        // DES: 用于添加hook的方法
        addHookMethods: MultiHook.()-> Unit
    ): this(className, null as ClassLoader?, addHookMethods)

    constructor(
        // DES: 类名
        className: String?,
        // DES: 父类的类名
        parentClassName: String?,
        // DES: 用于添加hook的方法
        addHookMethods: MultiHook.()-> Unit
    ): this(className, findClass(parentClassName)?.classLoader, addHookMethods)

    companion object {
        // DES: 默认的切割符号
        private const val DEFAULT_SPLIT_SYMBOL = "/"
        // DES: 用于hook构造器使用键
        private const val HOOK_CONSTRUCTOR_KEY = "Constructor$DEFAULT_SPLIT_SYMBOL"
    }

    // DES: 用于标志名字相同参数不同的hook
    private var mRepeatKeyIndex = 0
    // DES: 用于保存hook类方法的map数据
    private val mMethodMap: WeakHashMap<String, Array<out Any?>> = WeakHashMap(8)

    // DES: 添加需要hook的方法以及参数
    fun addMethod(methodName: String, vararg paramsTypesAndCallback: Any?){
        val paramsTypes = mMethodMap[methodName]
        if(paramsTypes != null && !paramsTypes.contentDeepEquals(paramsTypesAndCallback)) {
            val tempMethod = "$methodName$DEFAULT_SPLIT_SYMBOL$mRepeatKeyIndex"
            mMethodMap[tempMethod] = paramsTypesAndCallback
            mRepeatKeyIndex ++
        } else {
            mMethodMap[methodName] = paramsTypesAndCallback
        }
    }

    // DES: 用于添加hook构造器的方法
    fun addConstructor(vararg paramsTypesAndCallback: Any?) {
        mMethodMap["$HOOK_CONSTRUCTOR_KEY$mRepeatKeyIndex"] = paramsTypesAndCallback
        mRepeatKeyIndex ++
    }

    override fun onHook() {
        // DES: 添加hook方法
        addHookMethods()
        // DES: 找到目标类的class
        val targetClassCls = getClassByClassName(className)
        if(mMethodMap.isEmpty()) {
            // DES: 没有需要Hook的方法和构造函数，没有则抛出异常
            throw NoSuchElementException("该类（${targetClassCls.name}）下 你可能忘记了添加Hook的方法或者构造函数！")
        }
        // DES: 创建默认的hook回调接口
        val defaultMethodHookCallback = DefaultMethodHookWrapper()
        val iterator = mMethodMap.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            // DES: 重新组装参数类型
            val paramsTypes = repackParamsTypes(entry.value, defaultMethodHookCallback)
            // DES: 获取hook的方法名为空表示hook的是构造函数
            val methodName = if(entry.key?.startsWith(HOOK_CONSTRUCTOR_KEY) == false) {
                if(entry.key.contains(DEFAULT_SPLIT_SYMBOL)) {
                    entry.key.split(DEFAULT_SPLIT_SYMBOL)[0]
                } else {
                    entry.key
                }
            } else {
                null
            }
            // DES: hook方法或者构造函数
            hookCacheWrapper(targetClassCls, methodName, paramsTypes)
        }
    }
}