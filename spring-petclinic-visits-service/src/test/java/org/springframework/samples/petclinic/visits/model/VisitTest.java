package org.springframework.samples.petclinic.visits.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Date;
import org.junit.jupiter.api.Test;

class VisitTest {

    @Test
    void testVisitBuilder() {
        // Arrange
        Integer id = 1;
        Date date = new Date();
        String description = "Annual checkup";
        int petId = 2;

        // Act
        Visit visit = Visit.VisitBuilder.aVisit()
                .id(id)
                .date(date)
                .description(description)
                .petId(petId)
                .build();

        // Assert
        assertThat(visit.getId()).isEqualTo(id);
        assertThat(visit.getDate()).isEqualTo(date);
        assertThat(visit.getDescription()).isEqualTo(description);
        assertThat(visit.getPetId()).isEqualTo(petId);
    }

    @Test
    void testVisitSettersAndGetters() {
        // Arrange
        Visit visit = new Visit();
        Integer id = 1;
        Date date = new Date();
        String description = "Vaccination";
        int petId = 3;

        // Act
        visit.setId(id);
        visit.setDate(date);
        visit.setDescription(description);
        visit.setPetId(petId);

        // Assert
        assertThat(visit.getId()).isEqualTo(id);
        assertThat(visit.getDate()).isEqualTo(date);
        assertThat(visit.getDescription()).isEqualTo(description);
        assertThat(visit.getPetId()).isEqualTo(petId);
    }

    @Test
    void testDefaultDate() {
        // Arrange & Act
        Visit visit = new Visit();

        // Assert
        assertThat(visit.getDate()).isNotNull();
    }

    // @Test
    // void testDescriptionOverflow() {
    // // Arrange
    // Visit visit = new Visit();
    // String longDescription = "a".repeat(8193); // 8193 characters long

    // // Act
    // visit.setDescription(longDescription);

    // // Assert
    // assertThat(visit.getDescription()).isEqualTo(longDescription);
    // assertThat(visit.getDescription().length()).isLessThanOrEqualTo(8192);
    // assertThatThrownBy(() -> visit.setDescription("a".repeat(8193)))
    // .isInstanceOf(IllegalArgumentException.class)
    // .hasMessageContaining("Description exceeds maximum length of 8192
    // characters");
    // }
}