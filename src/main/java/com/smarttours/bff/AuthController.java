package com.smarttours.bff;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@Profile("bff")
public class AuthController {

    @GetMapping("/user")
    public ResponseEntity<?> currentUser(@AuthenticationPrincipal OidcUser oidcUser) {
        if (oidcUser == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(Map.of(
                "sub",      oidcUser.getSubject(),
                "email",    oidcUser.getEmail()            != null ? oidcUser.getEmail()           : "",
                "name",     oidcUser.getFullName()         != null ? oidcUser.getFullName()        : "",
                "username", oidcUser.getPreferredUsername() != null ? oidcUser.getPreferredUsername() : ""
        ));
    }
}
