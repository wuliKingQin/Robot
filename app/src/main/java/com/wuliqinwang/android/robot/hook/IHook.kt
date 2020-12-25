package com.wuliqinwang.android.robot.hook

import de.robv.android.xposed.XC_MethodHook

/**
 * @Description: 实现该接口，方便进行统一对某个类的方法进行hook
 * @CreateDate: 2020/6/24 11:00
 * @Version: 1.0.0
 */
interface IHook: Runnable {

    companion object {
        // DES: hook锁，保证操作缓存数据有先后顺序，
        // 防止一样的方法被同时hook多次
        private val HOOK_LOCK = Any()
    }

    override fun run() {
        synchronized(HOOK_LOCK) {
            onHook()
        }
    }

    // DES: 用于执行hook的方法
    fun onHook()

    // DES: Hook执行方法前的回调方法
    fun onBeforeHookedMethod(param: XC_MethodHook.MethodHookParam?)

    // DES: Hook执行方法后的回调方法
    fun onAfterHookedMethod(param: XC_MethodHook.MethodHookParam?)
}