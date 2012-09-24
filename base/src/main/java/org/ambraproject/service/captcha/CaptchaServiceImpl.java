package org.ambraproject.service.captcha;

import net.tanesha.recaptcha.ReCaptcha;
import net.tanesha.recaptcha.ReCaptchaFactory;
import net.tanesha.recaptcha.ReCaptchaImpl;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Required;
import java.util.Properties;

/**
 * Implementation for a captcha
 *
 * Uses google ReCaptcha.
 */
public class CaptchaServiceImpl implements CaptchaService {
  protected static final String PUBLIC_KEY_KEY = "ambra.platform.captcha.pubkey";
  protected static final String PRIVATE_KEY_KEY = "ambra.platform.captcha.privatekey";

  private Configuration configuration;

  /** {@inheritDoc}
   */
  public boolean validateCaptcha(String ip, String challenge, String response)
  {
    ReCaptchaImpl reCaptcha = new ReCaptchaImpl();
    String privateKey = configuration.getString(PRIVATE_KEY_KEY);

    if(privateKey == null) {
      throw new RuntimeException("No private key specified for recaptcha to be enabled.");
    }

    reCaptcha.setPrivateKey(privateKey);

    return reCaptcha.checkAnswer(ip, challenge, response).isValid();
  }

  /** {@inheritDoc}
   */
  public String getCaptchaHTML()
  {
    String publicKey = configuration.getString(PUBLIC_KEY_KEY);
    String privateKey = configuration.getString(PRIVATE_KEY_KEY);

    if(publicKey == null || privateKey == null) {
      throw new RuntimeException("No keys specified for recaptcha to be enabled.");
    }

    ReCaptcha c = ReCaptchaFactory.newReCaptcha(publicKey, privateKey, false);

    return c.createRecaptchaHtml(null, new Properties()
      {{
        setProperty("theme","white");
      }}
    );
  }

  /**
   * Setter method for configuration. Injected through Spring.
   *
   * @param configuration Ambra configuration
   */
  @Required
  public void setAmbraConfiguration(Configuration configuration) {
    this.configuration = configuration;
  }
}
