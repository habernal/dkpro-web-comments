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

package de.tudarmstadt.ukp.dkpro.web.comments.clustering.debatefiltering;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.Vector;

/**
 * (c) 2015 Ivan Habernal
 */
public class DebateRanker
        extends JCasConsumer_ImplBase
{
    File mainOutputDir;

    File domainTopicVectorModel;

    // loaded domain/topic vector map
    Map<String, Vector> domainTopicVectorMap;

    @SuppressWarnings("unchecked")
    @Override
    public void initialize(UimaContext context)
            throws ResourceInitializationException
    {
        // load domain topic map
        try {
            this.domainTopicVectorMap = (Map<String, Vector>) new ObjectInputStream(
                    new FileInputStream(domainTopicVectorModel)).readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            throw new ResourceInitializationException(e);
        }

        System.out.println(domainTopicVectorMap);
    }

    @Override
    public void process(JCas aJCas)
            throws AnalysisEngineProcessException
    {

    }
}
