package com.sangyunpark.user.domain.entity;

import com.sangyunpark.user.common.BaseEntity;
import com.sangyunpark.user.domain.vo.RegisterType;
import com.sangyunpark.user.domain.vo.UserStatus;
import com.sangyunpark.user.domain.vo.UserType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id @GeneratedValue
    @Column(name = "user_id")
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserType userType;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private UserStatus userStatus;

    @Column
    private String providerId;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private RegisterType registerType;

    @Column(nullable = false)
    private String phoneNumber;

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserAddress> userAddress = new ArrayList<>();

    public void addUserAddress(UserAddress address) {
        this.userAddress.add(address);
        address.setUser(this);
    }

}
