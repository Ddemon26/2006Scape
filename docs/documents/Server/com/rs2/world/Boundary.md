# Boundary

Package `com.rs2.world`.

Defined in [`2006Scape Server/src/main/java/com/rs2/world/Boundary.java`](2006Scape Server/src/main/java/com/rs2/world/Boundary.java).

@author Andrew (Mr Extremez) - added all the boundaries @author Jason http://www.rune-server.org/members/jason - made the system @date Mar 2, 2014

```java
public class Boundary {
public Boundary(int minX, int highX, int minY, int highY)
public Boundary(int minX, int highX, int minY, int highY, int height)
public static boolean isIn(game.entities.Player player, Boundary[] boundaries)
public static boolean isIn(game.entities.Player player, Boundary boundaries)
public static boolean isIn(int x, int y, Boundary boundaries)
public static boolean isIn(int x, int y, int h, Boundary boundaries)
public static boolean isIn(Npc npc, Boundary boundaries)
public static boolean isIn(Npc npc, Boundary[] boundaries)
public static boolean isInSameBoundary(game.entities.Player player1, game.entities.Player player2, Boundary[] boundaries)
public static int entitiesInArea(Boundary boundary)
public static Coordinate centre(Boundary boundary)
public static final Boundary F2P = new Boundary(2944, 3328, 3097, 3515);
public static final Boundary TUTORIAL = new Boundary(3055, 3150, 3054, 3128);
public static final Boundary CRANDOR = new Boundary(2813, 2867, 3226, 3307);
public static final Boundary LUMBRIDGE = new Boundary(3134, 3266, 3131, 3317);
public static final Boundary WIZARDS_TOWER = new Boundary(3094, 3124, 3141, 3172);
public static final Boundary FALADOR = new Boundary(2945, 3066, 3303, 3390);
public static final Boundary VARROCK = new Boundary(3172, 3289, 3368, 3504);
public static final Boundary DRAYNOR = new Boundary(3079, 3149, 3226, 3382);
public static final Boundary BARB = new Boundary(3072, 3098, 3399, 3445);
public static final Boundary GOBLIN_VILLAGE = new Boundary(2945, 2970, 3475, 3515);
public static final Boundary EDGEVILLE = new Boundary(3072, 3126, 3459, 3517);
public static final Boundary PORT_SARIM = new Boundary(3327, 3423, 3131, 3324);
public static final Boundary RIMMINGTON = new Boundary(3327, 3423, 3131, 3324);
public static final Boundary AL_KHARID = new Boundary(3327, 3423, 3131, 3324);
public static final Boundary ZAMMY_WAIT = new Boundary(2409, 2431, 9511, 9535);
public static final Boundary SARA_WAIT = new Boundary(2368, 2392, 9479, 9498);
public static final Boundary BRIMHAVEN = new Boundary(2688, 2815, 3131, 3258);
public static final Boundary DESERT = new Boundary(3137, 3517, 2747, 3130, 0);
public static final Boundary NARDAH = new Boundary(3392, 3455, 2876, 2940);
public static final Boundary BANDIT_CAMP = new Boundary(3151, 3192, 2963, 2986);
public static final Boundary MINING_CAMP = new Boundary(3267, 3311, 3000, 3043);
public static final Boundary BEDABIN = new Boundary(3160, 3187, 3015, 3046);
public static final Boundary UZER = new Boundary(3462, 3503, 3068, 3109);
public static final Boundary AGILITY_PYRAMID = new Boundary(3329, 3391, 2812, 2855);
public static final Boundary PYRAMID = new Boundary(3217, 3250, 2881, 2908);
public static final Boundary SOPHANEM = new Boundary(3273, 3323, 2749, 2806);
public static final Boundary MENAPHOS = new Boundary(3200, 3266, 2749, 2806);
public static final Boundary POLLIVNEACH = new Boundary(3329, 3377, 2936, 3002);
public static final Boundary SHANTAY_PASS = new Boundary(3295, 3311, 3116, 3128);
public static final Boundary MORTYANIA = new Boundary(3401, 3773, 3157, 3577);
public static final Boundary[] WILDERNESS = new Boundary[] { new Boundary(2941, 3392, 3518, 3966), new Boundary(2941, 3392, 9922, 10366) };
public static final Boundary IN_LESSER = new Boundary(3108, 3112, 3156, 3158, 2);
public static final Boundary IN_DUEL = new Boundary(3331, 3391, 3242, 3260);
public static final Boundary[] IN_DUEL_AREA = new Boundary[] { new Boundary(3322, 3394, 3195, 3291), new Boundary(3311, 3323, 3223, 3248) };
public static final Boundary TRAWLER_GAME = new Boundary (2808, 2811, 3415, 3425);
public static final Boundary PITS_WAIT = new Boundary (2394, 2404, 5169, 5175);
public static final Boundary[] LUMB_BUILDING = new Boundary[] { new Boundary(3205, 3216, 3209, 3228), new Boundary(3229, 3233, 3206, 3208), new Boundary(3228, 3233, 3201, 3205), new Boundary(3230, 3237, 3195, 3198), new Boundary(3238, 3229, 3209, 3211),
public static final Boundary[] DRAYNOR_BUILDING = new Boundary[] { new Boundary(3097, 3102, 3277, 3281), new Boundary(3088, 3092, 3273, 3276), new Boundary(3096, 3102, 3266, 3270), new Boundary(3089, 3095, 3265, 3268), new Boundary(3083, 3088, 3256, 3261),
public static final Boundary VARROCK_BANK_BASEMENT = new Boundary(3186, 3197, 9817, 9824, 0);
public static final Boundary MAGE_TOWER_CAGE = new Boundary(3108, 3112, 3156, 3158, 2);
public static final Boundary ARDOUGNE_ZOO = new Boundary(2593, 2639, 3265, 3288);
public static final Boundary APE_ATOLL = new Boundary(2694, 2811, 2691, 2805);
public static final Boundary BARROWS = new Boundary(3543, 3584, 3265, 3311);
public static final Boundary BARROWS_UNDERGROUND = new Boundary(3529, 3581, 9673, 9722);
public static final Boundary PC_BOAT = new Boundary(2660, 2663, 2638, 2643);
public static final Boundary PC_GAME = new Boundary(2624, 2690, 2550, 2619);
public static final Boundary FIGHT_CAVES = new Boundary(2360, 2445, 5045, 5125);
public static final Boundary PIRATE_HOUSE = new Boundary(3038, 3044, 3949, 3959);
public static final Boundary[] FIGHT_PITS = new Boundary[] { new Boundary(2378, 3415, 5133, 5167), new Boundary(2394, 2404, 5169, 5174) };
public static final Boundary PARTY_ROOM = new Boundary(2727, 2746, 3460, 3479);
public static final Boundary PARTY_ROOM_TABLE = new Boundary(2735, 2740, 3467, 3468);
public static final Boundary MAGE_TRAINING_ARENA = new Boundary(3330, 3388, 9614, 9727);
public static final Boundary MAGE_TRAINING_ARENA_ENCHANTING = new Boundary(3341, 3386, 9618, 9662, 0);
public static final Boundary MAGE_TRAINING_ARENA_GRAVEYARD =  new Boundary(3340, 3386, 9616, 9662, 1);
public static final Boundary MAGE_TRAINING_ARENA_ALCHEMY = new Boundary(3350, 3379, 9616, 9655, 2);
public static final Boundary MAGE_TRAINING_ARENA_TELEKINETIC =  new Boundary(3329, 3390, 9665, 9726);
public static final Boundary[] DWARF_NO_FIREMAKING = new Boundary[] { new Boundary(2944, 3072, 3392, 3456), new Boundary(3008, 3072, 3456, 3520), new Boundary(2880, 2944, 3456, 3520) };
```
