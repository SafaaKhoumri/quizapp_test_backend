package com.example.demo.controllers;

import com.example.demo.model.*;
import com.example.demo.repositories.*;
import com.example.demo.services.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private AdministrateurRepository administrateurRepository;

    @Autowired
    private ThemeRepository domaineRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompetencyRepository competencyRepository;

    @Autowired
    private CandidatRepository condidatsRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/sendTest")
    public ResponseEntity<String> sendTestEmails(@RequestBody TestRequest testRequest) {
        try {
            logger.info("Received test request: {}", testRequest);

            // Afficher les candidats pour vérifier qu'ils sont correctement envoyés
            logger.info("Candidates: {}", testRequest.getCandidates());

            Optional<Administrateur> administratorOpt = administrateurRepository
                    .findByEmail(testRequest.getAdminEmail());
            if (!administratorOpt.isPresent()) {
                logger.error("Administrator not found with email: {}", testRequest.getAdminEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Administrator not found.");
            }
            Administrateur administrator = administratorOpt.get();

            Optional<Theme> domaineOpt = domaineRepository.findById(testRequest.getDomaineId());
            if (!domaineOpt.isPresent()) {
                logger.error("Domain not found with ID: {}", testRequest.getDomaineId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Domain not found.");
            }
            Theme domaine = domaineOpt.get();

            Optional<Role> roleOpt = roleRepository.findById(testRequest.getRoleId());
            if (!roleOpt.isPresent()) {
                logger.error("Role not found with ID: {}", testRequest.getRoleId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found.");
            }
            Role role = roleOpt.get();

            List<Competency> competencies = competencyRepository.findAllById(testRequest.getCompetencyIds());
            if (competencies.isEmpty()) {
                logger.error("No competencies found for IDs: {}", testRequest.getCompetencyIds());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No competencies found.");
            }

            List<Condidats> candidates = testRequest.getCandidates();
            if (candidates == null || candidates.isEmpty()) {
                logger.error("No candidates provided in the request.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No candidates provided.");
            }

            // Enregistrer les candidats
            for (Condidats candidate : candidates) {
                if (candidate.getId() == null) {
                    condidatsRepository.save(candidate);
                }
            }

            Test test = new Test();
            test.setName(testRequest.getTestName());
            test.setLanguage(testRequest.getTestLanguage());
            test.setAdministrator(administrator);
            test.setDomaine(domaine);
            test.setRole(role);
            test.setCompetencies(competencies);
            test.setCandidates(candidates);

            logger.info("Saving test: {}", test);
            testRepository.save(test);

            String testLink = "http://localhost:3000/TakeTest/"; // Adjust this link according to your
                                                                 // frontend routing

            // Envoi d'email aux candidats
            for (Condidats candidate : candidates) {
                if (candidate.getEmail() != null && !candidate.getEmail().isEmpty()) {
                    logger.info("Sending email to: {}", candidate.getEmail());
                    String emailBody = "You are invited to take the test: " + testRequest.getTestName() + "\n\n"
                            + "Please click on the following link to take the test:\n"
                            + testLink;
                    emailService.sendEmail(candidate.getEmail(), "Test Invitation", emailBody);
                }
            }

            logger.info("Test created and emails sent successfully.");
            return ResponseEntity.ok("Emails sent successfully!");
        } catch (Exception e) {
            logger.error("Error creating test and sending emails", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send emails.");
        }
    }

    @PostMapping("/tests")
    public ResponseEntity<Test> createTest(@RequestBody TestRequest testRequest) {
        try {
            logger.info("Creating test: {}", testRequest);

            Optional<Administrateur> administratorOpt = administrateurRepository
                    .findByEmail(testRequest.getAdminEmail());
            if (!administratorOpt.isPresent()) {
                logger.error("Administrator not found with email: {}", testRequest.getAdminEmail());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            Administrateur administrator = administratorOpt.get();

            Optional<Theme> domaineOpt = domaineRepository.findById(testRequest.getDomaineId());
            if (!domaineOpt.isPresent()) {
                logger.error("Domain not found with ID: {}", testRequest.getDomaineId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            Theme domaine = domaineOpt.get();

            Optional<Role> roleOpt = roleRepository.findById(testRequest.getRoleId());
            if (!roleOpt.isPresent()) {
                logger.error("Role not found with ID: {}", testRequest.getRoleId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            Role role = roleOpt.get();

            List<Competency> competencies = competencyRepository.findAllById(testRequest.getCompetencyIds());
            if (competencies.isEmpty()) {
                logger.error("No competencies found for IDs: {}", testRequest.getCompetencyIds());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            List<Condidats> candidates = testRequest.getCandidates();
            if (candidates == null || candidates.isEmpty()) {
                logger.error("No candidates provided in the request.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }

            // Enregistrer les candidats
            for (Condidats candidate : candidates) {
                if (candidate.getId() == null) {
                    condidatsRepository.save(candidate);
                }
            }

            Test test = new Test();
            test.setName(testRequest.getTestName());
            test.setLanguage(testRequest.getTestLanguage());
            test.setAdministrator(administrator);
            test.setDomaine(domaine);
            test.setRole(role);
            test.setCompetencies(competencies);
            test.setCandidates(candidates);

            logger.info("Saving test: {}", test);
            testRepository.save(test);

            logger.info("Test created successfully: {}", test);
            return ResponseEntity.ok(test);

        } catch (Exception e) {
            logger.error("Error creating test", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    public static class TestRequest {
        private String testName;
        private String testLanguage;
        private String adminEmail;
        private Long domaineId;
        private Long roleId;
        private Long levelId;
        private List<Long> competencyIds;
        private List<Condidats> candidates;

        // Getters and setters
        public String getTestName() {
            return testName;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public String getTestLanguage() {
            return testLanguage;
        }

        public void setTestLanguage(String testLanguage) {
            this.testLanguage = testLanguage;
        }

        public String getAdminEmail() {
            return adminEmail;
        }

        public void setAdminEmail(String adminEmail) {
            this.adminEmail = adminEmail;
        }

        public Long getDomaineId() {
            return domaineId;
        }

        public void setDomaineId(Long domaineId) {
            this.domaineId = domaineId;
        }

        public Long getRoleId() {
            return roleId;
        }

        public void setRoleId(Long roleId) {
            this.roleId = roleId;
        }

        public Long getLevelId() {
            return levelId;
        }

        public void setLevelId(Long levelId) {
            this.levelId = levelId;
        }

        public List<Long> getCompetencyIds() {
            return competencyIds;
        }

        public void setCompetencyIds(List<Long> competencyIds) {
            this.competencyIds = competencyIds;
        }

        public List<Condidats> getCandidates() {
            return candidates;
        }

        public void setCandidates(List<Condidats> candidates) {
            this.candidates = candidates;
        }
    }
}
