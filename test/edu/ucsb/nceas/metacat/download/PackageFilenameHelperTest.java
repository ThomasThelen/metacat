package edu.ucsb.nceas.metacat.download;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * Unit tests for PackageFilenameHelper using JUnit 4 parameterized tests.
 * 
 * Tests the sanitization logic and filename generation for package downloads.
 * 
 * Note: Solr integration tests should be done separately as integration tests,
 * not unit tests.
 * 
 * @author Thomas Thelen
 */
@RunWith(Parameterized.class)
public class PackageFilenameHelperTest {

    private String input;
    private String expectedOutput;
    private String testDescription;

    public PackageFilenameHelperTest(String input, String expectedOutput, String testDescription) {
        this.input = input;
        this.expectedOutput = expectedOutput;
        this.testDescription = testDescription;
    }

    @Parameters(name = "{2}: ''{0}'' -> ''{1}''")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            // Valid inputs
            {"Arctic Research Data 2020", "Arctic_Research_Data_2020", "Simple title"},
            {"Data from: Climate change effects (2015-2020)", "Data_from_Climate_change_effects_2015-2020", "Special characters"},
            {"Arctic ice/sediment samples #1 & #2", "Arctic_ice_sediment_samples_1_2", "Path characters"},
            {"Data\\File:Name*Test?\"More<Data>Here|End", "Data_File_Name_Test_More_Data_Here_End", "Invalid filename chars"},
            {"Data   from    Study   2020", "Data_from_Study_2020", "Multiple spaces"},
            {"Test Data___", "Test_Data", "Trailing underscores"},
            {"Données écologiques 2020", "Données_écologiques_2020", "Unicode characters"},
            {"Arctic RESEARCH data 2020", "Arctic_RESEARCH_data_2020", "Mixed case"},
            {"Study-2015-2020-Results", "Study-2015-2020-Results", "Numbers and hyphens"},
            {"Data (Version 2)", "Data_Version_2", "Parentheses"},
            {"Test::Data--File", "Test_Data-File", "Consecutive special chars"},
            {"Test Dataset", "Test_Dataset", "No extension added"},
            {null, null, "Null input"},
            {"", null, "Empty string"},
            {"   ", null, "Whitespace only"},
            {":::***???", null, "Only special chars"},
            {"___", null, "Only underscores"}
        });
    }

    @Test
    public void testSanitizeTitleForFilename() {
        String result = PackageFilenameHelper.sanitizeTitleForFilename(input);
        assertEquals(testDescription + " failed", expectedOutput, result);
    }

    @RunWith(Parameterized.class)
    public static class AdditionalTests {
        
        @Test
        public void testSanitizeTitleForFilename_LongTitle() {
            // Create a 150 character title
            String title = "This is a very long title that exceeds one hundred characters and should be truncated " +
                          "to exactly one hundred characters for filesystem safety";
            String result = PackageFilenameHelper.sanitizeTitleForFilename(title);
            
            assertNotNull(result);
            assertEquals("Long titles should be truncated to 100 characters", 100, result.length());
            assertTrue(result.startsWith("This_is_a_very_long_title"));
        }

        @Test
        public void testSanitizeTitleForFilename_RealWorldExample() {
            String title = "Riparian Shrub expansion: soil analysis data, microbial communities " +
                          "and microarray gene data from the North Slope of Alaska, 2016";
            String result = PackageFilenameHelper.sanitizeTitleForFilename(title);
            
            assertNotNull(result);
            assertTrue(result.startsWith("Riparian_Shrub_expansion_soil_analysis"));
            assertFalse("Should not contain colons", result.contains(":"));
            assertFalse("Should not contain commas", result.contains(","));
            assertEquals("Should be truncated to 100 characters", 100, result.length());
        }

        @Test
        public void testSanitizeTitleForFilename_99Chars() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 99; i++) sb.append("a");
            String title = sb.toString();
            
            String result = PackageFilenameHelper.sanitizeTitleForFilename(title);
            assertEquals(99, result.length());
        }

        @Test
        public void testSanitizeTitleForFilename_100Chars() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 100; i++) sb.append("a");
            String title = sb.toString();
            
            String result = PackageFilenameHelper.sanitizeTitleForFilename(title);
            assertEquals(100, result.length());
        }

        @Test
        public void testSanitizeTitleForFilename_101Chars() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 101; i++) sb.append("a");
            String title = sb.toString();
            
            String result = PackageFilenameHelper.sanitizeTitleForFilename(title);
            assertEquals("Should be truncated to 100", 100, result.length());
        }

        @Test
        public void testSanitizeTitleForFilename_NoExtensionAdded() {
            String title = "Test Dataset";
            String result = PackageFilenameHelper.sanitizeTitleForFilename(title);
            assertEquals("Test_Dataset", result);
            assertFalse("Should not end with .zip", result.endsWith(".zip"));
        }

        @Test
        public void testSanitizeTitleForFilename_NoDoubleUnderscores() {
            String title = "Test::Data";
            String result = PackageFilenameHelper.sanitizeTitleForFilename(title);
            assertNotNull(result);
            assertFalse("Should not contain double underscores", result.contains("__"));
        }
    }
}
