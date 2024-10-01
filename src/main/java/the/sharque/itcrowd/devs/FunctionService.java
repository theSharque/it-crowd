package the.sharque.itcrowd.devs;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FunctionService {

    private final FunctionRepository functionRepository;

    public List<FunctionModel> getFunctions(LanguageType language) {
        return functionRepository.findAllByLanguage(language);
    }

    public Optional<LanguageType> resetMethod(Long id) {
        return functionRepository.findById(id).map(function -> {
            function.setStatus(FunctionStatus.NEW);
            functionRepository.save(function);
            return function.getLanguage();
        });
    }
}
