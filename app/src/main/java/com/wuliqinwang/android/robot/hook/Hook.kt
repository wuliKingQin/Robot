package com.wuliqinwang.android.robot.hook

import com.wuliqinwang.android.robot.callMethod
import com.wuliqinwang.android.robot.tryCatch
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import java.lang.NullPointerException

/**
 * @Description: hook包装器，用来执行hook操作
 * @Version: 1.0.0
 */
open class Hook constructor(
    // DES: hook的目标类对象
    protected open val className: String?
): IHook, Comparable<Hook>{

    constructor(
        // DES: 类名
        className: String?,
        // DES: 方法名
        methodName: String?,
        // DES: hook的目标方法参数类型和hook后的回调对象
        vararg paramsAndCallback: Any
    ): this(className) {
        mMethodName = methodName
        mParamsAndCallback = paramsAndCallback
    }

    // DES: 带ClassLoader的构造器
    constructor(
        className: String?,
        classLoader: ClassLoader?
    ): this(className) {
        mClassLoader = classLoader
    }

    // DES: 使用Class对象最为构造函数
    constructor(
        targetCls: Class<*>?,
        methodName: String?,
        vararg paramsAndCallback: Any
    ): this("", methodName, *paramsAndCallback) {
        mTargetClass = targetCls
    }

    companion object {
        // DES: 用于给默认的构造器的名字
        const val DEFAULT_CONSTRUCTOR_NAME = "Constructor"
        // DES: 通过类名找对应实现类的class
        fun findClass(className: String?, targetCls: ClassLoader?=null): Class<*>? {
            className ?: return null
            return CachePool.getClass(className) {
                val tempCls = targetCls ?: ClassLoaderUtils.targetClassLoader
                XposedHelpers.findClass(className, tempCls)
            }
        }
    }

    // DES: hook的目标对象方法名
    private var mMethodName: String? = null
    // DES: 用于提供找类的ClassLoader
    private var mClassLoader: ClassLoader? = null
    // DES: 目标class
    private var mTargetClass: Class<*>? = null
    // DES: 用于保存方法的参数类型
    private var mParamsAndCallback: Array<out Any?>? = null

    override fun onHook() {
        val targetCls = getClassByClassName(className)
        if(mParamsAndCallback == null) {
            throw NullPointerException("hook的方法未设置参数类型！")
        }
        mParamsAndCallback = repackParamsTypes(mParamsAndCallback!!, DefaultMethodHookWrapper())
        hookCacheWrapper(targetCls, mMethodName, mParamsAndCallback)
    }

    // DES: hook的时候会去判断是否有一样的
    open fun hookCacheWrapper(targetCls: Class<*>?, methodName: String?, params: Array<out Any?>?) {
        val cacheKey = generateCacheKey(methodName)
        val cacheParams = CachePool.getSameHook(cacheKey, params)
        if(cacheParams == null && params != null) {
            CachePool.put(cacheKey, params)
            if(methodName.isNullOrEmpty()) {
                hookConstructorWrapper(targetCls, *params)
            } else {
                hookMethodWrapper(targetCls, methodName, *params)
            }
        } else if(params != null){
            // DES: 保证多个相同方法只有一个被hook，其他的依然能接收到hook后的回调信息
            val newCallback = params.lastOrNull()
            if(newCallback is XC_MethodHook) {
                val cacheCallback = cacheParams?.lastOrNull()
                if(cacheCallback is DefaultMethodHookWrapper) {
                    cacheCallback.addCallback(newCallback)
                }
            }
        }
    }

    // DES: 生成的缓存hook方法的key
    open fun generateCacheKey(methodName: String?): String? {
        // DES: 用于构造缓存key后缀
        val tempMethodName = if(methodName.isNullOrEmpty()) {
            DEFAULT_CONSTRUCTOR_NAME
        } else {
            methodName
        }
        // DES: 用于构造缓存key的前缀
        val tempClassName = if(className.isNullOrEmpty() && mTargetClass != null) {
            mTargetClass?.name
        } else {
            className
        }
        return tempClassName?.plus(".$tempMethodName")
    }

    // DES: 通过类名去获取对象的类的class
    open fun getClassByClassName(className: String?): Class<*> {
        val targetCls = (if(!className.isNullOrEmpty()) {
            findClass(className, mClassLoader)
        } else {
            mTargetClass
        })
        targetCls ?: throw NullPointerException("需要hook的目标类的Class为空!")
        return targetCls
    }

    override fun onBeforeHookedMethod(param: XC_MethodHook.MethodHookParam?) {
    }

    override fun onAfterHookedMethod(param: XC_MethodHook.MethodHookParam?) {
    }

    // DES: 找参数里的class
    open fun repackParamsTypes(
        oldParamsTypes: Array<out Any?>,
        defaultMethodHook: DefaultMethodHookWrapper
    ): Array<out Any?> {
        val isIncludeCallback = oldParamsTypes.filterIsInstance<XC_MethodHook>().isNotEmpty()
        val oldParamsSize = oldParamsTypes.size
        val newParamsSize = if(!isIncludeCallback) {
            oldParamsSize + 1
        } else {
            oldParamsSize
        }
        val tempParamsAndCallback = Array<Any?>(newParamsSize) { size ->
            size
        }
        oldParamsTypes.forEachIndexed { index, paramsType ->
            if(paramsType is String) {
                val findTargetCls =
                    findClass(
                        paramsType
                    )
                if(findTargetCls != null) {
                    tempParamsAndCallback[index] = findTargetCls
                } else {
                    throw NullPointerException("未找到($paramsType)对应的class!")
                }

            } else if(paramsType is XC_MethodHook) {
                tempParamsAndCallback[index] = DefaultMethodHookWrapper(paramsType)
            } else {
                tempParamsAndCallback[index] = paramsType
            }
        }
        if(oldParamsSize != newParamsSize) {
            tempParamsAndCallback[oldParamsSize] = defaultMethodHook
        }
        return tempParamsAndCallback
    }

    // DES: hook包装器，用于执行实际的hook
    open fun hookMethodWrapper(targetCls: Class<*>?, methodName: String, vararg paramsTypesAndCallback: Any?){
        hookWrapper(targetCls, methodName, *paramsTypesAndCallback)
    }

    // DES: hook构造器的包装方法
    open fun hookConstructorWrapper(targetCls: Class<*>?, vararg paramsTypesAndCallback: Any?) {
        hookWrapper(targetCls, null, *paramsTypesAndCallback)
    }

    // DES: hook包装器，包括方法和构造器
    private fun hookWrapper(targetCls: Class<*>?, methodName: String?, vararg paramsTypesAndCallback: Any?) {
        if(targetCls == null) {
            return
        }
        tryCatch {
            val hookInstance = if(methodName.isNullOrEmpty()) {
                XposedHelpers.findAndHookConstructor(targetCls, *paramsTypesAndCallback)
            } else {
                XposedHelpers.findAndHookMethod(
                    targetCls,
                    methodName,
                    *paramsTypesAndCallback
                )
            }
        }
    }

    // DES: 方法hook后的回调包装器
    inner class DefaultMethodHookWrapper(
        // DES: 接收需要包装的回调对象
        vararg oldMethodCallback: XC_MethodHook
    ): XC_MethodHook(){

        // DES: 用于存放之前的或者相同hook的回调方法
        private val mOldMethodCallback = ArrayList<XC_MethodHook>(oldMethodCallback.toList())

        override fun afterHookedMethod(param: MethodHookParam?) {
           tryCatch {
               callOldHookMethod("afterHookedMethod", param)
               onAfterHookedMethod(param)
           }
        }

        override fun beforeHookedMethod(param: MethodHookParam?) {
            tryCatch {
                callOldHookMethod("beforeHookedMethod", param)
                onBeforeHookedMethod(param)
            }
        }

        // DES: 调用之前的Hook回调方法
        private fun callOldHookMethod(methodName: String, param: MethodHookParam?) {
            mOldMethodCallback.forEach {
                it.callMethod<Any>(methodName, param)
            }
        }

        // DES: 添加其他回调接口
        fun addCallback(callback: XC_MethodHook) {
            mOldMethodCallback.add(callback)
        }
    }

    override fun compareTo(other: Hook): Int {
        return if(other.hashCode() != hashCode())  {
            1
        } else {
            0
        }
    }
}