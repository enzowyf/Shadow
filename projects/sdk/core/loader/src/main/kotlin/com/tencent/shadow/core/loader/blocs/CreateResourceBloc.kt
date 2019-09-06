/*
 * Tencent is pleased to support the open source community by making Tencent Shadow available.
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * Licensed under the BSD 3-Clause License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     https://opensource.org/licenses/BSD-3-Clause
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.tencent.shadow.core.loader.blocs

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.content.res.Resources
import android.os.Build
import com.tencent.shadow.core.load_parameters.LoadParameters
import com.tencent.shadow.core.loader.infos.PluginParts

object CreateResourceBloc {
    fun create(loadParameters: LoadParameters, packageArchiveInfo: PackageInfo, archiveFilePath: String, hostAppContext: Context, pluginPartsMap: MutableMap<String, PluginParts>): Resources {

        packageArchiveInfo.applicationInfo.apply {
            publicSourceDir = archiveFilePath
            sourceDir = archiveFilePath
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            //加入宿主资源
            val splitPublicSourceDirs = mutableListOf(hostAppContext.applicationInfo.sourceDir)

            //加入依赖插件的资源
            loadParameters.dependsOn?.forEach { partKey ->
                splitPublicSourceDirs.add(pluginPartsMap[partKey]?.apkPath ?: "")
            }

            packageArchiveInfo.applicationInfo.splitPublicSourceDirs = splitPublicSourceDirs.toTypedArray()
            try {
                return hostAppContext.packageManager.getResourcesForApplication(packageArchiveInfo.applicationInfo)
            } catch (e: PackageManager.NameNotFoundException) {
                throw RuntimeException(e)
            }
        } else {
            try {
                val resource = hostAppContext.packageManager.getResourcesForApplication(packageArchiveInfo.applicationInfo)
                val assetManager = resource.assets
                val addAssetPath = AssetManager::class.java.getDeclaredMethod("addAssetPath", String::class.java)

                //加入宿主资源
                addAssetPath.invoke(assetManager, hostAppContext.packageResourcePath)

                //加入依赖插件的资源
                loadParameters.dependsOn?.forEach { partKey ->
                    addAssetPath.invoke(assetManager, pluginPartsMap[partKey]?.apkPath)
                }

                return resource
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

    }
}
