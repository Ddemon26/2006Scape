// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class ItemDef {

    public static void resetCache() {
            modelCache = null;
            spriteCache = null;
            streamIndices = null;
            cache = null;
            stream = null;
    }

       public boolean areDialogueModelsCached(int gender) {
               int head = maleHeadModel;
               int head2 = maleHeadModel2;
               if (gender == 1) {
                       head = femaleHeadModel;
                       head2 = femaleHeadModel2;
               }
               if (head == -1) {
                       return true;
               }
               boolean flag = true;
               if (!Model.isLoaded(head)) {
                       flag = false;
               }
               if (head2 != -1 && !Model.isLoaded(head2)) {
                       flag = false;
               }
               return flag;
       }

	public static void unpackConfig(StreamLoader streamLoader) {
		stream = new Stream(streamLoader.getFileData("obj.dat"));
		Stream stream = new Stream(streamLoader.getFileData("obj.idx"));
		totalItems = stream.readUnsignedWord();
		streamIndices = new int[totalItems];
		int i = 2;
		for (int j = 0; j < totalItems; j++) {
			streamIndices[j] = i;
			i += stream.readUnsignedWord();
		}

		cache = new ItemDef[10];
		for (int k = 0; k < 10; k++) {
			cache[k] = new ItemDef();
		}

	}

       public Model getDialogueModel(int gender) {
               int head = maleHeadModel;
               int head2 = maleHeadModel2;
               if (gender == 1) {
                       head = femaleHeadModel;
                       head2 = femaleHeadModel2;
               }
               if (head == -1) {
                       return null;
               }
               Model model = Model.create(head);
               if (head2 != -1) {
                       Model model_1 = Model.create(head2);
                       Model aclass30_sub2_sub4_sub6s[] = {model, model_1};
                       model = new Model(2, aclass30_sub2_sub4_sub6s);
               }
		if (modifiedModelColors != null) {
			for (int i1 = 0; i1 < modifiedModelColors.length; i1++) {
				model.recolor(modifiedModelColors[i1], originalModelColors[i1]);
			}

		}
		return model;
	}

       public boolean areWearModelsCached(int gender) {
               int body = maleModel1;
               int arm = maleModel2;
               int leg = maleModel3;
               if (gender == 1) {
                       body = femaleModel1;
                       arm = femaleModel2;
                       leg = femaleModel3;
               }
               if (body == -1) {
                       return true;
               }
               boolean flag = true;
               if (!Model.isLoaded(body)) {
                       flag = false;
               }
               if (arm != -1 && !Model.isLoaded(arm)) {
                       flag = false;
               }
               if (leg != -1 && !Model.isLoaded(leg)) {
                       flag = false;
               }
               return flag;
       }

       public Model getWearModel(int gender) {
               int body = maleModel1;
               int arm = maleModel2;
               int leg = maleModel3;
               if (gender == 1) {
                       body = femaleModel1;
                       arm = femaleModel2;
                       leg = femaleModel3;
               }
               if (body == -1) {
                       return null;
               }
               Model model = Model.create(body);
               if (arm != -1) {
                       if (leg != -1) {
                               Model model_1 = Model.create(arm);
                               Model model_3 = Model.create(leg);
                               Model aclass30_sub2_sub4_sub6_1s[] = {model, model_1, model_3};
                               model = new Model(3, aclass30_sub2_sub4_sub6_1s);
                       } else {
                               Model model_2 = Model.create(arm);
                               Model aclass30_sub2_sub4_sub6s[] = {model, model_2};
                               model = new Model(2, aclass30_sub2_sub4_sub6s);
                       }
               }
               if (gender == 0 && maleOffsetY != 0) {
                       model.translate(0, maleOffsetY, 0);
               }
               if (gender == 1 && femaleOffsetY != 0) {
                       model.translate(0, femaleOffsetY, 0);
               }
		if (modifiedModelColors != null) {
			for (int i1 = 0; i1 < modifiedModelColors.length; i1++) {
				model.recolor(modifiedModelColors[i1], originalModelColors[i1]);
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
		zRotation = 0;
		offsetX = 0;
		offsetY = 0;
		stackable = false;
		value = 1;
		membersObject = false;
		groundActions = null;
		actions = null;
		maleModel1 = -1;
		maleModel2 = -1;
		maleOffsetY = 0;
		femaleModel1 = -1;
		femaleModel2 = -1;
		femaleOffsetY = 0;
		maleModel3 = -1;
		femaleModel3 = -1;
		maleHeadModel = -1;
		maleHeadModel2 = -1;
		femaleHeadModel = -1;
		femaleHeadModel2 = -1;
		stackIDs = null;
		stackAmounts = null;
		certID = -1;
		certTemplateID = -1;
		resizeX = 128;
		resizeY = 128;
		resizeZ = 128;
		ambient = 0;
		contrast = 0;
		team = 0;
	}

    public static ItemDef lookup(int i) {
		for (int j = 0; j < 10; j++) {
			if (cache[j].id == i) {
				return cache[j];
			}
		}

		cacheIndex = (cacheIndex + 1) % 10;
		ItemDef itemDef = cache[cacheIndex];
                if (i < streamIndices.length) {
                        stream.currentOffset = streamIndices[i];
                        itemDef.id = i;
                        itemDef.setDefaults();
                        itemDef.readValues(stream);
                } else {
                        // custom items are not present in obj.dat
                        itemDef.id = i;
                        itemDef.setDefaults();
                }
                if (itemDef.certTemplateID != -1) {
                        itemDef.convertToNote();
                }
		if (i == 6543) {
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
		switch (i) {
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.zRotation = 0;
			itemDef.maleModel1 = 18920;
			itemDef.femaleModel1 = 18969;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18920;
			itemDef.femaleModel1 = 18969;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.zRotation = 0;
			itemDef.maleModel1 = 18954;
			itemDef.femaleModel1 = 18989;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18954;
			itemDef.femaleModel1 = 18989;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18928;
			itemDef.femaleModel1 = 18973;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18928;
			itemDef.femaleModel1 = 18973;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18948;
			itemDef.femaleModel1 = 18985;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18948;
			itemDef.femaleModel1 = 18985;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18945;
			itemDef.femaleModel1 = 18983;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18945;
			itemDef.femaleModel1 = 18983;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18915;
			itemDef.femaleModel1 = 18981;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18915;
			itemDef.femaleModel1 = 18981;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18950;
			itemDef.femaleModel1 = 18986;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18950;
			itemDef.femaleModel1 = 18986;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18939;
			itemDef.femaleModel1 = 18980;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18939;
			itemDef.femaleModel1 = 18980;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18918;
			itemDef.femaleModel1 = 18968;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18918;
			itemDef.femaleModel1 = 18968;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18938;
			itemDef.femaleModel1 = 18978;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18938;
			itemDef.femaleModel1 = 18978;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18956;
			itemDef.femaleModel1 = 18956;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18956;
			itemDef.femaleModel1 = 18956;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18925;
			itemDef.femaleModel1 = 18972;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18925;
			itemDef.femaleModel1 = 18972;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18935;
			itemDef.femaleModel1 = 18977;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18935;
			itemDef.femaleModel1 = 18977;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18951;
			itemDef.femaleModel1 = 18987;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18951;
			itemDef.femaleModel1 = 18987;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.modelID = 19045;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18916;
			itemDef.femaleModel1 = 18970;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.modelID = 19045;
			itemDef.modelZoom = 2128;
			itemDef.modelRotation1 = 504;
			itemDef.modelRotation2 = 0;
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18916;
			itemDef.femaleModel1 = 18970;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18942;
			itemDef.femaleModel1 = 18982;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18942;
			itemDef.femaleModel1 = 18982;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18953;
			itemDef.femaleModel1 = 18988;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18953;
			itemDef.femaleModel1 = 18988;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18934;
			itemDef.femaleModel1 = 18976;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18934;
			itemDef.femaleModel1 = 18976;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18924;
			itemDef.femaleModel1 = 18971;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18924;
			itemDef.femaleModel1 = 18971;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.zRotation = 0;
			itemDef.maleModel1 = 18932;
			itemDef.femaleModel1 = 18975;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.zRotation = 0;
			itemDef.maleModel1 = 18932;
			itemDef.femaleModel1 = 18975;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18955;
			itemDef.femaleModel1 = 18991;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18955;
			itemDef.femaleModel1 = 18991;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18929;
			itemDef.femaleModel1 = 18974;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18929;
			itemDef.femaleModel1 = 18974;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
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
			itemDef.zRotation = 0;
			itemDef.offsetX = 0;
			itemDef.offsetY = 1;
			itemDef.maleModel1 = 18946;
			itemDef.femaleModel1 = 18984;
			itemDef.maleHeadModel = -1;
			itemDef.femaleHeadModel = -1;
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
			itemDef.offsetX = 0;
			itemDef.offsetY = 0;
			itemDef.maleModel1 = 18914;
			itemDef.femaleModel1 = 18967;
			itemDef.name = "Quest hood";
                        itemDef.description = "Quest skillcape hood.".getBytes();
                        break;

                case 16022:
                        itemDef.actions = new String[5];
                        itemDef.actions[1] = "Wield";
                        itemDef.modelID = 5412;
                        itemDef.maleModel1 = 5409;
                        itemDef.femaleModel1 = 5409;
                        itemDef.modelZoom = 840;
                        itemDef.modelRotation1 = 280;
                        itemDef.modelRotation2 = 0;
                        itemDef.offsetX = 0;
                        itemDef.offsetY = 56;
                        itemDef.stackable = false;
                        itemDef.name = "Lime whip";
                        itemDef.description = "A lime-colored abyssal whip.".getBytes();
                        itemDef.modifiedModelColors = new int[1];
                        itemDef.originalModelColors = new int[1];
                        // keep vanilla colors on the equipped model for now
                        itemDef.modifiedModelColors[0] = 528;
                        itemDef.originalModelColors[0] = 17350;
                        break;
                        }
                return itemDef;
        }

        private void convertToNote() {
                ItemDef itemDef = lookup(certTemplateID);
		modelID = itemDef.modelID;
		modelZoom = itemDef.modelZoom;
		modelRotation1 = itemDef.modelRotation1;
		modelRotation2 = itemDef.modelRotation2;

		zRotation = itemDef.zRotation;
		offsetX = itemDef.offsetX;
		offsetY = itemDef.offsetY;
		modifiedModelColors = itemDef.modifiedModelColors;
		originalModelColors = itemDef.originalModelColors;
                ItemDef itemDef_1 = lookup(certID);
		name = itemDef_1.name;
		membersObject = itemDef_1.membersObject;
		value = itemDef_1.value;
		String s = "a";
		char c = itemDef_1.name.charAt(0);
		if (c == 'A' || c == 'E' || c == 'I' || c == 'O' || c == 'U') {
			s = "an";
		}
		description = ("Swap this note at any bank for " + s + " " + itemDef_1.name + ".").getBytes();
		stackable = true;
	}

	public static Sprite getSprite(int i, int j, int k) {
		if (k == 0) {
        Sprite sprite = (Sprite) spriteCache.get(i);
			if (sprite != null && sprite.trimHeight != j && sprite.trimHeight != -1) {
				sprite.unlink();
				sprite = null;
			}
			if (sprite != null) {
				return sprite;
			}
		}
                ItemDef itemDef = lookup(i);
		if (itemDef.stackIDs == null) {
			j = -1;
		}
		if (j > 1) {
			int i1 = -1;
			for (int j1 = 0; j1 < 10; j1++) {
				if (j >= itemDef.stackAmounts[j1] && itemDef.stackAmounts[j1] != 0) {
					i1 = itemDef.stackIDs[j1];
				}
			}

			if (i1 != -1) {
                                itemDef = lookup(i1);
			}
		}
               Model model = itemDef.getModel(1);
		if (model == null) {
			return null;
		}
		Sprite sprite = null;
		if (itemDef.certTemplateID != -1) {
			sprite = getSprite(itemDef.certID, 10, -1);
			if (sprite == null) {
				return null;
			}
		}
		Sprite sprite2 = new Sprite(32, 32);
		int k1 = Texture.textureInt1;
		int l1 = Texture.textureInt2;
		int ai[] = Texture.lineOffsets;
		int ai1[] = DrawingArea.pixels;
		int i2 = DrawingArea.width;
		int j2 = DrawingArea.height;
		int k2 = DrawingArea.topX;
		int l2 = DrawingArea.bottomX;
		int i3 = DrawingArea.topY;
		int j3 = DrawingArea.bottomY;
		Texture.highQuality = false;
		DrawingArea.initDrawingArea(32, 32, sprite2.pixels);
		DrawingArea.fillArea(32, 0, 0, 32, 0);
		Texture.init();
		int k3 = itemDef.modelZoom;
		if (k == -1) {
			k3 = (int) (k3 * 1.5D);
		}
		if (k > 0) {
			k3 = (int) (k3 * 1.04D);
		}
		int l3 = Texture.sineTable[itemDef.modelRotation1] * k3 >> 16;
		int i4 = Texture.cosineTable[itemDef.modelRotation1] * k3 >> 16;
		model.transformVertices(itemDef.modelRotation2, itemDef.zRotation, itemDef.modelRotation1, itemDef.offsetX, l3 + model.modelHeight / 2 + itemDef.offsetY, i4 + itemDef.offsetY);
		for (int i5 = 31; i5 >= 0; i5--) {
			for (int j4 = 31; j4 >= 0; j4--) {
				if (sprite2.pixels[i5 + j4 * 32] == 0) {
					if (i5 > 0 && sprite2.pixels[i5 - 1 + j4 * 32] > 1) {
						sprite2.pixels[i5 + j4 * 32] = 1;
					} else if (j4 > 0 && sprite2.pixels[i5 + (j4 - 1) * 32] > 1) {
						sprite2.pixels[i5 + j4 * 32] = 1;
					} else if (i5 < 31 && sprite2.pixels[i5 + 1 + j4 * 32] > 1) {
						sprite2.pixels[i5 + j4 * 32] = 1;
					} else if (j4 < 31 && sprite2.pixels[i5 + (j4 + 1) * 32] > 1) {
						sprite2.pixels[i5 + j4 * 32] = 1;
					}
				}
			}

		}

		if (k > 0) {
			for (int j5 = 31; j5 >= 0; j5--) {
				for (int k4 = 31; k4 >= 0; k4--) {
					if (sprite2.pixels[j5 + k4 * 32] == 0) {
						if (j5 > 0 && sprite2.pixels[j5 - 1 + k4 * 32] == 1) {
							sprite2.pixels[j5 + k4 * 32] = k;
						} else if (k4 > 0 && sprite2.pixels[j5 + (k4 - 1) * 32] == 1) {
							sprite2.pixels[j5 + k4 * 32] = k;
						} else if (j5 < 31 && sprite2.pixels[j5 + 1 + k4 * 32] == 1) {
							sprite2.pixels[j5 + k4 * 32] = k;
						} else if (k4 < 31 && sprite2.pixels[j5 + (k4 + 1) * 32] == 1) {
							sprite2.pixels[j5 + k4 * 32] = k;
						}
					}
				}

			}

		} else if (k == 0) {
			for (int k5 = 31; k5 >= 0; k5--) {
				for (int l4 = 31; l4 >= 0; l4--) {
					if (sprite2.pixels[k5 + l4 * 32] == 0 && k5 > 0 && l4 > 0 && sprite2.pixels[k5 - 1 + (l4 - 1) * 32] > 0) {
						sprite2.pixels[k5 + l4 * 32] = 0x302020;
					}
				}

			}

		}
		if (itemDef.certTemplateID != -1) {
			int l5 = sprite.trimWidth;
			int j6 = sprite.trimHeight;
			sprite.trimWidth = 32;
			sprite.trimHeight = 32;
                        sprite.drawTransparentSprite(0, 0);
			sprite.trimWidth = l5;
			sprite.trimHeight = j6;
		}
		if (k == 0) {
        spriteCache.put(sprite2, i);
		}
		DrawingArea.initDrawingArea(j2, i2, ai1);
		DrawingArea.setDrawingArea(j3, k2, l2, i3);
		Texture.textureInt1 = k1;
		Texture.textureInt2 = l1;
		Texture.lineOffsets = ai;
		Texture.highQuality = true;
		if (itemDef.stackable) {
			sprite2.trimWidth = 33;
		} else {
			sprite2.trimWidth = 32;
		}
		sprite2.trimHeight = j;
		return sprite2;
	}

       public Model getModel(int amount) {
               if (stackIDs != null && amount > 1) {
                       int id = -1;
                       for (int k = 0; k < 10; k++) {
                               if (amount >= stackAmounts[k] && stackAmounts[k] != 0) {
                                       id = stackIDs[k];
                               }
                       }

                       if (id != -1) {
                               return lookup(id).getModel(1);
                       }
               }
        Model model = (Model) modelCache.get(id);
		if (model != null) {
			return model;
		}
		model = Model.create(modelID);
		if (model == null) {
			return null;
		}
		if (resizeX != 128 || resizeY != 128 || resizeZ != 128) {
			model.scaleModel(resizeX, resizeZ, resizeY);
		}
		if (modifiedModelColors != null) {
			for (int l = 0; l < modifiedModelColors.length; l++) {
				model.recolor(modifiedModelColors[l], originalModelColors[l]);
			}

		}
		model.applyLighting(64 + ambient, 768 + contrast, -50, -10, -50, true);
		model.aBoolean1659 = true;
        modelCache.put(model, id);
		return model;
	}

       public Model getInterfaceModel(int amount) {
               if (stackIDs != null && amount > 1) {
                       int id = -1;
                       for (int k = 0; k < 10; k++) {
                               if (amount >= stackAmounts[k] && stackAmounts[k] != 0) {
                                       id = stackIDs[k];
                               }
                       }

                       if (id != -1) {
                               return lookup(id).getInterfaceModel(1);
                       }
               }
               Model model = Model.create(modelID);
		if (model == null) {
			return null;
		}
		if (modifiedModelColors != null) {
			for (int l = 0; l < modifiedModelColors.length; l++) {
				model.recolor(modifiedModelColors[l], originalModelColors[l]);
			}

		}
		return model;
	}

	private void readValues(Stream stream) {
		do {
			int i = stream.readUnsignedByte();
			if (i == 0) {
				return;
			}
			if (i == 1) {
				modelID = stream.readUnsignedWord();
			} else if (i == 2) {
				name = stream.readString();
			} else if (i == 3) {
				description = stream.readBytes();
			} else if (i == 4) {
				modelZoom = stream.readUnsignedWord();
			} else if (i == 5) {
				modelRotation1 = stream.readUnsignedWord();
			} else if (i == 6) {
				modelRotation2 = stream.readUnsignedWord();
			} else if (i == 7) {
				offsetX = stream.readUnsignedWord();
				if (offsetX > 32767) {
					offsetX -= 0x10000;
				}
			} else if (i == 8) {
				offsetY = stream.readUnsignedWord();
				if (offsetY > 32767) {
					offsetY -= 0x10000;
				}
			} else if (i == 10) {
				stream.readUnsignedWord();
			} else if (i == 11) {
				stackable = true;
			} else if (i == 12) {
				value = stream.readDWord();
			} else if (i == 16) {
				membersObject = true;
			} else if (i == 23) {
				maleModel1 = stream.readUnsignedWord();
				maleOffsetY = stream.readSignedByte();
			} else if (i == 24) {
				maleModel2 = stream.readUnsignedWord();
			} else if (i == 25) {
				femaleModel1 = stream.readUnsignedWord();
				femaleOffsetY = stream.readSignedByte();
			} else if (i == 26) {
				femaleModel2 = stream.readUnsignedWord();
			} else if (i >= 30 && i < 35) {
				if (groundActions == null) {
					groundActions = new String[5];
				}
				groundActions[i - 30] = stream.readString();
				if (groundActions[i - 30].equalsIgnoreCase("hidden")) {
					groundActions[i - 30] = null;
				}
			} else if (i >= 35 && i < 40) {
				if (actions == null) {
					actions = new String[5];
				}
				actions[i - 35] = stream.readString();
			} else if (i == 40) {
				int j = stream.readUnsignedByte();
				modifiedModelColors = new int[j];
				originalModelColors = new int[j];
				for (int k = 0; k < j; k++) {
					modifiedModelColors[k] = stream.readUnsignedWord();
					originalModelColors[k] = stream.readUnsignedWord();
				}

			} else if (i == 78) {
				maleModel3 = stream.readUnsignedWord();
			} else if (i == 79) {
				femaleModel3 = stream.readUnsignedWord();
			} else if (i == 90) {
				maleHeadModel = stream.readUnsignedWord();
			} else if (i == 91) {
				femaleHeadModel = stream.readUnsignedWord();
			} else if (i == 92) {
				maleHeadModel2 = stream.readUnsignedWord();
			} else if (i == 93) {
				femaleHeadModel2 = stream.readUnsignedWord();
			} else if (i == 95) {
				zRotation = stream.readUnsignedWord();
			} else if (i == 97) {
				certID = stream.readUnsignedWord();
			} else if (i == 98) {
				certTemplateID = stream.readUnsignedWord();
			} else if (i >= 100 && i < 110) {
				if (stackIDs == null) {
					stackIDs = new int[10];
					stackAmounts = new int[10];
				}
				stackIDs[i - 100] = stream.readUnsignedWord();
				stackAmounts[i - 100] = stream.readUnsignedWord();
			} else if (i == 110) {
				resizeX = stream.readUnsignedWord();
			} else if (i == 111) {
				resizeY = stream.readUnsignedWord();
			} else if (i == 112) {
				resizeZ = stream.readUnsignedWord();
			} else if (i == 113) {
				ambient = stream.readSignedByte();
			} else if (i == 114) {
				contrast = stream.readSignedByte() * 5;
			} else if (i == 115) {
				team = stream.readUnsignedByte();
			}
		} while (true);
	}

	private ItemDef() {
		id = -1;
	}

	private byte femaleOffsetY;
	public int value;
	private int[] modifiedModelColors;
	public int id;
    static MRUCache spriteCache = new MRUCache(100);
    public static MRUCache modelCache = new MRUCache(50);
	private int[] originalModelColors;
	public boolean membersObject;
	private int femaleModel3;
	public int certTemplateID;
	private int femaleModel2;
	private int maleModel1;
	private int maleHeadModel2;
	private int resizeX;
	public String groundActions[];
	private int offsetX;
	public String name;
	private static ItemDef[] cache;
	private int femaleHeadModel2;
	private int modelID;
	private int maleHeadModel;
	public boolean stackable;
	public byte description[];
	private int certID;
	private static int cacheIndex;
	public int modelZoom;
	public static boolean isMembers = true;
	private static Stream stream;
	public int contrast;
	private int maleModel3;
	private int maleModel2;
	public String actions[];
	public int modelRotation1;
	private int resizeZ;
	private int resizeY;
	private int[] stackIDs;
	private int offsetY;
	private static int[] streamIndices;
	public int ambient;
	private int femaleHeadModel;
	public int modelRotation2;
	private int femaleModel1;
	public int[] stackAmounts;
	public int team;
	public static int totalItems;
	private int zRotation;
	private byte maleOffsetY;

}
