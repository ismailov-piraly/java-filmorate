package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaMapper;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@Service
public class MpaService {

    private final MpaStorage mpaStorage;
    private final MpaMapper mpaMapper;

    public MpaService(
            @Qualifier("mpaDbStorage") MpaStorage mpaStorage,
            MpaMapper mpaMapper) {

        this.mpaStorage = mpaStorage;
        this.mpaMapper = mpaMapper;
    }

    public List<MpaDto> getMpa() {
        return mpaStorage.findAll()
                .stream()
                .map(mpaMapper::toDto)
                .toList();
    }

    public MpaDto getMpaById(Integer id) {
        Mpa mpa = mpaStorage.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Рейтинг не найден: " + id));

        return mpaMapper.toDto(mpa);
    }
}