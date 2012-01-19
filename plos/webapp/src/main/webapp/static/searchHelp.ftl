<!-- begin : main content -->
<div id="content" class="static"><h1>Advanced Search Tips</h1>

<h2>Searching by Author</h2>
<p>Enter an author's first name, last name or complete name.  For example, to search for documents that contain the author "Albert Einstein", you can enter "Albert", "Einstein" or "Albert Einstein".</p>
<p>Search for multiple authors by using the "Add another author..." link to create more author name fields. Please enter only one name per field.</p>
<p>To remove an author field, use the "Remove" link next to the field. You may also leave one or more fields blank.</p>

<h2>Editing Your Query</h2>
<p>You can use the following tips to edit an existing query that appears on a search results page or use them to conduct an advanced search query within the search field that appears at the top of every page.</p>
<p>Jump to Topic:</p>
<ul>
    <li><a href="#general">General</a></li>
    <li><a href="#wildcards">Wildcards</a></li>
    <li><a href="#fuzzy">Fuzzy Searches</a></li>
    <li><a href="#proximity">Proximity Searches</a></li>
    <li><a href="#range">Range Searches</a></li>
    <li><a href="#boolean">Boolean Operators</a></li>
    <ul>
        <li><a href="#boolean_or">OR</a></li>
        <li><a href="#boolean_and">AND</a></li>
        <li><a href="#boolean_plus">+</a></li>
        <li><a href="#boolean_not">NOT</a></li>
        <li><a href="#boolean_minus">-</a></li>
    </ul>
    <li><a href="#grouping">Grouping</a></li>
    <li><a href="#fieldgrouping">Field Grouping</a></li>
    <li><a href="#special">Escaping Special Characters</a></li>
</ul>

<h2><a name="general"></a>General</h2>
    <p>A search query is broken up into terms and operators.  There are two types of terms:  Single Terms and Phrases.  A Single Term is a single word such as <strong>global</strong> or <strong>climate</strong>. A Phrase is a group of words surrounded by double quotes such as <strong>global warming</strong>.  Multiple terms can be combined together with Boolean operators to form a more complex query (see below).</p>
    
<h2><a name="wildcards"></a>Wildcards</h2>

    <p>Advanced Search supports single and multiple character wildcard searches within single terms (not within phrase queries).</p>

    <p>The single character wildcard (<strong>?</strong>) search looks for terms that match that the search term with the <em>single character</em> replaced. For example:

    <blockquote>
        <p>
            <strong>te?t</strong> will return <strong>text,</strong> <strong>test</strong>, <strong>tent</strong>, etc.
        </p>
    </blockquote>
    

    <p>Right truncation involves placing the wildcard on the right-hand-side of the search string. For example:</p>

    <blockquote>
        <p>
            <strong>clea?</strong> will return <strong>clear,</strong> <strong>clean</strong>, <strong>cleat</strong>, etc.
        </p>
    </blockquote>

    <p>This wildcard can be used multiple times within the same string:</p>

    <blockquote>
        <p>
            <strong>ra?n?</strong> will return <strong>rains,</strong> <strong>rainy</strong>, etc.
        </p>
    </blockquote>

    <p>The multiple character wildcard (<strong>*</strong>) search looks for <em>0 or more characters</em>. Right truncation involves placing the wildcard on the right-hand-side of the search string. For example</p>

    <blockquote>
        <p>
            <strong>radio*</strong> will return <strong>radiometer,</strong> <strong>radiometric</strong>, <strong>radiosonde</strong>, etc.
        </p>
    </blockquote>

    <p><strong>Note: You cannot use a * or ? symbol as the first character of a search.</strong></p>

<h2><a name="fuzzy"></a>Fuzzy Searches</h2>

    <p>Advanced Search supports fuzzy searches based on the <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein Distance</a>, or Edit Distance algorithm. To do a fuzzy search use the tilde symbol ("~") at the end of a Single word Term. For example, to search for a term similar in spelling to <strong>roam</strong> use the fuzzy search: </p>

    <blockquote>
        <p>
        <strong>roam~</strong> will return terms like <strong>foam</strong> and <strong>roams</strong>.
        </p>
    </blockquote>

<h2><a name="proximity"></a>Proximity Searches</h2>

    <p>Advanced Search supports finding words that are a within a specific distance away. To do a proximity search, use the tilde symbol ("~") at the end of a Phrase. For example, to search for <strong>vaccine</strong> and <strong>disease</strong> within 10 words of each other in a document, use the search: </p>

    <blockquote>
        <p>
        <strong>vaccine disease</strong>~10
        </p>
    </blockquote>

<h2><a name="range"></a>Range Searches</h2>

    <p>Range Queries allow one to match documents whose field(s) values are between the lower and upper bound specified by the Range Query.  Range Queries can be inclusive or exclusive of the upper and lower bounds. Inclusive range queries are denoted by square brackets ("[" ... "]").  Exclusive range queries are denoted by curly brackets ("{"..."}").  Sorting is done lexicographically.</p>

    <blockquote>
        <p>
        date: <strong>[20080216 TO 20080316]</strong>
        </p>
    </blockquote>

    <p>This will find documents whose published date has values between <strong>20080216</strong> and <strong>20080316</strong>, inclusive. Note that Range Queries are not reserved for date fields.  You could also use range queries with non-date fields:</p>

    <blockquote>
        <p>
        title: <strong>{Aida TO Carmen}</strong>
        </p>
    </blockquote>

    <p>This will find all documents whose titles are between <strong>Aida</strong> and <strong>Carmen</strong>, but not including <strong>Aida</strong> and <strong>Carmen</strong>.</p>
    
<h2><a name="boolean"></a>Boolean Operators</h2>

    <p>Boolean operators allow terms to be combined through logic operators.  Advanced Search supports AND, "+", OR, NOT and "-" as Boolean operators.<p>
    
    <p><strong>Note: Boolean operators must be ALL CAPS.</strong></p>

    <a name="boolean_or"></a>
    <h4>OR</h4>
        
        <p>The OR operator is the default conjunction operator. This means that if there is no Boolean operator between two terms, the OR operator is used. The OR operator links two terms and finds a matching document if either of the terms exist in a document. The symbol "||" can be used in place of the word OR.</p>

        <p>To search for documents that contain either <strong>global warming</strong> or just <strong>warming</strong> use the query:</p>

            <blockquote>
                <p>
                <strong>global warming</strong>
                </p>
            </blockquote>

        <p>or</p>

            <blockquote>
                <p>
                 <strong>global warming</strong> OR <strong>global</strong>
                </p>
            </blockquote>
        
    <a name="boolean_and"></a>
    <h4>AND</h4>

        <p>The AND operator matches documents where both terms exist anywhere in the text of a single document. The symbol "&amp;&amp"; can be used in place of the word AND.</p>

        <p>To search for documents that contain <strong>global warming</strong> and <strong>warming climate</strong> use the query: </p>

            <blockquote>
                <p>
                 <strong>global warming</strong> AND <strong>warming climate</strong>
                </p>
            </blockquote>

    <a name="boolean_plus"></a>
    <h4>+</h4>

        <p>The "+" or required operator requires that the term after the "+" symbol exist somewhere in the field of a single document.</p>

        <p>To search for documents that must contain <strong>global</strong> and may contain <strong>climate</strong> use the query:</p>

            <blockquote>
                <p>
                 +<strong>global climate</strong>
                </p>
            </blockquote>

    <a name="boolean_not"></a>
    <h4>NOT</h4>

        <p>The NOT operator excludes documents that contain the term after NOT. The symbol "!" can be used in place of the word NOT.</p>

        <p>To search for documents that contain <strong>global warming</strong> but not <strong>climate</strong> use the query: </p>

            <blockquote>
                <p>
                <strong>global warming</strong> NOT <strong>climate</strong>
                </p>
            </blockquote>

        <p>Note: The NOT operator cannot be used with just one term. For example, the following search will return no results:</p>

            <blockquote>
                <p>
                 NOT <strong>global warming</strong>
                </p>
            </blockquote>

    <a name="boolean_minus"></a>
    <h4>-</h4>

        <p>The "-" or prohibit operator excludes documents that contain the term after the "-" symbol.</p>
        
        <p>To search for documents that contain <strong>global warming</strong> but not <strong>warming climate</strong> use the query: </p>
        
            <blockquote>
                <p>
                 <strong>global warming</strong> -<strong>warming climate</strong>
                </p>
            </blockquote>

<h2><a name="grouping"></a>Grouping</h2>

    <p>Advanced Search supports using parentheses to group terms to form sub queries. This can be very useful if you want to control the boolean logic for a query.</p>

    <p>To search for either <strong>global</strong> or <strong>warming</strong> and <strong>climate</strong> use the query:</p>
        
        <blockquote>
            <p>
           (<strong>global</strong> OR <strong>warming</strong>) AND <strong>climate</strong>
            </p>
        </blockquote>

    <p>This eliminates any confusion and makes sure you that <strong>climate</strong> must exist and either term <strong>global</strong> or <strong>warming</strong> may exist.</p>

<h2><a name="fieldgrouping"></a>Field Grouping</h2>

    <p>Advanced Search supports using parentheses to group multiple terms to a single field.</p>

    <p>To search for a title that contains both the word <strong>warming</strong> and the phrase <strong>climate change</strong> use the query:</p>

        <blockquote>
            <p>
            title: (+<strong>warming</strong> +<strong>climate change</strong>)
            </p>
        </blockquote>

<h2><a name="special"></a>Escaping Special Characters</h2>

    <p>Advanced Search supports escaping special characters that are part of the query syntax. The current list special characters are</p>
    
    <blockquote>
        <p>
        <strong>+ - &amp;&amp; || ! ( ) { } [ ] ^ " ~ * ? : \</strong>
        </p>
    </blockquote>

    <p>To escape these character use the \ before the character. For example, to search for <strong>(1+1):2</strong> use the query:</p>
 
        <blockquote>
            <p>
            <strong>\(1\+1\)\:2</strong>
            </p>
        </blockquote>
</div>
<!-- end : main contents -->
