package com.justedlev.storage.component.impl;

import com.justedlev.storage.component.UploadFileComponent;
import com.justedlev.storage.properties.JStorageProperties;
import com.justedlev.storage.properties.ServiceProperties;
import com.justedlev.storage.constant.EndpointConstant;
import com.justedlev.storage.constant.PathVariableConstant;
import com.justedlev.storage.model.response.UploadFileResponse;
import com.justedlev.storage.repository.FileRepository;
import com.justedlev.storage.repository.entity.FileEntity;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UploadFileComponentImpl implements UploadFileComponent {
    private final JStorageProperties properties;
    private final ServiceProperties serviceProperties;
    private final FileRepository fileRepository;

    @Override
    public List<UploadFileResponse> upload(List<MultipartFile> files) {
        return saveFiles(files).stream()
                .map(current -> UploadFileResponse.builder()
                        .url(getUri(current))
                        .contentType(current.getContentType())
                        .extension(current.getExtension())
                        .fileName(current.getFileName())
                        .size(current.getSize())
                        .build())
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private void saveFileToDir(MultipartFile file, FileEntity fileEntity) {
        Path copyLocation = properties.getRootPath().resolve(fileEntity.getFileName());
        Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
    }

    private String getUri(FileEntity fileEntity) {
        return UriComponentsBuilder.fromHttpUrl(serviceProperties.getHost())
                .path(EndpointConstant.FILE)
                .path(EndpointConstant.FILE_NAME.replace(PathVariableConstant.FILE_NAME, fileEntity.getFileName()))
                .toUriString();
    }

    private List<FileEntity> saveFiles(List<MultipartFile> files) {
        var entities = files.parallelStream()
                .map(this::toEntity)
                .collect(Collectors.toList());

        return fileRepository.saveAll(entities);
    }

    private FileEntity toEntity(MultipartFile file) {
        var fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        var extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        var entity = FileEntity.builder()
                .originalFileName(fileName)
                .extension(extension)
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();
        saveFileToDir(file, entity);

        return entity;
    }
}