# IsaacRandom

Package `org.apollo.util.security`.

Defined in [`2006Scape Server/src/main/java/org/apollo/util/security/IsaacRandom.java`](2006Scape Server/src/main/java/org/apollo/util/security/IsaacRandom.java).

<p> An implementation of the <a href="http://www.burtleburtle.net/bob/rand/isaacafa.html">ISAAC</a> psuedorandom number generator. </p>  <pre> ------------------------------------------------------------------------------ Rand.java: By Bob Jenkins.  My random number generator, ISAAC. rand.init() -- initialize rand.val()  -- get a random value MODIFIED: 960327: Creation (addition of randinit, really) 970719: use context, not global variables, for internal state 980224: Translate to Java ------------------------------------------------------------------------------ </pre> <p> This class has been changed to be more conformant to Java and javadoc conventions. </p>  @author Bob Jenkins

```java
* This class has been changed to be more conformant to Java and javadoc conventions.
public IsaacRandom(int[] seed)
private void isaac()
private void init()
public int nextInt()
```
