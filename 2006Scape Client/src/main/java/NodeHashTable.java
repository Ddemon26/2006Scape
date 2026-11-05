// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

final class NodeHashTable {

	public NodeHashTable() {
		int initialBucketCount = 1024;// was parameter
		bucketCount = initialBucketCount;
		buckets = new Node[initialBucketCount];
		for (int bucketIndex = 0; bucketIndex < initialBucketCount; bucketIndex++) {
			Node bucketSentinel = buckets[bucketIndex] = new Node();
			bucketSentinel.prev = bucketSentinel;
			bucketSentinel.next = bucketSentinel;
		}

	}

	public Node findNodeByID(long key) {
		Node bucketSentinel = buckets[(int) (key & bucketCount - 1)];
		for (Node candidate = bucketSentinel.prev; candidate != bucketSentinel; candidate = candidate.prev) {
			if (candidate.id == key) {
				return candidate;
			}
		}

		return null;
	}

	public void removeFromCache(Node node, long key) {
		try {
			if (node.next != null) {
				node.unlink();
			}
			Node bucketSentinel = buckets[(int) (key & bucketCount - 1)];
			node.next = bucketSentinel.next;
			node.prev = bucketSentinel;
			node.next.prev = node;
			node.prev.next = node;
			node.id = key;
			return;
		} catch (RuntimeException tableFailure) {
			Signlink.reporterror("91499, " + node + ", " + key + ", " + (byte) 7 + ", " + tableFailure.toString());
		}
		throw new RuntimeException();
	}

	private final int bucketCount;
	private final Node[] buckets;
}
