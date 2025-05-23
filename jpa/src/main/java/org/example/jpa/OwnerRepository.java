package org.example.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OwnerRepository extends JpaRepository<Owner, Long> {

    @Query("SELECT o FROM Owner o JOIN FETCH o.pets")
    List<Owner> findAllWithPets();
    
    @EntityGraph(value = "Owner.withPets", type = EntityGraph.EntityGraphType.FETCH)
    @Query("SELECT o FROM Owner o")
    List<Owner> findAllWithNamedEntityGraph();

    @EntityGraph(attributePaths = "pets")
    @Query("SELECT o FROM Owner o")
    List<Owner> findAllWithPetsEntityGraph();
}
