// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class MRUCache {

        public MRUCache(int i) {
                sentinel = new NodeSub();
                lruList = new NodeSubList();
                capacity = i;
                remaining = i;
                lookup = new NodeHashTable();
        }

        public NodeSub get(long key) {
                NodeSub nodeSub = (NodeSub) lookup.findNodeById(key);
                if (nodeSub != null) {
                        lruList.insertHead(nodeSub);
                }
                return nodeSub;
        }

        public void put(NodeSub node, long key) {
                try {
                        if (remaining == 0) {
                                NodeSub nodeSub_1 = lruList.popTail();
                                nodeSub_1.unlink();
                                nodeSub_1.unlinkSub();
                                if (nodeSub_1 == sentinel) {
                                        NodeSub nodeSub_2 = lruList.popTail();
                                        nodeSub_2.unlink();
                                        nodeSub_2.unlinkSub();
                                }
                        } else {
                                remaining--;
                        }
                        lookup.insertNode(node, key);
                        lruList.insertHead(node);
                        return;
                } catch (RuntimeException runtimeexception) {
                        Signlink.reporterror("47547, " + node + ", " + key + ", " + (byte) 2 + ", " + runtimeexception.toString());
                }
                throw new RuntimeException();
        }

	public void unlinkAll() {
		do {
                        NodeSub nodeSub = lruList.popTail();
                        if (nodeSub != null) {
                                nodeSub.unlink();
                                nodeSub.unlinkSub();
                        } else {
                                remaining = capacity;
                                return;
                        }
                } while (true);
        }

        private final NodeSub sentinel;
        private final int capacity;
        private int remaining;
        private final NodeHashTable lookup;
        private final NodeSubList lruList;
}
