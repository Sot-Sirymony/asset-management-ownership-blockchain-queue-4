package com.up.asset_holder_api.service.serviceImp;

import com.up.asset_holder_api.exception.NotFoundException;
import com.up.asset_holder_api.service.AppFileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;


@Service
public class AppFileServiceImp implements AppFileService {
    private final Path path = Paths.get("src/main/resources/images");
    @Override
    public String saveFile(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        assert fileName != null;
        if (fileName.toLowerCase().endsWith(".png") ||
                fileName.toLowerCase().endsWith(".jpg") ||
                fileName.toLowerCase().endsWith(".jpeg") ||
                fileName.toLowerCase().endsWith(".avis") ||
                fileName.toLowerCase().endsWith(".avi")
        ){

            // convert file name to uuid format form

            fileName = UUID.randomUUID()+ "." + StringUtils.getFilenameExtension(fileName);
            // if the folder not exist create one
            if (!Files.exists(path)){Files.createDirectories(path);}
            // copy byte that from input stream to file
            Files.copy(file.getInputStream(), path.resolve(fileName));

            return fileName;

        }else throw new NotFoundException("File must be contain jpg, png, jpeg");
    }
    @Override
    public Resource getFileByFileName(String fileName) throws IOException {

        Path path = Paths.get("src/main/resources/images/" + fileName);
        if(!Files.exists(path)){
            throw new NotFoundException("File not found");
        }
        return new ByteArrayResource(Files.readAllBytes(path));
    }

    @Override
    public List<String> listFileNames() {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (Stream<Path> stream = Files.list(path)) {
            List<String> names = new ArrayList<>();
            stream.filter(Files::isRegularFile).forEach(p -> names.add(p.getFileName().toString()));
            return names;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
}