# core.managers.OnDemandFetcher

Defined in [`2006Scape Client/src/main/java/core.managers.OnDemandFetcher.java`](2006Scape Client/src/main/java/core.managers.OnDemandFetcher.java).

On Demand Fetcher helper class.

```java
public final class core.managers.OnDemandFetcher extends core.managers.OnDemandFetcherParent implements Runnable {
public void start(core.network.StreamLoader streamLoader, core.engine.Game client1)
public int getNodeCount()
public void disable()
public void requestMapFiles(boolean flag)
public int getVersionCount(int j)
public int getAnimCount()
public void queueRequest(int i, int j)
public int getModelIndex(int i)
public void run()
public void requestFileNow(int i, int j)
public core.managers.OnDemandData getNextNode()
public int getRegionArchiveId(int type, int regionX, int regionY)
public void requestModel(int modelId)
public void validateOrQueue(byte byte0, int i, int j)
public boolean hasLandscape(int i)
public void clearPriorityQueue()
public boolean isMidiRequired(int i)
public core.managers.OnDemandFetcher()
```
