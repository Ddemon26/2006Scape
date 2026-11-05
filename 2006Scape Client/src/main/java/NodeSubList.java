// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class NodeSubList {

	public NodeSubList() {
		sentinel = new NodeSub();
		sentinel.prevNodeSub = sentinel;
		sentinel.nextNodeSub = sentinel;
	}

	public void insertHead(NodeSub nodeSub) {
		if (nodeSub.nextNodeSub != null) {
			nodeSub.unlinkSub();
		}
		nodeSub.nextNodeSub = sentinel.nextNodeSub;
		nodeSub.prevNodeSub = sentinel;
		nodeSub.nextNodeSub.prevNodeSub = nodeSub;
		nodeSub.prevNodeSub.nextNodeSub = nodeSub;
	}

	public NodeSub popTail() {
		NodeSub nodeSub = sentinel.prevNodeSub;
		if (nodeSub == sentinel) {
			return null;
		} else {
			nodeSub.unlinkSub();
			return nodeSub;
		}
	}

	public NodeSub firstFromTail() {
		NodeSub nodeSub = sentinel.prevNodeSub;
		if (nodeSub == sentinel) {
			traversalNext = null;
			return null;
		} else {
			traversalNext = nodeSub.prevNodeSub;
			return nodeSub;
		}
	}

	public NodeSub nextTowardHead() {
		NodeSub nodeSub = traversalNext;
		if (nodeSub == sentinel) {
			traversalNext = null;
			return null;
		} else {
			traversalNext = nodeSub.prevNodeSub;
			return nodeSub;
		}
	}

	public int getNodeCount() {
		int nodeCount = 0;
		for (NodeSub nodeSub = sentinel.prevNodeSub; nodeSub != sentinel; nodeSub = nodeSub.prevNodeSub) {
			nodeCount++;
		}

		return nodeCount;
	}

	private final NodeSub sentinel;
	private NodeSub traversalNext;
}
