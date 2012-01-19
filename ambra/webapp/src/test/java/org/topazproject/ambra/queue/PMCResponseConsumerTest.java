/*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2009 by Topaz, Inc.
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

package org.topazproject.ambra.queue;

import org.testng.annotations.Test;
import org.topazproject.ambra.ApplicationException;
import org.topazproject.ambra.models.Syndication;
import org.topazproject.ambra.admin.service.SyndicationService;

import static org.easymock.EasyMock.*;

import java.net.URISyntaxException;

/**
 * @author Dragisa Krsmanovic
 */
public class PMCResponseConsumerTest {
  private static final String DOI = "info:doi/123.456/journal.plosone.1234";
  private static final String ERROR = "Test error";

  @Test
  public void okMessage() throws ApplicationException, URISyntaxException {
    PMCResponseConsumer consumer = new PMCResponseConsumer();
    SyndicationService service = createMock(SyndicationService.class);
    consumer.setSyndicationService(service);
    expect(service.asynchronousUpdateSyndication(eq(DOI), eq("PMC"), eq(Syndication.STATUS_SUCCESS), eq((String) null)))
        .andReturn(null)
        .once();
    replay(service);

    consumer.handleResponse(DOI + "|" + PMCResponseConsumer.OK);

    verify(service);
  }

  @Test
  public void failMessage() throws ApplicationException, URISyntaxException {
    PMCResponseConsumer consumer = new PMCResponseConsumer();
    SyndicationService service = createMock(SyndicationService.class);
    consumer.setSyndicationService(service);
    expect(service.asynchronousUpdateSyndication(eq(DOI), eq("PMC"), eq(Syndication.STATUS_FAILURE), eq(ERROR)))
        .andReturn(null)
        .once();
    replay(service);

    consumer.handleResponse(DOI + "|" + PMCResponseConsumer.FAILED + "|" + ERROR);

    verify(service);
  }

  @Test(expectedExceptions = {ApplicationException.class})
  public void emptyMessage() throws ApplicationException, URISyntaxException {
    PMCResponseConsumer consumer = new PMCResponseConsumer();
    SyndicationService service = createMock(SyndicationService.class);
    consumer.setSyndicationService(service);

    consumer.handleResponse("");

  }

  @Test(expectedExceptions = {ApplicationException.class})
  public void nullMessage() throws ApplicationException, URISyntaxException {
    PMCResponseConsumer consumer = new PMCResponseConsumer();
    SyndicationService service = createMock(SyndicationService.class);
    consumer.setSyndicationService(service);

    consumer.handleResponse(null);

  }

  @Test(expectedExceptions = {ApplicationException.class})
  public void missingResultCode() throws ApplicationException, URISyntaxException {
    PMCResponseConsumer consumer = new PMCResponseConsumer();
    SyndicationService service = createMock(SyndicationService.class);
    consumer.setSyndicationService(service);

    consumer.handleResponse(DOI + "|");

  }
}
