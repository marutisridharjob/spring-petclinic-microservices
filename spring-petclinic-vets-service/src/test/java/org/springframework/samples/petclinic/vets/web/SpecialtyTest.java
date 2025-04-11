package org.springframework.samples.petclinic.vets.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.samples.petclinic.vets.model.Specialty;

class SpecialtyTest {

    private Specialty specialty;

    @BeforeEach
    void setUp() {
        specialty = new Specialty();
    }

    @Test
    void testSetAndGetName() {
        String name = "surgery";
        specialty.setName(name);
        assertThat(specialty.getName()).isEqualTo(name);
    }

    @Test
    void testGetId() {
        // ID is null by default until saved to database
        assertThat(specialty.getId()).isNull();
    }
}