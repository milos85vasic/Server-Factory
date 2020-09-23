package net.milosvasic.factory.account

enum class AccountType(val type: String) {

    POSTMASTER("postmaster"),
    EMAIL("email"),
    UNKNOWN("unknown");

    companion object {

        fun getByValue(value: String): AccountType {
            values().forEach {
                if (it.type == value) {
                    return it
                }
            }
            return UNKNOWN
        }
    }
}