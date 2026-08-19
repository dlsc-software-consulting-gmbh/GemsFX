package com.dlsc.gemsfx.util;

/**
 * Enum of standard HTTP status codes as defined by
 * <a href="https://www.iana.org/assignments/http-status-codes">IANA HTTP Status Code Registry</a>.
 *
 * <p>Each constant carries its numeric {@link #getStatusCode() code}, a human-readable
 * {@link #getReasonPhrase() reason phrase}, and a {@link Family} classification
 * (1xx informational, 2xx success, 3xx redirection, 4xx client error, 5xx server error).
 */
public enum HttpStatusCode {

    /**
     * 200 OK — the request succeeded.
     */
    OK(200, "OK"),
    /**
     * 201 Created — the request succeeded and created a new resource.
     */
    CREATED(201, "Created"),
    /**
     * 202 Accepted — the request was accepted for processing but is not complete.
     */
    ACCEPTED(202, "Accepted"),
    /**
     * 204 No Content — the request succeeded and there is no response body.
     */
    NO_CONTENT(204, "No Content"),
    /**
     * 205 Reset Content — the request succeeded and the client should reset the document view.
     */
    RESET_CONTENT(205, "Reset Content"),
    /**
     * 206 Partial Content — the response contains the requested byte range.
     */
    PARTIAL_CONTENT(206, "Partial Content"),
    /**
     * 301 Moved Permanently — the resource has been assigned a new permanent URI.
     */
    MOVED_PERMANENTLY(301, "Moved Permanently"),
    /**
     * 302 Found — the resource is temporarily available under a different URI.
     */
    FOUND(302, "Found"),
    /**
     * 303 See Other — the response to the request can be found under another URI.
     */
    SEE_OTHER(303, "See Other"),
    /**
     * 304 Not Modified — the cached representation can be reused.
     */
    NOT_MODIFIED(304, "Not Modified"),
    /**
     * 305 Use Proxy — the resource must be accessed through a proxy.
     */
    USE_PROXY(305, "Use Proxy"),
    /**
     * 307 Temporary Redirect — the request should be repeated at another URI using the same method.
     */
    TEMPORARY_REDIRECT(307, "Temporary Redirect"),
    /**
     * 400 Bad Request — the server cannot process the malformed request.
     */
    BAD_REQUEST(400, "Bad Request"),
    /**
     * 401 Unauthorized — authentication is required or has failed.
     */
    UNAUTHORIZED(401, "Unauthorized"),
    /**
     * 402 Payment Required — the status code is reserved for future use.
     */
    PAYMENT_REQUIRED(402, "Payment Required"),
    /**
     * 403 Forbidden — the server understood the request but refuses to authorize it.
     */
    FORBIDDEN(403, "Forbidden"),
    /**
     * 404 Not Found — the requested resource could not be found.
     */
    NOT_FOUND(404, "Not Found"),
    /**
     * 405 Method Not Allowed — the request method is not supported for the resource.
     */
    METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
    /**
     * 406 Not Acceptable — no response representation matches the request headers.
     */
    NOT_ACCEPTABLE(406, "Not Acceptable"),
    /**
     * 407 Proxy Authentication Required — authentication with a proxy is required.
     */
    PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),
    /**
     * 408 Request Timeout — the server timed out waiting for the request.
     */
    REQUEST_TIMEOUT(408, "Request Timeout"),
    /**
     * 409 Conflict — the request conflicts with the current resource state.
     */
    CONFLICT(409, "Conflict"),
    /**
     * 410 Gone — the resource is no longer available and no forwarding address is known.
     */
    GONE(410, "Gone"),
    /**
     * 411 Length Required — the request requires a valid Content-Length header.
     */
    LENGTH_REQUIRED(411, "Length Required"),
    /**
     * 412 Precondition Failed — a request precondition evaluated to false.
     */
    PRECONDITION_FAILED(412, "Precondition Failed"),
    /**
     * 413 Request Entity Too Large — the request payload is larger than the server will process.
     */
    REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"),
    /**
     * 414 Request-URI Too Long — the request URI is longer than the server will process.
     */
    REQUEST_URI_TOO_LONG(414, "Request-URI Too Long"),
    /**
     * 415 Unsupported Media Type — the request payload format is not supported.
     */
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),
    /**
     * 416 Requested Range Not Satisfiable — the requested range cannot be served.
     */
    REQUESTED_RANGE_NOT_SATISFIABLE(416, "Requested Range Not Satisfiable"),
    /**
     * 417 Expectation Failed — the server cannot meet the Expect request-header expectation.
     */
    EXPECTATION_FAILED(417, "Expectation Failed"),
    /**
     * 428 Precondition Required — the origin server requires the request to be conditional.
     */
    PRECONDITION_REQUIRED(428, "Precondition Required"),
    /**
     * 429 Too Many Requests — the user has sent too many requests in a given time.
     */
    TOO_MANY_REQUESTS(429, "Too Many Requests"),
    /**
     * 431 Request Header Fields Too Large — the request header fields are too large.
     */
    REQUEST_HEADER_FIELDS_TOO_LARGE(431, "Request Header Fields Too Large"),
    /**
     * 500 Internal Server Error — the server encountered an unexpected condition.
     */
    INTERNAL_SERVER_ERROR(500, "Internal Server Error"),
    /**
     * 501 Not Implemented — the server does not support the required functionality.
     */
    NOT_IMPLEMENTED(501, "Not Implemented"),
    /**
     * 502 Bad Gateway — a gateway or proxy received an invalid upstream response.
     */
    BAD_GATEWAY(502, "Bad Gateway"),
    /**
     * 503 Service Unavailable — the server is temporarily unable to handle the request.
     */
    SERVICE_UNAVAILABLE(503, "Service Unavailable"),
    /**
     * 504 Gateway Timeout — a gateway or proxy did not receive a timely upstream response.
     */
    GATEWAY_TIMEOUT(504, "Gateway Timeout"),
    /**
     * 505 HTTP Version Not Supported — the server does not support the request HTTP version.
     */
    HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version Not Supported"),
    /**
     * 511 Network Authentication Required — the client must authenticate to gain network access.
     */
    NETWORK_AUTHENTICATION_REQUIRED(511, "Network Authentication Required");

    private final int code;
    private final String reason;
    private final HttpStatusCode.Family family;

    HttpStatusCode(int statusCode, String reasonPhrase) {
        code = statusCode;
        reason = reasonPhrase;
        family = HttpStatusCode.Family.familyOf(statusCode);
    }

    /**
     * Returns the status code family.
     *
     * @return the status code family
     */
    public HttpStatusCode.Family getFamily() {
        return family;
    }

    /**
     * Returns the numeric status code.
     *
     * @return the numeric status code
     */
    public int getStatusCode() {
        return code;
    }

    /**
     * Returns the reason phrase.
     *
     * @return the reason phrase
     */
    public String getReasonPhrase() {
        return toString();
    }

    /**
     * Returns the reason phrase.
     *
     * @return the reason phrase
     */
    public String toString() {
        return reason;
    }

    /**
     * Returns the enum constant for the given status code.
     *
     * @param statusCode the status code
     * @return the matching enum constant, or {@code null} if none matches
     */
    public static HttpStatusCode fromStatusCode(int statusCode) {
        HttpStatusCode[] codes = values();

        for (HttpStatusCode s : codes) {
            if (s.code == statusCode) {
                return s;
            }
        }

        return null;
    }

    /**
     * The available HTTP status code families.
     */
    public enum Family {
        /**
         * A 1xx status code indicating an informational response.
         */
        INFORMATIONAL,
        /**
         * A 2xx status code indicating a successful request.
         */
        SUCCESSFUL,
        /**
         * A 3xx status code indicating that further action is needed.
         */
        REDIRECTION,
        /**
         * A 4xx status code indicating a client error.
         */
        CLIENT_ERROR,
        /**
         * A 5xx status code indicating a server error.
         */
        SERVER_ERROR,
        /**
         * A status code outside the standard 1xx through 5xx families.
         */
        OTHER;

        Family() {
        }

        /**
         * Determines the family for the given status code.
         *
         * @param statusCode the status code
         * @return the status code family
         */
        public static HttpStatusCode.Family familyOf(int statusCode) {
            switch (statusCode / 100) {
                case 1:
                    return INFORMATIONAL;
                case 2:
                    return SUCCESSFUL;
                case 3:
                    return REDIRECTION;
                case 4:
                    return CLIENT_ERROR;
                case 5:
                    return SERVER_ERROR;
                default:
                    return OTHER;
            }
        }
    }
}
