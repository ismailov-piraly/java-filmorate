package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public ResponseEntity<List<MpaDto>> getMpa() {
        return ResponseEntity.ok(mpaService.getMpa());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MpaDto> getMpaById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(
                mpaService.getMpaById(id)
        );
    }
}