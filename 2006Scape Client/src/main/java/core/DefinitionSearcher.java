package core;

import core.engine.Game;
import game.EntityDef;
import game.ItemDef;
import game.ObjectDef;
import ui.RSInterface;
import java.nio.charset.StandardCharsets;

/**
 * Searches item, NPC or object definitions extracted from {@link Game}.
 */
public final class DefinitionSearcher {
    private DefinitionSearcher() {}

    public static void search(Game game, String name, int type) {
        int amount;
        int definitionResultsTotal = 0;
        int[] definitionResultIDs = new int[352];
        String[] definitionResults = new String[352];
        String sType;
        if (type == 1) {
            amount = ItemDef.totalItems;
            sType = "Item";
        } else if (type == 2) {
            amount = EntityDef.totalNPCs;
            sType = "NPC";
        } else if (type == 3) {
            amount = ObjectDef.totalObjects;
            sType = "Object";
        } else {
            type = 1;
            amount = ItemDef.totalItems;
            sType = "Item";
        }
        if (type != 1) {
            for (int line = 0; line < 100; line++) {
                game.pushMessage("", 0, "");
            }
        }
        if (name == null || name.length() == 0) {
            return;
        }

        String search = name;
        String[] parts = new String[100];
        int found = 0;
        do {
            int regex = search.indexOf(' ');
            if (regex == -1) {
                break;
            }
            String part = search.substring(0, regex).trim();
            if (part.length() > 0) {
                parts[found++] = part.toLowerCase();
            }
            search = search.substring(regex + 1);
        } while (true);
        search = search.trim();
        if (search.length() > 0) {
            parts[found++] = search.toLowerCase();
        }
        label0:
        for (int definition = 0; definition < amount; definition++) {
            String result = "";
            if (type == 1) {
                ItemDef item = ItemDef.lookup(definition);
                if (item.certTemplateID != -1 || item.name == null) {
                    continue;
                }
                result = item.name + "@bla@ - " + new String(item.description, StandardCharsets.UTF_8);
            } else if (type == 2) {
                EntityDef npc = EntityDef.forID(definition);
                if (npc.name == null) {
                    continue;
                }
                result = npc.name;
            } else if (type == 3) {
                ObjectDef object = ObjectDef.forID(definition);
                if (object.name == null) {
                    continue;
                }
                result = object.name;
            }
            for (int index = 0; index < found; index++) {
                if (!result.toLowerCase().contains(parts[index])) {
                    continue label0;
                }
            }
            if (type != 1) {
                game.pushMessage("@whi@[" + definition + "] @blu@" + result + "", 0, "");
            }
            definitionResults[definitionResultsTotal] = result;
            definitionResultIDs[definitionResultsTotal] = definition;
            definitionResultsTotal++;
            if (definitionResultsTotal >= definitionResults.length) {
                break;
            }
        }

        if (type == 1) {
            game.needDrawTabArea = true;
            int interfaceID = 5382;
            RSInterface childWidget = RSInterface.interfaceCache[interfaceID];
            game.openInterface(5292);
            RSInterface.interfaceCache[5383].disabledText = "Search results for @yel@" + name;

            int itemCount = 0;
            for (int ID : definitionResultIDs) {
                if (ID > 0 && itemCount < childWidget.inv.length) {
                    childWidget.inv[itemCount] = ID + 1;
                    childWidget.invStackSizes[itemCount++] = 1;
                }
            }
            while (itemCount < childWidget.inv.length) {
                childWidget.inv[itemCount] = 0;
                childWidget.invStackSizes[itemCount++] = 0;
            }
        } else {
            game.pushMessage("@blu@" + sType + " @bla@search results for @blu@" + name + "@bla@ displayed above (@blu@" + definitionResultsTotal + "@bla@ results).", 0, "");
        }
    }
}
