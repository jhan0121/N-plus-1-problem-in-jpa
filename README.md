# N + 1 문제가 발생하는 원인

주요한 N + 1 문제 발생 원인은 JPQL에서 엔티티 데이터를 어디까지 가져올지 명시하지 않은 경우 발생합니다. 여기서 주목할 점은 엔티티 데이터를 어디까지 가져올지 설정하는 옵션(EAGER, LAZY)에 무관한
모든 경우에서 발생할 수 있다는 점입니다.

전제 조건:

- `Owner` 와 `Pet` 을 일대다 연관 관계를 가지고 있다고 가정하겠습니다.
- 10명의 `Owner` 는 각각 10 마리의 `Pet`을 가지고 있다고 가정하겠습니다.
- 상호 연관 관계 fetch 옵션은 각 상황별 동일하게 설정하겠습니다.

## fetch = EAGER 일 경우

- Owner 엔티티에 `@OneToMany(fetch = EAGER)`로 설정되어 있으면, Owner를 조회할 때마다 연관된 Pet을 즉시 로딩
- Owner가 10명이므로 각 Owner의 Pets를 로딩하기 위해 Pet 테이블 select 쿼리를 10번 실행
    - N(Pet 쿼리)+1(Owner 쿼리) 문제 발생

## fetch = LAZY 일 경우

- Owner 엔티티에 `@OneToMany(fetch = LAZY)`로 설정되어 있으면, Owner를 조회할 때에는 연관된 Pet을 실제 entity가 아닌 proxy로 로딩(pet 초기화를 위한 쿼리가 실행되지는
  않음)
- 여기서 Owner의 pets에 접근할 시점에 pets 초기화를 위한 select 쿼리가 실행
    - 각 최초 접근마다 1(Pet 쿼리)+1(Owner 쿼리) 쿼리 발생
    - 즉, 모든 Pet에 최초 접근 시, N(Pet 쿼리)+1(Owner 쿼리) 문제 발생

- 또한 반대로 Pet을 조회하면 Owner는 실제 entity가 아닌 proxy로 로딩(owner 초기화를 위한 쿼리가 실행되지 않음)
- 여기서 Pet의 owner에 접근하면 owner 초기화를 위한 select 쿼리가 실행
    - 최초 접근 시, 1(Owner 쿼리)+1(Pet 쿼리) 쿼리 발생

결론적으로 N + 1 문제가 발생하는 주요 원인은 어떤 `fetch 타입이냐`가 아닌 `연관된 엔티티 로딩을 어디까지 할 것인지 명시되지 않았을 때`라는 것을 알 수 있습니다.
(즉, fetch 타입 설정에 따른 차이점은 `언제 N + 1 문제가 발생하느냐`라는 점입니다. )

---

N + 1 문제를 해결하는 대표적인 방법은 `어디까지 로딩할 것인지` 명시하는 것입니다. `어디까지 로딩할지 명시` 하는 주요 방법은 2가지가 있습니다.

## fetch join

jpql의 `fetch join`을 사용하면 쿼리 단에서 연관된 엔티티를 한 번에 조회할 수 있습니다.

#### 예시

```java
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    @Query("select o from Owner o join fetch o.pets")
    List<Owner> findAllFetchPets();
}
```

## entityGraph

Spring Data JPA에서 제공하는 `@EntityGraph` annotation을 Repository 메서드에 적용하여 연관된 엔티티를 한 번에 조회할 수 있습니다.

#### 예시

```java
public interface OwnerRepository extends JpaRepository<Owner, Long> {

    @EntityGraph(attributePaths = "pets")
    @Query("select o from Owner o")
    List<Owner> findAllFetchPetsByEntityGraph();
}
```
