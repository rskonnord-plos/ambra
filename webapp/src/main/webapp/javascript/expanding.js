/**********************************************
 *
 * Expanding Textareas
 * Brian Grinstead
 * 2013-02-07 09:55
 * 1.0?
 * https://github.com/bgrins/ExpandingTextareas
 *
 **********************************************/

/*
MIT License

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
    distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

    The above copyright notice and this permission notice shall be
included in all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
    EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

(function (factory) {
  // Add jQuery via AMD registration or browser globals
  if (typeof define === 'function' && define.amd) {
    define([ 'jquery' ], factory);
  }
  else {
    factory(jQuery);
  }
}(function ($) {
  $.expandingTextarea = $.extend({
    autoInitialize:true,
    initialSelector:"textarea.expanding",
    opts:{
      resize:function () {
      }
    }
  }, $.expandingTextarea || {});

  var cloneCSSProperties = [
    'lineHeight', 'textDecoration', 'letterSpacing',
    'fontSize', 'fontFamily', 'fontStyle',
    'fontWeight', 'textTransform', 'textAlign',
    'direction', 'wordSpacing', 'fontSizeAdjust',
    'wordWrap', 'word-break',
    'borderLeftWidth', 'borderRightWidth',
    'borderTopWidth', 'borderBottomWidth',
    'paddingLeft', 'paddingRight',
    'paddingTop', 'paddingBottom',
    'marginLeft', 'marginRight',
    'marginTop', 'marginBottom',
    'boxSizing', 'webkitBoxSizing', 'mozBoxSizing', 'msBoxSizing'
  ];

  var textareaCSS = {
    position:"absolute",
    height:"100%",
    resize:"none"
  };

  var preCSS = {
    visibility:"hidden",
    border:"0 solid",
    whiteSpace:"pre-wrap"
  };

  var containerCSS = {
    position:"relative"
  };

  function resize() {
    $(this).closest('.expandingText').find("div").text(this.value.replace(/\r\n/g, "\n") + ' ');
    $(this).trigger("resize.expanding");
  }

  $.fn.expandingTextarea = function (o) {

    var opts = $.extend({ }, $.expandingTextarea.opts, o);

    if (o === "resize") {
      return this.trigger("input.expanding");
    }

    if (o === "destroy") {
      this.filter(".expanding-init").each(function () {
        var textarea = $(this).removeClass('expanding-init');
        var container = textarea.closest('.expandingText');

        container.before(textarea).remove();
        textarea
            .attr('style', textarea.data('expanding-styles') || '')
            .removeData('expanding-styles');
      });

      return this;
    }

    this.filter("textarea").not(".expanding-init").addClass("expanding-init").each(function () {
      var textarea = $(this);

      textarea.wrap("<div class='expandingText'></div>");
      textarea.after("<pre class='textareaClone'><div></div></pre>");

      var container = textarea.parent().css(containerCSS);
      var pre = container.find("pre").css(preCSS);

      // Store the original styles in case of destroying.
      textarea.data('expanding-styles', textarea.attr('style'));
      textarea.css(textareaCSS);

      $.each(cloneCSSProperties, function (i, p) {
        var val = textarea.css(p);

        // Only set if different to prevent overriding percentage css values.
        if (pre.css(p) !== val) {
          pre.css(p, val);
        }
      });

      textarea.bind("input.expanding propertychange.expanding keyup.expanding", resize);
      resize.apply(this);

      if (opts.resize) {
        textarea.bind("resize.expanding", opts.resize);
      }
    });

    return this;
  };

  $(function () {
    if ($.expandingTextarea.autoInitialize) {
      $($.expandingTextarea.initialSelector).expandingTextarea();
    }
  });

}));