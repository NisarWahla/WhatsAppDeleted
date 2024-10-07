package dzm.wamr.recover.deleted.messages.photo.media.util

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.facebook.cache.disk.DiskCacheConfig
import com.facebook.common.disk.NoOpDiskTrimmableRegistry
import com.facebook.common.memory.MemoryTrimType
import com.facebook.common.memory.NoOpMemoryTrimmableRegistry
import com.facebook.common.util.ByteConstants
import com.facebook.imagepipeline.core.ImagePipelineConfig
import com.facebook.imagepipeline.core.ImagePipelineFactory
import dzm.wamr.recover.deleted.messages.photo.media.util.Utils.getPrivateAppFolder
import java.io.File

object ImagePipelineConfigUtils {
    //Allocated free memory
    private val MAX_HEAP_SIZE = Runtime.getRuntime().maxMemory().toInt()

    //Maximum value of small pictures with very low disk space cache (Feature: a large number of small pictures can be placed on another disk space to prevent large pictures from occupying disk space and deleting a large number of small pictures)
    private const val MAX_SMALL_DISK_VERYLOW_CACHE_SIZE = 20 * ByteConstants.MB

    //Maximum value of low disk space cache for small pictures (Feature: a large number of small pictures can be placed on another disk space to prevent large pictures from occupying disk space and deleting a large number of small pictures)
    private const val MAX_SMALL_DISK_LOW_CACHE_SIZE = 60 * ByteConstants.MB

    //The maximum value of the default picture very low disk space cache
    private const val MAX_DISK_CACHE_VERYLOW_SIZE = 20 * ByteConstants.MB

    //The maximum value of the default graph low disk space cache
    private const val MAX_DISK_CACHE_LOW_SIZE = 60 * ByteConstants.MB

    //The maximum value of the disk cache of the default graph
    private const val MAX_DISK_CACHE_SIZE = 100 * ByteConstants.MB

    //The folder name of the path where the small picture is placed
    private const val IMAGE_PIPELINE_SMALL_CACHE_DIR = "images_small_fresco"

    //The folder name of the path where the default image is placed
    private const val IMAGE_PIPELINE_CACHE_DIR = "images_fresco"
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun getDefaultImagePipelineConfig(context: Context?): ImagePipelineConfig {
        //Memory configuration
        val diskSmallCacheConfig = DiskCacheConfig.newBuilder(context).setBaseDirectoryPath(
            File(
                getPrivateAppFolder(
                    context!!
                )
            )
        ) //Cache image base path
            .setBaseDirectoryName(IMAGE_PIPELINE_SMALL_CACHE_DIR) //folder name
            .setMaxCacheSize(MAX_DISK_CACHE_SIZE.toLong()) //The maximum size of the default cache.
            .setMaxCacheSizeOnLowDiskSpace(MAX_SMALL_DISK_LOW_CACHE_SIZE.toLong()) //The maximum size of the cache, low disk space when using the device.
            .setMaxCacheSizeOnVeryLowDiskSpace(MAX_SMALL_DISK_VERYLOW_CACHE_SIZE.toLong()) //The maximum size of the cache, when the device is extremely low disk space
            .setDiskTrimmableRegistry(NoOpDiskTrimmableRegistry.getInstance())
            .build()
        //Disk configuration of the default picture
        val diskCacheConfig = DiskCacheConfig.newBuilder(context).setBaseDirectoryPath(
            File(
                getPrivateAppFolder(
                    context
                )
            )
        ) //Cache image base path
            .setBaseDirectoryName(IMAGE_PIPELINE_CACHE_DIR) //folder name
            .setMaxCacheSize(MAX_DISK_CACHE_SIZE.toLong()) //The maximum size of the default cache.
            .setMaxCacheSizeOnLowDiskSpace(MAX_DISK_CACHE_LOW_SIZE.toLong()) //The maximum size of the cache, low disk space when using the device.
            .setMaxCacheSizeOnVeryLowDiskSpace(MAX_DISK_CACHE_VERYLOW_SIZE.toLong()) //The maximum size of the cache, when the device is extremely low disk space
            .setDiskTrimmableRegistry(NoOpDiskTrimmableRegistry.getInstance())
            .build()
        //Cache image configuration
        val configBuilder = ImagePipelineConfig.newBuilder(context)
            .setSmallImageDiskCacheConfig(diskSmallCacheConfig)
            .setMainDiskCacheConfig(diskCacheConfig)
            .setMemoryTrimmableRegistry(NoOpMemoryTrimmableRegistry.getInstance())
            .setResizeAndRotateEnabledForNetwork(true)
        // is this piece of code, used to clean up the cache
        NoOpMemoryTrimmableRegistry.getInstance().registerMemoryTrimmable { trimType ->
            val suggestedTrimRatio = trimType.suggestedTrimRatio
            if (MemoryTrimType.OnCloseToDalvikHeapLimit.suggestedTrimRatio == suggestedTrimRatio || MemoryTrimType.OnSystemLowMemoryWhileAppInBackground.suggestedTrimRatio == suggestedTrimRatio || MemoryTrimType.OnSystemLowMemoryWhileAppInForeground.suggestedTrimRatio == suggestedTrimRatio) {
                ImagePipelineFactory.getInstance().imagePipeline.clearMemoryCaches()
            }
        }
        return configBuilder.build()
    }
}