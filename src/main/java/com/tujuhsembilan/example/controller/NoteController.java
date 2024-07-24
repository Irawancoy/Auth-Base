package com.tujuhsembilan.example.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tujuhsembilan.example.controller.dto.NoteDto;
import com.tujuhsembilan.example.model.Note;
import com.tujuhsembilan.example.repository.NoteRepo;
import com.tujuhsembilan.example.repository.RefreshTokenRepo;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/note")
@RequiredArgsConstructor
public class NoteController {

    private final NoteRepo repo;
    private final ModelMapper mdlMap;
    private final RefreshTokenRepo refreshTokenRepo; 

    @GetMapping("/all")
    public ResponseEntity<?> getNotes(Authentication auth, @RequestHeader("Device-Id") String deviceId) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        String userName = jwt.getClaimAsString("sub");
        
        // Validasi Device ID
        if (!isDeviceIdValid(userName, deviceId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(HttpStatus.FORBIDDEN);
        }

        List<Note> notes = repo.findByUserName(userName);
        List<NoteDto> notesDto = notes.stream().map(note -> mdlMap.map(note, NoteDto.class)).collect(Collectors.toList());

        return ResponseEntity.ok(notesDto);
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveNote(@RequestBody NoteDto body, Authentication auth, @RequestHeader("Device-Id") String deviceId) {
        Jwt jwt = (Jwt) auth.getPrincipal();
        String userName = jwt.getClaimAsString("sub");

        // Validasi Device ID
        if (!isDeviceIdValid(userName, deviceId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(HttpStatus.FORBIDDEN);
        }

        Note note = mdlMap.map(body, Note.class);
        note.setUserName(userName);

        return ResponseEntity.status(HttpStatus.CREATED).body(mdlMap.map(repo.save(note), NoteDto.class));
    }

    private boolean isDeviceIdValid(String userName, String deviceId) {
        return refreshTokenRepo.findByUsernameAndDeviceId(userName, deviceId).isPresent();
    }
}
