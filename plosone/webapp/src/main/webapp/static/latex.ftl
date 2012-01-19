
<!-- begin : main content -->
<div id="content" class="static">

<h1>Converting LaTeX files to Word or RTF format</h1>

<p>Our production system can only accept text files in Word or RTF format. Here are instructions for converting LaTeX files to Word or RTF format. For further information on preparing files for submission, read our <a href="guidelines.action" title="PLoS ONE | Author Guidelines">author guidelines</a>.</p>

<h2>LaTex Converters</h2>
<ul>
	<li><a href="http://www.chikrii.com/products/tex2word/about/">TeX2Word</a></li>
	<li><a href="http://sourceforge.net/projects/latex2rtf/">LaTeX2RTF</a></li>
	<li><a href="http://sourceforge.net/projects/texconverter/">TexConverter</a></li>
	<li><a href="http://www.ktalk.com/texport.html">TexPort</a></li>
	<li><a href="http://www.cambridgedocs.com/">CambridgeDocs</a></li>
</ul>
  
<p>Before converting your paper to Word or RTF:</p>

<ol>
	<li>Combine all sections of the file into a single file; do not submit separate .bbl or .bib files (see BiBTeX instructions below). Avoid use of personalized macros and shortcuts (e.g., \newcommand, \def). The TeX file must contain expanded versions of shortcuts and macros.</li>
	<li>Please comment out any graphics and table files (e.g., \includegraphics, \figbox); all images must be submitted as separate .eps or .tif files and all tables must be included in the text file, at the very end of the manuscript.</li>
	<li>If you use BiBTeX:
		<ul>
			<li>Run LaTeX on your LaTeX file.</li>
			<li>Run BiBTeX on your LaTeX file.</li>
			<li>Open the new .bbl file containing the reference list and copy all the contents into your LaTeX file after the acknowledgments section. </li>
			<li>Comment out the old \bibliographystyle and \bibliography commands. Run LaTeX on your new file before submitting. </li>
		</ul>
	</li>
	<li>The best way to view equations is with MathType (<a href="http://www.dessci.com/en/products/mathtype/trial.asp">download the free "trial" version of MathType</a>). However, you can use any equation editor as long as the equations open in Word. </li>
</ol>

<p>Once the file has been converted, please check the following:</p>
<ol>
	<li>Please make sure that footnotes are incorporated into the text.</li>
	<li>Check to see that references are complete and in order. References may have dropped out in the conversion if they were added later in your writing/editing process and not inserted correctly. Often the reference tag from the TeX file will remain at the end of each line as an artifact. All extraneous text should be deleted. It might look something like this: 
		<ul>
			<li>[45] Orr HA (2005) The Genetic Theory of Adaptation: A Brief History. Nature Reviews Genetics 6:119-127. orrNRG2005Key: orrNRG2005 Annotation</li>
		</ul>
	</li>
	<li>Please check all numbered citations throughout the paper (including figures and tables).</li>
	<li>Figure captions need to be listed at the end of the paper. Check that they are there.</li>
	<li>Scan the paper for misspellings. The conversion often takes spaces out of the text so that two words are pushed together (e.g., "about a" will become "abouta"). There shouldn't be too many of these, and they should be relatively easy to find.</li>
	<li>Please check all special characters to make sure that these are appearing as they should. If you need to change any of the characters, you can use the "insert symbol" found in the "Insert" menu.</li>
	<li>Numbered equations (equations set apart from paragraph text) should automatically appear in math boxes. If they are not appearing, please retype them (using an equation editor such as MathType). </li>
	<li>If your in-line (in-text) equations and mathematical expressions show up as graphic objects or MathType boxes instead of as word symbols and characters, they need to be retyped. (Mathematical expressions that have both superscript and subscript in the same character space cannot be typed in Word, so those can stay in a MathType box.) To check for MathType boxes, highlight all text and turn it red. Anything in the document that doesn't change color is not considered text, and needs to be manually retyped (with the exception of the numbered equations mentioned above).</li>
</ol>

</div>
<!-- end : main contents -->
