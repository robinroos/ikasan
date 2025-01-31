![IKASAN](../../developer/docs/quickstart-images/Ikasan-title-transparent.png)
## Converters

### Purpose

<img src="../../developer/docs/quickstart-images/message-translator.png" width="200px" align="left">The main responsibility of a converter is to convert from one POJO type to another. Coverter acts as an adapter between components requiring different input types.
Read more about EIP [Translators](http://www.enterpriseintegrationpatterns.com/patterns/messaging/MessageTranslator.html)
In order to create your own converter you need to implement [Converter Interface](../spec/component/src/main/java/org/ikasan/spec/component/transformation/Converter.java)
<br/>

### Types
- [JSON to XML Converter](/converter/JsonToXmlConverter.md)
- [Map Message to Object Converter](/converter/MapMessageToObjectConverter.md)
- [Map Message to Payload Converter](/converter/MapMessageToPayloadConverter.md)
- [Object Message to Object Converter](/converter/ObjectMessageToObjectConverter.md)
- [Object to XML String Converter](/converter/ObjectToXmlStringConverter.md)
- [Payload to XML Document Converter](/converter/PayloadToXmlDocumentConverter.md)
- [Text Message to String Converter](/converter/TextMessageToStringConverter.md)
- [Threadsafe XSLT Converter](/converter/ThreadSafeXsltConverter.md)
- [XML Byte Array to Object Converter](/converter/XmlByteArrayToObjectConverter.md)
- [XML String to Object Converter](/converter/XmlStringToObjectConverter.md)
- [XML to JSON Converter](/converter/XmlToJsonConverter.md)
- [XSLT Configuration Parameter Converter](/converter/XsltConfigurationParameterConverter.md)
- [XSLT Converter](/converter/XsltConverter.md)
