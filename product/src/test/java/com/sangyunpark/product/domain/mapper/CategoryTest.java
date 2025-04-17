package com.sangyunpark.product.domain.mapper;

import com.sangyunpark.product.constant.ErrorCode;
import com.sangyunpark.product.domain.entity.Category;
import com.sangyunpark.product.exception.BusinessException;
import com.sangyunpark.product.infrastructure.repository.CategoryJpaRepository;
import com.sangyunpark.product.presentation.dto.request.CategoryRequestDto;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@SuppressWarnings("NonAsciiCharacters")
class CategoryTest {

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private EntityManager em;

    @Test
    @DisplayName("부모가 없는 최상위 카테고리 생성")
    void 최상위_카테고리_생성() {
        Category category = Category.builder().name("운동").build();
        Category saved = categoryJpaRepository.save(category);

        assertThat(saved.getParent()).isNull();
        assertThat(saved.getChildren()).isEmpty();
    }

    @Test
    @DisplayName("부모 카테고리를 가진 자식 카테고리 생성")
    void 자식_카테고리_생성() {
        Category parent = categoryJpaRepository.save(Category.builder().name("운동기구").build());
        Category child = categoryJpaRepository.save(Category.builder().name("팔운동").parent(parent).build());

        em.flush();
        em.clear();

        Category foundParent = categoryJpaRepository.findById(parent.getId()).orElseThrow();
        assertThat(child.getParent().getId()).isEqualTo(parent.getId());
        assertThat(foundParent.getChildren()).extracting(Category::getName).contains("팔운동");
    }

    @Test
    @DisplayName("다단계 카테고리 구조 생성")
    void 다단계_카테고리_생성() {
        Category root = categoryJpaRepository.save(Category.builder().name("운동").build());
        Category middle = categoryJpaRepository.save(Category.builder().name("유산소").parent(root).build());
        Category leaf = categoryJpaRepository.save(Category.builder().name("러닝머신").parent(middle).build());

        assertThat(leaf.getParent().getName()).isEqualTo("유산소");
        assertThat(leaf.getParent().getParent().getName()).isEqualTo("운동");
    }

    @Test
    @DisplayName("부모 카테고리 삭제 시 자식도 삭제")
    void 부모_삭제_시_자식_삭제() {
        Category parent = Category.builder().name("운동기구").build();
        Category child = Category.builder().name("팔운동").build();

        parent.addChild(child);
        categoryJpaRepository.save(parent);
        categoryJpaRepository.delete(parent);

        em.flush();
        em.clear();

        assertThat(categoryJpaRepository.findById(child.getId())).isEmpty();
    }

    @Test
    @DisplayName("부모 카테고리 변경")
    void 부모_변경() {
        Category parent1 = categoryJpaRepository.save(Category.builder().name("운동기구").build());
        Category parent2 = categoryJpaRepository.save(Category.builder().name("헬스기구").build());
        Category child = categoryJpaRepository.save(Category.builder().name("팔운동").parent(parent1).build());

        child.update(new CategoryRequestDto("팔운동", parent2.getId()), parent2);

        em.flush();
        em.clear();

        Category updated = categoryJpaRepository.findById(child.getId()).orElseThrow();
        assertThat(updated.getParent().getId()).isEqualTo(parent2.getId());
    }

    @Test
    @DisplayName("자식 없는 leaf 카테고리 삭제")
    void 자식_없는_카테고리_삭제() {
        Category leaf = categoryJpaRepository.save(Category.builder().name("단일").build());
        categoryJpaRepository.delete(leaf);

        em.flush();
        em.clear();

        assertThat(categoryJpaRepository.findById(leaf.getId())).isEmpty();
    }

    @Test
    @DisplayName("자식 있는 중간 카테고리 삭제 시 전부 삭제")
    void 중간_삭제_전부_삭제() {
        Category parent = Category.builder().name("운동").build();
        Category middle = Category.builder().name("중간").build();
        Category leaf = Category.builder().name("하위").build();

        parent.addChild(middle);
        middle.addChild(leaf);

        categoryJpaRepository.save(parent); // cascade로 다 저장됨
        categoryJpaRepository.delete(middle);

        assertThat(categoryJpaRepository.findById(leaf.getId())).isEmpty();
    }

    @Test
    @DisplayName("순환 참조 방지")
    void 순환_참조_방지() {
        // given
        Category parent = Category.builder().name("부모").build();
        Category child = Category.builder().name("자식").build();
        parent.addChild(child);

        categoryJpaRepository.save(parent); // 이 한 줄로 둘 다 저장됨

        // when & then
        assertThatThrownBy(() ->
                child.update(new CategoryRequestDto("자식", child.getId()), child)
        ).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CATEGORY_SELF_PARENT);
    }

    @Test
    @DisplayName("손자 카테고리를 부모로 설정하여 순환 참조 유도")
    void 손자_카테고리를_부모로_설정하면_순환참조_예외() {
        // given
        Category grandParent = Category.builder().name("A").build();
        Category parent = Category.builder().name("B").build();
        Category child = Category.builder().name("C").build();

        grandParent.addChild(parent);
        parent.addChild(child);

        categoryJpaRepository.save(parent);

        // when & then
        assertThatThrownBy(() ->
                grandParent.update(new CategoryRequestDto("A", child.getId()), child)
        ).isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.CATEGORY_SELF_PARENT);
    }

    @Test
    @DisplayName("정상적인 부모 변경은 예외 없이 동작")
    void 정상_부모_변경() {
        // given
        Category a = categoryJpaRepository.save(Category.builder().name("A").build());
        Category b = categoryJpaRepository.save(Category.builder().name("B").build());

        // when
        a.update(new CategoryRequestDto("A변경", b.getId()), b);

        // then
        assertThat(a.getParent()).isEqualTo(b);
    }

    @Test
    @DisplayName("카테고리 이름만 수정")
    void 카테고리_이름_수정() {
        Category category = categoryJpaRepository.save(Category.builder().name("기존").build());
        category.update(new CategoryRequestDto("수정됨", null), null);
        assertThat(category.getName()).isEqualTo("수정됨");
    }

    @Test
    @DisplayName("카테고리 이름은 전역에서 유일해야 한다")
    void 카테고리_이름_중복_예외() {
        categoryJpaRepository.save(Category.builder().name("헬스").build());

        Category duplicate = Category.builder().name("헬스").build();

        assertThatThrownBy(() -> categoryJpaRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
