# Connection

Package `com.rs2`.

Defined in [`2006Scape Server/src/main/java/com/rs2/Connection.java`](2006Scape Server/src/main/java/com/rs2/Connection.java).

Connection Check Class  @author Ryan / Lmctruck30

```java
public class Connection {
public static ArrayList<String> bannedIps = new ArrayList<String>();
public static ArrayList<String> bannedNames = new ArrayList<String>();
public static ArrayList<String> mutedIps = new ArrayList<String>();
public static ArrayList<String> mutedNames = new ArrayList<String>();
public static ArrayList<String> loginLimitExceeded = new ArrayList<String>();
public static void initialize()
public static void addIpToLoginList(String IP)
public static void removeIpFromLoginList(String IP)
public static void clearLoginList()
public static boolean checkLoginList(String IP)
public static void unMuteUser(String name)
public static void unIPMuteUser(String name)
public static void addIpToBanList(String IP)
public static void addIpToMuteList(String IP)
public static void removeIpFromBanList(String IP)
public static boolean isIpBanned(String IP)
public static void addNameToBanList(String name)
public static void addNameToMuteList(String name)
public static void removeNameFromBanList(String name)
public static void removeNameFromMuteList(String name)
public static void deleteFromFile(String file, String name)
public static boolean isNamedBanned(String name)
public static void banUsers()
public static void muteUsers()
public static void banIps()
public static void muteIps()
public static void addNameToFile(String Name)
public static void addUserToFile(String Name)
public static void addIpToFile(String Name)
public static void addIpToMuteFile(String Name)
public static boolean isMuted(Player player)
```
