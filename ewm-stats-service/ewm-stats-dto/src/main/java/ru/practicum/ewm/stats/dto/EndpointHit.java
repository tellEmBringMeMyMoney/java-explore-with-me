package ru.practicum.ewm.stats.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EndpointHit(
        Long id,
        @NotBlank String app,
        @NotBlank String uri,
        @NotBlank String ip,
        @NotBlank @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}") String timestamp
) {
}
