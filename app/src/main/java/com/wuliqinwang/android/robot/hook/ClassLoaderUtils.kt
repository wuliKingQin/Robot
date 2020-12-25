package com.wuliqinwang.android.robot.hook

/**
 * @Description: 用于保存目标类的唯一的类加载器，用于查找需要类型信息
 * @Version: 1.0.0
 */
object ClassLoaderUtils {
    // DES: 目标类加载器
    var targetClassLoader: ClassLoader? = null
}