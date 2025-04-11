package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.samples.petclinic.vets.model.VetRepository;
import org.springframework.samples.petclinic.vets.model.Vet;
import org.springframework.samples.petclinic.vets.model.Specialty;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class VetRepositoryTest {

    @Autowired
    private VetRepository vetRepository;

    @Test
    void testFindAll() {
        List<Vet> vets = vetRepository.findAll();
        
        // Based on the test data.sql, there should be 6 vets
        assertThat(vets).hasSize(6);
        
        // Verify some data from the test dataset
        assertThat(vets).anyMatch(v -> 
            v.getFirstName().equals("James") && v.getLastName().equals("Carter")
        );
        
        assertThat(vets).anyMatch(v -> 
            v.getFirstName().equals("Helen") && v.getLastName().equals("Leary")
        );
    }

    @Test
    void testFindById() {
        // ID 3 in test data is Linda Douglas with surgery and dentistry specialties
        Vet vet = vetRepository.findById(3).orElse(null);
        
        assertThat(vet).isNotNull();
        assertThat(vet.getFirstName()).isEqualTo("Linda");
        assertThat(vet.getLastName()).isEqualTo("Douglas");
        
        // Should have 2 specialties: surgery and dentistry
        assertThat(vet.getNrOfSpecialties()).isEqualTo(2);
        List<Specialty> specialties = vet.getSpecialties();
        assertThat(specialties).extracting("name")
            .containsExactlyInAnyOrder("surgery", "dentistry");
    }
    
    @Test
    void testSaveVet() {
        Vet newVet = new Vet();
        newVet.setFirstName("John");
        newVet.setLastName("Doe");
        
        Vet savedVet = vetRepository.save(newVet);
        
        assertThat(savedVet.getId()).isNotNull();
        assertThat(savedVet.getFirstName()).isEqualTo("John");
        assertThat(savedVet.getLastName()).isEqualTo("Doe");
        
        // Verify it's in the database
        Vet found = vetRepository.findById(savedVet.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getFirstName()).isEqualTo("John");
    }
}