# How to include a plugin module

### config modules to be built by gradle

config the modules to be included by `IntellijPluginModules` in [gradle.properties](./gradle.properties), and separate the modules by comma:

```properties
IntellijPluginModules=azure-intellij-plugin-springcloud,azure-sdk-reference-book
```

### config plugin fragments to be imported by plugin

config the modules to be included in [plugin.xml](./src/main/resources/META-INF/plugin.xml)

```xml
<xi:include href="/META-INF/azure-intellij-plugin-lib.xml" xpointer="xpointer(/idea-plugin/*)"/>
<xi:include href="/META-INF/azure-sdk-reference-book.xml" xpointer="xpointer(/idea-plugin/*)"/>
<xi:include href="/META-INF/azure-intellij-plugin-service-explorer.xml" xpointer="xpointer(/idea-plugin/*)"/>
<xi:include href="/META-INF/azure-intellij-plugin-springcloud.xml" xpointer="xpointer(/idea-plugin/*)"/>
```