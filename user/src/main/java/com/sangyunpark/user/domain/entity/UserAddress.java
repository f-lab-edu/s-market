package com.sangyunpark.user.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_address")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_address_id")
    private Long id;

    @Column(nullable = false)
    private String receiverName;

    @Setter
    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT), nullable = false)
    private User user;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Boolean defaultAddress;

}