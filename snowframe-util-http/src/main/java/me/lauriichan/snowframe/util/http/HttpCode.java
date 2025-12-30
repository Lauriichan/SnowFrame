package me.lauriichan.snowframe.util.http;

import it.unimi.dsi.fastutil.ints.Int2ReferenceArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMaps;

public final class HttpCode {

    public static final HttpCode CONTINUE;
    public static final HttpCode SWITCHING_PROTOCOLS;
    public static final HttpCode PROCESSING;
    public static final HttpCode EARLY_HINTS;

    public static final HttpCode OK;
    public static final HttpCode CREATED;
    public static final HttpCode ACCEPTED;
    public static final HttpCode NON_AUTHORITATIVE_INFORMATION;
    public static final HttpCode NO_CONTENT;
    public static final HttpCode RESET_CONTENT;
    public static final HttpCode PARTIAL_CONTENT;
    public static final HttpCode MULTI_STATUS;
    public static final HttpCode ALREADY_REPORTED;
    public static final HttpCode IM_USED;

    public static final HttpCode MULTIPLE_CHOICES;
    public static final HttpCode MOVED_PERMANENTLY;
    public static final HttpCode FOUND;
    public static final HttpCode SEE_OTHER;
    public static final HttpCode NOT_MODIFIED;
    public static final HttpCode TEMPORARY_REDIRECT;
    public static final HttpCode PERMANENT_REDIRECT;

    public static final HttpCode BAD_REQUEST;
    public static final HttpCode UNAUTHORIZED;
    public static final HttpCode PAYMENT_REQUIRED;
    public static final HttpCode FORBIDDEN;
    public static final HttpCode NOT_FOUND;
    public static final HttpCode METHOD_NOT_ALLOWED;
    public static final HttpCode NOT_ACCEPTABLE;
    public static final HttpCode PROXY_AUTHENTICATION_REQUIRED;
    public static final HttpCode REQUEST_TIMEOUT;
    public static final HttpCode CONFLICT;
    public static final HttpCode GONE;
    public static final HttpCode LENGTH_REQUIRED;
    public static final HttpCode PRECONDITION_FAILED;
    public static final HttpCode PAYLOAD_TOO_LARGE;
    public static final HttpCode URI_TOO_LONG;
    public static final HttpCode UNSUPPORTED_MEDIA_TYPE;
    public static final HttpCode RANGE_NOT_SATISFIED;
    public static final HttpCode EXPECTATION_FAILED;
    public static final HttpCode IM_A_TEAPOT;
    public static final HttpCode MISDIRECTED_REQUEST;
    public static final HttpCode UNPROCESSABLE_CONTENT;
    public static final HttpCode LOCKED;
    public static final HttpCode FAILED_DEPENDENCY;
    public static final HttpCode TOO_EARLY;
    public static final HttpCode UPGRADE_REQUIRED;
    public static final HttpCode PRECONDITION_REQUIRED;
    public static final HttpCode TOO_MANY_REQUESTS;
    public static final HttpCode REQUEST_HEADER_FIELDS_TOO_LARGE;
    public static final HttpCode UNAVAILABLE_FOR_LEGAL_REASONS;

    public static final HttpCode INTERNAL_SERVER_ERROR;
    public static final HttpCode NOT_IMPLEMENTED;
    public static final HttpCode BAD_GATEWAY;
    public static final HttpCode SERVICE_UNAVAILABLE;
    public static final HttpCode GATEWAY_TIMEOUT;
    public static final HttpCode HTTP_VERSION_NOT_SUPPORTED;
    public static final HttpCode VARIANT_ALSO_NEGOTIATES;
    public static final HttpCode INSUFFICIENT_STORAGE;
    public static final HttpCode LOOP_DETECTED;
    public static final HttpCode NOT_EXTENDED;
    public static final HttpCode NETWORK_AUTHENTICATION_REQUIRED;

    private static final Int2ReferenceMap<HttpCode> CODE2OBJECT;

    static {
        Int2ReferenceArrayMap<HttpCode> map = new Int2ReferenceArrayMap<>(60);
        CONTINUE = register(map, "Continue", 100);
        SWITCHING_PROTOCOLS = register(map, "Switching Protocols", 101);
        PROCESSING = register(map, "Processing", 102);
        EARLY_HINTS = register(map, "Early Hints", 103);

        OK = register(map, "Ok", 200);
        CREATED = register(map, "Created", 201);
        ACCEPTED = register(map, "Accepted", 202);
        NON_AUTHORITATIVE_INFORMATION = register(map, "Non-Authoritative Information", 203);
        NO_CONTENT = register(map, "No Content", 204);
        RESET_CONTENT = register(map, "Reset Content", 205);
        PARTIAL_CONTENT = register(map, "Partial Content", 206);
        MULTI_STATUS = register(map, "Multi-Status", 207);
        ALREADY_REPORTED = register(map, "Already Reported", 208);
        IM_USED = register(map, "IM Used", 226);

        MULTIPLE_CHOICES = register(map, "Multiple Choices", 300);
        MOVED_PERMANENTLY = register(map, "Moved Permanently", 301);
        FOUND = register(map, "Found", 302);
        SEE_OTHER = register(map, "See Other", 303);
        NOT_MODIFIED = register(map, "Not Modified", 304);
        TEMPORARY_REDIRECT = register(map, "Temporary Redirect", 307);
        PERMANENT_REDIRECT = register(map, "Permanent Redirect", 308);

        BAD_REQUEST = register(map, "Bad Request", 400);
        UNAUTHORIZED = register(map, "Unauthorized", 401);
        PAYMENT_REQUIRED = register(map, "Payment Required", 402);
        FORBIDDEN = register(map, "Forbidden", 403);
        NOT_FOUND = register(map, "Not Found", 404);
        METHOD_NOT_ALLOWED = register(map, "Method Not Allowed", 405);
        NOT_ACCEPTABLE = register(map, "Not Acceptable", 406);
        PROXY_AUTHENTICATION_REQUIRED = register(map, "Proxy Authentication Required", 407);
        REQUEST_TIMEOUT = register(map, "Request Timeout", 408);
        CONFLICT = register(map, "Conflict", 409);
        GONE = register(map, "Gone", 410);
        LENGTH_REQUIRED = register(map, "Length Required", 411);
        PRECONDITION_FAILED = register(map, "Precondition Failed", 412);
        PAYLOAD_TOO_LARGE = register(map, "Payload Too Large", 413);
        URI_TOO_LONG = register(map, "URI Too Long", 414);
        UNSUPPORTED_MEDIA_TYPE = register(map, "Unsupposed Media Type", 415);
        RANGE_NOT_SATISFIED = register(map, "Range Not Satisfied", 416);
        EXPECTATION_FAILED = register(map, "Expectation Failed", 417);
        IM_A_TEAPOT = register(map, "I'm a teapot", 418);
        MISDIRECTED_REQUEST = register(map, "Misdirected Request", 421);
        UNPROCESSABLE_CONTENT = register(map, "Unprocessable Content", 422);
        LOCKED = register(map, "Locked", 423);
        FAILED_DEPENDENCY = register(map, "Failed Dependency", 424);
        TOO_EARLY = register(map, "Too Early", 425);
        UPGRADE_REQUIRED = register(map, "Upgrade Required", 426);
        PRECONDITION_REQUIRED = register(map, "Precondition Required", 428);
        TOO_MANY_REQUESTS = register(map, "Too Many Requests", 429);
        REQUEST_HEADER_FIELDS_TOO_LARGE = register(map, "Request Header Fields Too Large", 431);
        UNAVAILABLE_FOR_LEGAL_REASONS = register(map, "Unavailable For Legal Reasons", 451);

        INTERNAL_SERVER_ERROR = register(map, "Internal Server Error", 500);
        NOT_IMPLEMENTED = register(map, "Not Implemented", 501);
        BAD_GATEWAY = register(map, "Bad Gateway", 502);
        SERVICE_UNAVAILABLE = register(map, "Service Unavailable", 503);
        GATEWAY_TIMEOUT = register(map, "Gateway Timeout", 504);
        HTTP_VERSION_NOT_SUPPORTED = register(map, "HTTP Version Not Supported", 505);
        VARIANT_ALSO_NEGOTIATES = register(map, "Variant Also Negotiates", 506);
        INSUFFICIENT_STORAGE = register(map, "Insufficient Storage", 507);
        LOOP_DETECTED = register(map, "Loop Detected", 508);
        NOT_EXTENDED = register(map, "Not Extended", 510);
        NETWORK_AUTHENTICATION_REQUIRED = register(map, "Network Authentication Required", 511);
        CODE2OBJECT = Int2ReferenceMaps.unmodifiable(map);
    }

    private static HttpCode register(Int2ReferenceArrayMap<HttpCode> map, String name, int code) {
        if (map.containsKey(code)) {
            throw new IllegalStateException("Code %s is already allocated.".formatted(code));
        }
        HttpCode obj = new HttpCode(name, code);
        map.put(code, obj);
        return obj;
    }

    public static HttpCode byCode(int code) {
        return CODE2OBJECT.get(code);
    }

    private final String name;
    private final int code;

    private HttpCode(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String name() {
        return name;
    }

    public int code() {
        return code;
    }

    @Override
    public int hashCode() {
        return code;
    }

    @Override
    public String toString() {
        return name;
    }

}