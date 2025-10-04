package ua.besf0r.kovadlo.minecraft

import io.ktor.client.*
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import ua.besf0r.kovadlo.minecraft.fabric.FabricVersionList
import ua.besf0r.kovadlo.minecraft.forge.ForgeVersionList
import ua.besf0r.kovadlo.minecraft.liteloader.LiteLoaderVersionList
import ua.besf0r.kovadlo.minecraft.minecraft.MinecraftVersionList
import ua.besf0r.kovadlo.minecraft.optifine.OptiFineVersionList
import ua.besf0r.kovadlo.minecraft.quilt.QuiltVersionList
import ua.besf0r.kovadlo.network.DownloadService

object InitialiseDI {
    fun versionsModule() = DI.Module("versionLists"){
        bind<MinecraftVersionList>() with singleton { MinecraftVersionList(instance<HttpClient>()) }
        bind<ForgeVersionList>() with singleton { ForgeVersionList(instance<HttpClient>()) }
        bind<FabricVersionList>() with singleton { FabricVersionList(instance<DownloadService>()) }
        bind<LiteLoaderVersionList>() with singleton { LiteLoaderVersionList(instance<DownloadService>()) }
        bind<OptiFineVersionList>() with singleton { OptiFineVersionList(instance<DownloadService>()) }
        bind<QuiltVersionList>() with singleton { QuiltVersionList(instance<DownloadService>()) }
    }
}