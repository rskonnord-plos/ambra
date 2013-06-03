<#assign filterJournalsAsString>
  <#list headers.searchParameters.filterJournals as journalKey>
  ${journalKey}<#if journalKey_has_next> OR </#if>
  </#list>
</#assign>

<#assign filterSubjectsAsString>
  <#list headers.searchParameters.filterSubjects as subject>
  "${subject}"<#if subject_has_next> AND </#if>
  </#list>
</#assign>

<table width="728" cellspacing="0" cellpadding="0" border="0" style="font-family: Verdana,Arial,Helvetica,sans-serif;" align="center">
  <tbody>
    <tr>
      <td>
        <div style="border: 1px solid rgb(204, 204, 204); margin: 3px 0px; padding: 3px; font-size: 9px; text-align: center; color: rgb(153, 153, 153); background-color: rgb(238, 238, 238);">
          <strong>PLEASE DO NOT REPLY DIRECTLY TO THIS E-MAIL</strong><br/>
          <a href="http://www.plosone.org/user/secure/profile/alerts/search" title="Change/Discontinue E-mail Alerts">Click here to change or discontinue your alerts.</a>
        </div>
        <div style="border: 1px solid rgb(204, 204, 204); margin: 3px 0px; padding: 3px; font-size: 9px; text-align: center; color: rgb(153, 153, 153);
          background-color: rgb(238, 238, 238);">Add <a href="mailto:news@lists.plos.org">news@lists.plos.org</a> to your safe senders list in your e-mail address book. Doing so ensures that our e-mails reach your Inbox. Thank you.
        </div>
      </td>
    </tr>
  </tbody>
</table>

<table width="728" cellspacing="0" cellpadding="0" border="0" align="center">
  <tbody>
    <tr>
      <td><a href="http://ads.plos.org/www/delivery/ck.php?zoneid=294"><img vspace="6" border="0" alt="" src="http://ads.plos.org/www/delivery/avw.php?zoneid=294" /></a></td>
    </tr>

    <tr>
      <td><a href="http://ads.plos.org/www/delivery/ck.php?zoneid=295"><img vspace="6" border="0" alt="" src="http://ads.plos.org/www/delivery/avw.php?zoneid=295" /></a></td>
    </tr>
  </tbody>
</table>

<table width="728" cellspacing="0" cellpadding="0" border="0" align="center">
  <tbody>
    <tr>
      <td nowrap="">
        <div style="border: 0px solid rgb(204, 204, 204);">
          <a title="PLOS Open for Discovery" href="http://www.plosone.org">
            <img width="728" height="73" border="0" alt="PLOS Open for Discovery" src=${headers.imagePath}/>
          </a>
        </div>
      </td>
    </tr>
  </tbody>
</table>

<table width="728" cellspacing="0" cellpadding="4" style="border-bottom: 1px solid rgb(204, 204, 204); font-family: Verdana,Arial,Helvetica,sans-serif; font-size: 10px; color: rgb(153, 153, 153);" align="center">
  <tbody>
    <tr>
      <td>|</td>
      <td align="center"><a title="Read the Journal" style="padding: 0px 12px; text-decoration: none;" href="http://www.plos.org/publications/journals/">Read our Journals</a></td>
      <td>|</td>
      <td align="center"><a title="Submit to PLOS" style="padding: 0px 12px; text-decoration: none;" href="http://www.plos.org/publications/journals/">Submit to PLOS</a></td>
      <td>|</td>
      <td align="center"><a title="Contact Us" style="padding: 0px 12px; text-decoration: none;" href="http://www.plos.org/contact/">Contact Us</a></td>
      <td>|</td>
    </tr>
  </tbody>
</table>

<p>&nbsp;</p>

<table width="728" cellspacing="0" border="0" style="font-family: Verdana,Arial,Helvetica,sans-serif; font-size: 12px;" align="center">
  <tbody>
    <tr>
      <td valign="top"><a name="query"></a>

        <h3>Query:</h3>
        <p>
          <#if headers.searchParameters.unformattedQuery?has_content>
            <span>${headers.searchParameters.unformattedQuery}</span>
          <#else>
            <span>${headers.searchParameters.query}</span>
          </#if>
        </p>

        <h3>Filters:</h3>

        <#if filterJournalsAsString?has_content>
          Journals:${filterJournalsAsString}
        </#if>

        <#if filterSubjectsAsString?has_content>
          Subject Category:${filterSubjectsAsString}
        </#if>

        <#if headers.searchParameters.filterKeyword?has_content>
          Keyword: ${headers.searchParameters.filterKeyword}
        </#if>

        <#if headers.searchParameters.filterKeyword?has_content>
          Article Type: ${headers.searchParameters.filterKeyword}
        </#if>
      </td>
    </tr>
  </tbody>
</table>

<table width="728" cellspacing="0" border="0" style="font-family: Verdana,Arial,Helvetica,sans-serif; font-size: 12px;" align="center">
  <tbody>
    <tr>
      <td valign="top"><a name="top"></a>
        <h3>New Articles in <em>PLOS Journals</em></h3>
        <h4>Published between ${headers.lastSearchTime?string("MMM dd yyyy")} - ${headers.currentTime?string("MMM dd yyyy")}</h4>
      </td>
    </tr>

    <tr>
      <td valign="top">
        <#list headers.searchHitList as searchHit>
          <p><a href="http://dx.doi.org/info:doi/${searchHit.uri}"><strong>${searchHit.title}</strong></a><br />
            ${searchHit.creator}
          </p>
        </#list>

        <table width="728" cellspacing="0" cellpadding="0" border="0">
          <tbody>
            <tr>
              <td><a href="http://ads.plos.org/www/delivery/ck.php?zoneid=296"><img vspace="6" border="0" alt="" src="http://ads.plos.org/www/delivery/avw.php?zoneid=296" /></a></td>
            </tr>

            <tr>
              <td><a href="http://ads.plos.org/www/delivery/ck.php?zoneid=297"><img vspace="6" border="0" alt="" src="http://ads.plos.org/www/delivery/avw.php?zoneid=297" /></a></td>
            </tr>
          </tbody>
        </table>

        <br/>

        <table width="728" cellspacing="0" cellpadding="0" border="0" style="font-family: Verdana,Arial,Helvetica,sans-serif;">
          <tbody>
            <tr>
              <td valign="top">
                <div style="border: 1px solid rgb(204, 204, 204); margin: 3px 0px; padding: 3px; font-size: 9px; text-align: center; color: rgb(153, 153, 153); background-color: rgb(238, 238, 238);">

                  <p>All works published in PLOS journals are open access, distributed under the terms of the <a title="CreativeCommons.org | Attribution License" href="http://creativecommons.org/licenses/by/2.5/">Creative Commons Attribution License</a>, which permits unrestricted use, distribution, and reproduction in any medium, provided the original author and source are credited. Copyright is retained by the author.</p>

                  <p>This NEW ARTICLES WEEKLY alert includes articles published in the past week. It will be sent no more often than once per week. If you would like less frequent alerts, please subscribe to the NEW ARTICLES MONTHLY alert only.</p>

                  <p>You can <a title="Change/Discontinue E-mail Alerts" href="http://www.plosone.org/user/secure/profile/alerts/search">change or discontinue your alerts</a>  at any time by modifying your profile.</p>

                  <p>For further assistance with this alert, <a href="mailto:webmaster@plos.org">e-mail the webmaster</a>.</p>

                  <p>For other inquiries, <a title="Contact Us" href="http://www.plos.org/contact/">contact <em>PLOS</em></a>.</p>

                  <p>Public Library of Science<br/>

                    1160 Battery St<br />

                    Koshland Building East, Ste 100<br/>

                    San Francisco, CA 94111 USA</p>

                  <p>This alert contains both plain-text and html versions. To configure which one displays for you, please change the settings in your e-mail client.</p>
                </div>

                <div style="border: 1px solid rgb(204, 204, 204); margin: 3px 0px; padding: 3px; font-size: 9px; text-align: center; color: rgb(153, 153, 153); background-color: rgb(238, 238, 238);">If you are having problems viewing this e-mail correctly, please unblock the images on your e-mail client.</div>
              </td>
            </tr>
          </tbody>
        </table>

      </td>
    </tr>
  </tbody>
</table>