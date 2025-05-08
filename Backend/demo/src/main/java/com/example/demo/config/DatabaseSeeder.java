package com.example.demo.config;
import com.example.demo.model.*;
import com.example.demo.model.Module;
import com.example.demo.repository.*;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor // Lombok for constructor injection
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TagNameRepository tagNameRepository;
    private final ArticleRepository articleRepository;
    private final ArticleTagRepository articleTagRepository;
    private final ArticleVoteRepository articleVoteRepository;
    private final CommentRepository commentRepository;
    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final QuizRepository quizRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizCompletionRepository quizCompletionRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final SpaceMissionRepository spaceMissionRepository;

    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    @Transactional // Ensures all operations are part of a single transaction
    public void run(String... args) throws Exception {
        System.out.println("Starting database seeding...");

        // Check if data already exists to prevent re-seeding
        if (userRepository.count() > 0) {
            System.out.println("Database already seeded. Skipping.");
            return;
        }

        // --- 1. Seed Users ---
        List<User> users = seedUsers();

        // --- 2. Seed TagNames ---
        List<TagName> tagNames = seedTagNames();

        // --- 3. Seed Articles ---
        List<Article> articles = seedArticles(users);

        // --- 4. Seed ArticleTags (linking Articles and TagNames) ---
        seedArticleTags(articles, tagNames);

        // --- 5. Seed ArticleVotes ---
        seedArticleVotes(users, articles);

        // --- 6. Seed Comments ---
        seedComments(users, articles);

        // --- 7. Seed Courses ---
        List<Course> courses = seedCourses();

        // --- 8. Seed Modules (linking to Courses) ---
        List<Module> modules = seedModules(courses);

        // --- 9. Seed Lessons (linking to Modules) ---
        List<Lesson> lessons = seedLessons(modules);

        // --- 10. Seed Quizzes (linking to Lessons) ---
        List<Quiz> quizzes = seedQuizzes(lessons);

        // --- 11. Seed QuizQuestions (linking to Quizzes) ---
        seedQuizQuestions(quizzes);

        // --- 12. Seed QuizCompletions (Users completing Quizzes) ---
        seedQuizCompletions(users, quizzes);

        // --- 13. Seed CourseProgress (Users progressing through Courses) ---
        seedCourseProgress(users, courses, lessons);

        // --- 14. Seed ReadingHistory (Users reading Articles) ---
        seedReadingHistory(users, articles);

        // --- 15. Seed SpaceMissions (Only by verified users) ---
        seedSpaceMissions(users);

        System.out.println("Database seeding completed.");
    }

    private List<User> seedUsers() {
        System.out.println("Seeding Users...");
        List<User> users = new ArrayList<>();

        User admin = new User();
        admin.setUsername("admin_user");

        admin.setPassword(passwordEncoder.encode("password123")); // With Spring Security
        admin.setEmail("admin@example.com");
        admin.setBio("Administrator of the platform.");
        admin.setProfileImageUrl("https://i.pravatar.cc/150?u=admin");
        admin.setPhotoCoverUrl("https://picsum.photos/seed/admincover/800/200");
        admin.setRole(User.UserRole.ADMIN);
        admin.setVerificationStatus(User.UserVerification.VERIFIED);
        admin.setExperiencePoints(15000); // To be GALACTIC
        admin.addExperience(0); // to trigger level update
        users.add(admin);

        User verifiedUser = new User();
        verifiedUser.setUsername("verified_astronaut");
        verifiedUser.setPassword(passwordEncoder.encode("password123"));
        verifiedUser.setEmail("astronaut@example.com");
        verifiedUser.setBio("Verified user, loves space exploration.");
        verifiedUser.setProfileImageUrl("https://i.pravatar.cc/150?u=astro");
        verifiedUser.setPhotoCoverUrl("https://picsum.photos/seed/astrocover/800/200");
        verifiedUser.setRole(User.UserRole.USER);
        verifiedUser.setVerificationStatus(User.UserVerification.VERIFIED);
        verifiedUser.setExperiencePoints(6000); // To be ASTRONAUT
        verifiedUser.addExperience(0);
        users.add(verifiedUser);

        User regularUser = new User();
        regularUser.setUsername("space_explorer");
        regularUser.setPassword(passwordEncoder.encode("password123"));
        regularUser.setEmail("explorer@example.com");
        regularUser.setBio("Regular user, eager to learn.");
        regularUser.setProfileImageUrl("https://i.pravatar.cc/150?u=explorer");
        regularUser.setPhotoCoverUrl("https://picsum.photos/seed/explorercover/800/200");
        regularUser.setRole(User.UserRole.USER);
        regularUser.setVerificationStatus(User.UserVerification.UNVERIFIED);
        regularUser.setExperiencePoints(500); // To be NOVICE
        regularUser.addExperience(0);
        users.add(regularUser);

        User pendingUser = new User();
        pendingUser.setUsername("pending_genius");
        pendingUser.setPassword(passwordEncoder.encode("password123"));
        pendingUser.setEmail("pending@example.com");
        pendingUser.setBio("Awaiting verification.");
        pendingUser.setProfileImageUrl("https://i.pravatar.cc/150?u=pending");
        pendingUser.setPhotoCoverUrl("https://picsum.photos/seed/pendingcover/800/200");
        pendingUser.setRole(User.UserRole.USER);
        pendingUser.setVerificationStatus(User.UserVerification.PENDING);
        pendingUser.setExperiencePoints(1200); // To be EXPLORER
        pendingUser.addExperience(0);
        users.add(pendingUser);

        return userRepository.saveAll(users);
    }

    private List<TagName> seedTagNames() {
        System.out.println("Seeding TagNames...");
        List<TagName> tagNames = Arrays.asList(
                new TagName("Space Exploration"),
                new TagName("Astrophysics"),
                new TagName("Technology"),
                new TagName("Mars"),
                new TagName("Rockets"),
                new TagName("Java") // For Course/Article context
        );
        return tagNameRepository.saveAll(tagNames);
    }

    private List<Article> seedArticles(List<User> users) {
        System.out.println("Seeding Articles...");
        List<Article> articles = new ArrayList<>();
        String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.";
        String longContent = loremIpsum + "\n\n" + loremIpsum + "\n\n" + loremIpsum;


        articles.add(new Article(null, "The Future of Mars Colonization", "A look into upcoming missions and technologies for settling on Mars.", longContent,
                Arrays.asList("https://picsum.photos/seed/mars1/600/400", "https://picsum.photos/seed/mars2/600/400"),
                users.get(0), LocalDateTime.now().minusDays(10), new ArrayList<>(), new HashSet<>(), 0, 0L, new HashSet<>()));
        articles.add(new Article(null, "Understanding Black Holes", "Exploring the mysteries of black holes and their impact on the universe.", longContent,
                Arrays.asList("https://picsum.photos/seed/blackhole1/600/400"),
                users.get(1), LocalDateTime.now().minusDays(5), new ArrayList<>(), new HashSet<>(), 0, 0L, new HashSet<>()));
        articles.add(new Article(null, "Advancements in Rocket Propulsion", "New technologies that are making space travel faster and more efficient.", longContent,
                Arrays.asList("https://picsum.photos/seed/rocket1/600/400", "https://picsum.photos/seed/rocket2/600/400"),
                users.get(0), LocalDateTime.now().minusDays(2), new ArrayList<>(), new HashSet<>(), 0, 0L, new HashSet<>()));
        articles.add(new Article(null, "The Search for Extraterrestrial Life", "Current efforts and methods in the quest to find life beyond Earth.", longContent,
                Collections.emptyList(),
                users.get(2), LocalDateTime.now().minusDays(1), new ArrayList<>(), new HashSet<>(), 0, 0L, new HashSet<>()));

        return articleRepository.saveAll(articles);
    }

    private void seedArticleTags(List<Article> articles, List<TagName> tagNames) {
        System.out.println("Seeding ArticleTags...");
        List<ArticleTag> articleTags = new ArrayList<>();

        // Article 1: Mars Colonization -> Tags: Space Exploration, Mars, Technology
        articleTags.add(new ArticleTag(articles.get(0), tagNames.get(0))); // Space Exploration
        articleTags.add(new ArticleTag(articles.get(0), tagNames.get(3))); // Mars
        articleTags.add(new ArticleTag(articles.get(0), tagNames.get(2))); // Technology

        // Article 2: Black Holes -> Tags: Astrophysics
        articleTags.add(new ArticleTag(articles.get(1), tagNames.get(1))); // Astrophysics

        // Article 3: Rocket Propulsion -> Tags: Technology, Rockets, Space Exploration
        articleTags.add(new ArticleTag(articles.get(2), tagNames.get(2))); // Technology
        articleTags.add(new ArticleTag(articles.get(2), tagNames.get(4))); // Rockets
        articleTags.add(new ArticleTag(articles.get(2), tagNames.get(0))); // Space Exploration

        // Article 4: Extraterrestrial Life -> Tags: Space Exploration, Astrophysics
        articleTags.add(new ArticleTag(articles.get(3), tagNames.get(0))); // Space Exploration
        articleTags.add(new ArticleTag(articles.get(3), tagNames.get(1))); // Astrophysics

        articleTagRepository.saveAll(articleTags);

        // Refresh articles to reflect tags if needed for subsequent operations (though @Formula fields handle counts)
        // For direct association:
        articles.get(0).getTags().add(articleTags.get(0));
        articles.get(0).getTags().add(articleTags.get(1));
        articles.get(0).getTags().add(articleTags.get(2));
        articles.get(1).getTags().add(articleTags.get(3));
        // ... and so on, then articleRepository.saveAll(articles)
        // However, with @OneToMany(mappedBy = "article", cascade = CascadeType.ALL) in Article
        // and saving ArticleTag, the association should be managed.
    }

    private void seedArticleVotes(List<User> users, List<Article> articles) {
        System.out.println("Seeding ArticleVotes...");
        List<ArticleVote> votes = new ArrayList<>();

        // Article 1: 2 upvotes, 1 downvote
        votes.add(new ArticleVote(null, 1, users.get(0), articles.get(0)));
        votes.add(new ArticleVote(null, 1, users.get(1), articles.get(0)));
        votes.add(new ArticleVote(null, -1, users.get(2), articles.get(0)));

        // Article 2: 1 upvote
        votes.add(new ArticleVote(null, 1, users.get(0), articles.get(1)));

        // Article 3: 3 upvotes
        votes.add(new ArticleVote(null, 1, users.get(0), articles.get(2)));
        votes.add(new ArticleVote(null, 1, users.get(1), articles.get(2)));
        votes.add(new ArticleVote(null, 1, users.get(3), articles.get(2)));

        // Article 4: 1 downvote (by user 0)
        votes.add(new ArticleVote(null, -1, users.get(0), articles.get(3)));


        articleVoteRepository.saveAll(votes);
    }

    private void seedComments(List<User> users, List<Article> articles) {
        System.out.println("Seeding Comments...");
        List<Comment> comments = new ArrayList<>();

        comments.add(new Comment(null, "Great insights on Mars colonization!", users.get(1), articles.get(0), LocalDateTime.now().minusDays(9)));
        comments.add(new Comment(null, "I learned a lot about black holes, thanks!", users.get(2), articles.get(1), LocalDateTime.now().minusDays(4)));
        comments.add(new Comment(null, "Fascinating rocket tech!", users.get(0), articles.get(2), LocalDateTime.now().minusHours(10)));
        comments.add(new Comment(null, "This is a very thought-provoking article.", users.get(3), articles.get(0), LocalDateTime.now().minusDays(1)));
        comments.add(new Comment(null, "Any updates on the James Webb findings?", users.get(1), articles.get(3), LocalDateTime.now().minusHours(5)));

        commentRepository.saveAll(comments);
    }

    private List<Course> seedCourses() {
        System.out.println("Seeding Courses...");
        List<Course> courses = new ArrayList<>();

        courses.add(new Course(null, "Introduction to Astrophysics", "Learn the basics of astrophysics, from stars to galaxies.", Course.DifficultyLevel.BEGINNER, new ArrayList<>(), new ArrayList<>()));
        courses.add(new Course(null, "Rocket Science 101", "Understand the principles of rocket propulsion and spaceflight.", Course.DifficultyLevel.INTERMEDIATE, new ArrayList<>(), new ArrayList<>()));
        courses.add(new Course(null, "Planetary Geology", "Explore the geological features of planets in our solar system.", Course.DifficultyLevel.INTERMEDIATE, new ArrayList<>(), new ArrayList<>()));
        courses.add(new Course(null, "Advanced Space Mission Design", "Deep dive into designing complex space missions.", Course.DifficultyLevel.ADVANCED, new ArrayList<>(), new ArrayList<>()));

        return courseRepository.saveAll(courses);
    }

    private List<Module> seedModules(List<Course> courses) {
        System.out.println("Seeding Modules...");
        List<Module> modules = new ArrayList<>();

        // Modules for Course 1 (Astrophysics)
        modules.add(new Module(null, "Stars and Stellar Evolution", courses.get(0), new ArrayList<>(), 0));
        modules.add(new Module(null, "Galaxies and Cosmology", courses.get(0), new ArrayList<>(), 0));

        // Modules for Course 2 (Rocket Science)
        modules.add(new Module(null, "Propulsion Systems", courses.get(1), new ArrayList<>(), 0));
        modules.add(new Module(null, "Orbital Mechanics", courses.get(1), new ArrayList<>(), 0));

        // Modules for Course 3 (Planetary Geology)
        modules.add(new Module(null, "Terrestrial Planets", courses.get(2), new ArrayList<>(), 0));
        modules.add(new Module(null, "Gas Giants and Moons", courses.get(2), new ArrayList<>(), 0));

        // Modules for Course 4 (Mission Design)
        modules.add(new Module(null, "Mission Planning", courses.get(3), new ArrayList<>(), 0));
        modules.add(new Module(null, "Spacecraft Systems", courses.get(3), new ArrayList<>(), 0));

        return moduleRepository.saveAll(modules);
    }

    private List<Lesson> seedLessons(List<Module> modules) {
        System.out.println("Seeding Lessons...");
        List<Lesson> lessons = new ArrayList<>();
        String sampleContent = "This lesson covers fundamental concepts related to the topic. We will explore various aspects and examples.";

        // Lessons for Module 1 (Stars) - Belongs to Course 1
        lessons.add(new Lesson(null, "The Sun", sampleContent, "https://youtube.com/watch?v=sun", modules.get(0), null));
        lessons.add(new Lesson(null, "Lifecycle of a Star", sampleContent, "https://youtube.com/watch?v=lifecycle", modules.get(0), null));

        // Lessons for Module 2 (Galaxies) - Belongs to Course 1
        lessons.add(new Lesson(null, "The Milky Way", sampleContent, "https://youtube.com/watch?v=milkyway", modules.get(1), null));
        lessons.add(new Lesson(null, "Types of Galaxies", sampleContent, null, modules.get(1), null)); // No video

        // Lessons for Module 3 (Propulsion) - Belongs to Course 2
        lessons.add(new Lesson(null, "Chemical Rockets", sampleContent, "https://youtube.com/watch?v=chemrocket", modules.get(2), null));
        lessons.add(new Lesson(null, "Ion Thrusters", sampleContent, "https://youtube.com/watch?v=ion", modules.get(2), null));

        // Lessons for Module 4 (Orbital Mechanics) - Belongs to Course 2
        lessons.add(new Lesson(null, "Kepler's Laws", sampleContent, null, modules.get(3), null));

        return lessonRepository.saveAll(lessons);
    }

    private List<Quiz> seedQuizzes(List<Lesson> lessons) {
        System.out.println("Seeding Quizzes...");
        List<Quiz> quizzes = new ArrayList<>();

        // Quiz for Lesson 1 (The Sun)
        quizzes.add(new Quiz(null, "The Sun: Basic Facts", lessons.get(0), new ArrayList<>(), 50));
        // Quiz for Lesson 3 (The Milky Way)
        quizzes.add(new Quiz(null, "Our Galaxy Quiz", lessons.get(2), new ArrayList<>(), 75));
        // Quiz for Lesson 5 (Chemical Rockets)
        quizzes.add(new Quiz(null, "Rocket Fuels Quiz", lessons.get(4), new ArrayList<>(), 60));
        // Quiz for Lesson 7 (Kepler's Laws)
        quizzes.add(new Quiz(null, "Kepler's Laws Challenge", lessons.get(6), new ArrayList<>(), 100));


        return quizRepository.saveAll(quizzes);
    }

    private void seedQuizQuestions(List<Quiz> quizzes) {
        System.out.println("Seeding QuizQuestions...");
        List<QuizQuestion> questions = new ArrayList<>();

        // Questions for Quiz 1 (The Sun)
        QuizQuestion q1_1 = new QuizQuestion();
        q1_1.setQuiz(quizzes.get(0));
        q1_1.setQuestionText("What is the approximate surface temperature of the Sun?");
        q1_1.setOptions(Arrays.asList("1,000°C", "5,500°C", "15,000,000°C", "100,000°C"));
        q1_1.setCorrectOptionIndex(1);
        questions.add(q1_1);

        QuizQuestion q1_2 = new QuizQuestion();
        q1_2.setQuiz(quizzes.get(0));
        q1_2.setQuestionText("The Sun is primarily composed of which two elements?");
        q1_2.setOptions(Arrays.asList("Oxygen & Carbon", "Iron & Nickel", "Hydrogen & Helium", "Nitrogen & Oxygen"));
        q1_2.setCorrectOptionIndex(2);
        questions.add(q1_2);

        // Questions for Quiz 2 (Milky Way)
        QuizQuestion q2_1 = new QuizQuestion();
        q2_1.setQuiz(quizzes.get(1));
        q2_1.setQuestionText("What type of galaxy is the Milky Way?");
        q2_1.setOptions(Arrays.asList("Elliptical", "Irregular", "Spiral", "Lenticular"));
        q2_1.setCorrectOptionIndex(2);
        questions.add(q2_1);

        // Questions for Quiz 3 (Rocket Fuels)
        QuizQuestion q3_1 = new QuizQuestion();
        q3_1.setQuiz(quizzes.get(2));
        q3_1.setQuestionText("Which is a common liquid rocket fuel oxidizer?");
        q3_1.setOptions(Arrays.asList("Kerosene", "Liquid Oxygen (LOX)", "Methane", "Hydrazine"));
        q3_1.setCorrectOptionIndex(1);
        questions.add(q3_1);

        quizQuestionRepository.saveAll(questions);
    }

    private void seedQuizCompletions(List<User> users, List<Quiz> quizzes) {
        System.out.println("Seeding QuizCompletions...");
        List<QuizCompletion> completions = new ArrayList<>();

        // User 0 completes Quiz 0 with 100 score
        completions.add(new QuizCompletion(null, users.get(0), quizzes.get(0), 100, LocalDateTime.now().minusDays(1)));
        users.get(0).addExperience(quizzes.get(0).getExperienceReward());

        // User 1 completes Quiz 0 with 50 score
        completions.add(new QuizCompletion(null, users.get(1), quizzes.get(0), 50, LocalDateTime.now().minusHours(10)));
        users.get(1).addExperience(quizzes.get(0).getExperienceReward() / 2); // Half points for 50%

        // User 1 also completes Quiz 1 with 100 score
        completions.add(new QuizCompletion(null, users.get(1), quizzes.get(1), 100, LocalDateTime.now().minusHours(5)));
        users.get(1).addExperience(quizzes.get(1).getExperienceReward());

        // User 2 completes Quiz 2 with 75 score
        completions.add(new QuizCompletion(null, users.get(2), quizzes.get(2), 75, LocalDateTime.now().minusDays(2)));
        users.get(2).addExperience((int)(quizzes.get(2).getExperienceReward() * 0.75));

        quizCompletionRepository.saveAll(completions);
        userRepository.saveAll(users); // Save users to update experience points and level
    }

    private void seedCourseProgress(List<User> users, List<Course> courses, List<Lesson> allLessons) {
        System.out.println("Seeding CourseProgress...");
        List<CourseProgress> progresses = new ArrayList<>();

        // User 0 is partway through Course 0 (Astrophysics)
        Course course0 = courses.get(0);
        List<Lesson> course0Lessons = allLessons.stream().filter(l -> l.getModule().getCourse().equals(course0)).collect(Collectors.toList());
        if (!course0Lessons.isEmpty()) {
            CourseProgress cp1 = new CourseProgress();
            cp1.setUser(users.get(0));
            cp1.setCourse(course0);
            cp1.setCurrentLesson(course0Lessons.get(0)); // Current lesson is the first one
            cp1.getCompletedLessonIds().add(course0Lessons.get(0).getId()); // Mark first lesson as completed
            cp1.setLastAccessed(LocalDateTime.now().minusDays(3));
            cp1.updateCompletion(); // Manually trigger for seeding
            progresses.add(cp1);
        }


        // User 1 has completed Course 1 (Rocket Science)
        Course course1 = courses.get(1);
        List<Lesson> course1Lessons = allLessons.stream().filter(l -> l.getModule().getCourse().equals(course1)).collect(Collectors.toList());
        if (!course1Lessons.isEmpty()) {
            CourseProgress cp2 = new CourseProgress();
            cp2.setUser(users.get(1));
            cp2.setCourse(course1);
            cp2.setCurrentLesson(course1Lessons.get(course1Lessons.size() -1)); // Last lesson
            course1Lessons.forEach(l -> cp2.getCompletedLessonIds().add(l.getId())); // All lessons completed
            cp2.setLastAccessed(LocalDateTime.now().minusDays(1));
            cp2.updateCompletion(); // Manually trigger
            progresses.add(cp2);
        }


        // User 2 started Course 0 but completed no lessons yet
        if (!course0Lessons.isEmpty()) {
            CourseProgress cp3 = new CourseProgress();
            cp3.setUser(users.get(2));
            cp3.setCourse(course0);
            cp3.setCurrentLesson(course0Lessons.get(0)); // Started, on first lesson
            cp3.setLastAccessed(LocalDateTime.now().minusHours(5));
            cp3.updateCompletion(); // Manually trigger
            progresses.add(cp3);
        }

        // User 3 started Course 2 (Planetary Geology), completed one lesson
        Course course2 = courses.get(2);
        List<Lesson> course2Lessons = allLessons.stream().filter(l -> l.getModule().getCourse().equals(course2)).collect(Collectors.toList());
        if (course2Lessons.size() > 1) { // Ensure there are at least two lessons
            CourseProgress cp4 = new CourseProgress();
            cp4.setUser(users.get(3));
            cp4.setCourse(course2);
            cp4.setCurrentLesson(course2Lessons.get(1)); // On the second lesson
            cp4.getCompletedLessonIds().add(course2Lessons.get(0).getId()); // Completed the first lesson
            cp4.setLastAccessed(LocalDateTime.now().minusDays(2));
            cp4.updateCompletion();
            progresses.add(cp4);
        }


        courseProgressRepository.saveAll(progresses);
    }

    private void seedReadingHistory(List<User> users, List<Article> articles) {
        System.out.println("Seeding ReadingHistory...");
        List<ReadingHistory> histories = new ArrayList<>();

        // User 0 read Article 0 for 120 seconds
        ReadingHistory rh1 = new ReadingHistory(null, users.get(0), articles.get(0), false, 120, LocalDateTime.now().minusDays(5));
        rh1.updateIsRead();
        histories.add(rh1);

        // User 1 read Article 1 for 25 seconds (not marked as read)
        ReadingHistory rh2 = new ReadingHistory(null, users.get(1), articles.get(1), false, 25, LocalDateTime.now().minusDays(2));
        rh2.updateIsRead();
        histories.add(rh2);

        // User 0 also read Article 2 for 300 seconds
        ReadingHistory rh3 = new ReadingHistory(null, users.get(0), articles.get(2), false, 300, LocalDateTime.now().minusDays(1));
        rh3.updateIsRead();
        histories.add(rh3);

        // User 2 read Article 3 for 60 seconds
        ReadingHistory rh4 = new ReadingHistory(null, users.get(2), articles.get(3), false, 60, LocalDateTime.now().minusHours(6));
        rh4.updateIsRead();
        histories.add(rh4);

        readingHistoryRepository.saveAll(histories);
    }

    private void seedSpaceMissions(List<User> allUsers) {
        System.out.println("Seeding SpaceMissions...");
        List<User> verifiedUsers = allUsers.stream()
                .filter(u -> u.getVerificationStatus() == User.UserVerification.VERIFIED)
                .collect(Collectors.toList());

        if (verifiedUsers.isEmpty()) {
            System.out.println("No verified users to create space missions. Skipping.");
            return;
        }

        List<SpaceMission> missions = new ArrayList<>();
        missions.add(new SpaceMission(null, "Artemis I", "NASA", LocalDateTime.of(2022, 11, 16, 6, 47),
                "Uncrewed Moon-orbiting mission, the first flight of the Space Launch System rocket and the second flight of the Orion MPCV.",
                "https://www.nasa.gov/sites/default/files/thumbnails/image/art001m000000001_orig.jpg",
                "https://www.youtube.com/watch?v=21X5lGlDOfg",  SpaceMission.MissionStatus.COMPLETED, verifiedUsers.get(0 % verifiedUsers.size())));

        missions.add(new SpaceMission(null, "Mars Perseverance Rover", "NASA", LocalDateTime.of(2020, 7, 30, 11, 50),
                "Search for signs of ancient microbial life, collect rock and soil samples for possible return to Earth.",
                "https://mars.nasa.gov/layout/general_assets/images/mars_perseverance_banner.jpg",
                null, SpaceMission.MissionStatus.IN_PROGRESS, verifiedUsers.get(1 % verifiedUsers.size())));

        missions.add(new SpaceMission(null, "Starlink Group 6-1 Launch", "SpaceX", LocalDateTime.now().plusMonths(1),
                "Upcoming launch to deploy more Starlink satellites into low Earth orbit.",
                "https://cdn.spacex.com/layout/general/starlink.jpg",
                "https://www.spacex.com/launches/",  SpaceMission.MissionStatus.UPCOMING, verifiedUsers.get(0 % verifiedUsers.size())));

        missions.add(new SpaceMission(null, "Chandrayaan-3", "ISRO", LocalDateTime.of(2023, 7, 14, 9, 5),
                "India's third lunar exploration mission. Successful soft landing on the lunar south pole.",
                "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c4/Chandrayaan-3_Integrated_Module.jpg/800px-Chandrayaan-3_Integrated_Module.jpg",
                null,  SpaceMission.MissionStatus.COMPLETED, verifiedUsers.get(1 % verifiedUsers.size())));


        spaceMissionRepository.saveAll(missions);
    }
}