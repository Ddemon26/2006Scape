import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Controlled original-name observations of the interface hover ids (areaHoverId, viewportHoverId, tabAreaHoverId, chatAreaHoverId),
 * the inventory drag state (dragInterfaceId, dragSlotIndex, dragStartX, dragStartY, dragHeldCycles) and the selected-item state
 * (selectedItemSlot, selectedItemInterfaceId, selectedItemId). Consumers in drawInterface are covered by InterfaceFieldRolesTest and
 * GameDrawInterfaceBranchTest.
 */
public class InterfaceHoverDragSelectionFieldsTest {
    private RSInterface[] savedCache;
    private Object savedItemCache;

    @Before public void before() throws Exception {
        savedCache = RSInterface.interfaceCache;
        RSInterface.interfaceCache = new RSInterface[400];
        Field f = ItemDef.class.getDeclaredField("cache"); f.setAccessible(true); savedItemCache = f.get(null);
        Constructor<ItemDef> k = ItemDef.class.getDeclaredConstructor(); k.setAccessible(true);
        ItemDef[] cache = new ItemDef[10];
        for (int n = 0; n < 10; n++) { cache[n] = k.newInstance(); cache[n].id = -1; }
        cache[3].id = 777; cache[3].name = "Test item";
        f.set(null, cache);
    }
    @After public void after() throws Exception {
        RSInterface.interfaceCache = savedCache;
        Field f = ItemDef.class.getDeclaredField("cache"); f.setAccessible(true); f.set(null, savedItemCache);
    }

    // ---------- hover ids: processRightClick routes the scratch hover id of each screen area ----------
    static class HoverSpy extends Game {
        @Override public void buildSplitPrivateChatMenu() { }
        @Override public void processMinimapActions() { }
        @Override public void build3dScreenMenu() { }
        @Override public void buildChatAreaMenu(int hoverY) { }
    }
    private static RSInterface root(int id, int childId) {
        RSInterface root = new RSInterface(); root.id = id; root.type = 0; root.width = 200; root.height = 200;
        RSInterface c = new RSInterface(); c.id = childId; c.type = 5; c.width = 20; c.height = 20; c.hoverRedirectId = -1; c.disabledHoverColor = 0x1234;
        root.children = new int[]{childId}; root.childX = new int[]{10}; root.childY = new int[]{10};
        RSInterface.interfaceCache[id] = root; RSInterface.interfaceCache[childId] = c;
        return root;
    }
    private static HoverSpy hoverGame(int mx, int my) {
        HoverSpy g = new HoverSpy();
        g.openInterfaceID = -1; g.invOverlayInterfaceID = -1; g.backDialogID = -1; g.dialogID = -1; g.fullScreenInterfaceId = -1;
        g.viewportHoverId = 99; g.tabAreaHoverId = 99; g.chatAreaHoverId = 99;
        g.mouseX = mx; g.mouseY = my;
        return g;
    }
    @Test public void mainAreaHoverIdIsTakenFromTheOpenInterfaceAndClearedOtherwise() {
        root(1, 2);
        HoverSpy g = hoverGame(20, 20); g.openInterfaceID = 1;
        g.needDrawTabArea = false; g.inputTaken = false;
        g.processRightClick();
        assertEquals(2, g.viewportHoverId);     // child id hovered inside area 4..516 x 4..338
        assertEquals(0, g.tabAreaHoverId);     // tab area scanned and empty: cleared (was 99)
        assertTrue(g.needDrawTabArea);    // the tab hover id changed
        assertEquals(99, g.chatAreaHoverId);    // no dialog open: untouched
        assertFalse(g.inputTaken);
        g = hoverGame(40, 30); g.openInterfaceID = 1; // inside root, outside child (child spans 14..34)
        g.processRightClick();
        assertEquals(0, g.viewportHoverId);
        g = hoverGame(20, 20);            // mouse inside but no interface open: area scanned, id cleared
        g.processRightClick();
        assertEquals(0, g.viewportHoverId);
        g = hoverGame(600, 20); g.openInterfaceID = 1;   // mouse outside the area (x > 516)
        g.processRightClick();
        assertEquals(0, g.viewportHoverId);
    }
    @Test public void tabAreaHoverIdIsTakenFromTheTabOrOverlayInterfaceAndRedrawsOnChange() {
        root(3, 4);
        HoverSpy g = hoverGame(570, 225); g.tabID = 2; g.tabInterfaceIDs[2] = 3;   // child spans 563..583 x 215..235
        g.processRightClick();
        assertEquals(4, g.tabAreaHoverId);
        assertTrue(g.needDrawTabArea);
        assertEquals(0, g.viewportHoverId);     // viewport scanned and empty
        g.needDrawTabArea = false; g.viewportHoverId = 99;
        g.processRightClick();
        assertFalse(g.needDrawTabArea);   // unchanged hover id: no redraw
        // overlay interface takes precedence over the selected tab
        root(5, 6);
        g = hoverGame(570, 225); g.tabID = 2; g.tabInterfaceIDs[2] = 3; g.invOverlayInterfaceID = 5;
        g.processRightClick();
        assertEquals(6, g.tabAreaHoverId);
        g = hoverGame(570, 225); g.tabID = 1; g.tabInterfaceIDs[2] = 3;   // selected tab has no interface
        g.processRightClick();
        assertEquals(0, g.tabAreaHoverId);
    }
    @Test public void chatAreaHoverIdNeedsAnOpenDialogAndRequestsInputRedrawOnChange() {
        root(7, 8);
        HoverSpy g = hoverGame(40, 380); g.backDialogID = 7;      // child spans 27..47 x 367..387
        g.processRightClick();
        assertEquals(8, g.chatAreaHoverId);
        assertTrue(g.inputTaken);
        assertEquals(0, g.viewportHoverId);
        g.inputTaken = false;
        g.processRightClick();
        assertFalse(g.inputTaken);        // unchanged: no redraw request
        g = hoverGame(40, 380); g.dialogID = 7;
        g.processRightClick();
        assertEquals(8, g.chatAreaHoverId);
        g = hoverGame(40, 380);           // no dialog: untouched even though the mouse is in the area
        g.processRightClick();
        assertEquals(99, g.chatAreaHoverId);
        g = hoverGame(40, 340); g.backDialogID = 7;   // above the chat area
        g.processRightClick();
        assertEquals(0, g.chatAreaHoverId);
    }
    @Test public void fullScreenInterfaceFeedsOnlyTheMainAreaHoverId() {
        root(1, 2);
        HoverSpy g = hoverGame(15, 15); g.fullScreenInterfaceId = 1; g.openInterfaceID = 1;   // child spans 10..30 from origin 0
        g.processRightClick();
        assertEquals(2, g.viewportHoverId);
        assertEquals(99, g.tabAreaHoverId);
        assertEquals(99, g.chatAreaHoverId);
    }

    // ---------- drag state: written when a draggable inventory entry is pressed ----------
    private static Game pressGame(int actionId, boolean draggable, int parentId, int openId, int backId) {
        Game g = new Game();
        g.stream = new Stream(new byte[64]); g.stream.encryption = new ISAACRandomGen(new int[]{1, 2, 3, 4});
        RSInterface c = new RSInterface(); c.id = 7; c.aBoolean259 = draggable; c.parentID = parentId;
        RSInterface.interfaceCache[7] = c;
        g.menuActionRow = 1; g.menuActionID[0] = actionId; g.menuActionCmd2[0] = 5; g.menuActionCmd3[0] = 7;
        g.openInterfaceID = openId; g.backDialogID = backId;
        g.clickMode3 = 1; g.saveClickX = 123; g.saveClickY = 77;
        g.dragInterfaceId = -9; g.dragSlotIndex = -9; g.dragStartX = -9; g.dragStartY = -9; g.dragHeldCycles = 9; g.dragMoved = true;
        return g;
    }
    private static boolean press(Game g) { try { return g.processMenuClick(); } catch (RuntimeException e) { return false; } }
    @Test public void pressingADraggableEntryRecordsInterfaceSlotAndStartPosition() {
        Game g = pressGame(632, true, -5, -1, -1);
        assertTrue(g.processMenuClick());
        assertEquals(7, g.dragInterfaceId);     // component id (menu cmd3)
        assertEquals(5, g.dragSlotIndex);     // slot index (menu cmd2)
        assertEquals(123, g.dragStartX);   // click x
        assertEquals(77, g.dragStartY);    // click y
        assertEquals(0, g.dragHeldCycles);      // held-cycle counter restarted
        assertFalse(g.dragMoved);
        assertEquals(2, g.activeInterfaceType);
        g = pressGame(632, true, 40, 40, -1); press(g); assertEquals(1, g.activeInterfaceType);
        g = pressGame(632, true, 40, -1, 40); press(g); assertEquals(3, g.activeInterfaceType);
        g = pressGame(633, true, -5, -1, -1); press(g);    // other action id: no drag
        assertEquals(-9, g.dragInterfaceId); assertEquals(0, g.activeInterfaceType);
        g = pressGame(632, false, -5, -1, -1); press(g);   // not draggable: no drag
        assertEquals(-9, g.dragInterfaceId); assertEquals(9, g.dragHeldCycles); assertEquals(0, g.activeInterfaceType);
    }

    // ---------- drag state: advanced every cycle by mainGameProcessor ----------
    static class DragSpy extends Game {
        int hoverSlot, hoverInterface;
        @Override public boolean parsePacket() { return false; }
        @Override public void loadingStages() { }
        @Override public void advancePendingSceneChanges() { }
        @Override public void advancePlayerEntities() { }
        @Override public void advanceNpcEntities() { }
        @Override public void decrementOverheadTextTimers() { }
        @Override public void dropClient() { }
        @Override public void processRightClick() { lastActiveInvInterface = hoverInterface; mouseInvInterfaceIndex = hoverSlot; }
    }
    private static DragSpy cycleGame() {
        DragSpy s = new DragSpy();
        s.mouseDetection = new MouseDetection(s);
        s.stream = new Stream(new byte[5000]); s.stream.encryption = new ISAACRandomGen(new int[]{1, 2, 3, 4});
        s.loggedIn = true;
        s.dragStartX = 100; s.dragStartY = 100; s.mouseX = 100; s.mouseY = 100;
        s.activeInterfaceType = 1; s.dragMoved = false; s.dragHeldCycles = 3; s.clickMode2 = 1;
        return s;
    }
    private static void cycle(Game s) { try { s.mainGameProcessor(); } catch (RuntimeException e) { /* rest of the cycle needs a live session */ } }
    @Test public void heldCycleCounterIncrementsOncePerCycleOnlyWhileDragging() {
        DragSpy s = cycleGame(); cycle(s); assertEquals(4, s.dragHeldCycles); cycle(s); assertEquals(5, s.dragHeldCycles);
        s = cycleGame(); s.activeInterfaceType = 0; cycle(s); assertEquals(3, s.dragHeldCycles);
    }
    @Test public void movedFlagNeedsMoreThanFivePixelsFromTheStartPosition() {
        int[][] cases = {{105, 100, 0}, {106, 100, 1}, {95, 100, 0}, {94, 100, 1}, {100, 105, 0}, {100, 106, 1}, {100, 95, 0}, {100, 94, 1}};
        for (int[] c : cases) {
            DragSpy s = cycleGame(); s.mouseX = c[0]; s.mouseY = c[1]; cycle(s);
            assertEquals("mouse " + c[0] + "," + c[1], c[2] == 1, s.dragMoved);
        }
    }
    private int[] release(int heldBefore, boolean moved, int hoverInterface, int hoverSlot, int[] ids, int fromSlot) {
        DragSpy s = cycleGame();
        RSInterface inv = new RSInterface(); inv.id = 300; inv.itemIdsPlusOne = ids.clone(); inv.itemQuantities = new int[ids.length];
        RSInterface.interfaceCache[300] = inv;
        s.dragInterfaceId = 300; s.dragSlotIndex = fromSlot; s.dragHeldCycles = heldBefore; s.dragMoved = moved; s.clickMode2 = 0;
        s.hoverInterface = hoverInterface; s.hoverSlot = hoverSlot;
        cycle(s);
        return inv.itemIdsPlusOne;
    }
    @Test public void releaseSwapsFromTheRecordedSlotOfTheRecordedInterfaceOnlyAfterFiveHeldCycles() {
        int[] ids = {11, 22, 33, 44};
        assertArrayEquals(new int[]{33, 22, 11, 44}, release(4, true, 300, 2, ids, 0));   // 4 + this cycle = 5: drop applies
        assertArrayEquals(ids, release(3, true, 300, 2, ids, 0));                        // 4 held cycles: treated as a click
        assertArrayEquals(ids, release(10, false, 300, 2, ids, 0));                      // never moved: no swap
        assertArrayEquals(ids, release(10, true, 301, 2, ids, 0));                       // released over another interface
        assertArrayEquals(new int[]{44, 22, 33, 11}, release(10, true, 300, 0, ids, 3)); // source slot is the recorded one
        assertArrayEquals(ids, release(10, true, 300, 3, ids, 3));                       // same slot: nothing
    }
    @Test public void releaseSendsTheRecordedInterfaceAndSlotInPacket214() {
        DragSpy s = cycleGame();
        RSInterface inv = new RSInterface(); inv.id = 300; inv.itemIdsPlusOne = new int[]{11, 22, 33, 44}; inv.itemQuantities = new int[4];
        RSInterface.interfaceCache[300] = inv;
        s.dragInterfaceId = 300; s.dragSlotIndex = 1; s.dragHeldCycles = 10; s.dragMoved = true; s.clickMode2 = 0; s.hoverInterface = 300; s.hoverSlot = 3;
        cycle(s);
        Stream ref = new Stream(new byte[16]);
        ref.writeShortLittleEndianAdd128(300); ref.writeNegatedByte(0); ref.writeShortLittleEndianAdd128(1); ref.writeShortLittleEndian(3);
        assertEquals(1 + 7, s.stream.currentOffset);
        for (int n = 0; n < 7; n++) assertEquals("byte " + n, ref.buffer[n], s.stream.buffer[1 + n]);
    }

    // ---------- dragMoved (drag moved flag) and the release routine's locals ----------
    @Test public void movedFlagStaysSetWhenTheMouseReturnsToTheStartPosition() {
        DragSpy s = cycleGame(); s.mouseX = 120; cycle(s); assertTrue(s.dragMoved);
        s.mouseX = 100; cycle(s); assertTrue(s.dragMoved);                    // never re-cleared by movement
        s = cycleGame(); s.dragMoved = true; s.mouseX = 100; cycle(s); assertTrue(s.dragMoved);
        s = cycleGame(); s.activeInterfaceType = 0; s.mouseX = 150; cycle(s); assertFalse(s.dragMoved); // not dragging: untouched
    }
    @Test public void releaseDoesNotClearTheMovedFlagButPressDoes() {
        DragSpy s = cycleGame(); s.dragMoved = true; s.dragHeldCycles = 10; s.clickMode2 = 0; s.dragInterfaceId = 300;
        cycle(s); assertEquals(0, s.activeInterfaceType); assertTrue(s.dragMoved);
        Game g = pressGame(632, true, -5, -1, -1); g.dragMoved = true; g.processMenuClick(); assertFalse(g.dragMoved);
    }
    @Test public void startItemDragMarksTheDragAsMovedAtOnce() {
        DragSpy s = new DragSpy(); s.loggedIn = true;
        RSInterface inv = new RSInterface(); inv.id = 300; inv.aBoolean259 = true; inv.itemIdsPlusOne = new int[]{11, 0};
        RSInterface.interfaceCache[300] = inv;
        s.hoverInterface = 300; s.hoverSlot = 0; s.dragMoved = false; s.dragHeldCycles = 0;
        assertTrue(s.startItemDrag(50, 60));
        assertTrue(s.dragMoved); assertEquals(10, s.dragHeldCycles); assertEquals(300, s.dragInterfaceId); assertEquals(0, s.dragSlotIndex);
        s = new DragSpy(); s.loggedIn = true; s.hoverInterface = 300; s.hoverSlot = 1; s.dragMoved = false;   // empty slot: no drag
        assertFalse(s.startItemDrag(50, 60)); assertFalse(s.dragMoved);
    }
    private DragSpy releaseWith(boolean moved, boolean freeSwap, int mode, int content, int[] ids, int[] qty, int from, int to, RSInterface[] out) {
        DragSpy s = cycleGame();
        RSInterface inv = new RSInterface(); inv.id = 300; inv.contentType = content; inv.aBoolean235 = freeSwap;
        inv.itemIdsPlusOne = ids.clone(); inv.itemQuantities = qty.clone(); out[0] = inv;
        RSInterface.interfaceCache[300] = inv;
        s.insertDragMode = mode;
        s.dragInterfaceId = 300; s.dragSlotIndex = from; s.dragHeldCycles = 10; s.dragMoved = moved; s.clickMode2 = 0;
        s.hoverInterface = 300; s.hoverSlot = to;
        cycle(s);
        return s;
    }
    private int packetFlagByte(DragSpy s) {
        Stream ref = new Stream(new byte[16]); ref.writeNegatedByte(1);
        return s.stream.buffer[1 + 2] == ref.buffer[0] ? 1 : 0;   // byte after the interface id short
    }
    @Test public void releasePacketFlagByteIsOneOnlyForAnInsertOnAnOccupiedSlot() {
        RSInterface[] o = new RSInterface[1];
        assertEquals(1, packetFlagByte(releaseWith(true, false, 1, 206, new int[]{11, 22, 33, 44}, new int[4], 0, 2, o)));
        assertEquals(0, packetFlagByte(releaseWith(true, false, 0, 206, new int[]{11, 22, 33, 44}, new int[4], 0, 2, o)));
        assertEquals(0, packetFlagByte(releaseWith(true, false, 1, 206, new int[]{11, 22, 33, 0}, new int[4], 0, 3, o)));
    }
    @Test public void insertWalksFromTheSourceSlotToTheTargetSlotInBothDirections() {
        RSInterface[] o = new RSInterface[1];
        releaseWith(true, false, 1, 206, new int[]{11, 22, 33, 44, 55}, new int[5], 1, 4, o);
        assertArrayEquals(new int[]{11, 33, 44, 55, 22}, o[0].itemIdsPlusOne);   // forward: source goes to target, others shift down
        releaseWith(true, false, 1, 206, new int[]{11, 22, 33, 44, 55}, new int[5], 4, 1, o);
        assertArrayEquals(new int[]{11, 55, 22, 33, 44}, o[0].itemIdsPlusOne);   // backward: others shift up
    }
    @Test public void freeSwapInterfaceMovesItemAndQuantityToTheTargetAndEmptiesTheSource() {
        RSInterface[] o = new RSInterface[1];
        DragSpy s = releaseWith(true, true, 0, 0, new int[]{11, 22, 33, 44}, new int[]{5, 6, 7, 8}, 1, 3, o);
        assertArrayEquals(new int[]{11, -1, 33, 22}, o[0].itemIdsPlusOne);
        assertArrayEquals(new int[]{5, 0, 7, 6}, o[0].itemQuantities);
        assertEquals(1 + 7, s.stream.currentOffset);                              // packet 214 still sent
        releaseWith(true, true, 1, 206, new int[]{11, 22, 33, 44}, new int[]{5, 6, 7, 8}, 3, 0, o);   // free swap wins over insert
        assertArrayEquals(new int[]{44, 22, 33, -1}, o[0].itemIdsPlusOne);
        assertArrayEquals(new int[]{8, 6, 7, 0}, o[0].itemQuantities);
    }
    @Test public void releaseWithoutMovementFallsBackToTheMenuAndSendsNoDropPacket() {
        RSInterface[] o = new RSInterface[1];
        DragSpy s = releaseWith(false, false, 0, 0, new int[]{11, 22, 33, 44}, new int[4], 0, 2, o);
        assertArrayEquals(new int[]{11, 22, 33, 44}, o[0].itemIdsPlusOne);
        assertEquals(0, s.stream.currentOffset);                                  // no 7-byte drop packet
        assertEquals(10, s.atInventoryLoopCycle);
    }

    // ---------- selected item: written by action 447 ----------
    private static Game selectGame(int actionId) {
        Game g = new Game();
        g.stream = new Stream(new byte[64]); g.stream.encryption = new ISAACRandomGen(new int[]{1, 2, 3, 4});
        g.menuActionID[0] = actionId; g.menuActionCmd1[0] = 777; g.menuActionCmd2[0] = 5; g.menuActionCmd3[0] = 7;
        g.selectedItemSlot = -9; g.selectedItemInterfaceId = -9; g.selectedItemId = -9;
        return g;
    }
    @Test public void action447RecordsSelectedSlotInterfaceAndItemId() {
        Game g = selectGame(447); g.doAction(0);
        assertEquals(1, g.itemSelected);
        assertEquals(5, g.selectedItemSlot);     // slot (menu cmd2)
        assertEquals(7, g.selectedItemInterfaceId);     // interface id (menu cmd3)
        assertEquals(777, g.selectedItemId);   // item id (menu cmd1): its definition name becomes the selected item name
        assertEquals("Test item", g.selectedItemName);
        g = selectGame(446); g.doAction(0);
        assertEquals(0, g.itemSelected); assertEquals(-9, g.selectedItemSlot); assertEquals(-9, g.selectedItemInterfaceId); assertEquals(-9, g.selectedItemId);
    }
    @Test public void useItemOnItemPacketCarriesTheSelectedSlotInterfaceAndItem() {
        Game g = selectGame(870); g.menuActionCmd1[0] = 600; g.menuActionCmd2[0] = 21; g.menuActionCmd3[0] = 309;
        RSInterface.interfaceCache[309] = new RSInterface();
        g.selectedItemSlot = 5; g.selectedItemInterfaceId = 313; g.selectedItemId = 777;
        g.doAction(0);
        Stream ref = new Stream(new byte[32]);
        ref.writeShortBigEndian(21);                 // target slot
        ref.writeShortBigEndianAdd128(5);            // selected slot
        ref.writeShortLittleEndianAdd128(600);       // target item id
        ref.writeShortBigEndian(313);                // selected interface id
        ref.writeShortLittleEndian(777);             // selected item id
        ref.writeShortBigEndian(309);                // target interface id
        assertEquals(1 + 12, g.stream.currentOffset);
        for (int n = 0; n < 12; n++) assertEquals("byte " + n, ref.buffer[n], g.stream.buffer[1 + n]);
    }

    // ---------- selected item: the matching inventory slot gets no "Use ... with" row ----------
    private int useWithRows(int selectedSlot, int selectedInterface, int hoverX) {
        RSInterface root = new RSInterface(); root.id = 1; root.type = 0; root.width = 200; root.height = 100;
        RSInterface inv = new RSInterface(); inv.id = 2; inv.type = 2; inv.width = 2; inv.height = 1; inv.isInventoryInterface = true;
        inv.itemIdsPlusOne = new int[]{778, 778}; inv.itemQuantities = new int[]{1, 1};
        inv.spritesX = new int[20]; inv.spritesY = new int[20]; inv.invSpritePadX = 0; inv.invSpritePadY = 0; inv.actions = new String[5];
        root.children = new int[]{2}; root.childX = new int[]{0}; root.childY = new int[]{0};
        RSInterface.interfaceCache[1] = root; RSInterface.interfaceCache[2] = inv;
        Game g = new Game();
        g.itemSelected = 1; g.selectedItemName = "Sel"; g.spellSelected = 0;
        g.selectedItemSlot = selectedSlot; g.selectedItemInterfaceId = selectedInterface;
        g.menuActionRow = 0;
        g.buildInterfaceMenu(0, root, hoverX, 0, 10, 0);   // slot 0 spans x 0..31, slot 1 spans 32..63
        int rows = 0;
        for (int n = 0; n < g.menuActionRow; n++) if (g.menuActionID[n] == 870) rows++;
        return rows;
    }
    @Test public void useWithRowIsSkippedOnlyForTheSelectedSlotOfTheSelectedInterface() {
        assertEquals(0, useWithRows(0, 2, 10));   // same interface and slot
        assertEquals(1, useWithRows(1, 2, 10));   // other slot selected
        assertEquals(1, useWithRows(0, 9, 10));   // other interface
        assertEquals(0, useWithRows(1, 2, 40));   // slot 1 hovered and selected
        assertEquals(1, useWithRows(0, 2, 40));
    }
    @Test public void twelveItemActionBranchesRecordComponentIdAndZeroBasedSlotIndependently() {
        int[] actions = {74,431,53,539,870,847,78,632,493,867,543,454};
        for (int action : actions) for (int id : new int[]{100,101}) for (int slot : new int[]{0,4}) {
            RSInterface component = new RSInterface(); component.id = id; component.parentID = 50;
            RSInterface.interfaceCache[id] = component;
            Game g = new Game();
            g.stream = new Stream(new byte[128]); g.stream.encryption = new ISAACRandomGen(new int[]{1,2,3,4});
            g.menuActionID[0] = action; g.menuActionCmd1[0] = 777;
            g.menuActionCmd2[0] = slot; g.menuActionCmd3[0] = id;
            g.actionHighlightInterfaceId = -9; g.actionHighlightSlotIndex = -9; g.atInventoryLoopCycle = 8;
            g.doAction(0);
            assertEquals("action " + action, id, g.actionHighlightInterfaceId);
            assertEquals("action " + action, slot, g.actionHighlightSlotIndex);
            assertEquals(0, g.atInventoryLoopCycle);
            assertEquals(2, g.atInventoryInterfaceType);
        }
    }
}
