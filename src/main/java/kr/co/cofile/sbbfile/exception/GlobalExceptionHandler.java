package kr.co.cofile.sbbfile.exception;

import jakarta.servlet.http.HttpServletRequest;
import kr.co.cofile.sbbfile.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.IOException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(FileNotFoundException.class)
    public String handleFileNotFoundException(
            FileNotFoundException e,
            Model model,
            HttpServletRequest request) {

        log.error("파일 다운로드 오류: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.of(
                "FILE_NOT_FOUND",
                e.getMessage(),
                request.getRequestURI()
        );
        model.addAttribute("error", errorResponse);

        return "error/error";  // error.html 템플릿을 사용
    }

//    @ExceptionHandler(IOException.class)
//    public String handleIOException(
//            IOException e,
//            Model model,
//            HttpServletRequest request) {
//
//        log.error("IO Exception: {}", e.getMessage(), e);
//
//        ErrorResponse error = ErrorResponse.of(
//                "FILE_IO_ERROR",
//                "파일 처리 중 오류가 발생했습니다.",
//                request.getRequestURI()
//        );
//        model.addAttribute("error", error);
//
//        return "error/error";
//    }
}
