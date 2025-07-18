# CombatAssistant

**Package:** `com.rs2.game.content.combat`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/content/combat/CombatAssistant.java`](2006Scape Server/src/main/java/com/rs2/game/content/combat/CombatAssistant.java)

**Authors:** Multiple contributors, Andrew (Mr Extremez)

## Overview

The `CombatAssistant` class is the core combat engine for the 2006Scape server, handling all aspects of player combat including melee, ranged, and magic combat against both NPCs and other players. It manages damage calculations, hit timing, combat requirements, special attacks, and all combat-related mechanics. This class is essential for implementing authentic RuneScape combat mechanics and ensuring balanced gameplay.

## Key Responsibilities

- **Combat Processing**: Managing attack sequences, hit delays, and damage application
- **Damage Calculations**: Computing damage for melee, ranged, and magic attacks
- **Combat Validation**: Checking requirements, distances, and combat restrictions
- **Experience Distribution**: Awarding appropriate experience for combat actions
- **Special Effects**: Handling weapon special attacks and combat bonuses
- **PvP and PvM**: Supporting both player vs player and player vs monster combat
- **Combat States**: Managing combat timers, animations, and status effects

## Core Architecture

### Player Association
```java
private final Player player;

public CombatAssistant(Player player) {
    this.player = player;
}
```

Each CombatAssistant instance is tied to a specific player, managing their combat interactions and calculations.

### Combat Types
The system supports three main combat types:
- **Melee Combat**: Close-range weapon attacks
- **Ranged Combat**: Bow, crossbow, and thrown weapon attacks  
- **Magic Combat**: Spell casting and magical attacks

## Core Methods

### Combat State Management

#### `inCombat()`
Determines if the player is currently in combat:

```java
public boolean inCombat() {
    return (player.underAttackBy > 0 || player.underAttackBy2 > 0);
}
```

This method checks if the player is being attacked by any other entity, which affects various game mechanics like teleportation restrictions and logout delays.

#### `checkReqs()`
Validates combat requirements before allowing attacks:

```java
public boolean checkReqs() {
    // Check if player can attack (not stunned, frozen, etc.)
    if (player.freezeTimer > 0) {
        return false;
    }
    
    // Check if target is valid
    if (player.playerIndex > 0) {
        Player target = PlayerHandler.players[player.playerIndex];
        if (target == null || target.isDead || target.disconnected) {
            return false;
        }
        
        // Check combat level restrictions
        if (!validCombatLevel(target)) {
            player.getPacketSender().sendMessage("Your combat level difference is too great!");
            return false;
        }
    }
    
    return true;
}
```

### NPC Combat

#### `attackNpc(int npcIndex)`
Initiates an attack against an NPC:

```java
public void attackNpc(int npcIndex) {
    if (NpcHandler.npcs[npcIndex] == null) return;
    
    Npc npc = NpcHandler.npcs[npcIndex];
    
    // Check if NPC is dead or invalid
    if (npc.isDead || npc.MaxHP <= 0) {
        resetPlayerAttack();
        return;
    }
    
    // Check special requirements (slayer, quest items, etc.)
    if (!SlayerRequirements.itemNeededSlayer(player, npcIndex) || 
        !player.getSlayer().canAttackNpc(npcIndex)) {
        return;
    }
    
    // Handle special NPCs (Count Draynor, etc.)
    if (npc.npcType == COUNT_DRAYNOR && player.vampSlayer > 2) {
        if (!player.getItemAssistant().playerHasItem(1549, 1) || // Stake
            !player.getItemAssistant().playerHasItem(2347, 1)) { // Hammer
            player.getPacketSender().sendMessage("You need a stake and hammer to attack count draynor.");
            resetPlayerAttack();
            return;
        }
    }
    
    // Set combat target and begin attack sequence
    player.npcIndex = npcIndex;
    player.followNpcId = npcIndex;
    player.faceNpc(npcIndex);
    
    // Calculate attack delay and start combat
    player.attackTimer = getAttackDelay();
}
```

#### `delayedHit(int npcIndex)`
Processes delayed damage application to NPCs:

```java
public void delayedHit(int npcIndex) {
    if (NpcHandler.npcs[npcIndex] == null || NpcHandler.npcs[npcIndex].isDead) {
        player.npcIndex = 0;
        return;
    }
    
    Npc npc = NpcHandler.npcs[npcIndex];
    
    // Apply block animation if NPC is defending
    if (npc.attackTimer <= 3 || (npc.attackTimer == 0 && npc.hitDelayTimer > 0 && !player.castingMagic)) {
        npc.animNumber = NpcEmotes.getBlockEmote(npcIndex);
        npc.animUpdateRequired = true;
        npc.updateRequired = true;
    }
    
    // Play combat sounds
    if (CombatConstants.COMBAT_SOUNDS) {
        player.getPacketSender().sendSound(
            CombatSounds.getNpcBlockSound(npc.npcType), 100, 0);
    }
    
    // Make NPC face the player
    npc.facePlayer(player);
    
    // Set combat ownership
    if (npc.underAttackBy > 0 && GameEngine.npcHandler.getsPulled(player, npcIndex)) {
        npc.killerId = player.playerId;
    }
    
    // Apply damage based on combat type
    if (player.projectileStage == 0) { // Melee damage
        applyNpcMeleeDamage(npcIndex, 1);
        if (player.doubleHit) {
            applyNpcMeleeDamage(npcIndex, 2);
        }
    } else if (!player.castingMagic && player.projectileStage > 0) { // Ranged damage
        applyNpcRangedDamage(npcIndex);
    }
}
```

#### `applyNpcMeleeDamage(int npcIndex, int damageMask)`
Calculates and applies melee damage to NPCs:

```java
public void applyNpcMeleeDamage(int npcIndex, int damageMask) {
    if (NpcHandler.npcs[npcIndex] == null) return;
    
    Npc npc = NpcHandler.npcs[npcIndex];
    int damage = 0;
    
    // Calculate base damage
    int maxHit = meleeMaxHit();
    damage = Misc.random(maxHit);
    
    // Check if attack hits (accuracy calculation)
    int attackRoll = Misc.random(calcAtt());
    int defenceRoll = Misc.random(npc.defence);
    
    if (defenceRoll > attackRoll) {
        damage = 0; // Attack missed
    }
    
    // Apply special weapon effects
    boolean guthansEffect = player.getPlayerAssistant().fullGuthans() && Misc.random(4) == 0;
    
    // Cap damage to remaining HP
    if (npc.HP - damage < 0) {
        damage = npc.HP;
    }
    
    // Award experience
    awardMeleeExperience(damage, npcIndex);
    
    // Apply special effects
    if (damage > 0 && guthansEffect) {
        // Guthan's healing effect
        player.playerLevel[Constants.HITPOINTS] += damage;
        if (player.playerLevel[Constants.HITPOINTS] > 
            player.getPlayerAssistant().getLevelForXP(player.playerXP[Constants.HITPOINTS])) {
            player.playerLevel[Constants.HITPOINTS] = 
                player.getPlayerAssistant().getLevelForXP(player.playerXP[Constants.HITPOINTS]);
        }
        player.getPlayerAssistant().refreshSkill(Constants.HITPOINTS);
        npc.gfx0(398); // Guthan's heal graphic
    }
    
    // Apply damage to NPC
    switch (damageMask) {
        case 1:
            npc.hitDiff = damage;
            npc.HP -= damage;
            npc.hitUpdateRequired = true;
            break;
        case 2:
            npc.hitDiff2 = damage;
            npc.HP -= damage;
            npc.hitUpdateRequired2 = true;
            player.doubleHit = false;
            break;
    }
    
    npc.updateRequired = true;
    player.totalDamageDealt += damage;
}
```

### Player vs Player Combat

#### `attackPlayer(int playerIndex)`
Initiates combat against another player:

```java
public void attackPlayer(int playerIndex) {
    if (PlayerHandler.players[playerIndex] == null) return;
    
    Player target = PlayerHandler.players[playerIndex];
    
    // Check if target is valid
    if (target.isDead || target.disconnected) {
        resetPlayerAttack();
        return;
    }
    
    // Check combat restrictions
    if (!checkReqs()) {
        return;
    }
    
    // Check wilderness level restrictions
    if (!validCombatLevel(target)) {
        player.getPacketSender().sendMessage("Your combat level difference is too great!");
        resetPlayerAttack();
        return;
    }
    
    // Check safe areas
    if (target.inSafeArea() || player.inSafeArea()) {
        player.getPacketSender().sendMessage("You cannot attack other players here!");
        resetPlayerAttack();
        return;
    }
    
    // Set PvP target
    player.playerIndex = playerIndex;
    player.followPlayerId = playerIndex;
    player.faceUpdate(playerIndex);
    
    // Start combat sequence
    player.attackTimer = getAttackDelay();
}
```

### Damage Calculations

#### `meleeMaxHit()`
Calculates maximum melee damage:

```java
public int meleeMaxHit() {
    int maxHit = 0;
    
    // Base damage from strength level
    int strengthLevel = player.playerLevel[Constants.STRENGTH];
    maxHit = (int) (0.5 + strengthLevel * 0.325);
    
    // Add strength bonus from equipment
    int strengthBonus = player.playerBonus[10]; // Strength bonus index
    maxHit += (strengthBonus * 0.00175 * strengthLevel);
    
    // Apply prayer bonuses
    if (player.getPrayer().prayerActive[1]) { // Burst of Strength
        maxHit *= 1.05;
    } else if (player.getPrayer().prayerActive[6]) { // Superhuman Strength
        maxHit *= 1.10;
    } else if (player.getPrayer().prayerActive[14]) { // Ultimate Strength
        maxHit *= 1.15;
    }
    
    // Apply special attack multipliers
    if (player.usingSpecial) {
        maxHit = (int) (maxHit * MeleeData.getSpecialMultiplier(player.playerEquipment[player.playerWeapon]));
    }
    
    // Apply combat style bonuses
    if (player.fightMode == 2) { // Aggressive mode
        maxHit += 3;
    } else if (player.fightMode == 3) { // Controlled mode
        maxHit += 1;
    }
    
    return maxHit;
}
```

#### `rangeMaxHit()`
Calculates maximum ranged damage:

```java
public int rangeMaxHit() {
    int maxHit = 0;
    
    // Base damage from ranged level
    int rangedLevel = player.playerLevel[Constants.RANGED];
    maxHit = (int) (0.5 + rangedLevel * 0.325);
    
    // Add ranged strength bonus
    int rangedStrength = RangeData.getRangedStrength(player);
    maxHit += (rangedStrength * 0.00175 * rangedLevel);
    
    // Apply prayer bonuses
    if (player.getPrayer().prayerActive[3]) { // Sharp Eye
        maxHit *= 1.05;
    } else if (player.getPrayer().prayerActive[11]) { // Hawk Eye
        maxHit *= 1.10;
    } else if (player.getPrayer().prayerActive[19]) { // Eagle Eye
        maxHit *= 1.15;
    }
    
    // Apply special attack effects
    if (player.usingSpecial) {
        maxHit = (int) (maxHit * RangeData.getSpecialMultiplier(player.playerEquipment[player.playerWeapon]));
    }
    
    return maxHit;
}
```

### Combat Timing and Animation

#### `getAttackDelay()`
Calculates attack speed based on weapon:

```java
public int getAttackDelay() {
    int weaponId = player.playerEquipment[player.playerWeapon];
    
    // Default attack speed (4 ticks = 2.4 seconds)
    int delay = 4;
    
    // Get weapon-specific attack speed
    delay = MeleeData.getAttackDelay(weaponId);
    
    // Apply special attack speed modifications
    if (player.usingSpecial) {
        delay = MeleeData.getSpecialAttackDelay(weaponId);
    }
    
    return delay;
}
```

#### `getPlayerAnimIndex()`
Sets appropriate combat animations:

```java
public void getPlayerAnimIndex() {
    int weaponId = player.playerEquipment[player.playerWeapon];
    
    if (player.usingBow) {
        player.animNumber = RangeData.getBowAnimation(weaponId);
    } else if (player.usingRangeWeapon) {
        player.animNumber = RangeData.getRangeAnimation(weaponId);
    } else if (player.usingMagic) {
        player.animNumber = MagicData.getSpellAnimation(player.spellId);
    } else {
        // Melee animation
        player.animNumber = MeleeData.getWeaponAnimation(weaponId);
        
        if (player.usingSpecial) {
            player.animNumber = MeleeData.getSpecialAnimation(weaponId);
        }
    }
    
    player.animUpdateRequired = true;
    player.updateRequired = true;
}
```

### Combat Distance and Positioning

#### `attackingNpcTick()` / `attackingPlayerTick()`
Manages combat positioning and distance requirements:

```java
public void attackingNpcTick() {
    int npcIndex = player.npcIndex;
    if (npcIndex <= 0 || NpcHandler.npcs[npcIndex] == null) return;
    
    Npc npc = NpcHandler.npcs[npcIndex];
    
    // Check if NPC is still alive
    if (npc.isDead) {
        player.npcIndex = 0;
        player.followNpcId = 0;
        player.faceNpc(0);
        return;
    }
    
    // Check line of sight for projectiles
    if (!PathFinder.isProjectilePathClear(player.getX(), player.getY(), player.heightLevel, 
                                         npc.absX, npc.absY)) {
        return;
    }
    
    // Check attack distance based on combat type
    int requiredDistance = getRequiredDistance();
    
    if (!player.goodDistance(player.getX(), player.getY(), npc.getX(), npc.getY(), requiredDistance)) {
        return; // Too far away
    }
    
    // Stop movement when in range
    if (player.usingMagic || player.usingBow || player.usingRangeWeapon) {
        player.followNpcId = 0;
    }
    player.stopMovement();
}
```

#### `getRequiredDistance()`
Determines attack range based on combat type:

```java
public int getRequiredDistance() {
    if (player.usingMagic || player.usingBow) {
        return 10; // Long range
    } else if (player.usingRangeWeapon) {
        return 4; // Medium range
    } else if (RangeData.usingHally(player)) {
        return 2; // Halberd range
    } else {
        return 1; // Melee range
    }
}
```

### Experience and Rewards

#### Experience Distribution
The combat system awards experience based on damage dealt and combat type:

```java
private void awardMeleeExperience(int damage, int npcIndex) {
    if (damage <= 0) return;
    
    // Skip experience for certain NPCs (pheasants, etc.)
    if (isTrainingDummy(npcIndex)) return;
    
    double expRate = CombatConstants.MELEE_EXP_RATE;
    
    if (player.fightMode == 3) { // Controlled mode - shared XP
        player.getPlayerAssistant().addSkillXP(damage * expRate / 3, Constants.ATTACK);
        player.getPlayerAssistant().addSkillXP(damage * expRate / 3, Constants.STRENGTH);
        player.getPlayerAssistant().addSkillXP(damage * expRate / 3, Constants.DEFENCE);
        player.getPlayerAssistant().addSkillXP(damage * expRate / 3, Constants.HITPOINTS);
    } else {
        // Award XP to selected combat skill
        player.getPlayerAssistant().addSkillXP(damage * expRate, player.fightMode);
        player.getPlayerAssistant().addSkillXP(damage * expRate / 3, Constants.HITPOINTS);
    }
    
    // Refresh skill interfaces
    player.getPlayerAssistant().refreshSkill(player.fightMode);
    player.getPlayerAssistant().refreshSkill(Constants.HITPOINTS);
}
```

## Usage Examples

### Basic Combat Operations
```java
// Check if player is in combat
if (player.getCombatAssistant().inCombat()) {
    player.getPacketSender().sendMessage("You cannot do that while in combat!");
    return;
}

// Attack an NPC
player.getCombatAssistant().attackNpc(npcIndex);

// Attack another player
player.getCombatAssistant().attackPlayer(targetPlayerId);
```

### Combat Calculations
```java
// Get maximum hit potential
int maxMeleeHit = player.getCombatAssistant().meleeMaxHit();
int maxRangedHit = player.getCombatAssistant().rangeMaxHit();

// Check combat requirements
if (!player.getCombatAssistant().checkReqs()) {
    // Cannot attack right now
    return;
}

// Calculate combat level difference
int levelDiff = player.getCombatAssistant().getCombatDifference(
    player.calculateCombatLevel(), 
    target.calculateCombatLevel()
);
```

### Special Attacks
```java
// Check if player has enough special attack energy
if (player.getCombatAssistant().checkSpecAmount(weaponId)) {
    player.usingSpecial = true;
    player.specAmount -= getSpecialCost(weaponId);
    // Perform special attack
}
```

## Performance Considerations

### Optimization Strategies
- **Efficient Distance Calculations**: Use squared distances to avoid expensive square root operations
- **Combat State Caching**: Cache frequently calculated values like max hit
- **Batch Processing**: Group combat updates together
- **Memory Management**: Properly clean up combat references

### Common Pitfalls
- **Null Pointer Exceptions**: Always check for null NPCs and players
- **Infinite Combat Loops**: Ensure proper combat reset conditions
- **Memory Leaks**: Clean up combat timers and references
- **Synchronization Issues**: Handle concurrent combat interactions carefully

## Best Practices

1. **Always validate targets** before initiating combat
2. **Check combat restrictions** (safe areas, combat levels, etc.)
3. **Handle special cases** for unique NPCs and items
4. **Implement proper cooldowns** to prevent combat spam
5. **Award appropriate experience** based on damage and combat type
6. **Use authentic formulas** for damage and accuracy calculations
7. **Handle edge cases** gracefully to prevent crashes

## Related Classes

- [`Player`](Player.md) - Contains CombatAssistant instance
- [`MeleeData`](MeleeData.md) - Melee combat calculations and data
- [`RangeData`](RangeData.md) - Ranged combat mechanics
- [`MagicData`](MagicData.md) - Magic combat and spells
- [`NpcHandler`](NpcHandler.md) - NPC combat interactions
- [`CombatConstants`](CombatConstants.md) - Combat configuration values
