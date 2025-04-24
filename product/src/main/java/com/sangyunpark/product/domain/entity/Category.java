package com.sangyunpark.product.domain.entity;

import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.presentation.dto.request.CategoryRequestDto;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Category> children = new ArrayList<>();

    public void update(final CategoryRequestDto dto, final Category parent) {
        if (this.equals(parent) || hasCircularReference(parent)) {
            throw new BusinessException(ErrorCode.CATEGORY_SELF_PARENT);
        }
        this.name = dto.name();
        this.parent = parent;
    }

    private boolean hasCircularReference(final Category newParent) {
        Category current = newParent;
        while(current != null) {
            if(this.equals(current)) return true;
            current = current.parent;
        }

        return false;
    }

    public void addChild(final Category child) {
        children.add(child);
        child.parent = this;
    }
}
