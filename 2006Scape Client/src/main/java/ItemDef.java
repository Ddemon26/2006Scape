// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class ItemDef {

	public static void nullLoader() {
		mruNodes2 = null;
		mruNodes1 = null;
		streamIndices = null;
		cache = null;
		stream = null;
	}

	public boolean isHeadModelReady(int gender) {
		int modelID1 = maleHeadModelID1;
		int modelID2 = maleHeadModelID2;
		if (gender == 1) {
			modelID1 = femaleHeadModelID1;
			modelID2 = femaleHeadModelID2;
		}
		if (modelID1 == -1) {
			return true;
		}
		boolean ready = true;
		if (!Model.isModelLoaded(modelID1)) {
			ready = false;
		}
		if (modelID2 != -1 && !Model.isModelLoaded(modelID2)) {
			ready = false;
		}
		return ready;
	}

	public static void unpackConfig(StreamLoader streamLoader) {
		stream = new Stream(streamLoader.getDataForName("obj.dat"));
		Stream indexStream = new Stream(streamLoader.getDataForName("obj.idx"));
		totalItems = indexStream.readUnsignedShortBigEndian();
		streamIndices = new int[totalItems];
		int recordOffset = 2;
		for (int itemID = 0; itemID < totalItems; itemID++) {
			streamIndices[itemID] = recordOffset;
			recordOffset += indexStream.readUnsignedShortBigEndian();
		}

		cache = new ItemDef[10];
		for (int slot = 0; slot < 10; slot++) {
			cache[slot] = new ItemDef();
		}

	}

	public Model getHeadModel(int gender) {
		int modelID1 = maleHeadModelID1;
		int modelID2 = maleHeadModelID2;
		if (gender == 1) {
			modelID1 = femaleHeadModelID1;
			modelID2 = femaleHeadModelID2;
		}
		if (modelID1 == -1) {
			return null;
		}
		Model model = Model.getModel(modelID1);
		if (modelID2 != -1) {
			Model model2 = Model.getModel(modelID2);
			Model parts[] = {model, model2};
			model = new Model(2, parts);
		}
		if (modifiedModelColors != null) {
			for (int colorIndex = 0; colorIndex < modifiedModelColors.length; colorIndex++) {
				model.recolor(modifiedModelColors[colorIndex], originalModelColors[colorIndex]);
			}

		}
		return model;
	}

	public boolean isWornModelReady(int gender) {
		int modelID1 = maleWornModelID1;
		int modelID2 = maleWornModelID2;
		int modelID3 = maleWornModelID3;
		if (gender == 1) {
			modelID1 = femaleWornModelID1;
			modelID2 = femaleWornModelID2;
			modelID3 = femaleWornModelID3;
		}
		if (modelID1 == -1) {
			return true;
		}
		boolean ready = true;
		if (!Model.isModelLoaded(modelID1)) {
			ready = false;
		}
		if (modelID2 != -1 && !Model.isModelLoaded(modelID2)) {
			ready = false;
		}
		if (modelID3 != -1 && !Model.isModelLoaded(modelID3)) {
			ready = false;
		}
		return ready;
	}

	public Model getWornModel(int gender) {
		int modelID1 = maleWornModelID1;
		int modelID2 = maleWornModelID2;
		int modelID3 = maleWornModelID3;
		if (gender == 1) {
			modelID1 = femaleWornModelID1;
			modelID2 = femaleWornModelID2;
			modelID3 = femaleWornModelID3;
		}
		if (modelID1 == -1) {
			return null;
		}
		Model model = Model.getModel(modelID1);
		if (modelID2 != -1) {
			if (modelID3 != -1) {
				Model model2 = Model.getModel(modelID2);
				Model model3 = Model.getModel(modelID3);
				Model parts[] = {model, model2, model3};
				model = new Model(3, parts);
			} else {
				Model model2 = Model.getModel(modelID2);
				Model parts[] = {model, model2};
				model = new Model(2, parts);
			}
		}
		if (gender == 0 && maleWornOffsetY != 0) {
			model.translate(0, maleWornOffsetY, 0);
		}
		if (gender == 1 && femaleWornOffsetY != 0) {
			model.translate(0, femaleWornOffsetY, 0);
		}
		if (modifiedModelColors != null) {
			for (int colorIndex = 0; colorIndex < modifiedModelColors.length; colorIndex++) {
				model.recolor(modifiedModelColors[colorIndex], originalModelColors[colorIndex]);
			}

		}
		return model;
	}

	private void setDefaults() {
		modelID = 0;
		name = null;
		description = null;
		modifiedModelColors = null;
		originalModelColors = null;
		modelZoom = 2000;
		modelRotation1 = 0;
		modelRotation2 = 0;
		modelRotation3 = 0;
		modelOffset1 = 0;
		modelOffset2 = 0;
		stackable = false;
		value = 1;
		membersObject = false;
		groundActions = null;
		actions = null;
		maleWornModelID1 = -1;
		maleWornModelID2 = -1;
		maleWornOffsetY = 0;
		femaleWornModelID1 = -1;
		femaleWornModelID2 = -1;
		femaleWornOffsetY = 0;
		maleWornModelID3 = -1;
		femaleWornModelID3 = -1;
		maleHeadModelID1 = -1;
		maleHeadModelID2 = -1;
		femaleHeadModelID1 = -1;
		femaleHeadModelID2 = -1;
		stackIDs = null;
		stackAmounts = null;
		certID = -1;
		certTemplateID = -1;
		modelScaleX = 128;
		modelScaleY = 128;
		modelScaleZ = 128;
		ambientLightOffset = 0;
		lightDivisorOffset = 0;
		team = 0;
	}

	public static ItemDef forID(int itemID) {
		for (int slot = 0; slot < 10; slot++) {
			if (cache[slot].id == itemID) {
				return cache[slot];
			}
		}

		cacheIndex = (cacheIndex + 1) % 10;
		ItemDef itemDef = cache[cacheIndex];
		try {
			stream.currentOffset = streamIndices[itemID];
		} catch (Exception e) {
			System.out.println("Itemdef issue? Apparently this ID is out of bounds: " + e);
		}
		itemDef.id = itemID;
		itemDef.setDefaults();
		itemDef.readValues(stream);
		if (itemDef.certTemplateID != -1) {
			itemDef.toNote();
		}
		if (itemID == 6543) {
			itemDef.name = "Magical Lamp";
			itemDef.description = "I wonder what will happen when I rub this...".getBytes();
		}
		if (!isMembers && itemDef.membersObject) {
			itemDef.name = "Members Object";
			itemDef.description = "Login to a members' server to use this object.".getBytes();
			itemDef.groundActions = null;
			itemDef.actions = null;
			itemDef.team = 0;
		}
		switch (itemID) {
		case 9747:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[5];
			itemDef.originalModelColors = new int[5];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 923;
			itemDef.originalModelColors[1] = 920;
			itemDef.originalModelColors[2] = 920;
			itemDef.originalModelColors[3] = 923;
			itemDef.modelID = 19047;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.modelRotation3 = 0;
			itemDef.maleWornModelID1 = 18920;
			itemDef.femaleWornModelID1 = 18969;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Attack cape";
			itemDef.description = "The cape worn by masters of Attack.".getBytes();
			break;
			
		case 9748:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 9152;
			itemDef.originalModelColors[1] = 7104;
			itemDef.originalModelColors[2] = 7104;
			itemDef.originalModelColors[3] = 796;
			itemDef.modelID = 19047;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18920;
			itemDef.femaleWornModelID1 = 18969;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Attack cape(t)";
			itemDef.description = "The cape worn by masters of Attack.".getBytes();
			break;
			
		case 9749:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.originalModelColors[0] = 912;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.originalModelColors[1] = 912;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[2] = 912;
			itemDef.modelID = 19058;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Attack hood";
			itemDef.description = "Attack skillcape hood.".getBytes();
			break;
			
		case 9750:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[6];
			itemDef.originalModelColors = new int[6];
			itemDef.modifiedModelColors = new int[6];
			itemDef.originalModelColors = new int[6];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 27549;
			itemDef.originalModelColors[1] = 27544;
			itemDef.originalModelColors[2] = 27544;
			itemDef.originalModelColors[3] = 27549;
			itemDef.modelID = 19067;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.modelRotation3 = 0;
			itemDef.maleWornModelID1 = 18954;
			itemDef.femaleWornModelID1 = 18989;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Strength cape";
			itemDef.description = "The cape worn by only the strongest people.".getBytes();
			break;
			
		case 9751:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 940;
			itemDef.originalModelColors[1] = 930;
			itemDef.originalModelColors[2] = 930;
			itemDef.originalModelColors[3] = 940;
			itemDef.modelID = 19067;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18954;
			itemDef.femaleWornModelID1 = 18989;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Strength cape(t)";
			itemDef.description = "The cape worn by only the strongest people.".getBytes();
			break;
			
		case 9752:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 27544;
			itemDef.originalModelColors[1] = 27544;
			itemDef.originalModelColors[2] = 27544;
			itemDef.modelID = 19058;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Strength hood";
			itemDef.description = "Strength skillcape hood.".getBytes();
			break;
			
		case 9753:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 41436;
			itemDef.originalModelColors[1] = 41416;
			itemDef.originalModelColors[2] = 41416;
			itemDef.originalModelColors[3] = 41436;
			itemDef.modelID = 19051;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18928;
			itemDef.femaleWornModelID1 = 18973;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			/*itemDef.anInt199 = -1;*/
			itemDef.name = "Defence cape";
			itemDef.description = "The cape worn by masters of the art of Defence.".getBytes();
			break;
			
		case 9754:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.originalModelColors[0] = 107;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.originalModelColors[1] = 107;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.originalModelColors[2] = 107;
			itemDef.modelID = 19051;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18928;
			itemDef.femaleWornModelID1 = 18973;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Defence cape(t)";
			itemDef.description = "The cape worn by masters of the art of Defence.".getBytes();
			break;
			
		case 9755:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.originalModelColors[0] = 41416;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.originalModelColors[1] = 41416;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[2] = 41416;
			itemDef.modelID = 19058;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Defence hood";
			itemDef.description = "Defence skillcape hood.".getBytes();
			break;
			
		case 9756:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 13214;
			itemDef.originalModelColors[1] = 13214;
			itemDef.originalModelColors[2] = 13214;
			itemDef.originalModelColors[3] = 13214;
			itemDef.modelID = 19063;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18948;
			itemDef.femaleWornModelID1 = 18985;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Ranging cape";
			itemDef.description = "The cape worn by master archers.".getBytes();
			break;
			
		case 9757:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 3755;
			itemDef.originalModelColors[1] = 4003;
			itemDef.originalModelColors[2] = 4003;
			itemDef.originalModelColors[3] = 3755;
			itemDef.modelID = 19063;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18948;
			itemDef.femaleWornModelID1 = 18985;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Ranging cape(t)";
			itemDef.description = "The cape worn by master archers.".getBytes();
			break;
			
		case 9758:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 13214;
			itemDef.originalModelColors[1] = 13214;
			itemDef.originalModelColors[2] = 13214;
			itemDef.modelID = 19058;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Range hood";
			itemDef.description = "Range skillcape hood.".getBytes();
			break;
			
		case 9759:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 107;
			itemDef.originalModelColors[1] = 107;
			itemDef.originalModelColors[2] = 107;
			itemDef.originalModelColors[3] = 107;
			itemDef.modelID = 19061;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18945;
			itemDef.femaleWornModelID1 = 18983;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Prayer cape";
			itemDef.description = "The cape worn by the most pious of heroes.".getBytes();
			break;
			
		case 9760:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 9152;
			itemDef.originalModelColors[1] = 9172;
			itemDef.originalModelColors[2] = 9172;
			itemDef.originalModelColors[3] = 9152;
			itemDef.modelID = 19061;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18945;
			itemDef.femaleWornModelID1 = 18983;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Prayer cape(t)";
			itemDef.description = "The cape worn by the most pious of heroes.".getBytes();
			break;
			
		case 9761:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 107;
			itemDef.originalModelColors[1] = 107;
			itemDef.originalModelColors[2] = 107;
			itemDef.modelID = 19058;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Prayer hood";
			itemDef.description = "Prayer skillcape hood.".getBytes();
			break;
			
		case 9762:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 6348;
			itemDef.originalModelColors[1] = 6340;
			itemDef.originalModelColors[2] = 6331;
			itemDef.originalModelColors[3] = 6331;
			itemDef.modelID = 19059;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18915;
			itemDef.femaleWornModelID1 = 18981;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Magic cape";
			itemDef.description = "The cape worn by the most powerful mages.".getBytes();
			break;
			
		case 9763:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 43569;
			itemDef.originalModelColors[1] = 43685;
			itemDef.originalModelColors[2] = 43685;
			itemDef.originalModelColors[3] = 43569;
			itemDef.modelID = 19059;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18915;
			itemDef.femaleWornModelID1 = 18981;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Magic cape(t)";
			itemDef.description = "The cape worn by the most powerful mages.".getBytes();
			break;
			
		case 9764:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 6348;
			itemDef.originalModelColors[1] = 6340;
			itemDef.originalModelColors[2] = 6331;
			itemDef.modelID = 19058;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Magic hood";
			itemDef.description = "Magic skillcape hood.".getBytes();
			break;
			
		case 9765:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 90;
			itemDef.originalModelColors[1] = 80;
			itemDef.originalModelColors[2] = 80;
			itemDef.originalModelColors[3] = 90;
			itemDef.modelID = 19053;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18950;
			itemDef.femaleWornModelID1 = 18986;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "RuneCrafting cape";
			itemDef.description = "The cape worn by master runecrafters.".getBytes();
			break;
			
		case 9766:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 9152;
			itemDef.originalModelColors[1] = 7080;
			itemDef.originalModelColors[2] = 7080;
			itemDef.originalModelColors[3] = 796;
			itemDef.modelID = 19053;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18950;
			itemDef.femaleWornModelID1 = 18986;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "RuneCrafting cape(t)";
			itemDef.description = "The cape worn by master runecrafters.".getBytes();
			break;
			
		case 9767:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 99;
			itemDef.originalModelColors[1] = 99;
			itemDef.originalModelColors[2] = 99;
			itemDef.modelID = 19058;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "RuneCrafting hood";
			itemDef.description = "RuneCrafting skillcape hood.".getBytes();
			break;
			
		case 9768:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 75;
			itemDef.originalModelColors[1] = 75;
			itemDef.originalModelColors[2] = 75;
			itemDef.originalModelColors[3] = 75;
			itemDef.modelID = 19057;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18939;
			itemDef.femaleWornModelID1 = 18980;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Hitpoint cape";
			itemDef.description = "The cape worn by the healthiest adventurers.".getBytes();
			break;
			
		case 9769:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 940;
			itemDef.originalModelColors[1] = 920;
			itemDef.originalModelColors[2] = 920;
			itemDef.originalModelColors[3] = 940;
			itemDef.modelID = 19057;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18939;
			itemDef.femaleWornModelID1 = 18980;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Hitpoint cape(t)";
			itemDef.description = "The cape worn by the healthiest adventurers.".getBytes();
			break;
			
		case 9770:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 90;
			itemDef.originalModelColors[1] = 90;
			itemDef.originalModelColors[2] = 90;
			itemDef.modelID = 19058;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Hitpoint hood";
			itemDef.description = "Hitpoint skillcape hood.".getBytes();
			break;
			
		case 9771:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 43674;
			itemDef.originalModelColors[1] = 43680;
			itemDef.originalModelColors[2] = 43680;
			itemDef.originalModelColors[3] = 43674;
			itemDef.modelID = 19046;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18918;
			itemDef.femaleWornModelID1 = 18968;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Agility cape";
			itemDef.description = "The cape worn by the most agile of heroes.".getBytes();
			break;
			
		case 9772:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 801;
			itemDef.originalModelColors[1] = 677;
			itemDef.originalModelColors[2] = 677;
			itemDef.originalModelColors[3] = 801;
			itemDef.modelID = 19046;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18918;
			itemDef.femaleWornModelID1 = 18968;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Agility cape(t)";
			itemDef.description = "The cape worn by the best of agility trainers.".getBytes();
			break;
			
		case 9773:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 43674;
			itemDef.originalModelColors[1] = 43674;
			itemDef.originalModelColors[2] = 43674;
			itemDef.modelID = 19058;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Agility hood";
			itemDef.description = "Agility skillcape hood.".getBytes();
			break;
			
		case 9774:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 22426;
			itemDef.originalModelColors[1] = 22416;
			itemDef.originalModelColors[2] = 22416;
			itemDef.originalModelColors[3] = 22426;
			itemDef.modelID = 19056;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18938;
			itemDef.femaleWornModelID1 = 18978;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Herblore cape";
			itemDef.description = "The cape worn by master herbalists.".getBytes();
			break;
			
		case 9775:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 9152;
			itemDef.originalModelColors[1] = 7104;
			itemDef.originalModelColors[2] = 7104;
			itemDef.originalModelColors[3] = 796;
			itemDef.modelID = 19056;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18938;
			itemDef.femaleWornModelID1 = 18978;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Herblore cape(t)";
			itemDef.description = "The cape worn by master herbalists.".getBytes();
			break;
			
		case 9776:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 22416;
			itemDef.originalModelColors[1] = 22416;
			itemDef.originalModelColors[2] = 22416;
			itemDef.modelID = 19058;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Herblore hood";
			itemDef.description = "Herblore skillcape hood.".getBytes();
			break;
			
		case 9777:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 58795;
			itemDef.originalModelColors[1] = 58785;
			itemDef.originalModelColors[2] = 58785;
			itemDef.originalModelColors[3] = 58795;
			itemDef.modelID = 19068;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18956;
			itemDef.femaleWornModelID1 = 18956;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Thieving cape";
			itemDef.description = "The cape worn by the best of thieves.".getBytes();
			break;
			
		case 9778:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 15;
			itemDef.originalModelColors[1] = 0;
			itemDef.originalModelColors[2] = 0;
			itemDef.originalModelColors[3] = 15;
			itemDef.modelID = 19068;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18956;
			itemDef.femaleWornModelID1 = 18956;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Thieving cape(t)";
			itemDef.description = "The cape worn by the best of thieves.".getBytes();
			break;
			
		case 9779:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 58785;
			itemDef.originalModelColors[1] = 58785;
			itemDef.originalModelColors[2] = 58785;
			itemDef.modelID = 19058;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Thieving hood";
			itemDef.description = "Thieving skillcape hood.".getBytes();
			break;
			
		case 9780:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 4520;
			itemDef.originalModelColors[1] = 4510;
			itemDef.originalModelColors[2] = 4510;
			itemDef.originalModelColors[3] = 4520;
			itemDef.modelID = 19050;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18925;
			itemDef.femaleWornModelID1 = 18972;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Crafting cape";
			itemDef.description = "The cape worn by master craftworkers.".getBytes();
			break;
			
		case 9781:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 9152;
			itemDef.originalModelColors[1] = 7104;
			itemDef.originalModelColors[2] = 7104;
			itemDef.originalModelColors[3] = 796;
			itemDef.modelID = 19050;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18925;
			itemDef.femaleWornModelID1 = 18972;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Crafting cape(t)";
			itemDef.description = "The cape worn by master craftworkers.".getBytes();
			break;
			
		case 9782:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 4520;
			itemDef.originalModelColors[1] = 4520;
			itemDef.originalModelColors[2] = 4520;
			itemDef.modelID = 19058;
			itemDef.stackable = false;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Crafting hood";
			itemDef.description = "Crafting skillcape hood.".getBytes();
			break;
			
		case 9783:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 33682;
			itemDef.originalModelColors[1] = 33676;
			itemDef.originalModelColors[2] = 33676;
			itemDef.originalModelColors[3] = 33682;
			itemDef.modelID = 19055;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18935;
			itemDef.femaleWornModelID1 = 18977;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Fletching cape";
			itemDef.description = "The cape worn by the best of fletchers.".getBytes();
			break;
			
		case 9784:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 9152;
			itemDef.originalModelColors[1] = 7104;
			itemDef.originalModelColors[2] = 7104;
			itemDef.originalModelColors[3] = 796;
			itemDef.modelID = 19055;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18935;
			itemDef.femaleWornModelID1 = 18977;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Fletching cape(t)";
			itemDef.description = "The cape worn by the best of fletchers.".getBytes();
			break;
			
		case 9785:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 33676;
			itemDef.originalModelColors[1] = 33676;
			itemDef.originalModelColors[2] = 33676;
			itemDef.modelID = 19058;
			itemDef.stackable = false;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Fletching hood";
			itemDef.description = "Fletching skillcape hood.".getBytes();
			break;
			
		case 9786:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 8;
			itemDef.originalModelColors[1] = 8;
			itemDef.originalModelColors[2] = 8;
			itemDef.originalModelColors[3] = 8;
			itemDef.modelID = 19065;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18951;
			itemDef.femaleWornModelID1 = 18987;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Slayer cape";
			itemDef.description = "The cape worn by Slayer masters.".getBytes();
			break;
			
		case 9787:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 920;
			itemDef.originalModelColors[1] = 916;
			itemDef.originalModelColors[2] = 916;
			itemDef.originalModelColors[3] = 920;
			itemDef.modelID = 19065;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18951;
			itemDef.femaleWornModelID1 = 18987;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Slayer cape(t)";
			itemDef.description = "The cape worn by Slayer masters.".getBytes();
			break;
			
		case 9788:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 8;
			itemDef.originalModelColors[1] = 8;
			itemDef.originalModelColors[2] = 8;
			itemDef.modelID = 19058;
			itemDef.stackable = false;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Slayer hood";
			itemDef.description = "Slayer skillcape hood.".getBytes();
			break;
			
		case 9789:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 6327;
			itemDef.originalModelColors[1] = 6336;
			itemDef.originalModelColors[2] = 6336;
			itemDef.originalModelColors[3] = 6327;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.modelID = 19045;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18916;
			itemDef.femaleWornModelID1 = 18970;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Construct. cape";
			itemDef.description = "The cape worn by the best of constructers.".getBytes();
			break;
			
		case 9790:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 6077;
			itemDef.originalModelColors[1] = 6067;
			itemDef.originalModelColors[2] = 6067;
			itemDef.originalModelColors[3] = 6077;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.modelID = 19045;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18916;
			itemDef.femaleWornModelID1 = 18970;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Const. cape(t)";
			itemDef.description = "The cape worn by the best of constructers.".getBytes();
			break;
			
		case 9791:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 6327;
			itemDef.originalModelColors[1] = 6327;
			itemDef.originalModelColors[2] = 6327;
			itemDef.modelID = 19058;
			itemDef.stackable = false;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Const. hood";
			itemDef.description = "Construction skillcape hood.".getBytes();
			break;	

		case 9792:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 10530;
			itemDef.originalModelColors[1] = 10520;
			itemDef.originalModelColors[2] = 10520;
			itemDef.originalModelColors[3] = 10530;
			itemDef.modelID = 19060;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18942;
			itemDef.femaleWornModelID1 = 18982;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Mining cape";
			itemDef.description = "The cape worn by the most skilled miners.".getBytes();
			break;
			
		case 9793:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 38226;
			itemDef.originalModelColors[1] = 38236;
			itemDef.originalModelColors[2] = 38236;
			itemDef.originalModelColors[3] = 38226;
			itemDef.modelID = 19060;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18942;
			itemDef.femaleWornModelID1 = 18982;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Mining cape(t)";
			itemDef.description = "The cape worn by the most skilled miners.".getBytes();
			break;

		case 9794:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 10512;
			itemDef.originalModelColors[1] = 10512;
			itemDef.originalModelColors[2] = 10512;
			itemDef.modelID = 19058;
			itemDef.stackable = false;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Mining hood";
			itemDef.description = "Mining skillcape hood.".getBytes();
			break;			
			
		case 9795:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 10530;
			itemDef.originalModelColors[1] = 10520;
			itemDef.originalModelColors[2] = 10520;
			itemDef.originalModelColors[3] = 10530;
			itemDef.modelID = 19066;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18953;
			itemDef.femaleWornModelID1 = 18988;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Smithing cape";
			itemDef.description = "The cape worn by the best of smithers.".getBytes();
			break;
			
		case 9796:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 9152;
			itemDef.originalModelColors[1] = 7104;
			itemDef.originalModelColors[2] = 7104;
			itemDef.originalModelColors[3] = 796;
			itemDef.modelID = 19066;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18953;
			itemDef.femaleWornModelID1 = 18988;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Smithing cape(t)";
			itemDef.description = "The cape worn by the best of smithers.".getBytes();
			break;
			
		case 9797:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 10512;
			itemDef.originalModelColors[1] = 10512;
			itemDef.originalModelColors[2] = 10512;
			itemDef.modelID = 19058;
			itemDef.stackable = false;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Smithing hood";
			itemDef.description = "Smithing skillcape hood.".getBytes();
			break;
			
		case 9798:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 38256;
			itemDef.originalModelColors[1] = 38226;
			itemDef.originalModelColors[2] = 38226;
			itemDef.originalModelColors[3] = 38256;
			itemDef.modelID = 19054;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18934;
			itemDef.femaleWornModelID1 = 18976;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Fishing cape";
			itemDef.description = "The cape worn by the best fishermen.".getBytes();
			break;
			
		case 9799:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 9152;
			itemDef.originalModelColors[1] = 7104;
			itemDef.originalModelColors[2] = 7104;
			itemDef.originalModelColors[3] = 796;
			itemDef.modelID = 19054;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18934;
			itemDef.femaleWornModelID1 = 18976;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Fishing cape(t)";
			itemDef.description = "The cape worn by the best fishermen.".getBytes();
			break;
			
		case 9800:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 38226;
			itemDef.originalModelColors[1] = 38226;
			itemDef.originalModelColors[2] = 38226;
			itemDef.modelID = 19058;
			itemDef.stackable = false;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Fishing hood";
			itemDef.description = "Fishing skillcape hood.".getBytes();
			break;		

		case 9801:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 51869;
			itemDef.originalModelColors[1] = 51864;
			itemDef.originalModelColors[2] = 51864;
			itemDef.originalModelColors[3] = 51869;
			itemDef.modelID = 19049;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18924;
			itemDef.femaleWornModelID1 = 18971;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Cooking cape";
			itemDef.description = "The cape worn by the world's best chefs.".getBytes();
			break;
			
		case 9802:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 801;
			itemDef.originalModelColors[1] = 677;
			itemDef.originalModelColors[2] = 677;
			itemDef.originalModelColors[3] = 801;
			itemDef.modelID = 19049;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18924;
			itemDef.femaleWornModelID1 = 18971;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Cooking cape(t)";
			itemDef.description = "The cape worn by the world's best chefs.".getBytes();
			break;
			
		case 9803:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 51864;
			itemDef.originalModelColors[1] = 51864;
			itemDef.originalModelColors[2] = 51864;
			itemDef.modelID = 19058;
			itemDef.stackable = false;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Cooking hood";
			itemDef.description = "Cooking skillcape hood.".getBytes();
			break;
			
			
		case 9804:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 4032;
			itemDef.originalModelColors[1] = 4027;
			itemDef.originalModelColors[2] = 4027;
			itemDef.originalModelColors[3] = 4032;
			itemDef.modelID = 19048;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.modelRotation3 = 0;
			itemDef.maleWornModelID1 = 18932;
			itemDef.femaleWornModelID1 = 18975;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Firemaking cape";
			itemDef.description = "The cape worn by the master firelighters.".getBytes();
			break;
			
		case 9805:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 9152;
			itemDef.originalModelColors[1] = 7104;
			itemDef.originalModelColors[2] = 7104;
			itemDef.originalModelColors[3] = 796;
			itemDef.modelID = 19048;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.modelRotation3 = 0;
			itemDef.maleWornModelID1 = 18932;
			itemDef.femaleWornModelID1 = 18975;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Firemaking cape(t)";
			itemDef.description = "The cape worn by the master firelighters.".getBytes();
			break;
			
		case 9806:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 4027;
			itemDef.originalModelColors[1] = 4027;
			itemDef.originalModelColors[2] = 4027;
			itemDef.modelID = 19058;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "FireMaking hood";
			itemDef.description = "FireMaking skillcape hood.".getBytes();
			break;
			
		case 9807:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 5570;
			itemDef.originalModelColors[1] = 5560;
			itemDef.originalModelColors[2] = 5560;
			itemDef.originalModelColors[3] = 5570;
			itemDef.modelID = 19064;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18955;
			itemDef.femaleWornModelID1 = 18991;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Woodcutting cape";
			itemDef.description = "The cape worn by master woodcutters.".getBytes();
			break;
			
		case 9808:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 25113;
			itemDef.originalModelColors[1] = 25113;
			itemDef.originalModelColors[2] = 25113;
			itemDef.originalModelColors[3] = 25113;
			itemDef.modelID = 19064;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18955;
			itemDef.femaleWornModelID1 = 18991;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Woodcutting cape(t)";
			itemDef.description = "The cape worn by master woodcutters.".getBytes();
			break;
			
		case 9809:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 5560;
			itemDef.originalModelColors[1] = 5560;
			itemDef.originalModelColors[2] = 5560;
			itemDef.modelID = 19058;
			itemDef.stackable = false;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Woodcutting hood";
			itemDef.description = "Woodcutting skillcape hood.".getBytes();
			break;
			
		case 9810:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 22426;
			itemDef.originalModelColors[1] = 22416;
			itemDef.originalModelColors[2] = 22416;
			itemDef.originalModelColors[3] = 22426;
			itemDef.modelID = 19052;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18929;
			itemDef.femaleWornModelID1 = 18974;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Farming cape";
			itemDef.description = "The cape worn by the best of farmers.".getBytes();
			break;
			
		case 9811:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 21844;
			itemDef.originalModelColors[1] = 21844;
			itemDef.originalModelColors[2] = 21844;
			itemDef.originalModelColors[3] = 21844;
			itemDef.modelID = 19052;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18929;
			itemDef.femaleWornModelID1 = 18974;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Farming cape(t)";
			itemDef.description = "The cape worn by the best of farmers.".getBytes();
			break;
			
		case 9812:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 22416;
			itemDef.originalModelColors[1] = 22416;
			itemDef.originalModelColors[2] = 22416;
			itemDef.modelID = 19058;
			itemDef.stackable = false;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Farming hood";
			itemDef.description = "Farming skillcape hood.".getBytes();
			break;
			
		case 9813:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[4];
			itemDef.originalModelColors = new int[4];
			itemDef.modifiedModelColors[0] = 57280;
			itemDef.modifiedModelColors[1] = 54503;
			itemDef.modifiedModelColors[2] = 54183;
			itemDef.modifiedModelColors[3] = 11200;
			itemDef.originalModelColors[0] = 107;
			itemDef.originalModelColors[1] = 107;
			itemDef.originalModelColors[2] = 107;
			itemDef.originalModelColors[3] = 107;
			itemDef.modelID = 19062;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.modelRotation3 = 0;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 1;
			itemDef.maleWornModelID1 = 18946;
			itemDef.femaleWornModelID1 = 18984;
			itemDef.maleHeadModelID1 = -1;
			itemDef.femaleHeadModelID1 = -1;
			itemDef.name = "Quest cape";
			itemDef.description = "Its a quest cape".getBytes();
			break;
			
		case 9814:
			itemDef.actions = new String[5];
			itemDef.actions[1] = "Wear";
			itemDef.modifiedModelColors = new int[3];
			itemDef.originalModelColors = new int[3];
			itemDef.modifiedModelColors[0] = 960;
			itemDef.modifiedModelColors[1] = 22464;
			itemDef.modifiedModelColors[2] = 43968;
			itemDef.originalModelColors[0] = 107;
			itemDef.originalModelColors[1] = 107;
			itemDef.originalModelColors[2] = 107;
			itemDef.modelID = 19058;
			itemDef.stackable = false;
			itemDef.modelZoom = 720;
			itemDef.modelRotation1 = 21;
			itemDef.modelRotation2 = 18;
			itemDef.modelOffset1 = 0;
			itemDef.modelOffset2 = 0;
			itemDef.maleWornModelID1 = 18914;
			itemDef.femaleWornModelID1 = 18967;
			itemDef.name = "Quest hood";
			itemDef.description = "Quest skillcape hood.".getBytes();
			break;
			}
		return itemDef;
	}

	private void toNote() {
		ItemDef templateDef = forID(certTemplateID);
		modelID = templateDef.modelID;
		modelZoom = templateDef.modelZoom;
		modelRotation1 = templateDef.modelRotation1;
		modelRotation2 = templateDef.modelRotation2;

		modelRotation3 = templateDef.modelRotation3;
		modelOffset1 = templateDef.modelOffset1;
		modelOffset2 = templateDef.modelOffset2;
		modifiedModelColors = templateDef.modifiedModelColors;
		originalModelColors = templateDef.originalModelColors;
		ItemDef baseDef = forID(certID);
		name = baseDef.name;
		membersObject = baseDef.membersObject;
		value = baseDef.value;
		String article = "a";
		char firstLetter = baseDef.name.charAt(0);
		if (firstLetter == 'A' || firstLetter == 'E' || firstLetter == 'I' || firstLetter == 'O' || firstLetter == 'U') {
			article = "an";
		}
		description = ("Swap this note at any bank for " + article + " " + baseDef.name + ".").getBytes();
		stackable = true;
	}

	public static Sprite getSprite(int itemID, int amount, int outline) {
		if (outline == 0) {
			Sprite cachedSprite = (Sprite) mruNodes1.insertFromCache(itemID);
			if (cachedSprite != null && cachedSprite.trimHeight != amount && cachedSprite.trimHeight != -1) {
				cachedSprite.unlink();
				cachedSprite = null;
			}
			if (cachedSprite != null) {
				return cachedSprite;
			}
		}
		ItemDef itemDef = forID(itemID);
		if (itemDef.stackIDs == null) {
			amount = -1;
		}
		if (amount > 1) {
			int variantID = -1;
			for (int stackIndex = 0; stackIndex < 10; stackIndex++) {
				if (amount >= itemDef.stackAmounts[stackIndex] && itemDef.stackAmounts[stackIndex] != 0) {
					variantID = itemDef.stackIDs[stackIndex];
				}
			}

			if (variantID != -1) {
				itemDef = forID(variantID);
			}
		}
		Model model = itemDef.getLitModel(1);
		if (model == null) {
			return null;
		}
		Sprite baseSprite = null;
		if (itemDef.certTemplateID != -1) {
			baseSprite = getSprite(itemDef.certID, 10, -1);
			if (baseSprite == null) {
				return null;
			}
		}
		Sprite icon = new Sprite(32, 32);
		int savedTextureInt1 = Texture.textureInt1;
		int savedTextureInt2 = Texture.textureInt2;
		int savedLineOffsets[] = Texture.lineOffsets;
		int savedPixels[] = DrawingArea.pixels;
		int savedWidth = DrawingArea.width;
		int savedHeight = DrawingArea.height;
		int savedTopX = DrawingArea.topX;
		int savedBottomX = DrawingArea.bottomX;
		int savedTopY = DrawingArea.topY;
		int savedBottomY = DrawingArea.bottomY;
		Texture.blockShading = false;
		DrawingArea.bindRaster(32, 32, icon.pixels);
		DrawingArea.fillRectangle(32, 0, 0, 32, 0);
		Texture.setScreenSizeFromDrawingArea();
		int viewDistance = itemDef.modelZoom;
		if (outline == -1) {
			viewDistance = (int) (viewDistance * 1.5D);
		}
		if (outline > 0) {
			viewDistance = (int) (viewDistance * 1.04D);
		}
		int zoomSine = Texture.sineTable[itemDef.modelRotation1] * viewDistance >> 16;
		int zoomCosine = Texture.cosineTable[itemDef.modelRotation1] * viewDistance >> 16;
		model.projectRotated(itemDef.modelRotation2, itemDef.modelRotation3, itemDef.modelRotation1, itemDef.modelOffset1, zoomSine + model.modelHeight / 2 + itemDef.modelOffset2, zoomCosine + itemDef.modelOffset2);
		for (int x = 31; x >= 0; x--) {
			for (int y = 31; y >= 0; y--) {
				if (icon.pixels[x + y * 32] == 0) {
					if (x > 0 && icon.pixels[x - 1 + y * 32] > 1) {
						icon.pixels[x + y * 32] = 1;
					} else if (y > 0 && icon.pixels[x + (y - 1) * 32] > 1) {
						icon.pixels[x + y * 32] = 1;
					} else if (x < 31 && icon.pixels[x + 1 + y * 32] > 1) {
						icon.pixels[x + y * 32] = 1;
					} else if (y < 31 && icon.pixels[x + (y + 1) * 32] > 1) {
						icon.pixels[x + y * 32] = 1;
					}
				}
			}

		}

		if (outline > 0) {
			for (int x = 31; x >= 0; x--) {
				for (int y = 31; y >= 0; y--) {
					if (icon.pixels[x + y * 32] == 0) {
						if (x > 0 && icon.pixels[x - 1 + y * 32] == 1) {
							icon.pixels[x + y * 32] = outline;
						} else if (y > 0 && icon.pixels[x + (y - 1) * 32] == 1) {
							icon.pixels[x + y * 32] = outline;
						} else if (x < 31 && icon.pixels[x + 1 + y * 32] == 1) {
							icon.pixels[x + y * 32] = outline;
						} else if (y < 31 && icon.pixels[x + (y + 1) * 32] == 1) {
							icon.pixels[x + y * 32] = outline;
						}
					}
				}

			}

		} else if (outline == 0) {
			for (int x = 31; x >= 0; x--) {
				for (int y = 31; y >= 0; y--) {
					if (icon.pixels[x + y * 32] == 0 && x > 0 && y > 0 && icon.pixels[x - 1 + (y - 1) * 32] > 0) {
						icon.pixels[x + y * 32] = 0x302020;
					}
				}

			}

		}
		if (itemDef.certTemplateID != -1) {
			int savedTrimWidth = baseSprite.trimWidth;
			int savedTrimHeight = baseSprite.trimHeight;
			baseSprite.trimWidth = 32;
			baseSprite.trimHeight = 32;
			baseSprite.drawSprite(0, 0);
			baseSprite.trimWidth = savedTrimWidth;
			baseSprite.trimHeight = savedTrimHeight;
		}
		if (outline == 0) {
			mruNodes1.removeFromCache(icon, itemID);
		}
		DrawingArea.bindRaster(savedHeight, savedWidth, savedPixels);
		DrawingArea.setClip(savedBottomY, savedTopX, savedBottomX, savedTopY);
		Texture.textureInt1 = savedTextureInt1;
		Texture.textureInt2 = savedTextureInt2;
		Texture.lineOffsets = savedLineOffsets;
		Texture.blockShading = true;
		if (itemDef.stackable) {
			icon.trimWidth = 33;
		} else {
			icon.trimWidth = 32;
		}
		icon.trimHeight = amount;
		return icon;
	}

	public Model getLitModel(int amount) {
		if (stackIDs != null && amount > 1) {
			int variantID = -1;
			for (int stackIndex = 0; stackIndex < 10; stackIndex++) {
				if (amount >= stackAmounts[stackIndex] && stackAmounts[stackIndex] != 0) {
					variantID = stackIDs[stackIndex];
				}
			}

			if (variantID != -1) {
				return forID(variantID).getLitModel(1);
			}
		}
		Model model = (Model) mruNodes2.insertFromCache(id);
		if (model != null) {
			return model;
		}
		model = Model.getModel(modelID);
		if (model == null) {
			return null;
		}
		if (modelScaleX != 128 || modelScaleY != 128 || modelScaleZ != 128) {
			model.scale(modelScaleX, modelScaleZ, modelScaleY);
		}
		if (modifiedModelColors != null) {
			for (int colorIndex = 0; colorIndex < modifiedModelColors.length; colorIndex++) {
				model.recolor(modifiedModelColors[colorIndex], originalModelColors[colorIndex]);
			}

		}
		model.applyLighting(64 + ambientLightOffset, 768 + lightDivisorOffset, -50, -10, -50, true);
		model.aBoolean1659 = true;
		mruNodes2.removeFromCache(model, id);
		return model;
	}

	public Model getUnlitModel(int amount) {
		if (stackIDs != null && amount > 1) {
			int variantID = -1;
			for (int stackIndex = 0; stackIndex < 10; stackIndex++) {
				if (amount >= stackAmounts[stackIndex] && stackAmounts[stackIndex] != 0) {
					variantID = stackIDs[stackIndex];
				}
			}

			if (variantID != -1) {
				return forID(variantID).getUnlitModel(1);
			}
		}
		Model model = Model.getModel(modelID);
		if (model == null) {
			return null;
		}
		if (modifiedModelColors != null) {
			for (int colorIndex = 0; colorIndex < modifiedModelColors.length; colorIndex++) {
				model.recolor(modifiedModelColors[colorIndex], originalModelColors[colorIndex]);
			}

		}
		return model;
	}

	private void readValues(Stream stream) {
		do {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0) {
				return;
			}
			if (opcode == 1) {
				modelID = stream.readUnsignedShortBigEndian();
			} else if (opcode == 2) {
				name = stream.readLfTerminatedString();
			} else if (opcode == 3) {
				description = stream.readLfTerminatedBytes();
			} else if (opcode == 4) {
				modelZoom = stream.readUnsignedShortBigEndian();
			} else if (opcode == 5) {
				modelRotation1 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 6) {
				modelRotation2 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 7) {
				modelOffset1 = stream.readUnsignedShortBigEndian();
				if (modelOffset1 > 32767) {
					modelOffset1 -= 0x10000;
				}
			} else if (opcode == 8) {
				modelOffset2 = stream.readUnsignedShortBigEndian();
				if (modelOffset2 > 32767) {
					modelOffset2 -= 0x10000;
				}
			} else if (opcode == 10) {
				stream.readUnsignedShortBigEndian();
			} else if (opcode == 11) {
				stackable = true;
			} else if (opcode == 12) {
				value = stream.readIntBigEndian();
			} else if (opcode == 16) {
				membersObject = true;
			} else if (opcode == 23) {
				maleWornModelID1 = stream.readUnsignedShortBigEndian();
				maleWornOffsetY = stream.readSignedByte();
			} else if (opcode == 24) {
				maleWornModelID2 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 25) {
				femaleWornModelID1 = stream.readUnsignedShortBigEndian();
				femaleWornOffsetY = stream.readSignedByte();
			} else if (opcode == 26) {
				femaleWornModelID2 = stream.readUnsignedShortBigEndian();
			} else if (opcode >= 30 && opcode < 35) {
				if (groundActions == null) {
					groundActions = new String[5];
				}
				groundActions[opcode - 30] = stream.readLfTerminatedString();
				if (groundActions[opcode - 30].equalsIgnoreCase("hidden")) {
					groundActions[opcode - 30] = null;
				}
			} else if (opcode >= 35 && opcode < 40) {
				if (actions == null) {
					actions = new String[5];
				}
				actions[opcode - 35] = stream.readLfTerminatedString();
			} else if (opcode == 40) {
				int colorCount = stream.readUnsignedByte();
				modifiedModelColors = new int[colorCount];
				originalModelColors = new int[colorCount];
				for (int colorIndex = 0; colorIndex < colorCount; colorIndex++) {
					modifiedModelColors[colorIndex] = stream.readUnsignedShortBigEndian();
					originalModelColors[colorIndex] = stream.readUnsignedShortBigEndian();
				}

			} else if (opcode == 78) {
				maleWornModelID3 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 79) {
				femaleWornModelID3 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 90) {
				maleHeadModelID1 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 91) {
				femaleHeadModelID1 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 92) {
				maleHeadModelID2 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 93) {
				femaleHeadModelID2 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 95) {
				modelRotation3 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 97) {
				certID = stream.readUnsignedShortBigEndian();
			} else if (opcode == 98) {
				certTemplateID = stream.readUnsignedShortBigEndian();
			} else if (opcode >= 100 && opcode < 110) {
				if (stackIDs == null) {
					stackIDs = new int[10];
					stackAmounts = new int[10];
				}
				stackIDs[opcode - 100] = stream.readUnsignedShortBigEndian();
				stackAmounts[opcode - 100] = stream.readUnsignedShortBigEndian();
			} else if (opcode == 110) {
				modelScaleX = stream.readUnsignedShortBigEndian();
			} else if (opcode == 111) {
				modelScaleY = stream.readUnsignedShortBigEndian();
			} else if (opcode == 112) {
				modelScaleZ = stream.readUnsignedShortBigEndian();
			} else if (opcode == 113) {
				ambientLightOffset = stream.readSignedByte();
			} else if (opcode == 114) {
				lightDivisorOffset = stream.readSignedByte() * 5;
			} else if (opcode == 115) {
				team = stream.readUnsignedByte();
			}
		} while (true);
	}

	private ItemDef() {
		id = -1;
	}

	private byte femaleWornOffsetY;
	public int value;
	private int[] modifiedModelColors;
	public int id;
	static LruCache mruNodes1 = new LruCache(100);
	public static LruCache mruNodes2 = new LruCache(50);
	private int[] originalModelColors;
	public boolean membersObject;
	private int femaleWornModelID3;
	public int certTemplateID;
	private int femaleWornModelID2;
	private int maleWornModelID1;
	private int maleHeadModelID2;
	private int modelScaleX;
	public String groundActions[];
	private int modelOffset1;
	public String name;
	private static ItemDef[] cache;
	private int femaleHeadModelID2;
	private int modelID;
	private int maleHeadModelID1;
	public boolean stackable;
	public byte description[];
	private int certID;
	private static int cacheIndex;
	public int modelZoom;
	public static boolean isMembers = true;
	private static Stream stream;
	public int lightDivisorOffset;
	private int maleWornModelID3;
	private int maleWornModelID2;
	public String actions[];
	public int modelRotation1;
	private int modelScaleZ;
	private int modelScaleY;
	private int[] stackIDs;
	private int modelOffset2;
	private static int[] streamIndices;
	public int ambientLightOffset;
	private int femaleHeadModelID1;
	public int modelRotation2;
	private int femaleWornModelID1;
	public int[] stackAmounts;
	public int team;
	public static int totalItems;
	private int modelRotation3;
	private byte maleWornOffsetY;

}
