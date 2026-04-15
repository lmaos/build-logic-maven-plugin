package com.clmcat.maven.plugins.action.support;

import com.clmcat.maven.plugins.action.*;
import com.clmcat.maven.plugins.action.anns.NotAttr;
import com.clmcat.maven.plugins.action.factory.ActionFactory;
import com.clmcat.maven.plugins.action.variable.*;
import com.clmcat.maven.plugins.action.variable.number.NumberVariable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 {@code
 <http url="http://localhost:8080" method="POST" >
  <header name="Content-Type" value="application/x-www-form-urlencoded" />
  <content>
    name=clmcat
  </content>
 </http>
 }
 * </pre>
 */
public class HttpAction extends VariableAction implements DeferredChildrenParsingAction {

    private String method = "GET";
    private String encoding = "UTF-8";
    private String url;

    public HttpAction() {
        setNameRequired(false);
    }

    @Override
    protected void callExecute(ActionParam actionParam, Action parentAction) throws Exception {
        String encoding = XUtils.isEmpty(this.encoding) ? "UTF-8" : this.encoding.trim();
        String method = XUtils.isEmpty(this.method) ? "GET" : this.method.trim().toUpperCase();
        String url = actionParam.format(this.url);
        if (XUtils.isEmpty(url)) {
            throw new IllegalArgumentException("<http> url is required");
        }

        ActionFactory actionFactory = actionParam.getActionFactory().copy();
        actionFactory.addActionType("header", HttpHeaderAction.class);
        actionFactory.addActionType("content", HttpContentAction.class);
        actionFactory.addActionType("response", HttpResponseAction.class);
        List<Action> childrens = parseChildren(actionFactory);

        List<HttpHeaderAction> headerActions = new ArrayList<>();
        HttpContentAction contentAction = null;
        HttpResponseAction responseAction = null;
        // 子节点执行
        for (Action children : childrens) {
            if (children instanceof HttpHeaderAction) {
                headerActions.add((HttpHeaderAction) children);
            } else if (children instanceof HttpContentAction) {
                if (contentAction != null) {
                    throw new IllegalArgumentException("<http> only supports one <content> child");
                }
                contentAction = (HttpContentAction) children;
                contentAction.setEncoding(encoding);
            } else if (children instanceof HttpResponseAction) {
                if (responseAction != null) {
                    throw new IllegalArgumentException("<http> only supports one <response> child");
                }
                responseAction = (HttpResponseAction) children;
            } else {
                throw new IllegalArgumentException("<http> only supports <header>, <content> and <response> children");
            }
        }
        for (HttpHeaderAction headerAction : headerActions) {
            headerAction.execute(actionParam, this);
            if ("content-type".equalsIgnoreCase(headerAction.getName())) {
                if (contentAction != null) {
                    contentAction.setContentType(headerAction.getValue());
                }
            } else if ("encoding".equalsIgnoreCase(headerAction.getName())) {
                encoding = headerAction.getValue();
                if (contentAction != null) {
                    contentAction.setEncoding(encoding);
                }
            }
        }
        if (contentAction != null) {
            contentAction.setEncoding(encoding);
            contentAction.execute(actionParam, this);
        }
        url = XUtils.toUrlString(url, encoding);

        HttpURLConnection conn = null;
        HttpResponse httpResponse;
        try {
            URL realUrl = new URL(url);
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestMethod(method);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setDoInput(true);
            conn.setDoOutput(!"GET".equalsIgnoreCase(method) && contentAction != null);

            for (HttpHeaderAction headerAction : headerActions) {
                conn.setRequestProperty(headerAction.getName(), headerAction.getValue());
            }
            if (!"GET".equalsIgnoreCase(method) && contentAction != null) {
                if (conn.getRequestProperty("Content-Type") == null) {
                    conn.setRequestProperty("Content-Type", contentAction.getContentType());
                }
                byte[] contentBytes = contentAction.getContentBytes();
                conn.setRequestProperty("Content-Length", String.valueOf(contentBytes.length));
                try (OutputStream outputStream = conn.getOutputStream()) {
                    outputStream.write(contentBytes);
                    outputStream.flush();
                }
            }

            int responseCode = conn.getResponseCode();
            String responseMessage = conn.getResponseMessage();
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            Long contentLength = XUtils.toLong(conn.getHeaderField("Content-Length"));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream rawInputStream = responseCode >= 200 && responseCode < 400 ? conn.getInputStream() : conn.getErrorStream();
            if (rawInputStream != null) {
                try (InputStream inputStream = rawInputStream) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        if (contentLength != null && out.size() == contentLength) {
                            break;
                        }
                    }
                }
            }
            byte[] responseContentBytes = out.toByteArray();
            httpResponse = new HttpResponse(responseCode, responseMessage, new HttpHeaders(headerFields), responseContentBytes, encoding);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        storeResponseVariables(actionParam, getName(), httpResponse);
        if (responseAction != null) {
            responseAction.setHttpResponse(httpResponse);
            responseAction.execute(actionParam, this);
        }
    }

    private void storeResponseVariables(ActionParam actionParam, String name, HttpResponse httpResponse) {
        if (XUtils.isEmpty(name)) {
            return;
        }
        super.setVariable(actionParam, null, name, new HttpResponseVeriable(httpResponse));
        super.setVariable(actionParam, "this", name + ".code", NumberVariable.of(httpResponse.code));
        super.setVariable(actionParam, "this", name + ".message", StringVariable.ofNotNull(httpResponse.message));
        super.setVariable(actionParam, "this", name + ".content", BytesVariable.of(httpResponse.responseContentBytes));
        super.setVariable(actionParam, "this", name + ".headers", MapVariable.of(httpResponse.headers));
    }

    public static class HttpResponseAction extends CodeBlockAction.AbstractCodeBlockAction {
        private HttpResponse httpResponse;
        private String to = "response";
        public void setHttpResponse(HttpResponse httpResponse) {
            this.httpResponse = httpResponse;
        }

        @Override
        protected void callCodeBlockExecute(ActionParam actionParam, Action parentAction, List<Action> actions) throws Exception {
            String responseName = XUtils.isEmpty(to) ? "response" : to;
            setVariable(actionParam, "this", responseName, new HttpResponseVeriable(httpResponse));
            setVariable(actionParam, "this", responseName + ".code", NumberVariable.of(httpResponse.code));
            setVariable(actionParam, "this", responseName + ".message", StringVariable.ofNotNull(httpResponse.message));
            setVariable(actionParam, "this", responseName + ".content", BytesVariable.of(httpResponse.responseContentBytes));
            setVariable(actionParam, "this", responseName + ".headers", MapVariable.of(httpResponse.headers));
            actions = parseChildren(actionParam.getActionFactory().copy());
            super.callCodeBlockExecute(actionParam, parentAction, actions);
        }
    }

    public static class HttpHeaders extends HashMap<String, List<String>> {
        public HttpHeaders() {
            super();
        }

        public HttpHeaders(Map<String, List<String>> map) {
            super(map);
        }

        public String getHeader(String name) {
            List<String> strings = get(name);
            if (strings != null && !strings.isEmpty()) {
                return strings.get(0);
            }
            for (Map.Entry<String, List<String>> entry : entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                    List<String> values = entry.getValue();
                    return values == null || values.isEmpty() ? null : values.get(0);
                }
            }
            return null;
        }
    }

    public static class HttpResponse {
        public final int code;
        public final String message;
        public final HttpHeaders headers;
        public final byte[] responseContentBytes;
        public final String encoding;
        private final String content;
        public HttpResponse(int code, String message, HttpHeaders headers, byte[] responseContentBytes, String encoding) {
            this.code = code;
            this.message = message;
            this.headers = headers;
            this.responseContentBytes = responseContentBytes;
            this.encoding = encoding;
            if (responseContentBytes != null) {
                this.content = new String(responseContentBytes, Charset.forName(encoding));
            } else  {
                this.content = null;
            }
        }

        public byte[] contentBytes() {
            return responseContentBytes;
        }

        public int code() {
            return code;
        }

        public Map<String, List<String>> headers() {
            return headers;
        }

        public String message() {
            return message;
        }

        public String content() {
            return this.content;
        }



        @Override
        public String toString() {
            if (responseContentBytes != null) {
                return new String(responseContentBytes, Charset.forName(encoding));
            } else  {
                return null;
            }
        }
    }


    public static class HttpHeaderAction extends Action.AbstractAction {

        private String name;
        private String value;

        @Override
        protected void callExecute(ActionParam actionParam, Action action) throws Exception {
            if (XUtils.isEmpty(name)) {
                throw new IllegalArgumentException("<header> name is required");
            }
        }

        @Override
        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    public static class HttpContentAction extends Action.AbstractAction {

        @NotAttr
        private byte[] contentBytes;
        @NotAttr
        private String contentType = "application/json;charset=utf-8";
        @NotAttr
        private String encoding = "utf-8";

        private String ref;

        public void setEncoding(String encoding) {
            if (XUtils.isEmpty(encoding)) {
                encoding = "utf-8";
            }
            this.encoding = encoding;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getEncoding() {
            return encoding;
        }

        public String getContentType() {
            return contentType;
        }

        @Override
        protected void callExecute(ActionParam actionParam, Action action) throws Exception {
            if (!XUtils.isEmpty(ref)) {
                Variable variable = getVariable(ref);
                if (!Variable.isExist(variable)) {
                    throw new IllegalArgumentException("<content ref=\"" + ref + "\"> variable does not exist");
                }
                if (variable instanceof ListVariable || variable instanceof MapVariable) {
                    if (contentType.contains("application/json") || contentType.contains("text/plain")) {
                        this.contentBytes = XUtils.toJsonString(variable.getValue()).getBytes(Charset.forName(encoding));
                    } else if (contentType.contains("application/x-www-form-urlencoded") && variable instanceof MapVariable) {
                        Map<?, ?> map = (Map<?, ?>) variable.getValue();
                        StringBuilder sb = new StringBuilder();
                        for (Map.Entry<?, ?> entry : map.entrySet()) {
                            if (sb.length() > 0) {
                                sb.append("&");
                            }
                            sb.append(entry.getKey()).append("=")
                                    .append(URLEncoder.encode(String.valueOf(entry.getValue()), encoding));
                        }
                        this.contentBytes = sb.toString().getBytes(Charset.forName(encoding));
                    } else {
                        throw new IllegalArgumentException("List or Map Content-Type must be application/json, text/plain or x-www-form-urlencoded");
                    }
                } else if (variable instanceof BytesVariable) {
                    this.contentBytes = ((BytesVariable) variable).getValue();
                } else if (variable instanceof FileVariable) {
                    File file = ((FileVariable) variable).getValue();
                    if (file.exists()) {
                        this.contentBytes = XUtils.readFileToBytes(file);
                    } else {
                        throw new IllegalArgumentException("<content ref=\""+ref+"\"> File not exists: " + file);
                    }
                } else {
                    this.contentBytes = XUtils.toBytes(variable.getStringValue(), encoding);
                }
            } else {
                String value = getValue();
                this.contentBytes = XUtils.toBytes(value == null ? null : value.trim(), encoding);
            }
        }

        public byte[] getContentBytes() {
            return contentBytes;
        }

        public boolean isBytes() {
            return contentBytes != null;
        }

    }
}
