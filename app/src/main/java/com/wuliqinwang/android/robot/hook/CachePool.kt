package com.wuliqinwang.android.robot.hook

import com.wuliqinwang.android.robot.filterOutXX
import de.robv.android.xposed.XC_MethodHook
import java.util.*

/**
 * @Description: 用于缓存一些公共的class,防止内存泄漏等
 * @Version: 1.0.0
 */
class CachePool private constructor(){

    companion object {
        // DES: 默认缓存池子大小
        private const val DEFAULT_POOL_SIZE = 100
        // DES: 默认的参数缓存大小
        private const val DEFAULT_PARAMS_CACHE_SIZE = 10

        // DES: 缓存池实例
        private val instance by lazy {
            CachePool()
        }

        // DES: 获取缓存的hook
        fun getHook(key: String?): ArrayList<Array<out Any>>? {
            return instance.getHook(key)
        }

        // DES: 获取一样的hook
        fun getSameHook(key: String?, targetParams: Array<out Any?>?): Array<out Any>? {
            return instance.getSameHook(key, targetParams)
        }

        // DES: 获取缓存的class
        fun getClass(key: String?, generateCallback: (() -> Class<*>?)? = null): Class<*>? {
            return instance.getClass(key, generateCallback)
        }

        // DES: 添加到缓存
        fun put(key: String?, value: Any?) {
            instance.put(key, value)
        }

        // DES: 清除缓存
        fun clear(clearType: Int=0) {
            instance.clear(clearType)
        }
    }

    // DES: 用户缓存class的map
    private val mClassCacheMap by lazy {
        Collections.synchronizedMap(WeakHashMap<String, Class<*>>(DEFAULT_POOL_SIZE))
    }

    private val mHookCacheMap by lazy {
        Collections.synchronizedMap(WeakHashMap<String, ArrayList<Array<out Any>>>(DEFAULT_POOL_SIZE))
    }

    // DES: 获取缓存的hook
    fun getHook(key: String?): ArrayList<Array<out Any>>? {
        return getCache(key, { ArrayList<Array<out Any>>(DEFAULT_PARAMS_CACHE_SIZE) }, false)
    }

    // DES: 获取缓存的class
    fun getClass(key: String?, generateCallback: (() -> Class<*>?)?): Class<*>? {
        return getCache(key, generateCallback, true)
    }

    // DES: 获取缓存内容
    private inline fun <reified T> getCache(
        key: String?,
        noinline generateCallback: (() -> T)?,
        isClassCache: Boolean
    ): T? {
        val targetObj = get(key, isClassCache)
        return if(targetObj == null && !isContainKey(key, isClassCache) && generateCallback != null) {
            val cacheObj = generateCallback.invoke()
            put(key, cacheObj)
            cacheObj
        } else {
            targetObj as? T
        }
    }

    // DES: 获取缓存内容
    private fun get(key: String?, isClassCache: Boolean): Any? {
        return if(isClassCache) {
            mClassCacheMap[key]
        } else {
            mHookCacheMap[key]
        }
    }

    // DES: 是否包含key
    private fun isContainKey(key: String?, isClassCache: Boolean): Boolean {
        return if(isClassCache) {
            mClassCacheMap.containsKey(key)
        } else {
            mHookCacheMap.containsKey(key)
        }
    }

    // DES: 添加需要缓存对象到缓存器
    @Suppress("UNCHECKED_CAST")
    fun put(key: String?, targetObj: Any?): Any? {
        return when (targetObj) {
            is Class<*> -> putClass(key, targetObj)
            is Array<*> -> putHook(key, targetObj as? Array<out Any>)
            else -> null
        }
    }

    // DES: 清除缓存,
    // 传值1清除class，传值21清除Hook，其他清除所有缓存
    fun clear(clearType: Int) {
        when(clearType) {
            1 -> mClassCacheMap.clear()
            2 -> mHookCacheMap.clear()
            else -> {
                mClassCacheMap.clear()
                mHookCacheMap.clear()
            }
        }
    }

    // DES: 添加Class到缓存
    private fun putClass(key: String?, targetObj: Class<*>?): Any? {
        key ?: return null
        mClassCacheMap[key] = targetObj
        return key
    }

    // DES: 添加Hook到缓存
    private fun putHook(key: String?, targetObj: Array<out Any>?): Any? {
        key ?: return null
        var cacheParamsList = mHookCacheMap[key]
        if(cacheParamsList == null) {
            cacheParamsList = ArrayList(DEFAULT_PARAMS_CACHE_SIZE)
            mHookCacheMap[key] = cacheParamsList
        }
        if(cacheParamsList.isNullOrEmpty() && targetObj != null) {
            cacheParamsList.add(targetObj)
        } else if(targetObj != null){
            val sameHook = getSameHook(key, targetObj)
            if(sameHook == null) {
                cacheParamsList.add(targetObj)
            }
        }
        return targetObj
    }

    // DES: 用来判断是否有相同的Hook方法
    private fun getSameHook(key: String?, targetParams: Array<out Any?>?): Array<out Any>? {
        val cacheList = getHook(key)?.filter { params ->
            val oldParams = params.filterOutXX<XC_MethodHook>()
            val newParams = targetParams.filterOutXX<XC_MethodHook>()
            val isSameHook = if(oldParams != null && newParams != null) {
                oldParams.contentDeepEquals(newParams)
            } else {
                false
            }
            isSameHook
        }
        return if(!cacheList.isNullOrEmpty()) {
            cacheList[0]
        } else {
            null
        }
    }
}