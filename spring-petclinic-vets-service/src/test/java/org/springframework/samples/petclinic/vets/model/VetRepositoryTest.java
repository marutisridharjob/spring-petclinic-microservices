package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VetRepositoryTest {

    @Autowired
    private VetRepository vetRepository;

    @BeforeEach
    void setUp() {
        // Pre-populate the database with sample data
        Vet vet1 = new Vet();
        vet1.setFirstName("John");
        vet1.setLastName("Doe");

        Vet vet2 = new Vet();
        vet2.setFirstName("Jane");
        vet2.setLastName("Smith");

        vetRepository.save(vet1);
        vetRepository.save(vet2);
    }

    @Test
    void testFindAll() {
        List<Vet> vets = vetRepository.findAll();

        assertNotNull(vets);
        assertFalse(vets.isEmpty()); // Ensure the list is not empty
        assertEquals(2, vets.size()); // Verify the number of vets
    }

    @Test
    void testSaveVet() {
        Vet vet = new Vet();
        vet.setFirstName("Alice");
        vet.setLastName("Brown");

        Vet savedVet = vetRepository.save(vet);

        assertNotNull(savedVet.getId());
        assertEquals("Alice", savedVet.getFirstName());
        assertEquals("Brown", savedVet.getLastName());
    }
}
