package gr.pants.tdebt.dto.user_dto;

import java.time.Instant;

public record UserReadOnlyDTO(

        String uuid,

        String email,

        boolean deleted,

        Instant createdAt,

        Instant updatedAt
) {
}
