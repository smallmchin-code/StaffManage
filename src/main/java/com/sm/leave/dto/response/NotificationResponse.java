package com.sm.leave.dto.response;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record NotificationResponse(Long          notificationId,
                                   String        title,
                                   String        content,
                                   Boolean       isRead,
                                   LocalDateTime createdAt) {}

