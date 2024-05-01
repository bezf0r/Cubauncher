package ua.besf0r.cubauncher.minecraft.auth.microsoft

class GameOwnershipResponse {
    var items: List<Item> = listOf()

    data class Item (
        var name: String,
        var signature: String
    )
}
