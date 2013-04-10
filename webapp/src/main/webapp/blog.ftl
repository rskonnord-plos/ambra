<#macro blogFeed type>
	<ul class="articles">
<#if type = 'plos'>
		<li><a href="http://feeds.plos.org/~r/plos/blogs/blogosphere/~3/fMP-TMkYqfk/">Method and madness in science and art</a></li>
		<li><a href="http://feeds.plos.org/~r/plos/blogs/blogosphere/~3/B6AwQJpJ_5I/">Bye-bye Google Reader</a></li>
		<li><a href="http://feeds.plos.org/~r/plos/blogs/blogosphere/~3/BLrWk6h7w3U/">Do Cats With FIV Foretell HIV’s Future?</a></li>
		<li><a href="http://blogs.plos.org/plos/2013/02/plos-commends-white-house-directive-on-open-access/">PLOS Commends White House Directive on Open Access</a></li>
		<li><a href="http://blogs.plos.org/plos/2013/02/plos-welcomes-introduction-of-us-legislation-for-open-access/">PLOS Welcomes Introduction of US Legislation for Open Access</a></li>
<#elseif type = 'som'>
		<li><a href="http://blogs.plos.org/speakingofmedicine/2013/03/12/neglected-tropical-diseases-and-the-new-pope/">Neglected Tropical Diseases and the new Pope</a></li>
		<li><a href="http://blogs.plos.org/speakingofmedicine/2013/03/06/giving-back-to-research-participants/">Giving back to research participants</a></li>
		<li><a href="http://blogs.plos.org/speakingofmedicine/2013/03/01/violence-against-individuals-with-disability/">Violence Against Individuals with Disability</a></li>		
		<li><a href="http://blogs.plos.org/speakingofmedicine/2013/02/22/buruli-ulcer-in-a-brave-new-world/">Buruli Ulcer in a Brave New World</a></li>	
		<li><a href="http://blogs.plos.org/speakingofmedicine/2013/02/06/making-progress-in-prognostic-research/">Making PROGRESS in Prognostic Research</a></li>			
		
		
<#elseif type = 'everyone'>
		<li><a href="http://blogs.plos.org/everyone/2013/03/11/forest-elephants-and-whitetip-sharks-plos-one-papers-at-cites/">Forest elephants and whitetip sharks: PLOS ONE papers at CITES</a></li>
		<li><a href="http://blogs.plos.org/everyone/2013/03/08/dressed-up-donkey-discovered-at-religious-burial-site/">Dressed-Up Donkey Discovered at Religious Burial Site</a></li>
		<li><a href="http://blogs.plos.org/everyone/2013/03/06/marine-microbes-make-musical-waves/">Marine microbes make musical waves</a></li>
<#elseif type = 'ntd'>
		<li><a href="http://blogs.plos.org/speakingofmedicine/2013/02/22/buruli-ulcer-in-a-brave-new-world/">Buruli Ulcer in a Brave New World</a></li>
		<li><a href="http://blogs.plos.org/speakingofmedicine/2013/01/25/plos-ntds-fifth-anniversary-qa/">PLOS NTDs Fifth Anniversary Q&A</a></li>
		<li><a href="http://blogs.plos.org/speakingofmedicine/2012/12/06/stuck-in-a-time-warp-who-brokered-global-rd-action-plan-shelved/">Stuck in a time warp: WHO-brokered global R&D action plan shelved</a></li>
<#elseif type = 'biologue'>
		<li><a href="http://blogs.plos.org/biologue/2013/03/13/positive-charges-clog-the-ribosomal-tunnel/">Positive Charges Clog the Ribosomal Tunnel</a></li>
		<li><a href="http://blogs.plos.org/biologue/2013/03/11/calling-all-researchers-in-tuberculosis/">Calling all researchers in tuberculosis</a></li>
		<li><a href="http://blogs.plos.org/biologue/2013/02/28/molecular-movements-drive-bacterial-signaling/">Molecular movements drive bacterial signaling</a></li>
</#if>
	</ul>
</#macro>
