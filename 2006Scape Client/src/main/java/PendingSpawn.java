// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class PendingSpawn extends Node {

        PendingSpawn() {
                delay = -1;
        }

        /** Identifier for the object to spawn. */
        public int id;

        /** Orientation for the spawned object. */
        public int orientation;

        /** Type of the spawned object. */
        public int type;

        /** Remaining delay before this spawn is processed. */
        public int delay;

        /** Plane on which the object should spawn. */
        public int plane;

        /** Spawn category: wall, decoration, etc. */
        public int category;

        /** X-coordinate in tile units. */
        public int x;

        /** Y-coordinate in tile units. */
        public int y;

        /** Existing object id located at this position. */
        public int oldId;

        /** Existing object type. */
        public int oldType;

        /** Existing object orientation. */
        public int oldOrientation;

        /** Countdown until the object is spawned. */
        public int spawnDelay;
}
