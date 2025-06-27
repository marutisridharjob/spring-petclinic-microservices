package org.springframework.samples.petclinic.customers.web;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Owner;

public class OwnerTest {
    @Test
    void testOwnerToString() {
        Owner owner = new Owner();
        owner.setFirstName("John");
        owner.setLastName("Doe");
        assertTrue(owner.toString().contains("John"));
    }
}