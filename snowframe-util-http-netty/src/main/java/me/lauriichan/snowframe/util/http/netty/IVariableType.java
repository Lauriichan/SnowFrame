package me.lauriichan.snowframe.util.http.netty;

import me.lauriichan.snowframe.util.http.HttpHeaders;
import me.lauriichan.snowframe.util.http.data.HttpEncoded;

public interface IVariableType {
    
    Object resolve(HttpHeaders headers, HttpEncoded parameters, String input);

}
