package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VetTest {

    private Vet vet;
    private Specialty surgery;
    private Specialty radiology;
    private Specialty dentistry;

    @BeforeEach
    void setUp() {
        vet = new Vet();

        surgery = new Specialty();
        surgery.setName("surgery");

        radiology = new Specialty();
        radiology.setName("radiology");

        dentistry = new Specialty();
        dentistry.setName("dentistry");
    }

    @Test
    void addSpeciality_specialityAdded_success() {
        // Initial state
        assertEquals(0, vet.getNrOfSpecialties(), "Should start with no specialties");

        // Add a specialty
        vet.addSpecialty(surgery);
        assertTrue(vet.getSpecialtiesInternal().contains(surgery), "Specialties should contain added specialty");
        assertEquals(1, vet.getNrOfSpecialties(), "Should have one specialty after adding");

        // Add a null specialty - this should probably be avoided in practice, but let's test
        vet.addSpecialty(null);
        assertEquals(2, vet.getNrOfSpecialties(), "Null specialty should still be counted");
    }

    @Test
    void getSpecialties_sortedByName_success() {
        // Add specialties
        vet.addSpecialty(radiology);
        vet.addSpecialty(surgery);
        vet.addSpecialty(dentistry);

        // Get sorted specialties
        List<Specialty> sortedSpecialties = vet.getSpecialties();

        // Check the order
        assertEquals(dentistry, sortedSpecialties.get(0), "First specialty should be dentistry");
        assertEquals(radiology, sortedSpecialties.get(1), "Second specialty should be radiology");
        assertEquals(surgery, sortedSpecialties.get(2), "Third specialty should be surgery");
    }

    @Test
    void getAndSetVetProperties_success() {
        // Set properties
        vet.setId(1);
        vet.setFirstName("John");
        vet.setLastName("Doe");

        // Check properties
        assertEquals(1, vet.getId(), "ID should be 1");
        assertEquals("John", vet.getFirstName(), "First name should be John");
        assertEquals("Doe", vet.getLastName(), "Last name should be Doe");
    }
}
