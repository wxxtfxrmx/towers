package com.wxxtfxrmx.towers.level.ui

import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.BodyDef
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.PolygonShape
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.utils.viewport.StretchViewport
import com.wxxtfxrmx.towers.common.*
import com.wxxtfxrmx.towers.level.box2d.FloorContactListener
import com.wxxtfxrmx.towers.level.component.BodyComponent
import com.wxxtfxrmx.towers.level.component.SortingLayerComponent
import com.wxxtfxrmx.towers.level.component.SpriteComponent
import com.wxxtfxrmx.towers.level.model.SortingLayer
import com.wxxtfxrmx.towers.level.model.TowersTexture
import com.wxxtfxrmx.towers.level.system.EmitFloorSystem
import com.wxxtfxrmx.towers.level.system.UpdateSpritePositionSystem
import com.wxxtfxrmx.towers.level.system.camera.UpdateCameraPositionSystem
import com.wxxtfxrmx.towers.level.system.camera.UpdateViewportSizeSystem
import com.wxxtfxrmx.towers.level.system.rendering.RenderingSystem

class LevelScreen(
        private val textureAtlas: TextureAtlas,
) : ScreenAdapter() {

    private val camera = OrthographicCamera(UiConstants.WIDTH_F, UiConstants.HEIGHT_F).apply {
        position.set(UiConstants.WIDTH_F * 0.5f, UiConstants.HEIGHT_F * 0.5f, 0f)
    }

    private val viewport = StretchViewport(UiConstants.WIDTH_F, UiConstants.HEIGHT_F, camera)

    private val b2dDebugRenderer = Box2DDebugRenderer(true, true, true, true, true, true)
    private val world = World(Vector2(0f, -9.8f), true)
    private val engine = PooledEngine()
    private val entityBuilder = EntityBuilder(engine)
    private val bodyBuilder = BodyBuilder(world)

    private val batch = SpriteBatch()
    private val renderingSystem = RenderingSystem(camera, batch)
    private val updateSpritePositionSystem = UpdateSpritePositionSystem()
    private val emitFloorSystem = EmitFloorSystem(entityBuilder, bodyBuilder, textureAtlas, viewport)
    private val updateViewportSizeSystem = UpdateViewportSizeSystem(engine, viewport)
    private val updateCameraPositionSystem = UpdateCameraPositionSystem(camera, viewport)

    init {
        engine.addEntity(foundationEntity())
        engine.addEntity(groundEntities())
        engine.addEntity(fenceEntities())
        engine.addEntity(keepOutSignEntity())
        engine.addEntity(alertEntity())

        engine.addSystem(updateViewportSizeSystem)
        engine.addSystem(updateCameraPositionSystem)
        engine.addSystem(updateSpritePositionSystem)
        engine.addSystem(renderingSystem)
        engine.addSystem(emitFloorSystem)

        world.setContactListener(FloorContactListener(engine))
    }

    private fun foundationEntity(): Entity {
        val foundationTexture = textureAtlas.region(TowersTexture.FOUNDATION)

        val body = bodyBuilder
                .begin()
                .define {
                    position.set(UiConstants.WIDTH_F * 0.5f, UiConstants.HEIGHT_F - foundationTexture.heightInMeters)
                    fixedRotation = true
                    type = BodyDef.BodyType.DynamicBody
                }
                .after()
                .fixture(PolygonShape::class, 1f) {
                    setAsBox(foundationTexture.halfWidthInMeters, foundationTexture.halfHeightInMeters)
                }
                .build()

        return entityBuilder
                .begin()
                .component(BodyComponent::class) {
                    this.body = body
                }
                .component(SortingLayerComponent::class) {
                    layer = SortingLayer.FRONT
                }
                .component(SpriteComponent::class) {
                    sprite = Sprite(foundationTexture).apply {
                        setSize(foundationTexture.widthInMeters, foundationTexture.heightInMeters)
                        setPosition(body.position.x - halfWidthInMeters, body.position.y - halfHeightInMeters)
                    }
                }
                .build()
    }

    private fun fenceEntities(): Entity {
        val fenceTexture = textureAtlas.region(TowersTexture.FENCE)
        val groundTexture = textureAtlas.region(TowersTexture.GROUND)

        val body = bodyBuilder
                .begin()
                .define {
                    position.set(fenceTexture.halfWidthInMeters, fenceTexture.halfHeightInMeters + groundTexture.heightInMeters)
                    fixedRotation = true
                    type = BodyDef.BodyType.StaticBody
                    active = false
                }
                .after()
                .fixture(PolygonShape::class, 0f) {
                    setAsBox(fenceTexture.halfWidthInMeters, fenceTexture.halfHeightInMeters)
                }
                .build()

        return entityBuilder
                .begin()
                .component(BodyComponent::class) {
                    this.body = body
                }
                .component(SortingLayerComponent::class) {
                    layer = SortingLayer.MIDDLE
                }
                .component(SpriteComponent::class) {
                    sprite = Sprite(fenceTexture).apply {
                        setSize(fenceTexture.widthInMeters, fenceTexture.heightInMeters)
                        setPosition(body.position.x - halfWidthInMeters, body.position.y - halfHeightInMeters)
                        setOrigin(body.position.x, body.position.y)
                    }
                }
                .build()
    }

    private fun groundEntities(): Entity {
        val groundTexture = textureAtlas.region(TowersTexture.GROUND)

        val body = bodyBuilder
                .begin()
                .define {
                    position.set(groundTexture.halfWidthInMeters, groundTexture.halfHeightInMeters)
                    fixedRotation = true
                    type = BodyDef.BodyType.StaticBody
                    active = true
                }
                .after()
                .fixture(PolygonShape::class, 1f) {
                    setAsBox(groundTexture.halfWidthInMeters, groundTexture.halfHeightInMeters)
                }
                .build()

        return entityBuilder
                .begin()
                .component(BodyComponent::class) {
                    this.body = body
                }
                .component(SpriteComponent::class) {
                    sprite = Sprite(groundTexture).apply {
                        setSize(groundTexture.widthInMeters, groundTexture.heightInMeters)
                        setPosition(body.position.x - halfWidthInMeters, body.position.y - halfHeightInMeters)
                    }
                }
                .component(SortingLayerComponent::class) {
                    layer = SortingLayer.MIDDLE
                }
                .build()
    }

    private fun keepOutSignEntity(): Entity {
        val keepOutSignTexture = textureAtlas.region(TowersTexture.KEEP_OUT_SIGN)
        val fenceTexture = textureAtlas.region(TowersTexture.FENCE)
        val groundTexture = textureAtlas.region(TowersTexture.GROUND)

        val body = bodyBuilder
                .begin()
                .define {
                    position.set(UiConstants.WIDTH_F - keepOutSignTexture.widthInMeters, groundTexture.heightInMeters + fenceTexture.halfHeightInMeters)
                    fixedRotation = true
                    type = BodyDef.BodyType.StaticBody
                    active = false
                }
                .after()
                .fixture(PolygonShape::class, 0f) {
                    setAsBox(keepOutSignTexture.halfWidthInMeters, keepOutSignTexture.halfHeightInMeters)
                }
                .build()

        return entityBuilder
                .begin()
                .component(BodyComponent::class) {
                    this.body = body
                }
                .component(SortingLayerComponent::class) {
                    layer = SortingLayer.MIDDLE
                }
                .component(SpriteComponent::class) {
                    sprite = Sprite(keepOutSignTexture).apply {
                        setSize(keepOutSignTexture.widthInMeters, keepOutSignTexture.heightInMeters)
                        setPosition(body.position.x - halfWidthInMeters, body.position.y - halfHeightInMeters)
                    }
                }
                .build()
    }

    private fun alertEntity(): Entity {
        val alertTexture = textureAtlas.region(TowersTexture.ALERT_SIGN)
        val fenceTexture = textureAtlas.region(TowersTexture.FENCE)
        val groundTexture = textureAtlas.region(TowersTexture.GROUND)

        val body = bodyBuilder
                .begin()
                .define {
                    position.set(alertTexture.widthInMeters, groundTexture.heightInMeters + fenceTexture.halfHeightInMeters)
                    fixedRotation = true
                    type = BodyDef.BodyType.StaticBody
                    active = false
                }
                .after()
                .fixture(PolygonShape::class, 0f) {
                    setAsBox(alertTexture.halfWidthInMeters, alertTexture.halfHeightInMeters)
                }
                .build()

        return entityBuilder
                .begin()
                .component(BodyComponent::class) {
                    this.body = body
                }
                .component(SortingLayerComponent::class) {
                    layer = SortingLayer.MIDDLE
                }
                .component(SpriteComponent::class) {
                    sprite = Sprite(alertTexture).apply {
                        setSize(alertTexture.widthInMeters, alertTexture.heightInMeters)
                        setPosition(body.position.x - halfWidthInMeters, body.position.y - halfHeightInMeters)
                    }
                }
                .build()
    }

    override fun render(delta: Float) {
        super.render(delta)
        val newDelta = if (delta > 0.1f) 0.1f else delta
        camera.update()

        batch.projectionMatrix = camera.combined
        engine.update(newDelta)

        world.step(newDelta, 8, 8)
        b2dDebugRenderer.render(world, camera.combined)
    }

    override fun pause() {
        super.pause()
        engine.systems.forEach { it.setProcessing(false) }
    }

    override fun resume() {
        super.resume()
        engine.systems.forEach { it.setProcessing(true) }
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height)
    }

    override fun dispose() {
        super.dispose()
        world.dispose()
        b2dDebugRenderer.dispose()
    }
}