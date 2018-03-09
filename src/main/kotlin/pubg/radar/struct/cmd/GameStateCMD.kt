package pubg.radar.struct.cmd

import com.badlogic.gdx.math.Vector2
import pubg.radar.GameListener
import pubg.radar.register
import pubg.radar.struct.Actor
import pubg.radar.struct.Bunch
import pubg.radar.struct.NetGuidCacheObject
import pubg.radar.struct.cmd.CMD.propertyBool
import pubg.radar.struct.cmd.CMD.propertyByte
import pubg.radar.struct.cmd.CMD.propertyFloat
import pubg.radar.struct.cmd.CMD.propertyInt
import pubg.radar.struct.cmd.CMD.propertyName
import pubg.radar.struct.cmd.CMD.propertyObject
import pubg.radar.struct.cmd.CMD.propertyString
import pubg.radar.struct.cmd.CMD.propertyVector

object GameStateCMD : GameListener {
    init {
        register(this)
    }

    override fun onGameOver() {
        SafetyZonePosition.setZero()
        SafetyZoneRadius = 0f
        SafetyZoneBeginPosition.setZero()
        SafetyZoneBeginRadius = 0f
        PoisonGasWarningPosition.setZero()
        PoisonGasWarningRadius = 0f
        RedZonePosition.setZero()
        RedZoneRadius = 0f
        TotalWarningDuration = 0f
        ElapsedWarningDuration = 0f
        TotalReleaseDuration = 0f
        ElapsedReleaseDuration = 0f
        NumJoinPlayers = 0
        NumAlivePlayers = 0
        NumAliveTeams = 0
        RemainingTime = 0
        MatchElapsedMinutes = 0

    }

    var TotalWarningDuration = 0f
    var ElapsedWarningDuration = 0f
    private var RemainingTime = 0
    var MatchElapsedMinutes = 0
    val SafetyZonePosition = Vector2()
    var SafetyZoneRadius = 0f
    private val SafetyZoneBeginPosition = Vector2()
    private var SafetyZoneBeginRadius = 0f
    val PoisonGasWarningPosition = Vector2()
    var PoisonGasWarningRadius = 0f
    val RedZonePosition = Vector2()
    var RedZoneRadius = 0f
    private var TotalReleaseDuration = 0f
    private var ElapsedReleaseDuration = 0f
    private var NumJoinPlayers = 0
    var NumAlivePlayers = 0
    var NumAliveTeams = 0

    fun process(actor: Actor, bunch: Bunch, repObj: NetGuidCacheObject?, waitingHandle: Int, data: HashMap<String, Any?>): Boolean {
        with(bunch) {
            when (waitingHandle) {
                16 -> {
                    val GameModeClass = propertyObject()
                    val b = GameModeClass
                }
                17 -> {
                    val SpectatorClass = propertyObject()
                    val b = SpectatorClass
                }
                18 -> {
                    val bReplicatedHasBegunPlay = propertyBool()
                    val b = bReplicatedHasBegunPlay
                }
                19 -> {
                    val ReplicatedWorldTimeSeconds = propertyFloat()
                    val b = ReplicatedWorldTimeSeconds
                }
                20 -> {
                    val MatchState = propertyName()
                    val b = MatchState
                }
                21 -> {
                    val ElapsedTime = propertyInt()
                    val b = ElapsedTime
                }
                22 -> {
                    val MatchId = propertyString()
                    val b = MatchId
                }
                23 -> {
                    val MatchShortGuid = propertyString()
                    val b = MatchShortGuid
                }
                24 -> propertyBool()//bIsCustomGame
                25 -> propertyBool() //bIsWinnerZombieTeam
                26 -> {
                    val NumTeams = propertyInt()
                    val b = NumTeams
                }
                27 -> {
                    RemainingTime = propertyInt()
                }
                28 -> {
                    MatchElapsedMinutes = propertyInt()
                }
                29 -> {
                    val bTimerPaused = propertyBool()
                    val b = bTimerPaused
                }
                30 -> {
                    NumJoinPlayers = propertyInt()
                }
                31 -> {
                    NumAlivePlayers = propertyInt()
                }
                32 -> {
                    val NumAliveZombiePlayers = propertyInt()
                    val b = NumAliveZombiePlayers
                }
                33 -> {
                    NumAliveTeams = propertyInt()
                }
                34 -> {
                    val NumStartPlayers = propertyInt()
                    val b = NumStartPlayers
                }
                35 -> {
                    val NumStartTeams = propertyInt()
                    val b = NumStartTeams
                }
                36 -> {
                    val pos = propertyVector()
                    SafetyZonePosition.set(pos.x, pos.y)
                }
                37 -> {
                    SafetyZoneRadius = propertyFloat()
                }
                38 -> {
                    val pos = propertyVector()
                    PoisonGasWarningPosition.set(pos.x, pos.y)
                }
                39 -> {
                    PoisonGasWarningRadius = propertyFloat()
                }
                40 -> {
                    val pos = propertyVector()
                    RedZonePosition.set(pos.x, pos.y)

                    val b = RedZonePosition
                }
                41 -> {
                    RedZoneRadius = propertyFloat()
                    val b = RedZoneRadius
                }
                42 -> {
                    TotalReleaseDuration = propertyFloat()
                    val b = TotalReleaseDuration
                }
                43 -> {
                    ElapsedReleaseDuration = propertyFloat()
                    val b = ElapsedReleaseDuration
                }
                44 -> {
                    TotalWarningDuration = propertyFloat()
                }
                45 -> {
                    ElapsedWarningDuration = propertyFloat()
                }
                46 -> {
                    val bIsGasRelease = propertyBool()
                }
                47 -> {
                    val bIsTeamMatch = propertyBool()
                    val b = bIsTeamMatch
                }
                48 -> {
                    val bIsZombieMode = propertyBool()
                }
                49 -> {
                    val pos = propertyVector()
                    SafetyZoneBeginPosition.set(pos.x, pos.y)
                }
                50 -> {
                    SafetyZoneBeginRadius = propertyFloat()
                }
                51 -> {
                    val MatchStartType = propertyByte()
                }
                52 -> return false
                else -> return false
            }
            return true
        }
    }
}