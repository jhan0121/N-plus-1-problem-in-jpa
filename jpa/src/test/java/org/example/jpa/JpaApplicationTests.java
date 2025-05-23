package org.example.jpa;

import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.Session;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class JpaApplicationTests {

    @Autowired
    PetRepository petRepository;

    @Autowired
    OwnerRepository ownerRepository;

    @PersistenceContext
    Session session;

    /**
     * Owner와 Pet의 양방향 fetch 옵션을 EAGER로 설정 후 실행해주세요.
     */
    @Test
    @Disabled
    void test() {
        for (int k = 0; k < 10; k++) {
            List<Pet> pets = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Pet pet = Pet.builder().name("pet" + (i + k * 10)).build();
                pets.add(pet);
            }
            petRepository.saveAll(pets);
            Owner owner = Owner.builder().name("owner" + k).build();
            owner.setPets(pets);
            for (Pet pet : pets) {
                pet.setOwner(owner);
            }
            ownerRepository.save(owner);
        }

        session.flush();
        session.clear();

        System.out.println("-------------------------------");
        List<Owner> ownerList = ownerRepository.findAll();
        for (Owner owner : ownerList) {
            System.out.println(owner);
        }
        System.out.println("-------------------------------");
        List<Pet> petList = petRepository.findAll();
        for (Pet pet : petList) {
            System.out.println(pet);
        }
    }

    /**
     * Owner와 Pet의 양방향 fetch 옵션을 LAZY로 설정 후 실행해주세요.
     */
    @Test
    void testLazyLoadingNPlus1Problem() {
        // Create 10 owners, each with 10 pets
        for (int k = 0; k < 10; k++) {
            List<Pet> pets = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Pet pet = Pet.builder().name("pet" + (i + k * 10)).build();
                pets.add(pet);
            }
            petRepository.saveAll(pets);
            Owner owner = Owner.builder().name("owner" + k).build();
            owner.setPets(pets);
            for (Pet pet : pets) {
                pet.setOwner(owner);
            }
            ownerRepository.save(owner);
        }

        session.flush();
        session.clear();

        System.out.println("-------------------------------");
        System.out.println("Fetching all owners");
        System.out.println("-------------------------------");
        List<Owner> ownerList = ownerRepository.findAll();

        System.out.println("-------------------------------");
        System.out.println("Accessing pets of each owner - N+1 problem demonstration");
        System.out.println("-------------------------------");
        for (Owner owner : ownerList) {
            System.out.println("Owner: " + owner.getName());
            // This will trigger lazy loading for each owner's pets - N+1 problem
            System.out.println("Pets count: " + owner.getPets().size());
        }

        System.out.println("-------------------------------");
        System.out.println("Fetching all pets");
        System.out.println("-------------------------------");
        List<Pet> petList = petRepository.findAll();

        System.out.println("-------------------------------");
        System.out.println("Accessing owner of each pet - N+1 problem demonstration");
        System.out.println("-------------------------------");
        for (Pet pet : petList) {
            System.out.println("Pet: " + pet.getName());
            // This will trigger lazy loading for each pet's owner - N+1 problem
            if (pet.getOwner() != null) {
                System.out.println("Owner: " + pet.getOwner().getName());
            }
        }
    }

    /**
     * OwnerRepository에 대한 N + 1 문제 해결 방법 코드 입니다.
     */
    @Test
    void testNPlus1ProblemSolutions() {
        // Create 10 owners, each with 10 pets
        for (int k = 0; k < 10; k++) {
            List<Pet> pets = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Pet pet = Pet.builder().name("pet" + (i + k * 10)).build();
                pets.add(pet);
            }
            petRepository.saveAll(pets);
            Owner owner = Owner.builder().name("owner" + k).build();
            owner.setPets(pets);
            for (Pet pet : pets) {
                pet.setOwner(owner);
            }
            ownerRepository.save(owner);
        }

        session.flush();
        session.clear();

        // Solution 1: Using JOIN FETCH
        System.out.println("-------------------------------");
        System.out.println("Solution 1: Using JOIN FETCH");
        System.out.println("-------------------------------");
        List<Owner> ownersWithJoinFetch = ownerRepository.findAllWithPets();
        System.out.println("Owners fetched with JOIN FETCH: " + ownersWithJoinFetch.size());

        // Access pets without triggering additional queries
        for (Owner owner : ownersWithJoinFetch) {
            System.out.println("Owner: " + owner.getName());
            System.out.println("Pets count: " + owner.getPets().size());
            // Access each pet's name without additional queries
            for (Pet pet : owner.getPets()) {
                System.out.println("  Pet: " + pet.getName());
            }
        }

        session.clear();

        // Solution 2: Using Named Entity Graph
        System.out.println("-------------------------------");
        System.out.println("Solution 2: Using Named Entity Graph");
        System.out.println("-------------------------------");
        List<Owner> ownersWithNamedEntityGraph = ownerRepository.findAllWithNamedEntityGraph();
        System.out.println("Owners fetched with Named Entity Graph: " + ownersWithNamedEntityGraph.size());

        // Access pets without triggering additional queries
        for (Owner owner : ownersWithNamedEntityGraph) {
            System.out.println("Owner: " + owner.getName());
            System.out.println("Pets count: " + owner.getPets().size());
            // Access each pet's name without additional queries
            for (Pet pet : owner.getPets()) {
                System.out.println("  Pet: " + pet.getName());
            }
        }

        session.clear();

        // Solution 3: Using Entity Graph with Attribute Paths
        System.out.println("-------------------------------");
        System.out.println("Solution 3: Using Entity Graph with Attribute Paths");
        System.out.println("-------------------------------");
        List<Owner> ownersWithEntityGraph = ownerRepository.findAllWithPetsEntityGraph();
        System.out.println("Owners fetched with Entity Graph (Attribute Paths): " + ownersWithEntityGraph.size());

        // Access pets without triggering additional queries
        for (Owner owner : ownersWithEntityGraph) {
            System.out.println("Owner: " + owner.getName());
            System.out.println("Pets count: " + owner.getPets().size());
            // Access each pet's name without additional queries
            for (Pet pet : owner.getPets()) {
                System.out.println("  Pet: " + pet.getName());
            }
        }
    }
}
