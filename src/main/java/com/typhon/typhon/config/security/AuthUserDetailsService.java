package com.typhon.typhon.config.security;

import com.typhon.typhon.entity.User;
import com.typhon.typhon.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userService.getValidUser(username);
        if (Objects.isNull(user)) {
            throw new UsernameNotFoundException("用户不存在");
        }
        SecurityUserDetail userDetail = new SecurityUserDetail();
        userDetail.setUser(user);
        return userDetail;
    }
}
