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

package org.ambraproject.models;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.testng.annotations.Test;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class VolumeTest extends BaseHibernateTest {

  @Test
  public void testSaveBasicVolume() {
    Volume volume = new Volume("id:testVolumeToSave");
    volume.setTitle("test volume title");
    volume.setDisplayName("test volume display name");
    volume.setDescription("test volume description");
    volume.setImageUri("id:testImageUri");

    Serializable id = hibernateTemplate.save(volume);
    Volume storedVolume = (Volume) hibernateTemplate.get(Volume.class, id);
    assertNotNull(storedVolume, "didn't save volume");
    assertEquals(storedVolume, volume, "didn't store correct volume properties");
    assertNotNull(storedVolume.getCreated(), "Volume didn't get created date set");
  }

  @Test
  public void testSaveWithIssues() {
    Volume volume = new Volume("id:testVolumeWithIssue");

    final Issue issue1 = new Issue("id:testIssueForSaveVolume1");
    issue1.setDisplayName("test issue 1");
    issue1.setArticleDois(Arrays.asList("doi1", "doi2"));

    final Issue issue2 = new Issue("id:testIssueForSaveVolume2");
    issue2.setDisplayName("test issue 2");
    issue2.setArticleDois(Arrays.asList("doi3", "doi4"));

    volume.setIssues(Arrays.asList(issue1, issue2));

    final Serializable volumeId = hibernateTemplate.save(volume);

    //need to access issues in a session b/c they're lazy
    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Volume storedVolume = (Volume) session.get(Volume.class, volumeId);
        assertNotNull(storedVolume, "didn't store volume");
        assertEquals(storedVolume.getIssues().toArray(), new Issue[]{issue1, issue2}, "didn't store correct issues");
        return null;
      }
    });
  }

  @Test
  public void testUpdate() {
    final long testStart = Calendar.getInstance().getTimeInMillis();
    final Volume volume = new Volume("id:testVolumeToUpdate");
    volume.setDescription("old description");
    volume.setDisplayName("old display name");
    volume.setTitle("old title");

    final Serializable volumeId = hibernateTemplate.save(volume);

    volume.setDescription("new description");
    volume.setDisplayName("new display name");
    volume.setTitle("new title");
    volume.setIssues(Arrays.asList(
        new Issue("id:testIssueForUpdateVolume1"),
        new Issue("id:testIssueForUpdateVolume2"),
        new Issue("id:testIssueForUpdateVolume3")
    ));

    hibernateTemplate.update(volume);

    //need to access issues in a session b/c they're lazy
    hibernateTemplate.execute(new HibernateCallback() {
      @Override
      public Object doInHibernate(Session session) throws HibernateException, SQLException {
        Volume storedVolume = (Volume) session.get(Volume.class, volumeId);
        assertEquals(storedVolume, volume, "volume didn't get properties updated");
        assertEquals(storedVolume.getIssues().size(), volume.getIssues().size(), "volume didn't get issues added");
        assertNotNull(storedVolume.getLastModified(), "volume didn't get last modified date set");
        assertTrue(storedVolume.getLastModified().getTime() > testStart, "last modified wasn't after test start");
        assertTrue(storedVolume.getLastModified().getTime() > storedVolume.getCreated().getTime(),
            "last modified wasn't after created");
        return null;
      }
    });
  }

}
