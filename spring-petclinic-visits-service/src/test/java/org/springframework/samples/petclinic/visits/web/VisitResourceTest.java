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
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;

@ExtendWith(SpringExtension.class)
@WebMvcTest(VisitResource.class)
@ActiveProfiles("test")
class VisitResourceTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    VisitRepository visitRepository;

    @Autowired
    ObjectMapper objectMapper;

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
    void shouldFetchVisitsForSinglePet() throws Exception {
        // Arrange
        given(visitRepository.findByPetId(333))
                .willReturn(
                        singletonList(
                                Visit.VisitBuilder.aVisit()
                                        .id(4)
                                        .petId(333)
                                        .description("Regular checkup")
                                        .build()));

        // Act & Assert
        mvc.perform(get("/owners/*/pets/333/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4))
                .andExpect(jsonPath("$[0].petId").value(333))
                .andExpect(jsonPath("$[0].description").value("Regular checkup"));
    }

    @Test
    void shouldCreateNewVisit() throws Exception {
        // Arrange
        Visit visit = Visit.VisitBuilder.aVisit()
                .date(new Date())
                .description("Dental cleaning")
                .build();

        Visit savedVisit = Visit.VisitBuilder.aVisit()
                .id(5)
                .petId(444)
                .date(visit.getDate())
                .description("Dental cleaning")
                .build();

        given(visitRepository.save(any(Visit.class))).willReturn(savedVisit);

        // Act & Assert
        mvc.perform(post("/owners/*/pets/444/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(visit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.petId").value(444))
                .andExpect(jsonPath("$.description").value("Dental cleaning"));

        verify(visitRepository).save(any(Visit.class));
    }

    // @Test
    // void shouldReturnBadRequestWhenVisitInvalid() throws Exception {
    // // Creating a visit with an empty description (validation would fail)
    // Visit invalidVisit = new Visit();
    // invalidVisit.setDescription(""); // Empty description

    // mvc.perform(post("/owners/*/pets/555/visits")
    // .contentType(MediaType.APPLICATION_JSON)
    // .content(objectMapper.writeValueAsString(invalidVisit)))
    // .andExpect(status().isBadRequest());
    // }
}
