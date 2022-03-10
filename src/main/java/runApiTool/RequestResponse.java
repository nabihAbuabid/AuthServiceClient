package runApiTool;

import org.apache.http.Header;
import org.apache.http.cookie.Cookie;

import java.util.List;

public class RequestResponse {

    private String content;
    private int statusCode;
    private String statusDescription;
    private String contentType;
    private Exception exception;
    List<Cookie> cookies;
    List<Header> httpHeaders;
    private long timeTaken;

    public RequestResponse(String content,
                           int statusCode,
                           String statusDescription,
                           String contentType,
                           List<Cookie> cookies,
                           List<Header> httpHeaders,
                           Exception exception,
                           long timeTaken) {
        super();
        this.content = content;
        this.statusCode = statusCode;
        this.statusDescription = statusDescription;
        this.contentType = contentType;
        this.cookies = cookies;
        this.httpHeaders = httpHeaders;
        this.exception = exception;
        this.timeTaken = timeTaken;
    }


    public String getContent() {
        return content;
    }


    public void setContent(String content) {
        this.content = content;
    }


    public int getStatusCode() {
        return statusCode;
    }


    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }


    public String getStatusDescription() {
        return statusDescription;
    }


    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }


    public Exception getException() {
        return exception;
    }


    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getContentType() {
        return contentType;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public List<Header> getHttpHeaders() {
        return httpHeaders;
    }

    public long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }
}
