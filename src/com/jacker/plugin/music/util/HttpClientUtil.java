package com.jacker.plugin.music.util;


import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;


/**
 * http工具类 封装了apache httpclient 使用了连接池相关的api 需要配置每个ip+端口，链接的数量
 *
 * @author bjzhaojh
 */
public class HttpClientUtil {

    public static final Logger log = Logger.getLogger("HttpClientUtil");

    /**
     * 默认的编码方式
     */
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final String HTTPS_PREFIX = "https";
    public static final String HTTP_PREFIX = "http";
    /**
     * 默认的connect timeout 10s
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 20000;
    /**
     * 默认的read timeout 10s 从连接池去连接的超时时间
     */
    private static final int DEFAULT_READ_TIMEOUT = 20000;
    /**
     * 默认的socket timeout 10s
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 30000;
    /**
     * 10分钟报一次警
     */
    private static final long TIME = 600000L;

    private static final String JSON_CONTENT_TYPE = "application/json";

    // 初始化httpclient
    private static CloseableHttpClient httpClient;
    private static Map<HttpHost, Long> hostAliamMap = new HashMap<>();

    private enum BODY_TYPE {
        PARAM, JSON
    }

    static {
        PoolingHttpClientConnectionManager cm = buildConnectionManager();
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(DEFAULT_SOCKET_TIMEOUT).build();
        cm.setDefaultSocketConfig(socketConfig);
        cm.closeIdleConnections(30, TimeUnit.MINUTES);
        cm.setMaxTotal(200);
        ConnectionConfig defaultConnectionConfig = ConnectionConfig.custom().setCharset(DEFAULT_CHARSET).build();
        cm.setDefaultConnectionConfig(defaultConnectionConfig);
        cm.setDefaultMaxPerRoute(20);
        httpClient = HttpClients.custom().setConnectionManager(cm).build();
    }

    public static CloseableHttpClient getSslHttpClient(Charset charset) {
        try {
            ConnectionConfig config = ConnectionConfig.custom().setCharset(charset).build();
            return HttpClients.custom().setDefaultConnectionConfig(config).setSSLSocketFactory(buildSSLConnectionFactory()).build();
        } catch (Exception e) {
            log.warning("getSslHttpClient failed");
        }
        return null;
    }

    public static CloseableHttpClient getSslHttpClientWithRetryHandler(Charset charset) {
        try {
            ConnectionConfig config;
            if (charset != null) {
                config = ConnectionConfig.custom().setCharset(charset).build();
            } else {
                config = ConnectionConfig.custom().build();
            }
            return HttpClients.custom().setDefaultConnectionConfig(config).setSSLSocketFactory(buildSSLConnectionFactory())
                    .setRetryHandler(new DefaultHttpRequestRetryHandler(3, false)).build();
        } catch (Exception e) {
            log.warning("getSslClient");
        }
        return null;
    }

    public static Pair<String, Header[]> get(String url) throws Exception {
        return get(url, null);
    }

    public static Pair<String, Header[]> get(String url, Map<String, String> params) throws Exception {
        return execute("GET", DEFAULT_SOCKET_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, url, params);
    }

    public static Pair<String, Header[]> getShort(String url, Map<String, String> params) throws Exception {
        return execute("GET", DEFAULT_SOCKET_TIMEOUT / 10, DEFAULT_CONNECT_TIMEOUT / 10, DEFAULT_READ_TIMEOUT / 10, url, params);
    }

    public static Pair<String, Header[]> get(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
        return execute("GET", DEFAULT_SOCKET_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, url, params, headers);
    }

    public static Pair<String, Header[]> getLong(String url, Map<String, String> params) throws Exception {
        return execute("GET", 2 * DEFAULT_SOCKET_TIMEOUT, 2 * DEFAULT_CONNECT_TIMEOUT, 3 * DEFAULT_READ_TIMEOUT, url, params);
    }

    public static Pair<String, Header[]> getLong(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
        return execute("GET", 2 * DEFAULT_SOCKET_TIMEOUT, 2 * DEFAULT_CONNECT_TIMEOUT, 3 * DEFAULT_READ_TIMEOUT, url, params,
                headers);
    }

    public static Pair<String, Header[]> getVeryLong(String url, Map<String, String> params) throws Exception {
        return execute("GET", 5 * DEFAULT_SOCKET_TIMEOUT, 5 * DEFAULT_CONNECT_TIMEOUT, 30 * DEFAULT_READ_TIMEOUT, url, params);
    }

    public static Pair<String, Header[]> post(String url) throws Exception {
        return post(url, null);
    }

    public static Pair<String, Header[]> post(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
        return execute("POST", DEFAULT_SOCKET_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, url, params, headers);
    }

    public static Pair<String, Header[]> post(String url, Map<String, String> params) throws Exception {
        return execute("POST", DEFAULT_SOCKET_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, url, params);
    }

    public static Pair<String, Header[]> post(String url, Map<String, String> params, int connectTimeout) throws Exception {
        return execute("POST", DEFAULT_SOCKET_TIMEOUT, connectTimeout, DEFAULT_READ_TIMEOUT, url, params);
    }

    public static Pair<String, Header[]> postIgnoreStatusCode(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
        return execute("POST", DEFAULT_SOCKET_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, url, params, headers,
                BODY_TYPE.PARAM, false);
    }

    public static Pair<String, Header[]> postLong(String url, Map<String, String> params) throws Exception {
        return execute("POST", 4 * DEFAULT_SOCKET_TIMEOUT, 2 * DEFAULT_CONNECT_TIMEOUT, 2 * DEFAULT_READ_TIMEOUT, url, params);
    }

    public static Pair<String, Header[]> postShort(String url, Map<String, String> params) throws Exception {
        return execute("POST", DEFAULT_SOCKET_TIMEOUT / 3, DEFAULT_CONNECT_TIMEOUT / 3, DEFAULT_READ_TIMEOUT / 3, url, params);
    }

    public static Pair<String, Header[]> postVeryShort(String url, Map<String, String> params) throws Exception {
        return execute("POST", 200, 200, 200, url, params);
    }

    public static Pair<String, Header[]> execute(String method, int socketTimeout, int connectTimeout, int readTimeout, String url,
                                                 Map<String, String> params) throws Exception {
        return execute(method, socketTimeout, connectTimeout, readTimeout, url, params, null);
    }

    /**
     * POST json
     *
     * @param url
     * @param json
     * @return
     * @throws Exception
     */
    public static Pair<String, Header[]> postJson(String url, String json, Map<String, String> headers) throws Exception {
        return execute("POST", DEFAULT_SOCKET_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, url, json, headers,
                BODY_TYPE.JSON);
    }

    public static Pair<String, Header[]> postJsonLong(String url, String json, Map<String, String> headers) throws Exception {
        return execute("POST", DEFAULT_SOCKET_TIMEOUT * 3, DEFAULT_CONNECT_TIMEOUT * 3, DEFAULT_READ_TIMEOUT * 3, url, json, headers,
                BODY_TYPE.JSON);
    }

    /**
     * 用连接池的http请求.
     *
     * @param method
     * @param socketTimeout  socket连接超时时间
     * @param connectTimeout http连接超时时间
     * @param readTimeout    http读取超时时间
     * @param url            请求的url地址
     * @param params         请求参数 string形式的键值对
     * @return 响应的内容体
     * @throws Exception 异常
     */
    public static Pair<String, Header[]> execute(String method, int socketTimeout, int connectTimeout, int readTimeout, String url,
                                                 Map<String, String> params, Map<String, String> headers) throws Exception {
        return execute(method, socketTimeout, connectTimeout, readTimeout, url, params, headers, BODY_TYPE.PARAM);
    }

    private static Pair<String, Header[]> execute(String method, int socketTimeout, int connectTimeout, int readTimeout, String url, Object body,
                                                  Map<String, String> headers, BODY_TYPE bodyType) throws Exception {
        return execute(method, socketTimeout, connectTimeout, readTimeout, url, body, headers, bodyType, true);
    }

    private static Pair<String, Header[]> execute(String method, int socketTimeout, int connectTimeout, int readTimeout, String url, Object body,
                                                  Map<String, String> headers, BODY_TYPE bodyType, boolean checkStatusCode) throws Exception {
        if (StringUtils.isEmpty(url)) {
            throw new Exception("httpclient execute url is empty");
        }
        HttpRequestBase request = null;
        switch (bodyType) {
            case JSON:
                if (body instanceof String) {
                    request = getHttpRequest(method, url, (String) body);
                }
                break;
            default: {
                request = getHttpRequest(method, url, (Map<String, String>) body);
            }
        }
        if (request == null) {
            throw new Exception("httpclient illegal body type");
        }
        RequestConfig config = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(readTimeout).build();
        request.setConfig(config);
        request.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.107 Safari/537.36");
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                request.setHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        CloseableHttpResponse response = execute(request, url);
        return parseResponse(null, response, url, checkStatusCode);
    }

    public static Pair<String, Header[]> parseResponse(CloseableHttpClient client, CloseableHttpResponse response, String url)
            throws Exception {
        return parseResponse(client, response, url, true);
    }

    private static Pair<String, Header[]> parseResponse(CloseableHttpClient client, CloseableHttpResponse response, String url,
                                                        boolean checkStatusCode) throws Exception {
        StatusLine status = response.getStatusLine();
        Header[] allHeaders = response.getAllHeaders();
        try {
            HttpEntity entity = response.getEntity();
            if (checkStatusCode && status.getStatusCode() != 200 && status.getStatusCode() != 201) {
                log.warning("http error,url=" + url + ",resp =" + EntityUtils.toString(entity, DEFAULT_CHARSET));
                return null;
            } else {
                return Pair.of(EntityUtils.toString(entity, DEFAULT_CHARSET), allHeaders);
            }
        } catch (Exception e) {
            log.warning(String.format("http exception occured, url: %s, errMsg: %s", url, e.getMessage()));
            throw e;
        } finally {
            if (client != null) {
                client.close();
            }
            response.close();
        }
    }

    public static CloseableHttpResponse execute(HttpRequestBase request, String url) throws Exception {
        CloseableHttpResponse response;
        CloseableHttpClient hc = httpClient;
        try {
            response = hc.execute(request);
        } catch (Exception e) {
            if (e instanceof HttpHostConnectException) {
                HttpHostConnectException httpHostConnectException = (HttpHostConnectException) e;
                connectExceptionAlarm(httpHostConnectException);
            } else {
                log.warning(String.format("http exception occured, url: %s, errMsg: %s", url, e.getMessage()));
            }
            if (request != null) {
                // 出错时释放连接
                log.info("error in executing http request, will release connection. url: " + url);
                request.releaseConnection();
            }
            throw e;
        }
        return response;
    }

    public static boolean isExists(String url) {
        return isExists("GET", DEFAULT_SOCKET_TIMEOUT, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT, url, null);
    }

    public static boolean isExists(String method, int socketTimeout, int connectTimeout, int readTimeout, String url,
                                   Map<String, String> params) {
        HttpRequestBase request;
        CloseableHttpResponse response = null;
        try {
            long start = System.currentTimeMillis();
            if (StringUtils.isEmpty(url)) {
                return false;
            }
            request = getHttpRequest(method, url, params);
            RequestConfig config =
                    RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout)
                            .setConnectionRequestTimeout(readTimeout).build();
            request.setConfig(config);
            request.setHeader("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36");
            CloseableHttpClient hc = httpClient;
            response = hc.execute(request);
            long end = System.currentTimeMillis();
            if (end - start > 500) {
                log.info(String.format("httpclient execute,url=%1$s,time=%2$d", url, (end - start)));
            }
            StatusLine status = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            if (status.getStatusCode() == 200 || status.getStatusCode() == 201) {
                return true;
            } else {
                log.warning("http error,url=" + url + ",resp =" + EntityUtils.toString(entity, DEFAULT_CHARSET));
                return false;
            }
        } catch (Exception e) {
            log.warning(String.format("http exception occured, url: %s, errMsg: %s", url, e.getMessage()));
            return false;
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (Exception ex) {
                log.warning("close resource failed" + ex.getMessage());
            }
        }
    }

    public static HttpRequestBase getHttpRequest(String method, String url, Map<String, String> params) throws Exception {
        HttpRequestBase request;
        if ("POST".equals(method)) {
            HttpPost httpPost = new HttpPost(url);
            if (MapUtils.isNotEmpty(params)) { // 添加请求参数
                List<NameValuePair> list = new ArrayList<>(params.size());
                for (Map.Entry<String, String> ent : params.entrySet()) {
                    if (StringUtils.isBlank(ent.getKey())) {
                        try {
                            httpPost.setEntity(new StringEntity(ent.getValue(), "utf-8"));
                        } catch (Exception e) {
                            log.warning("excute, httpPost.setEntity failed caused by " + e.getMessage());
                        }
                    } else {
                        list.add(new BasicNameValuePair(ent.getKey(), ent.getValue()));
                    }
                }
                if (!list.isEmpty()) {
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, DEFAULT_CHARSET);
                    httpPost.setEntity(entity);
                }
            }
            request = httpPost;
        } else {
            request = new HttpGet(appendParams(url, params));
        }
        return request;
    }

    private static HttpRequestBase getHttpRequest(String method, String url, String body) {
        HttpRequestBase request;
        StringEntity entity = new StringEntity(body, DEFAULT_CHARSET);
        entity.setContentType(JSON_CONTENT_TYPE);
        switch (method) {
            case "PUT":
                HttpPut httpPut = new HttpPut(url);
                httpPut.setEntity(entity);
                request = httpPut;
                break;
            default:
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(entity);
                request = httpPost;
        }
        return request;
    }

    public static boolean exists(String URLName, String method) {
        HttpURLConnection con = null;
        try {
            // 设置此类是否应该自动执行 HTTP 重定向（响应代码为 3xx 的请求）。
            HttpURLConnection.setFollowRedirects(true);
            // 到 URL 所引用的远程对象的连接
            con = (HttpURLConnection) new URL(URLName).openConnection();
            /* 设置 URL 请求的方法， GET POST HEAD OPTIONS PUT DELETE TRACE 以上方法之一是合法的，具体取决于协议的限制。 */
            if ("post".equalsIgnoreCase(method)) {
                // 设置连接输出流为true,默认false (post 请求是以流的方式隐式的传递参数)
                con.setDoOutput(true);
                // 设置连接输入流为true
                con.setDoInput(true);
            }
            // 从 HTTP 响应消息获取状态码
            return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        } catch (Exception e) {
            log.warning("exists url error" + URLName + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }

    public static boolean exists(String URLName) {
        return exists(URLName, "GET") || isExists(URLName);
    }

    static String appendParams(String url, Map<String, String> params) throws Exception {
        return appendParams(null, url, params);
    }

    static String appendParams(String charset, String url, Map<String, String> params) throws Exception {
        if (MapUtils.isEmpty(params)) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        for (Map.Entry<String, String> ent : params.entrySet()) {
            if (StringUtils.isNotBlank(ent.getValue())) {
                if (sb.indexOf("?") == -1) {
                    sb.append("?");
                } else {
                    sb.append("&");
                }
                if (StringUtils.isEmpty(charset)) {
                    charset = DEFAULT_CHARSET.name();
                }
                sb.append(ent.getKey()).append("=").append(URLEncoder.encode(ent.getValue(), charset));
            }
        }
        return sb.toString();
    }

    public static void connectExceptionAlarm(HttpHostConnectException e) {
        long curr = System.currentTimeMillis();
        HttpHost host = e.getHost();
        if (hostAliamMap.containsKey(host)) {
            if (curr - hostAliamMap.get(host) < TIME) {
                return;
            }
        }
        hostAliamMap.put(host, curr);
        log.warning("http connection error " + host.toHostString() + e);
    }

    public static CloseableHttpClient getHttpClient(Charset charset) {
        ConnectionConfig config = ConnectionConfig.custom().setCharset(charset).build();
        return HttpClients.custom().setDefaultConnectionConfig(config).build();
    }

    public static CloseableHttpClient getHttpClientWithRetry(Charset charset) {
        ConnectionConfig config;
        if (charset != null) {
            config = ConnectionConfig.custom().setCharset(charset).build();
        } else {
            config = ConnectionConfig.custom().build();
        }
        return HttpClients.custom().setDefaultConnectionConfig(config).setRetryHandler(new DefaultHttpRequestRetryHandler(3, false))// 默认重试三次
                .build();
    }

    public static CloseableHttpClient getSslHttpClientWithRetry(Charset charset) {
        try {
            ConnectionConfig config = ConnectionConfig.custom().setCharset(charset).build();
            return HttpClients.custom().setDefaultConnectionConfig(config).setSSLSocketFactory(buildSSLConnectionFactory()).
                    setRetryHandler(new DefaultHttpRequestRetryHandler(3, false)).build();
        } catch (Exception e) {
            log.warning("getSslHttpClient failed" + e.getMessage());
        }
        return null;
    }

    /**
     * 不用连接池的http请求post. http读取超时时间
     *
     * @param url    请求的url地址
     * @param params 请求参数 string形式的键值对
     * @return 响应的内容体
     * @throws Exception 异常
     */
    public static Pair<String, Header[]> postNoPool(Charset charset, String url, Map<String, String> params) throws Exception {
        CloseableHttpClient httpclient;
        if (url.startsWith(HTTPS_PREFIX)) {
            httpclient = getSslHttpClient(charset);
        } else {
            httpclient = getHttpClient(charset);
        }
        HttpPost httpPost = new HttpPost(url);
        if (MapUtils.isNotEmpty(params)) {
            List<NameValuePair> nvps = params.entrySet().stream().map(ent -> new BasicNameValuePair(ent.getKey(), ent.getValue()))
                    .collect(Collectors.toList());
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvps, charset);
            httpPost.setEntity(entity);
        }
        CloseableHttpResponse response = null;
        if (httpclient != null) {
            response = httpclient.execute(httpPost);
        }
        return parseResponse(httpclient, response, url);
    }

    /**
     * 不用连接池的http请求get
     *
     * @param charset
     * @param url
     * @param params
     * @return
     * @throws Exception
     */
    public static String getNoPool(Charset charset, String url, Map<String, String> params) throws Exception {
        CloseableHttpClient httpclient;
        if (url.startsWith(HTTPS_PREFIX)) {
            httpclient = getSslHttpClient(charset);
        } else {
            httpclient = getHttpClient(charset);
        }
        HttpGet httpPost = new HttpGet(appendParams(charset.name(), url, params));
        CloseableHttpResponse response = null;
        if (httpclient != null) {
            response = httpclient.execute(httpPost);
        }
        if (response == null) {
            return null;
        }
        try {
            if (200 == response.getStatusLine().getStatusCode() || 201 == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, charset);
            } else {
                log.warning(String.format("httpget,url=%1$s,status=%2$d,msg=%3$s", appendParams(url, params), response.getStatusLine()
                        .getStatusCode(), response.getStatusLine().getReasonPhrase()));
            }
        } catch (Exception e) {
            log.warning(String.format("failed to http get without pool, url: %s, params: %s", url, params) + e.getMessage());
        } finally {
            response.close();
            httpclient.close();
        }
        return null;
    }

    public static String post(HttpPost post) throws Exception {
        BasicHttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        cm.setConnectionConfig(ConnectionConfig.custom().setCharset(DEFAULT_CHARSET).build());
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            httpclient = HttpClients.custom().setConnectionManager(cm).build();
            response = httpClient.execute(post);
            if (200 == response.getStatusLine().getStatusCode() || 201 == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, DEFAULT_CHARSET);
            } else {
                log.warning(String.format("httppost,status=%1$d,msg=%2$s", response.getStatusLine().getStatusCode(), response.getStatusLine()
                        .getReasonPhrase()));
            }
        } finally {
            if (response != null) {
                response.close();
            }
            if (httpclient != null) {
                httpclient.close();
            }
            cm.shutdown();
            cm.close();
        }
        return null;
    }

    public static InputStream getStream(String url) throws Exception {
        BasicHttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        cm.setConnectionConfig(ConnectionConfig.custom().setCharset(DEFAULT_CHARSET).build());
        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(cm).build();
        HttpGet get = new HttpGet(url);
        RequestConfig config = RequestConfig.custom().setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT).setConnectionRequestTimeout(DEFAULT_READ_TIMEOUT).build();
        get.setConfig(config);
        CloseableHttpResponse response = httpclient.execute(get);
        return response.getEntity().getContent();
    }

    public static InputStream getDefaultStream(String url) throws Exception {
        BasicHttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        cm.setConnectionConfig(ConnectionConfig.custom().setCharset(DEFAULT_CHARSET).build());
        HttpGet get = new HttpGet(url);
        RequestConfig config = RequestConfig.custom().setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
                .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT).setConnectionRequestTimeout(DEFAULT_READ_TIMEOUT).build();
        get.setConfig(config);
        CloseableHttpResponse response = httpClient.execute(get);
        return response.getEntity().getContent();
    }

    /**
     * post 数据到指定地址，并获取返回结果. Multipart
     *
     * @throws IOException
     */
    public static String postContentBySsl(String url, String paraStr, String encoding) throws IOException {
        CloseableHttpClient httpClient = getSslHttpClient(Charset.forName(encoding));
        // 重试
        // 超时
        RequestConfig config = RequestConfig.custom().setSocketTimeout(15000).setConnectTimeout(15000).build();
        String res = null;
        HttpPost httppost = new HttpPost(url);
        httppost.setConfig(config);
        HttpEntity reqEntity = new StringEntity(paraStr, encoding);
        httppost.setEntity(reqEntity);
        httppost.setHeader("Content-Type", " application/x-www-form-urlencoded");
        CloseableHttpResponse response = null;
        if (httpClient != null) {
            response = httpClient.execute(httppost);
        }
        if (response == null) {
            return null;
        }
        try {
            res = EntityUtils.toString(response.getEntity(), encoding);
        } finally {
            response.close();
            httpClient.close();
        }
        return res;
    }


    public static String multiPartPost(String url, Map<String, String> headers, Map<String, String> stringParams,
                                       Map<String, File> fileParams, Map<String, byte[]> bytesParams) throws Exception {
        BasicHttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        cm.setConnectionConfig(ConnectionConfig.custom().setCharset(DEFAULT_CHARSET).build());
        HttpPost httpPost = new HttpPost(appendParams("UTF-8", url, stringParams));
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        if (fileParams != null && fileParams.size() > 0) {
            for (Map.Entry<String, File> entry : fileParams.entrySet()) {
                multipartEntityBuilder.addBinaryBody(entry.getKey(), entry.getValue());
            }
        }
        if (bytesParams != null && bytesParams.size() > 0) {
            for (Map.Entry<String, byte[]> entry : bytesParams.entrySet()) {
                multipartEntityBuilder.addBinaryBody(entry.getKey(), entry.getValue());
            }
        }
        HttpEntity httpEntity = multipartEntityBuilder.build();
        httpPost.setEntity(httpEntity);
        RequestConfig config =
                RequestConfig.custom().setSocketTimeout(4 * DEFAULT_SOCKET_TIMEOUT).setConnectTimeout(4 * DEFAULT_CONNECT_TIMEOUT)
                        .setConnectionRequestTimeout(4 * DEFAULT_READ_TIMEOUT).build();
        httpPost.setConfig(config);
        if (MapUtils.isNotEmpty(headers)) {
            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
                httpPost.setHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        try {
            httpclient = HttpClients.custom().setConnectionManager(cm).build();
            response = httpClient.execute(httpPost);
            if (200 == response.getStatusLine().getStatusCode() || 201 == response.getStatusLine().getStatusCode()
                    || 400 == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity, DEFAULT_CHARSET);
            } else {
                log.warning(String.format("httpget,url=%1$s,status=%2$d,msg=%3$s", url, response.getStatusLine().getStatusCode(), response
                        .getStatusLine().getReasonPhrase()));
            }
        } finally {
            if (response != null) {
                response.close();
            }
            if (httpclient != null) {
                httpclient.close();
            }
            cm.shutdown();
            cm.close();
        }
        return null;
    }

    public static Map<String, String> getCookieHeader(List<String[]> keyValues) {
        Map<String, String> headers = new HashMap<>();
        StringBuilder cookieValueBuilder = new StringBuilder();
        for (String[] kv : keyValues) {
            if (kv == null || kv.length != 2) {
                throw new IllegalArgumentException(String.format("illegal key value format: %s", kv));
            }
            cookieValueBuilder.append(kv[0]).append('=').append(kv[1]).append(';');
        }
        headers.put("Cookie", cookieValueBuilder.toString());
        return headers;
    }

    public static String postWithRawBody(String url, String body) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(body, DEFAULT_CHARSET);
        httpPost.setEntity(entity);
        RequestConfig requestConfig = RequestConfig.custom().
                setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).
                setConnectTimeout(DEFAULT_CONNECT_TIMEOUT).
                setConnectionRequestTimeout(DEFAULT_READ_TIMEOUT).build();
        httpPost.setConfig(requestConfig);
        CloseableHttpResponse response;
        try {
            response = httpClient.execute(httpPost);
        } catch (Exception e) {
            if (e instanceof HttpHostConnectException) {
                HttpHostConnectException hostConnectException = (HttpHostConnectException) e;
                log.warning(hostConnectException.getHost().toHostString() + e.getMessage());
            }
            throw e;
        }
        StatusLine statusLine = response.getStatusLine();
        String responseEntity = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
        if (statusLine.getStatusCode() != 200) {
            log.warning("http status error, url = " + url + ", status code = " + statusLine.getStatusCode() + ", resp = " +
                    responseEntity);
            return null;
        }
        return responseEntity;
    }

    /**
     * 重试3次 GET
     *
     * @param charset
     * @param url
     * @param params
     * @return
     * @throws Exception
     */
    public static String getWithRetry(Charset charset, String url, Map<String, String> params) throws Exception {
        HttpGet httpGet = new HttpGet(appendParams(charset.name(), url, params));
        RequestConfig requestConfig = RequestConfig.custom().
                setSocketTimeout(DEFAULT_SOCKET_TIMEOUT).
                setConnectTimeout(DEFAULT_CONNECT_TIMEOUT).
                setConnectionRequestTimeout(DEFAULT_READ_TIMEOUT).build();
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        CloseableHttpClient httpClient = null;
        try {
            httpClient = null;
            if (url.startsWith(HTTPS_PREFIX)) {
                httpClient = getSslHttpClientWithRetry(charset);
            } else {
                httpClient = getHttpClientWithRetry(charset);
            }
            if (httpClient != null) {
                response = httpClient.execute(httpGet);
            }
            StatusLine statusLine = response.getStatusLine();
            String responeEntity = EntityUtils.toString(response.getEntity(), DEFAULT_CHARSET);
            if (statusLine.getStatusCode() != 200) {
                log.warning("get retry http status error, url = " + url + ", status code = " + statusLine.getStatusCode() +
                        ", resp = " + responeEntity);
                return null;
            }
            return responeEntity;
        } catch (Exception e) {
            if (e instanceof HttpHostConnectException) {
                HttpHostConnectException hostConnectException = (HttpHostConnectException) e;
                log.warning(hostConnectException.getHost().toHostString() + e.getMessage());
            }
            throw e;
        } finally {
            if (response != null) {
                response.close();
            }
            if (httpClient != null) {
                httpClient.close();
            }
        }

    }

    /**
     * 以下都是解决SSL CertificateException异常 而写
     *
     * @return
     * @throws FileNotFoundException
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private static SSLConnectionSocketFactory buildSSLConnectionFactory() throws FileNotFoundException, KeyStoreException,
            NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = new MySSLContextBuilder().useTLS().loadTrustMaterial(null, (chain, authType) -> true).build();
        return new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }

    // 解决连接池ssl验证异常的问题
    private static PoolingHttpClientConnectionManager buildConnectionManager() {
        SSLConnectionSocketFactory reuseStrategy = null;
        try {
            reuseStrategy = buildSSLConnectionFactory();
        } catch (Exception e) {
            log.warning("create ssl SSLConnectionSocketFactory error" + e.getMessage());
        }
        if (reuseStrategy == null) {
            return new PoolingHttpClientConnectionManager();
        } else {
            Registry registry = RegistryBuilder.create().register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", reuseStrategy).build();
            return new PoolingHttpClientConnectionManager(registry);
        }
    }

    static class MySSLContextBuilder extends SSLContextBuilder {
        private Set<TrustManager> trustManagers;

        public MySSLContextBuilder() {
            super();
            this.trustManagers = new HashSet<>();
        }

        static class TrustManagerDelegate extends X509ExtendedTrustManager {
            private final X509TrustManager trustManager;
            private final TrustStrategy trustStrategy;

            TrustManagerDelegate(final X509TrustManager trustManager, final TrustStrategy trustStrategy) {
                super();
                this.trustManager = trustManager;
                this.trustStrategy = trustStrategy;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, Socket socket) throws CertificateException {

            }

            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s, SSLEngine sslEngine) throws CertificateException {

            }

            @Override
            public void checkClientTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                this.trustManager.checkClientTrusted(chain, authType);
            }

            @Override
            public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                if (!this.trustStrategy.isTrusted(chain, authType)) {
                    this.trustManager.checkServerTrusted(chain, authType);
                }
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return this.trustManager.getAcceptedIssuers();
            }

        }

        @Override
        public SSLContextBuilder loadTrustMaterial(final KeyStore trustStore, final TrustStrategy trustStrategy)
                throws NoSuchAlgorithmException, KeyStoreException {
            final TrustManagerFactory tmFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmFactory.init(trustStore);
            final TrustManager[] tms = tmFactory.getTrustManagers();
            if (tms != null) {
                if (trustStrategy != null) {
                    for (int i = 0; i < tms.length; i++) {
                        final TrustManager tm = tms[i];
                        if (tm instanceof X509TrustManager) {
                            tms[i] = new TrustManagerDelegate((X509TrustManager) tm, trustStrategy);
                        }
                    }
                }
                Collections.addAll(this.trustManagers, tms);
            }
            return this;
        }
    }
}
