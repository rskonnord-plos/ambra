<!-- begin : main content -->
<div id="content" class="static"><h1>Help Using Advanced Search</h1>

<ul>
    <li><a href="#general">General</a></li>
    <li><a href="#wildcards">Wildcards</a></li>
    <li><a href="#fuzzy">Fuzzy Searches</a></li>
    <li><a href="#proximity">Proximity Searches</a></li>
    <li><a href="#range">Range Searches</a></li>

    <li><a href="#boolean">Boolean Operators</a></li>
    <ul>
        <li><a href="#boolean_and">OR</a></li>
        <li><a href="#boolean_or">AND</a></li>
        <li><a href="#boolean_plus">+</a></li>
        <li><a href="#boolean_not">NOT</a></li>

        <li><a href="#boolean-minus">-</a></li>
    </ul>
    <li><a href="#grouping">Grouping</a></li>
    <li><a href="#fieldgrouping">Field Grouping</a></li>
    <li><a href="#special">Escaping Special Characters</a></li>
</ul>

<h2><a name="general"></a>General</h2>

    <p>A search query is broken up into terms and operators.  There are two types of terms:  Single Terms and Phrases.  A Single Term is a single word such as "global" or "climate". A Phrase is a group of words surrounded by double quotes such as "global warming".  Multiple terms can be combined together with Boolean operators to form a more complex query (see below).</p>
    
    <p>Enter a term (or terms) you would like to <a href="search/advancedSearch.action">search</a> with. You may enter terms in more than one field (such as Title: "Reaction" and Last Name: "Miller"), and/or several terms in each field. Pull down the menu next to each field and select how the multiple terms you entered in that field should be combined while searching. To narrow your search, enter terms in more than one field. For example:</p>

    <form action="">
        <table>
            <tbody>

                <tr>
                    <td align="right"><strong>Title</strong></td>
                    <td>
                        <select name="title_type">
                            <option value="all">with all the words</option>
                            <option value="any">with any of the words</option>

                            <option value="phrase" selected="selected">with the exact phrase</option>

                        </select>
                        <input size="20" name="title" value="Chain Reaction"/>
                    </td>
                </tr>
                <tr>
                    <td align="right"><strong>Author First Name</strong></td>
                    <td>

                        <select name="fname_type">

                            <option value="all">with all the words</option>
                            <option value="any" selected="selected">with any of the words</option>
                            <option value="phrase">with the exact phrase</option>
                        </select>
                        <input size="20" name="fname" value="William Will Bill"/>
                    </td>

                </tr>

                <tr>
                    <td align="right"><strong>Author Last Name</strong></td>
                    <td>
                        <select name="surname_type">
                            <option value="all" selected="selected">with all the words</option>
                            <option value="any">with any of the words</option>

                            <option value="phrase">with the exact phrase</option>

                        </select>
                        <input size="20" name="surname" value="Miller"/>
                    </td>
                </tr>
            </tbody>
        </table>
    </form>

<h2><a name="wildcards"></a>Wildcards</h2>

    <p>Advanced Search supports single and multiple character wildcard searches within single terms (not within phrase queries).</p>

    <p>The single character wildcard (<strong>?</strong>) search looks for terms that match that with the <em>single character</em> replaced. For example:

    <blockquote>
        <p>
            <strong>te?t</strong> will return <strong>text,</strong> <strong>test</strong>, <strong>tent</strong>, etc.
        </p>

    </blockquote>
    

    <p>Right truncation involves placing the wildcard on the right-hand-side of the search string. For example</p>

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

    <p>The multiple character wildcard (<strong>*</strong>) searches looks for <em>0 or more characters</em>. Right truncation involves placing the wildcard on the right-hand-side of the search string. For example</p>

    <blockquote>
        <p>
            <strong>radio*</strong> will return <strong>radiometer,</strong> <strong>radiometric</strong>, <strong>radiosonde</strong>, etc.
        </p>

    </blockquote>

    <p><strong>Note: You cannot use a * or ? symbol as the first character of a search.</strong></p>

<h2><a name="fuzzy"></a>Fuzzy Searches</h2>

    <p>Advanced Search supports fuzzy searches based on the <a href="http://en.wikipedia.org/wiki/Levenshtein_distance">Levenshtein Distance</a>, or Edit Distance algorithm. To do a fuzzy search use the tilde, "~", symbol at the end of a Single word Term. For example to search for a term similar in spelling to "roam" use the fuzzy search: </p>

    <pre>roam~</pre>

    <p>This search will find terms like foam and roams.</p>

<h2><a name="proximity"></a>Proximity Searches</h2>

    <p>Advanced Search supports finding words are a within a specific distance away. To do a proximity search use the tilde, "~", symbol at the end of a Phrase. For example to search for a "vaccine" and "disease" within 10 words of each other in a document use the search: </p>

    <pre>"vaccine disease"~10</pre>

<h2><a name="range"></a>Range Searches</h2>

    <p>Range Queries allow one to match documents whose field(s) values are between the lower and upper bound specified by the Range Query.  Range Queries can be inclusive or exclusive of the upper and lower bounds. Sorting is done lexicographically.</p>

    <pre>date:[20080216 TO 20080316]</pre>

    <p>This will find documents whose published date has values between 20080216 and 20080316, inclusive. Note that Range Queries are not reserved for date fields.  You could also use range queries with non-date fields:</p>

    <pre>title:{Aida TO Carmen}</pre>

    <p>This will find all documents whose titles are between Aida and Carmen, but not including Aida and Carmen.</p>
    
    <p>Inclusive range queries are denoted by square brackets.  Exclusive range queries are denoted by curly brackets.</p>

<h2><a name="boolean"></a>Boolean Operators</h2>

    <p>Boolean operators allow terms to be combined through logic operators.  Advanced Search supports AND, "+", OR, NOT and "-" as Boolean operators.<p>
    
    <p><strong>Note: Boolean operators must be ALL CAPS.</strong></p>

    <a name="boolean_or"></a>
    <h4>OR</h4>
        
        <p>The OR operator is the default conjunction operator. This means that if there is no Boolean operator between two terms, the OR operator is used. The OR operator links two terms and finds a matching document if either of the terms exist in a document. This is equivalent to a union using sets. The symbol "||" can be used in place of the word OR.</p>

        <p>To search for documents that contain either "global warming" or just "warming" use the query:</p>

            <pre>" global" warming</pre>

        <p>or</p>

            <pre>"global warming" OR global</pre>
        
    <a name="boolean_and"></a>
    <h4>AND</h4>

        <p>The AND operator matches documents where both terms exist anywhere in the text of a single document. This is equivalent to an intersection using sets. The symbol "&amp;&amp"; can be used in place of the word AND.</p>

        <p>To search for documents that contain "global warming" and "warming climate" use the query: </p>

            <pre>"global warming" AND "warming climate"</pre>

    <a name="boolean_plus"></a>
    <h4>+</h4>

        <p>The "+" or required operator requires that the term after the "+" symbol exist somewhere in a the field of a single document.</p>

        <p>To search for documents that must contain "global" and may contain "climate" use the query:</p>

            <pre>+global climate</pre>

    <a name="boolean_not"></a>
    <h4>NOT</h4>

        <p>The NOT operator excludes documents that contain the term after NOT. This is equivalent to a difference using sets. The symbol "!" can be used in place of the word NOT.</p>

        <p>To search for documents that contain "global warming" but not "warming climate" use the query: </p>

            <pre>"global warming" NOT "warming climate"</pre>

        <p>Note: The NOT operator cannot be used with just one term. For example, the following search will return no results:</p>

            <pre>NOT "global warming"</pre>

    <a name="boolean_minus"></a>
    <h4>-</h4>

        <p>The "-" or prohibit operator excludes documents that contain the term after the "-" symbol.</p>
        
        <p>To search for documents that contain "global warming" but not "warming climate" use the query: </p>
        
            <pre>"global warming" -"warming climate"</pre>

<h2><a name="grouping"></a>Grouping</h2>

    <p>Advanced Search supports using parentheses to group clauses to form sub queries. This can be very useful if you want to control the boolean logic for a query.</p>

    <p>To search for either "global" or "warming" and "climate" use the query:</p>
        
        <pre >(global OR warming) AND climate</pre>

    <p>This eliminates any confusion and makes sure you that "climate" must exist and either term "global" or "warming" may exist.</p>

<h2><a name="fieldgrouping"></a>Field Grouping</h2>

    <p>Advanced Search supports using parentheses to group multiple clauses to a single field.</p>

    <p>To search for a title that contains both the word "return" and the phrase "climate change" use the query:</p>

        <pre >title:(+return +"climate change")</pre>


<h2><a name="reserved"></a>Reserved Characters</h2>

    <p>The table below lists the reserved characters that must be preceded by a backslash ( <strong>\</strong> ) when you use them in a search term. For example:</p>

    <blockquote>
        <p>

            The phrase <strong>%10</strong> (which uses the reserved character <strong>%</strong>) must be entered as <strong>\%10</strong>

        </p>
    </blockquote>

<h2><a name="special"></a>Special Characters</h2>

    <p>Advanced Search supports escaping special characters that are part of the query syntax. The current list special characters are</p>
    
    <p>+ - &amp;&amp; || ! ( ) { } [ ] ^ " ~ * ? : \</p>

    <p>To escape these character use the \ before the character. For example to search for (1+1):2 use the query:</p>
 
        <pre>\(1\+1\)\:2</pre>
</div>
<!-- end : main contents -->