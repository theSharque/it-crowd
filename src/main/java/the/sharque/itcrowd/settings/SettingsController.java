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
    public String index(@RequestParam String model, @RequestParam String roy, @RequestParam String moss) {
        settingsService.updateValue("model", model);
        settingsService.updateValue("roy", roy);
        settingsService.updateValue("moss", moss);

        return "redirect:/settings";
    }
}
