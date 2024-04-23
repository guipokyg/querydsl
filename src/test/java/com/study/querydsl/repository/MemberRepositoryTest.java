package com.study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.QMember;
import com.study.querydsl.entity.QTeam;
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

    @Test
    public void queryDslPredicateExcutorTest(){
        QMember member = QMember.member;
        QTeam team = QTeam.team;
        //Iterable<Member> member1 = memberRepository.findAll(member.age.between(20, 40).and(member.username.eq("member1")));
        Iterable<Member> member1 = memberRepository.findAll(team.name.eq("A").and(member.username.eq("member1")));

        for (Member findMember : member1) {
            System.out.println("findMember = " + findMember);
        }
    }
}