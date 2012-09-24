package org.ambraproject.service.captcha;

/**
 * Interface for a captcha implementation
 *
 */
public interface CaptchaService {

  /**
   * Validate the given challenge and response
   *
   * @parem ip the current user's IP address
   * @param challenge challenge (from the html form snippet)
   * @param response response (from the html form snippet)
   *
   * @return true if the captcha is valid
   *
   * @throws Exception
   */
  public boolean validateCaptcha(String ip, String challenge, String response);

  /**
   * @return Returns a captchaHTML block to insert into a web page
   */
  public String getCaptchaHTML();
}
