package com.rs2.domain;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class NodeHashTable {

        public NodeHashTable() {
                int capacity = 1024; // was parameter
                size = capacity;
                cache = new Node[capacity];
                for (int index = 0; index < capacity; index++) {
                        Node node = cache[index] = new Node();
                        node.prev = node;
                        node.next = node;
                }

        }

        public Node findNodeById(long id) {
                Node bucketHead = cache[(int) (id & size - 1)];
                for (Node node = bucketHead.prev; node != bucketHead; node = node.prev) {
                        if (node.id == id) {
                                return node;
                        }
                }

                return null;
        }

        public void insertNode(Node node, long id) {
                try {
                        if (node.next != null) {
                                node.unlink();
                        }
                        Node bucketHead = cache[(int) (id & size - 1)];
                        node.next = bucketHead.next;
                        node.prev = bucketHead;
                        node.next.prev = node;
                        node.prev.next = node;
                        node.id = id;
                        return;
                } catch (RuntimeException runtimeexception) {
                        Signlink.reporterror("91499, " + node + ", " + id + ", " + (byte) 7 + ", " + runtimeexception.toString());
                }
                throw new RuntimeException();
        }

	private final int size;
	private final Node[] cache;
}
