/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2007-2012 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
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

package org.ambraproject.action.article;

import org.apache.poi.hslf.model.Hyperlink;
import org.apache.poi.hslf.model.Picture;
import org.apache.poi.hslf.model.Slide;
import org.apache.poi.hslf.model.TextBox;
import org.apache.poi.hslf.model.TextRun;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.im4java.core.ETOperation;
import org.im4java.core.ExiftoolCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.Operation;
import org.im4java.process.ArrayListOutputConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds a PowerPoint slide from an article figure, encapsulating and using information provided by {@link
 * org.ambraproject.service.article.ArticleAssetServiceImpl}.
 */
public class FigureSlideShow {
  private static final Logger log = LoggerFactory.getLogger(FigureSlideShow.class);

  private static final int SLIDE_WIDTH = 792; // width of a landscape letter page (11 inches * 72 points/inch)
  private static final int SLIDE_HEIGHT = 612; // height of a landscape letter page (8.5 inches * 72 points/inch)
  private static final int LOGO_MARGIN = 5;

  private final String title;
  private final String citation;
  private final String journalName;
  private final URL citationLink;
  private final byte[] logoImage;
  private final String logoPath;
  private final String imgAbsolutePath;

  public FigureSlideShow(String title, String citation, String journalName, URL citationLink, byte[] logoImage, String logoPath, String imgAbsolutePath) {
    this.title = title;
    this.citation = citation;
    this.journalName = journalName;
    this.citationLink = citationLink;
    this.logoImage = logoImage;
    this.logoPath = logoPath;
    this.imgAbsolutePath = imgAbsolutePath;
  }

  /**
   * Build a PowerPoint slide show, containing one slide of the figure with title and citation.
   *
   * @return the slide show, represented as an Apache HSLF object
   * @throws IOException
   */
  public SlideShow convert() throws IOException, IM4JavaException, InterruptedException {
    SlideShow slideShow = new SlideShow();
    slideShow.setPageSize(new Dimension(SLIDE_WIDTH, SLIDE_HEIGHT));

    Picture picture = setPictureBox(slideShow);
    Slide slide = slideShow.createSlide();
    slide.addShape(picture);
    if (!title.isEmpty()) {
      putTitleInSlide(slide);
    }

    // Create and set the citation
    TextBox pptCitationText = buildCitationBox(slideShow);
    slide.addShape(pptCitationText);

    putJournalLogoInSlide(slide);
    includeCopyRightInfoInSlide(slide);

    return slideShow;
  }

  private void includeCopyRightInfoInSlide(Slide slide) throws IOException, IM4JavaException, InterruptedException {

    //to retrieve the xmp data from image
    ExiftoolCmd exiftoolCmd = new ExiftoolCmd();
    Operation operation = new ETOperation().getTags("xmp:Rights").addImage();
    ArrayListOutputConsumer outputConsumer = new ArrayListOutputConsumer();
    exiftoolCmd.setOutputConsumer(outputConsumer);
    exiftoolCmd.run(operation, imgAbsolutePath);

    List<String> output = outputConsumer.getOutput();
    String ccText = null;
    if (output != null && output.size() > 0) {
      ccText = output.get(0).split(":")[1].trim();

      if (ccText != null && !ccText.equalsIgnoreCase("")) {
        TextBox pptCopyRightText = new TextBox();
        pptCopyRightText.setText(ccText);
        pptCopyRightText.setAnchor(new Rectangle(25, 587, 370, 13));
        RichTextRun rtr = pptCopyRightText.getTextRun().getRichTextRuns()[0];
        rtr.setFontSize(11);
        slide.addShape(pptCopyRightText);
      }
    } else {
      log.warn("Copyright information is not available for this image");
    }
  }

  /**
   * Write a *.ppt file representing this object to the stream. The stream is not closed by this method; it is the
   * invoker's responsibility to close the stream.
   *
   * @param buffer a ready stream
   * @throws IOException
   */
  public void write(OutputStream buffer) throws IOException, IM4JavaException, InterruptedException {
    SlideShow show = convert();
    show.write(buffer);
  }

  private void putTitleInSlide(Slide slide) {
    TextBox pptTitle = slide.addTitle();
    pptTitle.setAnchor(new Rectangle(28, 22, 737, 36));
    setRichText(pptTitle, title, AmbraStyle.TITLE);
  }

  private TextBox buildCitationBox(SlideShow slideShow) {
    TextBox pptCitationText = new TextBox();
    String linkText = citationLink.toString();
    pptCitationText.setAnchor(new Rectangle(35, 513, 723, 26));

    String text = citation + '\r' + linkText; // '\r' for a line break within the paragraph, preserving paragraph style
    setRichText(pptCitationText, text, AmbraStyle.CITATION);
    text = pptCitationText.getText(); // update with actual display text (no rich formatting tags)

    Hyperlink link = new Hyperlink();
    link.setAddress(linkText);
    link.setTitle("click to visit the article page");
    int linkId = slideShow.addHyperlink(link);
    int startIndex = text.indexOf(linkText);
    pptCitationText.setHyperlink(linkId, startIndex, startIndex + linkText.length());
    return pptCitationText;
  }

  private void putJournalLogoInSlide(Slide slide) throws IOException {
    File logoFile = new File(logoPath);
    if (logoFile.exists()) {
      InputStream input = null;
      Dimension dimension;
      try {
        input = new FileInputStream(logoFile);
        dimension = getImageDimension(input);
      } finally {
        if (input != null) {
          input.close();
        }
      }

      int logoIdx = slide.getSlideShow().addPicture(logoFile, Picture.PNG);
      Picture logo = new Picture(logoIdx);
      logo.setAnchor(new Rectangle(SLIDE_WIDTH - LOGO_MARGIN - dimension.width,
          SLIDE_HEIGHT - LOGO_MARGIN - dimension.height,
          dimension.width, dimension.height));
      slide.addShape(logo);
    } else {
      log.warn("Logo for journal " + journalName + " not found at " + logoPath);
    }
  }

  /**
   * set the dimension of picture box
   *
   * @param slideShow
   * @return
   * @throws IOException
   */
  private Picture setPictureBox(SlideShow slideShow) throws IOException {

    int index = slideShow.addPicture(logoImage, Picture.PNG);

    InputStream input = new ByteArrayInputStream(logoImage);
    Dimension dimension = getImageDimension(input);
    input.close();

    //get the image size
    int imW = dimension.width;
    int imH = dimension.height;

    //add the image to picture and add picture to shape
    Picture picture = new Picture(index);

    // Image box size 750x432 at xy=21,68

    if (imW > 0 && imH > 0) {
      double pgRatio = 750.0 / 432.0;
      double imRatio = (double) imW / (double) imH;
      if (pgRatio >= imRatio) {
        // horizontal center
        int mw = (int) ((double) imW * 432.0 / (double) imH);
        int mx = 21 + (750 - mw) / 2;

        picture.setAnchor(new Rectangle(mx, 68, mw, 432));
      } else {
        // vertical center
        int mh = (int) ((double) imH * 750.0 / (double) imW);
        int my = 68 + (432 - mh) / 2;

        picture.setAnchor(new Rectangle(21, my, 750, mh));
      }
    }

    return picture;
  }

  /**
   * get the image dimension
   *
   * @param input
   * @return
   */
  private static Dimension getImageDimension(InputStream input) {
    try {
      ImageInputStream in = ImageIO.createImageInputStream(input);
      try {
        Iterator readers = ImageIO.getImageReaders(in);
        if (readers.hasNext()) {
          ImageReader reader = (ImageReader) readers.next();
          try {
            reader.setInput(in);
            return new Dimension(reader.getWidth(0), reader.getHeight(0));
          } finally {
            reader.dispose();
          }
        }
      } finally {
        if (in != null)
          in.close();
      }
    } catch (Exception ex) {
      log.error("cannot get image dimension", ex);
    }
    return new Dimension(0, 0);
  }


  /*
   * Very narrow regex for matching the formatting tags expected to be in valid titles (which are specified at
   * <http://dtd.nlm.nih.gov/publishing/3.0/format3.ent>). Does NOT gracefully handle all XML syntax. In particular,
   * empty-element tags (for example, <br/>) are matched like opening tags (would be interpreted as <br>).
   *
   * Group 1 is "/" if the tag is closing and "" if it is opening. Group 2 is the tag name. Group 3 captures the
   * attributes if any, which are ignored. (Most formatting tags have no attributes, but the spec permits an "arrange"
   * attribute on <sup> and <sub>.)
   */
  private static final Pattern TAG_PATTERN = Pattern.compile("<(/?)([^<>]*?)(\\s+[^<>]*)?\\s*>");

  /**
   * Convert XML-formatted rich text and set it inside the text run. The text run's previous contents are overwritten.
   *
   * @param box         the HSLF object that will receive the formatted text
   * @param text        the text to format, with rich-text XML tags
   * @param globalStyle the font style to apply to the entire text run (underneath any tag formatting)
   */
  private static void setRichText(TextBox box, String text, RichTextModifier globalStyle) {
    TextRun textRun = box.getTextRun();

    Deque<RichTextModifier> tagStack = new ArrayDeque<RichTextModifier>(2);
    int cursor = 0;
    int length = text.length();
    boolean textHasBeenOverwritten = false;
    while (cursor < length) {
      Matcher m = TAG_PATTERN.matcher(text);
      boolean tagFound = m.find(cursor);

      // Put everything up to the tag (or, if no tag, the text's end) into the output
      int chunkEnd = tagFound ? m.start() : length;
      String chunk = text.substring(cursor, chunkEnd);

      if (!chunk.isEmpty()) {
        RichTextRun richText;
        if (!textHasBeenOverwritten) {
          /*
           * Blank out the preexisting text with the first non-empty chunk of new text. Calling textRun.setText("")
           * before entering the loop seems cleaner but messes with the TextRun's internal RichTextRun objects, so use
           * this workaround.
           */
          box.setText(chunk);
          richText = textRun.getRichTextRunAt(0);
          textHasBeenOverwritten = true;
        } else {
          richText = textRun.appendText(chunk);
        }

        // Apply the stack's pre-tag styles to the chunk
        globalStyle.modify(richText);
        Iterator<RichTextModifier> stackStyles = tagStack.descendingIterator();
        while (stackStyles.hasNext()) {
          stackStyles.next().modify(richText);
        }
      }

      // Process the tag in order to modify the stack for the next chunk
      if (tagFound) {
        String tagName = m.group(2);
        boolean isOpeningTag = m.group(1).isEmpty();
        readTag(tagStack, tagName, isOpeningTag);
      }

      // Iterate to next tag
      cursor = tagFound ? m.end() : length;
    }

    if (!tagStack.isEmpty()) {
      StringBuilder warning = new StringBuilder("Unclosed tags: ");
      for (RichTextModifier tag : tagStack) {
        warning.append('<').append(tag.getTag()).append("> ");
      }
      log.warn(warning.toString());
    }

    if (textRun.getRichTextRuns().length > 1) {
      /*
       * This is a kludge to cover up a bug when an exported file is opened in LibreOffice. If there is more than one
       * RichTextRun, the text of the last one is repeated several times (once for each RichTextRun). By making that
       * text a newline, we prevent it from changing the visual appearance of the output. A Microsoft Office user will
       * see only one extra newline (and only then if they click the text box to edit it).
       */
      RichTextRun dummyTerminator = textRun.appendText("\n");
      globalStyle.modify(dummyTerminator);
    }
  }

  /**
   * Read a rich text formatting tag and modify the stack of open tags appropriately. An opening tag will be matched to
   * a rich text style pushed onto the stack. A closing tag is expected to match the tag from the top of the stack; it
   * will pop the stack if it does. In case of an invalid tag, log a warning but continue.
   *
   * @param tagStack   the stack of tags that are open in the current state; open tags will be pushed onto the stack and
   *                   closed tags will be used to pop a tag from the top
   * @param tagName    the element name
   * @param openingTag {@code true} if the tag is opening; {@code false} if it is closing
   */
  private static void readTag(Deque<RichTextModifier> tagStack, String tagName, boolean openingTag) {
    if (openingTag) {
      RichTextModifier modifier = NlmTag.TAGS.get(tagName);
      if (modifier != null) {
        tagStack.push(modifier);
      } else {
        log.warn("Unrecognized formatting tag: <" + tagName + '>');
        tagStack.push(new NullTag(tagName)); // Put in a dummy so it can be closed later
      }
    } else if (tagStack.isEmpty()) {
      log.warn("Imbalanced closing tag: </" + tagName + '>');
    } else {
      RichTextModifier balancing = tagStack.peek();
      if (tagName.equals(balancing.getTag())) {
        tagStack.pop();
      } else {
        log.warn("Mismatched closing tag: </" + tagName + "> (expected </" + balancing.getTag() + ">)");
      }
    }
  }

  /**
   * A formatting style to apply to a RichTextRun.
   */
  private static interface RichTextModifier {
    /**
     * @return the XML tag label that means we should apply this style to the contents (may be {@code null} if this
     *         object will be used only as a {@code globalStyle} for {@link #setRichText})
     */
    public abstract String getTag();

    /**
     * Modify the run to match the formatting style represented by this object.
     *
     * @param rtr the text chunk to modify
     */
    public abstract void modify(RichTextRun rtr);
  }

  /**
   * Does nothing. For degrading gracefully in case of an unrecognized tag.
   */
  private static class NullTag implements RichTextModifier {
    private final String tag;

    public NullTag(String tag) {
      this.tag = tag;
    }

    @Override
    public String getTag() {
      return tag;
    }

    @Override
    public void modify(RichTextRun rtr) {
    }
  }


  private static enum AmbraStyle implements RichTextModifier {
    TITLE {
      @Override
      public void modify(RichTextRun rt) {
        rt.setFontSize(16);
        rt.setBold(true);
        rt.setAlignment(TextBox.AlignCenter);
      }
    },
    CITATION {
      @Override
      public void modify(RichTextRun rtr) {
        rtr.setFontSize(12);
      }
    };

    @Override
    public String getTag() {
      return null;
    }
  }

  /**
   * Formatting styles to go with markup specified by <http://dtd.nlm.nih.gov/publishing/3.0/format3.ent>
   */
  private static enum NlmTag implements RichTextModifier {
    BOLD("bold") {
      @Override
      public void modify(RichTextRun rtr) {
        rtr.setBold(true);
      }
    },
    ITALIC("italic") {
      @Override
      public void modify(RichTextRun rtr) {
        rtr.setItalic(true);
      }
    },
    STRIKETHROUGH("strike") {
      @Override
      public void modify(RichTextRun rtr) {
        rtr.setStrikethrough(true);
      }
    },
    SUBSCRIPT("sub") {
      @Override
      public void modify(RichTextRun rtr) {
        rtr.setSuperscript(-SCRIPT_PROPORTION);
      }
    },
    SUPERSCRIPT("sup") {
      @Override
      public void modify(RichTextRun rtr) {
        rtr.setSuperscript(SCRIPT_PROPORTION);
      }
    },
    UNDERLINE("underline") {
      @Override
      public void modify(RichTextRun rtr) {
        rtr.setUnderlined(true);
      }
    };
    /*
     * Tags specified for NLM format but not supported here:
     * monospace, roman, sans-serif, sc, overline
     */

    public static final Map<String, RichTextModifier> TAGS;

    static {
      Map<String, RichTextModifier> tags = new HashMap<String, RichTextModifier>((int) (values().length / 0.75) + 1);
      for (RichTextModifier tag : values()) {
        tags.put(tag.getTag(), tag);
      }
      TAGS = Collections.unmodifiableMap(tags);
    }

    /**
     * Percentage of font size for superscripts and subscripts to appear, expressed from 0 to 100.
     *
     * @see org.apache.poi.hslf.usermodel.RichTextRun#setSuperscript(int)
     */
    private static final int SCRIPT_PROPORTION = 33;


    private final String tag;

    private NlmTag(String tag) {
      this.tag = tag;
    }

    @Override
    public String getTag() {
      return tag;
    }

    @Override
    public String toString() {
      return '<' + tag + '>';
    }
  }

}
