## LOG-AGENT

fork自三梦大佬的仓库，如其名，主要是用于观察是否有关注的方法被调用，感觉更像是用来观测sink点是否被调用？应该是自动化挖掘漏洞偏半自动的一种思路探索吧....

其原理就是通过Java Agent + javassist把关注的一些sink类或者sink包中的所有的方法都给加一个调用的日志打印，然后通过各种输入来观测是否触发了sink方法，比静态挖掘要准确一些，但是感觉还不如直接上iast...

---

利用agent hook指定的class，在jar运行周期内，用于跟踪被执行的方法，辅助做一些事情，比如挖洞啊

这样子，就不用干看代码的啦，说不定一运行就能找到漏洞的啦，想想3分钟一个CVE就激动啦

### 编译jar
```
mvn clean compile assembly:single
```

### 运行
```
java -javaagent:/Users/threedr3am/log-agent.jar="^org\.aaa\.bbb$" -jar bug-test-env-1-1.0-SNAPSHOT.jar
```
1. /Users/threedr3am/log-agent.jar: 编译出来的agent jar
2. "^org\.aaa\.bbb$": 双引号内为需要hock的class name匹配正则
3. bug-test-env-1-1.0-SNAPSHOT.jar: 运行的jar

### 辅助挖洞比较实用的正则
```
(org\.aaa\.bbb)|(java\.io\.ObjectInputStream)|(sun\.rmi\.registry)|(com\.sun\.jndi)|(javax\.naming\.InitialContext)|(org\.hibernate\.validator\.internal\.engine\.constraintvalidation\.ConstraintValidatorContextImpl)|(org\.springframework\.expression)|(javax\.el)|(org\.springframework\.jdbc\.core\.StatementCallback)|(javax\.xml\.parsers\.DocumentBuilder)|(org\.jdom\.input\.SAXBuilder)|(javax\.xml\.parsers\.SAXParser)|(org\.dom4j\.io\.SAXReader)|(javax\.xml\.transform\.sax\.SAXTransformerFactory)|(javax\.xml\.validation\.SchemaFactory)|(javax\.xml\.transform\.Transformer)|(javax\.xml\.bind\.Unmarshaller)|(javax\.xml\.validation\.Validator)|(org\.xml\.sax\.XMLReader)|(java\.lang\.Runtime)|(java\.lang\.ProcessBuilder)|(java\.beans\.XMLDecoder)|(org\.yaml\.snakeyaml\.Yaml)|(java\.net\.URL)|(com\.fasterxml\.jackson\.databind\.ObjectMapper)|(com\.alibaba\.fastjson\.JSON)
```
- org\.aaa\.bbb: 改成当前运行jar能匹配上所有class的包名（因为这样能知道当前服务的执行栈信息，更好的定位漏洞）





