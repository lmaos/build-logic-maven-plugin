# 逻辑插件

Maven 插件，用于在 Maven 构建过程中执行逻辑。 比如： 创建目录， 压缩文件， 和一些文件操作， 逻辑操作， Http上传等。

比如构建项目发布压缩包示范: 
```xml
<main>
    <!-- 创建用于发布的目录 -->
    <mkdir path="${project.basedir}/publish" />
    <mkdir path="${project.basedir}/publish/scripts" />
    <mkdir path="${project.basedir}/publish/nginx" />
    
    <file name="appJarFile" path="${project.basedir}/target/app.jar" />
    <!-- 复制 app.jar 到 publish 目录下 -->
    <file name="appJarFileWrite" path="${project.basedir}/publish/app.jar" />
    <!-- 读取appJarFile文件内容， 到目标appJarFileWrite文件, overwrite="true" 表示覆盖-->
    <write ref="appJarFile" file="appJarFileWrite"  overwrite="true" />
    <!-- 初始化一个默认启动脚本 -->
    <file name="startShell" file="${project.basedir}/publish/start.sh" />
    <!-- 写入默认启动脚本内容 -->
    <write file="startShell" ><![CDATA[
    #!/bin/sh
    
    APP_NAME=app
    SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
    pkill -f "${APP_NAME}.jar"
    sleep 3
    nohup java "-Xms1g -Xmx1g" -jar "${SCRIPT_DIR}/${APP_NAME}.jar" > "${SCRIPT_DIR}/${APP_NAME}.log" 2>&1 &
    ]]></write>
    
    <!-- 压缩目录 -->
    <date name="versionDate" format="yyyyMMddHHmmss" />
    <!-- 创建 zip 文件变量 -->
    <file name="appZipFile" path="${project.basedir}/app-${versionDate}.zip"/>
    <!-- 压缩目录到 zip 文件 -->
    <zip file="appZipFile">
        <entry dir="${project.basedir}/publish"/>
    </zip>
    
    <if test="appZipFile" >
        <then>
        <!-- 压缩包进行上传 -->
        <http url="http://localhost:8080/build/success?filename=${appZipFile.getName()}" method="POST" >
            <header name="Content-Type" value="application/octet-stream" />
            <content ref="appZipFile" />
            <response to="response">
                <echo>${response}</echo>
                <echo test="response.statusCode == 200">上传成功</echo>
                <echo test="response.statusCode != 200">上传失败</echo>
            </response>
        </http>
        </then>
        <else>app.zip 压缩失败</else>
    </if>
    
    
</main>

```

## 插件介绍
可以使用基本的逻辑表达式，来判断是否执行某个操作。所有逻辑需要写在 < main ></ main > 标签中。

常见属性： 

- name: 定义变量名， 用于在后续逻辑中引用。
- test: 用于判断是否可以执行某个操作。test 接受简单判断逻辑表达式或布尔值。
- value: 用于设置变量的值。value 与 标签内容 不能同时使用，属于同值。
- ref: 用于引用其他变量的值。
- to: 用于将变量的值赋值给其他变量。
- format: 用于格式化变量的值。
- encoding: 编码格式。
- overwrite: 是否覆盖目标文件。

## 示范

```xml
<main>
    <var name="a" value="10" />
    <echo>${a}</echo>
</main>
```

## Maven 插件使用方法

```xml
    <build>
    <plugins>
        <plugin>
            <groupId>com.clmcat.maven.plugins</groupId>
            <artifactId>build-logic-maven-plugin</artifactId>
            <version>---插件版本---</version>
            <executions>
                <execution>
                    <id>build-script</id>
                    <!-- 
                        <phase>生命周期阶段</phase>
                        例如：package, install, clean, test, compile等 时执行逻辑
                    -->
                    <phase>package</phase>
                    <goals>
                        <goal>run</goal>
                    </goals>
                    <configuration>
                        <main>
                        <!-- 逻辑 -->                
                        </main>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>

```

## 扩展 ${} 表达式

 变量：逻辑创建的变量。

 类型：Java数据类型，基本和部分数据类型支持简写。

 方法：Java对象的方法。 
 
 值和引用变量名： 部分值可以直接传入，特殊对象值需要使用引用。

 
```xml
<echo>${变量名.方法(类型 值或变量名)}</echo>
```

例如:

```xml
<str name="str">hello world</str>
<echo>${str}</echo>   <!-- 输出：hello world -->
<echo>${str.length()}</echo> <!-- 输出：11 -->
<echo>${str.toUpperCase()}</echo> <!-- 输出：HELLO WORLD -->
<echo>${str.substring(int 0, int 5)}</echo> <!-- 输出：HELLO -->
```


## 标签说明

### 通用属性 < XxxTag test="true/false" >
属性test: 所有标签都可以使用， 来判断是否可以执行某个操作。test 接受简单判断逻辑表达式或布尔值。

```xml
<echo test="a == 10" value="a等于10" />
<echo test="a > 10" value="a大于10" />
<echo test="varName" value="varName的值存在" />

```
...

### < main >

主逻辑标签，所有逻辑都需要卸载这个标签中。
```xml
<main>
<!-- 逻辑 -->            
</main>
```

### < var >

用于定义一个变量。 所有变量的名字只可以包含字母、数字和下划线。
```xml
<!-- 自动变量类型, 通用定义 -->
<var name="a" value="10" />
<echo>a=${a}</echo>

<!-- 强制指定变量类型 -->
<var.int name="a" value="10" />
<var.string name="a" value="10" />
```

引用一个变量 
```xml
<var name="b" ref="a" />
<echo>b=${b}</echo>
```

### < mkdir />
创建目录
```xml
<mkdir name="testDir" path="${project.basedir}/test" />
```


### < file />
文件变量定义
```xml
<file name="testFile" path="---文件---" />
<echo>testFile=${testFile}</echo>
```

项目下面的文件

```xml
<file name="testTxtFile" path="${project.basedir}/test.txt" />
<echo test="testTxtFile">文件存在</echo>
<echo test="!testTxtFile">文件不存在</echo>
```

属性test: 所有标签都可以使用， 来判断是否可以执行某个操作。test 接受简单判断逻辑表达式或布尔值。



#### < read />
读取文件内容，到某个变量里
```xml
<file name="testTxtFile" path="${project.basedir}/test.txt" />
<read name="testTxtFileRead" file="testTxtFile" />
<!-- 输出文件内容 -->
<echo>testTxtFileRead=${testTxtFileRead}</echo>
```

#### < write />
写入文件, 默认存在则不覆盖， 直接写入值
```xml
<file name="testTxtFileWrite" path="${project.basedir}/test-write.txt" />
<write file="testTxtFileWrite"><![CDATA[
    可以写入的任何内容, 等同于 <write file="testTxtFileWrite" value="直接写入的内容" />
    在<![CDATA[]> 内容中，可以包含任何字符。
]]></write>
```

追加写入文件, 直接追加写入值。
```xml
<write file="testTxtFileWrite" value="追加写入的内容" append="true" />
```

使用引用写入文件内容

1. 使用文件变量引用， 复制文件内容， 到目标文件。
```xml
<file name="testTxtFile" path="${project.basedir}/test.txt" />
<file name="testTxtFileWrite" path="${project.basedir}/test-write.txt" />
<write file="testTxtFileWrite" ref="testTxtFile" overwrite="true" />
```

2. 使用变量引用， 复制变量值， 到目标文件。
```xml
<var name="a" value="10" />
<file name="testTxtFileWrite" path="${project.basedir}/test-write.txt" />
<write file="testTxtFileWrite" value="${a}" overwrite="true" />
```

3. 使用 < read /> 标签， 复制文件内容， 到目标文件。
```xml
<file name="testTxtFile" path="${project.basedir}/test.txt" />
<file name="testTxtFileWrite" path="${project.basedir}/test-write.txt" />
<!-- 读取文件内容， 到变量 testTxtFileRead -->
<read name="testTxtFileRead" file="testTxtFile" />
<!-- 写入文件内容， 到目标文件, overwrite="true" 表示覆盖-->
<write file="testTxtFileWrite" ref="testTxtFileRead" overwrite="true" />
```

#### < delete >
删除文件或目录, 默认：只能删除 项目目录下的文件或目录， 不能删除其他目录下的文件或目录。

```xml
<file name="testTxtFile" path="${project.basedir}/test.txt" />
<delete file="testTxtFile" />
```

强制删除， 不限制。
force="true" 表示强制删除， 任意位置都可以删除， 慎重使用。
```xml
<file name="testTxtFile" path="${project.basedir}/test.txt" />
<delete file="testTxtFile" force="true" />
```

手动指定安全目录:
```xml
<main>
    <allowWriteDir path="${project.basedir}" />
    <!-- 其他逻辑 -->
</main>

```



### < list >

集合变量定义
```xml
<list name="testList"  > 
    <item>1</item>
    <item>2</item>
    <item>3</item>
</list>
<foreach collection="testList" item="item">
    <echo>${item}</echo>
</foreach>
```

集合追加数据
```xml

<list.add name="testList">
    <item>4</item>
    <item>5</item>
</list.add>
<foreach collection="testList" item="item">
    <echo>${item}</echo>
</foreach>
```

集合存储文件数据

```xml

<list name="fileList">
    <file path="${project.basedir}/test.txt" />
</list>
<foreach collection="fileList" item="item">
    <echo>${item}</echo>
</foreach>
```

### < foreach >
便利集合或便利文件列表

1. 便利集合
```xml
<list.add name="testList">
    <item>4</item>
    <item>5</item>
</list.add>
<foreach collection="testList" item="item">
<echo>${item}</echo>
</foreach>
```

2. 便利文件列表
```xml
<file name="testDir" path="${project.basedir}" />
<foreach collection="testDir" item="item">
<echo>file: ${item}</echo>
</foreach>
```

3. 循环次数

最终输出 0 ~ 9 共10个数

```xml
<foreach collection="0..9" item="item">
    <echo>${item}</echo>
</foreach>

```

### < zip >
压缩文件或目录

1. 目录下面全部压缩到 zip 文件
```xml
<!-- 创建 zip 文件变量 -->
<file name="appZipFile" path="${project.basedir}/app.zip"/>
<!-- 压缩目录到 zip 文件 -->
<zip file="appZipFile">
    <entry dir="${project.basedir}/test_dir"/>
</zip>
```

2. 正则匹配文件

简单匹配: pattern="*.txt"

```xml
<!-- 压缩目录到 zip 文件 regex: 开头代表标准正则表达式-->
<zip file="appZipFile">
    <entry dir="${project.basedir}/test_dir" pattern="regex:.*\\.txt$"/>
</zip>
```


### < if >
条件判断

```xml
<var name="a" value="10" />
<if test="a == 10">
    <then>
        a is 10
    </then>
    <elseif test="a == 20">
        a is 20
    </elseif>
    <else>
        a is not 10 or 20
    </else>
</if>

```


### < func > or < call >

函数与调用

1. 定义函数与调用
```xml
<func.funcName>
    <var name="a" value="10" />
    <echo>a=${a}</echo>
</func.funcName>

<call.funcName />
```

2.传递参数
```xml
<func.funcName>
    <echo>a=${a}</echo>
</func.funcName>

<call.funcName>
    <arg name="a" value="20" />
</call.funcName>
```

### <date />
创建日期时间变量

```xml
<!-- 创建日期时间变量 testDate, 默认格式： yyyy-MM-dd HH:mm:ss, 数据当前时间, 东八区时区 -->
<date name="testDate" />
<!-- 创建日期时间变量 testDate2, 指定时间格式与时间值 -->
<date name="testDate2" format="yyyy-MM-dd HH:mm:ss" timeZone="GMT+8" value="2023-01-01 00:00:00" />
```

### < str >
字符串操作

属性说明： 

| 属性          | 说明                      | 是否必须 |
|-------------|-------------------------|------|
| name        | 新变量或引用变量名               | 必填   |
| to          | 新数据赋值的变量名，如果未填写则直接覆盖原变量 | 非必填  |
| params      | 参数， 有写方法需要参数， 逗号分割      | 非必填  |
| <str.method | method方法，默认：set， 选择执行方式 |      |



方法说明:

| 方法          | 说明                                     | 参数                               |
|-------------|----------------------------------------|----------------------------------|
| set         | 设置新的String。或者将name="?"字符串变量赋值给to="?"变量 | 无                                |
| substr      | 裁剪字符串                                  | params="0,1" 或 params="0"        |
| split       | 分割字符串得到字符串集合                           | params=";"                       |
| trim        | 清楚字符串左右空字符                             | 无                                |
| len         | 计算字符串长度，并赋值给一个变量                       | 无                                |
| chatAt      | 通过下表得到字符串中的某个单字                        | params="下表" 例如：params="1"        |
| toLowerCase | 字符串转换为小写                               |                                  |
| toUpperCase | 字符串转换为大写                               |                                  |
| json        | 将可转换JSON的变量数据转换为 JSONString            |                                  |
| random      | 随机字符串                                  | params="随机字符长度" 或 params="最小，最大" |
| append      | 追加新字符                                  |                                  |




```xml
<!-- to 赋值给谁，如果不设置则赋值给name -->
<str name="name" to="to" />
<str.set name="name" value="hello" />
<!-- name变量字符串裁剪 start=1, end=3 赋值给 to="aaa"-->
<str.substr name="name" to="aaa" params="1,3"/>
<!-- 裁剪 从1位置开始到结束 -->
<str.substr name="name" params="1"/>
<!-- 分割字符串，获得一个list集合 -->
<str.split name="name" to="" />
<!-- 随机字符串， name 可以是 字符串变量，List变量， params="最小，最大，字符分割方式" ，可以单独存在1个, 2个参数 -->
<str.random name=""  to="" params="10" />
<str.trim name=""  />
<str.trim name="" to="" />
<str.toUpperCase name="" to="" />
<str.toLowerCase name="" to="" />
<str.replace name="" to="" params="aaa,ccc"/>
<str.length name="str" to="strLen" />
<str.len name="str" to="strLen" />
<str.json name="obj" to="jsonStr" />
<str.charAt name="str" to="strAt" params="0" />
```

1. 截取字符串
params="start, end"
start: 截取开始位置， end: 截取结束位置， 包含 end 位置的字符。
name: 截取指定变量的字符串， 重新赋值给 name 变量。
```xml
<str.substr name="str" params="0,5" />

```

2. 截取Value中的字符串并赋值给 name 变量。

```xml
<str.substr name="str" params="0,5" value="hello world" />
```

3. 设置字符串
params="value"
name: 设置指定变量的字符串值。
```xml
<str.set name="str" value="hello world" />
<str name="str" value="hello world" />
<str name="str">hello world</str>
<str name="str"><![CDATA[hello world]]></str>
```
4. < str.len >计算字符串长度, 并赋值给新的变量

计算传入字符串长度, 并赋值给新的变量 strLen
```xml
<!-- 计算传入字符串长度, 并赋值给新的变量 strLen -->
<str.len name="strLen" value="hello world" />
```

计算字符串变量长度， 赋值给新的变量 strLen
```xml
<!-- 计算字符串变量长度， 赋值给新的变量 strLen -->
<str name="str">hello world</str>
<str.len name="str" to="strLen" />
<echo>strLen=${strLen}</echo>
```

5. < str.split > 分割字符串得到字符串List

分割 value中的 字符串并设置到 list 中
```xml
<str.split name="list" value="a,b,c,d,e,f,g" params=","/>
<echo>${list}</echo>
```

分割某变量的字符串并赋值
```xml
<str name="str">hello,world</str>
<str.split name="str" params="," to="list"/>
<echo>${list}</echo>
```

6. < str.ramdom > 字符串随机


| params参数 | 说明            | 是否必须 |
|----------|---------------| --- |
| params0  | min 字符串长度     | 必填写 |
| params1  | max 字符串长度     | 可选
| params2  | 使用随机的数据的分割方式。 | 可选 |



```xml
<!-- 分割出一个集合 -->
<str.split name="list" value="a,b,c,d,e,f,g" params=","/>
<!-- 通过集合随机字符串 -->
<str.random name="list" params="10" to="randomStr" />
```

通过value的index随机，得到10个长度的随机字符串
```xml
<!-- 通过value的index随机，得到10个长度的随机数字字符串 -->
<str.random name="randomStr" params="10" value="1234567890" />
```

通过value的index随机，得到10个长度的随机字符串,


超过 最终计算结果的字符串长度，会被切割。

```xml
<!-- 通过value的index随机，得到10个长度的随机字符串 -->
<str.random name="randomStr" params="10,10,','" value="1,2,3,4,5,6,7,8,9,0" />
```

7. 追加字符串
```xml
<!-- 追加给str字符串， 覆盖 str -->
<str name="str">Hello</str>
<str.append name="str" params="World" />
<echo>${str}</echo> <!-- 输出：HelloWorld -->

<!-- 追加字符串， 不赋值给新的变量， 直接修改 str0 -->
<str name="str0">Hello</str>
<str name="str1">World</str>
<str.append name="str0" ref="str1" />
<echo>${str0}</echo> <!-- 输出：HelloWorld -->

<!-- 追加字符串， 赋值给新的变量 str -->
<str name="str0">Hello</str>
<str name="str1">World</str>
<str.append name="str0" ref="str1" to="str" />
<echo>${str}</echo> <!-- 输出：HelloWorld -->

```

---


## HTTP 请求

### < http > 发送HTTP请求
```xml

<http url="http://localhost:8080" method="POST" >
    <header name="Content-Type" value="application/json" />
    <content>{"name": "zzxx"}</content>
    <!-- 应答处理的逻辑块 -->
    <response to="response" >
        <echo>${response}</echo>
        <echo>${response.code}</echo>
        <echo>${response.message}</echo>
        <echo>${response.headers.getHeader(String "Content-Type")}</echo>
    </response>
</http>

```

直接返回响应对象

```xml
<http name="httpResult" url="http://www.xxx.com" method="GET">
    <header name="Content-Type" value="application/json" />
</http>
<echo>httpResult=${httpResult.code()}</echo>
<echo>httpResult=${httpResult.message()}</echo>
<echo>httpResult=${httpResult}</echo>
<echo>httpResult=${httpResult.content()}</echo>
```

直接读取文件的内容，二进制流上传数据。 content的包是application/octet-stream，byte[]类型。

```xml

<file name="file" path=""/>
<http url="http://localhost:8080" method="POST" >
<header name="Content-Type" value="application/octet-stream" />
<content ref="file" />
<!-- 应答处理的逻辑块 -->
<response to="response" >
    <echo>${response}</echo>
    <echo>${response.code}</echo>
    <echo>${response.message}</echo>
    <echo>${response.headers.getHeader(String "Content-Type")}</echo>
</response>
</http>
```

---

