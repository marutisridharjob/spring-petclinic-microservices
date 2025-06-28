package org.springframework.samples.petclinic.customers.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.Pet;
import org.springframework.samples.petclinic.customers.model.PetType;

public class PetTest {
    @Test
    void testPetProperties() {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Buddy");

        Calendar cal = Calendar.getInstance();
        cal.set(2020, Calendar.JANUARY, 15);
        Date birthDate = cal.getTime();
        pet.setBirthDate(birthDate);

        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");
        pet.setOwner(owner);

        PetType type = new PetType();
        type.setId(3);
        type.setName("Dog");
        pet.setType(type);

        assertEquals(1, pet.getId());
        assertEquals("Buddy", pet.getName());
        assertEquals(birthDate, pet.getBirthDate());
        assertEquals(owner, pet.getOwner());
        assertEquals(type, pet.getType());
    }

    @Test
    void testPetEquals() {
        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");

        PetType type = new PetType();
        type.setId(3);
        type.setName("Dog");

        Pet pet1 = new Pet();
        pet1.setId(1);
        pet1.setName("Fluffy");
        pet1.setOwner(owner);
        pet1.setType(type);

        Pet pet2 = new Pet();
        pet2.setId(1);
        pet2.setName("Fluffy");
        pet2.setOwner(owner);
        pet2.setType(type);

        assertTrue(pet1.equals(pet2));
    }

    @Test
    void testPetToString() {
        Pet pet = new Pet();
        pet.setId(1);
        pet.setName("Fluffy");

        Owner owner = new Owner();
        owner.setFirstName("George");
        owner.setLastName("Bush");
        pet.setOwner(owner);

        PetType type = new PetType();
        type.setId(3);
        type.setName("Dog");
        pet.setType(type);

        String result = pet.toString();
        assertTrue(result.contains("Fluffy"));
        assertTrue(result.contains("1"));
    }
}