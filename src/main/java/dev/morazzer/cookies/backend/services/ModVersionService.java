package dev.morazzer.cookies.backend.services;

import org.springframework.stereotype.Service;

@Service
public class ModVersionService {

    public String getModVersion(String userAgent) {
        if (!userAgent.contains("CookiesMod")) {
            return "unknown";
        }
        return userAgent.split("/")[1].split(" ")[0];
    }
}
