package ua.besf0r.cubauncher.util

import java.awt.image.BufferedImage

class SkinUtils private constructor() {
    init {
        throw UnsupportedOperationException()
    }

    companion object {
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
}
