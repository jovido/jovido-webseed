package biz.jovido.seed.content.url;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.handler.AbstractHandlerMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Stephan Grundner
 */
public class AliasRequestMapping extends AbstractHandlerMapping {

    @Autowired
    private AliasForwardController aliasForwardController;

    @Autowired
    private HostService hostService;

    @Autowired
    private AliasService aliasService;

    @Override
    protected Object getHandlerInternal(HttpServletRequest request) throws Exception {
        Host host = hostService.getHost(request.getServerName());
        if (host != null) {
            String path = request.getServletPath();
            if (StringUtils.hasLength(path) && path.startsWith("/")) {
                path = path.substring(1);
            }
            Alias alias = aliasService.getAlias(host, path);
            if (alias != null) {
                request.setAttribute(Alias.class.getName(), alias);
                return new HandlerExecutionChain(aliasForwardController);
            }
        }

        return null;
    }
}