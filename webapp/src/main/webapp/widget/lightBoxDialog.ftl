<#--
/*
* $HeadURL$
* $Id$
* Copyright (c) 2006-2012 by Public Library of Science http://plos.org http://ambraproject.org
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
-->
<div dojoType="dijit.Dialog" id="LightBox">

    <!-- start main window wrapper-->
    <div class="figure-window-wrapper">

      <!--start film strip Wrapper-->
      <div class="figure-window-nav-wrapper">
        <div id="figure-window-nav"></div>
      </div><!--end figure-window-nav-wrapper-->

      <!-- start figure container-->
      <div class="figure-window-container">

        <!-- start/end figure viewer-->
        <div id="figure-window-viewer">
          <img id="figure-window-img" class="large"/>
        </div>

        <!-- start/end figure doi-->
        <div id="figure-window-doi"></div>

        <!--start figure title-box-->
        <div id="figure-window-title-box" class="figure-window-title-less">
          <div id="figure-window-title"></div>
          <div id="figure-window-more" class="figure-window-more" onclick="return ambra.lightBox.showMoreOrLess();"></div>
          <div id="figure-window-description"></div>
        </div><!--end figure title-box-->

        <!--start figure download-->
        <div class="figure-window-download">
          <ul>
            <li class="download icon"><strong>Download:</strong>
              <a id="figure-window-ppt" title="Click to download PowerPoint slide of this image">PowerPoint
                slide</a> |
              <a id="figure-window-large" title="Click to download a larger version of this image">larger image
                (<span id="figure-window-large-size"></span>)</a> |
              <a id="figure-window-tiff" title="Click to download a original version of this image">original image
                (<span id="figure-window-tiff-size"></span>)</a>
            </li>
          </ul>
        </div><!--end figure download-->

        <!--start/end of close, context, next and previous button-->
        <div class="figure-window-close" onclick="return ambra.lightBox.hide();"></div>
        <div class="figure-window-context" onclick="return ambra.lightBox.showInContext();"></div>
        <div id="figure-window-previous" onclick="return ambra.lightBox.showPrevious();"></div>
        <div id="figure-window-next" onclick="return ambra.lightBox.showNext();"></div>

      </div><!--end figure-window-container-->

    </div><!--end figure-window-wrapper-->

</div><!--end dijit.Dialog-->
