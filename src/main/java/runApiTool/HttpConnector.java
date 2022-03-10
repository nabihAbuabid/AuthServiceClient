package runApiTool;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import runApiTool.RequestResponse;

import javax.net.ssl.SSLContext;
/**
 * Base class for all HTTP requests. Suits REST client implementation. Usage is
 * straightforward, most of the parameters are configured via a constructor;
 * Override <code>configureParameters()</code> to add http parameters;
 */
public class HttpConnector {
    private enum NUM_OF_CONNECTIONS_MODE {SO, TOMCAT, REDIS, TIME_WAIT}

    private static final Timer loggingTimer = new Timer("NumOfCopnnectionsLoggingTimer", true);

    private static final String UTF8_BOM = "\uFEFF";
    private String private_key="MIIEpAIBAAKCAQEAyGf8DqRBprLHPLKeYGGTqBMwIfEH0jFnrR1wJ2GLevqwjEKsqm4OLl/jV3qcr1ptPAyFY0DIYlUk/8mh5lFzjU51+qxVeTZwq3e5g3CH5/Q7ib8BeOHVHdYAkh87TSnWphMWRwiQTvwStIiRbfDYitgo76Hr8CxoGTIMWYf9sUjWlePX yB6ycSvJ3g0mTAfCaY6M5+gsLsTOwOJQiAs1SQ4MOlDNqgVPQ9PMV+jSII4+RtROQgSJjslz7ay3dX4M+k52pa+JuIuCeFrcPQErlUK2w7gBvEOOycq6IZdSCCzxARIDHa5CkbzXMbOvMn2ujDmyNQ9wbzaog89c4qDTDwIDAQABAoIBAB8R5AWfGKCQCgySgrRdnGdL5kP02uoPB8xSio9Ic7fywslUEvHvUxqEejoXQ9B53AKZzFpJcmewXKada5DKIviO6AKfkjbTJl+nFadXnHJtLofaCY/kr0ZLZdZBqk95w5IG1obXwIvGT94WaDmbw+6uuZYBTBN1xwPU+w4sY7wG0QlaX1SCc31PDYGZGRy4YaOtwfQGNpk8Yq6X/w/gHH2qzvELBEAdiGwP/rld2OKM8gqCcPAP87To3e4ZfUz7YlViC0rM5A3PvhGjBmjiKHiJLnLpfzuYvRoWHe8Xd1oWsbDRF8UyJ6B3Rfzv2ZeyAy0655oHI9PLdnFWG7dJcT0CgYEA1gCWUZzOq/7+mnEAIx7CZ916QVzUQR86PXTW5hxGkLQ1pJ70Hg4Uy1z+h3vgrxaHQq0fVvoJn299rTxOtEtSJ7N4OkD61yZq+fkrA7e3IeEKMdXv8n/jIgc6O+xhlo8FX+6yUL/3Ggex1bWwlOEwZFBfy1z3o8OqSWZge827+o0CgYEA77xVR5yI2pleAaRKIbS1x7Fr+PUicLy/uEM4vRqFQQ0/Y5eIi0yHRbD6w/Mb8mbQe36VktOA7FfLmPax6o/bNhj1b67W3dBgwoNEJvtqyISfV8nd0yrKdKAd08eMa1Mbp5vlm/W8LT9tv6FhnF0UBgkn0spEX8n299sXf+EtCwsCgYEAmsI98rM1eoijUsjZUoySBk7SdKZPEPEmSv9N0YctOMQ57tzMqVeBjjeoEg5xw+zE0GEmQt37S4NzHW55dETsGq3dCjOnsyOjRTb99mhLVYLKvpN6PKJjfV5ArMkbIag4ONIDvgW1Cuv5nRURp/jZ6BF/1S9cHuAHK6GPsLhtcpkCgYB+7Q2RrpTed9jHsb31/oFHyu3Jj6++VJCE9EI0DLiEaoknJ3GJnuj3wu3hlPM08a5r0+dJJ2XYu9r1j5c/Aw8iozA/AyiLo+m20lzg7rfolh7vEde6F6u895ETMVFs+UFpCicU3ZPVuQFYNg9CBB233F0TQzfUJ0/0hjD46bU5ZQKBgQCSgWQWyrblIcFaVPWCdOmZ6H6H9cM8iV0pasvOmYMtlBmOnDM+UykvxXLFtyBs0dakk0ypQ9MAkMy9/XeSSSN1VNS59wkHKZWgDsZY3GspTAqExldcdWiZqIP7MDfI0qiE7nuugY0XsPku9phwLWrGo4ZYg+CS/Fpc/+eLfdoGIQ==";
    /**
     * Resource host, such as www.amazon.com
     */
    private String host;

    /**
     * Resource port. Typically 80 for HTTP, 443 for HTTPS and so on. Full list
     * of default ports:
     * http://en.wikipedia.org/wiki/List_of_TCP_and_UDP_port_numbers
     */
    private int port;

    /**
     * Path to resource, such as "about-cyren.html" in
     * http://www.cyren.com/about-cyren.html
     */
    protected String path;

    private static final int DEFAULT_PORT = 80;
    private static final int DEFAULT_MAX_CONNECTIONS = 200;
    private static final int DEFAULT_MAX_CONNECTIONS_PER_ROUTE = 200;
    private static final String LOGIN_UI_CONFIG = "loginUIConfig";
    final static String KINESIS_KEY = "kinesis";
    private static final String MAX_CONNECTIONS = "maxConnections";
    private static final String MAX_401_RETRY_COUNT = "max401RetryCount";
    private static final String MAX_CONNECTIONS_PER_ROUTE = "maxConnectionsPerRoute";
    private static final String RESET_PASSWORD_CONFIG ="resetPasswords3Settings";
    private static boolean isInternalMonitoringStarted = false;
    private static final Object lockObj = new Object();
    public static final String PROVISIONING_SERVICE_DOCKER = "PROVISIONING-SERVICE-DOCKER-%s";
    private static final String METRICS_NAMESPACE = "metricsNamespace";
    private static final String METRICS_NAMESPACE_DEFAULT = "AuthSvcConnections";
    private static final String DIMENSION_NAME = "dimensionName";
    private static final String DIMENSION_NAME_DEFAULT = "NumOfConn-";

    //The timeouts are in millis
    private int socketTimeout;
    private int connectionTimeout;
    private String proxyHost;
    private int proxyPort;
    private RequestConfig requestConfig;

    public String getPod() {
        return pod;
    }

    public String getKibanaRegion() {
        return kibanaRegion;
    }

    private String pod;
    private String kibanaRegion;
    private CloseableHttpClient httpClient = null;

    private PoolingHttpClientConnectionManager connManager;

    public enum HttpMethod {
        GET, PUT, POST, DELETE, HEAD

    }

    HttpConnector() {
    }

    public HttpConnector(String host, Integer port, String path,
                         String proxyHost, int proxyPort,
                         int socketTimeout, int connectionTimeout, String pod, String kibanaRegion) {

        int maxConnections = DEFAULT_MAX_CONNECTIONS;
        int maxConnectionsPerRoute = DEFAULT_MAX_CONNECTIONS_PER_ROUTE;

        if (port != null) {
            this.port = port;
        } else {
            this.port = DEFAULT_PORT;
        }
        this.host = host;
        this.path = path;
        this.pod = pod;
        this.kibanaRegion = kibanaRegion;

        if (proxyHost != null && proxyPort > 0) {
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
        }
        this.socketTimeout = socketTimeout;
        this.connectionTimeout = connectionTimeout;
        this.pod = pod;
        this.kibanaRegion = kibanaRegion;

        connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(maxConnections);
        connManager.setDefaultMaxPerRoute(maxConnectionsPerRoute);
        httpClient = HttpClients.custom().setConnectionManager(connManager).build();
        initRequestConfig();
    }

    private void initRequestConfig() {
        if (proxyHost == null || proxyPort <= 0) {
            requestConfig = RequestConfig.custom()
                    .setSocketTimeout(socketTimeout)
                    .setConnectTimeout(connectionTimeout)
                    .build();
        } else {
            requestConfig = RequestConfig.custom().setSocketTimeout(socketTimeout).setConnectTimeout(connectionTimeout)
                    .setProxy(new HttpHost(proxyHost, proxyPort)).build();
        }

    }


    /**
     * This method is called once during HttpConnector initialization
     *
     * @param httpMethod http method
     * @return http request
     */
    private HttpRequestBase createHttpMethod(HttpMethod httpMethod) {
        switch (httpMethod) {
            case GET:
                return new HttpGet();
            case POST:
                return new HttpPost();
            case PUT:
                return new HttpPut();
            case DELETE:
                return new HttpDelete();
            case HEAD:
                return new HttpHead();
        }
        return null;
    }

    HttpRequestBase createHttpMethod(String httpMethod) {
        return createHttpMethod(HttpMethod.valueOf(httpMethod));
    }


    /**
     * Executes the request
     *
     * @return response
     */

    public RequestResponse execute(String path, URI uri, Map<String, String> headers, byte[] body, String uploadedFileName,
                                   String method, String userName, String tenant) {
        HttpRequestBase httpMethod = createHttpMethod(method);
        httpMethod.setURI(uri);
        httpMethod.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        int responseCode = 0;
        String responseDescription = "";
        String responseStr = "";
        String contentType = "text/html";
        Exception exception = null;
        List<Cookie> cookies = null;
        List<Header> responseHeaders = null;
        long timeBefore = -1, timeAfter = -1;
        String soHost = "";
        int retryCount = 1;
        int max401RetryCount = 20;

        try {
            if (headers != null) {
                for (String headerKey : headers.keySet()) {
                    httpMethod.setHeader(headerKey, headers.get(headerKey));
                }
            }
            HttpClientContext context = HttpClientContext.create();
            //elapsedHttpConnect.start();
            timeBefore = System.currentTimeMillis();
            String p=Paths.get(".").toAbsolutePath().toString();
            String pathCert=Paths.get("C:/Tools-Re02").toString();
            String key=getKey(pathCert+"/private.pem");
            String cacerts=pathCert+"/cacerts";
            SSLContext sslcontext = SSLContexts.custom()
                    .loadKeyMaterial(new File(cacerts), "changeit".toCharArray(),
                            key.toCharArray())
                    .loadTrustMaterial(new File(cacerts))
                    .build();
//            String key=getKey("C:/Tools-Re02/private.pem");
//            System.out.println("private key22: "+key);
//            SSLContext sslcontext = SSLContexts.custom()
//                    .loadKeyMaterial(new File("C:/Tools-Re02/cacerts"), "changeit".toCharArray(),
//                    key.toCharArray())
//                    .loadTrustMaterial(new File(Paths.get("C:/Tools-Re02/cacerts").toString()))
//                 // .loadTrustMaterial(new File("C:/Tools-Re02/cacerts"), "changeit".toCharArray())
//                   .build();
            //System.out.println("after load sslcontext"+sslcontext.getProtocol()+" getClientSessionContext  :"+sslcontext.getClientSessionContext());

            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext,NoopHostnameVerifier.INSTANCE);
            httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).setSSLHostnameVerifier(new NoopHostnameVerifier()).build();
            System.out.println("request:"+httpClient+"method:"+httpMethod+"context:"+context);
            response = httpClient.execute(httpMethod, context);
            timeAfter = System.currentTimeMillis();
            CookieStore cookieStore = context.getCookieStore();
            cookies = cookieStore.getCookies();

            HttpEntity httpEntity;
            if (response != null && response.getStatusLine().getStatusCode() == 401) {
                System.out.println("get 401");
                responseCode = 401;
                //The way kerberos works id if you get 401, retry the call. The problem is that we have many SO servers and I might get 401
                //few times, 20 is the max number of SO servers
                while (responseCode == 401 && retryCount < max401RetryCount) {
                    Header soHostHeader = response.getFirstHeader("X-CKSW-Debug-Host");
                    soHost = soHostHeader != null ? soHostHeader.getValue() : "";
                    String msg = String.format("Got 401 on Kerberos call for url:%s, retryCount:%d so-host:%s, max401RetryCount:%d",
                            uri.toURL().toString(),
                            retryCount,
                            soHost,
                            max401RetryCount);
                    //If soHost is not empty it means that the 401 is comming from SO and there is no need for retry
                    //If soHost is empty it means it comes from Kerberos Negotiate protocol and we must retry
                    retryCount++;
                    response.close();
                    response = httpClient.execute(httpMethod, context);
                    responseCode = (response != null) ? response.getStatusLine().getStatusCode() : -1;
                }
            }
            if (response != null) {
                Header soHostHeader = response.getFirstHeader("X-CKSW-Debug-Host");
                soHost = soHostHeader != null ? soHostHeader.getValue() : "";
                responseDescription = response.getStatusLine().getReasonPhrase();
                responseCode = response.getStatusLine().getStatusCode();
                responseHeaders = Arrays.asList(response.getAllHeaders());
                httpEntity = response.getEntity();

                if ((httpEntity != null) && (httpEntity.getContentType() != null)) {//some cases no contents is returning and the http entity is null
                    contentType = httpEntity.getContentType().getValue();
                    if (isUTF8EncodingPath(path)) {
                        responseStr = EntityUtils.toString(httpEntity, "UTF-8");
                    } else {
                        responseStr = EntityUtils.toString(httpEntity);
                    }
                    if (uri.getPath().matches(".*?\\.json.*") && StringUtils.isNotEmpty(responseStr)) {
                        responseStr = removeUTF8BOM(responseStr);
                    }
                }
                EntityUtils.consume(httpEntity);
            }
        } catch (ConnectionPoolTimeoutException e) {
            System.out.println("get 401");
            e.printStackTrace();
            if (timeAfter == -1) {
                timeAfter = System.currentTimeMillis();
                responseCode = -1;
            }
        } catch (SocketTimeoutException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            if (timeAfter == -1) {
                timeAfter = System.currentTimeMillis();
                responseCode = -1;
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (timeAfter == -1) {
                timeAfter = System.currentTimeMillis();
                responseCode = -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (timeAfter == -1) {
                timeAfter = System.currentTimeMillis();
                responseCode = -1;
            }
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    String msg = "error while closing response ";
                }
            } else {
                String msg = "Response is null, releaseConnection";
                System.out.println(msg);
            }
            httpMethod.releaseConnection();

        }

        return new RequestResponse(responseStr, responseCode, responseDescription, contentType, cookies, responseHeaders, exception, timeAfter - timeBefore);
    }

    public boolean isUTF8EncodingPath(String path) {
        String regexForUtf8Path = "(?i).*\\.(js[/]{0,1}|json[/]{0,1})$";
        return Pattern.compile(regexForUtf8Path).matcher(path).matches();
    }

    private static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }
    private static String getKey(String filename) throws IOException {
        // Read key from file
        String strKeyPEM = "";
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = br.readLine()) != null) {
            strKeyPEM += line + "\n";
        }
        br.close();
        return getPrivateKeyFromString(strKeyPEM);
    }
    public static String getPrivateKeyFromString(String key){
        String privateKeyPEM = key;
        privateKeyPEM = privateKeyPEM.replace("-----BEGIN RSA PRIVATE KEY-----\n", "");
        privateKeyPEM = privateKeyPEM.replace("-----END RSA PRIVATE KEY-----", "");

        return  privateKeyPEM;
//        byte[] encoded = Base64.decodeBase64(privateKeyPEM);
//        KeyFactory kf = KeyFactory.getInstance("RSA");
//        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
//        RSAPrivateKey privKey = (RSAPrivateKey) kf.generatePrivate(keySpec);
//        return privKey;
    }

}
