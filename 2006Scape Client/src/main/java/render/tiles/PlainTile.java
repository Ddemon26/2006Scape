package render.tiles;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class PlainTile {

        public PlainTile(int southWestColor, int southEastColor, int northEastColor,
                          int northWestColor, int textureId, int orientation,
                          boolean flatShade) {
                this.flatShade = true;
                this.southWestColor = southWestColor;
                this.southEastColor = southEastColor;
                this.northEastColor = northEastColor;
                this.northWestColor = northWestColor;
                this.textureId = textureId;
                this.orientation = orientation;
                this.flatShade = flatShade;
        }

        public final int southWestColor;
        public final int southEastColor;
        public final int northEastColor;
        public final int northWestColor;
        public final int textureId;
        public boolean flatShade;
        public final int orientation;
}
