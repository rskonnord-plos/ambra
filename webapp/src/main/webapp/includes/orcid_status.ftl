<@s.url id="orcIDRemoveURL" action="orcidRemove" namespace="/user/secure" />
<@s.url action="authors" namespace="/static" includeParams="none" id="authorsURL"/>
<div class="panel orcid-info short cf">
    <div class="left">
        <a href="http://orcid.org/about/what-is-orcid" target="_blank" class="image-text orcid">ORCID</a>

    </div>
    <div class="right ">
        <#-- Change orcURL to be production -->
        <p>Your ORCiD account: <strong><a href="http://orcid.org/${orcid}">${orcid}</a></strong><br />
            is linked to your PLOS account. <strong><a href="${orcIDRemoveURL}"
                                                    data-js="orcid-delink">De-link</a></strong>
        </p>
    </div>
</div>

<div class="orcid-form no-display" >
    <p class="messaging action"><strong>Are you sure you want to de-link your ORCid account from your PLOS
        account?</strong></p>
    <p class="messaging success no-display"><strong>Your account has been successfully de-linked</strong></p>
    <p class="messaging failure no-display"><strong>Something has gone wrong! would you like to try again</strong></p>
</div>

