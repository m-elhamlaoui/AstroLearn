-- Create the lesson_completions table
CREATE TABLE IF NOT EXISTS lesson_completions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    completion_date TIMESTAMP NOT NULL,
    CONSTRAINT fk_lesson_completions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_lesson_completions_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    CONSTRAINT fk_lesson_completions_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_lesson UNIQUE (user_id, lesson_id)
);

-- Create the user_current_lessons table
CREATE TABLE IF NOT EXISTS user_current_lessons (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    lesson_id BIGINT NOT NULL,
    last_accessed TIMESTAMP NOT NULL,
    CONSTRAINT fk_user_current_lessons_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_current_lessons_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_current_lessons_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE CASCADE,
    CONSTRAINT uq_user_course UNIQUE (user_id, course_id)
);

-- Create indexes for better performance
CREATE INDEX idx_lesson_completions_user_id ON lesson_completions(user_id);
CREATE INDEX idx_lesson_completions_course_id ON lesson_completions(course_id);
CREATE INDEX idx_lesson_completions_lesson_id ON lesson_completions(lesson_id);
CREATE INDEX idx_user_current_lessons_user_id ON user_current_lessons(user_id);
CREATE INDEX idx_user_current_lessons_course_id ON user_current_lessons(course_id);
