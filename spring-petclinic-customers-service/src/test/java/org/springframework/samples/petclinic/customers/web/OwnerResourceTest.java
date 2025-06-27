package org.springframework.samples.petclinic.customers.web;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.samples.petclinic.customers.model.Owner;
import org.springframework.samples.petclinic.customers.model.OwnerRepository;
import org.springframework.samples.petclinic.customers.web.mapper.OwnerEntityMapper;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OwnerResource.class)
@ActiveProfiles("test")
@Import(OwnerEntityMapper.class)
public class OwnerResourceTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OwnerRepository ownerRepository;

    @Test
    void shouldGetOwnerById() throws Exception {
        Owner owner = new Owner();

        java.lang.reflect.Field idField = Owner.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(owner, 1);

        owner.setFirstName("John");

        given(ownerRepository.findById(1)).willReturn(Optional.of(owner));

        mockMvc.perform(get("/owners/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void shouldCreateOwner() throws Exception {
        Owner owner = new Owner();
        owner.setFirstName("Jane");
        owner.setLastName("Doe");

        given(ownerRepository.save(org.mockito.ArgumentMatchers.any(Owner.class)))
                .willReturn(owner);

        OwnerRequest request = new OwnerRequest("Jane", "Doe", "123 Main St", "Springfield", "1234567890");
        String json = new ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/owners")
                .contentType("application/json")
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }
}
