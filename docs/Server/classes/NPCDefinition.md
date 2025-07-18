# NpcDefinition

Data holder describing a non-player character. Definitions are loaded from
`npcDefinitions.json` on startup and cached for quick lookup via
`NPCDefinition.forId(int)`.

Source: [NPCDefinition.java](../../2006Scape%20Server/src/main/java/com/rs2/game/npcs/NPCDefinition.java)

```java
NPCDefinition goblin = NPCDefinition.forId(1);
System.out.println(goblin.getName());
```
