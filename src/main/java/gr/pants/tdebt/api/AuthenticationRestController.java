package gr.pants.tdebt.api;

import gr.pants.tdebt.authentication.AuthenticationService;
import gr.pants.tdebt.dto.AuthenticationRequestDTO;
import gr.pants.tdebt.dto.AuthenticationResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationRestController {

    private final AuthenticationService authenticationService;

    @Operation(summary="Authenticate", description="Authenticates user and returns a token")
    @ApiResponses({
            @ApiResponse(responseCode="200", description="OK", content=@Content(schema=@Schema(implementation=AuthenticationResponseDTO.class))),
            @ApiResponse(responseCode="400", description="Validation error")
    })
    @PostMapping
    public ResponseEntity<AuthenticationResponseDTO> authenticate(
            @Valid @RequestBody AuthenticationRequestDTO authenticationRequestDTO) {

        AuthenticationResponseDTO authenticationResponseDTO = authenticationService.authenticate(authenticationRequestDTO);
        return ResponseEntity.ok(authenticationResponseDTO);
    }
}
