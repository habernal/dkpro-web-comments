/*
 * Copyright 2015 XXX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xxx.web.comments.debates.impl;

import xxx.web.comments.createdebate.Debate;
import xxx.web.comments.createdebate.DebateSerializer;
import org.junit.Test;

import java.net.URL;

/**
 * (c) 2015 XXX
 */
public class CreateDebateComParserTest
{

    @Test
    public void testParseDebate()
            throws Exception
    {
        URL url = new URL(
                "http://www.createdebate.com/debate/show/Is_it_more_important_to_reduce_abortions_or_a_law_banning_it");
        Debate debate = new CreateDebateComParser().parseDebate(url.openStream());

        //        System.out.println(debate);

        System.out.println(DebateSerializer.serializeToXML(debate));
    }

    @Test
    public void testAllFiles()
            throws Exception
    {

    }
}