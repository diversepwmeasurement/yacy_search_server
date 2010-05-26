/**
 *  RSSReader
 *  Copyright 2007 by Michael Peter Christen
 *  First released 16.7.2007 at http://yacy.net
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program in the file COPYING.LESSER.
 *  If not, see <http://www.gnu.org/licenses/>.
 */

package net.yacy.cora.document;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class RSSReader extends DefaultHandler {
    
    // class variables
    private RSSMessage item;
    private final StringBuilder buffer;
    private boolean parsingChannel, parsingImage, parsingItem;
    private final RSSFeed theChannel;
    
    public RSSReader() {
        theChannel = new RSSFeed();
        buffer = new StringBuilder();
        item = null;
        parsingChannel = false;
        parsingImage = false;
        parsingItem = false;
    }
    
    public RSSReader(final String path) throws IOException {
        this();
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            final SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(path, this);
        } catch (SAXException e) {
            throw new IOException (e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new IOException (e.getMessage());
        }
    }
    
    public RSSReader(final InputStream stream) throws IOException {
        this();
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            final SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(stream, this);
        } catch (SAXException e) {
            throw new IOException (e.getMessage());
        } catch (ParserConfigurationException e) {
            throw new IOException (e.getMessage());
        }
    }
    
    public static RSSReader parse(final byte[] a) throws IOException {

        // check integrity of array
        if ((a == null) || (a.length == 0)) {
            throw new IOException("response=null");
        }
        if (a.length < 100) {
            throw new IOException("response=" + new String(a));
        }
        if (!equals(a, "<?xml".getBytes())) {
            throw new IOException("response does not contain valid xml");
        }
        final String end = new String(a, a.length - 10, 10);
        if (end.indexOf("rss") < 0) {
            throw new IOException("response incomplete");
        }
        
        // make input stream
        final ByteArrayInputStream bais = new ByteArrayInputStream(a);
        
        // parse stream
        RSSReader reader = null;
        try {
            reader = new RSSReader(bais);
        } catch (final Exception e) {
            throw new IOException("parse exception: " + e.getMessage(), e);
        }
        try { bais.close(); } catch (final IOException e) {}
        return reader;
    }
    
    private final static boolean equals(final byte[] buffer, final byte[] pattern) {
        // compares two byte arrays: true, if pattern appears completely at offset position
        if (buffer.length < pattern.length) return false;
        for (int i = 0; i < pattern.length; i++) if (buffer[i] != pattern[i]) return false;
        return true;
    }

    @Override
    public void startElement(final String uri, final String name, final String tag, final Attributes atts) throws SAXException {
        if ("channel".equals(tag)) {
            item = new RSSMessage();
            parsingChannel = true;
        } else if ("item".equals(tag)) {
            item = new RSSMessage();
            parsingItem = true;
        } else if ("image".equals(tag)) {
            parsingImage = true;
        }
    }

    @Override
    public void endElement(final String uri, final String name, final String tag) {
        if (tag == null) return;
        if ("channel".equals(tag)) {
            parsingChannel = false;
            theChannel.setChannel(item);
        } else if ("item".equals(tag)) {
            theChannel.addMessage(item);
            parsingItem = false;
        } else if ("image".equals(tag)) {
            parsingImage = false;
        } else if ((parsingImage) && (parsingChannel)) {
            final String value = buffer.toString().trim();
            buffer.setLength(0);
            if ("url".equals(tag)) theChannel.setImage(value);
        } else if (parsingItem)  {
            final String value = buffer.toString().trim();
            buffer.setLength(0);
            if (RSSMessage.tags.contains(tag)) item.setValue(tag, value);
        } else if (parsingChannel) {
            final String value = buffer.toString().trim();
            buffer.setLength(0);
            if (RSSMessage.tags.contains(tag)) item.setValue(tag, value);
        }
    }

    @Override
    public void characters(final char ch[], final int start, final int length) {
        if (parsingItem || parsingChannel) {
            buffer.append(ch, start, length);
        }
    }
    
    public RSSFeed getFeed() {
        return theChannel;
    }

}