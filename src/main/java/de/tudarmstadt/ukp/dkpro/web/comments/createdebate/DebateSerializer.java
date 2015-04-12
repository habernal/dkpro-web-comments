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

package de.tudarmstadt.ukp.dkpro.web.comments.createdebate;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * (c) 2015 Ivan Habernal
 */
public class DebateSerializer
{
    public static String serializeToXML(Debate debate)
    {
        XStream xstream = new XStream(new StaxDriver());
        return xstream.toXML(debate);
    }

    public static Debate deserializeFromXML(String xml)
    {
        XStream xstream = new XStream(new StaxDriver());
        return (Debate) xstream.fromXML(xml);
    }
}
