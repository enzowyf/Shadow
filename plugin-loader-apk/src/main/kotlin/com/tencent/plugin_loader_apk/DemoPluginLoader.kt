package com.tencent.plugin_loader_apk

import com.tencent.cubershi.plugin_loader.CuberPluginLoader
import com.tencent.cubershi.plugin_loader.managers.PluginActivitiesManager

class DemoPluginLoader : CuberPluginLoader() {
    private val mDemoPluginActivitiesManager = DemoPluginActivitiesManager()

    override fun getBusinessPluginActivitiesManager(): PluginActivitiesManager {
        return mDemoPluginActivitiesManager
    }
}