// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class Item extends Animable {

	@Override
	public final Model getRotatedModel() {
               ItemDef itemDef = ItemDef.lookup(ID);
               return itemDef.getModel(amount);
	}

	public Item() {
	}

	public int ID;
	public int x;
	public int y;
	public int amount;
}
