-- Migrate data from the old course_progress and completed_lessons tables to the new lesson_completions table
INSERT INTO lesson_completions (user_id, lesson_id, course_id, completion_date)
SELECT 
    cp.user_id, 
    cl.lesson_id, 
    cp.course_id, 
    cp.last_accessed
FROM 
    course_progress cp
JOIN 
    completed_lessons cl ON cl.progress_id = cp.id
ON CONFLICT (user_id, lesson_id) DO NOTHING;

-- Migrate current lesson data to the user_current_lessons table
INSERT INTO user_current_lessons (user_id, course_id, lesson_id, last_accessed)
SELECT 
    user_id, 
    course_id, 
    current_lesson_id, 
    last_accessed
FROM 
    course_progress
WHERE 
    current_lesson_id IS NOT NULL
ON CONFLICT (user_id, course_id) DO NOTHING;
