package com.example.HiBuddy.global.response.code.resultCode;

import com.example.HiBuddy.global.response.code.BaseErrorCode;
import com.example.HiBuddy.global.response.code.ErrorReasonDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.annotations.DialectOverride;
import org.springframework.http.HttpStatus;

// Enum Naming Format: {주체}_{이유}
@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {
    // Global
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "GLOBAL501", "서버 오류"),
    KAKAO_TOKEN_ERROR(HttpStatus.BAD_REQUEST, "GLOBAL502", "토큰관련 서버 에러"),
    GOOGLE_TOKEN_ERROR(HttpStatus.BAD_REQUEST, "GLOBAL503", "구글 토큰관련 서버 에러"),

    // OAuth
    EXPIRED_JWT_EXCEPTION(HttpStatus.UNAUTHORIZED, "OAUTH401", "기존 토큰이 만료되었습니다."),

    // Users
    USER_EXISTS_NICKNAME(HttpStatus.BAD_REQUEST, "USER401", "중복된 닉네임입니다."),

    // Posts
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST401", "게시글을 찾을 수 없습니다."),
    POST_CREATE_FAIL(HttpStatus.BAD_REQUEST, "POST402", "게시글 작성에 실패했습니다."),
    POST_SCRAB_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST403", "스크랩 목록을 찾을 수 없습니다."),
    POST_LIST_NOT_FOUND(HttpStatus.NOT_FOUND, "POST404", "게시글 목록이 존재하지 않습니다."),
    POST_TITLE_NOT_FOUND(HttpStatus.NOT_FOUND, "POST405", "존재하지 않는 게시글 제목입니다."),

    // PostLikes
    POSTLIKE_CREATE_FAIL(HttpStatus.NOT_FOUND, "POSTLIKE401", "좋아요 누르기에 실패했습니다."),
    POSTLIKE_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "POSTLIKE402", "이미 좋아요를 누른 게시물입니다."),
    POSTLIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "POSTLIKE403", "존재하지 않는 공감입니다."),

    // Comments
    COMMENT_CREATE_FAIL(HttpStatus.NOT_FOUND, "COMMENT401", "댓글 생성에 실패했습니다."),
    COMMENT_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "COMMENT402", "이미 작성한 댓글입니다."),

    // Images
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "IMAGE401", "파일이 존재하지 않습니다."),
    IMAGE_UPLOAD_FAIL(HttpStatus.BAD_REQUEST, "IMAGE402", "이미지 업로드에 실패했습니다."),
    IMAGE_NOT_PROVIDED(HttpStatus.NOT_FOUND, "IMAGE403", "프로필 이미지가 존재하지 않습니다."),

    // Searchs
    SEARCH_NOT_FOUND(HttpStatus.NOT_FOUND, "SEARCH401", "존재하지 않는 게시글 제목입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDto getReason() {
        return ErrorReasonDto.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDto getReasonHttpStatus() {
        return ErrorReasonDto.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build();
    }

}