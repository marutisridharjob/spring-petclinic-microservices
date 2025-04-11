package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.Specialty;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VetTest {

    private Vet vet;
    private Specialty specialty1;
    private Specialty specialty2;

    @BeforeEach
    void setUp() {
        vet = new Vet();
        specialty1 = new Specialty();
        specialty1.setName("radiology");
        specialty2 = new Specialty();
        specialty2.setName("surgery");
    }

    @Test
    void testSetAndGetFirstName() {
        String firstName = "James";
        vet.setFirstName(firstName);
        assertThat(vet.getFirstName()).isEqualTo(firstName);
    }

    @Test
    void testSetAndGetLastName() {
        String lastName = "Carter";
        vet.setLastName(lastName);
        assertThat(vet.getLastName()).isEqualTo(lastName);
    }

    @Test
    void testSetAndGetId() {
        Integer id = 1;
        vet.setId(id);
        assertThat(vet.getId()).isEqualTo(id);
    }

    @Test
    void testSpecialtiesInitiallyEmpty() {
        assertThat(vet.getSpecialties()).isEmpty();
        assertThat(vet.getNrOfSpecialties()).isZero();
    }

    @Test
    void testAddSpecialty() {
        vet.addSpecialty(specialty1);
        assertThat(vet.getNrOfSpecialties()).isEqualTo(1);
        assertThat(vet.getSpecialties()).contains(specialty1);
    }

    @Test
    void testAddMultipleSpecialties() {
        vet.addSpecialty(specialty1);
        vet.addSpecialty(specialty2);
        assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
        assertThat(vet.getSpecialties()).containsExactlyInAnyOrder(specialty1, specialty2);
    }

    @Test
    void testSpecialtiesSorting() {
        // Add specialties in reverse order to check sorting
        vet.addSpecialty(specialty2); // "surgery"
        vet.addSpecialty(specialty1); // "radiology"
        
        List<Specialty> specialties = vet.getSpecialties();
        assertThat(specialties.size()).isEqualTo(2);
        // "radiology" should come before "surgery" when sorted by name
        assertThat(specialties.get(0).getName()).isEqualTo("radiology");
        assertThat(specialties.get(1).getName()).isEqualTo("surgery");
    }
}