package the.sharque.itcrowd.settings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAllAttributes(settingsService.getAllSettings());

        return "settings";
    }

    @GetMapping("/settings/save/{key}/{value}")
    public String index(@PathVariable String key, @PathVariable String value) {
        settingsService.updateValue(key, value);

        return "redirect:/settings";
    }
}
