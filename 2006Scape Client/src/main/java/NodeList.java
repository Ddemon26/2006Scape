// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class NodeList {

	public NodeList() {
		sentinel = new Node();
		sentinel.prev = sentinel;
		sentinel.next = sentinel;
	}

	public void insertHead(Node node) {
		if (node.next != null) {
			node.unlink();
		}
		node.next = sentinel.next;
		node.prev = sentinel;
		node.next.prev = node;
		node.prev.next = node;
	}

	public void insertTail(Node node) {
		if (node.next != null) {
			node.unlink();
		}
		node.next = sentinel;
		node.prev = sentinel.prev;
		node.next.prev = node;
		node.prev.next = node;
	}

	public Node popTail() {
		Node node = sentinel.prev;
		if (node == sentinel) {
			return null;
		} else {
			node.unlink();
			return node;
		}
	}

	public Node firstFromTail() {
		Node node = sentinel.prev;
		if (node == sentinel) {
			traversalNext = null;
			return null;
		} else {
			traversalNext = node.prev;
			return node;
		}
	}

	public Node firstFromHead() {
		Node node = sentinel.next;
		if (node == sentinel) {
			traversalNext = null;
			return null;
		} else {
			traversalNext = node.next;
			return node;
		}
	}

	public Node nextTowardHead() {
		Node node = traversalNext;
		if (node == sentinel) {
			traversalNext = null;
			return null;
		} else {
			traversalNext = node.prev;
			return node;
		}
	}

	public Node nextTowardTail() {
		Node node = traversalNext;
		if (node == sentinel) {
			traversalNext = null;
			return null;
		}
		traversalNext = node.next;
		return node;
	}

	public void removeAll() {
		if (sentinel.prev == sentinel) {
			return;
		}
		do {
			Node node = sentinel.prev;
			if (node == sentinel) {
				return;
			}
			node.unlink();
		} while (true);
	}

	private final Node sentinel;
	private Node traversalNext;
}
