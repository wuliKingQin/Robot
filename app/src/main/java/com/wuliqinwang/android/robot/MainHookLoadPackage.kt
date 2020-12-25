package com.wuliqinwang.android.robot

import android.util.Log
import com.wuliqinwang.android.robot.hook.ClassLoaderUtils
import com.wuliqinwang.android.robot.hook.IHookProvider
import com.wuliqinwang.android.robot.wework.WeworkHookProvider
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * @Version: 1.0.0
 */
class MainHookLoadPackage: IXposedHookLoadPackage {

    companion object {
        private const val WEWORK_PROCESS_NAME = "com.tencent.wework"
    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        lpparam ?: return
        tryCatch {
            // DES: 判断是否在企业微信开始hook
            val isHookProcess = lpparam.processName == WEWORK_PROCESS_NAME
            val isHookPackage = lpparam.packageName == WEWORK_PROCESS_NAME
            if (isHookProcess && isHookPackage) {
                // DES: 是的话则进入的企业微信的进程
                ClassLoaderUtils.targetClassLoader = lpparam.classLoader
                Log.d("test===", "hook 企业微信成功")
                // DES: 开始进行hook处理
                runHook(WeworkHookProvider::class.java)
            }
        }
    }

    // DES: 运行hook开始地方
    private fun runHook(vararg hookCls: Class<*>) {
        hookCls.forEach {
            val provider = it.newInstance()
            if(provider is IHookProvider) {
                provider.onHooks()?.forEach { hook ->
                    hook.run()
                }
            }
        }
    }
}