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

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setUsername(oAuth2User.getAttribute("name"));
            newUser.setRole(Role.ROLE_USER);
            newUser.setEnabled(true);
            newUser.setProvider("GOOGLE");
            newUser.setProviderId(oAuth2User.getAttribute("sub"));
            return userRepository.save(newUser);
        });

        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}
