package com.wuliqinwang.android.robot.hook

/**
 * @Description: 用于抽象出方便hook的提供器接口
 * @CreateDate: 2020/7/17 9:15
 * @Version: 1.0.0
 */
interface IHookProvider {
    // DES: 用于提供给
    fun onHooks(): ArrayList<IHook>?
}