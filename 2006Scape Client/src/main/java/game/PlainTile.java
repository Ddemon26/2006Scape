// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class PlainTile {

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

        final int southWestColor;
        final int southEastColor;
        final int northEastColor;
        final int northWestColor;
        final int textureId;
        boolean flatShade;
        final int orientation;
}
