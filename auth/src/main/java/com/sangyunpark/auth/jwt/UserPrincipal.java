package com.sangyunpark.auth.jwt;

import com.sangyunpark.auth.constants.enums.UserStatus;
import com.sangyunpark.auth.constants.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private final String email;
    private final String userType;
    private final String userStatus;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> userType);
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return UserStatus.valueOf(userStatus) == UserStatus.ACTIVE;
    }

    public UserType getUserType() {
        return UserType.valueOf(userType);
    }

    public UserStatus getUserStatus() {
        return UserStatus.valueOf(userStatus);
    }
}
