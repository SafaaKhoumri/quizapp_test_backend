package com.example.demo.controllers;

import com.example.demo.model.*;
import com.example.demo.repositories.*;
import com.example.demo.services.EmailService;
import com.example.demo.services.QuestionService;
import com.example.demo.services.TestService;

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
    private NiveauRepository levelRepository;

    @Autowired
    private CompetencyRepository competencyRepository;

    @Autowired
    private CandidatRepository candidatsRepository;

    @Autowired
    private EmailService emailService;
    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ReponseRepository answerRepository;

    @Autowired
    private QuestionService questionService;

    @Autowired
    private TestService testService;

    @GetMapping("/tests")
    public ResponseEntity<List<Test>> getAllTests() {
        List<Test> tests = testService.getAllTests();
        return ResponseEntity.ok(tests);
    }

    @GetMapping("/tests/{id}")
    public ResponseEntity<Test> getTestById(@PathVariable Long id) {
        Optional<Test> test = testService.getTestById(id);
        return test.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/tests/{id}/candidates")
    public ResponseEntity<List<Condidats>> getCandidatesByTestId(@PathVariable Long id) {
        List<Condidats> candidates = testService.getCandidatesByTestId(id);
        return candidates != null ? ResponseEntity.ok(candidates) : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/tests/{testId}/questions")
    public ResponseEntity<List<Question>> getQuestionsByTestId(@PathVariable Long testId) {
        Optional<Test> testOpt = testService.getTestById(testId);
        if (!testOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        Test test = testOpt.get();
        List<Question> questions = test.getQuestions();
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/createAndSendTest")
    public ResponseEntity<String> createAndSendTest(@RequestBody TestRequest testRequest) {
        try {
            logger.info("Received test request: {}", testRequest);

            // Retrieve and verify related entities
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

            Optional<Level> levelOpt = levelRepository.findById(testRequest.getLevelId());
            if (!levelOpt.isPresent()) {
                logger.error("Level not found with ID: {}", testRequest.getLevelId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Level not found.");
            }
            Level level = levelOpt.get();

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
                    candidatsRepository.save(candidate);
                }
            }

            List<Question> questions = questionService.findQuestionsByCompetencyIds(testRequest.getCompetencyIds());

            Test test = new Test();
            test.setName(testRequest.getTestName());
            test.setAdministrator(administrator);
            test.setDomaine(domaine);
            test.setRole(role);
            test.setLevel(level);
            test.setCompetencies(competencies);
            test.setCandidates(candidates);

            // Set the test reference in each question
            for (Question question : questions) {
                question.setTest(test);
            }

            test.setQuestions(questions);

            logger.info("Saving test: {}", test);
            testRepository.save(test);

            String testLink = "http://localhost:3000/TakeTest/" + test.getId();

            // Envoi d'email aux candidats
            for (Condidats candidate : candidates) {
                if (candidate.getEmail() != null && !candidate.getEmail().isEmpty()) {
                    logger.info("Sending email to: {}", candidate.getEmail());
                    String emailBody = "You are invited to take the test: " + testRequest.getTestName() + "\n\n"
                            + "Please click on the following link to take the test:\n" + testLink;
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

    public static class TestRequest {
        private String testName;
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

    @PostMapping("/tests/{id}/submit")
    public ResponseEntity<String> submitTest(@PathVariable Long id, @RequestBody List<AnswerRequest> answerRequests) {
        Optional<Test> testOpt = testService.getTestById(id);
        if (!testOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Test not found.");
        }

        Test test = testOpt.get();
        for (AnswerRequest answerRequest : answerRequests) {
            Answer answer = new Answer();
            answer.setTexteReponse(answerRequest.getTexteReponse());
            answer.setEstCorrecte(answerRequest.isEstCorrecte());

            Optional<Question> questionOpt = questionRepository.findById(answerRequest.getQuestionId());
            if (questionOpt.isPresent()) {
                answer.setQuestion(questionOpt.get());
            }

            Optional<Condidats> candidatOpt = candidatsRepository.findById(answerRequest.getCandidatId());
            if (candidatOpt.isPresent()) {
                answer.setCandidat(candidatOpt.get());
            }

            answerRepository.save(answer);
        }

        return ResponseEntity.ok("Test submitted successfully!");
    }

    public static class AnswerRequest {
        private Long questionId;
        private Long candidatId;
        private String texteReponse;
        private boolean estCorrecte;

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public Long getCandidatId() {
            return candidatId;
        }

        public void setCandidatId(Long candidatId) {
            this.candidatId = candidatId;
        }

        public String getTexteReponse() {
            return texteReponse;
        }

        public void setTexteReponse(String texteReponse) {
            this.texteReponse = texteReponse;
        }

        public boolean isEstCorrecte() {
            return estCorrecte;
        }

        public void setEstCorrecte(boolean estCorrecte) {
            this.estCorrecte = estCorrecte;
        }
    }

}
