/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package org.wisdom.engine.wrapper;

import com.google.common.collect.ImmutableList;
import com.google.common.net.MediaType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import org.wisdom.api.cookies.Cookie;
import org.wisdom.api.cookies.Cookies;
import org.wisdom.api.http.Context;
import org.wisdom.api.http.HeaderNames;
import org.wisdom.api.http.MimeTypes;
import org.wisdom.api.http.Request;
import org.wisdom.engine.wrapper.cookies.CookiesImpl;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Request implementation based on HttpRequest.
 */
public class RequestFromNetty extends Request {

    private final HttpRequest request;
    private final ChannelHandlerContext channel;
    private final Cookies cookies;
    private final Context context;

    private Map<String, List<String>> headers;

    private final Map<String, Object> data;

    public RequestFromNetty(Context context, ChannelHandlerContext ctx, HttpRequest request) {
        this.request = request;
        this.channel = ctx;
        this.context = context;
        this.cookies = new CookiesImpl(request);
        this.data = new HashMap<>();
    }

    /**
     * The Content-Type header field indicates the media type of the request
     * body sent to the recipient. E.g. {@code Content-Type: text/html;
     * charset=ISO-8859-4}
     *
     * @return the content type of the incoming request.
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     * >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    @Override
    public String contentType() {
        return request.headers().get(HeaderNames.CONTENT_TYPE);
    }

    /**
     * Get the encoding that is acceptable for the client. E.g. Accept-Encoding:
     * compress, gzip
     * <p/>
     * The Accept-Encoding request-header field is similar to Accept, but
     * restricts the content-codings that are acceptable in the response.
     *
     * @return the encoding that is acceptable for the client
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     * >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    @Override
    public String encoding() {
        return request.headers().get(HeaderNames.ACCEPT_ENCODING);
    }

    /**
     * Get the language that is acceptable for the client. E.g. Accept-Language:
     * da, en-gb;q=0.8, en;q=0.7
     * <p/>
     * The Accept-Language request-header field is similar to Accept, but
     * restricts the set of natural languages that are preferred as a response
     * to the request.
     *
     * @return the language that is acceptable for the client
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     * >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    @Override
    public String language() {
        return request.headers().get(HeaderNames.ACCEPT_LANGUAGE);
    }

    /**
     * Get the charset that is acceptable for the client. E.g. Accept-Charset:
     * iso-8859-5, unicode-1-1;q=0.8
     * <p/>
     * The Accept-Charset request-header field can be used to indicate what
     * character sets are acceptable for the response. This field allows clients
     * capable of understanding more comprehensive or special- purpose character
     * sets to signal that capability to a server which is capable of
     * representing documents in those character sets.
     *
     * @return the charset that is acceptable for the client
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     * >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    @Override
    public String charset() {
        return request.headers().get(HeaderNames.ACCEPT_CHARSET);
    }

    /**
     * The complete request URI, containing both path and query string.
     */
    @Override
    public String uri() {
        return request.getUri();
    }

    /**
     * Returns the name of the HTTP method with which this
     * request was made, for example, GET, POST, or PUT.
     * Same as the value of the CGI variable REQUEST_METHOD.
     *
     * @return a <code>String</code>
     * specifying the name
     * of the method with which
     * this request was made (eg GET, POST, PUT...)
     */
    @Override
    public String method() {
        return request.getMethod().name();
    }

    /**
     * The client IP address.
     * <p/>
     * If the <code>X-Forwarded-For</code> header is present, then this method will return the value in that header
     * if either the local address is 127.0.0.1, or if <code>trustxforwarded</code> is configured to be true in the
     * application configuration file.
     */
    @Override
    public String remoteAddress() {
        if (headers().containsKey(HeaderNames.X_FORWARD_FOR)) {
            return getHeader(HeaderNames.X_FORWARD_FOR);
        } else {
            InetSocketAddress remote = (InetSocketAddress) channel.channel().remoteAddress();
            return remote.getAddress().getHostAddress();
        }
    }

    /**
     * The request host.
     */
    @Override
    public String host() {
        InetSocketAddress remote = (InetSocketAddress) channel.channel().remoteAddress();
        return remote.getHostName();
    }

    /**
     * The URI path, without the query part.
     */
    @Override
    public String path() {
        try {
            return new URI(request.getUri()).getRawPath();
        } catch (URISyntaxException e) { //NOSONAR
            // Should never be the case.
            return uri();
        }
    }

    /**
     * Get the preferred content media type that is acceptable for the client. For instance, in Accept: text/*;q=0.3,
     * text/html;q=0.7, text/html;level=1,text/html;level=2;q=0.4, text/html is returned.
     * <p/>
     * The Accept request-header field can be used to specify certain media
     * types which are acceptable for the response. Accept headers can be used
     * to indicate that the request is specifically limited to a small set of
     * desired types, as in the case of a request for an in-line image.
     *
     * @return a MediaType that is acceptable for the
     * client or {@see MediaType#HTML_UTF_8} if not set
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     * >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    @Override
    public MediaType mediaType() {
        Collection<MediaType> types = mediaTypes();
        if (types == null || types.isEmpty()) {
            return MediaType.ANY_TEXT_TYPE;
        } else if (types.size() == 1 && types.iterator().next().equals(MediaType.ANY_TYPE)) {
            return MediaType.ANY_TEXT_TYPE;
        } else {
            return types.iterator().next();
        }
    }

    /**
     * Get the content media type that is acceptable for the client. E.g. Accept: text/*;q=0.3, text/html;q=0.7,
     * text/html;level=1,text/html;level=2;q=0.4
     * <p/>
     * The Accept request-header field can be used to specify certain media
     * types which are acceptable for the response. Accept headers can be used
     * to indicate that the request is specifically limited to a small set of
     * desired types, as in the case of a request for an in-line image.
     *
     * @return a MediaType that is acceptable for the
     * client or {@see MediaType#ANY_TEXT_TYPE} if not set
     * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html"
     * >http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html</a>
     */
    @Override
    public Collection<MediaType> mediaTypes() {
        String contentType = request.headers().get(HeaderNames.ACCEPT);

        if (contentType == null) {
            // Any text by default.
            return ImmutableList.of(MediaType.ANY_TEXT_TYPE);
        }

        TreeSet<MediaType> set = new TreeSet<>(new Comparator<MediaType>() {
            @Override
            public int compare(MediaType o1, MediaType o2) {
                double q1 = 1.0, q2 = 1.0;
                List<String> ql1 = o1.parameters().get("q");
                List<String> ql2 = o2.parameters().get("q");

                if (ql1 != null && !ql1.isEmpty()) {
                    q1 = Double.parseDouble(ql1.get(0));
                }

                if (ql2 != null && !ql2.isEmpty()) {
                    q2 = Double.parseDouble(ql2.get(0));
                }

                return new Double(q2).compareTo(q1);
            }
        });

        // Split and sort.
        String[] segments = contentType.split(",");
        for (String segment : segments) {
            MediaType type = MediaType.parse(segment.trim());
            set.add(type);
        }

        return set;
    }

    /**
     * Check if this request accepts a given media type.
     *
     * @return true if <code>mimeType</code> is in the Accept header, otherwise false
     */
    @Override
    public boolean accepts(String mimeType) {
        String contentType = request.headers().get(HeaderNames.ACCEPT);
        if (contentType == null) {
            contentType = MimeTypes.HTML;
        }
        // For performance reason, we first try a full match:
        if (contentType.contains(mimeType)) {
            return true;
        }
        // Else check the media types:
        MediaType input = MediaType.parse(mimeType);
        for (MediaType type : mediaTypes()) {
            if (input.is(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the request cookies
     */
    @Override
    public Cookies cookies() {
        return cookies;
    }

    public Cookie cookie(String name) {
        return cookies.get(name);
    }

    /**
     * Retrieves all headers.
     *
     * @return headers
     */
    @Override
    public Map<String, List<String>> headers() {
        if (headers != null) {
            return headers;
        }
        headers = new HashMap<>();
        final HttpHeaders requestHeaders = request.headers();
        Set<String> names = requestHeaders.names();
        for (String name : names) {
            headers.put(name, requestHeaders.getAll(name));
        }
        return headers;
    }

    /**
     * Get the parameter with the given key from the request. The parameter may
     * either be a query parameter, or in the case of form submissions, may be a
     * form parameter.
     * <p/>
     * When the parameter is multivalued, returns the first value.
     * <p/>
     * The parameter is decoded by default.
     *
     * @param name The key of the parameter
     * @return The value, or null if no parameter was found.
     * @see #parameterMultipleValues
     */
    @Override
    public String parameter(String name) {
        return context.parameter(name);
    }

    /**
     * Get the parameter with the given key from the request. The parameter may
     * either be a query parameter, or in the case of form submissions, may be a
     * form parameter.
     * <p/>
     * The parameter is decoded by default.
     *
     * @param name The key of the parameter
     * @return The values, possibly an empty list.
     */
    @Override
    public List<String> parameterMultipleValues(String name) {
        return context.parameterMultipleValues(name);
    }

    /**
     * Same like {@link #parameter(String)}, but returns given defaultValue
     * instead of null in case parameter cannot be found.
     * <p/>
     * The parameter is decoded by default.
     *
     * @param name         The name of the post or query parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter of the defaultValue if not found.
     */
    @Override
    public String parameter(String name, String defaultValue) {
        return context.parameter(name, defaultValue);
    }

    /**
     * Same like {@link #parameter(String)}, but converts the parameter to
     * Integer if found.
     * <p/>
     * The parameter is decoded by default.
     *
     * @param name The name of the post or query parameter
     * @return The value of the parameter or null if not found.
     */
    @Override
    public Integer parameterAsInteger(String name) {
        return context.parameterAsInteger(name);
    }

    /**
     * Same like {@link #parameter(String, String)}, but converts the
     * parameter to Integer if found.
     * <p/>
     * The parameter is decoded by default.
     *
     * @param name         The name of the post or query parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter of the defaultValue if not found.
     */
    @Override
    public Integer parameterAsInteger(String name, Integer defaultValue) {
        return context.parameterAsInteger(name, defaultValue);
    }

    /**
     * Same like {@link #parameter(String)}, but converts the parameter to
     * Boolean if found.
     * <p/>
     * The parameter is decoded by default.
     *
     * @param name The name of the post or query parameter
     * @return The value of the parameter or {@literal false} if not found.
     */
    @Override
    public Boolean parameterAsBoolean(String name) {
        return context.parameterAsBoolean(name);
    }

    /**
     * Same like {@link #parameter(String)}, but converts the parameter to
     * Boolean if found.
     * <p/>
     * The parameter is decoded by default.
     *
     * @param name         The name of the post or query parameter
     * @param defaultValue A default value if parameter not found.
     * @return The value of the parameter or the defaultValue if not found.
     */
    @Override
    public Boolean parameterAsBoolean(String name, boolean defaultValue) {
        return context.parameterAsBoolean(name, defaultValue);
    }

    /**
     * Get all the parameters from the request.
     *
     * @return The parameters
     */
    @Override
    public Map<String, List<String>> parameters() {
        return context.parameters();
    }

    /**
     * Retrieves the data shared by all the entities participating to the request resolution (i.e. computation of the
     * response). This method returns a live map, meaning that modification impacts all other participants. It can be
     * used to let filters or interceptors passing objects to action methods or templates.
     *
     * @return the map storing the data. Unlike session or flash, these data are not stored in cookies,
     * and are cleared once the response is sent back to the client.
     */
    @Override
    public Map<String, Object> data() {
        return data;
    }

}
