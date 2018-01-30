package pubg.radar.ui

import com.badlogic.gdx.*
import com.badlogic.gdx.Input.Buttons.RIGHT
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.backends.lwjgl3.*
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.Color.*
import com.badlogic.gdx.graphics.g2d.*
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.*
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.*
import com.badlogic.gdx.math.*
import pubg.radar.*
import pubg.radar.deserializer.channel.ActorChannel.Companion.airDropLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.visualActors
import pubg.radar.http.PlayerProfile.Companion.completedPlayerInfo
import pubg.radar.http.PlayerProfile.Companion.pendingPlayerInfo
import pubg.radar.http.PlayerProfile.Companion.query
import pubg.radar.sniffer.Sniffer.Companion.localAddr
import pubg.radar.sniffer.Sniffer.Companion.preDirection
import pubg.radar.sniffer.Sniffer.Companion.preSelfCoords
import pubg.radar.sniffer.Sniffer.Companion.selfCoords
import pubg.radar.sniffer.Sniffer.Companion.sniffOption
import pubg.radar.struct.*
import pubg.radar.struct.Archetype.*
import pubg.radar.struct.Archetype.Plane
import pubg.radar.struct.cmd.ActorCMD.actorWithPlayerState
import pubg.radar.struct.cmd.GameStateCMD.ElapsedWarningDuration
import pubg.radar.struct.cmd.GameStateCMD.MatchElapsedMinutes
import pubg.radar.struct.cmd.GameStateCMD.NumAlivePlayers
import pubg.radar.struct.cmd.GameStateCMD.NumAliveTeams
import pubg.radar.struct.cmd.GameStateCMD.PoisonGasWarningPosition
import pubg.radar.struct.cmd.GameStateCMD.PoisonGasWarningRadius
import pubg.radar.struct.cmd.GameStateCMD.RedZonePosition
import pubg.radar.struct.cmd.GameStateCMD.RedZoneRadius
import pubg.radar.struct.cmd.GameStateCMD.SafetyZonePosition
import pubg.radar.struct.cmd.GameStateCMD.SafetyZoneRadius
import pubg.radar.struct.cmd.GameStateCMD.TotalWarningDuration
import pubg.radar.struct.cmd.PlayerStateCMD.playerStateAndNames
import pubg.radar.util.tuple4
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*

typealias renderInfo = tuple4<Actor, Float, Float, Float>

fun Float.d(n: Int) = String.format("%.${n}f", this)
class GLMap: InputAdapter(), ApplicationListener, GameListener {
  companion object {
    operator fun Vector3.component1(): Float = x
    operator fun Vector3.component2(): Float = y
    operator fun Vector3.component3(): Float = z
    operator fun Vector2.component1(): Float = x
    operator fun Vector2.component2(): Float = y
    
    val spawnErangel = Vector2(795548.3f, 17385.875f)
    val spawnDesert = Vector2(78282f, 731746f)
  }
  
  override fun onGameStart() {
    preSelfCoords.set(if (isErangel) spawnErangel else spawnDesert)
    selfCoords.set(preSelfCoords)
    preDirection.setZero()
  }
  
  override fun onGameOver() {
    camera.zoom = 1 / 4f
    
    aimStartTime.clear()
    pinLocation.setZero()
  }
  
  fun show() {
    val config = Lwjgl3ApplicationConfiguration()
    config.setTitle("[${localAddr.hostAddress} ${sniffOption.name}] - PUBG Radar")
    config.useOpenGL3(true, 3, 3)
    config.setWindowedMode(1000, 1000)
    config.setResizable(true)
    config.setBackBufferConfig(8, 8, 8, 8, 32, 0, 8)
    register(this)
    Lwjgl3Application(this, config)
  }
  
  lateinit var spriteBatch: SpriteBatch
  lateinit var shapeRenderer: ShapeRenderer
  lateinit var mapErangel: Texture
  lateinit var mapMiramar: Texture
  lateinit var map: Texture
  lateinit var largeFont: BitmapFont
  lateinit var littleFont: BitmapFont
  lateinit var nameFont: BitmapFont
  lateinit var fontCamera: OrthographicCamera
  lateinit var camera: OrthographicCamera
  lateinit var alarmSound: Sound
  
  val layout = GlyphLayout()
  var windowWidth = initialWindowWidth
  var windowHeight = initialWindowWidth
  
  val aimStartTime = HashMap<NetworkGUID, Long>()
  val pinLocation = Vector2()
  
  fun Vector2.windowToMap() =
      Vector2(selfCoords.x + (x - windowWidth / 2.0f) * camera.zoom * windowToMapUnit,
              selfCoords.y + (y - windowHeight / 2.0f) * camera.zoom * windowToMapUnit)
  
  fun Vector2.mapToWindow() =
      Vector2((x - selfCoords.x) / (camera.zoom * windowToMapUnit) + windowWidth / 2.0f,
              (y - selfCoords.y) / (camera.zoom * windowToMapUnit) + windowHeight / 2.0f)
  
  override fun scrolled(amount: Int): Boolean {
    camera.zoom *= 1.1f.pow(amount)
    return true
  }
  
  override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
    if (button == RIGHT) {
      pinLocation.set(pinLocation.set(screenX.toFloat(), screenY.toFloat()).windowToMap())
      return true
    }
    return false
  }
  
  override fun create() {
    spriteBatch = SpriteBatch()
    shapeRenderer = ShapeRenderer()
    Gdx.input.inputProcessor = this;
    camera = OrthographicCamera(windowWidth, windowHeight)
    with(camera) {
      setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
      zoom = 1 / 4f
      update()
      position.set(mapWidth / 2, mapWidth / 2, 0f)
      update()
    }
    
    fontCamera = OrthographicCamera(initialWindowWidth, initialWindowWidth)
    alarmSound = Gdx.audio.newSound(Gdx.files.internal("Alarm.wav"))
    mapErangel = Texture(Gdx.files.internal("Erangel.bmp"))
    mapMiramar = Texture(Gdx.files.internal("Miramar.bmp"))
    map = mapErangel
    
    val generator = FreeTypeFontGenerator(Gdx.files.internal("GOTHICB.TTF"))
    val param = FreeTypeFontParameter()
    param.size = 50
    param.characters = DEFAULT_CHARS
    param.color = RED
    largeFont = generator.generateFont(param)
    param.size = 20
    param.color = WHITE
    littleFont = generator.generateFont(param)
    param.color = BLACK
    param.size = 15
    nameFont = generator.generateFont(param)
    generator.dispose()
    
  }
  
  val dirUnitVector = Vector2(1f, 0f)
  override fun render() {
    Gdx.gl.glClearColor(0.417f, 0.417f, 0.417f, 0f)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    if (gameStarted)
      map = if (isErangel) mapErangel else mapMiramar
    else
      return
    
    val (selfX, selfY) = selfCoords
    val selfDir = Vector2(selfX, selfY).sub(preSelfCoords)
    if (selfDir.len() < 1e-8)
      selfDir.set(preDirection)
    
    //move camera
    camera.position.set(selfX, selfY, 0f)
    camera.update()
    
    //draw map
    paint(camera.combined) {
      draw(map, 0f, 0f, mapWidth, mapWidth,
           0, 0, mapWidthCropped, mapWidthCropped,
           false, true)
    }
    
    shapeRenderer.projectionMatrix = camera.combined
    Gdx.gl.glEnable(GL20.GL_BLEND)
    
    draw(Filled) {
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
      largeFont.draw(spriteBatch, "$NumAlivePlayers/$NumAliveTeams\n" +
                                  "${MatchElapsedMinutes}min\n" +
                                  "${ElapsedWarningDuration.toInt()}/${TotalWarningDuration.toInt()}\n", 10f, windowHeight - 10f)
      val time = (pinLocation.cpy().sub(selfX, selfY).len() / runSpeed).toInt()
      val (x, y) = pinLocation.mapToWindow()
      littleFont.draw(spriteBatch, "$time", x, windowHeight - y)
      safeZoneHint()
      drawPlayerNames(typeLocation[Player])
    }
    
    val zoom = camera.zoom
    val vehicle2Radius = vehicle2Width * zoom
    val vehicle4Radius = vehicle4Width * zoom
    val vehicle6Radius = vehicle6Width * zoom
    var playersInVision = 0
    
    Gdx.gl.glEnable(GL20.GL_BLEND)
    draw(Filled) {
      color = redZoneColor
      circle(RedZonePosition, RedZoneRadius, 100)
      
      color = visionColor
      circle(selfX, selfY, visionRadius, 100)
      
      color = pinColor
      circle(pinLocation, pinRadius * zoom, 10)
      
      droppedItemLocation.values.asSequence().filter { it.second.isNotEmpty() }
          .forEach {
            val (x, y) = it.first
            val items = it.second
            val finalColor = it.third
            
            if (finalColor.a == 0f)
              finalColor.set(
                  when {
                    "98k" in items || "m416" in items || "scar" in items -> rareWeaponColor
                    "armor3" in items || "helmet3" in items -> rareArmorColor
                    "4x" in items || "8x" in items -> rareScopeColor
                    "Extended" in items || "Choke" in items || "Compensator" in items -> rareAttachColor
                    "heal" in items || "drink" in items -> healItemColor
                    else -> normalItemColor
                  })
            
            val rare = when (finalColor) {
              rareWeaponColor, rareArmorColor, rareScopeColor, rareAttachColor -> true
              else -> false
            }
            val backgroundRadius = (itemRadius + 2000f) * zoom
            val radius = itemRadius * zoom
            if (rare) {
              color = BLACK
              rect(x - backgroundRadius, y - backgroundRadius, backgroundRadius * 2, backgroundRadius * 2)
              color = finalColor
              rect(x - radius, y - radius, radius * 2, radius * 2)
            } else {
              color = BLACK
              circle(x, y, backgroundRadius, 10)
              color = finalColor
              circle(x, y, radius, 10)
            }
          }
      airDropLocation.values.forEach {
        val (x, y) = it
        val backgroundRadius = (airDropRadius + 2000) * zoom
        val airDropRadius = airDropRadius * zoom
        color = BLACK
        rect(x - backgroundRadius, y - backgroundRadius, backgroundRadius * 2, backgroundRadius * 2)
        color = BLUE
        rect(x, y - airDropRadius, airDropRadius, airDropRadius * 2)
        color = RED
        rect(x - airDropRadius, y - airDropRadius, airDropRadius, airDropRadius * 2)
      }
      val currentTime = System.currentTimeMillis()
      //draw self
      drawPlayer(LIME, tuple4(null, selfX, selfY, selfDir.angle()))
      for ((type, actorInfos) in typeLocation) {
        when (type) {
          TwoSeatBoat -> actorInfos?.forEach {
            drawVehicle(boatColor, it, vehicle2Radius, vehicle6Radius)
          }
          SixSeatBoat -> actorInfos?.forEach {
            drawVehicle(boatColor, it, vehicle4Radius, vehicle6Radius)
          }
          TwoSeatCar -> actorInfos?.forEach {
            drawVehicle(carColor, it, vehicle2Radius, vehicle6Radius)
          }
          ThreeSeatCar -> actorInfos?.forEach {
            drawVehicle(carColor, it, vehicle2Radius, vehicle6Radius)
          }
          FourSeatCar -> actorInfos?.forEach {
            drawVehicle(carColor, it, vehicle4Radius, vehicle6Radius)
          }
          SixSeatCar -> actorInfos?.forEach {
            drawVehicle(carColor, it, vehicle2Radius, vehicle6Radius)
          }
          Plane -> actorInfos?.forEach {
            drawPlayer(planeColor, it)
          }
          Player -> actorInfos?.forEach {
            drawPlayer(playerColor, it)
            playersInVision++
            
            aimAtMe(it, selfX, selfY, currentTime, zoom)
          }
          Parachute -> actorInfos?.forEach {
            drawPlayer(parachuteColor, it)
          }
          Grenade -> actorInfos?.forEach {
            drawPlayer(WHITE, it, false)
          }
          else -> {
//            actorInfos?.forEach {
//            bugln { "${it._1!!.archetype.pathName} ${it._1.location}" }
//            drawPlayer(BLACK, it)
//            }
          }
        }
      }
    }
    
    preSelfCoords.set(selfX, selfY)
    preDirection = selfDir
    
    Gdx.gl.glDisable(GL20.GL_BLEND)
  }
  
  fun drawPlayerNames(players: MutableList<renderInfo>?) {
    players?.forEach {
      val (actor, x, y, _) = it
      actor!!
      val playerStateGUID = actorWithPlayerState[actor.netGUID] ?: return@forEach
      val name = playerStateAndNames[playerStateGUID] ?: return@forEach
      val (sx, sy) = Vector2(x, y).mapToWindow()
      query(name)
      if (completedPlayerInfo.containsKey(name)) {
        val info = completedPlayerInfo[name]!!
        val desc = "$name(${info.roundMostKill})\n${info.win}/${info.totalPlayed}\n${info.killDeathRatio.d(2)}/${info.headshotKillRatio.d(2)}"
        nameFont.draw(spriteBatch, desc, sx + 2, windowHeight - sy - 2)
      } else
        nameFont.draw(spriteBatch, name, sx + 2, windowHeight - sy - 2)
    }
    val profileText = "${completedPlayerInfo.size}/${completedPlayerInfo.size + pendingPlayerInfo.size}"
    layout.setText(largeFont, profileText)
    largeFont.draw(spriteBatch, profileText, windowWidth - layout.width, windowHeight - 10f)
  }
  
  var lastPlayTime = System.currentTimeMillis()
  fun safeZoneHint() {
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
  
  inline fun draw(type: ShapeType, draw: ShapeRenderer.() -> Unit) {
    shapeRenderer.apply {
      begin(type)
      draw()
      end()
    }
  }
  
  inline fun paint(matrix: Matrix4, paint: SpriteBatch.() -> Unit) {
    spriteBatch.apply {
      projectionMatrix = matrix
      begin()
      paint()
      end()
    }
  }
  
  fun ShapeRenderer.circle(loc: Vector2, radius: Float, segments: Int) {
    circle(loc.x, loc.y, radius, segments)
  }
  
  fun ShapeRenderer.aimAtMe(it: renderInfo, selfX: Float, selfY: Float, currentTime: Long, zoom: Float) {
    //draw aim line
    val (_, x, y, dir) = it
    val actorID = it._1!!.netGUID
    val dirVec = dirUnitVector.cpy().rotate(dir)
    val focus = Vector2(selfX - x, selfY - y)
    val distance = focus.len()
    var aim = false
    if (distance < aimLineRange && distance > aimCircleRadius) {
      val aimAngle = focus.angle(dirVec)
      if (aimAngle.absoluteValue < asin(aimCircleRadius / distance) * MathUtils.radiansToDegrees) {//aim
        aim = true
        aimStartTime.compute(actorID) { _, startTime ->
          if (startTime == null) currentTime
          else {
            if (currentTime - startTime > aimTimeThreshold) {
              color = aimLineColor
              rectLine(x, y, selfX, selfY, aimLineWidth * zoom)
            }
            startTime
          }
        }
      }
    }
    if (!aim)
      aimStartTime.remove(actorID)
  }
  
  fun ShapeRenderer.drawPlayer(pColor: Color?, actorInfo: renderInfo, drawSight: Boolean = true) {
    val zoom = camera.zoom
    val backgroundRadius = (playerRadius + 2000f) * zoom
    val playerRadius = playerRadius * zoom
    val directionRadius = directionRadius * zoom
    
    color = BLACK
    val (_, x, y, dir) = actorInfo
    circle(x, y, backgroundRadius, 10)
    
    color = pColor
    circle(x, y, playerRadius, 10)
    
    if (drawSight) {
      color = sightColor
      arc(x, y, directionRadius, dir - fov / 2, fov, 10)
    }
  }
  
  fun ShapeRenderer.drawVehicle(_color: Color, actorInfo: renderInfo,
                                width: Float, height: Float) {
    
    val (actor, x, y, dir) = actorInfo
    val v_x = actor!!.velocity.x
    val v_y = actor.velocity.y
    
    val dirVector = dirUnitVector.cpy().rotate(dir).scl(height / 2)
    color = BLACK
    val backVector = dirVector.cpy().nor().scl(height / 2 + 2200f * camera.zoom)
    rectLine(x - backVector.x, y - backVector.y,
             x + backVector.x, y + backVector.y, width + 4400f * camera.zoom)
    color = _color
    rectLine(x - dirVector.x, y - dirVector.y,
             x + dirVector.x, y + dirVector.y, width)
    
    if (actor.beAttached || v_x * v_x + v_y * v_y > 40) {
      color = playerColor
      circle(x, y, playerRadius * camera.zoom, 10)
    }
  }
  
  override fun resize(width: Int, height: Int) {
    windowWidth = width.toFloat()
    windowHeight = height.toFloat()
    camera.setToOrtho(true, windowWidth * windowToMapUnit, windowHeight * windowToMapUnit)
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
    mapErangel.dispose()
    mapMiramar.dispose()
    spriteBatch.dispose()
    shapeRenderer.dispose()
  }
  
}