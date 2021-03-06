/**
 * Yobi, Project Hosting SW
 *
 * Copyright 2013 NAVER Corp.
 * http://yobi.io
 *
 * @Author Keesun Baik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package actors;

import akka.actor.UntypedActor;
import info.schleichardt.play2.mailplugin.Mailer;
import models.Email;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.mail.HtmlEmail;
import play.Logger;
import play.i18n.Messages;
import utils.Config;

/**
 * 보조 이메일을 확인하는 이메일을 발송하는 Actor.
 *
 */
public class ValidationEmailSender extends UntypedActor {

    @Override
    public void onReceive(Object object) throws Exception {
        if (!(object instanceof Email)) {
            return;
        }

        Email email = (Email) object;

        final HtmlEmail htmlEmail = new HtmlEmail();

        try {
            htmlEmail.setFrom(Config.getEmailFromSmtp(), Messages.get("app.name"));
            htmlEmail.addTo(email.email, email.user.name);
            htmlEmail.setSubject(Messages.get("emails.validation.email.title"));
            htmlEmail.setHtmlMsg(getMessage(email.confirmUrl));
            htmlEmail.setCharset("utf-8");
            Mailer.send(htmlEmail);
            String escapedTitle = htmlEmail.getSubject().replace("\"", "\\\"");
            String logEntry = String.format("\"%s\" %s", escapedTitle, htmlEmail.getToAddresses());
            play.Logger.of("mail").info(logEntry);
        } catch (Exception e) {
            Logger.warn("Failed to send a notification: "
                    + email + "\n" + ExceptionUtils.getStackTrace(e));
        }
    }

    public String getMessage(String url) {
        String msg = String.format("<pre>%s</pre>", Messages.get("emails.click.link"));
        if (url != null) {
            msg += String.format("<hr><a href=\"%s\">%s</a>", url, url);
        }

        return msg;
    }

}
