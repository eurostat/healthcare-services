package eu.europa.ec.eurostat.basicservices.healthcare.cntr;

import java.util.ArrayList;
import java.util.Map;

import eu.europa.ec.eurostat.basicservices.ServicesGeocoding;
import eu.europa.ec.eurostat.basicservices.healthcare.HealthcareUtil;
import eu.europa.ec.eurostat.basicservices.healthcare.Validation;
import eu.europa.ec.eurostat.jgiscotools.geocoding.BingGeocoder;
import eu.europa.ec.eurostat.jgiscotools.gisco_processes.LocalParameters;
import eu.europa.ec.eurostat.jgiscotools.io.CSVUtil;
import eu.europa.ec.eurostat.jgiscotools.io.geo.CRSUtil;
import eu.europa.ec.eurostat.jgiscotools.io.geo.GeoData;

public class SI {

	public static void main(String[] args) {
		System.out.println("Start");

		//load data
		ArrayList<Map<String, String>> data = CSVUtil.load(HealthcareUtil.path + "SI/SI_geolocated.csv");
		System.out.println(data.size());

		CSVUtil.addColumn(data, "cc", "SI");
		CSVUtil.addColumn(data, "geo_qual", "-1"); //ask kimberly
		CSVUtil.addColumn(data, "ref_date", "06/03/2020");

		for(Map<String, String> h : data) {
			String s = h.get("postcode_city");
			String postcode = s.substring(0, 4);
			String city = s.substring(5, s.length());
			h.put("postcode", postcode);
			h.put("city", city);

			s = h.get("street_number");
			String[] parts = s.split(" ");
			String hn = parts[parts.length-1];
			h.put("house_number", hn);
			s = s.replace(hn, "").trim();
			h.put("street", s);
		}
		CSVUtil.removeColumn(data, "postcode_city");
		CSVUtil.removeColumn(data, "street_number");

		LocalParameters.loadProxySettings();
		ServicesGeocoding.set(BingGeocoder.get(), data, "lon", "lat", true, true);

		CSVUtil.addColumns(data, HealthcareUtil.cols, "");
		Validation.validate(true, data, "SI");

		// save
		System.out.println(data.size());
		CSVUtil.save(data, HealthcareUtil.path + "SI/SI.csv");
		GeoData.save(CSVUtil.CSVToFeatures(data, "lon", "lat"), HealthcareUtil.path + "SI/SI.gpkg", CRSUtil.getWGS_84_CRS());

		System.out.println("End");
	}

}
