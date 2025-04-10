package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VetTest {

    @Test
    void testAddSpecialty() {
        Vet vet = new Vet();
        Specialty specialty = new Specialty();
        specialty.setName("Radiology");

        vet.addSpecialty(specialty);

        assertEquals(1, vet.getNrOfSpecialties());
        assertEquals("Radiology", vet.getSpecialties().get(0).getName());
    }

    @Test
    void testGetSpecialtiesWhenEmpty() {
        Vet vet = new Vet();
        List<Specialty> specialties = vet.getSpecialties();

        assertNotNull(specialties);
        assertTrue(specialties.isEmpty());
    }

    @Test
    void testSettersAndGetters() {
        Vet vet = new Vet();
        vet.setId(1);
        vet.setFirstName("John");
        vet.setLastName("Doe");

        assertEquals(1, vet.getId());
        assertEquals("John", vet.getFirstName());
        assertEquals("Doe", vet.getLastName());
    }
}
