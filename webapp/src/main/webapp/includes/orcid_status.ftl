<@s.url id="orcIDRemoveURL" action="orcidRemove" namespace="/user/secure" />
<@s.url action="authors" namespace="/static" includeParams="none" id="authorsURL"/>
<div class="panel orcid-info short cf">
    <div class="left">
        <a href="http://orcid.org/about/what-is-orcid" target="_blank" class="image-text orcid">ORCID</a>

    </div>
    <div class="right ">
        <#-- Change orcURL to be production -->
        <p>Your ORCiD account: <strong><a href="http://sandbox-1.orcid.org/${orcid}">${orcid}</a></strong><br />
            is linked to your Ambra account. <strong><a href="${orcIDRemoveURL}">De-link</a></strong>
        </p>
    </div>
</div>