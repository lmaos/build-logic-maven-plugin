# Build Logic Plugin

Using a concise and unified XML DSL syntax, you can perform file operations, directory management, string processing, compression, conditional logic, loops, functions, HTTP calls, and more within the Maven build lifecycle — without writing any Java code or relying on external scripts.

***Design Philosophy***

This plugin is built on three core principles: minimal, unified, and unambiguous:

**name** — define a variable  
Declares a variable (a plain value, file, string, collection, date, etc.).

**ref** — reference a variable  
References a variable that was previously declared with `name`.

**file** — reference a file variable  
Specifically used to reference a file object declared with `<file name="xxx"/>`.

Example — building and packaging a project release:
```xml
<main>
    <!-- Create directories for publishing -->
    <mkdir path="${project.basedir}/publish" />
    <mkdir path="${project.basedir}/publish/scripts" />
    <mkdir path="${project.basedir}/publish/nginx" />
    <mkdir path="${project.basedir}/version" />

    <file name="appJarFile" path="${project.basedir}/target/${project.build.finalName}.jar" />
    <!-- Copy app.jar into the publish directory -->
    <file name="appJarFileWrite" path="${project.basedir}/publish/${project.build.finalName}.jar" />
    <!-- Copy the content of appJarFile into appJarFileWrite; overwrite="true" means overwrite if it exists -->
    <write ref="appJarFile" file="appJarFileWrite"  overwrite="true" />
    <!-- Initialise a default startup script -->
    <file name="startShell" path="${project.basedir}/publish/start.sh" />
    <!-- Write the default startup script content -->
    <write file="startShell" ><![CDATA[
#!/bin/sh

APP_NAME=app
JAVA_OPTS="-Xms800m -Xmx800m"
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
pkill -f "${APP_NAME}.jar"
sleep 3
nohup java ${JAVA_OPTS} -jar "${SCRIPT_DIR}/${APP_NAME}.jar" > "${SCRIPT_DIR}/${APP_NAME}.log" 2>&1 &
    ]]></write>

    <!-- Create a versioned zip -->
    <date name="versionDate" format="yyyyMMddHHmmss" />
    <!-- Create a zip file variable -->
    <file name="appZipFile" path="${project.basedir}/version/app-${versionDate}.zip"/>
    <!-- Compress the publish directory into the zip file -->
    <zip file="appZipFile">
        <entry dir="${project.basedir}/publish"/>
    </zip>
    
    <if test="appZipFile" >
        <then>
        <!-- Upload the archive -->
        <http url="http://localhost:8080/build/success?filename=${appZipFile.getName()}" method="POST" >
            <header name="Content-Type" value="application/octet-stream" />
            <content ref="appZipFile" />
            <response to="response">
                <echo>${response}</echo>
                <echo test="response.statusCode == 200">Upload succeeded</echo>
                <echo test="response.statusCode != 200">Upload failed</echo>
            </response>
        </http>
        </then>
        <else>app.zip compression failed</else>
    </if>
</main>

```

## Plugin Introduction
You can use basic logic expressions to control whether an operation executes. All logic must be placed inside `<main></main>` tags.

Common attributes:

- **name**: declares a variable name for use in subsequent logic.
- **test**: determines whether to execute an operation. Accepts a simple logic expression or boolean value.
- **value**: sets a variable's value. `value` and tag body text are equivalent; they cannot both be set.
- **ref**: references the value of another variable.
- **to**: assigns a value to a different variable.
- **format**: formats a variable's value.
- **encoding**: character encoding.
- **overwrite**: whether to overwrite the target file.

## Quick Example

```xml
<main>
    <var name="a" value="10" />
    <echo>${a}</echo>
</main>
```

## Maven Plugin Usage

```xml
    <build>
    <plugins>
        <plugin>
            <groupId>com.clmcat.maven.plugins</groupId>
            <artifactId>build-logic-maven-plugin</artifactId>
            <version>---plugin-version---</version>
            <executions>
                <execution>
                    <id>build-script</id>
                    <!-- 
                        <phase>lifecycle-phase</phase>
                        e.g., package, install, clean, test, compile, etc.
                    -->
                    <phase>package</phase>
                    <goals>
                        <goal>run</goal>
                    </goals>
                    <configuration>
                        <main>
                        <!-- logic here -->                
                        </main>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>

```

## Extended `${}` Expressions

 **Variable** — a variable created within your logic.

 **Type** — a Java data type; primitives and some common types support shorthand names.

 **Method** — a method on a Java object.

 **Value or variable reference** — some values can be passed inline; special object values must be referenced by name.

```xml
<echo>${variableName.method(Type valueOrVariableName)}</echo>
```

For example:

```xml
<str name="str">hello world</str>
<echo>${str}</echo>   <!-- output: hello world -->
<echo>${str.length()}</echo> <!-- output: 11 -->
<echo>${str.toUpperCase()}</echo> <!-- output: HELLO WORLD -->
<echo>${str.substring(int 0, int 5)}</echo> <!-- output: HELLO -->
```


## Tag Reference

### Universal attribute `<XxxTag test="true/false" >`
The `test` attribute is available on every tag. It controls whether the tag executes. Accepts a simple logic expression or a boolean value.

```xml
<echo test="a == 10" value="a is 10" />
<echo test="a > 10" value="a is greater than 10" />
<echo test="varName" value="varName exists" />

```
...

### `<main>`

Root logic tag. All logic must be written inside this tag.
```xml
<main>
<!-- logic here -->            
</main>
```

### `<var>`

Declares a variable. Variable names may only contain letters, digits, and underscores.
```xml
<!-- Auto-detect variable type (general usage) -->
<var name="a" value="10" />
<echo>a=${a}</echo>

<!-- Force a specific variable type -->
<var.int name="a" value="10" />
<var.string name="a" value="10" />
```

Reference another variable:
```xml
<var name="b" ref="a" />
<echo>b=${b}</echo>
```

### `<mkdir />`
Create a directory.
```xml
<mkdir name="testDir" path="${project.basedir}/test" />
```


### `<file />`
Declare a file variable.
```xml
<file name="testFile" path="---file path---" />
<echo>testFile=${testFile}</echo>
```

A file in the project:

```xml
<file name="testTxtFile" path="${project.basedir}/test.txt" />
<echo test="testTxtFile">File exists</echo>
<echo test="!testTxtFile">File does not exist</echo>
```

The `test` attribute is available on every tag to guard execution.



#### `<read />`
Read a file's content into a variable.
```xml
<file name="testTxtFile" path="${project.basedir}/test.txt" />
<read name="testTxtFileRead" file="testTxtFile" />
<!-- Print file content -->
<echo>testTxtFileRead=${testTxtFileRead}</echo>
```

#### `<write />`
Write to a file. By default, if the file already exists it is not overwritten.
```xml
<file name="testTxtFileWrite" path="${project.basedir}/test-write.txt" />
<write file="testTxtFileWrite"><![CDATA[
    Any content can be written here, equivalent to <write file="testTxtFileWrite" value="content to write" />
    Inside <![CDATA[]], any characters are allowed.
]]></write>
```

Append to a file:
```xml
<write file="testTxtFileWrite" value="content to append" append="true" />
```

Write using a variable reference:

1. Reference a file variable — copy one file's content to the target file.
```xml
<file name="testTxtFile" path="${project.basedir}/test.txt" />
<file name="testTxtFileWrite" path="${project.basedir}/test-write.txt" />
<write file="testTxtFileWrite" ref="testTxtFile" overwrite="true" />
```

2. Reference a variable — write its value to the target file.
```xml
<var name="a" value="10" />
<file name="testTxtFileWrite" path="${project.basedir}/test-write.txt" />
<write file="testTxtFileWrite" value="${a}" overwrite="true" />
```

3. Use `<read />` to copy one file's content to another.
```xml
<file name="testTxtFile" path="${project.basedir}/test.txt" />
<file name="testTxtFileWrite" path="${project.basedir}/test-write.txt" />
<!-- Read file content into variable testTxtFileRead -->
<read name="testTxtFileRead" file="testTxtFile" />
<!-- Write file content to the target file; overwrite="true" means overwrite -->
<write file="testTxtFileWrite" ref="testTxtFileRead" overwrite="true" />
```

#### `<delete>`
Delete a file or directory. By default, deletion is restricted to the project directory.

```xml
<file name="testTxtFile" path="${project.basedir}/test.txt" />
<delete file="testTxtFile" />
```

Force deletion without restriction. `force="true"` allows deleting any path — use with caution.
```xml
<file name="testTxtFile" path="${project.basedir}/test.txt" />
<delete file="testTxtFile" force="true" />
```

Manually specify a safe directory:
```xml
<main>
    <allowWriteDir path="${project.basedir}" />
    <!-- other logic -->
</main>

```



### `<list>`

Declare a list variable.
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

Append items to a list:
```xml

<list.add name="testList">
    <item>4</item>
    <item>5</item>
</list.add>
<foreach collection="testList" item="item">
    <echo>${item}</echo>
</foreach>
```

Store file objects in a list:

```xml

<list name="fileList">
    <file path="${project.basedir}/test.txt" />
</list>
<foreach collection="fileList" item="item">
    <echo>${item}</echo>
</foreach>
```

### `<foreach>`
Iterate over a collection or a list of files.

1. Iterate over a collection:
```xml
<list.add name="testList">
    <item>4</item>
    <item>5</item>
</list.add>
<foreach collection="testList" item="item">
<echo>${item}</echo>
</foreach>
```

2. Iterate over a file list:
```xml
<file name="testDir" path="${project.basedir}" />
<foreach collection="testDir" item="item">
<echo>file: ${item}</echo>
</foreach>
```

3. Loop a fixed number of times:

Outputs 0 through 9 — 10 numbers total.

```xml
<foreach collection="0..9" item="item">
    <echo>${item}</echo>
</foreach>

```

### `<zip>`
Compress files or directories.

1. Compress all files in a directory into a zip:
```xml
<!-- Create a zip file variable -->
<file name="appZipFile" path="${project.basedir}/app.zip"/>
<!-- Compress the directory into the zip file -->
<zip file="appZipFile">
    <entry dir="${project.basedir}/test_dir"/>
</zip>
```

2. Match files with a pattern:

Simple wildcard: `pattern="*.txt"`

```xml
<!-- regex: prefix means standard Java regex -->
<zip file="appZipFile">
    <entry dir="${project.basedir}/test_dir" pattern="regex:.*\\.txt$"/>
</zip>
```


### `<if>`
Conditional logic.

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
        a is neither 10 nor 20
    </else>
</if>

```


### `<func>` and `<call>`

Functions and calls.

1. Define and call a function:
```xml
<func.funcName>
    <var name="a" value="10" />
    <echo>a=${a}</echo>
</func.funcName>

<call.funcName />
```

2. Pass arguments:
```xml
<func.funcName>
    <echo>a=${a}</echo>
</func.funcName>

<call.funcName>
    <arg name="a" value="20" />
</call.funcName>
```

### `<date />`
Create a date/time variable.

```xml
<!-- Create a date/time variable testDate; default format: yyyy-MM-dd HH:mm:ss, current time, GMT+8 -->
<date name="testDate" />
<!-- Create a date/time variable testDate2 with a specific format and value -->
<date name="testDate2" format="yyyy-MM-dd HH:mm:ss" timeZone="GMT+8" value="2023-01-01 00:00:00" />
```

### `<str>`
String operations.

Attribute reference:

| Attribute    | Description                                                      | Required |
|--------------|------------------------------------------------------------------|----------|
| name         | New or referenced variable name                                  | Yes      |
| to           | Variable to assign the result to; if omitted, overwrites `name` | No       |
| params       | Parameters (comma-separated) required by some methods            | No       |
| `<str.method>` | Method suffix; default is `set`                                | —        |



Method reference:

| Method       | Description                                                | Parameters                         |
|--------------|------------------------------------------------------------|------------------------------------|
| set          | Set a new String or copy the `name` variable to `to`       | none                               |
| substr       | Substring                                                  | `params="0,1"` or `params="0"`     |
| split        | Split into a string list                                   | `params=";"`                       |
| trim         | Strip leading/trailing whitespace                          | none                               |
| len          | Compute string length and store it                         | none                               |
| charAt       | Get character at an index                                  | `params="index"` e.g. `params="1"` |
| toLowerCase  | Convert to lowercase                                       |                                    |
| toUpperCase  | Convert to uppercase                                       |                                    |
| json         | Convert a serialisable variable to a JSON string           |                                    |
| random       | Generate a random string                                   | `params="length"` or `params="min,max"` |
| append       | Append a new string                                        |                                    |



```xml
<!-- assign to 'to'; if not set, overwrites 'name' -->
<str name="name" to="to" />
<str.set name="name" value="hello" />
<!-- substring of 'name', start=1, end=3, assign to to="aaa" -->
<str.substr name="name" to="aaa" params="1,3"/>
<!-- substring from position 1 to end -->
<str.substr name="name" params="1"/>
<!-- split string into a list -->
<str.split name="name" to="" />
<!-- random string; name can be a String or List variable; params="min,max,splitChar", at least one param required -->
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

1. Substring.
`params="start, end"` — `start`: inclusive start index; `end`: exclusive end index.
```xml
<str.substr name="str" params="0,5" />

```

2. Substring of a value and assign to the name variable.

```xml
<str.substr name="str" params="0,5" value="hello world" />
```

3. Set a string value.
```xml
<str.set name="str" value="hello world" />
<str name="str" value="hello world" />
<str name="str">hello world</str>
<str name="str"><![CDATA[hello world]]></str>
```
4. `<str.len>` — compute string length and store in a new variable.

```xml
<!-- Compute the length of the given string and store it in strLen -->
<str.len name="strLen" value="hello world" />
```

Compute the length of a string variable and store it in a new variable:
```xml
<str name="str">hello world</str>
<str.len name="str" to="strLen" />
<echo>strLen=${strLen}</echo>
```

5. `<str.split>` — split a string into a List of strings.

Split the value and store the result in a list:
```xml
<str.split name="list" value="a,b,c,d,e,f,g" params=","/>
<echo>${list}</echo>
```

Split a string variable and assign the result:
```xml
<str name="str">hello,world</str>
<str.split name="str" params="," to="list"/>
<echo>${list}</echo>
```

6. `<str.random>` — random string.


| Parameter | Description                  | Required |
|-----------|------------------------------|----------|
| params[0] | min string length             | Yes      |
| params[1] | max string length             | No       |
| params[2] | delimiter for the source data | No       |



```xml
<!-- Split into a list -->
<str.split name="list" value="a,b,c,d,e,f,g" params=","/>
<!-- Generate a random string from the list -->
<str.random name="list" params="10" to="randomStr" />
```

Generate a random 10-character numeric string from the value by index:
```xml
<str.random name="randomStr" params="10" value="1234567890" />
```

Generate a 10-character random string from a comma-separated value.
The result is truncated to `count` characters if it exceeds that length.

```xml
<str.random name="randomStr" params="10,10,','" value="1,2,3,4,5,6,7,8,9,0" />
```

7. Append a string:
```xml
<!-- Append to str and overwrite str -->
<str name="str">Hello</str>
<str.append name="str" params="World" />
<echo>${str}</echo> <!-- output: HelloWorld -->

<!-- Append str1 to str0; modify str0 in-place -->
<str name="str0">Hello</str>
<str name="str1">World</str>
<str.append name="str0" ref="str1" />
<echo>${str0}</echo> <!-- output: HelloWorld -->

<!-- Append str1 to str0; assign the result to a new variable -->
<str name="str0">Hello</str>
<str name="str1">World</str>
<str.append name="str0" ref="str1" to="str" />
<echo>${str}</echo> <!-- output: HelloWorld -->

```

---


## HTTP Requests

### `<http>` — send an HTTP request
```xml

<http url="http://localhost:8080" method="POST" >
    <header name="Content-Type" value="application/json" />
    <content>{"name": "zzxx"}</content>
    <!-- Response handling block -->
    <response to="response" >
        <echo>${response}</echo>
        <echo>${response.code}</echo>
        <echo>${response.message}</echo>
        <echo>${response.headers.getHeader(String "Content-Type")}</echo>
    </response>
</http>

```

Return the response object directly:

```xml
<http name="httpResult" url="http://www.xxx.com" method="GET">
    <header name="Content-Type" value="application/json" />
</http>
<echo>httpResult=${httpResult.code()}</echo>
<echo>httpResult=${httpResult.message()}</echo>
<echo>httpResult=${httpResult}</echo>
<echo>httpResult=${httpResult.content()}</echo>
```

Upload a file as a binary stream. Content-Type is `application/octet-stream` and the body is a `byte[]`.

```xml

<file name="file" path=""/>
<http url="http://localhost:8080" method="POST" >
<header name="Content-Type" value="application/octet-stream" />
<content ref="file" />
<!-- Response handling block -->
<response to="response" >
    <echo>${response}</echo>
    <echo>${response.code}</echo>
    <echo>${response.message}</echo>
    <echo>${response.headers.getHeader(String "Content-Type")}</echo>
</response>
</http>
```

---

## Encoding / Decoding

| Attribute | Description                        | Required                    |
|-----------|------------------------------------|-----------------------------|
| name      | variable name                      | Yes                         |
| ref       | referenced variable                | Required if `value` is absent |
| value     | string value; mutually exclusive with `ref` | No             |
| encoding  | character encoding (default UTF-8) | No                          |


### `<base64.encode>` — encode

```xml

<base64.encode name="str" value="hello world" />
<echo>${str}</echo> <!-- output: aGVsbG8gd29ybGQ= -->

<str name="str">hello world</str>
<base64.encode name="strEncode" ref="str" /> 
<echo>${strEncode}</echo> <!-- output: aGVsbG8gd29ybGQ= --> 

<base64.decode name="strDecode" ref="strEncode" />  
<echo>${strDecode}</echo> <!-- output: hello world -->
```


### `<base64.decode>` — decode

```xml

<base64.encode name="str" value="hello world" />
<echo>${str}</echo> <!-- output: aGVsbG8gd29ybGQ= -->

<base64.decode name="strDecode" ref="str" />  
<echo>${strDecode}</echo> <!-- output: hello world -->
```
