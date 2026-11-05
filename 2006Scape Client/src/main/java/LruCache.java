// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

public final class LruCache {

	public LruCache(int maxEntries) {
		evictionSentinel = new NodeSub();
		recencyList = new NodeSubList();
		capacity = maxEntries;
		remainingSlots = maxEntries;
		table = new NodeHashTable();
	}

	public NodeSub insertFromCache(long key) {
		NodeSub cachedNode = (NodeSub) table.findNodeByID(key);
		if (cachedNode != null) {
			recencyList.insertHead(cachedNode);
		}
		return cachedNode;
	}

	public void removeFromCache(NodeSub node, long key) {
		try {
			if (remainingSlots == 0) {
				NodeSub evictedNode = recencyList.popTail();
				evictedNode.unlink();
				evictedNode.unlinkSub();
				if (evictedNode == evictionSentinel) {
					NodeSub nextEvictedNode = recencyList.popTail();
					nextEvictedNode.unlink();
					nextEvictedNode.unlinkSub();
				}
			} else {
				remainingSlots--;
			}
			table.removeFromCache(node, key);
			recencyList.insertHead(node);
			return;
		} catch (RuntimeException cacheFailure) {
			Signlink.reporterror("47547, " + node + ", " + key + ", " + (byte) 2 + ", " + cacheFailure.toString());
		}
		throw new RuntimeException();
	}

	public void clear() {
		do {
			NodeSub node = recencyList.popTail();
			if (node != null) {
				node.unlink();
				node.unlinkSub();
			} else {
				remainingSlots = capacity;
				return;
			}
		} while (true);
	}

	private final NodeSub evictionSentinel;
	private final int capacity;
	private int remainingSlots;
	private final NodeHashTable table;
	private final NodeSubList recencyList;
}
