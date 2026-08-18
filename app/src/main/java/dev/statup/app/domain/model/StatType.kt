package dev.statup.app.domain.model

import androidx.compose.ui.graphics.Color
import dev.statup.app.ui.theme.*

enum class StatType(val displayName: String, val color: Color) {
    STR("Strength", StatSTR),
    INT("Intelligence", StatINT),
    WIS("Wisdom", StatWIS),
    DEX("Dexterity", StatDEX),
    CHA("Charisma", StatCHA),
    VIT("Vitality", StatVIT);

    companion object {
        fun fromString(value: String): StatType? = entries.find { it.name == value }
    }
}
