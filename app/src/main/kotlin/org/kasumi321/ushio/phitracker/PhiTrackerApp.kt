package org.kasumi321.ushio.phitracker

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class PhiTrackerApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()

        // Timber 日志 - 仅 debug 模式
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.i("PhiTrackerApp initialized")
    }

    /**
     * 自定义 Coil ImageLoader
     *
     * 性能优化:
     * - 内存缓存: 应用最大内存的 25%
     * - 磁盘缓存: 100MB → 避免重复下载曲绘
     * - crossfade: 200ms 平滑过渡
     */
    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB
                    .build()
            }
            .crossfade(200)
            .respectCacheHeaders(false) // GitHub 图片不需要缓存头验证
            .build()
    }
}
