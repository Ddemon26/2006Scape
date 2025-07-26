# Game

Defined in [`2006Scape Client/src/main/java/Game.java`](2006Scape Client/src/main/java/Game.java).

NOTICE: IF YOU CHANGE ANYTHING IN GAME.JAVA, PLEASE COPY-PASTE THE WHOLE CLASS OVER TO LOCALGAME.JAVA THIS IS TO ALLOW LOCAL PARABOT TO CONTINUE TO WORK

```java
public class Game extends RSApplet {
// music methods moved to [GameMusicController](GameMusicController.md)
public void drawChatArea()
public void init()
public void startRunnable(Runnable runnable, int i)
public Socket openSocket(int i) throws IOException
public boolean processMenuClick() -> MenuManager.processMenuClick
public void saveMidi(boolean flag, byte abyte0[])
public void constructMapRegion() -> MapRegionBuilder.constructMapRegion
// cache cleanup moved to [CacheUtils](CacheUtils.md)
// minimap rendering moved to [MinimapRenderer](MinimapRenderer.md)
// ground item spawning moved to [GroundItemSpawner](GroundItemSpawner.md)
public void addNpcsToScene(boolean flag)
public boolean replayWave()
public void loadError()
public void buildInterfaceMenu(int i, RSInterface parentInterface, int k, int l, int i1, int j1)
public void drawScrollThumb(int j, int k, int l, int i1, int j1)
public void updateNPCs(Stream stream, int i)
public void processChatModeClick()
public void applyVarp(int i) -> SettingApplier.applyVarp
public void updateEntities() -> EntityOverlayRenderer.renderEntityOverlays
public void delFriend(long l) -> FriendManager.delFriend
public void drawButton(boolean enabled, int x, int y, int width) -> TabAreaRenderer.drawButton
public void drawCheckbox(boolean enabled, int x, int y) -> TabAreaRenderer.drawCheckbox
public void drawTabArea() -> TabAreaRenderer.drawTabArea
public void animateTextures(int j) -> TabAreaRenderer.animateTextures
public void updateEntityText() -> EntityTextUpdater.updateEntityText
public void calcCameraPos() -> CameraManager.calcCameraPos
// menu rendering moved to [MenuManager](MenuManager.md)
public void addFriend(long l) -> FriendManager.addFriend
public int getTileHeight(int plane, int worldY, int worldX)
public static String intToKOrMil(int j)
public static String intToShortLetter(long number)
public void resetLogout() -> LoginManager.resetLogout
public void resetCharacterOptions()
public void addLocalNPCs(int i, Stream stream)
public void processGameLoop()
public void addPlayersToScene(boolean flag)
public boolean promptUserForInput(RSInterface widget)
public void processPlayerUpdateMasks(Stream stream)
public void drawMinimapLoc(int i, int k, int l, int i1, int j1)
public static void setHighMem()
public void loadingStages()
public int checkMapLoadStatus()
public void processProjectiles()
public AppletContext getAppletContext()
public void processOnDemandQueue()
public void calcFlamesPosition()
public boolean saveWave(byte abyte0[], int i)
public void resetInterfaceAnimation(int i) -> InterfaceInputHandler.resetInterfaceAnimation
public void drawHeadIcon()
public void mainGameProcessor()
public void locatePendingSpawns() -> PendingSpawnManager.locatePendingSpawns
public void handleScrollbarInput(int i, int j, int k, int l, RSInterface scrollInterface, int i1, boolean flag, int j1) -> InterfaceInputHandler.handleScrollbarInput
public boolean walkToObject(int i, int j, int k) -> Pathfinder.walkToObject
public StreamLoader streamLoaderForName(int i, String s, String s1, int j, int k) -> LoadingHandler.streamLoaderForName
public void dropClient() -> LoginManager.dropClient
public void drawTextOnScreen(String s, String s1)
public void doAction(int i)
public void updateRestrictedArea()
public void run()
public void build3dScreenMenu() -> MenuManager.build3dScreenMenu
public void cleanUpForQuit()
public void printDebug()
public void processInput() -> InputHandler.processInput
public void buildChatAreaMenu(int j) -> ChatAreaRenderer.buildChatAreaMenu
public void drawFriendsListOrWelcomeScreen(RSInterface interfaceComponent) -> FriendManager.drawFriendsListOrWelcomeScreen
public String formatDate(int i) -> GameUtils.formatDate
public void drawSplitpublicChat() -> ChatAreaRenderer.drawSplitpublicChat
public void pushMessage(String s, int i, String s1)
public void processMinimapActions() -> MinimapRenderer.processMinimapActions
public void processTabClick() -> InterfaceInputHandler.processTabClick
public void run()
public void resetImageProducers()
public void resetAllImageProducers()
public void resetImageProducers2()
public void drawMinimapHint(Sprite sprite, int y, int x)
public void processRightClick()
public int blendColors(int i, int j, int k)
public void login(String s, String s1, boolean flag)
public boolean doWalkTo(int i, int j, int k, int i1, int j1, int k1, int l1, int i2, int j2, boolean flag, int k2)
public void processNpcUpdateMasks(Stream stream)
public void buildAtNPCMenu(EntityDef entityDef, int i, int j, int k) -> MenuManager.buildAtNPCMenu
public void buildAtPlayerMenu(int i, int j, Player player, int k) -> MenuManager.buildAtPlayerMenu
public void locateSceneObject(PendingSpawn pendingSpawn) -> PendingSpawnManager.locateSceneObject
public final void processSoundQueue() -> GameMusicController.processSoundQueue
public void addLocalPlayers(Stream stream, int i)
public void processMainScreenClick()
public String interfaceIntToString(int j)
public void showErrorScreen()
public URL getCodeBase()
public void animateNpcs()
public void updateEntityMovement(Entity entity) -> EntityMovementHandler.updateEntityMovement
public void updateForcedMovement(Entity entity) -> EntityMovementHandler.updateForcedMovement
public void updateInterpolatedMovement(Entity entity) -> EntityMovementHandler.updateInterpolatedMovement
public void updateWalkingStep(Entity entity) -> EntityMovementHandler.updateWalkingStep
public void updateEntityFacing(Entity entity) -> EntityAnimationHandler.updateEntityFacing
public void updateEntityAnimation(Entity entity) -> EntityAnimationHandler.updateEntityAnimation
public void drawGameScreen()
public boolean buildFriendsListMenu(RSInterface listInterface) -> FriendManager.buildFriendsListMenu
public void processGraphicsObjects()
public void drawInterface(int scrollPos, int k, RSInterface widget, int l)
public void randomizeBackground(Background background)
public void decodePlayerUpdateMask(int i, int j, Stream stream, Player player)
public void updateCameraPosition() -> CameraManager.updateCameraPosition
public void processDrawing()
public boolean isFriendOrSelf(String s) -> FriendManager.isFriendOrSelf
public static String combatDiffColor(int i, int j)
public void setWaveVolume(int i)
public void draw3dScreen()
public void addIgnore(long l) -> IgnoreManager.addIgnore
public void animatePlayers()
public void processPendingSpawns() -> PendingSpawnManager.processPendingSpawns
// menu sizing moved to [MenuManager](MenuManager.md)
public void updateSelfMovement(Stream stream)
public void nullLoader()
public boolean updateInterfaceAnimations(int i, int j)
public int determineCameraPlane() -> CameraManager.determineCameraPlane
public int getCurrentPlane()
public void delIgnore(long l) -> IgnoreManager.delIgnore
public String getParameter(String s)
public int extractInterfaceValues(RSInterface component, int j)
public void drawTooltip()
public void drawMinimap() -> MinimapRenderer.drawMinimap
public void npcScreenPos(Entity entity, int i) -> MinimapRenderer.npcScreenPos
public void calcEntityScreenPos(int i, int j, int l) -> MinimapRenderer.calcEntityScreenPos
public void buildSplitPrivateChatMenu() -> ChatAreaRenderer.buildSplitPrivateChatMenu
public void queuePendingSpawn(int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2) -> PendingSpawnManager.queuePendingSpawn
public boolean interfaceIsSelected(RSInterface component)
public DataInputStream openJagGrabInputStream(String s) throws IOException -> LoadingHandler.openJagGrabInputStream
public void doFlamesDrawing()
public void updateOtherPlayers(Stream stream) -> PlayerUpdater.updateOtherPlayers
public void drawLoginScreen(boolean flag) -> LoginScreen.drawLoginScreen
public void drawFlames()
public void raiseWelcomeScreen()
public void handleMapPackets(Stream stream, int j) -> MapPacketHandler.handleMapPackets
public static void setLowMem()
public void updateNpcList(Stream stream) -> NpcUpdater.updateNpcList
public void processLoginScreenInput() -> LoginScreen.processLoginScreenInput
public void markMinimap(Sprite sprite, int i, int j) -> MinimapRenderer.markMinimap
public void updateSceneObjects(int i, int j, int k, int l, int i1, int j1, int k1)
public void updatePlayers(int i, Stream stream) -> PlayerUpdater.updatePlayers
public void setCameraPos(int j, int k, int l, int i1, int j1, int k1) -> CameraManager.setCameraPos
public boolean parsePacket()
public void run()
public void renderGameView()
public void closeOpenInterfaces()
public Game()
public static final String validUserPassChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\243$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";
public void keyPressed(KeyEvent keyevent) -> InputHandler.handleKeyPressed
public long calculateTotalExp() -> PlayerStatsCalculator.calculateTotalExp
public int calculateTotalLevels() -> PlayerStatsCalculator.calculateTotalLevels
public void definitionSearch(String name, int type) -> DefinitionSearcher.search
public void openInterface(int interfaceID) -> InterfaceInputHandler.openInterface
public void openSideInterface(int tab, int interfaceID) -> InterfaceInputHandler.openSideInterface
public void mouseWheelDragged(int i, int j) -> InputHandler.handleMouseWheelDragged
public final void mouseWheelMoved(MouseWheelEvent e) -> InputHandler.handleMouseWheelMoved
```

Utility helpers like `random` and `intToKOrMilLongName` now reside in
[`GameUtils`](GameUtils.md).
Screenshots are handled by [`ScreenshotUtil`](ScreenshotUtil.md)
and clipboard access via [`ClipboardUtil`](ClipboardUtil.md).
Chat area rendering code lives in [`ChatAreaRenderer`](ChatAreaRenderer.md).
Minimap logic lives in [`MinimapRenderer`](MinimapRenderer.md) and
ground item piles are handled by [`GroundItemSpawner`](GroundItemSpawner.md).
