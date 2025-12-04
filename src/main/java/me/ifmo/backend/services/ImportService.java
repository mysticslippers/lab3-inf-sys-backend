package me.ifmo.backend.services;

import me.ifmo.backend.DTO.ImportOperationDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImportService {

    ImportOperationDTO importRoutes(MultipartFile file, String username) throws IOException;

    List<ImportOperationDTO> getMyOperations(String username);

    List<ImportOperationDTO> getAllOperations();
}
