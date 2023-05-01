package koziol.mooo.com.veedback4.models

import kotlinx.serialization.Serializable

enum class Color {
    GREY, YELLOW, GREEN, PINK, PURPLE, BLUE, BLACK, ORANGE, RED, WHITE
}

enum class Floor {
    GROUNDFLOOR, UPPERFLOOR
}

@Serializable
data class Problem(
    val id: Int = 0,
    val color: Color,
    val floor: Floor,
    val locationX: Byte,
    val locationY: Byte,
    var pictureUuid: String = "3f4b49b0-7025-4a3a-b8a9-08cdb3b49e2e"
)