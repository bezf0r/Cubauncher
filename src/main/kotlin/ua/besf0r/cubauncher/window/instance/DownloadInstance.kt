package ua.besf0r.cubauncher.window.instance

import androidx.compose.runtime.Composable
import ua.besf0r.cubauncher.account.OfflineAccount
import ua.besf0r.cubauncher.instance.InstanceRunner
import ua.besf0r.cubauncher.instanceManager

class DownloadInstance(
    private val name: String,
    private val version: String
) {
    @Composable
    fun downloadWindow() {
        val instance = instanceManager.createInstance(name,version)

        val runner = InstanceRunner(OfflineAccount("besf0r"), instance)
        runner.run()
    }
}