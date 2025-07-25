package core.network;

import util.collections.NodeSub;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class OnDemandData extends NodeSub {

	public OnDemandData() {
		incomplete = true;
	}

       public int type;
       public byte[] data;
       public int id;
       public boolean incomplete;
       int cycleCount;
}
