package dev.morazzer.cookies.backend.controllers;

import dev.morazzer.cookies.backend.exceptions.InvalidCredentialsException;
import dev.morazzer.cookies.backend.services.AuthService;
import dev.morazzer.cookies.entities.request.AuthRequest;
import dev.morazzer.cookies.entities.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping({"login", "auth"})
    public AuthResponse auth(@RequestBody AuthRequest authRequest, HttpServletRequest req, @RequestHeader("User-Agent") String userAgent) {
        try {
            return authService.createToken(userAgent, authRequest, req.getRemoteAddr());
        } catch (InvalidCredentialsException ignored) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Couldn't authorize");
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
