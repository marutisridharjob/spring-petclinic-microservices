package org.springframework.samples.petclinic.visits.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.visits.VisitsServiceApplication;
import org.springframework.samples.petclinic.visits.model.Visit;
import org.springframework.samples.petclinic.visits.model.VisitRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Combined test class for the Visit service components.
 * Contains nested test classes for controller, model, repository and application tests.
 * This structure allows for organized and focused testing of each component.
 */
public class VisitsServiceTests {

    /**
     * Tests for the VisitResource REST controller
     */
    @Nested
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
        
        private Visit visit1;
        private Visit visit2;
        private Visit visit3;
        
        @BeforeEach
        void setup() {
            visit1 = Visit.VisitBuilder.aVisit()
                .id(1)
                .petId(111)
                .date(new Date())
                .description("Annual checkup")
                .build();
                
            visit2 = Visit.VisitBuilder.aVisit()
                .id(2)
                .petId(222)
                .date(new Date())
                .description("Vaccination")
                .build();
                
            visit3 = Visit.VisitBuilder.aVisit()
                .id(3)
                .petId(222)
                .date(new Date())
                .description("Skin condition")
                .build();
        }

        @Test
        void shouldFetchVisitsByPetIds() throws Exception {
            given(visitRepository.findByPetIdIn(asList(111, 222)))
                .willReturn(asList(visit1, visit2, visit3));

            mvc.perform(get("/pets/visits?petId=111,222"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(1))
                .andExpect(jsonPath("$.items[1].id").value(2))
                .andExpect(jsonPath("$.items[2].id").value(3))
                .andExpect(jsonPath("$.items[0].petId").value(111))
                .andExpect(jsonPath("$.items[1].petId").value(222))
                .andExpect(jsonPath("$.items[2].petId").value(222))
                .andExpect(jsonPath("$.items[0].description").value("Annual checkup"))
                .andExpect(jsonPath("$.items[1].description").value("Vaccination"))
                .andExpect(jsonPath("$.items[2].description").value("Skin condition"));
        }
        
        @Test
        void shouldFetchVisitsForSinglePet() throws Exception {
            when(visitRepository.findByPetId(111)).thenReturn(List.of(visit1));
            
            mvc.perform(get("/owners/*/pets/111/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].petId").value(111))
                .andExpect(jsonPath("$[0].description").value("Annual checkup"));
                
            verify(visitRepository).findByPetId(111);
        }
        
        @Test
        void shouldFetchMultipleVisitsForSinglePet() throws Exception {
            when(visitRepository.findByPetId(222)).thenReturn(List.of(visit2, visit3));
            
            mvc.perform(get("/owners/*/pets/222/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[1].id").value(3))
                .andExpect(jsonPath("$[0].petId").value(222))
                .andExpect(jsonPath("$[1].petId").value(222));
                
            verify(visitRepository).findByPetId(222);
        }
        
        @Test
        void shouldCreateNewVisit() throws Exception {
            Visit newVisit = Visit.VisitBuilder.aVisit()
                .description("Dental cleaning")
                .date(new Date())
                .build();
                
            Visit savedVisit = Visit.VisitBuilder.aVisit()
                .id(4)
                .petId(333)
                .description("Dental cleaning")
                .date(newVisit.getDate())
                .build();
                
            when(visitRepository.save(any(Visit.class))).thenReturn(savedVisit);
            
            mvc.perform(post("/owners/*/pets/333/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newVisit)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.petId").value(333))
                .andExpect(jsonPath("$.description").value("Dental cleaning"));
        }
        
        @Test
        void shouldRejectInvalidVisit() throws Exception {
            StringBuilder tooLongDescription = new StringBuilder();
            for (int i = 0; i < 8193; i++) {
                tooLongDescription.append("a");
            }
            
            Visit invalidVisit = new Visit();
            invalidVisit.setDescription(tooLongDescription.toString());
            
            mvc.perform(post("/owners/*/pets/444/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidVisit)))
                .andExpect(status().isBadRequest());
        }
        
        @Test
        void shouldRejectInvalidPetId() throws Exception {
            Visit newVisit = Visit.VisitBuilder.aVisit()
                .description("Routine checkup")
                .date(new Date())
                .build();
                
            mvc.perform(post("/owners/*/pets/0/visits")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newVisit)))
                .andExpect(status().isBadRequest());
        }
        
        @Test
        void shouldReturnEmptyListForNonExistentPet() throws Exception {
            when(visitRepository.findByPetId(999)).thenReturn(List.of());
            
            mvc.perform(get("/owners/*/pets/999/visits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
        }
    }

    /**
     * Tests for the Visit model class
     */
    @Nested
    class VisitModelTest {

        @Test
        void shouldCreateVisitUsingBuilder() {
             
            Date testDate = new Date();
            
             
            Visit visit = Visit.VisitBuilder.aVisit()
                .id(1)
                .date(testDate)
                .description("Routine checkup")
                .petId(123)
                .build();
                
              
            assertEquals(1, visit.getId());
            assertEquals(testDate, visit.getDate());
            assertEquals("Routine checkup", visit.getDescription());
            assertEquals(123, visit.getPetId());
        }
        
        @Test
        void shouldCreateAndModifyVisit() {
             
            Visit visit = new Visit();
            Date testDate = new Date();
            
             
            visit.setId(5);
            visit.setDate(testDate);
            visit.setDescription("Vaccination");
            visit.setPetId(456);
            
              
            assertEquals(5, visit.getId());
            assertEquals(testDate, visit.getDate());
            assertEquals("Vaccination", visit.getDescription());
            assertEquals(456, visit.getPetId());
        }
        
        @Test
        void shouldHaveDefaultDateWhenCreated() {
             
            Visit visit = new Visit();
            
              
            assertNotNull(visit.getDate());
        }
        
        @Test
        void builderShouldCreateDifferentObjects() {
             
            Visit visit1 = Visit.VisitBuilder.aVisit().id(1).petId(100).build();
            Visit visit2 = Visit.VisitBuilder.aVisit().id(2).petId(200).build();
            
              
            assertNotEquals(visit1.getId(), visit2.getId());
            assertNotEquals(visit1.getPetId(), visit2.getPetId());
        }
    }

    /**
     * Tests for the VisitRepository using DataJpaTest
     */
    @Nested
    @DataJpaTest
    @ActiveProfiles("test")
    class VisitRepositoryTest {

        @Autowired
        private TestEntityManager entityManager;

        @Autowired
        private VisitRepository visitRepository;
        
        private Visit visit1;
        private Visit visit2;
        private Visit visit3;
        
        @BeforeEach
        void setup() {
            // Create test visits
            visit1 = Visit.VisitBuilder.aVisit()
                .date(new Date())
                .description("Annual checkup")
                .petId(1)
                .build();
                
            visit2 = Visit.VisitBuilder.aVisit()
                .date(new Date())
                .description("Vaccination")
                .petId(2)
                .build();
                
            visit3 = Visit.VisitBuilder.aVisit()
                .date(new Date())
                .description("Surgery followup")
                .petId(1)
                .build();
                
            entityManager.persist(visit1);
            entityManager.persist(visit2);
            entityManager.persist(visit3);
            entityManager.flush();
        }
        
        @Test
        void shouldFindAllVisits() {
             
            List<Visit> visits = visitRepository.findAll();
            
              
            assertEquals(3, visits.size());
        }
        
        @Test
        void shouldFindVisitsByPetId() {
             
            List<Visit> petVisits = visitRepository.findByPetId(1);
            
              
            assertEquals(2, petVisits.size());
            assertEquals("Annual checkup", petVisits.get(0).getDescription());
            assertEquals("Surgery followup", petVisits.get(1).getDescription());
        }
        
        @Test
        void shouldFindVisitsByMultiplePetIds() {
             
            List<Visit> visits = visitRepository.findByPetIdIn(Arrays.asList(1, 2));
            
              
            assertEquals(3, visits.size());
        }
        
        @Test
        void shouldSaveVisit() {
             
            Visit newVisit = Visit.VisitBuilder.aVisit()
                .date(new Date())
                .description("Dental cleaning")
                .petId(3)
                .build();
                
            Visit savedVisit = visitRepository.save(newVisit);
            
            assertNotNull(savedVisit.getId());
            assertEquals("Dental cleaning", savedVisit.getDescription());
            assertEquals(3, savedVisit.getPetId());
            
            Visit foundVisit = entityManager.find(Visit.class, savedVisit.getId());
            assertNotNull(foundVisit);
            assertEquals(savedVisit.getId(), foundVisit.getId());
        }
        
        @Test
        void shouldReturnEmptyListForNonExistentPetId() {
            List<Visit> visits = visitRepository.findByPetId(999);
            
            assertEquals(0, visits.size());
        }
        
        @Test
        void shouldFindVisitById() {
             
            Visit found = visitRepository.findById(visit1.getId()).orElse(null);
            
              
            assertNotNull(found);
            assertEquals(visit1.getDescription(), found.getDescription());
            assertEquals(visit1.getPetId(), found.getPetId());
        }
    }

    /**
     * Tests for application context loading
     */
    @Nested
    @SpringBootTest
    @ActiveProfiles("test")
    class VisitsServiceApplicationTests {

        @Autowired
        private ApplicationContext applicationContext;
        
        @Test
        void contextLoads() {
            assertNotNull(applicationContext);
        }
        
        @Test
        void controllerBeanExists() {
            VisitResource controller = applicationContext.getBean(VisitResource.class);
            assertNotNull(controller);
        }
        
        @Test
        void repositoryBeanExists() {
            VisitRepository repository = applicationContext.getBean(VisitRepository.class);
            assertNotNull(repository);
        }
        
        @Test
        void mainApplicationStartsSuccessfully() {
            VisitsServiceApplication.main(new String[] {});
        }
    }
}