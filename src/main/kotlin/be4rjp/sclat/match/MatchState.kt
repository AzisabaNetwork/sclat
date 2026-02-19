package be4rjp.sclat.match

enum class MatchState {
    WAITING, // No actions
    COUNTING, // Ready message
    RUNNING, // Run match
    FINISHED, // Show result
}
