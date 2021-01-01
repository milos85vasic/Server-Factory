package net.milosvasic.factory.error

enum class ERROR(
    val code: Int,
    val message: String
) {

    RUNTIME_ERROR(1, "Runtime error"),
    FATAL_EXCEPTION(4, "Fatal exception"),
    INITIALIZATION_FAILURE(5, "Initialization failure"),
    TERMINATION_FAILURE(6, "Termination failure")
}