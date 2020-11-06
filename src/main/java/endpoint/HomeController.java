package endpoint;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @RequestMapping("/")
    public String home(Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        } else {
            return "index.html";
        }
    }

}
