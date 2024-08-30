package dev.morazzer.cookies.backend.services;

import dev.morazzer.cookies.backend.auth.JwtUtils;
import dev.morazzer.cookies.backend.entities.db.PlayerPermissions;
import dev.morazzer.cookies.backend.entities.other.MinecraftUser;
import dev.morazzer.cookies.backend.exceptions.InvalidCredentialsException;
import dev.morazzer.cookies.backend.repositories.PlayerPermissionRepository;
import dev.morazzer.cookies.entities.request.AuthRequest;
import dev.morazzer.cookies.entities.response.AuthResponse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final JwtUtils jwtUtils;
    private final PlayerPermissionRepository playerPermissionRepository;
    private final MojangAuthService mojangAuthService;
    private final ModVersionService modVersionService;

    public AuthService(JwtUtils jwtUtils, PlayerPermissionRepository playerPermissionRepository, MojangAuthService mojangAuthService, ModVersionService modVersionService) {
        this.jwtUtils = jwtUtils;
        this.playerPermissionRepository = playerPermissionRepository;
        this.mojangAuthService = mojangAuthService;
        this.modVersionService = modVersionService;
    }

    public String createToken(String userAgent, MinecraftUser user) {
        final List<PlayerPermissions> allByUuid = playerPermissionRepository.findAllByUuid(user.uuid());

        return jwtUtils.createToken(
            modVersionService.getModVersion(userAgent),
            user.uuid(),
            user.name(),
            allByUuid.stream().map(PlayerPermissions::getScope).toList());
    }

    public AuthResponse createToken(String userAgent, AuthRequest authRequest, String ip) throws InvalidCredentialsException {
        final Optional<MinecraftUser> authenticated =
            this.mojangAuthService.isAuthenticated(authRequest.sharedSecret(), authRequest.username(), ip);

        if (authenticated.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        return new AuthResponse(createToken(userAgent, authenticated.get()));
    }
}
