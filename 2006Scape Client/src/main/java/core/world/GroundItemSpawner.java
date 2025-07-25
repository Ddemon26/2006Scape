package core.world;

import core.engine.Game;
import game.items.Item;
import game.definitions.ItemDef;
import util.collections.Node;
import util.collections.NodeList;

/** Manages ground item piles extracted from {@link Game}. */
public final class GroundItemSpawner {
    private final Game game;

    public GroundItemSpawner(Game game) {
        this.game = game;
    }

    public void spawnGroundItem(int i, int j) {
        NodeList itemList = game.groundArray[game.plane][i][j];
        if (itemList == null) {
            game.worldController.clearItemPile(game.plane, i, j);
            return;
        }
        long k = Long.MIN_VALUE;
        Item bestItem = null;
        for (Item itemCandidate = (Item) itemList.reverseGetFirst();
             itemCandidate != null; itemCandidate = (Item) itemList.reverseGetNext()) {
            ItemDef itemDef = ItemDef.lookup(itemCandidate.ID);
            long l = itemDef.value;
            if (itemDef.stackable) {
                l *= itemCandidate.amount + 1;
            }
            if (l > k) {
                k = l;
                bestItem = itemCandidate;
            }
        }
        itemList.insertTail((Node) bestItem);
        Item secondItem = null;
        Item thirdItem = null;
        for (Item item = (Item) itemList.reverseGetFirst();
             item != null; item = (Item) itemList.reverseGetNext()) {
            if (item.ID != bestItem.ID && secondItem == null) {
                secondItem = item;
            }
            if (item.ID != bestItem.ID && item.ID != secondItem.ID && thirdItem == null) {
                thirdItem = item;
            }
        }
        int i1 = i + (j << 7) + 0x60000000;
        game.worldController.addItemPile(i, i1, secondItem,
                game.getTileHeight(game.plane, j * 128 + 64, i * 128 + 64),
                thirdItem, bestItem, game.plane, j);
    }
}
