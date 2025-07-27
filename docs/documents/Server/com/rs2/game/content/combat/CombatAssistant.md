# CombatAssistant

**Package:** `com.rs2.game.content.combat`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/content/combat/CombatAssistant.java`](2006Scape Server/src/main/java/com/rs2/game/content/combat/CombatAssistant.java)

## Overview

The `CombatAssistant` class is the core combat system for players in the 2006Scape server. It handles all aspects of player combat including melee, ranged, and magic attacks against NPCs and other players. This class manages damage calculations, hit accuracy, special attacks, combat experience, and various combat mechanics like prayer effects, equipment bonuses, and combat styles.

## Key Responsibilities

- **Combat Processing**: Managing all forms of player combat (melee, ranged, magic)
- **Damage Calculations**: Computing damage values based on stats, equipment, and bonuses
- **Hit Accuracy**: Determining whether attacks hit or miss based on combat formulas
- **Experience Distribution**: Awarding appropriate experience for combat actions
- **Special Attacks**: Handling weapon special attacks and their effects
- **Combat States**: Managing combat timers, delays, and player combat status
- **Equipment Effects**: Processing set effects like Barrows armor bonuses
- **Prayer Integration**: Applying prayer bonuses and effects to combat

## Core Architecture

### game.entities.Player Association
```java
private final game.entities.Player player;

public CombatAssistant(game.entities.Player player) {
    this.player = player;
}
```

Each CombatAssistant instance is tied to a specific player, managing their combat interactions.

## Core Methods

### Combat State Management

#### `inCombat()`
Checks if the player is currently in combat:

```java
public boolean inCombat() {
    return (player.underAttackBy > 0 || player.underAttackBy2 > 0);
}
```

**Returns:** `true` if player is under attack by NPCs or other players

### game.entities.NPC Combat Processing

#### `delayedHit(int i)`
Processes delayed combat hits against NPCs:

```java
public void delayedHit(int npcIndex) {
    if (NpcHandler.npcs[npcIndex] == null || NpcHandler.npcs[npcIndex].isDead) {
        player.npcIndex = 0;
        return;
    }
    
    Npc npc = NpcHandler.npcs[npcIndex];
    
    // Handle game.entities.NPC block animation
    if (npc.attackTimer <= 3 || (npc.attackTimer == 0 && npc.hitDelayTimer > 0 && !player.castingMagic)) {
        npc.animNumber = NpcEmotes.getBlockEmote(npcIndex);
        npc.animUpdateRequired = true;
        npc.updateRequired = true;
    }
    
    // Play combat sounds
    if (CombatConstants.COMBAT_SOUNDS) {
        if (!PestControl.npcIsPCMonster(npc.npcType) && !PestControl.isPCPortal(npc.npcType)) {
            player.getPacketSender().sendSound(
                CombatSounds.getNpcBlockSound(npc.npcType), 100, 0);
        }
    }
    
    // Make game.entities.NPC face the player
    npc.facePlayer(player);
    
    // Set killer ID for loot rights
    if ((npc.underAttackBy > 0 && GameEngine.npcHandler.getsPulled(player, npcIndex)) ||
        (npc.underAttackBy < 0 && !GameEngine.npcHandler.getsPulled(player, npcIndex))) {
        npc.killerId = player.playerId;
    }
    
    player.lastNpcAttacked = npcIndex;
    
    // Process different combat types
    if (player.projectileStage == 0) {
        // Melee combat
        applyNpcMeleeDamage(npcIndex, 1);
        if (player.doubleHit) {
            applyNpcMeleeDamage(npcIndex, 2);
        }
    } else if (!player.castingMagic && player.projectileStage > 0) {
        // Ranged combat
        processRangedAttack(npcIndex);
    } else if (player.projectileStage > 0) {
        // Magic combat
        processMagicAttack(npcIndex);
    }
    
    // Reset combat state
    resetCombatState();
}
```

### Melee Combat

#### `applyNpcMeleeDamage(int npcIndex, int damageMask)`
Applies melee damage to an game.entities.NPC:

```java
public void applyNpcMeleeDamage(int npcIndex, int damageMask) {
    Npc npc = NpcHandler.npcs[npcIndex];
    int damage = Misc.random(meleeMaxHit());
    
    // Check for Verac's set effect (ignores defense)
    boolean fullVeracsEffect = player.getPlayerAssistant().fullVeracs() && Misc.random(3) == 1;
    
    // Cap damage to remaining HP
    if (npc.HP - damage < 0) {
        damage = npc.HP;
    }
    
    // Calculate hit accuracy
    if (!fullVeracsEffect) {
        if (Misc.random(npc.defence) > 10 + Misc.random(calcAtt())) {
            damage = 0; // Attack missed
        } else if (npc.npcType == DAGANNOTH_PRIME || npc.npcType == DAGANNOTH_REX) {
            damage = 0; // Immune to melee
        }
    }
    
    player.globalDamageDealt += damage;
    
    // Handle special boss mechanics (TzTok-Jad healers)
    if (npc.npcType == FightCaves.TZTOK_JAD && npc.spawnedBy == player.getId()) {
        int halfHp = FightCaves.getHp(FightCaves.TZTOK_JAD) / 2;
        if (npc.HP > halfHp && npc.HP - damage < halfHp && player.canHealersRespawn) {
            FightCaves.spawnHealers(player, npcIndex, 4 - player.spawnedHealers);
        }
    }
    
    // Check for Guthan's set effect (healing)
    boolean guthansEffect = false;
    if (player.getPlayerAssistant().fullGuthans() && Misc.random(3) == 1) {
        guthansEffect = true;
        int healAmount = damage;
        if (player.playerLevel[Constants.HITPOINTS] + healAmount >= 
            player.getPlayerAssistant().getLevelForXP(player.playerXP[Constants.HITPOINTS])) {
            player.playerLevel[Constants.HITPOINTS] = 
                player.getPlayerAssistant().getLevelForXP(player.playerXP[Constants.HITPOINTS]);
        } else {
            player.playerLevel[Constants.HITPOINTS] += healAmount;
        }
        player.getPlayerAssistant().refreshSkill(Constants.HITPOINTS);
        player.gfx0(398); // Guthan's heal effect
    }
    
    // Award experience based on combat style
    awardMeleeExperience(damage);
    
    // Apply damage to game.entities.NPC
    if (damageMask == 1) {
        npc.hitDiff = damage;
        npc.hitUpdateRequired = true;
    } else {
        npc.hitDiff2 = damage;
        npc.hitUpdateRequired2 = true;
    }
    
    npc.HP -= damage;
    npc.underAttack = true;
    npc.updateRequired = true;
    
    player.totalDamageDealt += damage;
    player.killingNpcIndex = player.oldNpcIndex;
}
```

### Ranged Combat

#### `processRangedAttack(int npcIndex)`
Processes ranged attacks against NPCs:

```java
private void processRangedAttack(int npcIndex) {
    Npc npc = NpcHandler.npcs[npcIndex];
    int damage = Misc.random(rangeMaxHit());
    int damage2 = -1;
    
    // Handle multi-hit weapons (Dark bow, special attacks)
    if (player.lastWeaponUsed == 11235 || player.bowSpecShot == 1) {
        damage2 = Misc.random(rangeMaxHit());
    }
    
    // Handle special arrow effects
    boolean ignoreDef = false;
    if (Misc.random(5) == 1 && player.lastArrowUsed == 9243) { // Dragon arrows
        ignoreDef = true;
        npc.gfx0(758);
    }
    
    // Calculate hit accuracy
    if (Misc.random(npc.defence) > Misc.random(10 + calculateRangeAttack()) && !ignoreDef ||
        (npc.npcType == DAGANNOTH_SUPREME || npc.npcType == DAGANNOTH_REX && !ignoreDef)) {
        damage = 0;
    }
    
    // Handle special arrow effects
    if (Misc.random(4) == 1 && player.lastArrowUsed == 9242 && damage > 0) { // Ruby bolts
        npc.gfx0(754);
        damage = npc.HP / 5; // 20% of target's HP
        // game.entities.Player takes damage too
        player.handleHitMask(player.playerLevel[Constants.HITPOINTS] / 10);
        player.dealDamage(player.playerLevel[Constants.HITPOINTS] / 10);
        player.gfx0(754);
    }
    
    // Second hit accuracy for multi-hit weapons
    if (player.lastWeaponUsed == 11235 || player.bowSpecShot == 1) {
        if (Misc.random(npc.defence) > Misc.random(10 + calculateRangeAttack())) {
            damage2 = 0;
        }
    }
    
    // Diamond arrows effect
    if (damage > 0 && Misc.random(5) == 1 && player.lastArrowUsed == 9244) {
        damage *= 1.45;
        npc.gfx0(756);
    }
    
    // Cap damage and handle death
    if (npc.HP - damage < 0) {
        damage = npc.HP;
    }
    if (npc.HP - damage <= 0 && damage2 > 0) {
        damage2 = 0;
    }
    
    player.globalDamageDealt += damage;
    if (damage2 > 0) {
        player.globalDamageDealt += damage2;
    }
    
    // Award ranged experience
    awardRangedExperience(damage);
    
    // Handle Pest Control damage tracking
    if (damage > 0 && (PestControl.npcIsPCMonster(npc.npcType) || PestControl.isPCPortal(npc.npcType))) {
        player.pcDamage += damage;
    }
    
    // Drop arrows (if applicable)
    boolean dropArrows = true;
    for (int noArrowId : RangeData.NO_ARROW_DROP) {
        if (player.lastWeaponUsed == noArrowId) {
            dropArrows = false;
            break;
        }
    }
    if (dropArrows) {
        player.getItemAssistant().dropArrowNpc();
    }
    
    // Apply damage to game.entities.NPC
    npc.underAttack = true;
    npc.hitDiff = damage;
    npc.HP -= damage;
    npc.hitUpdateRequired = true;
    
    if (damage2 > -1) {
        npc.hitDiff2 = damage2;
        npc.HP -= damage2;
        npc.hitUpdateRequired2 = true;
        player.totalDamageDealt += damage2;
    }
    
    player.totalDamageDealt += damage;
    player.killingNpcIndex = player.oldNpcIndex;
    npc.updateRequired = true;
}
```

### Magic Combat

#### `processMagicAttack(int npcIndex)`
Processes magic attacks against NPCs:

```java
private void processMagicAttack(int npcIndex) {
    Npc npc = NpcHandler.npcs[npcIndex];
    int damage = Misc.random(MagicData.MAGIC_SPELLS[player.oldSpellId][6]);
    
    // Handle god spell charge effect
    if (MagicSpells.godSpells(player)) {
        if (System.currentTimeMillis() - player.godSpellDelay < CombatConstants.GOD_SPELL_CHARGE) {
            damage += Misc.random(10);
        }
    }
    
    boolean magicFailed = false;
    int bonusAttack = getBonusAttack(npcIndex);
    
    // Calculate hit accuracy
    if (Misc.random(npc.defence) > 10 + Misc.random(mageAtk()) + bonusAttack) {
        damage = 0;
        magicFailed = true;
    } else if (npc.npcType == DAGANNOTH_SUPREME || npc.npcType == DAGANNOTH_PRIME) {
        damage = 0;
        magicFailed = true;
    }
    
    // Cap damage
    if (npc.HP - damage < 0) {
        damage = npc.HP;
    }
    
    // Award magic experience
    player.getPlayerAssistant().addSkillXP(
        MagicData.MAGIC_SPELLS[player.oldSpellId][7] + damage * CombatConstants.MAGIC_EXP_RATE, 
        Constants.MAGIC);
    
    // Award HP experience (except for certain spells)
    if (!isNonDamageSpell(player.oldSpellId)) {
        player.getPlayerAssistant().addSkillXP(
            damage * CombatConstants.MAGIC_EXP_RATE / 3, Constants.HITPOINTS);
    }
    
    player.getPlayerAssistant().refreshSkill(Constants.HITPOINTS);
    player.getPlayerAssistant().refreshSkill(Constants.MAGIC);
    
    // Handle Pest Control damage tracking
    if (damage > 0 && (PestControl.npcIsPCMonster(npc.npcType) || PestControl.isPCPortal(npc.npcType))) {
        player.pcDamage += damage;
    }
    
    // Apply spell effects
    if (MagicSpells.getEndGfxHeight(player) == 100 && !magicFailed) {
        npc.gfx100(MagicData.MAGIC_SPELLS[player.oldSpellId][5]);
    } else if (!magicFailed) {
        npc.gfx0(MagicData.MAGIC_SPELLS[player.oldSpellId][5]);
    }
    
    if (magicFailed) {
        npc.gfx100(85); // Magic splash effect
    }
    
    if (!magicFailed) {
        // Handle freeze spells
        int freezeDelay = MagicSpells.getFreezeTime(player);
        if (freezeDelay > 0 && npc.freezeTimer == 0) {
            npc.freezeTimer = freezeDelay;
        }
        
        // Handle blood spells (healing effect)
        switch (MagicData.MAGIC_SPELLS[player.oldSpellId][0]) {
            case 12901: case 12919: case 12911: case 12929: // Blood spells
                int heal = Misc.random(damage / 2);
                int maxHp = player.getPlayerAssistant().getLevelForXP(player.playerXP[Constants.HITPOINTS]);
                if (player.playerLevel[Constants.HITPOINTS] + heal >= maxHp) {
                    player.playerLevel[Constants.HITPOINTS] = maxHp;
                } else {
                    player.playerLevel[Constants.HITPOINTS] += heal;
                }
                player.getPlayerAssistant().refreshSkill(Constants.HITPOINTS);
                break;
        }
    }
    
    // Apply damage to game.entities.NPC
    npc.underAttack = true;
    if (MagicData.MAGIC_SPELLS[player.oldSpellId][6] != 0) {
        npc.hitDiff = damage;
        npc.HP -= damage;
        npc.hitUpdateRequired = true;
        player.totalDamageDealt += damage;
    }
    
    player.killingNpcIndex = player.oldNpcIndex;
    npc.updateRequired = true;
    
    // Reset magic state
    player.usingMagic = false;
    player.castingMagic = false;
    player.oldSpellId = 0;
}
```

### Combat Calculations

#### `meleeMaxHit()`
Calculates maximum melee damage:

```java
public int meleeMaxHit() {
    int maxHit = 0;
    int strength = player.playerLevel[Constants.STRENGTH];
    int combatStyleBonus = 0;
    
    // Combat style bonuses
    if (player.fightMode == 3) { // Aggressive
        combatStyleBonus = 3;
    } else if (player.fightMode == 1) { // Accurate
        combatStyleBonus = 1;
    }
    
    // Calculate base max hit
    maxHit = (int) (0.5 + strength * (player.playerBonus[10] + 64) / 640.0);
    
    // Apply combat style bonus
    maxHit += combatStyleBonus;
    
    // Apply prayer bonuses
    if (player.getPrayer().prayerActive[1]) { // Burst of Strength
        maxHit *= 1.05;
    } else if (player.getPrayer().prayerActive[6]) { // Superhuman Strength
        maxHit *= 1.10;
    } else if (player.getPrayer().prayerActive[14]) { // Ultimate Strength
        maxHit *= 1.15;
    }
    
    // Apply special attack bonuses
    if (player.usingSpecial) {
        maxHit = MeleeMaxHit.specialMaxHit(player, maxHit);
    }
    
    return maxHit;
}
```

#### `rangeMaxHit()`
Calculates maximum ranged damage:

```java
public int rangeMaxHit() {
    int rangeLevel = player.playerLevel[Constants.RANGED];
    int rangeBonus = player.playerBonus[4]; // Ranged strength bonus
    
    // Base calculation
    int maxHit = (int) (0.5 + rangeLevel * (rangeBonus + 64) / 640.0);
    
    // Apply prayer bonuses
    if (player.getPrayer().prayerActive[3]) { // Sharp Eye
        maxHit *= 1.05;
    } else if (player.getPrayer().prayerActive[11]) { // Hawk Eye
        maxHit *= 1.10;
    } else if (player.getPrayer().prayerActive[19]) { // Eagle Eye
        maxHit *= 1.15;
    }
    
    // Apply special attack bonuses
    if (player.usingSpecial) {
        maxHit = RangeMaxHit.specialMaxHit(player, maxHit);
    }
    
    return maxHit;
}
```

### Experience Distribution

#### `awardMeleeExperience(int damage)`
Awards experience for melee combat:

```java
private void awardMeleeExperience(int damage) {
    switch (player.fightMode) {
        case 0: // Accurate (Attack XP)
            player.getPlayerAssistant().addSkillXP(damage * CombatConstants.MELEE_EXP_RATE, Constants.ATTACK);
            break;
        case 1: // Aggressive (Strength XP)
            player.getPlayerAssistant().addSkillXP(damage * CombatConstants.MELEE_EXP_RATE, Constants.STRENGTH);
            break;
        case 2: // Defensive (Defence XP)
            player.getPlayerAssistant().addSkillXP(damage * CombatConstants.MELEE_EXP_RATE, Constants.DEFENCE);
            break;
        case 3: // Controlled (Shared XP)
            int sharedXP = damage * CombatConstants.MELEE_EXP_RATE / 3;
            player.getPlayerAssistant().addSkillXP(sharedXP, Constants.ATTACK);
            player.getPlayerAssistant().addSkillXP(sharedXP, Constants.STRENGTH);
            player.getPlayerAssistant().addSkillXP(sharedXP, Constants.DEFENCE);
            break;
    }
    
    // Always award HP experience
    player.getPlayerAssistant().addSkillXP(damage * CombatConstants.MELEE_EXP_RATE / 3, Constants.HITPOINTS);
    
    // Refresh skill interfaces
    player.getPlayerAssistant().refreshSkill(Constants.ATTACK);
    player.getPlayerAssistant().refreshSkill(Constants.STRENGTH);
    player.getPlayerAssistant().refreshSkill(Constants.DEFENCE);
    player.getPlayerAssistant().refreshSkill(Constants.HITPOINTS);
}
```

#### `awardRangedExperience(int damage)`
Awards experience for ranged combat:

```java
private void awardRangedExperience(int damage) {
    if (player.fightMode == 3) { // Long range (shared)
        player.getPlayerAssistant().addSkillXP(damage * CombatConstants.RANGE_EXP_RATE / 2, Constants.RANGED);
        player.getPlayerAssistant().addSkillXP(damage / 2, Constants.DEFENCE);
        player.getPlayerAssistant().addSkillXP(damage / 3, Constants.HITPOINTS);
        player.getPlayerAssistant().refreshSkill(Constants.DEFENCE);
    } else {
        player.getPlayerAssistant().addSkillXP(damage * CombatConstants.RANGE_EXP_RATE, Constants.RANGED);
        player.getPlayerAssistant().addSkillXP(damage * CombatConstants.RANGE_EXP_RATE / 3, Constants.HITPOINTS);
    }
    
    player.getPlayerAssistant().refreshSkill(Constants.HITPOINTS);
    player.getPlayerAssistant().refreshSkill(Constants.RANGED);
}
```

## Usage Examples

### Basic Combat Checks
```java
// Check if player is in combat
if (player.getCombatAssistant().inCombat()) {
    player.getPacketSender().sendMessage("You can't do that while in combat!");
    return;
}

// Process delayed hit on game.entities.NPC
player.getCombatAssistant().delayedHit(npcIndex);
```

### Combat Calculations
```java
// Get maximum damage values
int meleeMax = player.getCombatAssistant().meleeMaxHit();
int rangedMax = player.getCombatAssistant().rangeMaxHit();

// Calculate attack accuracy
int attackRoll = player.getCombatAssistant().calcAtt();
int rangeAttack = player.getCombatAssistant().calculateRangeAttack();
```

### Special Attack Processing
```java
// Check if player can use special attack
if (player.usingSpecial && player.specAmount >= 25) {
    // Process special attack
    player.getCombatAssistant().delayedHit(targetNpc);
    player.specAmount -= 25;
}
```

## Performance Considerations

### Optimization Strategies
- **Efficient Calculations**: Cache frequently used values
- **Batch Processing**: Group similar combat operations
- **Memory Management**: Clean up combat state properly
- **Network Optimization**: Minimize unnecessary packet sends

### Resource Management
- **Combat Timers**: Properly manage combat delays and cooldowns
- **Experience Calculation**: Optimize XP distribution algorithms
- **Effect Processing**: Efficiently handle special effects and bonuses

## Best Practices

1. **Always validate combat state** before processing attacks
2. **Check target validity** before applying damage
3. **Handle special cases** for unique NPCs and equipment
4. **Award appropriate experience** based on combat style
5. **Apply prayer and equipment bonuses** correctly
6. **Handle combat sounds and effects** appropriately
7. **Integrate with minigame systems** when applicable

## Integration Points

### game.entities.Player Integration
```java
// Combat assistant is part of every player
game.entities.Player player = new game.entities.Player();
CombatAssistant combat = player.getCombatAssistant();
```

### game.entities.NPC Integration
```java
// Combat affects game.entities.NPC state
NpcHandler.npcs[i].underAttack = true;
NpcHandler.npcs[i].HP -= damage;
```

### Equipment Integration
```java
// Equipment affects combat calculations
int strengthBonus = player.playerBonus[10];
boolean hasSpecialAttack = player.usingSpecial;
```

## Related Classes

- [`game.entities.Player`](game.entities.Player.md) - Contains CombatAssistant instance
- [`NpcHandler`](NpcHandler.md) - Manages combat targets
- [`MeleeData`](MeleeData.md) - Melee combat constants and data
- [`RangeData`](RangeData.md) - Ranged combat constants and data
- [`MagicData`](MagicData.md) - Magic combat constants and data
- [`CombatConstants`](CombatConstants.md) - Combat system constants
- [`PrayerDrain`](PrayerDrain.md) - Prayer effects on combat