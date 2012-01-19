#
#$HeadURL:
#$Id:
#
# Copyright (c) 2006-2011 by Public Library of Science
#
# http://plos.org
# http://ambraproject.org
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

To build and run:

First update the hostnames and authentication information in the following config files:

main/resources/hibernate.cfg.xml.example and save as hibernate.cfg.xml
main/resources/topaz.properties.example and save as topaz.properties

mysql -u root --execute "source create_db.sql;"
mvn package appassembler:assemble
bash target/appassembler/bin/migrate

Migrate the database (This adds indexes and populates new tables with data model changes

mysql -u root --execute "source migrate_db.sql;"

To run tests (after the migration):
mvn test

