package me.lauriichan.snowframe.util.http.netty;

import io.netty.channel.Channel;
import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.server.Response;

public interface IAuthenticator {

    /**
     * Authenticates a channel
     * 
     * @param  channel the request sender
     * @param  headers the request headers
     * 
     * @return         {@code null} if authentication was successful otherwise an
     *                 error response.
     */
    Response authenticate(Channel channel, HttpHeaders headers);

}
