package evo.developers.ru.service;

import evo.developers.ru.entity.User;
import evo.developers.ru.entity.VersionControl;
import evo.developers.ru.jpa.VersionControlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtVersionControlService {

    private final VersionControlRepository versionControlRepository;

    public long generateAndSaveVersionControl(User user) {
        return 99999999l;//user.getMaxVersionControl().set();//
    }

}
