package com.example.demo.mapper;

import com.example.demo.dto.LessonDTO;
import com.example.demo.dto.QuizDTO;
import com.example.demo.dto.QuizQuestionDTO;
import com.example.demo.model.Lesson;
import com.example.demo.model.Module;
import com.example.demo.model.Quiz;
import com.example.demo.model.QuizQuestion;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-04-14T01:16:33+0100",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.42.0.z20250331-1358, environment: Java 21.0.6 (Eclipse Adoptium)"
)
@Component
public class EntityMapperImpl implements EntityMapper {

    @Override
    public LessonDTO toDTO(Lesson lesson) {
        if ( lesson == null ) {
            return null;
        }

        Long moduleId = null;
        Long id = null;
        String title = null;
        String content = null;
        String videoUrl = null;
        QuizDTO quiz = null;

        moduleId = lessonModuleId( lesson );
        id = lesson.getId();
        title = lesson.getTitle();
        content = lesson.getContent();
        videoUrl = lesson.getVideoUrl();
        quiz = quizToQuizDTO( lesson.getQuiz() );

        LessonDTO lessonDTO = new LessonDTO( id, title, content, videoUrl, quiz, moduleId );

        return lessonDTO;
    }

    @Override
    public Lesson toEntity(LessonDTO lessonDTO) {
        if ( lessonDTO == null ) {
            return null;
        }

        Lesson lesson = new Lesson();

        lesson.setContent( lessonDTO.content() );
        lesson.setId( lessonDTO.id() );
        lesson.setTitle( lessonDTO.title() );
        lesson.setVideoUrl( lessonDTO.videoUrl() );

        return lesson;
    }

    private Long lessonModuleId(Lesson lesson) {
        if ( lesson == null ) {
            return null;
        }
        Module module = lesson.getModule();
        if ( module == null ) {
            return null;
        }
        Long id = module.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    protected QuizQuestionDTO quizQuestionToQuizQuestionDTO(QuizQuestion quizQuestion) {
        if ( quizQuestion == null ) {
            return null;
        }

        Long id = null;
        Long quizId = null;
        String questionText = null;
        List<String> options = null;
        int correctOptionIndex = 0;

        QuizQuestionDTO quizQuestionDTO = new QuizQuestionDTO( id, quizId, questionText, options, correctOptionIndex );

        return quizQuestionDTO;
    }

    protected List<QuizQuestionDTO> quizQuestionListToQuizQuestionDTOList(List<QuizQuestion> list) {
        if ( list == null ) {
            return null;
        }

        List<QuizQuestionDTO> list1 = new ArrayList<QuizQuestionDTO>( list.size() );
        for ( QuizQuestion quizQuestion : list ) {
            list1.add( quizQuestionToQuizQuestionDTO( quizQuestion ) );
        }

        return list1;
    }

    protected QuizDTO quizToQuizDTO(Quiz quiz) {
        if ( quiz == null ) {
            return null;
        }

        Long id = null;
        String title = null;
        List<QuizQuestionDTO> questions = null;
        int experienceReward = 0;

        id = quiz.getId();
        title = quiz.getTitle();
        questions = quizQuestionListToQuizQuestionDTOList( quiz.getQuestions() );
        experienceReward = quiz.getExperienceReward();

        Long lessonId = null;

        QuizDTO quizDTO = new QuizDTO( id, title, questions, lessonId, experienceReward );

        return quizDTO;
    }
}
