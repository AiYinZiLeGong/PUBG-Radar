@file:Suppress("NAME_SHADOWING")

package pubg.radar.ui

import com.badlogic.gdx.ApplicationListener
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Buttons.LEFT
import com.badlogic.gdx.Input.Buttons.MIDDLE
import com.badlogic.gdx.Input.Buttons.RIGHT
import com.badlogic.gdx.Input.Keys.*
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Color.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.DEFAULT_CHARS
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import pubg.radar.*
import pubg.radar.deserializer.channel.ActorChannel.Companion.actors
import pubg.radar.deserializer.channel.ActorChannel.Companion.airDropLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.corpseLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.visualActors
import pubg.radar.sniffer.Sniffer.Companion.preDirection
import pubg.radar.sniffer.Sniffer.Companion.preSelfCoords
import pubg.radar.sniffer.Sniffer.Companion.selfCoords
import pubg.radar.struct.Actor
import pubg.radar.struct.Archetype
import pubg.radar.struct.Archetype.*
import pubg.radar.struct.NetworkGUID
import pubg.radar.struct.cmd.ActorCMD.actorWithPlayerState
import pubg.radar.struct.cmd.ActorCMD.playerStateToActor
import pubg.radar.struct.cmd.GameStateCMD.ElapsedWarningDuration
import pubg.radar.struct.cmd.GameStateCMD.NumAlivePlayers
import pubg.radar.struct.cmd.GameStateCMD.NumAliveTeams
import pubg.radar.struct.cmd.GameStateCMD.PoisonGasWarningPosition
import pubg.radar.struct.cmd.GameStateCMD.PoisonGasWarningRadius
import pubg.radar.struct.cmd.GameStateCMD.RedZonePosition
import pubg.radar.struct.cmd.GameStateCMD.RedZoneRadius
import pubg.radar.struct.cmd.GameStateCMD.SafetyZonePosition
import pubg.radar.struct.cmd.GameStateCMD.SafetyZoneRadius
import pubg.radar.struct.cmd.GameStateCMD.TotalWarningDuration
import pubg.radar.struct.cmd.PlayerStateCMD.attacks
import pubg.radar.struct.cmd.PlayerStateCMD.playerNames
import pubg.radar.struct.cmd.PlayerStateCMD.selfID
import pubg.radar.util.PlayerProfile.Companion.query
import pubg.radar.util.tuple4
import wumo.pubg.struct.cmd.TeamCMD.team
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow


typealias renderInfo = tuple4<Actor, Float, Float, Float>

//fun Float.d(n: Int) = String.format("%.${n}f", this)
class GLMap : InputAdapter(), ApplicationListener, GameListener {
    companion object {
        operator fun Vector3.component1(): Float = x
        operator fun Vector3.component2(): Float = y
        operator fun Vector3.component3(): Float = z
        operator fun Vector2.component1(): Float = x
        operator fun Vector2.component2(): Float = y

        val spawnErangel = Vector2(795548.3f, 17385.875f)
        val spawnDesert = Vector2(78282f, 731746f)
    }

    init {
        register(this)
    }


    override fun onGameStart() {
        preSelfCoords.set(if (isErangel) spawnErangel else spawnDesert)
        selfCoords.set(preSelfCoords)
        preDirection.setZero()

    }

    override fun onGameOver() {
        camera.zoom = 2 / 4f

        aimStartTime.clear()
        attackLineStartTime.clear()
        pinLocation.setZero()
    }

    fun show() {
        val config = Lwjgl3ApplicationConfiguration()
        config.setTitle("")
        config.useOpenGL3(false, 2, 1)
        config.setWindowedMode(800, 800)
        config.setResizable(true)
        config.useVsync(false)
        config.setIdleFPS(120)
        config.setBackBufferConfig(4, 4, 4, 4, 16, 4, 8)
        Lwjgl3Application(this, config)
    }


    private lateinit var spriteBatch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var mapErangelTiles: MutableMap<String, MutableMap<String, MutableMap<String, Texture>>>
    private lateinit var mapMiramarTiles: MutableMap<String, MutableMap<String, MutableMap<String, Texture>>>
    private lateinit var mapTiles: MutableMap<String, MutableMap<String, MutableMap<String, Texture>>>
    private lateinit var iconImages: Icons
    private lateinit var corpseboximage: Texture
    private lateinit var airdropimage: Texture
    private lateinit var bg_compass: Texture
    private lateinit var largeFont: BitmapFont
    private lateinit var littleFont: BitmapFont
    private lateinit var nameFont: BitmapFont
    private lateinit var itemFont: BitmapFont
    private lateinit var fontCamera: OrthographicCamera
    private lateinit var itemCamera: OrthographicCamera
    private lateinit var camera: OrthographicCamera
    private lateinit var alarmSound: Sound
    private lateinit var hubpanel: Texture
    private lateinit var hubpanelblank: Texture
    private lateinit var vehicle: Texture
    private lateinit var plane: Texture
    private lateinit var boat: Texture
    private lateinit var bike: Texture
    private lateinit var bike3x: Texture
    private lateinit var buggy: Texture
    private lateinit var van: Texture
    private lateinit var pickup: Texture
    private lateinit var arrow: Texture
    private lateinit var arrowsight: Texture
    private lateinit var jetski: Texture
    private lateinit var player: Texture
    private lateinit var playersight: Texture
    private lateinit var parachute: Texture
    private lateinit var grenade: Texture
    private lateinit var hubFont: BitmapFont
    private lateinit var hubFontShadow: BitmapFont
    private lateinit var espFont: BitmapFont
    private lateinit var espFontShadow: BitmapFont
    private lateinit var compaseFont: BitmapFont
    private lateinit var compaseFontShadow: BitmapFont
    private lateinit var littleFontShadow: BitmapFont


    private val tileZooms = listOf("256", "512", "1024", "2048", "4096"/*, "8192"*/)
    private val tileRowCounts = listOf(1, 2, 4, 8, 16/*, 32*/)
    private val tileSizes = listOf(819200f, 409600f, 204800f, 102400f, 51200f/*, 25600f*/)

    private val layout = GlyphLayout()
    private var windowWidth = initialWindowWidth
    private var windowHeight = initialWindowWidth

    private val aimStartTime = HashMap<NetworkGUID, Long>()
    private val attackLineStartTime = LinkedList<Triple<NetworkGUID, NetworkGUID, Long>>()
    private val pinLocation = Vector2()
    private var filterWeapon = -1
    private var filterAttach = 1
    private var filterLvl2 = 1
    private var filterScope = -1
    private var filterHeals = -1
    private var filterAmmo = 1
    private var filterThrow = 1
    private var drawcompass = -1
    private var toggleView = -1
    private var scopesToFilter = arrayListOf("")
    private var weaponsToFilter = arrayListOf("")
    private var attachToFilter = arrayListOf("")
    private var level2Filter = arrayListOf("")
    private var healsToFilter = arrayListOf("")
    private var ammoToFilter = arrayListOf("")
    private var throwToFilter = arrayListOf("")
    private var dragging = false
    private var prevScreenX = -1f
    private var prevScreenY = -1f
    private var screenOffsetX = 0f
    private var screenOffsetY = 0f
    private var toggleVehicles = -1
    private var toggleVNames = 1
    private var drawgrid = -1

    private fun Vector2.windowToMap() =
            Vector2(selfCoords.x + (x - windowWidth / 2.0f) * camera.zoom * windowToMapUnit + screenOffsetX,
                    selfCoords.y + (y - windowHeight / 2.0f) * camera.zoom * windowToMapUnit + screenOffsetY)

    private fun Vector2.mapToWindow() =
            Vector2((x - selfCoords.x - screenOffsetX) / (camera.zoom * windowToMapUnit) + windowWidth / 2.0f,
                    (y - selfCoords.y - screenOffsetY) / (camera.zoom * windowToMapUnit) + windowHeight / 2.0f)


    override fun scrolled(amount: Int): Boolean {

      if (camera.zoom >= 0.01f && camera.zoom <= 1f) {
          camera.zoom *= 1.05f.pow(amount)
      } else {
          if (camera.zoom < 0.01f) {
              camera.zoom = 0.01f
              println("Max Zoom")
          }
          if (camera.zoom > 1f) {
              camera.zoom = 1f
              println("Min Zoom")
          }
      }

        return true
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        when (button) {
            RIGHT -> {
                pinLocation.set(pinLocation.set(screenX.toFloat(), screenY.toFloat()).windowToMap())
                camera.update()
                println(pinLocation)
                return true
            }
            LEFT -> {
                dragging = true
                prevScreenX = screenX.toFloat()
                prevScreenY = screenY.toFloat()
                return true
            }
            MIDDLE -> {
                screenOffsetX = 0f
                screenOffsetY = 0f
            }


        }
        return false
    }

    override fun keyDown(keycode: Int): Boolean {

        when (keycode) {

        // Icon Filter Keybinds
            HOME -> drawcompass = drawcompass * -1
            END -> drawgrid = drawgrid * -1
            NUMPAD_0 -> filterThrow = filterThrow * -1
            NUMPAD_4 -> filterWeapon = filterWeapon * -1
            NUMPAD_1 -> filterAttach = filterAttach * -1
            NUMPAD_5 -> filterLvl2 = filterLvl2 * -1
            NUMPAD_2 -> filterScope = filterScope * -1
            NUMPAD_6 -> filterHeals = filterHeals * -1
            NUMPAD_3 -> filterAmmo = filterAmmo * -1
            NUMPAD_7 -> camera.zoom = 1 / 8f
            NUMPAD_8 -> camera.zoom = 1 / 12f
            NUMPAD_9 -> camera.zoom = 1 / 24f

        // Toggle Transparent Player Icons
            F7 -> toggleVehicles = toggleVehicles * -1
            F6 -> toggleVNames = toggleVNames * -1

        // Zoom In/Out || Overrides Max/Min Zoom
            F9 -> camera.zoom = camera.zoom + 0.00525f
            F10 -> camera.zoom = camera.zoom - 0.00525f

        // Toggle View Line
            F11 -> toggleView = toggleView * -1


        }
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        if (!dragging) return false
        with(camera) {
            screenOffsetX += (prevScreenX - screenX.toFloat()) * camera.zoom * 500
            screenOffsetY += (prevScreenY - screenY.toFloat()) * camera.zoom * 500
            prevScreenX = screenX.toFloat()
            prevScreenY = screenY.toFloat()
        }
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button == LEFT) {
            dragging = false
            return true
        }
        return false
    }

    override fun create() {
        spriteBatch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        Gdx.input.inputProcessor = this
        camera = OrthographicCamera(windowWidth, windowHeight)
        with(camera) {
            setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
            zoom = 1 / 4f
            update()
            position.set(mapWidth / 2, mapWidth / 2, 0f)
            update()
        }

        itemCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
        fontCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
        alarmSound = Gdx.audio.newSound(Gdx.files.internal("sounds/Alarm.wav"))
        hubpanel = Texture(Gdx.files.internal("images/hub_panel.png"))
        bg_compass = Texture(Gdx.files.internal("images/bg_compass.png"))
        hubpanelblank = Texture(Gdx.files.internal("images/hub_panel_blank_long.png"))
        corpseboximage = Texture(Gdx.files.internal("icons/box.png"))
        airdropimage = Texture(Gdx.files.internal("icons/airdrop.png"))
        vehicle = Texture(Gdx.files.internal("images/vehicle.png"))
        arrow = Texture(Gdx.files.internal("images/arrow.png"))
        plane = Texture(Gdx.files.internal("images/plane.png"))
        player = Texture(Gdx.files.internal("images/player.png"))
        playersight = Texture(Gdx.files.internal("images/green_view_line.png"))
        arrowsight = Texture(Gdx.files.internal("images/red_view_line.png"))
        parachute = Texture(Gdx.files.internal("images/parachute.png"))
        boat = Texture(Gdx.files.internal("images/boat.png"))
        bike = Texture(Gdx.files.internal("images/bike.png"))
        jetski = Texture(Gdx.files.internal("images/jetski.png"))
        bike3x = Texture(Gdx.files.internal("images/bike3x.png"))
        pickup = Texture(Gdx.files.internal("images/pickup.png"))
        van = Texture(Gdx.files.internal("images/van.png"))
        buggy = Texture(Gdx.files.internal("images/buggy.png"))
        grenade = Texture(Gdx.files.internal("images/grenade.png"))
        iconImages = Icons(Texture(Gdx.files.internal("images/item-sprites.png")), 64)
        mapErangelTiles = mutableMapOf()
        mapMiramarTiles = mutableMapOf()
        var cur = 0
        tileZooms.forEach {
            mapErangelTiles[it] = mutableMapOf()
            mapMiramarTiles[it] = mutableMapOf()
            for (i in 1..tileRowCounts[cur]) {
                val y = if (i < 10) "0$i" else "$i"
                mapErangelTiles[it]?.set(y, mutableMapOf())
                mapMiramarTiles[it]?.set(y, mutableMapOf())
                for (j in 1..tileRowCounts[cur]) {
                    val x = if (j < 10) "0$j" else "$j"
                    mapErangelTiles[it]!![y]?.set(x, Texture(Gdx.files.internal("tiles/Erangel/$it/${it}_${y}_$x.png")))
                    mapMiramarTiles[it]!![y]?.set(x, Texture(Gdx.files.internal("tiles/Miramar/$it/${it}_${y}_$x.png")))
                }
            }
            cur++
        }
        mapTiles = mapErangelTiles


        val generatorHub = FreeTypeFontGenerator(Gdx.files.internal("font/AGENCYFB.TTF"))
        val paramHub = FreeTypeFontParameter()
        paramHub.characters = DEFAULT_CHARS
        paramHub.size = 30
        paramHub.color = WHITE
        hubFont = generatorHub.generateFont(paramHub)
        paramHub.color = Color(1f, 1f, 1f, 0.4f)
        hubFontShadow = generatorHub.generateFont(paramHub)
        paramHub.size = 16
        paramHub.color = WHITE
        espFont = generatorHub.generateFont(paramHub)
        paramHub.color = Color(1f, 1f, 1f, 0.2f)
        espFontShadow = generatorHub.generateFont(paramHub)
        val generatorNumber = FreeTypeFontGenerator(Gdx.files.internal("font/NUMBER.TTF"))
        val paramNumber = FreeTypeFontParameter()
        paramNumber.characters = DEFAULT_CHARS
        paramNumber.size = 24
        paramNumber.color = WHITE
        largeFont = generatorNumber.generateFont(paramNumber)
        val generator = FreeTypeFontGenerator(Gdx.files.internal("font/GOTHICB.TTF"))
        val param = FreeTypeFontParameter()
        param.characters = DEFAULT_CHARS
        param.size = 38
        param.color = WHITE
        largeFont = generator.generateFont(param)
        param.size = 15
        param.color = WHITE
        littleFont = generator.generateFont(param)
        param.color = BLACK
        param.size = 10
        nameFont = generator.generateFont(param)
        param.color = WHITE
        param.size = 6
        itemFont = generator.generateFont(param)
        val compaseColor = Color(0f, 0.95f, 1f, 1f)  //Turquoise1
        param.color = compaseColor
        param.size = 10
        compaseFont = generator.generateFont(param)
        param.color = Color(0f, 0f, 0f, 0.5f)
        compaseFontShadow = generator.generateFont(param)
        param.characters = DEFAULT_CHARS
        param.size = 20
        param.color = WHITE
        littleFont = generator.generateFont(param)
        param.color = Color(0f, 0f, 0f, 0.5f)
        littleFontShadow = generator.generateFont(param)

        generatorHub.dispose()
        generatorNumber.dispose()
        generator.dispose()
    }

    override fun render() {
        Gdx.gl.glClearColor(0.417f, 0.417f, 0.417f, 0f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        if (gameStarted)
            mapTiles = if (isErangel) mapErangelTiles else mapMiramarTiles
        else return
        val currentTime = System.currentTimeMillis()

        val (selfX, selfY) = selfCoords
        val selfDir = Vector2(selfX, selfY).sub(preSelfCoords)
        if (selfDir.len() < 1e-8)
            selfDir.set(preDirection)

        //move camera
        camera.position.set(selfX + screenOffsetX, selfY + screenOffsetY, 0f)
        camera.update()
        val cameraTileScale = Math.max(windowWidth, windowHeight) / camera.zoom
        val useScale: Int
        useScale = when {
            cameraTileScale > 4096 -> 4
            cameraTileScale > 2048 -> 3
            cameraTileScale > 1024 -> 2
            cameraTileScale > 512 -> 1
            cameraTileScale > 256 -> 0
            else -> 0
        }
        val (tlX, tlY) = Vector2(0f, 0f).windowToMap()
        val (brX, brY) = Vector2(windowWidth, windowHeight).windowToMap()
        val tileZoom = tileZooms[useScale]
        val tileRowCount = tileRowCounts[useScale]
        val tileSize = tileSizes[useScale]

        val xMin = (tlX.toInt() / tileSize.toInt()).coerceIn(1, tileRowCount)
        val xMax = ((brX.toInt() + tileSize.toInt()) / tileSize.toInt()).coerceIn(1, tileRowCount)
        val yMin = (tlY.toInt() / tileSize.toInt()).coerceIn(1, tileRowCount)
        val yMax = ((brY.toInt() + tileSize.toInt()) / tileSize.toInt()).coerceIn(1, tileRowCount)
        paint(camera.combined) {
            for (i in yMin..yMax) {
                val y = if (i < 10) "0$i" else "$i"
                for (j in xMin..xMax) {
                    val x = if (j < 10) "0$j" else "$j"
                    val tileStartX = (j - 1) * tileSize
                    val tileStartY = (i - 1) * tileSize
                    draw(mapTiles[tileZoom]!![y]!![x], tileStartX, tileStartY, tileSize, tileSize,
                            0, 0, 256, 256,
                            false, true)
                }
            }
        }

        shapeRenderer.projectionMatrix = camera.combined
        Gdx.gl.glEnable(GL20.GL_BLEND)

        drawCircles()

        val typeLocation = EnumMap<Archetype, MutableList<renderInfo>>(Archetype::class.java)
        for ((_, actor) in visualActors)
            typeLocation.compute(actor.Type) { _, v ->
                val list = v ?: ArrayList()
                val (centerX, centerY) = actor.location
                val direction = actor.rotation.y
                list.add(tuple4(actor, centerX, centerY, direction))
                list
            }

        paint(fontCamera.combined) {
            // NUMBER PANEL
            val numText = "$NumAlivePlayers"
            layout.setText(hubFont, numText)
            spriteBatch.draw(hubpanel, windowWidth - 130f, windowHeight - 60f)
            hubFontShadow.draw(spriteBatch, "ALIVE", windowWidth - 85f, windowHeight - 29f)
            hubFont.draw(spriteBatch, "$NumAlivePlayers", windowWidth - 110f - layout.width / 2, windowHeight - 29f)
            val teamText = "$NumAliveTeams"

            if (teamText != numText) {
                layout.setText(hubFont, teamText)
                spriteBatch.draw(hubpanel, windowWidth - 260f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "TEAM", windowWidth - 215f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "$NumAliveTeams", windowWidth - 240f - layout.width / 2, windowHeight - 29f)
            }

            if (teamText != numText) {
                val timeText = "${TotalWarningDuration.toInt() - ElapsedWarningDuration.toInt()}"
                layout.setText(hubFont, timeText)
                spriteBatch.draw(hubpanel, windowWidth - 390f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "SECS", windowWidth - 345f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "${TotalWarningDuration.toInt() - ElapsedWarningDuration.toInt()}", windowWidth - 370f - layout.width / 2, windowHeight - 29f)
            } else {
                spriteBatch.draw(hubpanel, windowWidth - 390f + 130f, windowHeight - 60f)
                hubFontShadow.draw(spriteBatch, "SECS", windowWidth - 345f + 128f, windowHeight - 29f)
                hubFont.draw(spriteBatch, "${TotalWarningDuration.toInt() - ElapsedWarningDuration.toInt()}", windowWidth - 370f + 128f - layout.width / 2, windowHeight - 29f)

            }


            // ITEM ESP FILTER PANEL
            spriteBatch.draw(hubpanelblank, 30f, windowHeight - 60f)

            // This is what you were trying to do
            if (filterWeapon != 1)
                espFont.draw(spriteBatch, "WEAPON", 40f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "WEAPON", 39f, windowHeight - 25f)

            if (filterAttach != 1)
                espFont.draw(spriteBatch, "ATTACH", 40f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "ATTACH", 40f, windowHeight - 42f)

            if (filterLvl2 != 1)
                espFont.draw(spriteBatch, "EQUIP", 100f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "EQUIP", 100f, windowHeight - 25f)

            if (filterScope != 1)
                espFont.draw(spriteBatch, "SCOPE", 98f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "SCOPE", 98f, windowHeight - 42f)

            if (filterHeals != 1)
                espFont.draw(spriteBatch, "MEDS", 150f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "MEDS", 150f, windowHeight - 25f)

            if (filterAmmo != 1)
                espFont.draw(spriteBatch, "AMMO", 150f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "AMMO", 150f, windowHeight - 42f)
            if (drawcompass == 1)
                espFont.draw(spriteBatch, "COMPASS", 200f, windowHeight - 42f)
            else
                espFontShadow.draw(spriteBatch, "COMPASS", 200f, windowHeight - 42f)
            if (filterThrow != 1)
                espFont.draw(spriteBatch, "THROW", 200f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "THROW", 200f, windowHeight - 25f)

            if (drawgrid == 1)
                espFont.draw(spriteBatch, "GRID", 260f, windowHeight - 25f)
            else
                espFontShadow.draw(spriteBatch, "GRID", 260f, windowHeight - 25f)


            val pinDistance = (pinLocation.cpy().sub(selfX, selfY).len() / 100).toInt()
            val (x, y) = pinLocation.mapToWindow()

            safeZoneHint()
            drawPlayerNames(typeLocation[Player])

            //     drawMyself(tuple4(null, selfX, selfY, selfDir.angle()))

            if (drawgrid == 1) {
                drawGrid()
            }


            if (drawcompass == 1) {
                spriteBatch.draw(bg_compass, windowWidth / 2 - 168f, windowHeight / 2 - 168f)
                layout.setText(compaseFont, "0")
                compaseFont.draw(spriteBatch, "0", windowWidth / 2 - layout.width / 2, windowHeight / 2 + layout.height + 150)                  // N
                layout.setText(compaseFont, "45")
                compaseFont.draw(spriteBatch, "45", windowWidth / 2 - layout.width / 2 + 104, windowHeight / 2 + layout.height / 2 + 104)          // NE
                layout.setText(compaseFont, "90")
                compaseFont.draw(spriteBatch, "90", windowWidth / 2 - layout.width / 2 + 147, windowHeight / 2 + layout.height / 2)                // E
                layout.setText(compaseFont, "135")
                compaseFont.draw(spriteBatch, "135", windowWidth / 2 - layout.width / 2 + 106, windowHeight / 2 + layout.height / 2 - 106)          // SE
                layout.setText(compaseFont, "180")
                compaseFont.draw(spriteBatch, "180", windowWidth / 2 - layout.width / 2, windowHeight / 2 + layout.height / 2 - 151)                // S
                layout.setText(compaseFont, "225")
                compaseFont.draw(spriteBatch, "225", windowWidth / 2 - layout.width / 2 - 109, windowHeight / 2 + layout.height / 2 - 109)          // SW
                layout.setText(compaseFont, "270")
                compaseFont.draw(spriteBatch, "270", windowWidth / 2 - layout.width / 2 - 153, windowHeight / 2 + layout.height / 2)                // W
                layout.setText(compaseFont, "315")
                compaseFont.draw(spriteBatch, "315", windowWidth / 2 - layout.width / 2 - 106, windowHeight / 2 + layout.height / 2 + 106)          // NW
            }
            littleFont.draw(spriteBatch, "$pinDistance", x, windowHeight - y)


        }

        // This makes the array empty if the filter is off for performance with an inverted function since arrays are expensive
        scopesToFilter = if (filterScope != 1) {
            arrayListOf("")
        } else {
            arrayListOf("DotSight", "Aimpoint", "Holosight", "CQBSS", "ACOG")
        }


        attachToFilter = if (filterAttach != 1) {
            arrayListOf("")
        } else {
            arrayListOf("AR.Stock", "S.Loops", "CheekPad", "A.Grip", "V.Grip", "U.Ext", "AR.Ext", "S.Ext", "U.ExtQ", "AR.ExtQ", "S.ExtQ", "Choke", "AR.Comp", "FH", "U.Supp", "AR.Supp", "S.Supp")
        }

        weaponsToFilter = if (filterWeapon != 1) {
            arrayListOf("")
        } else {
            arrayListOf("M16A4", "HK416", "Kar98k", "SCAR-L", "AK47", "SKS", "Mini14", "DP28", "UMP", "Vector", "UZI", "Pan")
        }

        healsToFilter = if (filterHeals != 1) {
            arrayListOf("")
        } else {
            arrayListOf("Bandage", "FirstAid", "MedKit", "Drink", "Pain", "Syringe")
        }

        ammoToFilter = if (filterAmmo != 1) {
            arrayListOf("")
        } else {
            arrayListOf("9mm", "45mm", "556mm", "762mm", "300mm")
        }

        throwToFilter = if (filterThrow != 1) {
            arrayListOf("")
        } else {
            arrayListOf("Grenade", "FlashBang", "SmokeBomb", "Molotov")
        }

        level2Filter = if (filterLvl2 != 1) {
            arrayListOf("")
        } else {
            arrayListOf("Bag2", "Arm2", "Helm2")
        }


        val iconScale = 2f / camera.zoom
        paint(itemCamera.combined) {

            droppedItemLocation.values.asSequence().filter { it.second.isNotEmpty() }
                    .forEach {
                        val (x, y) = it.first
                        val items = it.second
                        val (sx, sy) = Vector2(x, y).mapToWindow()
                        val syFix = windowHeight - sy

                        items.forEach {
                            if (it !in weaponsToFilter) {
                                if (it !in scopesToFilter) {
                                    if (it !in attachToFilter) {
                                        if (it !in level2Filter) {
                                            if (it !in ammoToFilter) {
                                                if (it !in healsToFilter) {
                                                    if (it !in throwToFilter) {
                                                        if (iconScale > 20 && sx > 0 && sx < windowWidth && syFix > 0 && syFix < windowHeight) {
                                                            iconImages.setIcon(it)

                                                            // Thanks https://github.com/PubgKnown
                                                            draw(iconImages.icon,
                                                                    sx - iconScale / 2, syFix - iconScale / 2,
                                                                    iconScale, iconScale)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

            //Draw Corpse Icon
            corpseLocation.values.forEach {
                val (x, y) = it
                val (sx, sy) = Vector2(x + 16, y - 16).mapToWindow()
                val syFix = windowHeight - sy
                val iconScale = 2f / camera.zoom
                spriteBatch.draw(corpseboximage, sx - iconScale / 2, syFix + iconScale / 2, iconScale, -iconScale,
                        0, 0, 64, 64,
                        false, true)
            }
            //Draw Airdrop Icon
            airDropLocation.values.forEach {
                val (x, y) = it
                val (sx, sy) = Vector2(x, y).mapToWindow()
                val syFix = windowHeight - sy
                val iconScale = 2f / camera.zoom
                spriteBatch.draw(airdropimage, sx - iconScale / 2, syFix + iconScale / 2, iconScale, -iconScale,
                        0, 0, 64, 64,
                        false, true)
            }



            drawMyself(tuple4(null, selfX, selfY, selfDir.angle()))
            drawPawns(typeLocation)


        }


        val zoom = camera.zoom
        Gdx.gl.glEnable(GL20.GL_BLEND)
        draw(Filled) {
            color = redZoneColor
            circle(RedZonePosition, RedZoneRadius, 100)

            color = visionColor
            circle(selfX, selfY, visionRadius, 100)

            color = pinColor
            circle(pinLocation, pinRadius * zoom, 10)


        }

        drawAttackLine(currentTime)

        preSelfCoords.set(selfX, selfY)
        preDirection = selfDir
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }


    private fun drawMyself(actorInfo: renderInfo) {
        val (_, x, y, dir) = actorInfo
        val (sx, sy) = Vector2(x, y).mapToWindow()
        if (toggleView == 1) {
            // Just draw them both at the same time to avoid player not drawing ¯\_(ツ)_/¯
            spriteBatch.draw(
                    player,
                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                    dir * -1, 0, 0, 64, 64, true, false)

            spriteBatch.draw(
                    playersight,
                    sx + 1, windowHeight - sy - 2,
                    2.toFloat() / 2,
                    2.toFloat() / 2,
                    12.toFloat(), 2.toFloat(),
                    10f, 10f,
                    dir * -1, 0, 0, 512, 64, true, false)
        } else {

            spriteBatch.draw(
                    player,
                    sx, windowHeight - sy - 2, 4.toFloat() / 2,
                    4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                    dir * -1, 0, 0, 64, 64, true, false)
        }
    }

    private fun drawAttackLine(currentTime: Long) {
        while (attacks.isNotEmpty()) {
            val (A, B) = attacks.poll()
            attackLineStartTime.add(Triple(A, B, currentTime))
        }
        if (attackLineStartTime.isEmpty()) return
        draw(Line) {
            val iter = attackLineStartTime.iterator()
            while (iter.hasNext()) {
                val (A, B, st) = iter.next()
                if (A == selfID || B == selfID) {
                    val enemyID = if (A == selfID) B else A
                    val actorEnemyID = playerStateToActor[enemyID]
                    if (actorEnemyID == null) {
                        iter.remove()
                        continue
                    }
                    val actorEnemy = actors[actorEnemyID]
                    if (actorEnemy == null || currentTime - st > attackMeLineDuration) {
                        iter.remove()
                        continue
                    }
                    color = attackLineColor
                    val (xA, yA) = selfCoords
                    val (xB, yB) = actorEnemy.location
                    line(xA, yA, xB, yB)
                } else {
                    val actorAID = playerStateToActor[A]
                    val actorBID = playerStateToActor[B]
                    if (actorAID == null || actorBID == null) {
                        iter.remove()
                        continue
                    }
                    val actorA = actors[actorAID]
                    val actorB = actors[actorBID]
                    if (actorA == null || actorB == null || currentTime - st > attackLineDuration) {
                        iter.remove()
                        continue
                    }
                    color = attackLineColor
                    val (xA, yA) = actorA.location
                    val (xB, yB) = actorB.location
                    line(xA, yA, xB, yB)
                }
            }
        }
    }

    private fun drawCircles() {
        Gdx.gl.glLineWidth(2f)
        draw(Line) {
            //vision circle

            color = safeZoneColor
            circle(PoisonGasWarningPosition, PoisonGasWarningRadius, 100)

            color = BLUE
            circle(SafetyZonePosition, SafetyZoneRadius, 100)

            if (PoisonGasWarningPosition.len() > 0) {
                color = safeDirectionColor
                line(selfCoords, PoisonGasWarningPosition)
            }

        }

        Gdx.gl.glLineWidth(1f)
    }

    private fun drawGrid() {
        draw(Filled) {
            val unit = gridWidth / 8
            val unit2 = unit / 10
            color = BLACK
            //thin grid
            for (i in 0..7)
                for (j in 0..9) {
                    rectLine(0f, i * unit + j * unit2, gridWidth, i * unit + j * unit2, 100f)
                    rectLine(i * unit + j * unit2, 0f, i * unit + j * unit2, gridWidth, 100f)
                }
            color = GRAY
            //thick grid
            for (i in 0..7) {
                rectLine(0f, i * unit, gridWidth, i * unit, 500f)
                rectLine(i * unit, 0f, i * unit, gridWidth, 500f)
            }
        }
    }

    private fun drawPawns(typeLocation: EnumMap<Archetype, MutableList<renderInfo>>) {
        val iconScale = 2f / camera.zoom
        for ((type, actorInfos) in typeLocation) {
            when (type) {
                TwoSeatBoat -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {

                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "JSKI", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                jetski,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, true, false
                        )
                    }
                }
                SixSeatBoat -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "BOAT", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                boat,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, true, false
                        )
                    }
                }
                TwoSeatBike -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()


                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "BIKE", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                bike,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), iconScale / 3, iconScale / 3,
                                dir * -1, 0, 0, 64, 64, true, false
                        )
                    }
                }
                TwoSeatCar -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "BUGGY", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                buggy,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, false, false
                        )
                    }
                }
                ThreeSeatCar -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "BIKE", sx + 15, windowHeight - sy - 2)

                        val selfDir = Vector2(x, y).sub(preSelfCoords)
                        if (selfDir.len() < 1e-8) selfDir.set(preDirection)
                        spriteBatch.draw(
                                bike3x,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2, 4.toFloat() / 2,
                                4.toFloat(), 4.toFloat(), iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, true, false
                        )
                    }

                }
                FourSeatDU -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "CAR", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                vehicle,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 128, 128, false, false
                        )
                    }
                }
                FourSeatP -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "PICKUP", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                pickup,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, false, false
                        )
                    }
                }
                SixSeatCar -> actorInfos?.forEach {
                    if (toggleVehicles != 1) {
                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        if (toggleVNames != 1) compaseFont.draw(spriteBatch, "VAN", sx + 15, windowHeight - sy - 2)

                        spriteBatch.draw(
                                van,
                                sx + 2, windowHeight - sy - 2,
                                2.toFloat() / 2, 2.toFloat() / 2,
                                2.toFloat(), 2.toFloat(),
                                iconScale / 2, iconScale / 2,
                                dir * -1, 0, 0, 64, 64, false, false
                        )
                    }
                }
                Player -> actorInfos?.forEach {

                    for ((_, _) in typeLocation) {
                        val (actor, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()


                        if (isTeamMate(actor)) {

                            if (toggleView == 1) {
                                spriteBatch.draw(
                                        player,
                                        sx, windowHeight - sy - 2, 4.toFloat() / 2,
                                        4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                                        dir * -1, 0, 0, 64, 64, true, false)

                                spriteBatch.draw(
                                        playersight,
                                        sx + 1, windowHeight - sy - 2,
                                        2.toFloat() / 2,
                                        2.toFloat() / 2,
                                        12.toFloat(), 2.toFloat(),
                                        10f, 10f,
                                        dir * -1, 0, 0, 512, 64, true, false)
                            } else{
                                spriteBatch.draw(
                                        player,
                                        sx, windowHeight - sy - 2, 4.toFloat() / 2,
                                        4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                                        dir * -1, 0, 0, 64, 64, true, false)
                            }

                        } else {

                            if (toggleView == 1) {
                                // Draw them both at same time to avoid it disappearing
                                spriteBatch.draw(
                                        arrow,
                                        sx, windowHeight - sy - 2, 4.toFloat() / 2,
                                        4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                                        dir * -1, 0, 0, 64, 64, true, false)

                                spriteBatch.draw(
                                        arrowsight,
                                        sx + 1, windowHeight - sy - 2,
                                        2.toFloat() / 2,
                                        2.toFloat() / 2,
                                        12.toFloat(), 2.toFloat(),
                                        10f, 10f,
                                        dir * -1, 0, 0, 512, 64, true, false)
                            } else{

                                spriteBatch.draw(
                                        arrow,
                                        sx, windowHeight - sy - 2, 4.toFloat() / 2,
                                        4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                                        dir * -1, 0, 0, 64, 64, true, false)
                            }
                        }
                    }

                }
                Parachute -> actorInfos?.forEach {
                    for ((_, _) in typeLocation) {

                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        spriteBatch.draw(
                                parachute,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 8f, 8f,
                                dir * -1, 0, 0, 128, 128, true, false)

                    }
                }
                Plane -> actorInfos?.forEach {
                    for ((_, _) in typeLocation) {

                        val (_, x, y, dir) = it
                        val (sx, sy) = Vector2(x, y).mapToWindow()

                        spriteBatch.draw(
                                plane,
                                sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                                4.toFloat() / 2, 5.toFloat(), 5.toFloat(), 10f, 10f,
                                dir * -1, 0, 0, 64, 64, true, false)

                    }
                }
                Grenade -> actorInfos?.forEach {
                    val (_, x, y, dir) = it
                    val (sx, sy) = Vector2(x, y).mapToWindow()
                    spriteBatch.draw(
                            grenade,
                            sx + 2, windowHeight - sy - 2, 4.toFloat() / 2,
                            4.toFloat() / 2, 4.toFloat(), 4.toFloat(), 5f, 5f,
                            dir * -1, 0, 0, 16, 16, true, false)
                }

                else -> {
                    //nothing
                }
            }

        }
    }

    private fun drawPlayerNames(players: MutableList<renderInfo>?) {
        players?.forEach {
            val (actor, x, y, _) = it
            actor!!
            val playerStateGUID = actorWithPlayerState[actor.netGUID] ?: return@forEach
            val name = playerNames[playerStateGUID] ?: return@forEach
            val (sx, sy) = Vector2(x, y).mapToWindow()
            query(name)
            nameFont.draw(spriteBatch, "$name "/* +
                    "/($numKills)\n$teamNumber*/
                    , sx + 2, windowHeight - sy - 2)
        }
    }


    private var lastPlayTime = System.currentTimeMillis()
    private fun safeZoneHint() {
        if (PoisonGasWarningPosition.len() > 0) {
            val dir = PoisonGasWarningPosition.cpy().sub(selfCoords)
            val road = dir.len() - PoisonGasWarningRadius
            if (road > 0) {
                val runningTime = (road / runSpeed).toInt()
                val (x, y) = dir.nor().scl(road).add(selfCoords).mapToWindow()
                littleFont.draw(spriteBatch, "$runningTime", x, windowHeight - y)
                val remainingTime = (TotalWarningDuration - ElapsedWarningDuration).toInt()
                if (remainingTime == 60 && runningTime > remainingTime) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastPlayTime > 10000) {
                        lastPlayTime = currentTime
                        alarmSound.play()
                    }
                }
            }
        }
    }

    private inline fun draw(type: ShapeType, draw: ShapeRenderer.() -> Unit) {
        shapeRenderer.apply {
            begin(type)
            draw()
            end()
        }
    }

    private inline fun paint(matrix: Matrix4, paint: SpriteBatch.() -> Unit) {
        spriteBatch.apply {
            projectionMatrix = matrix
            begin()
            paint()
            end()
        }
    }

    private fun ShapeRenderer.circle(loc: Vector2, radius: Float, segments: Int) {
        circle(loc.x, loc.y, radius, segments)
    }


    private fun isTeamMate(actor: Actor?): Boolean {
        if (actor != null) {
            val playerStateGUID = actorWithPlayerState[actor.netGUID]
            if (playerStateGUID != null) {
                val name = playerNames[playerStateGUID] ?: return false
                if (name in team)
                    return true
            }
        }
        return false
    }

    override fun resize(width: Int, height: Int) {
        windowWidth = width.toFloat()
        windowHeight = height.toFloat()
        camera.setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
        itemCamera.setToOrtho(false, windowWidth, windowHeight)
        fontCamera.setToOrtho(false, windowWidth, windowHeight)
    }

    override fun pause() {
    }

    override fun resume() {
    }

    override fun dispose() {
        deregister(this)
        alarmSound.dispose()
        nameFont.dispose()
        largeFont.dispose()
        littleFont.dispose()
        corpseboximage.dispose()
        airdropimage.dispose()
        vehicle.dispose()
        iconImages.iconSheet.dispose()
        compaseFont.dispose()
        compaseFontShadow.dispose()

        var cur = 0
        tileZooms.forEach {
            for (i in 1..tileRowCounts[cur]) {
                val y = if (i < 10) "0$i" else "$i"
                for (j in 1..tileRowCounts[cur]) {
                    val x = if (j < 10) "0$j" else "$j"
                    mapErangelTiles[it]!![y]!![x]!!.dispose()
                    mapMiramarTiles[it]!![y]!![x]!!.dispose()
                    mapTiles[it]!![y]!![x]!!.dispose()
                }
            }
            cur++
        }
        spriteBatch.dispose()
        shapeRenderer.dispose()
    }
}
