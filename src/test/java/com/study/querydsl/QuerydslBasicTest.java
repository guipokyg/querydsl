package com.study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.dto.MemberDto;
import com.study.querydsl.dto.UserDto;
import com.study.querydsl.entity.Member;
import com.study.querydsl.entity.QMember;
import com.study.querydsl.entity.Team;
import com.sun.java.accessibility.util.EventID;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Assert;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static com.study.querydsl.entity.QMember.*;
import static com.study.querydsl.entity.QTeam.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        em.flush();
        //초기화
        em.clear();
    }

    @Test
    public void startJPQL() {
        //member1을 찾아라.
        String qlString =
                "select m from Member m " +
                        "where m.username = :username";

        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void startQuerydsl(){
        queryFactory = new JPAQueryFactory(em);

        /* 1번
        QMember m = new QMember("m");

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1")) //파라메터 바인딩
                .fetchOne();*/

        //2번 QMember.member >> QMember는 스테틱 임포트
        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        System.out.println("findMember.getUsername() = " + findMember.getUsername());

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }


    /**
     *
     * member.username.eq("member1") // username = 'member1'
     * member.username.ne("member1") //username != 'member1'
     * member.username.eq("member1").not() // username != 'member1'
     * member.username.isNotNull() //이름이 is not null
     * member.age.in(10, 20) // age in (10,20)
     * member.age.notIn(10, 20) // age not in (10, 20)
     * member.age.between(10,30) //between 10, 30
     * member.age.goe(30) // age >= 30
     * member.age.gt(30) // age > 30
     * member.age.loe(30) // age <= 30
     * member.age.lt(30) // age < 30
     * member.username.like("member%") //like 검색
     * member.username.contains("member") // like ‘%member%’ 검색
     * member.username.startsWith("member") //like ‘member%’ 검색
     *
     */
    @Test
    public void search(){

        Member findMember = queryFactory
                .selectFrom(member)
                //이것도 AND  조건임
                .where(member.username.eq("member1")
                        ,member.age.eq(10)
                     )
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    /**
     * fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
     * fetchOne() : 단 건 조회
     * 결과가 없으면 : null
     * 결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException
     * fetchFirst() : limit(1).fetchOne()
     * fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행
     * fetchCount() : count 쿼리로 변경해서 count 수 조회
     *
     */

    @Test
    public void resultFetch(){
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();

        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();

        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();

        //카운트조회랑 쿼리조회 두번 쿼리가 나감
        QueryResults<Member> fetchResults = queryFactory
                .selectFrom(member)
                .fetchResults();

        long totalCount = fetchResults.getTotal();
        List<Member> list = fetchResults.getResults();

        long count = queryFactory
                .selectFrom(member)
                .fetchCount();
    }

    /**
     * 1. 회원 나이 내림차순
     * 2. 회원 이름 올림차순
     * 회원 이름이 없으면 마지막 출력 (nulls last)
     */
    @Test
    public void sort(){
        em.persist(new Member(null,100));
        em.persist(new Member("member6",100));
        em.persist(new Member("member7",100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())
                .fetch();

        Member member6 = result.get(0);
        Member member7 = result.get(1);
        Member memberNull = result.get(2);

        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(member7.getUsername()).isEqualTo("member7");
        assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1(){

        //offset,Limit로 처리
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetch();
    }

    @Test
    public void paging2(){

        //offset,Limit로 처리
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)
                .limit(2)
                .fetchResults();

        //fetch Results로 들고올수 있음.
        assertThat(results.getTotal()).isEqualTo(4);
        assertThat(results.getLimit()).isEqualTo(2);
        assertThat(results.getOffset()).isEqualTo(1);
        assertThat(results.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation(){
        List<Tuple> result = queryFactory
                .select(member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
    }

    @Test
    public void group(){
        List<Tuple> result = queryFactory
                .select(team.name
                        ,member.age.avg())
                .from(member.team, team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }

    @Test
    public void join(){
        List<Member> result = queryFactory
                .selectFrom(member)
                //.join(member.team, team)
                .leftJoin(member.team, team)
                .where(team.name.eq("teamA"))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인 - 외부조인이 불가능 (left outer join 이런게 불가능)
     */
    @Test
    public void theta_join(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * (외부조인)아우터조인 사용법
     */
    @Test
    public void join_on_filtering(){

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA"))
                .fetch();

        result.forEach(obj ->{
            System.out.println("obj = " + obj);
        });

        //이것도 결과는 같음(막조인) , join_on_no_relation와 같음
        List<Tuple> result3 = queryFactory
                .select(member,team)
                .from(member)
                .leftJoin(team)
                .on(member.team.eq(team),team.name.eq("teamA"))
                .fetch();
        result3.forEach(obj ->{
            System.out.println("obj3 = " + obj);
        });
    }



    /**
     * 연관관계가 없는 엔티티 외부조인 (막조인)
     */
    @Test
    public void join_on_no_relation(){
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member,team)
                .from(member)
                .leftJoin(team)
                .on(member.username.eq(team.name))
                .fetch();

        result.forEach(obj ->{
            System.out.println("obj = " + obj);
        });

        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    @PersistenceUnit
    EntityManagerFactory emf;
    @Test
    public void fetchJoinNo(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치조인 미적용").isFalse();
    }

    @Test
    public void fetchJoin(){
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team,team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        //*************on은 패치조인 사용불가!!
        Member findMember1 = queryFactory
                .selectFrom(member)
                .join(team).fetchJoin().on(member.team.eq(team))
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치조인 적용").isTrue();
    }



    /**
     * //서브쿼리
     * 나이가 가장많은 회원 조회
     */
    @Test
    public void subQuery() {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(40);
    }

    /**
     * //서브쿼리
     * 나이가 평균이상인 회원 조회
     */
    @Test
    public void subQueryGoe() {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(30, 40);
    }

    /**
     * //서브쿼리
     * 나이가 평균이상인 회원 조회
     */
    @Test
    public void subQueryIn() {
        QMember memberSub = new QMember("memberSub");
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();

        assertThat(result).extracting("age")
                .containsExactly(20,30, 40);

    }

    /**
     * select 스칼라 쿼리도 사용가능
     */
    @Test
    public void selectSubQuery(){
        QMember memberSub = new QMember("memberSub");
        List<Tuple> result = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub))
                .from(member)
                .fetch();


    }

    @Test
    public void baseCase(){
        List<String> result = queryFactory
                .select(member.age
                        .when(10).then("열심")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s: result){
            System.out.println("s = " + s);
        }
    }

    @Test
    public void complexCase(){
        List<String> result = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타"))
                .from(member)
                .fetch();

        for (String s: result){
            System.out.println("s = " + s);
        }
    }

    //상수,문자 더하기
    @Test
    public void constant(){
        List<Tuple> result = queryFactory.select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
            System.out.println("tuple = " + tuple.get(Expressions.constant("A")));
        }
    }

    @Test
    public void concat(){
        String result = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue())) //타입을 맞춰줘야함.
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();
    }

    @Test
    public void simpleProjection(){
        List<String> result = queryFactory.select(member.username)
                .from(member)
                .fetch();
    }

    @Test
    public void tupleProjection(){
        List<Tuple> fetch = queryFactory.select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : fetch) {
            System.out.println("tuple.get(member.username) = " + tuple.get(member.username));
            System.out.println("tuple.get(member.age) = " + tuple.get(member.age));

        }
    }

    @Test
    public void findDtoByJPQL(){
        List<MemberDto> result = em.createQuery(
                        "select new com.study.querydsl.dto.MemberDto(m.username, m.age) " +
                                "from Member m", MemberDto.class)
                .getResultList();
    }

    @Test
    //DTO의 getter setter 가 필요함
    public void findDtoBySetter(){
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
    }

    @Test
    //DTO의 getter setter 가 필요없음
    public void findDtoByField(){
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
    }

    @Test
    //생성자로 접근. DTO와 member의 타입이 맞아야 한다.
    public void findDtoByConstructor(){
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();
    }

    @Test
    //필드는 변수명과 타입을 맞춰주어야한다.
    public void findUserDto(){
        QMember memberSub = new QMember("memberSub");

        List<UserDto> fetch = queryFactory
                .select(Projections.fields(UserDto.class,
                                member.username.as("name"),
                                ExpressionUtils.as(
                                        JPAExpressions
                                                .select(memberSub.age.max())
                                                .from(memberSub), "age")
                        )
                ).from(member)
                .fetch();
    }

    @Test
    //생성자는 데이터 타입만 맞춰주면된다.
    public void findUserDtoByConstructor(){
        List<UserDto> result = queryFactory
                .select(Projections.constructor(UserDto.class,
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (UserDto dto : result){
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findDtoByQueryProjection(){
        queryFactory.select()
                .from(member)
                .fetch();
    }

    //@QueryProjection 활용

    @Test
    public void queryProjection(){
        /*List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }*/
    }

    @Test
    public void  distinct(){
        List<String> result = queryFactory
                .select(member.username).distinct()
                .from(member)
                .fetch();
    }

    /**  동적쿼리------ 처리 */
    @Test
    public void dynamicQuery_BooleanBuilder() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;
        List<Member> result = searchMember1(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameCond, Integer ageCond) {
        BooleanBuilder builder = new BooleanBuilder();

        //아래처럼 초기값 세팅가능
        //BooleanBuilder builder = new BooleanBuilder(member.username.eq(usernameCond));

        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }
        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }
        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }

    @Test
    public void dynamicQuery_WhereParam() throws Exception {
        String usernameParam = "member1";
        Integer ageParam = 10;
        List<Member> result = searchMember2(usernameParam, ageParam);
        Assertions.assertThat(result.size()).isEqualTo(1);
    }
    private List<Member> searchMember2(String usernameCond, Integer ageCond) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameCond), ageEq(ageCond))
                .fetch();
    }

    private BooleanExpression usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }
    private BooleanExpression ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    //조합가능
    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq(usernameCond).and(ageEq(ageCond));
    }

    @Test
    public void bulkUpdate(){
        long count = queryFactory.update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();
        //벌크연산을 사용하면..영속성컨텍스트와 동기화안되는 문제가 있다
        //초기화를 시켜주자
        em.flush();
        em.clear();

    }

    @Test
    public void bulkAdd(){
        long count = queryFactory
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();
        //multiply >> 곱하기
    }

    @Test
    public void bulkDelete(){
        long count = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();

    }

    @Test
    public void sqlFunction(){
        String result = queryFactory
                .select(Expressions.stringTemplate("function('replace', {0}, {1},{2})", member.username, "member", "M"))
                .from(member)
                .fetchFirst();

        List<String> fetch = queryFactory
                .select(member.username)
                .from(member)
                //.where(member.username.eq(Expressions.stringTemplate("function('lower', {0})", member.username)))
                .where(member.username.eq(member.username.lower()))
                .fetch();
    }
}
