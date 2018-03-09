package pubg.radar

import pubg.radar.LogLevel.*

enum class LogLevel {
    Off,
    Bug,
    Warning,
    Debug,
    Info
}

var logLevel = Off

inline fun info(info: () -> String) {
    if (logLevel.ordinal >= Info.ordinal)
        print(info())
}

inline fun infoln(info: () -> String) {
    if (logLevel.ordinal >= Info.ordinal)
        println(info())
}

inline fun debugln(info: () -> String) {
    if (logLevel.ordinal >= Debug.ordinal)
        println(info())
}

inline fun bug(info: () -> String) {
    if (logLevel.ordinal >= Bug.ordinal)
        print(info())
}

inline fun bugln(info: () -> String) {
    if (logLevel.ordinal >= Bug.ordinal)
        println(info())
}