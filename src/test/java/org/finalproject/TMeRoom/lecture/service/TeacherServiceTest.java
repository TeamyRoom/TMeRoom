package org.finalproject.TMeRoom.lecture.service;

import org.finalproject.tmeroom.lecture.data.dto.request.AppointTeacherRequestDto;
import org.finalproject.tmeroom.lecture.data.dto.response.TeacherDetailResponseDto;
import org.finalproject.tmeroom.lecture.data.entity.Lecture;
import org.finalproject.tmeroom.lecture.data.entity.Teacher;
import org.finalproject.tmeroom.lecture.repository.LectureRepository;
import org.finalproject.tmeroom.lecture.repository.TeacherRepository;
import org.finalproject.tmeroom.lecture.service.TeacherService;
import org.finalproject.tmeroom.member.constant.MemberRole;
import org.finalproject.tmeroom.member.data.dto.MemberDto;
import org.finalproject.tmeroom.member.data.entity.Member;
import org.finalproject.tmeroom.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@SpringBootTest(classes = {TeacherService.class})
@ActiveProfiles("test")
@DisplayName("강사 서비스")
class TeacherServiceTest {
    @Autowired
    private TeacherService teacherService;
    @MockBean
    private TeacherRepository teacherRepository;
    @MockBean
    private MemberRepository memberRepository;
    @MockBean
    private LectureRepository lectureRepository;

    private Lecture getMockLecture() {
        return Lecture.builder()
                .manager(getMockManager())
                .lectureName("강의명")
                .lectureCode("code")
                .build();
    }

    private Member getMockManager() {
        return Member.builder()
                .id("manager")
                .pw("encodedPw")
                .email("testGuest@test.com")
                .nickname("manager")
                .role(MemberRole.USER)
                .build();
    }

    private Member getMockTeacherMember() {
        return Member.builder()
                .id("teacher")
                .pw("encodedPw")
                .email("testGuest@test.com")
                .nickname("teacher")
                .role(MemberRole.USER)
                .build();
    }

    private MemberDto getMockManagerDto() {
        return MemberDto.builder()
                .id("manager")
                .nickname("manager")
                .build();
    }

    private Teacher getMockTeacher() {
        return Teacher.builder()
                .lecture(getMockLecture())
                .member(getMockTeacherMember())
                .build();
    }

    @Test
    @DisplayName("강사 목록 조회")
    void lookupTeachers() {
        //Given
        String lectureCode = "code";
        MemberDto mockManagerDto = getMockManagerDto();
        Lecture lecture = getMockLecture();
        Teacher teacher = getMockTeacher();
        Pageable pageable = PageRequest.of(0, 20);
        Page<Teacher> studentPage = new PageImpl<>(List.of(teacher), pageable, 0);


        given(lectureRepository.findById(lectureCode)).willReturn(Optional.of(lecture));
        given(teacherRepository.findByLecture(any(Pageable.class), eq(lecture))).willReturn(studentPage);

        //When
        Page<TeacherDetailResponseDto> teagerResponsePage = teacherService.lookupTeachers(lectureCode, mockManagerDto, pageable);

        //Then
        assertThat(teagerResponsePage.get().findFirst().get().getNickName()).isEqualTo(teacher.getMember().getNickname());
    }

    @Test
    @DisplayName("강사 임명")
    void appointTeacher() {
        //Given
        Lecture lecture = getMockLecture();
        String lectureCode = "code";
        MemberDto mockManagerDto = getMockManagerDto();
        Member teacher = getMockTeacherMember();
        AppointTeacherRequestDto dto = new AppointTeacherRequestDto();
        dto.setTeacherId("teacher");

        given(lectureRepository.findById(lectureCode)).willReturn(Optional.of(lecture));
        given(memberRepository.findById(dto.getTeacherId())).willReturn(Optional.of(teacher));

        //When
        teacherService.appointTeacher(lectureCode, mockManagerDto, dto);

        //Then
        then(teacherRepository).should().save(any(Teacher.class));
    }

    @Test
    @DisplayName("강사 해임")
    void dismissTeacher() {
        //Given
        Lecture lecture = getMockLecture();
        String lectureCode = "code";
        MemberDto mockManagerDto = getMockManagerDto();
        Teacher teacher = getMockTeacher();

        given(lectureRepository.findById(lectureCode)).willReturn(Optional.of(lecture));
        given(teacherRepository.findByMemberIdAndLectureCode(teacher.getTeacherId(), lectureCode)).willReturn(teacher);

        //When
        teacherService.dismissTeacher(lectureCode, teacher.getTeacherId(), mockManagerDto);

        //Then
        then(teacherRepository).should().delete(teacher);
    }
}