package org.springframework.samples.petclinic.visits.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest
class VisitRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VisitRepository visitRepository;

    private Visit visit1;
    private Visit visit2;
    private Visit visit3;

    @BeforeEach
    void setup() {
        // Create sample visits
        visit1 = Visit.VisitBuilder.aVisit()
                .date(new Date())
                .description("Annual checkup")
                .petId(1)
                .build();

        visit2 = Visit.VisitBuilder.aVisit()
                .date(new Date())
                .description("Vaccination")
                .petId(1)
                .build();

        visit3 = Visit.VisitBuilder.aVisit()
                .date(new Date())
                .description("Surgery")
                .petId(2)
                .build();

        // Save visits to database
        entityManager.persist(visit1);
        entityManager.persist(visit2);
        entityManager.persist(visit3);
        entityManager.flush();
    }

    @Test
    void shouldFindByPetId() {
        // Act
        List<Visit> foundVisits = visitRepository.findByPetId(1);

        // Assert
        assertThat(foundVisits).hasSize(2);
        assertThat(foundVisits)
                .extracting(Visit::getDescription)
                .containsExactlyInAnyOrder("Annual checkup", "Vaccination");
    }

    @Test
    void shouldFindByPetIdIn() {
        // Act
        List<Visit> foundVisits = visitRepository.findByPetIdIn(Arrays.asList(1, 2));

        // Assert
        assertThat(foundVisits).hasSize(3);
        assertThat(foundVisits)
                .extracting(Visit::getDescription)
                .containsExactlyInAnyOrder("Annual checkup", "Vaccination", "Surgery");
    }

    @Test
    void shouldReturnEmptyListWhenNoPetIdMatches() {
        // Act
        List<Visit> foundVisits = visitRepository.findByPetId(999);

        // Assert
        assertThat(foundVisits).isEmpty();
    }
}