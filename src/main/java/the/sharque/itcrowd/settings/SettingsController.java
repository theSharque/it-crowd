package the.sharque.itcrowd.settings;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAllAttributes(settingsService.getAllSettings());

        return "settings";
    }

    @GetMapping("/settings/update")
    public String index(@RequestParam("Model") String model,
            @RequestParam("Temperature") String temperature,
            @RequestParam("Language") String language,
            @RequestParam("Roy") String roy,
            @RequestParam("Moss") String moss) {

        settingsService.updateValue("Model", model);
        settingsService.updateValue("Temperature", temperature);
        settingsService.updateValue("Language", language);
        settingsService.updateValue("Roy", roy);
        settingsService.updateValue("Moss", moss);

        return "redirect:/settings";
    }
}
