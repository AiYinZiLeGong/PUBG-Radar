package pubg.radar.ui

import com.badlogic.gdx.*
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
import pubg.radar.deserializer.channel.ActorChannel.Companion.actorHasWeapons
import pubg.radar.deserializer.channel.ActorChannel.Companion.actors
import pubg.radar.deserializer.channel.ActorChannel.Companion.airDropLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.corpseLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.droppedItemLocation
import pubg.radar.deserializer.channel.ActorChannel.Companion.visualActors
import pubg.radar.deserializer.channel.ActorChannel.Companion.weapons
import pubg.radar.sniffer.Sniffer.Companion.localAddr
import pubg.radar.sniffer.Sniffer.Companion.sniffOption
import pubg.radar.struct.*
import pubg.radar.struct.Archetype.*
import pubg.radar.struct.Archetype.Plane
import pubg.radar.struct.cmd.*
import pubg.radar.struct.cmd.ActorCMD.actorHealth
import pubg.radar.struct.cmd.ActorCMD.actorWithPlayerState
import pubg.radar.struct.cmd.ActorCMD.playerStateToActor
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
import pubg.radar.struct.cmd.PlayerStateCMD.attacks
import pubg.radar.struct.cmd.PlayerStateCMD.playerNames
import pubg.radar.struct.cmd.PlayerStateCMD.playerNumKills
import pubg.radar.struct.cmd.PlayerStateCMD.selfID
import pubg.radar.struct.cmd.PlayerStateCMD.selfStateID
import pubg.radar.struct.cmd.PlayerStateCMD.teamNumbers
import pubg.radar.util.tuple4
import wumo.pubg.struct.cmd.TeamCMD.team
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
    
  }
  
  init {
    register(this)
  }
  
  override fun onGameStart() {
    selfCoords.setZero()
    selfAttachTo = null
  }
  
  override fun onGameOver() {
    camera.zoom = 1 / 4f
    
    aimStartTime.clear()
    attackLineStartTime.clear()
    pinLocation.setZero()
  }
  
  fun show() {
    val config = Lwjgl3ApplicationConfiguration()
    config.setTitle("[${localAddr.hostAddress} ${sniffOption.name}] - PUBG Radar")
    config.useOpenGL3(true, 3, 3)
    config.setWindowedMode(1000, 1000)
    config.setResizable(true)
    config.setBackBufferConfig(8, 8, 8, 8, 32, 0, 8)
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
  val attackLineStartTime = LinkedList<Triple<NetworkGUID, NetworkGUID, Long>>()
  val pinLocation = Vector2()
  
  fun windowToMap(x: Float, y: Float) =
      Vector2(selfCoords.x + (x - windowWidth / 2.0f) * camera.zoom * windowToMapUnit,
              selfCoords.y + (y - windowHeight / 2.0f) * camera.zoom * windowToMapUnit)
  
  fun mapToWindow(x: Float, y: Float) =
      Vector2((x - selfCoords.x) / (camera.zoom * windowToMapUnit) + windowWidth / 2.0f,
              (y - selfCoords.y) / (camera.zoom * windowToMapUnit) + windowHeight / 2.0f)
  
  fun Vector2.mapToWindow() = mapToWindow(x, y)
  
  override fun scrolled(amount: Int): Boolean {
    camera.zoom *= 1.1f.pow(amount)
    return true
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
    val currentTime = System.currentTimeMillis()
    selfAttachTo?.apply {
      selfCoords.set(location.x, location.y)
      selfDirection = rotation.y
    }
    val (selfX, selfY) = selfCoords
    
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
    
    drawGrid()
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
      largeFont.draw(spriteBatch, "$NumAlivePlayers/$NumAliveTeams\n" +
                                  "${MatchElapsedMinutes}min\n" +
                                  "${ElapsedWarningDuration.toInt()}/${TotalWarningDuration.toInt()}\n", 10f, windowHeight - 10f)
      safeZoneHint()
      drawPlayerInfos(typeLocation[Player], selfX, selfY)
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
      //draw self
      drawPlayer(LIME, tuple4(null, selfX, selfY, selfDirection))
      drawItem()
      drawAirDrop(zoom)
      drawCorpse()
      drawAPawn(typeLocation, selfX, selfY, zoom, currentTime)
    }
    
    drawAttackLine(currentTime)
    
    Gdx.gl.glDisable(GL20.GL_BLEND)
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
        if (A == selfStateID || B == selfStateID) {
          if (A != B) {
            val otherGUID = playerStateToActor[if (A == selfStateID) B else A]
            if (otherGUID == null) {
              iter.remove()
              continue
            }
            val other = actors[otherGUID]
            if (other == null || currentTime - st > attackLineDuration) {
              iter.remove()
              continue
            }
            color = attackLineColor
            val (xA, yA) = other.location
            val (xB, yB) = selfCoords
            line(xA, yA, xB, yB)
          }
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
  
  private fun ShapeRenderer.drawAPawn(typeLocation: EnumMap<Archetype, MutableList<renderInfo>>,
                                      selfX: Float, selfY: Float,
                                      zoom: Float,
                                      currentTime: Long) {
    for ((type, actorInfos) in typeLocation) {
      when (type) {
        TwoSeatBoat -> actorInfos?.forEach {
          drawVehicle(boatColor, it, vehicle2Width, vehicle6Width)
        }
        SixSeatBoat -> actorInfos?.forEach {
          drawVehicle(boatColor, it, vehicle4Width, vehicle6Width)
        }
        TwoSeatCar -> actorInfos?.forEach {
          drawVehicle(carColor, it, vehicle2Width, vehicle6Width)
        }
        ThreeSeatCar -> actorInfos?.forEach {
          drawVehicle(carColor, it, vehicle2Width, vehicle6Width)
        }
        FourSeatCar -> actorInfos?.forEach {
          drawVehicle(carColor, it, vehicle4Width, vehicle6Width)
        }
        SixSeatCar -> actorInfos?.forEach {
          drawVehicle(carColor, it, vehicle2Width, vehicle6Width)
        }
        Plane -> actorInfos?.forEach {
          drawPlayer(planeColor, it)
        }
        Player -> actorInfos?.forEach {
          drawPlayer(playerColor, it)
          
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
  
  private fun ShapeRenderer.drawCorpse() {
    corpseLocation.values.forEach {
      val (x, y) = it
      val backgroundRadius = (corpseRadius + 50f)
      val radius = corpseRadius
      color = BLACK
      rect(x - backgroundRadius, y - backgroundRadius, backgroundRadius * 2, backgroundRadius * 2)
      color = corpseColor
      rect(x - radius, y - radius, radius * 2, radius * 2)
      color = BLACK
      rectLine(x - radius, y, x + radius, y, 50f)
      rectLine(x, y - radius, x, y + radius, 50f)
    }
  }
  
  private fun ShapeRenderer.drawAirDrop(zoom: Float) {
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
  }
  
  private fun ShapeRenderer.drawItem() {
    droppedItemLocation.values
        .forEach {
          val (x, y) = it._1
          val items = it._2
          
          val finalColor = when {
            "98k" in items || "m416" in items || "Choke" in items || "scar" in items -> rareWeaponColor
            "armor3" in items || "helmet3" in items -> rareArmorColor
            "4x" in items || "8x" in items -> rareScopeColor
            "Extended" in items || "Compensator" in items -> rareAttachColor
            "heal" in items || "drink" in items -> healItemColor
            else -> normalItemColor
          }
          
          val rare = when (finalColor) {
            rareWeaponColor, rareArmorColor, rareScopeColor, rareAttachColor -> true
            else -> false
          }
          val backgroundRadius = (itemRadius + 50f)
          val radius = itemRadius
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
  }
  
  fun drawPlayerInfos(players: MutableList<renderInfo>?, selfX: Float, selfY: Float) {
    players?.forEach {
      val (actor, x, y, _) = it
      actor!!
      val dir = Vector2(x - selfX, y - selfY)
      val distance = (dir.len() / 100).toInt()
      val angle = ((dir.angle() + 90) % 360).toInt()
      val (sx, sy) = mapToWindow(x, y)
      val playerStateGUID = actorWithPlayerState[actor.netGUID] ?: return@forEach
      val name = playerNames[playerStateGUID] ?: return@forEach
      val teamNumber = teamNumbers[playerStateGUID] ?: 0
      val numKills = playerNumKills[playerStateGUID] ?: 0
      val equippedWeapons = actorHasWeapons[actor.netGUID]
      var weapon: String? = ""
      if (equippedWeapons != null) {
        for (w in equippedWeapons) {
          val a = weapons[w] ?: continue
          val result = a.archetype.pathName.split("_")
          weapon += result[2].substring(4) + "\n"
        }
      }
      nameFont.draw(spriteBatch, "$angle°${distance}m\n$name[$teamNumber]->$numKills\n$weapon", sx + 20, windowHeight - sy + 20)
    }
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
    val (actor, x, y, dir) = it
    if (isTeamMate(actor)) return
    val actorID = actor!!.netGUID
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
    val (actor, x, y, dir) = actorInfo
    if (actor?.netGUID == selfID) return
    circle(x, y, backgroundRadius, 10)
    
    val attach = actor?.attachChildren?.values?.firstOrNull()
    
    color = when {
      isTeamMate(actor) -> teamColor
      attach == null -> pColor
      attach == selfID -> selfColor
      isTeamMate(actors[attach]) -> teamColor
      else -> pColor
    }
    
    circle(x, y, playerRadius, 10)
    
    if (drawSight) {
      color = sightColor
      arc(x, y, directionRadius, dir - fov / 2, fov, 10)
    }
    if (actor != null && actor.isACharacter) {//draw health
      val health = actorHealth[actor.netGUID] ?: 100f
      val width = healthBarWidth * zoom
      val height = healthBarHeight * zoom
      val y = y + backgroundRadius + height / 2
//      color = WHITE
//      rectLine(x - width / 2, y, x + width / 2, y, height+50f*zoom)
      val healthWidth = (health / 100.0 * width).toFloat()
      color = when {
        health > 80f -> GREEN
        health > 33f -> ORANGE
        else -> RED
      }
      rectLine(x - width / 2, y, x - width / 2 + healthWidth, y, height)
    }
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
  
  fun ShapeRenderer.drawVehicle(_color: Color, actorInfo: renderInfo,
                                width: Float, height: Float) {
    
    val (actor, x, y, dir) = actorInfo
    val v_x = actor!!.velocity.x
    val v_y = actor.velocity.y
    
    val dirVector = dirUnitVector.cpy().rotate(dir).scl(height / 2)
    color = BLACK
    val backVector = dirVector.cpy().nor().scl(height / 2 + 200f)
    rectLine(x - backVector.x, y - backVector.y,
             x + backVector.x, y + backVector.y, width + 400f)
    color = _color
    rectLine(x - dirVector.x, y - dirVector.y,
             x + dirVector.x, y + dirVector.y, width)
    color = playerColor
    if (actor.attachChildren.isNotEmpty() || v_x * v_x + v_y * v_y > 40) {
      actor.attachChildren.forEach { k, _ ->
        if (k == selfID) {
          color = selfColor
          return@forEach
        } else if (isTeamMate(actors[k])) {
          color = teamColor
          return@forEach
        }
      }
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