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
- aClass28Array462 -> sceneObjectBuffer
- aClass19_477 -> tileQueue
- aBoolean434 -> boundaryToggle
- aBooleanArrayArrayArrayArray491 -> visibilityMap
- aBooleanArrayArray492 -> tileVisibilityMap
- anIntArray463 -> xOffset1
- anIntArray464 -> yOffset1
- anIntArray465 -> xOffset2
- anIntArray466 -> yOffset2
- anIntArray478 -> orientationLookup
- anIntArray479 -> orientationMasks
- anIntArray480 -> orientationAdjacency
- anIntArray481 -> cullMask1
- anIntArray482 -> cullMask2
- anIntArray483 -> cullMask3
- anIntArray484 -> cullMask4
- anIntArray485 -> textureLookup
- anIntArrayArray489 -> blendMap1
- anIntArrayArray490 -> blendMap2
- aBoolean1322 -> tileActive
- aBoolean1323 -> inQueue
- aBoolean1324 -> needsProcessing
- anInt1325 -> cullFlags
- anInt1326 -> cullOrientation
- anInt1327 -> cullOpposite
- anInt1328 -> boundaryFlags
- aclass30_sub3 -> planeTiles
- aclass30_sub3_1 -> planeTiles1
- aclass30_sub3_2 -> planeTiles2
- class30_sub2_sub4 -> renderable
- class30_sub2_sub4_1 -> secondaryRenderable
- class30_sub2_sub4_2 -> topRenderable
- class30_sub3 -> groundTile
- class30_sub3_1 -> currentTile
- class30_sub3_2 -> diagonalTile
- class30_sub3_3 -> westTile
- class30_sub3_4 -> eastTile
- class30_sub3_5 -> southTile
- class30_sub3_6 -> northTile
- class30_sub3_7 -> linkedTile
- class30_sub3_8 -> westNeighbor
- class30_sub3_9 -> eastNeighbor
- class30_sub3_10 -> southNeighbor
- class30_sub3_11 -> northNeighbor
- class30_sub3_12 -> upperTile
- class30_sub3_13 -> queueEastTile
- class30_sub3_14 -> queueNorthTile
- class30_sub3_15 -> queueWestTile
- class30_sub3_16 -> queueSouthTile
- class30_sub3_17 -> neighborTileEast
- class30_sub3_18 -> neighborTileNorth
- class30_sub3_19 -> neighborTileWest
- class30_sub3_20 -> neighborTileSouth
- class30_sub3_21 -> cullCheckTile
- class30_sub3_22 -> queuedNeighborTile
- class10_1 -> boundaryObjPrimary
- class10_2 -> boundaryObjSecondary
- class10_3 -> boundaryObj
- class10 -> boundaryObject
- class49 -> tileDecoration
- aclass47 -> planeClusters
- class47 -> cluster
- class33 -> normal
- class33_1 -> tempNormal
- class33_2 -> otherNormal
- class33_3 -> otherTempNormal
- class43 -> tile
- class43_1 -> alternateTile
- class28_1 -> objTile
- class28_2 -> queuedObj
- class28_3 -> sceneObj

## Ground
- aBoolean1322 -> tileActive
- aBoolean1323 -> inQueue
- aBoolean1324 -> needsProcessing
- anInt1325 -> cullFlags
- anInt1326 -> cullOrientation
- anInt1327 -> cullOpposite
- anInt1328 -> boundaryFlags

## MRUNodes -> MRUCache

## RSInterface
 - anInt255 -> enabledMediaType
 - method206 -> getModelForMedia
 - forID -> lookup
 - aMRUCache_415 -> modelCache
 - anInt413 -> ambient
- anInt414 -> contrast
- anInt208 -> animationCycle
- anInt214 -> contentType
- anInt216 -> hoverTextColor
- anInt219 -> activeTextColor
- anInt230 -> hoverTarget
- anInt239 -> activeHoverTextColor
- anInt246 -> animationFrame
- anInt265 -> offsetY
- anIntArray212 -> requiredValues
- anIntArray245 -> valueCompareType
- aBoolean223 -> centerText
- aBoolean227 -> filled
- aBoolean235 -> insertItems
- aBoolean259 -> allowItemDragging
- aBoolean266 -> hideUntilHovered
- aBoolean268 -> textShadow
- aByte254 -> opacity

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

 - anInt1104 -> systemUpdateTimer
 - anInt1071 -> minimapIconCount
 - anIntArray1072 -> minimapIconX
 - anIntArray1073 -> minimapIconY
- aClass30_Sub2_Sub1_Sub1Array1140 -> minimapIconSprites
- aBoolean1233 -> chatSettingsUpdateNeeded
- aClass30_Sub2_Sub1_Sub1_1263 -> minimapImage
- anInt1288 -> walkPacketCounter
- anInt1036 -> prevBaseX
- anInt1037 -> prevBaseY
- anInt1039 -> lastInteractionId
- anInt1040 -> flameMainColor
- anInt1041 -> flameSecondaryColor
- anInt1046 -> friendsListStatus
- anInt1048 -> hoveredTabId
- aString1049 -> errorMessage
- anInt1051 -> terrainLoadCycle
- anIntArray1052 -> minimapLineOffset
- anInt1054 -> flashingTabId
- anInt1055 -> multiCombatZone
- anInt1061 -> drawCycle
- anInt1097 -> mapLoadPacketCounter
- anInt1098 -> cameraTargetX
- anInt1099 -> cameraTargetY
- anInt1100 -> cameraTargetZ
- anInt1101 -> cameraMoveSpeed
- anInt1102 -> cameraMoveAcceleration
- anInt1117 -> antiCheatPacketCounter
- aString1121 -> inputPrompt
- anInt1134 -> npcAttackCounter
- anInt1142 -> clickPacketCounter
- aBoolean1149 -> actionPending
- anInt1155 -> npcInteractionCounter
- aBoolean1159 -> isDynamicRegion
- aBoolean1160 -> isCameraLocked
- anInt1171 -> minimapVerticalSpeed
- anInt1175 -> itemUseCounter
- anInt1208 -> flameDrawingCounter
- anInt1210 -> minimapHorizontalSpeed
- anInt839 -> entityRemovalCount
- anIntArray840 -> removedEntityIndices
- anInt841 -> lastPacketType
- aString844 -> messagePrompt
- aBoolean848 -> soundEffectEnabled
- anInt849 -> systemUpdateCounter
- anIntArray850 -> flameBuffer
- anIntArray851 -> flamePaletteRed
- anIntArray852 -> flamePaletteGreen
- anIntArray853 -> flamePaletteBlue
- anInt854 -> unusedCounter
- anInt855 -> hintIconState
- anInt874 -> selectedTargetId
- aBooleanArray876 -> tabFlashing
- anInt886 -> hoveredWidgetId
- anInt893 -> playerUpdateCount
- anIntArray894 -> playerUpdateIndices
- anInt900 -> interfaceMode
- anInt913 -> configActionId
- anInt933 -> selectedPlayerId
- anInt934 -> selectedNpcId
- anInt935 -> destinationX
- anInt936 -> destinationY
- anInt937 -> lastMouseX
- anInt938 -> lastMouseY
- anInt940 -> abuseReportCounter
- anInt945 -> animationCycle
- anInt985 -> lastPlane
- anInt995 -> cameraFocusX
- anInt996 -> cameraFocusY
- anInt997 -> cameraFocusHeight
- anInt998 -> cameraAdjustSpeed
- anInt999 -> cameraAdjustAcceleration
 - anInt1002 -> scrollBarColor
 - anInt1063 -> scrollBarHandleColor
 - anInt1009 -> connectionTimeoutCounter
 - anInt1010 -> keepAliveCounter
 - anInt1011 -> reconnectDelay
 - anInt1016 -> cameraUpdateDelay
 - anInt1018 -> overlayInterfaceId
 - anInt1021 -> minimapState
 - anInt1022 -> mouseIdleTicks
 - anInt1026 -> lastHoveredWidgetId
 - anInt1034 -> recoveryQuestionChangeDate
 - anInt1069 -> currentRegionX
 - anInt1070 -> currentRegionY
 - anInt1079 -> loadingPercent
 - anInt1083 -> lastPasswordChange
 - anInt1137 -> selectedSpellId
 - anInt1170 -> currentDateOffset
 - anInt1188 -> actionCounter
 - anInt1193 -> lastLoginIp
 - anInt1213 -> clickCycle
 - anInt1215 -> unusedInt1215
 - anInt1222 -> hintNpcIndex
 - anInt1226 -> npcClickCounter
 - anInt1237 -> lastMousePacketX
 - anInt1238 -> lastMousePacketY
 - anInt1249 -> chatEffectsState
 - anInt1253 -> oneMouseButtonMode
 - anInt1254 -> minimapRandomTimer
 - anInt1257 -> soundBufferOffset
 - anInt1265 -> waveCycle
 - anInt1268 -> mapEventX
 - anInt1269 -> mapEventY
 - anInt1275 -> flameOffset
 - anInt1289 -> unknownInt1289
- anInt1290 -> unknownInt1290
 - anIntArrayArray825 -> pathDistances
 - anIntArray828 -> flameBuffer1
 - anIntArray829 -> flameBuffer2
 - anIntArray873 -> cameraShakeAmplitude
 - anIntArrayArray901 -> pathDirections
 - anInt902 -> scrollBarLightColor
 - anInt924 -> objectClickCounter
 - anInt927 -> scrollBarDarkColor
 - anIntArray928 -> cameraShakeSpeed
 - anIntArrayArray929 -> occupiedTiles
 - anIntArray965 -> hitmarkColors
 - anIntArray968 -> mapBackLeft
 - anIntArray969 -> flameLineOffsets
 - anInt975 -> maxDisplayedText
 - anIntArray976 -> textX
 - anIntArray977 -> textY
 - anIntArray978 -> textHeight
 - anIntArray979 -> textWidth
 - anIntArray980 -> textColors
 - anIntArray981 -> textEffects
 - anIntArray982 -> textCycles
 - anInt986 -> playerOptionCounter
 - anIntArray990 -> characterColorIndices
 - anIntArray1019 -> levelExperience
 - anIntArray1030 -> cameraShakeCycle
 - anIntArray1045 -> varpArray
 - anIntArray1057 -> mapBackWidths
 - anIntArray1065 -> characterStyle
 - anIntArrayArrayArray1129 -> dynamicRegionData
 - anIntArray1177 -> objectData
 - anIntArray1190 -> flameGradient1
 - anIntArray1191 -> flameGradient2
 - anIntArray1203 -> cameraShakeFrequency
 - anIntArray1229 -> minimapLineLengths
 - anIntArray1232 -> bitMasks
 - anIntArray1234 -> regionBaseIds
 - anIntArray1235 -> terrainArchiveIds
 - anIntArray1236 -> objectArchiveIds
 - anInt1401 -> midiVolume
 - anInt992 -> scrollPadding
 - anInt1273 -> unusedField1273

## RSApplet
- TextDrawingArea -> clean
- aLong215 -> currentTime
- anInt34 -> idleTicks

## OnDemandFetcher
 - method548 -> requestModel
 - method225 -> decompress
 - method234 -> writeEntry
 - method558 -> queueRequest
 - method560 -> requestFileNow
 - method563 -> validateOrQueue
 - method566 -> clearPriorityQueue
 - method554 -> requestMapFiles
 - method568 -> processExtraFiles
 - method569 -> isMidiRequired
 - anInt1332 -> currentPriority
 - mapIndices1 -> regionIds
 - mapIndices2 -> mapArchiveIds
 - mapIndices3 -> landArchiveIds
- mapIndices4 -> mapMembershipFlags
- anIntArray1348 -> midiFileFlags
- anIntArray1360 -> animationFileIds
- aClass19_1358 -> completedRequestQueue
- aClass19_1368 -> incompleteRequestQueue
- aClass19_1370 -> pendingRequestQueue

## Runnable_Impl1 -> MidiHandler

## InputStream_Sub1 -> MuLawInputStream
 - anInt404 -> id
 - anInt812 -> x
 - anInt813 -> y

## Stream
 - anIntArray1409 -> BIT_MASKS
 - anInt1412 -> poolSize
 - nodeList -> pool
 - method426 -> readUnsignedByteAdd
 - method400 -> writeShortLE
 - method403 -> writeIntLE
 - method421 -> readSignedSmart
 - method422 -> readUnsignedSmart
 - doKeys -> rsaEncrypt
 - method424 -> writeByteNeg
 - method425 -> writeByteSub
 - method427 -> readUnsignedByteNeg
 - method428 -> readUnsignedByteSub
 - method429 -> readByteNeg
 - method430 -> readByteSub
 - method431 -> writeShortLEDup
 - method432 -> writeShortA
 - method433 -> writeShortLEA
 - method434 -> readShortLE
 - method435 -> readShortAdd
 - method436 -> readShortLEAdd
 - method437 -> readShortLESigned
 - method438 -> readShortLEAddSigned
 - method439 -> readIntV1
 - method440 -> readIntV2
 - method441 -> writeBytesReverseAdd
 - method442 -> readBytesReverse

## TextInput
 - method525 -> decodeChatMessage
 - method526 -> encodeChatMessage
 - aCharArray631 -> charBuffer

## Object5 -> SceneObject

## Animable_Sub5 -> DynamicObject
- method258 -> getFrameDelay
- anInt811 -> tileHeight
- class46 -> objectDef

## Object2 -> WallDecoration
 - aClass30_Sub2_Sub4_814 -> renderable

## Object1 -> BoundaryObject

 - anInt273 -> plane
 - anInt274 -> x
 - anInt275 -> y
 - orientation1 -> orientation2
 - aClass30_Sub2_Sub4_278 -> primary
 - aClass30_Sub2_Sub4_279 -> secondary
 - aByte281 -> config

## Animable_Sub3 -> GraphicsObject
 - anInt412 -> rotation
 - anInt1560 -> plane
 - anInt1561 -> x
 - anInt1562 -> y
 - anInt1563 -> height
 - anInt1564 -> endCycle
 - aBoolean1567 -> finished
 - aSpotAnim_1568 -> spotAnimation
 - anInt1569 -> frame
 - anInt1570 -> frameCycle
 - method454 -> update

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

## Sprite
 - method343 -> initializeDrawingArea
 - method344 -> adjustRgb
 - method345 -> crop
 - method346 -> drawSprite
 - method347 -> copyToCanvas
 - method349 -> drawTransparent
 - method351 -> blendPixels
 - method352 -> drawTransformed
 - method353 -> drawRotated
 - method354 -> drawWithMask
 - method355 -> copyMasked
 - anInt1442 -> offsetX
 - anInt1443 -> offsetY

## RSImageProducer
 - anIntArray315 -> pixels
 - anInt316 -> width
 - anInt317 -> height
 - aColorModel318 -> colorModel
 - anImageConsumer319 -> imageConsumer
 - anImage320 -> image
 - method239 -> updateImage

## Class40 -> ShapedTile

## Class56_Sub1_Sub1 -> SystemMidiPlayer
- method790 -> closeMidiSystem
- aReceiver1850 -> midiReceiver
- aSequencer1851 -> midiSequencer

## Class47 -> CullingCluster

## Class56 -> MidiPlayer

## Object3 -> TileDecoration

## Class13 -> BZip2Decompressor
 - class32 -> state

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
- aByteArrayArrayArray149 -> tileFlags
- aByteArrayArrayArray142 -> tileUnderlayIds
- aByteArrayArrayArray130 -> tileOverlayIds
- aByteArrayArrayArray136 -> tileOverlayShapes
- aByteArrayArrayArray148 -> tileOverlayOrientations
- aByteArrayArrayArray134 -> tileShadowing
- anIntArray140 -> boundaryRotationMasks
- aclass11 -> collisionMaps
- class11 -> collisionMap
- class42_sub1 -> onDemandFetcher
 - class46 -> objectDef
- obj -> tileDecoration
- obj1 -> dynamicObject
- obj2 -> genericObject
- obj3 -> straightBoundary
- obj4 -> cornerBoundary
- obj5 -> diagonalBoundary
- obj6 -> interactiveObject
- obj7 -> wallDecoration
- obj8 -> wallDecoration2
- obj9 -> wallDecoration3
- obj10 -> wallDecoration4
- obj11 -> boundaryPrimary
- obj12 -> boundarySecondary
- obj13 -> wallDecorationOffset

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
 - class29 -> envelope

## Class39 -> Instrument

## Class29 -> SoundEnvelope

## Class33 -> VertexNormal

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
- aModel_1621 -> placeholderModel
- anIntArray1622 -> tempVertexX
- anIntArray1623 -> tempVertexY
- anIntArray1624 -> tempVertexZ
- anIntArray1625 -> tempFaceTextures
- anInt1626 -> vertexCount
- anInt1630 -> faceCount
- anIntArray1634 -> shadeA
- anIntArray1635 -> shadeB
- anIntArray1636 -> shadeC
- anIntArray1637 -> faceRenderTypes
- anIntArray1638 -> facePriorities
- anIntArray1639 -> faceAlphas
- anInt1641 -> defaultPriority
- anInt1642 -> texturedTriangleCount
- anIntArray1643 -> texTriangleX
- anIntArray1644 -> texTriangleY
- anIntArray1645 -> texTriangleZ
- anInt1654 -> overrideHeight
- aBoolean1659 -> pickable
- aVertexNormalArray1660 -> vertexNormalTemp
- modelHeaders -> modelHeaderCache
- modelFetcher -> modelFetcherParent
- anInt1685 -> viewportCenterX
- anInt1686 -> viewportCenterY
- anInt1687 -> queueLength
- anIntArray1688 -> faceQueue
- modelIntArray1 -> sineTable
- modelIntArray2 -> cosineTable
- modelIntArray3 -> brightnessTable
- modelIntArray4 -> reciprocalTable

## Sounds
 - aClass3_Sub12_211 -> stream

## Decompressor
 - method235 -> writeBlock

## CollisionMap
 - anInt290 -> xInset
 - anInt291 -> yInset
 - anInt292 -> width
 - anInt293 -> height
 - anIntArrayArray294 -> clippingFlags
 - method210 -> reset
 - method211 -> addWall
 - method212 -> addObject
 - method213 -> blockTile
 - method214 -> addFlag
 - method215 -> removeWall
 - method216 -> removeObject
 - method217 -> removeFlag
 - method218 -> unblockTile
 - method219 -> canReachWall

## Censor
 - aCharArrayArray624 -> topLevelDomains
 - anIntArray625 -> tldBehavior
 - aCharArrayArray621 -> badWords
 - aByteArrayArrayArray622 -> badWordPatterns
 - aCharArrayArray623 -> domainWords
 - anIntArray620 -> bannedNameHashes
 - method493 -> loadBannedWords
 - method494 -> readCharArrayTable
 - method495 -> sanitizeInput
 - method496 -> isAllowedCharacter
 - method505 -> censorTopLevelDomains
 - method500 -> censorWords
 - method501 -> censorDomains
 - method502 -> censorDomain
 - method503 -> checkPrecedingContext
 - method504 -> checkFollowingContext
 - method506 -> censorTldHelper
 - method507 -> checkPrecedingPunctuation
 - method508 -> checkFollowingPunctuation
 - method498 -> restoreCapitalization
- method499 -> fixSentenceCase
- method511 -> matchDomainCharacter
- method512 -> matchLeetCharacter
- method513 -> charToByte
- method514 -> censorLongNumbers
- method515 -> findNextDigit
- method516 -> findNonDigit
- method517 -> isNonAlphanumeric
- method518 -> isFillerCharacter
- method510 -> lookupCharPair
- method523 -> isBannedName
- method524 -> computeNameHash

## Background
 - anInt1456 -> maxWidth
 - anInt1457 -> maxHeight
 - anIntArray1451 -> palette
 - anInt1454 -> offsetX
 - anInt1455 -> offsetY
 - anInt1452 -> width
 - anInt1453 -> height
 - aByteArray1450 -> pixels
 - method356 -> downscaleHalf
 - method357 -> normalize
 - method358 -> flipHorizontal
 - method359 -> flipVertical
 - method360 -> adjustPalette
 - method361 -> draw
 - method362 -> blit

## Texture
 - nullLoader -> reset
 - method364 -> init
 - method365 -> resize
 - method366 -> clearCache
 - method367 -> initCache
 - method368 -> loadTextures
 - method369 -> getAverageTextureColor
 - method370 -> unloadTexture
 - method371 -> getTexturePixels
 - method372 -> setBrightness
- method373 -> adjustBrightness
- anInt1459 -> UNUSED_TEXTURE_CONSTANT
 - method374 -> drawGouraudTriangle
 - method375 -> drawGouraudScanline
 - method378 -> drawTexturedTriangle
 - method379 -> drawTexturedScanline
 - anIntArray1468 -> reciprocalTable
 - anIntArray1469 -> reciprocal16
 - anIntArray1470 -> sineTable
 - anIntArray1471 -> cosineTable
 - anInt1473 -> textureCount
 - aBackgroundArray1474s -> textures
 - aBooleanArray1475 -> textureHasTransparency
 - anIntArray1476 -> averageTextureColor
 - anInt1477 -> cachePointer
 - anIntArrayArray1478 -> texturePool
 - anIntArrayArray1479 -> textureImages
 - anIntArray1480 -> textureLastUsed
 - anInt1481 -> cycle
 - anIntArray1482 -> brightnessTable
 - anIntArrayArray1483 -> texturePalettes
 - aBoolean1462 -> clip
 - aBoolean1463 -> textureIsOpaque
 - aBoolean1464 -> highQuality
 - anInt1465 -> alpha

## Model
 - method476 -> recolor

## ObjectDef
 - anIntArray773 -> modelIds
 - anIntArray776 -> modelTypes
 - anInt744 -> sizeX
 - anInt761 -> sizeY
 - aBoolean767 -> isSolid
 - aBoolean757 -> impenetrable
 - hasActions -> interactive
 - anInt781 -> animationId
 - anInt748 -> scaleX
 - anInt772 -> scaleY
 - anInt740 -> scaleZ
 - method574 -> requestModels
 - method577 -> isModelReady
 - method578 -> getModel
 - method579 -> areModelsReady
 - method580 -> getChildDefinition
- method581 -> buildModel
- readValues -> decode
- anInt758 -> mapSceneId
- anInt738 -> offsetX
- anInt745 -> offsetY
- anInt783 -> offsetZ
- anInt760 -> supportsItems
- anInt774 -> varbitId
- anInt749 -> varpId
- aBoolean736 -> occludes
- aByte737 -> ambient
- aByte742 -> contrast
- anInt746 -> mapIconId
- aBoolean751 -> mirrorOnRotate
- aBoolean762 -> contouredGround
- aBoolean764 -> adjustToTerrain
- aBoolean766 -> hollow
- anInt768 -> defaultOrientation
- aBoolean769 -> delayedShading
- anInt775 -> wallDecoOffset
- aBoolean779 -> clipped

## VarBit
 - anInt648 -> configId
 - anInt649 -> leastSignificantBit
 - anInt650 -> mostSignificantBit
 - aBoolean651 -> isActive

## OnDemandData
 - dataType -> type
 - buffer -> data
 - ID -> id
 - loopCycle -> cycleCount

## TextClass
 - method585 -> hashSpriteName
 - method586 -> intToIpString

## TextDrawingArea
 - aByteArrayArray1491 -> glyphPixels
 - anIntArray1492 -> glyphWidths
 - anIntArray1493 -> glyphHeights
 - anIntArray1494 -> xOffsets
 - anIntArray1495 -> yOffsets
 - anIntArray1496 -> glyphAdvances
 - aRandom1498 -> random
 - aBoolean1499 -> strikethrough
 - anInt1497 -> fontHeight
 - method384 -> measurePlainTextWidth
 - method386 -> drawWavyCenteredText
 - method387 -> drawWavyText
 - method388 -> drawShakeText
 - method390 -> drawRandomColorText

## Player
 - anIntArray1700 -> bodyColors
 - anInt1702 -> gender
 - method452 -> getBaseModel
 - method453 -> getDialogueModel
 - aLong1697 -> cachedModelHash
 - aLong1718 -> appearanceHash
 - anInt1707 -> animationStartCycle
 - anInt1708 -> animationEndCycle
 - anInt1709 -> animationBaseY
 - anInt1711 -> animationBaseX
- anInt1712 -> animationBaseHeight
- anInt1713 -> animationBaseZ
- aModel_1714 -> overlayModel
- aBoolean1699 -> skipAnimations
- anInt1719 -> boundingBoxMinX
- anInt1720 -> boundingBoxMinY
- anInt1721 -> boundingBoxMaxX
- anInt1722 -> boundingBoxMaxY

## Entity
 - anInt1504 -> turnSpeed
 - anInt1520 -> spotAnimId
 - anInt1521 -> spotAnimFrame
 - anInt1522 -> spotAnimFrameCycle
 - anInt1523 -> spotAnimStartTick
 - anInt1524 -> spotAnimHeight
- anInt1552 -> currentHeading
- method446 -> clearMovement
- anInt1503 -> movementDelay
- anInt1505 -> runAnimation
- anInt1511 -> standAnimation
- anInt1512 -> turnAnimation
- anInt1513 -> chatColor
- anInt1517 -> currentAnimation
- anInt1518 -> animationFrame
- anInt1519 -> animationFrameCycle
- anInt1527 -> graphicFrame
- anInt1528 -> graphicFrameCycle
- anInt1529 -> graphicDelay
- anInt1530 -> graphicCycle
- anInt1531 -> chatEffect
- anInt1537 -> lastUpdateCycle
- anInt1538 -> focusX
- anInt1539 -> focusY
- anInt1540 -> size
- anInt1542 -> animationDelay
- anInt1543 -> forceMoveStartX
- anInt1544 -> forceMoveEndX
- anInt1545 -> forceMoveStartY
- anInt1546 -> forceMoveEndY
- anInt1547 -> forceMoveStartCycle
- anInt1548 -> forceMoveEndCycle
- anInt1549 -> forceMoveDirection
- anInt1554 -> walkAnimation
- anInt1555 -> turn180Animation
- anInt1556 -> turn90CWAnimation
- anInt1557 -> turn90CCWAnimation

## ShapedTile
 - aBoolean683 -> flatShading
 - anInt684 -> shape
 - anInt685 -> rotation
 - anInt686 -> baseColor
 - anInt687 -> shadeColor
 - anIntArray673 -> vertexX
 - anIntArray674 -> vertexZ
 - anIntArray675 -> vertexY
 - anIntArray679 -> faceVertexA
 - anIntArray680 -> faceVertexB
 - anIntArray681 -> faceVertexC
 - anIntArray676 -> faceColorA
 - anIntArray677 -> faceColorB
 - anIntArray678 -> faceColorC
- anIntArray682 -> faceTexture
- anIntArray693 -> faceOrderA
- anIntArray694 -> faceOrderB
- anIntArray695 -> faceOrderC
- anIntArrayArray696 -> shapeVertexIndices
- anIntArrayArray697 -> shapeFaceTemplates

## Varp
 - anInt702 -> varpCount
 - anIntArray703 -> varpIndices
 - anInt709 -> actionType
 - aBoolean713 -> isActive
## Game
 - aBoolean831 -> flameThreadActive
 - aBoolean872 -> useJaggrab
 - aBoolean954 -> hasFocus
 - aBoolean972 -> scrollBarDragging
 - aBoolean993 -> initialLoadComplete
 - aBoolean1017 -> cameraUpdatePending
 - aBoolean1031 -> characterDesignChanged
 - aBoolean1047 -> isMaleCharacter
 - aBoolean1080 -> regionLoading
 - aBoolean1141 -> forceMapReload
 - aBoolean1242 -> itemBeingDragged
 - aClass19_1013 -> projectileList
 - aClass19_1056 -> graphicsObjectList
 - aClass9_1059 -> chatScrollComponent
 - aBackground_966 -> loginBoxBackground
 - aBackground_967 -> loginButtonBackground
 - aClass30_Sub2_Sub1_Sub1_931 -> maleIconSprite
 - aClass30_Sub2_Sub1_Sub1_932 -> femaleIconSprite
 - aClass30_Sub2_Sub1_Sub1_1201 -> titleBackgroundLeft
 - aClass30_Sub2_Sub1_Sub1_1202 -> titleBackgroundRight
 - aByteArray912 -> soundPayload
 - anIntArrayArray1003 -> appearanceColorOptions
 - anIntArray1204 -> additionalColorCodes
 - aCRC32_930 -> fileCRC
 - aRSImageProducer_1107 -> titleImageProducer
 - aRSImageProducer_1108 -> loginLeftProducer
 - aRSImageProducer_1109 -> loginRightProducer
 - aRSImageProducer_1110 -> titleLeftProducer
 - aRSImageProducer_1111 -> titleRightProducer
 - aRSImageProducer_1112 -> titleTopLeftProducer
 - aRSImageProducer_1113 -> titleTopRightProducer
 - aRSImageProducer_1114 -> titleBottomLeftProducer
 - aRSImageProducer_1115 -> titleBottomRightProducer
 - aRSImageProducer_1123 -> tabAreaIconBuffer
 - aRSImageProducer_1124 -> tabAreaBackgroundBuffer
 - aRSImageProducer_1125 -> mapEdgeBuffer
 - aRSImageProducer_1163 -> textBackground
 - aRSImageProducer_1164 -> chatBackground
- aRSImageProducer_1165 -> tabAreaBuffer
- aRSImageProducer_1166 -> fullScreenBackground
- aLong824 -> loadingStartTime
- aSocket832 -> jaggrabSocket
- aStream_834 -> chatBuffer
- aStream_847 -> updateBuffer
- aStreamArray895s -> playerBuffers
- aLong953 -> privateMessageRecipient
- aStringArray983 -> overheadTexts
- aBackgroundArray1152s -> runeBackgrounds
- aLong1172 -> lastSoundUpdate
- aByteArrayArray1183 -> terrainData
- aLong1215 -> serverSessionKey
- aLong1220 -> lastMouseClickTime
- anInt1239 -> pathSearchMax
- aByteArrayArray1247 -> objectMapData
- aTextDrawingArea_1270 -> plainFont
- aTextDrawingArea_1271 -> boldFont
- aTextDrawingArea_1273 -> smallFont
- unknownInt1289 -> unusedSlotIndex
- unknownInt1290 -> unusedSettingValue
- aByteArray347 -> queuedMidiData
- anIntArray385 -> midiChannels
- unusedInt1215 -> loginScreenDelay
- unusedField1273 -> unusedRecoveryDate
- unknownInt10 -> localPlayerIndex
- aClass19_1179 -> pendingSpawns
- aClass11Array1230 -> collisionMaps
- bigX -> pathTileX
- bigY -> pathTileY
- minimapInt2 -> minimapRotationOffset
- minimapInt3 -> minimapZoom
- backVmidIP2_2 -> midSubscreenBuffer
- anInt974 -> overheadTextCount
- obj -> bestItem
- obj1 -> secondItem
- obj2 -> thirdItem
- class30_sub2_sub4_sub2_1 -> itemCandidate
- obj -> entity
- class9_1 -> childWidget
- class9_2 -> targetWidget
- class9_3 -> configWidget
- class9_4 -> stackWidget
- class9_5 -> offsetWidget
- class30_sub2_sub4_sub4 -> projectile
- class30_sub1 -> pendingSpawn
- class30_sub1_1 -> pendingSpawnIter
- class30_sub2_sub4_sub2_2 -> newItem
- class30_sub2_sub4_sub2_3 -> itemToUpdate
- aclass30_sub2_sub4_sub6s -> modelParts
- aclass30_sub2_sub1_sub4s -> textFonts
- class30_sub2_sub1_sub1_2 -> itemSprite
- class30_sub2_sub1_sub1_1 -> slotSprite
- class30_sub2_sub2 -> crcStream

## DrawingArea
 - anInt1387 -> viewportHalfHeight

## EntityDef
 - anInt55 -> turn90CCWAnimation
 - anInt56 -> cacheIndex
 - anInt57 -> transformVarbit
 - anInt58 -> turn180Animation
 - anInt59 -> transformVarp
 - anInt67 -> walkAnimation
 - aByte68 -> size
 - anIntArray70 -> modifiedModelColors
 - anIntArray73 -> headModelIds
 - anInt75 -> headIcon
 - anIntArray76 -> originalModelColors
 - anInt77 -> standAnimation
 - anInt79 -> turnSpeed
 - anInt83 -> turn90CWAnimation
 - anInt85 -> ambient
 - anInt92 -> contrast
 - anIntArray94 -> modelIds
 - aBoolean84 -> clickable
 - aBoolean87 -> minimapVisible
 - aBoolean93 -> priorityRender
 - aclass30_sub2_sub4_sub6s -> tempModels
## Animation
 - anInt352 -> frameCount
 - anIntArray353 -> frameIds
 - anIntArray354 -> secondaryFrameIds
 - anIntArray355 -> frameLengths
 - anInt356 -> frameStep
 - anIntArray357 -> interleaveOrder
 - aBoolean358 -> stretches
 - anInt359 -> priority
 - anInt360 -> leftHandItem
 - anInt361 -> rightHandItem
 - anInt362 -> maxLoops
 - anInt363 -> precedenceAnimating
- anInt364 -> precedenceWalking
- anInt365 -> replayMode
- anInt367 -> animationCount

## NodeSub
 - anInt1305 -> subNodeCounter

## Entity
 - aBoolean1541 -> forcedAnimation
 - aBooleanArray1553 -> movementQueueFlags

## SizeConstants
 - anIntArray552 -> permutationTable
