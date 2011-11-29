/* vim: set ts=2 et sw=2 cindent fo=qroca: */

package com.globant.katari.core.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.Validate;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

/** Filter to catch all the exceptions and show then in a user defined page.
 *
 * This filter catches downstream exceptions and forwards it to a configurable
 * freemarker template. It also logs the exception to this class logger, at the
 * ERROR level.
 *
 * In debug mode, this filter just propagates the exception upwards, to let the
 * container show the normal exception page. You can pass the parameter
 * previewErrorPage to see how an error page would be generated without debug
 * mode. Note that the presence of the previewErrorPage parameter does not
 * force the error page to show up, there must be an exception downstream for
 * that.
 *
 * It is intended to be configured in two different places. First, just after
 * the sitemesh decorator filter, to catch the errors in the 'content' of the
 * page. This gives sitemesh the opportunity to decorate the error page, so the
 * user does not loose the navigation.
 *
 * Also, place it at the beginnig of the filter chain to catch all errors
 * generated in the decorator and intermediate filters.
 *
 * This filter generates the error page simply forwarding to templateName. The
 * most common option is to use a FreemarkerServlet, like the one used in
 * sitemesh.
 *
 * The error page template has access to the exception in a request parameter
 * called 'exception'.
 */
public class ExceptionHandlerFilter implements Filter {

  /** The class logger.*/
  private static Logger log = LoggerFactory.getLogger(
      ExceptionHandlerFilter.class);

  /** The Freemarker's template name, never null. */
  private final String templateName;

  /** Checks if the application is running in debug mode.*/
  private final boolean debugMode;

  /** The servlet context.
   *
   * This is used to generate the error page by forwarding the request to a
   * freemarker servlet. This is never null after init.
   */
  private ServletContext servletContext = null;

  /** Builds a new instance of the filter.
   *
   * @param theTemplateName the default view for all errors. Cannot be null.
   *
   * @param isInDebugMode if the application is running in debug mode.
   */
  public ExceptionHandlerFilter(final String theTemplateName,
      final boolean isInDebugMode) {
    Validate.notNull(theTemplateName, "The theTemplateName cannot be null");
    templateName = theTemplateName;
    debugMode = isInDebugMode;
  }

  /** A response wrapper that provides access to the data submitted to the
   * client.
   */
  private static class ResponseBufferer extends ServletOutputInterceptor {

    /** The output stream that holds the data that has been sent to the client.
     *
     * It is never null.
     */
    private ByteArrayOutputStream output = new ByteArrayOutputStream();

    /** Constructor.
     *
     * @param response the wrapped response.
     */
    public ResponseBufferer(final HttpServletResponse response) {
      super(response, false);
    }

    /** Returns the generated response as a byte array.
     *
     * @return the byte array of the generated response, never null.
     */
    public byte[] toByteArray() {
     return output.toByteArray();
    }

    /** {@inheritDoc}
    */
    protected OutputStream createOutputStream() {
      return output;
    }
  };

  /** {@inheritDoc}.
   */
  public void init(final FilterConfig filterConfig) throws ServletException {
    servletContext = filterConfig.getServletContext();
  }

  /** Filters the request and generates an error page in case of exception.
   *
   * This page contains information about:
   * <ul>
   *  <li>
   *    The Exception
   *  </li>
   *  <li>
   *    Current Request
   *  </li>
   *  <li>
   *    If the application runs on debug or not.
   *  </li>
   *  <li>
   *    The show stack trace parameter.
   *  </li>
   * </ul>
   *
   * {@inheritDoc}.
   */
  public void doFilter(final ServletRequest request, final ServletResponse
      response, final FilterChain chain) throws IOException,
      ServletException {
    log.trace("Entering doFilter.");
    if (!(request instanceof HttpServletRequest)) {
      throw new RuntimeException("Not an http request");
    }
    if (!(response instanceof HttpServletResponse)) {
      throw new RuntimeException("Not an http request");
    }

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    ResponseBufferer wrapper = new ResponseBufferer(httpResponse);

    try {
      chain.doFilter(request, wrapper);
      wrapper.flushBuffer();
      response.getOutputStream().write(wrapper.toByteArray());
    } catch (RuntimeException e) {
      if (!handleException(httpRequest, httpResponse, e)) {
       throw e;
      }
    } catch (ServletException e) {
      if (!handleException(httpRequest, httpResponse, e)) {
       throw e;
      }
    } catch (IOException e) {
      if (!handleException(httpRequest, httpResponse, e)) {
       throw e;
      }
    }
    log.trace("Leaving doFilter");
  }

  /** Handles the exception caught from the filter chain.
   *
   * This operation uses the freemarker template to generate an error page with
   * the information obtained from the exception.
   *
   * @param request the servlet request. It cannot be null.
   *
   * @param response the servlet response. It cannot be null.
   *
   * @param e the exception caught. It cannot be null.
   *
   * @return true if the exception was handled here. If this operation returns
   * false, the caller is intended to rethrow the exception.
   *
   * @throws IOException in case of error generating the output.
   *
   * @throws ServletException in case of another unexpect error.
   */
  private boolean handleException(final HttpServletRequest request,
      final HttpServletResponse response, final Exception e)
      throws IOException, ServletException {

    boolean preview = request.getParameter("previewErrorPage") != null;

    // We don't generate the output in debug mode.
    if (debugMode && !preview) {
      return false;
    } else {
      log.error(e.getMessage(), e);

      request.setAttribute("exception", e);
      RequestDispatcher dispatcher;
      dispatcher = servletContext.getRequestDispatcher(templateName);
      dispatcher.include(request, response);
    }
    return true;
  }

  /** {@inheritDoc}.
   *
   * This implementation does nothing.
   */
  public void destroy() {
  }
}

