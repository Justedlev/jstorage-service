package com.justedlev.service.storage.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class FileResponse {
    private String fileName;
    private String extension;
    private String contentType;
    private String url;
    private Long size;
}
