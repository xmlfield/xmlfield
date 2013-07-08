package org.xmlfield.core;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * This servlet filter cleans XmlField references after each request in order to
 * prevent memory issues in servlet containers.
 * <p>
 * Use it when :
 * <ul>
 * <li>You are using XmlFieldFactory with useThreadLocal enabled</li>
 * <li>The application is running in a servlet container which reuses threads
 * (thread pool)</li>
 * </ul>
 * 
 * @author Nicolas Richeton
 * 
 */
public class CleanThreadLocalFilter implements Filter {

	XmlFieldFactory factory = null;

	/**
	 * This method is intended to be overridden to setup global configuration.
	 * 
	 * @param factory
	 *            Global XmlFieldFactory
	 */
	public void configure(XmlFieldFactory factory) {
		// No special configuration
	}

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		try {
			chain.doFilter(request, response);
		} finally {
			factory.cleanThreadLocal();
		}

	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		factory = new XmlFieldFactory(true);

		// Call custom configuration if any.
		configure(factory);
	}

}
