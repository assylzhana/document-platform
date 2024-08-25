package com.assylzhana.user_service.service;

import com.assylzhana.user_service.model.CustomOAuth2User;
import com.assylzhana.user_service.model.Role;
import com.assylzhana.user_service.model.User;
import com.assylzhana.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuth2Service implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String githubId = (String) attributes.get("id");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from GitHub account");
        }
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> registerNewUser(email, githubId));

        return new CustomOAuth2User(user, attributes);
    }

    private User registerNewUser(String email, String githubId) {
        User user = new User();
        user.setEmail(email);
        user.setUsername(email);
        user.setProvider("github");
        user.setProviderId(githubId);
        user.setRole(Role.ROLE_USER);
        user.setEnabled(true);

        return userRepository.save(user);
    }
}