# DialogueHandler

Package `com.rs2.game.dialogues`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/dialogues/DialogueHandler.java`](2006Scape Server/src/main/java/com/rs2/game/dialogues/DialogueHandler.java).

Handles dialogue related functionality.

```java
public class DialogueHandler {
public DialogueHandler(Player player2)
public void endDialogue()
public void setOptionId(int id)
public void setNextDialogue(int id)
public void sendDialogues(int dialogue, int npcId)
public void sendDialogues2(int dialogue, int npcId)
public void clearChatBoxText(Player c)
public void sendStartInfo(String text, String text1, String text2, String text3, String title, boolean send)
public void sendPlayerChat(String... line)
public void sendPlayerChat(ChatEmotes e, String... line)
public void sendOption(String... line)
public void sendStatement(String... line)
public void itemMessage(String title, String message, int itemid, int size)
public void sendNpcChat(int npcId, ChatEmotes e, String... line)
public void sendNpcChat1(String s, int ChatNpc, String name)
public void sendNpcChat2(String s, String s1, int ChatNpc, String name)
public void itemMessage(String message1, int itemid, int size)
public void sendItemChat(int item, int zoom, String header, String... line)
```
