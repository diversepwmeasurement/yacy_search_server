/**
 *  linkScraperParser
 *  Copyright 2014 by Michael Peter Christen, mc@yacy.net, Frankfurt a. M., Germany
 *  First released 07.07.2014 at http://yacy.net
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file lgpl21.txt
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package net.yacy.document.parser;

import java.io.InputStream;
import java.util.Date;

import net.yacy.cora.document.id.AnchorURL;
import net.yacy.cora.document.id.MultiProtocolURL;
import net.yacy.document.AbstractParser;
import net.yacy.document.Document;
import net.yacy.document.Parser;

/**
 * This parser is used if we know that the content is text but the exact format is unknown.
 * We just expect that the content has some html links and we use it to discover those links.
 * Furthermore the URL is tokenized and added as the text part, just as the generic parser does.
 * This parser can i.e. be used for application source code and json files.
 */
public class linkScraperParser extends AbstractParser implements Parser {

    public linkScraperParser() {
        super("Link Scraper Parser");
        this.SUPPORTED_EXTENSIONS.add("js");
        this.SUPPORTED_EXTENSIONS.add("jsp");
        this.SUPPORTED_EXTENSIONS.add("json");
        this.SUPPORTED_EXTENSIONS.add("jsonp");
        this.SUPPORTED_EXTENSIONS.add("mf"); // metafont
        this.SUPPORTED_EXTENSIONS.add("pl"); // prolog
        this.SUPPORTED_EXTENSIONS.add("py"); // python
        this.SUPPORTED_EXTENSIONS.add("c"); // c
        this.SUPPORTED_EXTENSIONS.add("cpp"); // c++
        this.SUPPORTED_EXTENSIONS.add("h"); // header file (whatever)
        this.SUPPORTED_MIME_TYPES.add("application/json"); // correct for json
        this.SUPPORTED_MIME_TYPES.add("application/x-javascript"); // wrong, but used
        this.SUPPORTED_MIME_TYPES.add("text/javascript"); // correct for jsonp
        this.SUPPORTED_MIME_TYPES.add("text/x-javascript"); // wrong, but used
        this.SUPPORTED_MIME_TYPES.add("text/x-json"); // wrong, but used
        this.SUPPORTED_MIME_TYPES.add("text/sgml");
        this.SUPPORTED_MIME_TYPES.add("text/sgml");
    }
    @Override
    public Document[] parse(final AnchorURL location, final String mimeType,
            final String charset, final InputStream source)
            throws Parser.Failure, InterruptedException {
        
        Document[] htmlParserDocs = new htmlParser().parse(location, mimeType, charset, source);
        Document htmlParserDoc = htmlParserDocs == null ? null : Document.mergeDocuments(location, mimeType, htmlParserDocs);
        
        
        String filename = location.getFileName();
        final Document[] docs = new Document[]{new Document(
                location,
                mimeType,
                charset,
                this,
                null,
                null,
                singleList(filename.isEmpty() ? location.toTokens() : MultiProtocolURL.unescape(filename)), // title
                "", // author
                location.getHost(),
                null,
                null,
                0.0f, 0.0f,
                location.toTokens(),
                htmlParserDoc == null ? null : htmlParserDoc.getAnchors(),
                htmlParserDoc == null ? null : htmlParserDoc.getRSS(),
                htmlParserDoc == null ? null : htmlParserDoc.getImages(),
                false,
                new Date())};
        return docs;
    }
}