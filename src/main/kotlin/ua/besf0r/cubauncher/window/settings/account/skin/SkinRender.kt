package ua.besf0r.cubauncher.window.settings.account.skin

import java.awt.image.BufferedImage

object SkinRender {

    fun getScaledSkinHead(skin: BufferedImage): BufferedImage {
        val head = skin.getSubimage(8, 8, 8, 8)
        val scaledHead = head.getScaledInstance(32, 32, BufferedImage.SCALE_FAST)
        val result = BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB)
        val graphics = result.graphics
        graphics.drawImage(scaledHead, 0, 0, null)
        graphics.dispose()
        return result
    }

}
