# 逻辑插件
## 插件介绍
可以使用基本的逻辑表达式，来判断是否执行某个操作。所有逻辑需要卸载 <main></main> 标签中。

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

## 标签说明

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
<var name="a" value="10" />
<echo>a=${a}</echo>
```

引用一个变量 
```xml
<var name="b" ref="a" />
<echo>b=${b}</echo>
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

### < mkdir />
创建目录
```xml
<mkdir name="testDir" path="${project.basedir}/test" />
```


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

#### <delete />
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


### <func> or < call >

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