package tv.blademaker.killjoy.valorant

@Suppress("unused")
enum class Ranks(
        val id: Int,
        val emoji: String
) {
    Iron(       0, ":iron_3:771924280211537960"),
    Bronze(     1, ":bronze_3:771924279985176587"),
    Silver(     2, ":silver_3:771924279968268350"),
    Gold(       3, ":gold_3:771924280166187028>"),
    Platinum(   4, ":platinum_3:771924280245616650"),
    Diamond(    5, ":diamond_3:771924280131715092"),
    Inmortal(   6, ":inmortal_3:771924280144429057"),
    Radiant(    7, ":radiant:771924280425971762");

    val diple: String
        get() = "<${this.emoji}>"
}