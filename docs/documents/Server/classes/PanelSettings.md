# PanelSettings

Package `com.rs2.gui`.

Defined in [`2006Scape Server/src/main/java/com/rs2/gui/PanelSettings.java`](2006Scape Server/src/main/java/com/rs2/gui/PanelSettings.java).

Panel Settings helper class.

```java
public class PanelSettings {
public static ArrayList<String> npcList = new ArrayList<String>();
public PanelSettings(ControlPanel p)
public static String trim(String str)
public String getSelectedPlayer()
public boolean inList(String id)
public String getColor(String color)
public Client getClient(String name)
public Client getClient(int id)
public boolean validClient(int id)
public boolean validClient(String name)
public boolean validClient(Client c)
public boolean validNpc(int index)
public int getEntity(String name)
public Npc getNpc(int index)
public String getInput(String title, String msg)
public int getInt(String title, String msg)
public void executeCommand(String cmd)
public void playerCommand(String cmd, Client c)
public Location(int x, int y, int z)
public static Location getLocationByName(String name)
public void update(int x, int y, int z)
public int getX()
public int getY()
public int getZ()
```
