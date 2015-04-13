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

package de.tudarmstadt.ukp.dkpro.web.comments;

import de.tudarmstadt.ukp.dkpro.web.comments.createdebate.Argument;
import de.tudarmstadt.ukp.dkpro.web.comments.createdebate.Debate;
import de.tudarmstadt.ukp.dkpro.web.comments.createdebate.DebateSerializer;
import org.apache.commons.io.FileUtils;
import org.apache.uima.UimaContext;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.Progress;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * (c) 2015 Ivan Habernal
 */
public class CreateDebateArgumentReader
        extends JCasCollectionReader_ImplBase
{
    /**
     * Folder containing serialized XML files with {@link de.tudarmstadt.ukp.dkpro.web.comments.createdebate.Debate} object in each file
     */
    public static final String PARAM_SOURCE_LOCATION = "sourceLocation";
    @ConfigurationParameter(name = PARAM_SOURCE_LOCATION, mandatory = true) File sourceLocation;

    // list of all arguments
    private List<Argument> argumentList = new ArrayList<>();

    private Iterator<Argument> argumentIterator;

    @Override public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        super.initialize(context);

        File[] files = sourceLocation.listFiles(new FilenameFilter()
        {
            @Override public boolean accept(File dir, String name)
            {
                return name.endsWith(".xml");
            }
        });

        if (files != null) {
            for (File f : files) {
                try {
                    // load the debate
                    Debate debate = DebateSerializer
                            .deserializeFromXML(FileUtils.readFileToString(f));

                    // add all arguments
                    argumentList.addAll(debate.getArgumentList());
                }
                catch (IOException e) {
                    throw new ResourceInitializationException(e);
                }
            }
        }

        // set the iterator
        argumentIterator = argumentList.iterator();
    }

    @Override public void getNext(JCas jCas)
            throws IOException, CollectionException
    {
        // TODO continue...
    }

    @Override public boolean hasNext()
            throws IOException, CollectionException
    {
        return argumentIterator.hasNext();
    }

    @Override public Progress[] getProgress()
    {
        return new Progress[0];
    }
}
