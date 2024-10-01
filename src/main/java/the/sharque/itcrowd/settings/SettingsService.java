package the.sharque.itcrowd.settings;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;

    public Map<String, String> getAllSettings() {
        return StreamSupport.stream(settingsRepository.findAll().spliterator(), false)
                .collect(Collectors.toMap(SettingsModel::getId, SettingsModel::getVal));
    }

    public String getValue(String key, String defaultValue) {
        return settingsRepository.findById(key).map(SettingsModel::getVal).orElse(defaultValue);
    }

    public void updateValue(String key, String value) {
        settingsRepository.findById(key).ifPresent(settingsModel -> {
            settingsModel.setVal(value);
            settingsRepository.save(settingsModel);
        });
    }
}
