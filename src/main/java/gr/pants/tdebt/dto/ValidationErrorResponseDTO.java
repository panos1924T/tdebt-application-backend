package gr.pants.tdebt.dto;

import java.util.Map;

public record ValidationErrorResponseDTO (
        String errorCode,
        String message,
        Map<String, String> error
){
}
