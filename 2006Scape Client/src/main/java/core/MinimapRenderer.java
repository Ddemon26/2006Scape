package core;

import game.ObjectDef;
import render.Texture;

/** Handles minimap generation extracted from {@link Game}. */
final class MinimapRenderer {
    // ===================== Magic Numbers -> Named Constants =====================
    private static final int TILE_COUNT_WITH_BORDER = 104;           // world size per axis incl. border
    private static final int INNER_TILE_START = 1;                    // first drawable/iterable tile index
    private static final int INNER_TILE_END_EXCLUSIVE = 103;          // last + 1

    private static final int MINIMAP_PIXEL_OFFSET = 24628;            // starting pixel offset inside the sprite buffer
    private static final int BYTES_PER_PIXEL = 4;                     // ARGB stride
    private static final int PIXELS_PER_ROW = 512;                    // sprite width in pixels
    private static final int ROW_STRIDE = PIXELS_PER_ROW * BYTES_PER_PIXEL;

    // Tile flag masks
    private static final int TILE_FLAG_BLOCKED = 0x18;                // 24
    private static final int TILE_FLAG_BRIDGE = 0x8;                  // 8 (used for tiles one plane above)

    // Random shading constants for minimap locs
    private static final int SHADE_BASE = 238;
    private static final int SHADE_VARIATION = 20;                    // +/-10
    private static final int SHADE_OFFSET = 10;

    // Object / icon constants
    private static final int UID_SHIFT = 14;                          // >> 14 to get object id from uid
    private static final int UID_MASK = 0x7FFF;                       // 32767

    // Icons we DO NOT random-walk (hard coded list from cache)
    private static final int ICON_ALTAR = 22;
    private static final int ICON_BANK = 29;
    private static final int ICON_WATER_SOURCE = 34;
    private static final int ICON_ANVIL = 36;
    private static final int ICON_DUNGEON = 46;
    private static final int ICON_LADDER_UP = 47;
    private static final int ICON_LADDER_DOWN = 48;

    private static final int RANDOM_WALK_ATTEMPTS = 10;

    // Collision clipping flags (from Region / CollisionMap bitmasks)
    private static final int CLIP_WEST  = 0x1280108;  // blocked to west
    private static final int CLIP_EAST  = 0x1280180;  // blocked to east
    private static final int CLIP_NORTH = 0x1280102;  // blocked to north
    private static final int CLIP_SOUTH = 0x1280120;  // blocked to south

    private final Game game;

    MinimapRenderer(Game game) {
        this.game = game;
    }

    /**
     * Generates the minimap for the given plane.
     * NOTE: Only names/constants changed. Logic and control flow remain identical.
     */
    void generateMinimap(int plane) {
        int[] minimapPixels = game.minimapImage.pixels;
        for (int idx = 0; idx < minimapPixels.length; idx++) {
            minimapPixels[idx] = 0;
        }

        // Draw floor tiles
        for (int y = INNER_TILE_START; y < INNER_TILE_END_EXCLUSIVE; y++) {
            int pixelPtr = MINIMAP_PIXEL_OFFSET + (INNER_TILE_END_EXCLUSIVE - y) * ROW_STRIDE;
            for (int x = INNER_TILE_START; x < INNER_TILE_END_EXCLUSIVE; x++) {
                if ((game.tileFlags[plane][x][y] & TILE_FLAG_BLOCKED) == 0) {
                    game.worldController.renderMinimapTile(minimapPixels, pixelPtr, plane, x, y);
                }
                if (plane < 3 && (game.tileFlags[plane + 1][x][y] & TILE_FLAG_BRIDGE) != 0) {
                    game.worldController.renderMinimapTile(minimapPixels, pixelPtr, plane + 1, x, y);
                }
                pixelPtr += BYTES_PER_PIXEL;
            }
        }

        // Random shades
        int shadeRGB = (randomShade() << 16) + (randomShade() << 8) + randomShade();
        int shadeRComponent = randomShade() << 16;

        game.minimapImage.initializeDrawingArea();
        for (int y = INNER_TILE_START; y < INNER_TILE_END_EXCLUSIVE; y++) {
            for (int x = INNER_TILE_START; x < INNER_TILE_END_EXCLUSIVE; x++) {
                if ((game.tileFlags[plane][x][y] & TILE_FLAG_BLOCKED) == 0) {
                    game.drawMinimapLoc(y, shadeRGB, x, shadeRComponent, plane);
                }
                if (plane < 3 && (game.tileFlags[plane + 1][x][y] & TILE_FLAG_BRIDGE) != 0) {
                    game.drawMinimapLoc(y, shadeRGB, x, shadeRComponent, plane + 1);
                }
            }
        }

        if (game.tabAreaBuffer != null) {
            game.tabAreaBuffer.initDrawingArea();
            Texture.lineOffsets = game.chatBoxAreaOffsets;
        }

        game.minimapIconCount = 0;

        for (int x = 0; x < TILE_COUNT_WITH_BORDER; x++) {
            for (int y = 0; y < TILE_COUNT_WITH_BORDER; y++) {
                int decorationUid = game.worldController.getTileDecorationUid(game.plane, x, y);
                if (decorationUid != 0) {
                    int objectId = (decorationUid >> UID_SHIFT) & UID_MASK;
                    int mapIconId = ObjectDef.forID(objectId).mapIconId;
                    if (mapIconId >= 0) {
                        int iconX = x;
                        int iconY = y;

                        if (!isNonRandomWalkIcon(mapIconId)) {
                            final int maxX = TILE_COUNT_WITH_BORDER;
                            final int maxY = TILE_COUNT_WITH_BORDER;
                            int[][] clipping = game.collisionMaps[game.plane].clippingFlags;
                            for (int attempt = 0; attempt < RANDOM_WALK_ATTEMPTS; attempt++) {
                                int dir = (int) (Math.random() * 4D);
                                if (dir == 0 && iconX > 0 && iconX > x - 3 && (clipping[iconX - 1][iconY] & CLIP_WEST) == 0) {
                                    iconX--;
                                }
                                if (dir == 1 && iconX < maxX - 1 && iconX < x + 3 && (clipping[iconX + 1][iconY] & CLIP_EAST) == 0) {
                                    iconX++;
                                }
                                if (dir == 2 && iconY > 0 && iconY > y - 3 && (clipping[iconX][iconY - 1] & CLIP_NORTH) == 0) {
                                    iconY--;
                                }
                                if (dir == 3 && iconY < maxY - 1 && iconY < y + 3 && (clipping[iconX][iconY + 1] & CLIP_SOUTH) == 0) {
                                    iconY++;
                                }
                            }
                        }

                        game.minimapIconSprites[game.minimapIconCount] = game.mapFunctions[mapIconId];
                        game.minimapIconX[game.minimapIconCount] = iconX;
                        game.minimapIconY[game.minimapIconCount] = iconY;
                        game.minimapIconCount++;
                    }
                }
            }
        }
    }

    // ============================= Helpers =============================

    private static int randomShade() {
        return SHADE_BASE + (int) (Math.random() * (double) SHADE_VARIATION) - SHADE_OFFSET;
    }

    private static boolean isNonRandomWalkIcon(int iconId) {
        return iconId == ICON_ALTAR || iconId == ICON_BANK || iconId == ICON_WATER_SOURCE || iconId == ICON_ANVIL
                || iconId == ICON_DUNGEON || iconId == ICON_LADDER_UP || iconId == ICON_LADDER_DOWN;
    }
}