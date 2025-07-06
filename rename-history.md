# Variable Renames History

This document lists variable or identifier renames found in the commit history.

## WorldController
 - method314 -> processTile
 - method305 -> applySceneLighting
 - method320 -> isTileVisible
 - method277 -> addCullingCluster
 - anInt446 -> visibleTileCount
 - anInt475 -> cullingClusterBufferCount
 - anInt488 -> mergeCycleId
 - anInt493 -> halfViewportWidth
 - anInt494 -> halfViewportHeight
 - anInt495 -> viewportMinX
 - anInt496 -> viewportMinY
 - anInt497 -> viewportMaxX
 - anInt498 -> viewportMaxY
 - method290 -> updateWallDecorationPosition

## MRUNodes -> MRUCache

## RSInterface
 - anInt255 -> enabledMediaType
 - method206 -> getModelForMedia
 - forID -> lookup
 - aMRUCache_415 -> modelCache
 - anInt413 -> ambient
 - anInt414 -> contrast

## EntityDef
 - method464 -> copyFromModel
 - method160 -> getModel
 - aByte816 -> config

## Flo -> FloorOverlay

## Game
 - method446 -> clearMovement
 - anInt952 -> menuHeight
 - anInt989 -> dragCounter
 - anInt1084 -> dragInterfaceId
 - anInt1085 -> draggedSlot
 - anInt1087 -> dragStartX
 - anInt1088 -> dragStartY
 - anInt988 -> idleCycleCounter
 - anInt1014 -> cameraX
 - anInt1015 -> cameraY
 - anInt1184 -> cameraPitch
 - anInt1186 -> cameraYawAccel
 - anInt1187 -> cameraPitchAccel
 - minimapInt1 -> cameraYaw
 - anInt1278 -> cameraXOffset
 - anInt1279 -> cameraXOffsetSpeed
 - anInt1131 -> cameraYOffset
 - anInt1132 -> cameraYOffsetSpeed
 - anInt896 -> cameraYawOffset
 - anInt897 -> cameraYawOffsetSpeed
 - anInt984 -> cameraZoom
 - anInt1005 -> cameraMoveCycle
 - anInt1089 -> chatScrollPosition
 - anInt1211 -> chatScrollHeight
 - anInt1264 -> alternatePathFound
 - anInt1283 -> selectedItemSlot
 - anInt1284 -> selectedItemInterfaceId
 - anInt1285 -> selectedItemId
 - method42 -> getTileHeight
 - updateCameraMovement -> updateCameraPosition
 - method77 -> adjustColorBrightness
 - method119 -> updateInterfaceAnimations
 - method117 -> updateSelfMovement
 - method134 -> updateOtherPlayers
 - method137 -> handleMapPackets
 - method139 -> updateNpcList
 - method142 -> updateSceneObjects
 - method146 -> renderGameView
 - method130 -> queuePendingSpawn
 - method120 -> determineCameraPlane
 - method121 -> getCurrentPlane
 - method173 -> loadObjectModels
 - method178 -> isObjectVisible
 - anInt478 -> currentMidiVolume
 - anInt155 -> fadeVolume
 - anInt2200 -> fadeStep
 - aBoolean475 -> midiLooping
 - anInt116 -> nextSongDelay
 - aBoolean995 -> autoPlaySong
 - anInt139 -> queuedSongId
 - method482 -> transformVertices
 - anInt720 -> midiFadeCycles
 - anInt1478 -> queuedMidiVolume
 - method115 -> processPendingSpawns
 - processPendingSpawns -> locatePendingSpawns
 - method54 -> checkMapLoadStatus
 - method66 -> walkToObject
 - method83 -> blendColors
 - method114 -> animatePlayers
 - method95 -> animateNpcs
 - method73 -> processInput
 - method70 -> updateRestrictedArea
 - method89 -> locateSceneObject
 - method90 -> processSoundQueue
 - method91 -> addLocalPlayers
 - method97 -> updateForcedMovement
 - method98 -> updateInterpolatedMovement
 - method99 -> updateWalkingStep
 - method104 -> processGraphicsObjects
 - anInt1251 -> restrictedArea
 - method81 -> drawMinimapHint
 - method60 -> resetInterfaceAnimation
 - method45 -> resetCharacterOptions
 - method55 -> processProjectiles
 - method26 -> addNpcsToScene
 - method24 -> generateMinimap
 - method50 -> drawMinimapLoc
 - method65 -> handleScrollbarInput
 - method86 -> processNpcUpdateMasks
 - method37 -> animateTextures
 - method33 -> applyVarp
 - method38 -> updateEntityText
 - method63 -> processPendingSpawns
 - method22 -> constructMapRegion
 - method46 -> addLocalNPCs
 - method47 -> addPlayersToScene
 - method49 -> processMusicQueue
 - method49 -> processPlayerUpdateMasks
 - anInt311 -> cacheIndex
 - method100 -> updateEntityFacing
 - method101 -> updateEntityAnimation
 - method107 -> decodePlayerUpdateMask
 - method684 -> initiateMidiFade
 - method368 -> updateMidiFade
 - method1004 -> calculateLogVolume
 - method104(int) -> formatDate
 - method96 -> updateEntityMovement
 - method900 -> setMidiVolume
 - method55 -> stopMusic
 - method891 -> stopMidiPlayback
 - method58 -> queueSong
 - method56 -> playSong
 - method853 -> playMidiTrack
 - method899 -> queueMidiTrack

## RSApplet
 - TextDrawingArea -> clean
 - aLong215 -> currentTime

## OnDemandFetcher
 - method548 -> requestModel
 - method225 -> decompress
 - method234 -> writeEntry

## Runnable_Impl1 -> MidiHandler

## InputStream_Sub1 -> MuLawInputStream
 - anInt404 -> id
 - anInt812 -> x
 - anInt813 -> y

## Object5 -> SceneObject

## Animable_Sub5 -> DynamicObject
 - method258 -> getFrameDelay
 - anInt811 -> tileHeight

## Object2 -> WallDecoration
 - aClass30_Sub2_Sub4_814 -> renderable

## Object1 -> BoundaryObject

## Animable_Sub3 -> GraphicsObject
 - anInt412 -> rotation

## Object4 -> ItemPile

## Class56_Sub1_Sub2 -> QueuedMidiPlayer
 - anIntArray216 -> trackTicks
 - method520 -> readDeltaTime
 - method521 -> isTrackFinished
 - method522 -> saveTrackPosition
 - method523 -> clear
 - method525 -> load
 - method526 -> seekTrack
 - method527 -> isLoaded
 - method528 -> markTrackEnd
 - method529 -> getNextEvent
 - method531 -> allTracksFinished
 - method532 -> getTimeForTick
 - method533 -> getTrackCount
 - method534 -> resetTracks
 - method536 -> getNextTrack
 - method827 -> playMidi
 - method828 -> shutdown
 - method830 -> adjustVolume
 - method831 -> setVolume
 - method832 -> poll
 - method833 -> stopMidi

## Class56_Sub1 -> AbstractMidiController

## Class40 -> ShapedTile

## Class56_Sub1_Sub1 -> SystemMidiPlayer
 - method790 -> closeMidiSystem

## Class47 -> CullingCluster

## Class56 -> MidiPlayer

## Object3 -> TileDecoration

## Class13 -> BZip2Decompressor

## Kotlin -> Java

## Misc
 - deleteItem2 -> deleteItem
 - randomToggle -> randomEventsEnabled
 - method108 -> updateCameraMovement
 - processSoundQueue -> processSoundEffects
 - method90 -> processSoundEffects
 - method95 -> updateNpcMovement
 - method89 -> updatePendingSpawn
 - method97 -> advanceForceMovement
 - method139 -> decodeNpcMovement
 - method117 -> decodeSelfMovement
 - method134 -> decodePlayerMovement
 - method81 -> drawMinimapSprite
 - method70 -> updateMultiCombatArea

## ObjectManager
 - anIntArrayArrayArray135 -> renderFlags
 - anIntArrayArray139 -> tileShading
 - method172 -> getTerrainNoise
 - method182 -> getCorrectPlane
 - anInt406 -> animationId

## Censor
 - method509 -> applyWordFilter

## NodeCache -> NodeHashTable

## NPC
 - method450 -> getBaseModel
 - aAnimation_407 -> animation
 - anInt410 -> scaleX
 - anInt411 -> scaleY

## Animable_Sub4 -> Projectile

## Class30_Sub1 -> PendingSpawn

## Class4 -> TileRotation

## Class36 -> AnimFrame
 - anInt1681 -> transformX
 - anInt1682 -> transformY
 - anInt1683 -> transformZ

## Class18 -> FrameBase

## Class21 -> PlainTile

## Class43 -> ModelHeader

## Class6 -> SoundFilter

## Class39 -> Instrument

## DummyClass -> CachePlaceholder

## Class11 -> MidiFile
 - aByteArray210 -> opcodeSizeTable
 - anIntArray212 -> trackStatus
 - anIntArray214 -> trackPositions
 - anIntArray217 -> trackOffsets
 - anInt218 -> microsecondsPerQuarterNote
 - method524 -> readEvent
 - method535 -> parseEvent

## IDK
 - anInt405 -> modelId

## ItemDef
 - anIntArray408 -> originalModelColors
 - anIntArray409 -> modifiedModelColors

## Model
- nullLoader -> clearCache
- method459 -> init
- method460 -> loadModelData
- method461 -> unload
- method462 -> create
- method463 -> isLoaded
- aModelHeaderArray1661 -> modelHeaders
- aOnDemandFetcherParent_1662 -> modelFetcher
- aBooleanArray1663 -> visibilityMap1
- aBooleanArray1664 -> visibilityMap2
- anIntArray1665 -> projectedVertexX
- anIntArray1666 -> projectedVertexY
- anIntArray1667 -> projectedX
- anIntArray1668 -> projectedY
- anIntArray1669 -> projectedZ
- anIntArray1670 -> depthList
- anIntArray1671 -> vertexQueue
- anIntArrayArray1672 -> vertexGroups2D
- anIntArray1673 -> cameraSine
- anIntArrayArray1674 -> faceLists
- anIntArray1675 -> stackX
- anIntArray1676 -> stackY
- anIntArray1677 -> facePriority
- anIntArray1627 -> vertexX
- anIntArray1628 -> vertexY
- anIntArray1629 -> vertexZ
- anIntArray1631 -> faceA
- anIntArray1632 -> faceB
- anIntArray1633 -> faceC
- anIntArray1655 -> vertexSkins
- anIntArray1656 -> faceSkins
- anIntArray1640 -> faceColor
- anInt1650 -> boundingRadius
- anInt1651 -> maxY
- anInt1652 -> diagonal3D
- anInt1653 -> diagonal2D
- anInt1646 -> minX
- anInt1647 -> maxX
- anInt1648 -> maxZ
- anInt1649 -> minZ
- anIntArrayArray1657 -> vertexGroups
- anIntArrayArray1658 -> faceGroups
- method465 -> getOrCreateVertex
- method466 -> calculateBounds
- method467 -> calculateBoundsY
- method468 -> calculateExtremes
- method469 -> buildVertexGroups
- method470 -> applyFrame
- method471 -> applyFrames
- method472 -> transformVertices
- method473 -> calculateNormals
- method475 -> translate
- method478 -> scaleModel
- method479 -> applyLighting
 - method481 -> calculateShadedColor
 - method483 -> processVisibility
 - method484 -> drawFace
 - method485 -> drawClippedFace
 - method486 -> isTriangleVisible

## Sounds
 - aClass3_Sub12_211 -> stream

## Decompressor
 - method235 -> writeBlock
