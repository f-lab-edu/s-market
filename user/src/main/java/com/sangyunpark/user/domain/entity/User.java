package com.sangyunpark.user.domain.entity;

import com.sangyunpark.user.constant.enums.RegisterType;
import com.sangyunpark.user.constant.enums.UserStatus;
import com.sangyunpark.user.constant.enums.UserType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

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

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void addUserAddress(UserAddress address) {
        this.userAddress.add(address);
        address.setUser(this);
    }
}
