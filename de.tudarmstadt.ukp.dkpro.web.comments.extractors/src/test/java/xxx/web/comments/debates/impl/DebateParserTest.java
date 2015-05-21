/*
 * Copyright 2015
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
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

import xxx.web.comments.createdebate.Argument;
import xxx.web.comments.createdebate.Debate;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * (c) 2015 Ivan Habernal
 */
public class DebateParserTest
{

    @Test
    public void testParseDebate()
            throws Exception
    {
        InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("7595___Is-religion-child-abuse.html.txt");

        Debate debate = new ConvinceMeNetParser().parseDebate(stream);

        assertNotNull(debate);
        assertNotNull(debate.getTitle());

        assertTrue(debate.getArgumentList().size() > 0);

        for (Argument argument : debate.getArgumentList()) {
            assertNotNull(argument.getStance());

            if (argument.getParentId() != null) {
                assertNotEquals(argument.getId(), argument.getParentId());
            }
        }
    }

    @Test
    public final void testEmptyDebate()
            throws Exception
    {
        InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("7732___Will-the-average-American-be-unable-to-f.html.txt");

        Debate debate = new ConvinceMeNetParser().parseDebate(stream);

        assertNotNull(debate);
        assertNotNull(debate.getTitle());
    }
}