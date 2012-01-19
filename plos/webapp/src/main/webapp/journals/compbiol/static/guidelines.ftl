<#include "journal_include.ftl">
<#include "/static/cj_shared_blocks.ftl">

<!-- begin : main content -->
<div id="content" class="static">

<h1><em>${journal_name}</em> Guidelines for Authors</h1>

<h2>Contents:</h2>
<ol>
<li><a href="#about">About <em>${journal_name}</em></a></li>
<li><a href="#openaccess">Open Access</a></li>
<li><a href="#publicationcharges">Publication Charges</a></li>
<li><a href="#criteria">Criteria for Publication</a></li>
<li><a href="#editorial">Overview of the Editorial Process</a></li>
<li><a href="#presubs">Presubmission Inquiries</a></li>
<li><a href="#preparation">Preparation of Research Articles</a>
	<ol>
	<li><a href="#organization">Organization of the Manuscript</a></li>
	<li><a href="#nomenclature">Nomenclature</a></li>
	<li><a href="#accessionnumbers">Accession Numbers</a></li>
	<li><a href="#abbreviations">Abbreviations</a></li>
	</ol></li>
<li><a href="#submission">Materials Required for Manuscript Submission</a>
	<ol>
	<li><a href="#coverletter">Cover Letter</a></li>
	<li><a href="#authorstatus">Author Status</a></li>
	<li><a href="#financialdisclosure">Financial Disclosure</a></li>
	<li><a href="#competing">Competing Interests</a></li>
	<li><a href="#electronicformats">Electronic Formats</a>
		<ol>
		<li><a href="#textfiles">Manuscript and Tables File</a></li>
		<li><a href="#latex">LaTeX files</a></li>
		<li><a href="#figurefiles">Figure Files</a></li>
		<li><a href="#supportinginfo">Multimedia Files and Supporting Information</a></li>
		</ol></li>
	<li><a href="#readytosubmit">Ready to Submit Your Manuscript?</a></li>
	</ol></li>
<li><a href="#other">Other Types of Articles</a></li>
<li><a href="#production">Overview of the Production Process</a></li>
<li><a href="#embargoes">Blogs, Wikis, Embargoes, and the Media</a></li>
</ol>
 
<a name="about" id="about"></a>
<h2>1. About <em>PLoS Computational Biology</em></h2> 
<p><em>PLoS Computational Biology</em> features works of exceptional significance that further our understanding of living systems at all scales&#8212;from molecules and cells, to patient populations and ecosystems&#8212;through the application of computational methods. Readers include life and computational scientists, who can take the important findings presented here to the next level of discovery.</p>  

<p>Research articles that primarily describe new methods and offer only limited biological insight will be considered only in those exceptional instances in which the method is expected to significantly impact the field of computational biology, typically making substantial breakthroughs in areas of demonstrated importance. Research articles modeling aspects of biological systems should demonstrate both scientific novelty and profound new biological insights.  Research articles describing improved or routine methods, models, software, and databases will not be considered by <em>PLoS Computational Biology</em>, and may be more appropriate for <a href="http://www.plosone.org" title="PLoS ONE"><em>PLoS ONE</em></a>.</p> 

<p>Generally, reliability and significance of biological discovery are validated and enriched by experimental studies.  Experimental validation is not required for publication, however, nor does experimental validation of a modest biological discovery render a manuscript suitable for <em>PLoS Computational Biology</em>.</p>

<p>For all submissions, authors must clearly provide detail, data, and software to ensure readers' ability to reproduce the models, methods, and results.</p>  

 

<a name="openaccess"></a>
<h2>2. Open Access</h2>
${open_access}

<a name="publicationcharges"></a>
<h2>3. Publication Charges</h2>
${publication_charges}

<a name="criteria" id="criteria"></a>
<h2>4. Criteria for Publication</h2> 
<p>To be considered for publication in <em>PLoS Computational Biology</em>, any given manuscript must satisfy the following criteria:</p>
<ul> 
<li>Originality</li>
<li>High importance to researchers in the field</li>
<li>Significant biological insight and general interest to life scientists</li>
<li>Rigorous methodology</li>
<li>Substantial evidence for its conclusions</li> 
</ul>

<a name="editorial"></a>
<h2>5. Overview of the Editorial Process</h2>

<p>Our aim is to provide all authors with an efficient, courteous, and constructive editorial process. To achieve its required level of quality, <em>${journal_name}</em> is highly selective in the manuscripts that it publishes; rejection rates are high. To ensure the fairest and most objective decision-making, the editorial process is run as a partnership between the <em>PLoS Computational Biology</em> <a href="eic.action">Editor-in-Chief</a> (EIC), a team of Deputy Editors, and a group of academic experts who serve as <a href="edboard.action">Associate Editors</a> (AEs). These individuals are leaders in their fields and represent the full breadth of expertise in computational biology.</p>
 
<p>Submitted manuscripts are first reviewed by the EIC or one of the Deputy Editors, who may decide to reject the paper or send it on to an AE for further review.  The AE is most often a member of the <em>${journal_name}</em> Editorial Board, but occasionally a guest of the Board is invited to serve in this capacity. The AE evaluates the paper and decides whether it describes a sufficient body of work to support a major advance in a particular field. If so, the paper is sent out for external peer review, at which stage the technical and scientific merits of the work are carefully considered. Once the reviews have been received and considered by the editors, a decision letter to the corresponding author is drafted and sent.</p>

 
<p>The decision will be within one of the following categories:
 	<ul>
	<li>Reject</li> 
 	<li>Major revision</li> 
 	<li>Minor revision</li> 
 	<li>Accept</li> 
	</ul>
</p>
	


<a name="presubs"></a>
<h2>6. Presubmission Inquiries</h2>

${cj_presubmission}
 
<a name="preparation"></a>
<h2>7. Preparation of Research Articles</h2>
<p><em>${journal_name}</em> publishes original research that clearly demonstrates novelty, importance to a particular field, biological significance, and conclusions that are justified by the study.</p> 

<p>Our aim is to make the editorial process rigorous and consistent, and to offer the best possible support to our authors throughout this process. Authors are encouraged to decide how best to present their ideas, results, and conclusions. The writing style should be concise and accessible. Editors may make suggestions for how to achieve this, as well as suggestions for cuts or additions that could be made to the article to strengthen the argument.</p>

<p>Although we encourage submissions from around the globe, we require that manuscripts be submitted in English. As a step towards overcoming language barriers, we encourage authors fluent in other languages to provide copies of their full articles or abstracts in other languages. Translations should be submitted as supporting information and listed, together with other supporting information files, at the end of the article text.</p>


<a name="organization"></a>
<h3>Organization of the Manuscript</h3>
 
<p>Most articles published in <em>${journal_name}</em> are organized into the following sections: <a href="#title">Title</a>, <a href="#authors">Authors</a>, <a href="#affiliations">Affiliations</a>, <a href="#abstract">Abstract</a>, <a href="#authorsummary">Author Summary</a>, <a href="#introduction">Introduction</a>, <a href="#results">Results</a>, <a href="#discussion">Discussion</a>, <a href="#materials_methods">Materials and Methods</a>, <a href="#acknowledgments">Acknowledgments</a>, <a href="#references">References</a>, <a href="#figurelegends">Figure Legends</a>, and <a href="#tables">Tables</a>. Uniformity in format facilitates the experience of readers and users of the journal. To provide flexibility, however, authors are also able to include the Materials and Methods section before the Results section or before the Discussion section. Please also note that the Results and Discussion can be combined into one Results/Discussion section. Although we have no firm length restrictions for the entire manuscript, we urge authors to present and discuss their findings concisely.</p>


<a name="title"></a>
<h4>Title (150 characters or less)</h4>

<p>The title should be specific to the project, yet concise. It should be comprehensible to readers outside your field. Avoid specialist abbreviations, if possible. Titles should be presented in title case, meaning that all words except for prepositions, articles, and conjunctions should be capitalized. Please also provide a brief &quot;running head&quot; of no more than 30 characters.</p>
 
<p>Example: <br />
Detection of Specific Sequences among DNA Fragments Separated by Gel Electrophoresis. </p>


<a name="authors"></a><a name="affiliations"></a>
<h4>Authors and Affiliations</h4>

${authors_and_affiliations}

<a name="abstract"></a>
<h4>Abstract</h4>
<p>The abstract of the paper should be succinct; it should not exceed 250-300 words. Authors should mention the techniques used without going into methodological detail and should summarize the most important results. The abstract is conceptually divided into the following three sections: Background, Methodology/Principal Findings, and Conclusions/Significance. Please do not include any citations and avoid specialist abbreviations.</p>

<a name="authorsummary"></a>
<h4>Author Summary</h4>
<p>We ask that all authors of research articles include a 150-200 word non-technical summary of the work as part of the manuscript to immediately follow the abstract. This text is subject to editorial change, should be written in the first-person voice, and should be distinct from the scientific abstract. Aim to highlight where your work fits within a broader context; present the significance or possible implications of your work simply and objectively; and avoid the use of acronyms and complex terminology wherever possible. The goal is to make your findings accessible to a wide audience that includes both scientists and non-scientists. Authors may benefit from consulting with a science writer or press officer to ensure they effectively communicate their findings to a general audience. Examples are available at:<p>
<p><a href="http://www.ploscompbiol.org/article/info%3Adoi%2F10.1371%2Fjournal.pcbi.0030168#special">Systems Analysis of Chaperone Networks in the Malarial Parasite <em>Plasmodium falciparum</em></a></p>
<p><a href="http://www.ploscompbiol.org/article/info%3Adoi%2F10.1371%2Fjournal.pcbi.0030119#special">Protein&ndash;Protein Interaction Hotspots Carved into Sequences</a></p>
<p><a href="http://www.ploscompbiol.org/article/info%3Adoi%2F10.1371%2Fjournal.pcbi.0030161#special">Elucidating the Altered Transcriptional Programs in Breast Cancer using Independent Component Analysis</em></a></p>

<a name="introduction"></a>
<h4>Introduction</h4>

<p>The introduction should put the focus of the manuscript into a broader context. As you compose the introduction, think of readers who are not experts in this field. Include a brief review of the key literature. If there are relevant controversies or disagreements in the field, they should be mentioned so that a non-expert reader can delve into these issues further. The introduction should conclude with a brief statement of the overall aim of the experiments and a comment about whether that aim was achieved.</p> 

<a name="results"></a>
<h4>Results</h4>
<p>The results section should provide details of all of the experiments that are required to support the conclusions of the paper. There is no specific word limit for this section, but details of experiments that are peripheral to the main thrust of the article and that detract from the focus of the article should not be included. The section may be divided into subsections, each with a concise subheading. Large datasets, including raw data, should be submitted as supporting files; these are published online alongside the accepted article. The results section should be written in the past tense.</p> 

<a name="discussion"></a>
<h4>Discussion</h4>
<p>The discussion should spell out the major conclusions of the work along with some explanation or speculation on the significance of these conclusions. How do the conclusions affect the existing assumptions and models in the field? How can future research build on these observations? What are the key experiments that must be done? The discussion should be concise and tightly argued. The results and discussion may be combined into one section, if desired.</p>

<a name="materials_methods"></a> 
<h4>Materials and Methods (also called "Methods" or "Models")</h4>
<p>This section should provide enough detail for reproduction of the findings. Protocols for new methods should be included, but well-established protocols may simply be referenced. While we do encourage authors to submit all appendices, detailed protocols, or details of the algorithms for newer or less well-established methods, please do so as Supporting Information files. These are not included in the typeset manuscript, but are downloadable and fully searchable from the HTML version of the article. </p>

<a name="acknowledgments"></a>
<h4>Acknowledgments</h4> 

${cj_acknowledgements}


<a name="references"></a>
<h4>References</h4> 

${references}

<a name="figurelegends"></a>
<h4>Figure Legends</h4>
${figure_legends}

<a name="tables"></a>
<h4>Tables</h4> 
${cj_tables}

<a name="nomenclature"></a>
<h3>Nomenclature</h3>
${nomenclature}

<a name="accessionnumbers"></a>
<h3>Accession Numbers</h3>

${cj_accession_numbers}





<a name="abbreviations"></a>
<h3>Abbreviations</h3>

${cj_abbreviations}




<a name="submission"></a>
<h2>8. Materials Required for Manuscript Submission</h2>

${cj_materials}

<a name="other" id="other"></a>
<h2>9. Other Types of Articles</h2>


<p><em>PLoS Computational Biology</em> publishes a range of articles other than research articles. Contributions to the front section of the journal are subject to peer review. No publication charges apply. Prospective contributors are encouraged to review contributions in the respective sections of the journal before considering a submission.</p>

<p>Editorials &mdash; Editorials are written by <em>PLoS Computational Biology</em> editors. Guest editorials are invited by or at the discretion of the Editor-in-Chief. Editorials typically introduce new and changed features to the journal, discuss issues of professional development, and highlight developments in the field relevant to the readership.</p>

<p>Education articles &mdash; The goal of the <a href="http://collections.plos.org/ploscompbiol/index.php" title="View PLoS Computational Biology Education Collection">Education</a> section of <em>PLoS Computational Biology</em> is to provide both practical and background information on important computational methods and approaches used to investigate interesting biological problems. Contributions to the Education section can take several forms, including historical reviews and practical tutorials. Education articles should aim for 2000 and should not exceed 2500 words. The articles are generally invited, but unsolicited submissions will be considered and proceed at the discretion of the Education Editor.</p>

<p>Reviews &mdash; Reviews reflect rapidly advancing or topical areas in computational biology research that are of broad interest to the entire biology community and have not been covered significantly by other journals. A review should aim for 2500-3000 words and no more than 100 references and two or three  figures or other display items. Reviews are received both by invitation and as unsolicited submissions and are handled by the Reviews Editors.</p>

<p>Perspectives &mdash; Perspectives in <em>PLoS Computational Biology</em> typically reflect an author's viewpoint on a particular development in science and how, based on current knowledge of the field and the progress in it, this development evidences or can lead to change in how science is conducted or interpreted. Perspectives are intended to be more prospective than retrospective but require sufficient background to place the points made in context.  Perspectives are intended to invite debate and further comment as appropriate. The length is ideally around 2000 and limited to 2500 words. Suggestions for topics may be forwarded to ${email} and are usually handled by the Editor-in-Chief.</p> 

<p>Messages from ISCB &mdash; As the official journal of the International Society for Computational Biology (ISCB), <em>PLoS Computational Biology</em> publishes in this section short informational articles invited by the ISCB Editor as well as announcements from the Society.</p>

<a name="production"></a>
<h2>10. Overview of the Production Process</h2>

${cj_production_process}

<a name="embargoes"></a>
<h2>11. Blogs, Wikis, Embargoes, and the Media</h2>
<p>Authors are of course at liberty to present their findings at medical or scientific conferences ahead of publication. We recommend, however, that authors not contact the media or respond to such contact unless an article has been accepted for publication and an embargo date has been established. Respect for press embargoes will help to ensure that your work is reported accurately in the popular media. If a journalist has covered a piece of work ahead of publication, this will not affect consideration of the work for publication. See also our <a href="http://www.plos.org/journals/embargopolicy.html" title="embargo guidelines for journalists">embargo guidelines for journalists</a>.</p>

</div>
<!-- end : main contents -->