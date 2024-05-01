package ua.besf0r.cubauncher.minecraft.auth.microsoft

interface AuthListener {
    fun onUserCodeReceived(userCode: String, verificationUri: String)
    fun onMinecraftAuth()
    fun onCheckGameOwnership()
    fun onGettingSkin()
    fun onFinish()
}
