package com.clmcat.maven.plugins.action.variable;

import com.clmcat.maven.plugins.action.Variable;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;

public class BytesVariable extends Variable.AbstractVariable<byte[]> {


    private String encoding = "UTF-8";

    public BytesVariable(byte[] value) {
        super(value);
    }

    public BytesVariable(byte[] value, String encoding) {
        super(value);
        this.encoding = encoding;
    }

    public static Variable of(byte[] bytes, String encoding) {
        return new BytesVariable(bytes, encoding);
    }

    @Override
    public String getStringValue() {
        return new String(getValue(), Charset.forName(encoding));
    }

    public static BytesVariable of(byte[] value) {
        return new BytesVariable(value);
    }

    public static BytesVariable of(String fvalue) {
        if (fvalue.startsWith("[") && fvalue.endsWith("]") || fvalue.startsWith("{") && fvalue.endsWith("}")) {
            fvalue = fvalue.substring(1, fvalue.length() - 1);
        }

        String[] split = fvalue.split(",");
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
            byte b = Byte.parseByte(split[i]);
            bos.write(b);
        }
        return new BytesVariable(bos.toByteArray());
    }

    @Override
    public int compareTo(Variable<byte[]> o) {

        byte[] value = getValue();
        byte[] otherValue = o.getValue();
        for (int i = 0; i < value.length && i < otherValue.length; i++) {
            if (value[i] > otherValue[i]) {
                return 1;
            }  else if (value[i] < otherValue[i]) {
                return -1;
            }
        }
        if (value.length == otherValue.length) {
            return 0;
        } else if (value.length > otherValue.length) {
            return 1;
        }  else {
            return -1;
        }
    }
}
