package com.study.querydsl.repository;

import com.study.querydsl.entity.Member;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberRepository memberRepository;

    @Test
    public void  basicTest(){
        Member member = new Member("member1", 10);
        memberRepository.save(member);

        Member findMember = memberRepository.findById(member.getId()).get();
        System.out.println("findMember = " + findMember);

        List<Member> result1 = memberRepository.findAll();
        System.out.println("result2 = " + result1);

        List<Member> result2 = memberRepository.findByUsername("member1");
        System.out.println("result2 = " + result2);
    }
}