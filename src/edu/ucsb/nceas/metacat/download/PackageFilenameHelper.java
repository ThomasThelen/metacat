package edu.ucsb.nceas.metacat.download;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class for generating user-friendly filenames for data package downloads.
 * 
 * This class provides methods to query Solr for dataset titles and sanitize them
 * for use as filenames. When a title is unavailable or the query fails, the system
 * falls back to using the PID-based naming convention.
 * 
 */
public class PackageFilenameHelper {
    
    private static Log logMetacat = LogFactory.getLog(PackageFilenameHelper.class);
    
    /**
     * Get a user-friendly filename for a package based on its title from Solr.
     * Returns a filename using the pid if the title isn't found.
     * 
     * @param pid the package identifier (resource map PID)
     * @return sanitized filename with .zip extension
     */
    public static String getPackageFilename(String pid) {
        try {
            String title = queryTitleFromSolr(pid);
            if (title != null && !title.trim().isEmpty()) {
                return sanitizeTitleForFilename(title) + ".zip";
            }
        } catch (Exception e) {
            logMetacat.warn("Could not get title for package " + pid + ", using PID: " + e.getMessage());
        }
        return sanitizeTitleForFilename(pid.replaceAll("\\W", "_")); // Fallback to PID
    }
    
    /**
     * Query Solr index for the title field of a package.
     * 
     * @param pid the package identifier
     * @return the title string, or null if not found
     */
    private static String queryTitleFromSolr(String pid) {
        try {
            // Build Solr query to get the title field for this PID
            // Query: id:"pid" with fields limited to title
            org.apache.solr.client.solrj.SolrClient solrClient = 
                edu.ucsb.nceas.metacat.common.SolrServerFactory.createSolrServer();
            
            org.apache.solr.client.solrj.SolrQuery query = 
                new org.apache.solr.client.solrj.SolrQuery();
            query.setQuery("id:\"" + pid + "\"");
            query.setFields("title");
            query.setRows(1);
            
            org.apache.solr.client.solrj.response.QueryResponse response = 
                solrClient.query(query);
            org.apache.solr.common.SolrDocumentList results = response.getResults();
            
            if (results != null && results.size() > 0) {
                org.apache.solr.common.SolrDocument doc = results.get(0);
                Object titleObj = doc.getFieldValue("title");
                if (titleObj != null) {
                    return titleObj.toString();
                }
            }
        } catch (Exception e) {
            logMetacat.warn("Error querying Solr for title of " + pid + ": " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Sanitize a title string for use as a filename.
     * Replaces invalid characters, truncates length.
     * 
     * @param title the raw title string
     * @return sanitized filename (without extension), or null if title is invalid
     */
    public static String sanitizeTitleForFilename(String title) {
        if (title == null || title.trim().isEmpty()) {
            return null;
        }
        // Maximum filename length
        int maxLength = 100;
        
        // 1. Remove/replace invalid filename characters with underscores
        String safe = title.replaceAll("[^a-zA-Z0-9\\-\\.]", "_");
        
        if (safe.length() > maxLength) {
            safe = safe.substring(0, maxLength);
        }
        return safe;
    }
}
