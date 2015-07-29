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

import org.junit.Test;
import xxx.web.comments.createdebate.Debate;
import xxx.web.comments.debates.DebateParser;

import java.io.InputStream;

import static org.junit.Assert.assertFalse;

/**
 * @author Ivan Habernal
 */
public class ProConOrgParserTest
{

    @Test
    public void testDebate1()
            throws Exception
    {
        InputStream stream = this.getClass().getClassLoader()
                .getResourceAsStream("procon.org.marijuana.html");

        DebateParser parser = new ProConOrgParser();
        Debate debate = parser.parseDebate(stream);

        System.out.println(debate);

        assertFalse(debate.getTitle().isEmpty());

    }
}