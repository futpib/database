/**

Copyright (C) SYSTAP, LLC 2006-2012.  All rights reserved.

Contact:
     SYSTAP, LLC
     4501 Tower Road
     Greensboro, NC 27410
     licenses@bigdata.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
/*
 * Created on Feb 27, 2012
 */

package com.bigdata.rdf;

import info.aduna.lang.service.ServiceRegistry;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.ServiceLoader;

import org.apache.log4j.Logger;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.resultio.TupleQueryResultParserRegistry;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParserFactory;
import org.openrdf.rio.RDFParserRegistry;
import org.openrdf.rio.RDFWriterRegistry;

import com.bigdata.rdf.model.StatementEnum;
import com.bigdata.rdf.rio.json.BigdataSPARQLResultsJSONParserFactory;
import com.bigdata.rdf.rio.json.BigdataSPARQLResultsJSONParserForConstructFactory;
import com.bigdata.rdf.rio.json.BigdataSPARQLResultsJSONWriterFactory;
import com.bigdata.rdf.rio.json.BigdataSPARQLResultsJSONWriterForConstructFactory;
import com.bigdata.rdf.rio.ntriples.BigdataNTriplesParserFactory;
import com.bigdata.rdf.rio.turtle.BigdataTurtleParserFactory;
import com.bigdata.rdf.rio.turtle.BigdataTurtleWriterFactory;

/**
 * This static class provides a hook which allows the replacement of services
 * registered via the openrdf {@link ServiceRegistry} pattern which makes use of
 * the same underlying pattern which is disclosed by the {@link ServiceLoader}.
 * <p>
 * The {@link ServiceRegistry} pattern provides a declarative association
 * between a service and a service provider. The service declarations are
 * located in <code>META-INF/services/</code> packages. Each file in such a
 * package name the service interface and the contents of the file name the
 * service provider(s). The set of all such service provides located in all such
 * service packages is registered for a given service interface. For openrdf,
 * the service provides are initially associated with a <i>key</i>, such as an
 * {@link RDFFormat}, {@link QueryLanguage}, etc.
 * <p>
 * However, this service registration pattern does not support the specification
 * of a <em>preferred</em> service provider. In the context of multiple service
 * providers for the same <i>key</i> and service interface, there is no way to
 * control which service provider will remain in the {@link ServiceRegistry}.
 * <p>
 * This effects things such as the bigdata extension for the RDF/XML parser
 * which adds support for SIDs mode interchange and the interchange of
 * {@link StatementEnum} metadata.
 * <p>
 * This class is used to "hook" the various service registeries and force the
 * use of the bigdata extension when it adds semantics not present in the base
 * service provider implementation. For such "hooked" services providers, the
 * service registry pattern using <code>META-INF/services</code> is not
 * manditory, but following the pattern is never the less recommended.
 * 
 * @see <a href="https://sourceforge.net/apps/trac/bigdata/ticket/439">Class
 *      loader problems </a>
 * 
 * @author <a href="mailto:thompsonbry@users.sourceforge.net">Bryan Thompson</a>
 */
public class ServiceProviderHook {

	private static final Logger log = Logger.getLogger(ServiceProviderHook.class);
	
    static private boolean loaded = false;
    static {

    	TURTLE_RDR = new RDFFormat("Turtle-RDR",
				Arrays.asList("application/x-turtle-RDR"),
				Charset.forName("UTF-8"), Arrays.asList("ttlx"), true, false);
		
    	NTRIPLES_RDR = new RDFFormat("N-Triples-RDR",
				"application/x-n-triples-RDR", Charset.forName("US-ASCII"),
				"ntx", false, false);

        forceLoad();
    }

    /**
     * The extension MIME type for RDR data interchange using the RDR extension
     * of TURTLE.
     * 
	 * @see <a href="http://trac.bigdata.com/ticket/1038" >RDR RDF parsers not
	 *      always discovered </a>
	 * @see http://wiki.bigdata.com/wiki/index.php/Reification_Done_Right
	 */
	public static final RDFFormat TURTLE_RDR; //RDFFormat.TURTLE; 

    /**
     * The extension MIME type for RDR data interchange using the RDR extension
     * of N-TRIPLES.
     * 
	 * @see <a href="http://trac.bigdata.com/ticket/1038" >RDR RDF parsers not
	 *      always discovered </a>
	 * @see http://wiki.bigdata.com/wiki/index.php/Reification_Done_Right
	 */
	public static final RDFFormat NTRIPLES_RDR; //RDFFormat.NTRIPLES;

    /**
	 * This hook may be used to force the load of this class so it can ensure
	 * that the bigdata version of a service provider is used instead of the
	 * openrdf version. This is NOT optional. Without this hook, we do not have
	 * control over which version is resolved last in the processed
	 * <code>META-INF/services</code> files.
	 * <p>
	 * Note: We need to use a synchronized pattern in order to ensure that any
	 * threads contending for this method awaits its completion. It would not
	 * be enough for a thread to know that the method was running. The thread
	 * needs to wait until the method is done.
	 */
    synchronized static public void forceLoad() {
        
        if (loaded)
            return;

        log.warn("Running.");

		/*
		 * Force load of the openrdf service registry before we load our own
		 * classes.
		 */
		{
			final String className = "info.aduna.lang.service.ServiceRegistry";
			try {
				Class.forName(className);
			} catch (ClassNotFoundException ex) {
				log.error(ex);
			}
		}
        
		{
			{
				for (RDFFormat f : RDFFormat.values()) {
					log.warn("before: " + f);
				}
			}
			RDFFormat.register(NTRIPLES_RDR);
			RDFFormat.register(TURTLE_RDR);
			{
				for (RDFFormat f : RDFFormat.values()) {
					log.warn("after: " + f);
				}
			}
		}
		
		/*
         * Force the class loader to resolve the register, which will cause it
         * to be populated with the service provides as declared in the various
         * META-INF/services/serviceIface files.
         * 
         * Once that step is known to be complete, we override the service
         * provider for RDF/XML.
         */
        {

            final RDFParserRegistry r = RDFParserRegistry.getInstance();
			{
				for (RDFParserFactory f : r.getAll()) {
					log.warn("before: " + f);
				}
			}

//			final RDFParserFactory oldValue = RDFParserRegistry.getInstance().get(RDFFormat.NTRIPLES);
			
//			log.warn("Old value: " + oldValue + " for format=" + RDFFormat.NTRIPLES);
            
            // RDR-enabled
			r.add(new BigdataNTriplesParserFactory());
			assert r.has(new BigdataNTriplesParserFactory().getRDFFormat());
            
            // RDR-enabled
            r.add(new BigdataTurtleParserFactory());
            assert r.has(new BigdataTurtleParserFactory().getRDFFormat());
            
			{
				for (RDFParserFactory f : r.getAll()) {
					log.warn("after: " + f);
				}
			}
			
            /*
             * Allows parsing of JSON SPARQL Results with an {s,p,o,[c]} header.
             * RDR-enabled.
             */
            r.add(new BigdataSPARQLResultsJSONParserForConstructFactory());
            
        }
        
        {
        	
        	final TupleQueryResultWriterRegistry r = TupleQueryResultWriterRegistry.getInstance();

        	// add our custom RDR-enabled JSON writer
        	r.add(new BigdataSPARQLResultsJSONWriterFactory());
        	
        }

        {
            
            final TupleQueryResultParserRegistry r = TupleQueryResultParserRegistry.getInstance();

            // add our custom RDR-enabled JSON parser
            r.add(new BigdataSPARQLResultsJSONParserFactory());
            
        }

        // Ditto, but for the writer.
        {
            final RDFWriterRegistry r = RDFWriterRegistry.getInstance();

//            r.add(new BigdataRDFXMLWriterFactory());
            
            // RDR-enabled
            r.add(new BigdataTurtleWriterFactory());

            // RDR-enabled
            r.add(new BigdataSPARQLResultsJSONWriterForConstructFactory());
            
        }

//        {
//            final PropertiesParserRegistry r = PropertiesParserRegistry.getInstance();
//            
//            r.add(new PropertiesXMLParserFactory());
//            
//            r.add(new PropertiesTextParserFactory());
//            
//        }
//        
//        {
//            final PropertiesWriterRegistry r = PropertiesWriterRegistry.getInstance();
//            
//            r.add(new PropertiesXMLWriterFactory());
//            
//            r.add(new PropertiesTextWriterFactory());
//            
//        }

        loaded = true;
        
    }

//    private static void registerFactory
    
}
