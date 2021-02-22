package com.swingfrog.summer.web.token;

public interface WebTokenHandler {

    String createToken();
    String parseToken(String cookie);

}
