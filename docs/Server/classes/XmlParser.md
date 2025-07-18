# XmlParser

Package `org.apollo.util.xml`.

Defined in [`2006Scape Server/src/main/java/org/apollo/util/xml/XmlParser.java`](2006Scape Server/src/main/java/org/apollo/util/xml/XmlParser.java).

A simple XML parser that uses the internal {@link org.xml.sax} API to create a tree of {@link XmlNode} objects.  @author Graham

```java
public final class XmlParser {
public void characters(char[] ch, int start, int length) throws SAXException
public void endElement(String uri, String localName, String qName) throws SAXException
public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
public XmlParser() throws SAXException
public XmlNode parse(InputStream is) throws IOException, SAXException
public XmlNode parse(Reader reader) throws IOException, SAXException
```
