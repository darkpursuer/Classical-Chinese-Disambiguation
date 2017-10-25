package nlpccd.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class OptimizeController {

	@RequestMapping(value = "/optmz", method = RequestMethod.GET)
	public ModelAndView index() {
		return new ModelAndView("optmz");
	}

}
