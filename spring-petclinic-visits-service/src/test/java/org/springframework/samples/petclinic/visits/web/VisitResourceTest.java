package org.springframework.samples.petclinic.visits.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
class VisitResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VisitRepository visitRepository;

    @Test
    void shouldFetchVisits() throws Exception {
        given(visitRepository.findByPetIdIn(asList(111, 222)))
                .willReturn(
                        asList(
                                Visit.VisitBuilder.aVisit()
                                        .id(1)
                                        .petId(111)
                                        .build(),
                                Visit.VisitBuilder.aVisit()
                                        .id(2)
                                        .petId(222)
                                        .build(),
                                Visit.VisitBuilder.aVisit()
                                        .id(3)
                                        .petId(222)
                                        .build()));

        mvc.perform(get("/pets/visits?petId=111,222"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[1].id").value(2))
                .andExpect(jsonPath("$.items[2].id").value(3))
                .andExpect(jsonPath("$.items[0].petId").value(111))
                .andExpect(jsonPath("$.items[1].petId").value(222))
                .andExpect(jsonPath("$.items[2].petId").value(222));
    }

    @Test
    void shouldGetVisitsByPetId() throws Exception {
        given(visitRepository.findByPetId(5))
                .willReturn(
                        asList(
                                Visit.VisitBuilder.aVisit().id(10).petId(5).description("Dental cleaning").build(),
                                Visit.VisitBuilder.aVisit().id(11).petId(5).description("Vaccination").build()));

        mvc.perform(get("/owners/2/pets/5/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].description").value("Dental cleaning"))
                .andExpect(jsonPath("$[1].id").value(11))
                .andExpect(jsonPath("$[1].description").value("Vaccination"));
    }

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void shouldCreateVisit() throws Exception {
        Visit visit = Visit.VisitBuilder.aVisit()
                .id(1)
                .petId(7)
                .description("Routine checkup")
                .build();

        given(visitRepository.save(any(Visit.class))).willReturn(visit);

        mvc.perform(post("/owners/1/pets/7/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(visit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.petId").value(7))
                .andExpect(jsonPath("$.description").value("Routine checkup"));
    }
}
