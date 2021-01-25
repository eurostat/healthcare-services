package eu.europa.ec.eurostat.basicservices.education.cntr;

import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;

import eu.europa.ec.eurostat.basicservices.education.EducationUtil;
import eu.europa.ec.eurostat.basicservices.education.Validation;
import eu.europa.ec.eurostat.jgiscotools.io.CSVUtil;
import eu.europa.ec.eurostat.jgiscotools.io.geo.CRSUtil;
import eu.europa.ec.eurostat.jgiscotools.io.geo.GeoData;

public class PT {

	public static void main(String[] args) {
		System.out.println("Start");
		
		String inFilePath = EducationUtil.path + "PT/PT.csv";
		
		CSVFormat csvF = CSVFormat.DEFAULT.withFirstRecordAsHeader().withDelimiter(',');
		List<Map<String, String>> data = CSVUtil.load(inFilePath, csvF);
		System.out.println(data.size());

		//validation
		Validation.validate(true, data, "PT");

		//save
		System.out.println(data.size());
		CSVUtil.save(data, EducationUtil.path + "PT/PT.csv");
		GeoData.save(CSVUtil.CSVToFeatures(data, "lon", "lat"), EducationUtil.path + "PT/PT.gpkg", CRSUtil.getWGS_84_CRS());

		System.out.println("End");
	}

}
