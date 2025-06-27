package de.tum.devops.application.dto;

import java.util.UUID;

public record FileInfoDto(UUID applicationId, //
                          String originalFileName, //
                          String downloadFileName, //
                          long size, //
                          String contentType, //
                          boolean exists, //
                          String resumeTextPreview, //
                          int resumeTextLength) {
}