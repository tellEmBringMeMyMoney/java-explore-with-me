package ru.practicum.ewm.main.dto;

import jakarta.validation.constraints.NotNull;
import ru.practicum.ewm.main.model.ReactionType;

public record ReactionRequest(@NotNull ReactionType reaction) {
}
