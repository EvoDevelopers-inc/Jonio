package evo.developers.ru.service;

import evo.developers.ru.entity.JwtVersionControl;
import evo.developers.ru.entity.User;
import evo.developers.ru.jpa.JwtVersionControlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class JwtVersionControlService {

    private final JwtVersionControlRepository versionControlRepository;
    private final SecureRandom random = new SecureRandom();

    public JwtVersionControl generateRandomVersion(JwtVersionControl jwtVersionControl) {

        long versionControlId = random.nextLong();
        jwtVersionControl.setId(versionControlId);

        return jwtVersionControl;
    }

    public long plusPlusVersion(User user) {

        long versionControlId = user.getMaxVersionControl().getFirst().getIncrement() + 1;

        user.getMaxVersionControl().getFirst().setIncrement(versionControlId);
        versionControlRepository.save(user.getMaxVersionControl().getFirst());

        return versionControlId;
    }

}
