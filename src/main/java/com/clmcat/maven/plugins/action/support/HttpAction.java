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
public class HttpAction extends VariableAction {

    private String method = "GET";
    private String encoding = "UTF-8";
    private String url;

    public HttpAction() {
        setNameRequired(false);
    }

    @Override
    protected void callExecute(ActionParam actionParam, Action parentAction) throws Exception {
        String encoding = this.encoding;
        String method = this.method;
        HttpResponse httpResponse = null;
        // 执行http请求
        ActionFactory actionFactory = actionParam.getActionFactory().create();
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
                // 提取header节点
                headerActions.add((HttpHeaderAction) children);
            } else if (children instanceof HttpContentAction) {
                // 提取content节点
                contentAction = (HttpContentAction) children;
                contentAction.setEncoding(this.encoding);
            } else if (children instanceof HttpResponseAction) {
                responseAction = (HttpResponseAction) children;
            }
        }
        for (HttpHeaderAction headerAction : headerActions) {
            headerAction.execute(actionParam, parentAction);
            if (headerAction.getName().equals("content-type")) {
                contentAction.setContentType(headerAction.getValue());
            } else if (headerAction.getName().equals("encoding")) {
                encoding = headerAction.getValue();
                contentAction.setEncoding(encoding);
            }
        }
        if (contentAction != null) {
            contentAction.execute(actionParam, parentAction);
        }
        String url = XUtils.toUrlString(actionParam.format(this.url), encoding);
        HttpURLConnection conn = null;
        try {
            // 1. 创建URL对象
            URL realUrl = new URL(url);
            conn = (HttpURLConnection) realUrl.openConnection();

            // 2. 基础配置
            conn.setRequestMethod(method.toUpperCase()); // 请求方式POST
            conn.setConnectTimeout(5000);  // 连接超时5秒
            conn.setReadTimeout(5000);     // 读取超时5秒
            conn.setDoOutput(true);        // 允许写入请求体
            conn.setDoInput(true);         // 允许读取响应


            // 3. 设置请求头信息
            for (HttpHeaderAction headerAction : headerActions) {
                conn.setRequestProperty(headerAction.getName(), headerAction.getValue());
            }

            // 4. 写入请求体（Body
            if (!"GET".equalsIgnoreCase(method) && contentAction != null) {
                if (conn.getRequestProperty("Content-Type") == null) {
                    conn.setRequestProperty("Content-Type", contentAction.getContentType());
                }
                byte[] contentBytes = contentAction.getContentBytes();
                conn.setRequestProperty("Content-Length", String.valueOf(contentBytes.length));
                conn.getOutputStream().write(contentBytes);
            }

            int responseCode = conn.getResponseCode();
            String responseMessage = conn.getResponseMessage();
            Map<String, List<String>> headerFields = conn.getHeaderFields();
            Long contentLength = XUtils.toLong(conn.getHeaderField("Content-Length"));
            // 5. 读取响应结果
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            try (InputStream inputStream = responseCode >= 200 && responseCode < 400 ? conn.getInputStream() : conn.getErrorStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    if (contentLength != null && out.size() == contentLength) {
                        break;
                    }
                }
            }

            byte[] responseContentBytes = out.toByteArray();
            httpResponse = new HttpResponse(responseCode, responseMessage, headerFields, responseContentBytes, encoding);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        String name = getName();
        setVariable(actionParam, new HttpResponseVeriable(httpResponse));
        setVariable(actionParam, "this", name+ ".code", NumberVariable.of(httpResponse.responseCode));
        setVariable(actionParam, "this", name + ".message", StringVariable.ofNotNull(httpResponse.responseMessage));
        setVariable(actionParam, "this", name + ".content", BytesVariable.of(httpResponse.responseContentBytes));
        setVariable(actionParam, "this", name + ".headers", MapVariable.of(new HttpHeaders(httpResponse.headerFields)));
        if (responseAction != null) {
            responseAction.setHttpResponse(httpResponse);
            responseAction.execute(actionParam, parentAction);
        }
    }

    public static class HttpResponseAction extends CodeBlockAction.AbstractCodeBlockAction {
        private HttpResponse httpResponse;
        private String to = "response";
        public void setHttpResponse(HttpResponse httpResponse) {
            this.httpResponse = httpResponse;
        }

        @Override
        protected void callCodeBlockExecute(ActionParam actionParam, Action parentAction, List<Action> actions) throws Exception {


            setVariable(actionParam, "this", to, new HttpResponseVeriable(httpResponse));
            setVariable(actionParam, "this", to + ".code", NumberVariable.of(httpResponse.responseCode));
            setVariable(actionParam, "this", to + ".message", StringVariable.ofNotNull(httpResponse.responseMessage));
            setVariable(actionParam, "this", to + ".content", BytesVariable.of(httpResponse.responseContentBytes));
            setVariable(actionParam, "this", to + ".headers", MapVariable.of(new HttpHeaders(httpResponse.headerFields)));
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
            return strings == null || strings.isEmpty() ? null : strings.get(0);
        }
    }

    public static class HttpResponse {
        private int responseCode;
        private String responseMessage;
        private Map<String, List<String>> headerFields;
        private byte[] responseContentBytes;
        private String encoding;
        public HttpResponse(int responseCode, String responseMessage, Map<String, List<String>> headerFields, byte[] responseContentBytes, String encoding) {
            this.responseCode = responseCode;
            this.responseMessage = responseMessage;
            this.headerFields = headerFields;
            this.responseContentBytes = responseContentBytes;
            this.encoding = encoding;
        }

        public byte[] contentBytes() {
            return responseContentBytes;
        }

        public int code() {
            return responseCode;
        }

        public Map<String, List<String>> headers() {
            return headerFields;
        }

        public String message() {
            return responseMessage;
        }

        public String content() {
            if (responseContentBytes != null) {
                return new String(responseContentBytes, Charset.forName(encoding));
            } else  {
                return null;
            }
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
                if (Variable.isExist(variable)) {
                    if (variable instanceof ListVariable || variable instanceof MapVariable) {
                        if (contentType.contains("application/json") || contentType.contains("text/plain")) {
                            this.contentBytes = XUtils.toJsonString(variable.getValue()).getBytes(Charset.forName(encoding));
                        } else if (contentType.contains("application/x-www-form-urlencoded") && variable instanceof MapVariable) {
                            Map<?, ?> map = (Map<?, ?>) variable.getValue();
                            StringBuffer sb = new StringBuffer();
                            map.forEach((k, v) -> {
                                try {
                                    sb.append(k).append("=").append(URLEncoder.encode(v.toString(), encoding)).append("&");
                                } catch (UnsupportedEncodingException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            this.contentBytes = sb.toString().getBytes(Charset.forName(encoding));
                        } else {
                            throw new IllegalArgumentException("List or Map Content-Type must be application/json or text/plain");
                        }
                    } else if (variable instanceof BytesVariable) {
                        this.contentBytes = ((BytesVariable) variable).getValue();
                    }else if (variable instanceof FileVariable) {
                        File file = ((FileVariable) variable).getValue();
                        if (file.exists()) {
                            this.contentBytes = XUtils.readFileToBytes(file);
                        } else {
                            throw new IllegalArgumentException("<content ref=\""+ref+"\"> File not exists: " + file);
                        }
                    } else {
                        this.contentBytes = XUtils.toBytes(variable.getStringValue(), encoding);
                    }
                }
            } else {
                this.contentBytes = XUtils.toBytes(getValue().trim(), encoding);
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