# XmlNode

Package `org.apollo.util.xml`.

Defined in [`2006Scape Server/src/main/java/org/apollo/util/xml/XmlNode.java`](2006Scape Server/src/main/java/org/apollo/util/xml/XmlNode.java).

A class which represents a single node in the DOM tree, maintaining information about its children, attributes, value and name.  @author Graham

```java
* A class which represents a single node in the DOM tree, maintaining information about its children, attributes, value
public XmlNode(String name)
public void addChild(XmlNode child)
public boolean containsAttribute(String name)
public String getAttribute(String name)
public int getAttributeCount()
public Set<String> getAttributeNames()
public Set<Map.Entry<String, String>> getAttributes()
public XmlNode getChild(String name)
public int getChildCount()
public Collection<XmlNode> getChildren()
public String getName()
public String getValue()
public Optional<String> getOptionalValue()
public boolean hasValue()
public Iterator<XmlNode> iterator()
public void removeAllAttributes()
public void removeAllChildren()
public void removeAttribute(String name)
public void removeChild(XmlNode child)
public void removeValue()
public void setAttribute(String name, String value)
public void setName(String name)
public void setValue(String value)
```
