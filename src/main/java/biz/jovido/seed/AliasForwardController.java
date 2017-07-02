package biz.jovido.seed;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Stephan Grundner
 */
@Component
public class AliasForwardController implements Controller {

    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView modelAndView = new ModelAndView();

//        String path = request.getServletPath();
//        path = UriUtils.encodePathSegment(path, "UTF-8");
        Alias alias = (Alias) request.getAttribute(Alias.class.getName());
        modelAndView.setViewName("forward:/item?alias=" + alias.getId());
        return modelAndView;
    }
}
