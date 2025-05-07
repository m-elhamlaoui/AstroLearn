-- data.sql

-- Make sure to run these in an order that respects foreign key constraints.
-- IDs are assumed to be auto-generated starting from 1 for each table.

-- 1. Users
-- Passwords should be pre-hashed in a real application.
INSERT INTO users (username, password, email, bio, profile_image_url, photo_cover_url, role, verification_status, experience_points, level) VALUES
    ('adminuser', '$2a$10$YAMBCBFAHP5iI79c6oK5nekofdtBDh2USVbg/pqnfEHWms/S72iQq', 'admin@example.com', 'Site administrator and space enthusiast.', 'default_profile.png', 'default_cover.png', 'ADMIN', 'VERIFIED', 5000, 'ASTRONAUT')
    ON CONFLICT (username) DO NOTHING;

INSERT INTO users (username, password, email, bio, profile_image_url, photo_cover_url, role, verification_status, experience_points, level) VALUES
    ('testuser', '$2a$10$YAMBCBFAHP5iI79c6oK5nekofdtBDh2USVbg/pqnfEHWms/S72iQq', 'test@example.com', 'Curious explorer of the cosmos.', 'test_user_profile.png', 'test_user_cover.png', 'USER', 'VERIFIED', 150, 'NOVICE')
    ON CONFLICT (username) DO NOTHING;

-- 2. TagNames
INSERT INTO tag_names (name) VALUES
    ('Space Exploration') ON CONFLICT (name) DO NOTHING;
INSERT INTO tag_names (name) VALUES
    ('Mars') ON CONFLICT (name) DO NOTHING;
INSERT INTO tag_names (name) VALUES
    ('Rockets') ON CONFLICT (name) DO NOTHING;
INSERT INTO tag_names (name) VALUES
    ('Astrophysics') ON CONFLICT (name) DO NOTHING;
INSERT INTO tag_names (name) VALUES
    ('Cosmology') ON CONFLICT (name) DO NOTHING;

-- 3. Courses
INSERT INTO courses (title, description, difficulty) VALUES
    ('Introduction to Rocket Science', 'Learn the fundamental principles of rocket propulsion, design, and orbital mechanics. Perfect for beginners!', 'BEGINNER');
INSERT INTO courses (title, description, difficulty) VALUES
    ('Deep Space Navigation & Astronomy', 'Explore advanced techniques for navigating the cosmos and understanding celestial bodies. Suitable for intermediate learners.', 'INTERMEDIATE');

-- 4. Modules (Assuming Course IDs are 1 and 2)
INSERT INTO modules (title, course_id) VALUES
    ('Fundamentals of Propulsion', 1); -- For "Introduction to Rocket Science"
INSERT INTO modules (title, course_id) VALUES
    ('Rocket Design Principles', 1); -- For "Introduction to Rocket Science"
INSERT INTO modules (title, course_id) VALUES
    ('Celestial Mechanics', 2); -- For "Deep Space Navigation & Astronomy"
INSERT INTO modules (title, course_id) VALUES
    ('Observational Astronomy', 2); -- For "Deep Space Navigation & Astronomy"

-- 5. Lessons (Assuming Module IDs are 1, 2, 3, 4)
INSERT INTO lessons (title, content, video_url, module_id) VALUES
    ('Newtons Laws and Rockets', 'Detailed explanation of how Newtons laws apply to rocket flight.', 'https://example.com/video/newtons_laws', 1);
INSERT INTO lessons (title, content, video_url, module_id) VALUES
    ('Types of Rocket Engines', 'Overview of different rocket engine types: solid, liquid, ion.', 'https://example.com/video/rocket_engines', 1);
INSERT INTO lessons (title, content, video_url, module_id) VALUES
    ('Aerodynamics in Rocketry', 'Understanding aerodynamic forces on rockets during atmospheric flight.', 'https://example.com/video/aerodynamics', 2);
INSERT INTO lessons (title, content, video_url, module_id) VALUES
    ('Keplers Laws of Planetary Motion', 'Understanding how planets orbit stars.', 'https://example.com/video/keplers_laws', 3);

-- 6. Quizzes (Assuming Lesson ID 1)
INSERT INTO quizzes (title, lesson_id, experience_reward) VALUES
    ('Rocket Propulsion Basics Quiz', 1, 50); -- Quiz for "Newtons Laws and Rockets"
INSERT INTO quizzes (title, lesson_id, experience_reward) VALUES
    ('Planetary Motion Quiz', 4, 75); -- Quiz for "Keplers Laws"

-- 7. QuizQuestions (Assuming Quiz IDs are 1 and 2)
-- For Quiz 1
INSERT INTO quiz_questions (quiz_id, question_text, correct_option_index) VALUES
    (1, 'Which of Newtons laws primarily explains rocket thrust?', 1);
INSERT INTO quiz_questions (quiz_id, question_text, correct_option_index) VALUES
    (1, 'What does "ISP" stand for in rocketry?', 0);
-- For Quiz 2
INSERT INTO quiz_questions (quiz_id, question_text, correct_option_index) VALUES
    (2, 'Keplers First Law states that planets orbit in what shape?', 2);

-- 7a. question_options (Join table for QuizQuestion.options)
-- Options for Question 1 (Quiz 1)
INSERT INTO question_options (question_id, options, options_order) VALUES (1, 'First Law (Inertia)', 0);
INSERT INTO question_options (question_id, options, options_order) VALUES (1, 'Third Law (Action-Reaction)', 1);
INSERT INTO question_options (question_id, options, options_order) VALUES (1, 'Second Law (F=ma)', 2);
-- Options for Question 2 (Quiz 1)
INSERT INTO question_options (question_id, options, options_order) VALUES (2, 'Specific Impulse', 0);
INSERT INTO question_options (question_id, options, options_order) VALUES (2, 'Internal Specific Power', 1);
INSERT INTO question_options (question_id, options, options_order) VALUES (2, 'Instantaneous Speed Profile', 2);
-- Options for Question 3 (Quiz 2)
INSERT INTO question_options (question_id, options, options_order) VALUES (3, 'Circles', 0);
INSERT INTO question_options (question_id, options, options_order) VALUES (3, 'Parabolas', 1);
INSERT INTO question_options (question_id, options, options_order) VALUES (3, 'Ellipses', 2);


-- 8. Articles (Assuming User IDs are 1 and 2)
-- For Article.imageUrls, using PostgreSQL ARRAY syntax
INSERT INTO articles (title, summary, content, image_urls, author_id, created_at) VALUES
    ('The Future of Mars Colonization', 'A deep dive into the challenges and prospects of establishing a human presence on Mars.', 'Long detailed content about Mars missions, habitat construction, resource utilization, and ethical considerations...', ARRAY['mars_colony_concept.jpg', 'mars_rover_future.png'], 1, CURRENT_TIMESTAMP - INTERVAL '2 day');
INSERT INTO articles (title, summary, content, image_urls, author_id, created_at) VALUES
    ('Understanding Black Holes: Gateways or Endpoints?', 'Exploring the mysterious nature of black holes, from their formation to their potential role in the universe.', 'Content covering general relativity, event horizons, singularities, Hawking radiation, and speculative theories about black holes...', ARRAY['black_hole_simulation.gif'], 2, CURRENT_TIMESTAMP - INTERVAL '1 day');

-- 9. Comments (Assuming User IDs 1, 2 and Article IDs 1, 2)
INSERT INTO comments (content, user_id, article_id, created_at) VALUES
    ('This is a fantastic overview of Mars colonization! Really makes you think.', 2, 1, CURRENT_TIMESTAMP - INTERVAL '1 day 10 hour');
INSERT INTO comments (content, user_id, article_id, created_at) VALUES
    ('Great points, but what about the psychological impact on colonists?', 1, 1, CURRENT_TIMESTAMP - INTERVAL '1 day 5 hour');
INSERT INTO comments (content, user_id, article_id, created_at) VALUES
    ('Black holes are truly mind-bending. Loved the explanation of Hawking radiation.', 1, 2, CURRENT_TIMESTAMP - INTERVAL '20 hour');

-- 10. ArticleTags (Join Table - Assuming Article IDs 1, 2 and TagName IDs 1, 2, 3, 4)
INSERT INTO article_tags (article_id, tag_name_id) VALUES (1, 1); -- Article 1 tagged with "Space Exploration"
INSERT INTO article_tags (article_id, tag_name_id) VALUES (1, 2); -- Article 1 tagged with "Mars"
INSERT INTO article_tags (article_id, tag_name_id) VALUES (2, 4); -- Article 2 tagged with "Astrophysics"
INSERT INTO article_tags (article_id, tag_name_id) VALUES (2, 5); -- Article 2 tagged with "Cosmology"

-- 11. ArticleVotes (Assuming User IDs 1, 2 and Article IDs 1, 2)
-- User 2 upvotes Article 1
INSERT INTO article_votes (value, user_id, article_id) VALUES (1, 2, 1)
    ON CONFLICT (user_id, article_id) DO NOTHING;
-- User 1 upvotes Article 2
INSERT INTO article_votes (value, user_id, article_id) VALUES (1, 1, 2)
    ON CONFLICT (user_id, article_id) DO NOTHING;
-- User 1 downvotes Article 1 (example)
INSERT INTO article_votes (value, user_id, article_id) VALUES (-1, 1, 1)
    ON CONFLICT (user_id, article_id) DO UPDATE SET value = EXCLUDED.value; -- Allow changing vote

-- 12. CourseProgress (Assuming User ID 2, Course ID 1, Current Lesson ID 2)
-- User 2 starts "Introduction to Rocket Science", current lesson is "Types of Rocket Engines"
INSERT INTO course_progress (user_id, course_id, current_lesson_id, completion_percentage, completed, last_accessed) VALUES
    (2, 1, 2, 50.0, false, CURRENT_TIMESTAMP - INTERVAL '3 hour'); -- Assuming course 1 has 2 lessons for this module path

-- 12a. completed_lessons (Join table for CourseProgress.completedLessonIds - Assuming CourseProgress ID 1, Lesson ID 1)
-- User 2 completed "Newtons Laws and Rockets" (Lesson ID 1) for the above progress (Progress ID 1)
INSERT INTO completed_lessons (progress_id, lesson_id) VALUES (1, 1);

-- 13. QuizCompletions (Assuming User ID 2, Quiz ID 1)
INSERT INTO quiz_completions (user_id, quiz_id, score, completion_date) VALUES
    (2, 1, 80, CURRENT_TIMESTAMP - INTERVAL '2 hour'); -- Score 80% on "Rocket Propulsion Basics Quiz"

-- 14. ReadingHistory (Assuming User ID 2, Article ID 1)
-- User 2 read "The Future of Mars Colonization"
INSERT INTO reading_history (user_id, article_id, is_read, time_spent_seconds, last_accessed) VALUES
    (2, 1, true, 180, CURRENT_TIMESTAMP - INTERVAL '5 hour'); -- Marked as read because timeSpent > 30

-- 15. SpaceMissions (Assuming User ID 1)
INSERT INTO missions (name, agency, launch_date, description, mission_image, live_stream_url, status, user_id) VALUES
    ('Artemis III Lunar Landing', 'NASA', '2026-09-15 14:30:00', 'The first crewed lunar landing since Apollo 17, aiming to land the first woman and next man on the Moon.', 'artemis_iii_patch.png', 'https://nasa.gov/live', 'UPCOMING', 1);
INSERT INTO missions (name, agency, launch_date, description, mission_image, live_stream_url, status, user_id) VALUES
    ('Mars Sample Return - Orbiter', 'ESA/NASA', '2027-01-01 00:00:00', 'Earth Return Orbiter to capture samples collected by the Perseverance rover and bring them to Earth.', 'msr_orbiter.jpg', null, 'UPCOMING', 1);