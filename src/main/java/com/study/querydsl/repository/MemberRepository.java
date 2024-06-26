package com.study.querydsl.repository;

import com.querydsl.core.types.Predicate;
import com.study.querydsl.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface MemberRepository extends JpaRepository<Member, Long>,MemberRepositoryCustom, QuerydslPredicateExecutor<Member> {
    List<Member> findByUsername(String username);

    @Query(value = "select m from Member m")
    List<Member> findByNativeQuery(Predicate predicate);
}
