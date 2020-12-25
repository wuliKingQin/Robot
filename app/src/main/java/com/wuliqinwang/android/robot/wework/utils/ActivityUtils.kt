package com.wuliqinwang.android.robot.wework.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import java.util.*

/**
 * @Description: 用于保存所有启动的Activity
 * @Version: 1.0.0
 */
object ActivityUtils {

    // DES: 用于保存全局环境
    var globalContent: Context? = null

    /**
     * 保存界面生成和消失对象
     */
    private val mTopActivity = Stack<Activity>()

    /**
     * 获取堆栈顶部界面
     */
    fun getTopActivity(): Activity? {
        return if(mTopActivity.isNotEmpty()) {
            mTopActivity.peek()
        } else {
            null
        }
    }

    /**
     * 添加到堆栈中
     */
    fun pushActivity(activity: Activity?) {
        activity ?: return
        mTopActivity.push(activity)
    }

    /**
     * 移除对应的界面
     */
    fun popActivity(activity: Activity?) {
        if(mTopActivity.isNotEmpty() && mTopActivity.contains(activity)) {
            mTopActivity.remove(activity)
        }
    }

    // DES: 释放缓存
    fun release() {
        globalContent = null
        mTopActivity.clear()
    }

    // DES: 获取堆栈大小
    fun getStackSize(): Int = mTopActivity.size
}

// DES: 用于监听hook方app的Activity生命周期
class ActivityLifecycleCallbackIml: Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity?) {
    }

    override fun onActivityResumed(activity: Activity?) {
    }

    override fun onActivityStarted(activity: Activity?) {
    }

    override fun onActivityDestroyed(activity: Activity?) {
        ActivityUtils.popActivity(
            activity
        )
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
    }

    override fun onActivityStopped(activity: Activity?) {
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        ActivityUtils.pushActivity(
            activity
        )
    }
}