/*
 * $HeadURL::                                                                            $
 * $Id$
 *
 * Copyright (c) 2006-2008 by Topaz, Inc.
 * http://topazproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
var container;
var topBanner;

function globalInit() {
  if (BrowserDetect.browser == "Explorer") {
    container = dojo.byId("container");
    topBanner = dojo.byId("topBanner");
    
    if (container) {
      topaz.domUtil.setContainerWidth(container, 675, 940);
      
      dojo.event.connect(window, "onresize", function() {
          setTimeout("topaz.domUtil.setContainerWidth(container, 675, 940)", 100);
        }
      );
    }
    
    if (topBanner) {
      topaz.domUtil.setContainerWidth(topBanner, 942, 944);
      
      dojo.event.connect(window, "onresize", function() {
          setTimeout("topaz.domUtil.setContainerWidth(topBanner, 942, 944)", 100);
        }
      );
    }
  }
}

dojo.addOnLoad(globalInit);
