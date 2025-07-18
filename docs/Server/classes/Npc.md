# Npc

Package `com.rs2.game.npcs`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/npcs/Npc.java`](2006Scape Server/src/main/java/com/rs2/game/npcs/Npc.java).

Npc helper class.

```java
public class Npc {
public Npc(int _npcId, int _npcType)
public void requestTransform(int id)
public String name()
public void shearSheep(Player player, int itemNeeded, int itemGiven, int animation, final int currentId, final int newId, int transformTime)
public void execute(CycleEventContainer container)
public void stop()
public void appendTransformUpdate(Stream str)
public void updateNPCMovement(Stream str)
public void forceChat(String text)
public void appendMask80Update(Stream str)
public void gfx100(int gfx)
public void gfx0(int gfx)
public void appendAnimUpdate(Stream str)
public int startAnimation(int anim, int npcId)
public void turnNpc(int i, int j)
public int getNextWalkingDirection2()
public void getRandomAndHomeNPCWalking(int i)
public void appendFaceEntity(Stream str)
public void facePlayer(Player player)
public void appendFaceToUpdate(Stream str)
public void appendNPCUpdateBlock(Stream str)
public void clearUpdateFlags()
public int getNextWalkingDirection()
public void getNextNPCMovement(int i)
public void appendHitUpdate(Stream str)
public void appendHitUpdate2(Stream str)
public void handleHitMask(int damage)
public int getX()
public int getY()
public int getLastX()
public int getLastY()
public void setAbsX(int absX)
public void setAbsY(int absY)
public void deleteNPC(Npc npc)
public boolean inLesserNpc()
public boolean inMulti()
public boolean inWild() {// beg, end, beg, end, beg, end, beg, end
```
