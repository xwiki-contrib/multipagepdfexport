/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.pdf.multipageexport.internal;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.pdf.multipageexport.MultipagePdfExporter;
import org.xwiki.script.service.ScriptService;
import org.xwiki.security.authorization.AuthorizationManager;
import org.xwiki.security.authorization.Right;
import org.xwiki.stability.Unstable;

import com.xpn.xwiki.XWikiContext;

/**
 * Script service to expose the functionality of the pdf exporter implementation, which should also do some rights
 * verification before performing the actual operation.
 * 
 * @version $Id$
 */
@Component("pdfexporter")
@Singleton
public class MultipagePdfExporterService implements ScriptService
{
    /**
     * The execution, to get the context from it.
     */
    @Inject
    protected Execution execution;

    @Inject
    protected AuthorizationManager authorizationManager;

    @Inject
    @Named("xslfop")
    private MultipagePdfExporter pdfexporter;

    /**
     * Reference resolver for string representations of references.
     */
    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> currentDRResolver;

    /**
     * @param name the title of the document to export, the name of the file will be computed from this title replacing
     *            whitespace with underscores
     * @param docs the list of pages to export
     * @throws Exception if something goes wrong during the export
     */
    public void export(String name, List<String> docs) throws Exception
    {
        pdfexporter.export(name, getAccessibleDocuments(docs));
    }

    /**
     * @param name the title of the document to export, the name of the file will be computed from this title replacing
     *            whitespace with underscores
     * @param docs the list of pages to export
     * @param multiPageSequence whether each page should be exported in its own page sequence, with its own header and
     *            footer. If this parameter is true, all new documents will start on recto). If you need a different
     *            behaviour, use the 4 parameters version of this function (
     *            {@link #export(String, List, boolean, boolean)}).
     * @throws Exception if something goes wrong during the export
     */
    public void export(String name, List<String> docs, boolean multiPageSequence) throws Exception
    {
        pdfexporter.export(name, getAccessibleDocuments(docs), multiPageSequence);
    }

    /**
     * @param name the title of the document to export, the name of the file will be computed from this title replacing
     *            whitespace with underscores
     * @param docs the list of pages to export
     * @param multiPageSequence whether each page should be exported in its own page sequence, with its own header and
     *            footer. For the moment, this function needs a custom xhtml2fo.xsl, which can be passed by passing a
     *            the pdftemplate parameter
     * @param alwaysStartOnRecto used in conjunction with multiPageSequence, whether each page sequence (each wiki
     *            document) should always start on recto. If {@code multiPageSequence} is false, this parameter is
     *            ignored.
     * @throws Exception if something goes wrong during the export
     */
    public void export(String name, List<String> docs, boolean multiPageSequence, boolean alwaysStartOnRecto)
        throws Exception
    {
        pdfexporter.export(name, getAccessibleDocuments(docs), multiPageSequence, alwaysStartOnRecto);
    }

    /**
     * @param name the title of the document to export, the name of the file will be computed from this title replacing
     *            whitespace with underscores
     * @param docs the list of pages to export
     * @param multiPageSequence whether each page should be exported in its own page sequence, with its own header and
     *            footer. For the moment, this function needs a custom xhtml2fo.xsl, which can be passed by passing a
     *            the pdftemplate parameter
     * @param alwaysStartOnRecto used in conjunction with multiPageSequence, whether each page sequence (each wiki
     *            document) should always start on recto. If {@code multiPageSequence} is false, this parameter is
     *            ignored.
     * @param pageBreakBeforeDocument whether a page break should be added before a document nevertheless, even if it's
     *            not a multipage sequence (this parameter only has effect if the multipagesequence is on "false")
     * @throws Exception if something goes wrong during the export
     * @since 1.2
     */
    @Unstable
    public void export(String name, List<String> docs, boolean multiPageSequence, boolean alwaysStartOnRecto,
        boolean pageBreakBeforeDocument) throws Exception
    {
        pdfexporter.export(name, getAccessibleDocuments(docs), multiPageSequence, alwaysStartOnRecto,
            pageBreakBeforeDocument);
    }

    /**
     * Filters the passed list to exclude the documents on which current user does not have view right.
     *
     * @return the list of documents to which the current user has access, from the list of passed documents
     */
    protected List<String> getAccessibleDocuments(List<String> docs)
    {
        List<String> filteredDocs = new ArrayList<String>();
        XWikiContext xwikiContext = getXWikiContext();
        DocumentReference userReference = xwikiContext.getUserReference();
        for (String docName : docs) {
            if (authorizationManager.hasAccess(Right.VIEW, userReference, this.currentDRResolver.resolve(docName))) {
                filteredDocs.add(docName);
            }
        }

        return filteredDocs;
    }

    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) execution.getContext().getProperty("xwikicontext");
    }
}
