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
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setEmail("admin@example.com");
        admin.setBio("Administrator of the platform.");
        admin.setProfileImageUrl("https://i.pravatar.cc/150?u=admin");
        admin.setPhotoCoverUrl("https://picsum.photos/seed/admincover/800/200");
        admin.setRole(User.UserRole.ADMIN);
        admin.setVerificationStatus(User.UserVerification.VERIFIED);
        admin.setExperiencePoints(15000);
        admin.addExperience(0);
        users.add(admin); // User 0

        User verifiedUser1 = new User();
        verifiedUser1.setUsername("verified_astronaut");
        verifiedUser1.setPassword(passwordEncoder.encode("password123"));
        verifiedUser1.setEmail("astronaut@example.com");
        verifiedUser1.setBio("Verified user, loves space exploration.");
        verifiedUser1.setProfileImageUrl("https://i.pravatar.cc/150?u=astro");
        verifiedUser1.setPhotoCoverUrl("https://picsum.photos/seed/astrocover/800/200");
        verifiedUser1.setRole(User.UserRole.USER);
        verifiedUser1.setVerificationStatus(User.UserVerification.VERIFIED);
        verifiedUser1.setExperiencePoints(6000);
        verifiedUser1.addExperience(0);
        users.add(verifiedUser1); // User 1

        User regularUser1 = new User();
        regularUser1.setUsername("space_explorer");
        regularUser1.setPassword(passwordEncoder.encode("password123"));
        regularUser1.setEmail("explorer@example.com");
        regularUser1.setBio("Regular user, eager to learn.");
        regularUser1.setProfileImageUrl("https://i.pravatar.cc/150?u=explorer");
        regularUser1.setPhotoCoverUrl("https://picsum.photos/seed/explorercover/800/200");
        regularUser1.setRole(User.UserRole.USER);
        regularUser1.setVerificationStatus(User.UserVerification.UNVERIFIED);
        regularUser1.setExperiencePoints(500);
        regularUser1.addExperience(0);
        users.add(regularUser1); // User 2

        User pendingUser = new User();
        pendingUser.setUsername("pending_genius");
        pendingUser.setPassword(passwordEncoder.encode("password123"));
        pendingUser.setEmail("pending@example.com");
        pendingUser.setBio("Awaiting verification.");
        pendingUser.setProfileImageUrl("https://i.pravatar.cc/150?u=pending");
        pendingUser.setPhotoCoverUrl("https://picsum.photos/seed/pendingcover/800/200");
        pendingUser.setRole(User.UserRole.USER);
        pendingUser.setVerificationStatus(User.UserVerification.PENDING);
        pendingUser.setExperiencePoints(1200);
        pendingUser.addExperience(0);
        users.add(pendingUser); // User 3

        User verifiedUser2 = new User();
        verifiedUser2.setUsername("cosmic_voyager");
        verifiedUser2.setPassword(passwordEncoder.encode("securepass"));
        verifiedUser2.setEmail("voyager@example.com");
        verifiedUser2.setBio("Seasoned space enthusiast and content creator.");
        verifiedUser2.setProfileImageUrl("https://i.pravatar.cc/150?u=voyager");
        verifiedUser2.setPhotoCoverUrl("https://picsum.photos/seed/voyagercover/800/200");
        verifiedUser2.setRole(User.UserRole.USER);
        verifiedUser2.setVerificationStatus(User.UserVerification.VERIFIED);
        verifiedUser2.setExperiencePoints(8500);
        verifiedUser2.addExperience(0);
        users.add(verifiedUser2); // User 4

        User regularUser2 = new User();
        regularUser2.setUsername("star_gazer_99");
        regularUser2.setPassword(passwordEncoder.encode("newpass123"));
        regularUser2.setEmail("stargazer@example.com");
        regularUser2.setBio("Loves looking at the stars. New to the community.");
        regularUser2.setProfileImageUrl("https://i.pravatar.cc/150?u=stargazer");
        regularUser2.setPhotoCoverUrl("https://picsum.photos/seed/stargazercover/800/200");
        regularUser2.setRole(User.UserRole.USER);
        regularUser2.setVerificationStatus(User.UserVerification.UNVERIFIED);
        regularUser2.setExperiencePoints(150);
        regularUser2.addExperience(0);
        users.add(regularUser2); // User 5

        User moderatorUser = new User();
        moderatorUser.setUsername("mod_squad");
        moderatorUser.setPassword(passwordEncoder.encode("modPass!0"));
        moderatorUser.setEmail("moderator@example.com");
        moderatorUser.setBio("Keeping the forums clean and discussions productive.");
        moderatorUser.setProfileImageUrl("https://i.pravatar.cc/150?u=mod");
        moderatorUser.setPhotoCoverUrl("https://picsum.photos/seed/modcover/800/200");
        moderatorUser.setRole(User.UserRole.USER); // Corrected Role
        moderatorUser.setVerificationStatus(User.UserVerification.VERIFIED);
        moderatorUser.setExperiencePoints(12000);
        moderatorUser.addExperience(0);
        users.add(moderatorUser); // User 6

        User rejectedUser = new User();
        rejectedUser.setUsername("application_denied");
        rejectedUser.setPassword(passwordEncoder.encode("tryAgain"));
        rejectedUser.setEmail("rejected@example.com");
        rejectedUser.setBio("Verification application was rejected.");
        rejectedUser.setProfileImageUrl("https://i.pravatar.cc/150?u=rejected");
        rejectedUser.setPhotoCoverUrl("https://picsum.photos/seed/rejectedcover/800/200");
        rejectedUser.setRole(User.UserRole.USER);
        rejectedUser.setVerificationStatus(User.UserVerification.UNVERIFIED); // Corrected Status
        rejectedUser.setExperiencePoints(200);
        rejectedUser.addExperience(0);
        users.add(rejectedUser); // User 7

        User learnerUser = new User();
        learnerUser.setUsername("eager_learner_01");
        learnerUser.setPassword(passwordEncoder.encode("learnspace"));
        learnerUser.setEmail("learner01@example.com");
        learnerUser.setBio("Here to absorb all knowledge about space!");
        learnerUser.setProfileImageUrl("https://i.pravatar.cc/150?u=learner01");
        learnerUser.setPhotoCoverUrl("https://picsum.photos/seed/learner01cover/800/200");
        learnerUser.setRole(User.UserRole.USER);
        learnerUser.setVerificationStatus(User.UserVerification.UNVERIFIED);
        learnerUser.setExperiencePoints(750);
        learnerUser.addExperience(0);
        users.add(learnerUser); // User 8

        return userRepository.saveAll(users);
    }

    private List<TagName> seedTagNames() {
        System.out.println("Seeding TagNames...");
        List<TagName> tagNames = Arrays.asList(
                new TagName("Space Exploration"), //0
                new TagName("Astrophysics"),    //1
                new TagName("Technology"),      //2
                new TagName("Mars"),            //3
                new TagName("Rockets"),         //4
                new TagName("Java"),            //5 For Course/Article context
                new TagName("Black Holes"),     //6
                new TagName("Galaxies"),        //7
                new TagName("Satellites"),      //8
                new TagName("Space Telescopes"),//9
                new TagName("Moon")             //10
        );
        return tagNameRepository.saveAll(tagNames);
    }

    private List<Article> seedArticles(List<User> users) {
        System.out.println("Seeding Articles...");
        List<Article> articles = new ArrayList<>();
        String loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.";
        String longContent = loremIpsum + "\n\n" + loremIpsum + "\n\n" + loremIpsum + " Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

        // Original 4 Articles with picsum links
        articles.add(new Article(null, "The Future of Mars Colonization", "A look into upcoming missions and technologies for settling on Mars.", longContent,
                Arrays.asList("https://picsum.photos/seed/mars1/600/400", "https://picsum.photos/seed/mars2/600/400"),
                users.get(0), LocalDateTime.now().minusDays(10), new ArrayList<>(), new HashSet<>(), 0, 0L, new HashSet<>())); // Article 0
        articles.add(new Article(null, "Understanding Black Holes", "Exploring the mysteries of black holes and their impact on the universe.", longContent,
                Arrays.asList("https://picsum.photos/seed/blackhole1/600/400"),
                users.get(1), LocalDateTime.now().minusDays(5), new ArrayList<>(), new HashSet<>(), 0, 0L, new HashSet<>())); // Article 1
        articles.add(new Article(null, "Advancements in Rocket Propulsion", "New technologies that are making space travel faster and more efficient.", longContent,
                Arrays.asList("https://picsum.photos/seed/rocket1/600/400", "https://picsum.photos/seed/rocket2/600/400"),
                users.get(0), LocalDateTime.now().minusDays(2), new ArrayList<>(), new HashSet<>(), 0, 0L, new HashSet<>())); // Article 2
        articles.add(new Article(null, "The Search for Extraterrestrial Life", "Current efforts and methods in the quest to find life beyond Earth.", longContent,
                Arrays.asList("https://th.bing.com/th/id/OIP.oXb1UmRqtGIMOfMhPH1z6AHaE7?cb=iwc1&rs=1&pid=ImgDetMain"), // No images for this one as per original
                users.get(2), LocalDateTime.now().minusDays(1), new ArrayList<>(), new HashSet<>(), 0, 0L, new HashSet<>())); // Article 3

        // New Articles (5 additional)
        articles.add(new Article(null, "James Webb Space Telescope: A New Era of Astronomy", "Discover the groundbreaking capabilities and initial findings of the JWST.", longContent,
                Arrays.asList("https://th.bing.com/th/id/OIP.HLpEqrjU6jzS53j3cNAhYgHaE8?cb=iwc1&rs=1&pid=ImgDetMain", "https://webb.nasa.gov/content/webbLaunch/assets/images/deployment/steps/18-deploy-mirror-wing/deploy-mirror-wing-750px.jpg"),
                users.get(4), LocalDateTime.now().minusDays(15), new ArrayList<>(), new HashSet<>(), 0, 0L, new HashSet<>())); // Article 4
        articles.add(new Article(null, "The Artemis Program: Return to the Moon", "An overview of NASA's ambitious program to return humans to the Moon and establish a sustainable lunar presence.", longContent,
                Arrays.asList("https://solar-mems.com/wp-content/uploads/2022/09/MAF_20220830__KSC_Artemis1_epb_005medium.jpg", "https://www.nasa.gov/wp-content/uploads/2023/08/artemis-ii-patch-full-color-vector-logo.png"),
                users.get(1), LocalDateTime.now().minusDays(8), new ArrayList<>(), new HashSet<>(), 0, 0L, new HashSet<>())); // Article 5
        articles.add(new Article(null, "Starlink and the Future of Global Internet", "How SpaceX's Starlink constellation aims to provide high-speed internet access worldwide.", longContent,
                Arrays.asList("https://blogs.cornell.edu/info2040/files/2019/09/Screen-Shot-2019-09-20-at-9.58.36-PM.png", "https://picsum.photos/seed/starlink2/600/400"),
                users.get(6), LocalDateTime.now().minusDays(3), new ArrayList<>(), new HashSet<>(), 0, 0L, new HashSet<>())); // Article 6
        articles.add(new Article(null, "The Role of AI in Space Exploration", "Exploring how artificial intelligence is revolutionizing space missions, from navigation to data analysis.", longContent,
                Arrays.asList("https://lifeboat.com/blog.images/the-role-of-ai-robotics-in-space-exploration.jpg"), // NASA AI concept image
                users.get(0), LocalDateTime.now().minusDays(20), new ArrayList<>(), new HashSet<>(), 0, 0L, new HashSet<>())); // Article 7
        articles.add(new Article(null, "Asteroid Mining: The Next Frontier?", "A look at the potential and challenges of mining resources from asteroids.", longContent,
                Arrays.asList("https://picsum.photos/seed/asteroidmine/600/400"), // Placeholder for asteroid mining
                users.get(4), LocalDateTime.now().minusDays(6), new ArrayList<>(), new HashSet<>(), 0, 0L, new HashSet<>())); // Article 8

        return articleRepository.saveAll(articles);
    }

    private void seedArticleTags(List<Article> articles, List<TagName> tagNames) {
        System.out.println("Seeding ArticleTags...");
        List<ArticleTag> articleTags = new ArrayList<>();

        // Article 0: Mars Colonization -> Tags: Space Exploration, Mars, Technology
        articleTags.add(new ArticleTag(articles.get(0), tagNames.get(0)));
        articleTags.add(new ArticleTag(articles.get(0), tagNames.get(3)));
        articleTags.add(new ArticleTag(articles.get(0), tagNames.get(2)));

        // Article 1: Black Holes -> Tags: Astrophysics, Black Holes
        articleTags.add(new ArticleTag(articles.get(1), tagNames.get(1)));
        articleTags.add(new ArticleTag(articles.get(1), tagNames.get(6)));

        // Article 2: Rocket Propulsion -> Tags: Technology, Rockets, Space Exploration
        articleTags.add(new ArticleTag(articles.get(2), tagNames.get(2)));
        articleTags.add(new ArticleTag(articles.get(2), tagNames.get(4)));
        articleTags.add(new ArticleTag(articles.get(2), tagNames.get(0)));

        // Article 3: Extraterrestrial Life -> Tags: Space Exploration, Astrophysics
        articleTags.add(new ArticleTag(articles.get(3), tagNames.get(0)));
        articleTags.add(new ArticleTag(articles.get(3), tagNames.get(1)));

        // Article 4: JWST -> Tags: Space Telescopes, Astrophysics, Technology, Galaxies
        articleTags.add(new ArticleTag(articles.get(4), tagNames.get(9)));
        articleTags.add(new ArticleTag(articles.get(4), tagNames.get(1)));
        articleTags.add(new ArticleTag(articles.get(4), tagNames.get(2)));
        articleTags.add(new ArticleTag(articles.get(4), tagNames.get(7)));

        // Article 5: Artemis Program -> Tags: Space Exploration, Moon, Rockets
        articleTags.add(new ArticleTag(articles.get(5), tagNames.get(0)));
        articleTags.add(new ArticleTag(articles.get(5), tagNames.get(10)));
        articleTags.add(new ArticleTag(articles.get(5), tagNames.get(4)));

        // Article 6: Starlink -> Tags: Satellites, Technology
        articleTags.add(new ArticleTag(articles.get(6), tagNames.get(8)));
        articleTags.add(new ArticleTag(articles.get(6), tagNames.get(2)));

        // Article 7: AI in Space -> Tags: Technology, Space Exploration
        articleTags.add(new ArticleTag(articles.get(7), tagNames.get(2)));
        articleTags.add(new ArticleTag(articles.get(7), tagNames.get(0)));

        // Article 8: Asteroid Mining -> Tags: Space Exploration, Technology
        articleTags.add(new ArticleTag(articles.get(8), tagNames.get(0)));
        articleTags.add(new ArticleTag(articles.get(8), tagNames.get(2)));

        articleTagRepository.saveAll(articleTags);
    }

    private void seedArticleVotes(List<User> users, List<Article> articles) {
        System.out.println("Seeding ArticleVotes...");
        List<ArticleVote> votes = new ArrayList<>();

        votes.add(new ArticleVote(null, 1, users.get(0), articles.get(0)));
        votes.add(new ArticleVote(null, 1, users.get(1), articles.get(0)));
        votes.add(new ArticleVote(null, -1, users.get(2), articles.get(0)));
        votes.add(new ArticleVote(null, 1, users.get(0), articles.get(1)));
        votes.add(new ArticleVote(null, 1, users.get(4), articles.get(1)));
        votes.add(new ArticleVote(null, 1, users.get(0), articles.get(2)));
        votes.add(new ArticleVote(null, 1, users.get(1), articles.get(2)));
        votes.add(new ArticleVote(null, 1, users.get(3), articles.get(2)));
        votes.add(new ArticleVote(null, -1, users.get(0), articles.get(3)));
        votes.add(new ArticleVote(null, 1, users.get(5), articles.get(3)));
        votes.add(new ArticleVote(null, 1, users.get(0), articles.get(4)));
        votes.add(new ArticleVote(null, 1, users.get(1), articles.get(4)));
        votes.add(new ArticleVote(null, 1, users.get(4), articles.get(4)));
        votes.add(new ArticleVote(null, 1, users.get(6), articles.get(4)));
        votes.add(new ArticleVote(null, 1, users.get(1), articles.get(5)));
        votes.add(new ArticleVote(null, 1, users.get(4), articles.get(5)));
        votes.add(new ArticleVote(null, -1, users.get(2), articles.get(5)));
        votes.add(new ArticleVote(null, 1, users.get(6), articles.get(6)));
        votes.add(new ArticleVote(null, 1, users.get(8), articles.get(6)));
        votes.add(new ArticleVote(null, 1, users.get(0), articles.get(7)));
        votes.add(new ArticleVote(null, 1, users.get(3), articles.get(7)));
        votes.add(new ArticleVote(null, 1, users.get(5), articles.get(7)));
        votes.add(new ArticleVote(null, 1, users.get(4), articles.get(8)));
        votes.add(new ArticleVote(null, -1, users.get(7), articles.get(8)));

        articleVoteRepository.saveAll(votes);
    }

    private void seedComments(List<User> users, List<Article> articles) {
        System.out.println("Seeding Comments...");
        List<Comment> comments = new ArrayList<>();

        comments.add(new Comment(null, "Great insights on Mars colonization!", users.get(1), articles.get(0), LocalDateTime.now().minusDays(9)));
        comments.add(new Comment(null, "I learned a lot about black holes, thanks!", users.get(2), articles.get(1), LocalDateTime.now().minusDays(4)));
        comments.add(new Comment(null, "Fascinating rocket tech!", users.get(0), articles.get(2), LocalDateTime.now().minusHours(10)));
        comments.add(new Comment(null, "This is a very thought-provoking article.", users.get(3), articles.get(0), LocalDateTime.now().minusDays(1)));
        comments.add(new Comment(null, "Any updates on the James Webb findings?", users.get(1), articles.get(4), LocalDateTime.now().minusHours(5)));
        comments.add(new Comment(null, "The Artemis program is so exciting!", users.get(4), articles.get(5), LocalDateTime.now().minusDays(2)));
        comments.add(new Comment(null, "Can't wait for Starlink to be available in my area.", users.get(8), articles.get(6), LocalDateTime.now().minusHours(12)));
        comments.add(new Comment(null, "AI is truly the future. Great article!", users.get(5), articles.get(7), LocalDateTime.now().minusDays(7)));
        comments.add(new Comment(null, "Asteroid mining seems like science fiction, but it's getting closer.", users.get(0), articles.get(8), LocalDateTime.now().minusHours(20)));
        comments.add(new Comment(null, "What are the ethical considerations for Mars colonization?", users.get(6), articles.get(0), LocalDateTime.now().minusDays(8)));

        commentRepository.saveAll(comments);
    }

    private List<Course> seedCourses() {
        System.out.println("Seeding Courses...");
        List<Course> courses = new ArrayList<>();

        // Original 4 Courses with picsum links
        courses.add(new Course(null, "Introduction to Astrophysics", "Learn the basics of astrophysics, from stars to galaxies.", Course.DifficultyLevel.BEGINNER, new ArrayList<>(), new ArrayList<>(), "https://picsum.photos/seed/course1/600/300"));
        courses.add(new Course(null, "Rocket Science 101", "Understand the principles of rocket propulsion and spaceflight.", Course.DifficultyLevel.INTERMEDIATE, new ArrayList<>(), new ArrayList<>(), "https://picsum.photos/seed/course2/600/300"));
        courses.add(new Course(null, "Planetary Geology", "Explore the geological features of planets in our solar system.", Course.DifficultyLevel.INTERMEDIATE, new ArrayList<>(), new ArrayList<>(), "https://picsum.photos/seed/course3/600/300"));
        courses.add(new Course(null, "Advanced Space Mission Design", "Deep dive into designing complex space missions.", Course.DifficultyLevel.ADVANCED, new ArrayList<>(), new ArrayList<>(), "https://picsum.photos/seed/course4/600/300"));

        // New Courses (5 additional) with picsum links for consistency
        courses.add(new Course(null, "Cosmology: The Study of the Universe", "Delve into the origin, evolution, and ultimate fate of the universe.", Course.DifficultyLevel.ADVANCED, new ArrayList<>(), new ArrayList<>(), "https://picsum.photos/seed/cosmology/600/300"));
        courses.add(new Course(null, "Introduction to Astrobiology", "Search for life beyond Earth: methods, possibilities, and implications.", Course.DifficultyLevel.BEGINNER, new ArrayList<>(), new ArrayList<>(), "https://picsum.photos/seed/astrobiology/600/300"));
        courses.add(new Course(null, "Satellite Systems Engineering", "Learn about the design, launch, and operation of artificial satellites.", Course.DifficultyLevel.INTERMEDIATE, new ArrayList<>(), new ArrayList<>(), "https://picsum.photos/seed/satellite_eng/600/300"));
        courses.add(new Course(null, "Space Law and Policy", "Understand the legal frameworks governing space activities and exploration.", Course.DifficultyLevel.ADVANCED, new ArrayList<>(), new ArrayList<>(), "https://picsum.photos/seed/space_law/600/300"));
        courses.add(new Course(null, "Observational Astronomy Techniques", "Practical guide to using telescopes and analyzing astronomical data.", Course.DifficultyLevel.INTERMEDIATE, new ArrayList<>(), new ArrayList<>(), "https://picsum.photos/seed/obs_astro/600/300"));

        return courseRepository.saveAll(courses);
    }

    private List<Module> seedModules(List<Course> courses) {
        System.out.println("Seeding Modules...");
        List<Module> modules = new ArrayList<>();

        modules.add(new Module(null, "Stars and Stellar Evolution", courses.get(0), new ArrayList<>(), 0));
        modules.add(new Module(null, "Galaxies and Cosmology Basics", courses.get(0), new ArrayList<>(), 0));
        modules.add(new Module(null, "Propulsion Systems", courses.get(1), new ArrayList<>(), 0));
        modules.add(new Module(null, "Orbital Mechanics", courses.get(1), new ArrayList<>(), 0));
        modules.add(new Module(null, "Terrestrial Planets", courses.get(2), new ArrayList<>(), 0));
        modules.add(new Module(null, "Gas Giants and Moons", courses.get(2), new ArrayList<>(), 0));
        modules.add(new Module(null, "Mission Planning & Objectives", courses.get(3), new ArrayList<>(), 0));
        modules.add(new Module(null, "Spacecraft Systems Engineering", courses.get(3), new ArrayList<>(), 0));
        modules.add(new Module(null, "The Big Bang Theory", courses.get(4), new ArrayList<>(), 0));
        modules.add(new Module(null, "Dark Matter and Dark Energy", courses.get(4), new ArrayList<>(), 0));
        modules.add(new Module(null, "The Drake Equation", courses.get(5), new ArrayList<>(), 0));
        modules.add(new Module(null, "Extremophiles on Earth", courses.get(5), new ArrayList<>(), 0));
        modules.add(new Module(null, "Satellite Orbits and Trajectories", courses.get(6), new ArrayList<>(), 0));
        modules.add(new Module(null, "Communication Subsystems", courses.get(6), new ArrayList<>(), 0));
        modules.add(new Module(null, "International Space Treaties", courses.get(7), new ArrayList<>(), 0));
        modules.add(new Module(null, "Telescope Optics", courses.get(8), new ArrayList<>(), 0));
        modules.add(new Module(null, "Data Reduction Techniques", courses.get(8), new ArrayList<>(), 0));

        return moduleRepository.saveAll(modules);
    }


    private List<Lesson> seedLessons(List<Module> modules) {
        System.out.println("Seeding Lessons...");
        List<Lesson> lessons = new ArrayList<>();
        String sampleContent = "This lesson covers fundamental concepts related to the topic. We will explore various aspects and examples. This content is illustrative and should be expanded for a real course.";

        lessons.add(new Lesson(null, "The Sun: Our Star", sampleContent, "https://www.youtube.com/watch?v=2HoTK_Gqi2Q", modules.get(0), null));
        lessons.add(new Lesson(null, "Lifecycle of a Star", sampleContent, "https://www.youtube.com/watch?v=PM9CQDlQI0A", modules.get(0), null));
        lessons.add(new Lesson(null, "The Milky Way Galaxy", sampleContent, "https://www.youtube.com/watch?v=tj_QPnO8vpQ", modules.get(1), null));
        lessons.add(new Lesson(null, "Types of Galaxies", sampleContent, "https://www.youtube.com/watch?v=rKexqK3UKdE", modules.get(1), null));
        lessons.add(new Lesson(null, "Chemical Rockets", sampleContent, "https://www.youtube.com/watch?v=S7nZPh8jC7E", modules.get(2), null));
        lessons.add(new Lesson(null, "Ion Thrusters & Future Propulsion", sampleContent, "https://www.youtube.com/watch?v=6o77gq2Ak0I", modules.get(2), null));
        lessons.add(new Lesson(null, "Kepler's Laws of Planetary Motion", sampleContent, "https://www.youtube.com/watch?v=Dvoe8Ib5D1o", modules.get(3), null));
        lessons.add(new Lesson(null, "Understanding Orbits", sampleContent, "https://www.youtube.com/watch?v=N5G_K81Zrf0", modules.get(3), null));
        lessons.add(new Lesson(null, "Evidence for the Big Bang", sampleContent, "https://www.youtube.com/watch?v=1bK8p2XpL2M", modules.get(8), null));
        lessons.add(new Lesson(null, "Cosmic Microwave Background", sampleContent, null, modules.get(8), null));
        lessons.add(new Lesson(null, "Variables of the Drake Equation", sampleContent, "https://www.youtube.com/watch?v=80Rzpj2hCSo", modules.get(10), null));
        lessons.add(new Lesson(null, "LEO, MEO, GEO Orbits", sampleContent, "https://www.youtube.com/watch?v=JTwXAg35rDA", modules.get(12), null));
        lessons.add(new Lesson(null, "Reflecting vs Refracting Telescopes", sampleContent, null, modules.get(15), null));

        return lessonRepository.saveAll(lessons);
    }




    private List<Quiz> seedQuizzes(List<Lesson> lessons) {
        System.out.println("Seeding Quizzes...");
        List<Quiz> quizzes = new ArrayList<>();
        if (lessons.size() < 13) { // Ensure enough lessons exist for defined quizzes
            System.out.println("Warning: Not enough lessons to seed all planned quizzes. Actual lessons: " + lessons.size());
            // Adjust quiz creation based on available lessons or return
        }

        if (lessons.size() > 0) quizzes.add(new Quiz(null, "The Sun: Basic Facts", lessons.get(0), new ArrayList<>(), 50));
        if (lessons.size() > 2) quizzes.add(new Quiz(null, "Our Galaxy Quiz", lessons.get(2), new ArrayList<>(), 75));
        if (lessons.size() > 4) quizzes.add(new Quiz(null, "Rocket Fuels Quiz", lessons.get(4), new ArrayList<>(), 60));
        if (lessons.size() > 6) quizzes.add(new Quiz(null, "Kepler's Laws Challenge", lessons.get(6), new ArrayList<>(), 100));
        if (lessons.size() > 8) quizzes.add(new Quiz(null, "Big Bang Evidence", lessons.get(8), new ArrayList<>(), 80));
        if (lessons.size() > 10) quizzes.add(new Quiz(null, "Drake Equation Variables", lessons.get(10), new ArrayList<>(), 70));
        if (lessons.size() > 11) quizzes.add(new Quiz(null, "Satellite Orbits", lessons.get(11), new ArrayList<>(), 65));

        return quizRepository.saveAll(quizzes);
    }

    private void seedQuizQuestions(List<Quiz> quizzes) {
        System.out.println("Seeding QuizQuestions...");
        List<QuizQuestion> questions = new ArrayList<>();
        if (quizzes.isEmpty()) return;

        // Questions for Quiz 0 (The Sun)
        if (quizzes.size() > 0) {
            QuizQuestion q0_1 = new QuizQuestion(); q0_1.setQuiz(quizzes.get(0)); q0_1.setQuestionText("What is the approximate surface temperature of the Sun?"); q0_1.setOptions(Arrays.asList("1,000°C", "5,500°C", "15,000,000°C", "100,000°C")); q0_1.setCorrectOptionIndex(1); questions.add(q0_1);
            QuizQuestion q0_2 = new QuizQuestion(); q0_2.setQuiz(quizzes.get(0)); q0_2.setQuestionText("The Sun is primarily composed of which two elements?"); q0_2.setOptions(Arrays.asList("Oxygen & Carbon", "Iron & Nickel", "Hydrogen & Helium", "Nitrogen & Oxygen")); q0_2.setCorrectOptionIndex(2); questions.add(q0_2);
        }
        // Questions for Quiz 1 (Milky Way)
        if (quizzes.size() > 1) {
            QuizQuestion q1_1 = new QuizQuestion(); q1_1.setQuiz(quizzes.get(1)); q1_1.setQuestionText("What type of galaxy is the Milky Way?"); q1_1.setOptions(Arrays.asList("Elliptical", "Irregular", "Spiral", "Lenticular")); q1_1.setCorrectOptionIndex(2); questions.add(q1_1);
            QuizQuestion q1_2 = new QuizQuestion(); q1_2.setQuiz(quizzes.get(1)); q1_2.setQuestionText("Where is the Sun located within the Milky Way?"); q1_2.setOptions(Arrays.asList("In the galactic bulge", "In a globular cluster", "In one of the spiral arms", "In the halo")); q1_2.setCorrectOptionIndex(2); questions.add(q1_2);
        }
        // Questions for Quiz 2 (Rocket Fuels)
        if (quizzes.size() > 2) {
            QuizQuestion q2_1 = new QuizQuestion(); q2_1.setQuiz(quizzes.get(2)); q2_1.setQuestionText("Which is a common liquid rocket fuel oxidizer?"); q2_1.setOptions(Arrays.asList("Kerosene", "Liquid Oxygen (LOX)", "Methane", "Hydrazine")); q2_1.setCorrectOptionIndex(1); questions.add(q2_1);
        }
        // Questions for Quiz 3 (Kepler's Laws)
        if (quizzes.size() > 3) {
            QuizQuestion q3_1 = new QuizQuestion(); q3_1.setQuiz(quizzes.get(3)); q3_1.setQuestionText("Kepler's First Law states that planets orbit the Sun in what shape?"); q3_1.setOptions(Arrays.asList("Circles", "Ellipses", "Parabolas", "Hyperbolas")); q3_1.setCorrectOptionIndex(1); questions.add(q3_1);
        }
        // Questions for Quiz 4 (Big Bang)
        if (quizzes.size() > 4) {
            QuizQuestion q4_1 = new QuizQuestion(); q4_1.setQuiz(quizzes.get(4)); q4_1.setQuestionText("What is the Cosmic Microwave Background (CMB)?"); q4_1.setOptions(Arrays.asList("Light from the first stars", "Radiation from black holes", "Relic radiation from the Big Bang", "Dust clouds in our galaxy")); q4_1.setCorrectOptionIndex(2); questions.add(q4_1);
        }
        // Questions for Quiz 5 (Drake Equation)
        if (quizzes.size() > 5) {
            QuizQuestion q5_1 = new QuizQuestion(); q5_1.setQuiz(quizzes.get(5)); q5_1.setQuestionText("What does 'N' represent in the Drake Equation?"); q5_1.setOptions(Arrays.asList("Number of stars in our galaxy", "Number of habitable planets", "Number of civilizations we can communicate with", "Number of new technologies developed")); q5_1.setCorrectOptionIndex(2); questions.add(q5_1);
        }
        // Questions for Quiz 6 (Satellite Orbits)
        if (quizzes.size() > 6) {
            QuizQuestion q6_1 = new QuizQuestion(); q6_1.setQuiz(quizzes.get(6)); q6_1.setQuestionText("Which orbit is ideal for geostationary communication satellites?"); q6_1.setOptions(Arrays.asList("LEO (Low Earth Orbit)", "MEO (Medium Earth Orbit)", "GEO (Geostationary Earth Orbit)", "Polar Orbit")); q6_1.setCorrectOptionIndex(2); questions.add(q6_1);
        }
        quizQuestionRepository.saveAll(questions);
    }

    private void seedQuizCompletions(List<User> users, List<Quiz> quizzes) {
        System.out.println("Seeding QuizCompletions...");
        List<QuizCompletion> completions = new ArrayList<>();
        if (quizzes.isEmpty()) return;

        if (quizzes.size() > 0) {
            completions.add(new QuizCompletion(null, users.get(0), quizzes.get(0), 100, quizzes.get(0).getExperienceReward(), LocalDateTime.now().minusDays(1))); users.get(0).addExperience(quizzes.get(0).getExperienceReward());
            completions.add(new QuizCompletion(null, users.get(1), quizzes.get(0), 50, quizzes.get(0).getExperienceReward() / 2, LocalDateTime.now().minusHours(10))); users.get(1).addExperience(quizzes.get(0).getExperienceReward() / 2);
            completions.add(new QuizCompletion(null, users.get(4), quizzes.get(0), 100, quizzes.get(0).getExperienceReward(), LocalDateTime.now().minusDays(3))); users.get(4).addExperience(quizzes.get(0).getExperienceReward());
        }
        if (quizzes.size() > 1) {
            completions.add(new QuizCompletion(null, users.get(1), quizzes.get(1), 100, quizzes.get(1).getExperienceReward(), LocalDateTime.now().minusHours(5))); users.get(1).addExperience(quizzes.get(1).getExperienceReward());
            completions.add(new QuizCompletion(null, users.get(5), quizzes.get(1), 80, (int)(quizzes.get(1).getExperienceReward() * 0.80), LocalDateTime.now().minusHours(15))); users.get(5).addExperience((int)(quizzes.get(1).getExperienceReward() * 0.80));
        }
        if (quizzes.size() > 2) {
            completions.add(new QuizCompletion(null, users.get(2), quizzes.get(2), 75, (int)(quizzes.get(2).getExperienceReward() * 0.75), LocalDateTime.now().minusDays(2))); users.get(2).addExperience((int)(quizzes.get(2).getExperienceReward() * 0.75));
            completions.add(new QuizCompletion(null, users.get(8), quizzes.get(2), 90, (int)(quizzes.get(2).getExperienceReward() * 0.90), LocalDateTime.now().minusDays(1))); users.get(8).addExperience((int)(quizzes.get(2).getExperienceReward() * 0.90));
        }
        if (quizzes.size() > 3) {
            completions.add(new QuizCompletion(null, users.get(6), quizzes.get(3), 100, quizzes.get(3).getExperienceReward(), LocalDateTime.now().minusHours(8))); users.get(6).addExperience(quizzes.get(3).getExperienceReward());
        }
        if (quizzes.size() > 4) {
            completions.add(new QuizCompletion(null, users.get(0), quizzes.get(4), 90, (int)(quizzes.get(4).getExperienceReward() * 0.9), LocalDateTime.now().minusDays(4))); users.get(0).addExperience((int)(quizzes.get(4).getExperienceReward() * 0.9));
        }

        quizCompletionRepository.saveAll(completions);
        userRepository.saveAll(users);
    }

    private void seedCourseProgress(List<User> users, List<Course> courses, List<Lesson> allLessons) {
        System.out.println("Seeding CourseProgress...");
        List<CourseProgress> progresses = new ArrayList<>();

        Course course0 = courses.get(0); List<Lesson> course0Lessons = allLessons.stream().filter(l -> l.getModule().getCourse().equals(course0)).collect(Collectors.toList());
        if (course0Lessons.size() > 1) {
            CourseProgress cp1 = CourseProgress.builder()
                    .user(users.get(0))
                    .course(course0)
                    .currentLesson(course0Lessons.get(1))
                    .completedLessonIds(new HashSet<>(Arrays.asList(course0Lessons.get(0).getId())))
                    .completed(false)
                    .completionPercentage(33.33)
                    .lastAccessed(LocalDateTime.now())
                    .build();
            progresses.add(cp1);
        }
        if (!course0Lessons.isEmpty()) {
            CourseProgress cp3 = CourseProgress.builder()
                    .user(users.get(2))
                    .course(course0)
                    .currentLesson(course0Lessons.get(0))
                    .completedLessonIds(new HashSet<>())
                    .completed(false)
                    .completionPercentage(0.0)
                    .lastAccessed(LocalDateTime.now())
                    .build();
            progresses.add(cp3);
        }

        Course course1 = courses.get(1); List<Lesson> course1Lessons = allLessons.stream().filter(l -> l.getModule().getCourse().equals(course1)).collect(Collectors.toList());
        if (!course1Lessons.isEmpty()) {
            CourseProgress cp2 = CourseProgress.builder()
                    .user(users.get(1))
                    .course(course1)
                    .currentLesson(course1Lessons.get(course1Lessons.size() - 1))
                    .completedLessonIds(course1Lessons.stream().map(Lesson::getId).collect(Collectors.toSet()))
                    .completed(true)
                    .completionPercentage(100.0)
                    .lastAccessed(LocalDateTime.now())
                    .build();
            progresses.add(cp2);
        }

        Course course2 = courses.get(2); List<Lesson> course2Lessons = allLessons.stream().filter(l -> l.getModule().getCourse().equals(course2)).collect(Collectors.toList());
        if (course2Lessons.size() > 1) {
            CourseProgress cp4 = CourseProgress.builder()
                    .user(users.get(3))
                    .course(course2)
                    .currentLesson(course2Lessons.get(1))
                    .completedLessonIds(new HashSet<>(Arrays.asList(course2Lessons.get(0).getId())))
                    .completed(false)
                    .completionPercentage(33.33)
                    .lastAccessed(LocalDateTime.now())
                    .build();
            progresses.add(cp4);
        }

        if (courses.size() > 4 && users.size() > 4) {
            Course course4 = courses.get(4); List<Lesson> course4Lessons = allLessons.stream().filter(l -> l.getModule().getCourse().equals(course4)).collect(Collectors.toList());
            if (!course4Lessons.isEmpty()) {
                CourseProgress cp5 = CourseProgress.builder()
                        .user(users.get(4))
                        .course(course4)
                        .currentLesson(course4Lessons.get(0))
                        .completedLessonIds(new HashSet<>(Arrays.asList(course4Lessons.get(0).getId())))
                        .completed(false)
                        .completionPercentage(33.33)
                        .lastAccessed(LocalDateTime.now())
                        .build();
                progresses.add(cp5);
            }
        }
        if (courses.size() > 5 && users.size() > 5) {
            Course course5 = courses.get(5); List<Lesson> course5Lessons = allLessons.stream().filter(l -> l.getModule().getCourse().equals(course5)).collect(Collectors.toList());
            if (!course5Lessons.isEmpty()) {
                CourseProgress cp6 = CourseProgress.builder()
                        .user(users.get(5))
                        .course(course5)
                        .currentLesson(course5Lessons.get(course5Lessons.size() - 1))
                        .completedLessonIds(course5Lessons.stream().map(Lesson::getId).collect(Collectors.toSet()))
                        .completed(true)
                        .completionPercentage(100.0)
                        .lastAccessed(LocalDateTime.now())
                        .build();
                progresses.add(cp6);
            }
        }
        if (courses.size() > 6 && users.size() > 8) {
            Course course6 = courses.get(6); List<Lesson> course6Lessons = allLessons.stream().filter(l -> l.getModule().getCourse().equals(course6)).collect(Collectors.toList());
            if (course6Lessons.size() > 1) {
                CourseProgress cp7 = CourseProgress.builder()
                        .user(users.get(8))
                        .course(course6)
                        .currentLesson(course6Lessons.get(1))
                        .completedLessonIds(new HashSet<>(Arrays.asList(course6Lessons.get(0).getId())))
                        .completed(false)
                        .completionPercentage(33.33)
                        .lastAccessed(LocalDateTime.now())
                        .build();
                progresses.add(cp7);
            }
        }

        courseProgressRepository.saveAll(progresses);
    }

    private void seedReadingHistory(List<User> users, List<Article> articles) {
        System.out.println("Seeding ReadingHistory...");
        List<ReadingHistory> histories = new ArrayList<>();

        ReadingHistory rh1 = new ReadingHistory(null, users.get(0), articles.get(0), false, 120, LocalDateTime.now().minusDays(5)); rh1.updateIsRead(); histories.add(rh1);
        ReadingHistory rh2 = new ReadingHistory(null, users.get(1), articles.get(1), false, 25, LocalDateTime.now().minusDays(2)); rh2.updateIsRead(); histories.add(rh2);
        ReadingHistory rh3 = new ReadingHistory(null, users.get(0), articles.get(2), false, 300, LocalDateTime.now().minusDays(1)); rh3.updateIsRead(); histories.add(rh3);
        ReadingHistory rh4 = new ReadingHistory(null, users.get(2), articles.get(3), false, 60, LocalDateTime.now().minusHours(6)); rh4.updateIsRead(); histories.add(rh4);
        if (articles.size() > 4 && users.size() > 4) { ReadingHistory rh5 = new ReadingHistory(null, users.get(4), articles.get(4), false, 180, LocalDateTime.now().minusDays(10)); rh5.updateIsRead(); histories.add(rh5); }
        if (articles.size() > 5 && users.size() > 5) { ReadingHistory rh6 = new ReadingHistory(null, users.get(5), articles.get(5), false, 90, LocalDateTime.now().minusDays(4)); rh6.updateIsRead(); histories.add(rh6); }
        if (articles.size() > 6 && users.size() > 8) { ReadingHistory rh7 = new ReadingHistory(null, users.get(8), articles.get(6), false, 45, LocalDateTime.now().minusHours(20)); rh7.updateIsRead(); histories.add(rh7); }
        if (articles.size() > 7 && users.size() > 0) { ReadingHistory rh8 = new ReadingHistory(null, users.get(0), articles.get(7), false, 250, LocalDateTime.now().minusDays(15)); rh8.updateIsRead(); histories.add(rh8); }
        if (articles.size() > 8 && users.size() > 1) { ReadingHistory rh9 = new ReadingHistory(null, users.get(1), articles.get(8), false, 10, LocalDateTime.now().minusHours(2)); rh9.updateIsRead(); histories.add(rh9); }

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
        // Original 4 Missions with official/stable links
        missions.add(new SpaceMission(null, "Artemis I", "NASA", LocalDateTime.of(2022, 11, 16, 6, 47),
                "Uncrewed Moon-orbiting mission, the first flight of the Space Launch System rocket and the second flight of the Orion MPCV.",
                "https://th.bing.com/th/id/R.34b5555b4b6985cf7d7c22d062087218?rik=ul470SGlVBLjtA&riu=http%3a%2f%2feng.auburn.edu%2fimages%2fnews%2fartemis.jpg&ehk=4ci2T0TMahmbZwZcgUkQ4cJEY14lGonoJ5AmdsfgwHY%3d&risl=&pid=ImgRaw&r=0",
                "https://www.youtube.com/watch?v=21X5lGlDOfg",  SpaceMission.MissionStatus.COMPLETED,  verifiedUsers.get(0 % verifiedUsers.size())));
        missions.add(new SpaceMission(null, "Mars Perseverance Rover", "NASA", LocalDateTime.of(2020, 7, 30, 11, 50),
                "Search for signs of ancient microbial life, collect rock and soil samples for possible return to Earth.",
                "https://th.bing.com/th/id/R.33eacd53caf9f320085318a6bf5c1887?rik=tbqz%2bLyWgvNLQw&pid=ImgRaw&r=0",
                null, SpaceMission.MissionStatus.IN_PROGRESS,  verifiedUsers.get(1 % verifiedUsers.size())));
        missions.add(new SpaceMission(null, "Starlink Group 6-1 Launch", "SpaceX", LocalDateTime.now().plusMonths(1).withHour(10).withMinute(30),
                "Upcoming launch to deploy more Starlink satellites into low Earth orbit.",
                "https://i.ytimg.com/vi/YdYnoyeV74o/hqdefault.jpg",
                "https://www.spacex.com/launches/",  SpaceMission.MissionStatus.UPCOMING, verifiedUsers.get(0 % verifiedUsers.size())));
        missions.add(new SpaceMission(null, "Chandrayaan-3", "ISRO", LocalDateTime.of(2023, 7, 14, 9, 5),
                "India's third lunar exploration mission. Successful soft landing on the lunar south pole.",
                "https://cdn.siasat.com/wp-content/uploads/2023/08/sf-f.jpg",
                null,  SpaceMission.MissionStatus.COMPLETED, verifiedUsers.get(1 % verifiedUsers.size())));

        // New Missions (5 additional) with official/stable links
        missions.add(new SpaceMission(null, "James Webb Space Telescope (JWST) Launch", "NASA/ESA/CSA", LocalDateTime.of(2021, 12, 25, 12, 20),
                "Launch of the most powerful space telescope ever built, to observe the early universe, exoplanets, and more.",
                "https://cdn.mos.cms.futurecdn.net/qpAkShLp2kqGCkfGqGBMuf-1200-80.jpg",
                "https://www.youtube.com/watch?v=v6ihVeEoUdo", SpaceMission.MissionStatus.COMPLETED, verifiedUsers.get(0 % verifiedUsers.size())));
        missions.add(new SpaceMission(null, "Europa Clipper", "NASA", LocalDateTime.now().plusYears(1).plusMonths(3).withDayOfMonth(10).withHour(14).withMinute(0),
                "Mission to Jupiter's moon Europa to investigate its habitability, including the presence of a subsurface ocean.",
                "https://th.bing.com/th/id/R.c4f8e84c6e282d6804587d47eb0bdf81?rik=BUTTZJht%2fpZ9MA&pid=ImgRaw&r=0",
                null, SpaceMission.MissionStatus.UPCOMING, verifiedUsers.get(1 % verifiedUsers.size())));
        missions.add(new SpaceMission(null, "Psyche Mission", "NASA", LocalDateTime.of(2023, 10, 13, 14, 19),
                "Journey to a unique metal-rich asteroid orbiting the Sun between Mars and Jupiter.",
                "https://scitechdaily.com/images/Psyche-Spacecraft-Asteroid-Composite-2048x1152.jpg",
                "https://www.youtube.com/watch?v=yN9n40j5gqI", SpaceMission.MissionStatus.IN_PROGRESS, verifiedUsers.get(0 % verifiedUsers.size())));
        missions.add(new SpaceMission(null, "BepiColombo", "ESA/JAXA", LocalDateTime.of(2018, 10, 20, 1, 45),
                "Joint mission to Mercury, consisting of two orbiters to study the planet's composition, magnetosphere, and surface.",
                "https://cdn.futura-sciences.com/sources/images/bepicolombo-esa-jaxa-atg-medialab1.jpg",
                null, SpaceMission.MissionStatus.IN_PROGRESS, verifiedUsers.get(1 % verifiedUsers.size())));
        missions.add(new SpaceMission(null, "Hubble Space Telescope (HST) Servicing Mission 4 (SM4)", "NASA", LocalDateTime.of(2009, 5, 11, 18, 1),
                "Final servicing mission to the Hubble Space Telescope, extending its operational life and enhancing its capabilities.",
                "https://stsci-opo.org/STScI-01EVSRR9W0WZRR6DDKV79BX7MG.pngit ad",
                null, SpaceMission.MissionStatus.COMPLETED, verifiedUsers.get(0 % verifiedUsers.size())));

        spaceMissionRepository.saveAll(missions);
    }
}