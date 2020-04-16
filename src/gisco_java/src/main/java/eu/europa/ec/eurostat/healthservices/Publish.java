/**
 * 
 */
package eu.europa.ec.eurostat.healthservices;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import org.locationtech.jts.geom.Coordinate;

import eu.europa.ec.eurostat.jgiscotools.deprecated.NUTSUtils;
import eu.europa.ec.eurostat.jgiscotools.feature.Feature;
import eu.europa.ec.eurostat.jgiscotools.io.CSVUtil;
import eu.europa.ec.eurostat.jgiscotools.io.GeoData;
import eu.europa.ec.eurostat.jgiscotools.util.ProjectionUtil;

/**
 * Copy country CSV files to github repository.
 * Combine them in the all.csv file.
 * Convert as GeoJSON and GPKG format.
 * 
 * @author julien Gaffuri
 *
 */
public class Publish {

	static String destinationPath = "C:/Users/gaffuju/workspace/healthcare-services/";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Start");

		//publication date
		String timeStamp = HCUtil.dateFormat.format(Calendar.getInstance().getTime());
		System.out.println(timeStamp);

		//make outpur folders
		new File(destinationPath + "data/csv/").mkdirs();
		new File(destinationPath + "data/geojson/").mkdirs();
		new File(destinationPath + "data/gpkg/").mkdirs();

		var all = new ArrayList<Map<String, String>>();
		var changed = false;
		for(String cc : HCUtil.ccs) {

			var inCsvFile = HCUtil.path + cc+"/"+cc+".csv";
			var outCsvFile = destinationPath+"data/csv/"+cc+".csv";

			//compare file dates, skip the ones that have not been updated
			if(new File(outCsvFile).exists())
				try {
					FileTime tIn = Files.getLastModifiedTime(new File(inCsvFile).toPath());
					FileTime tOut = Files.getLastModifiedTime(new File(outCsvFile).toPath());
					if(tOut.compareTo(tIn) >= 0) {
						System.out.println("No change found for: " + cc);
						//if(tOut.compareTo(tIn) > 0)
						//	Files.copy(new File(outCsvFile).toPath(), new File(inCsvFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
						continue;
					}
				} catch (IOException e) { e.printStackTrace(); }

			changed = true;

			System.out.println("*** " + cc);
			System.out.println("Update");

			//load data
			ArrayList<Map<String, String>> data = CSVUtil.load(inCsvFile);
			System.out.println(data.size());

			//cc, country
			String cntr = NUTSUtils.getName(cc);
			if(cntr == null) System.err.println("cc: " + cc);
			if("DE".equals(cc)) cntr = "Germany";
			//CSVUtil.setValue(data, "cc", cc); //do not apply that - overseas territories
			CSVUtil.setValue(data, "country", cntr);

			//apply publication date
			CSVUtil.setValue(data, "pub_date", timeStamp);

			//apply geo_qual
			//replace(data, "geo_qual", null, "-1");
			//replace(data, "geo_qual", "", "-1");
			//CSVUtil.removeColumn(data, "geo_matching");
			//CSVUtil.removeColumn(data, "geo_confidence");

			//store for big EU file
			all.addAll(data);

			//export as geojson and GPKG
			CSVUtil.save(data, outCsvFile, HCUtil.cols_);
			Collection<Feature> fs = CSVUtil.CSVToFeatures(data, "lon", "lat");
			HCUtil.applyTypes(fs);
			GeoData.save(fs, destinationPath+"data/geojson/"+cc+".geojson", ProjectionUtil.getWGS_84_CRS());
			GeoData.save(fs, destinationPath+"data/gpkg/"+cc+".gpkg", ProjectionUtil.getWGS_84_CRS());
		}

		//handle "all" files
		if(changed) {

			//append cc to id
			for(Map<String, String> h : all) {
				String cc = h.get("cc");
				String id = h.get("id");
				if(id == null || "".equals(id)) {
					System.err.println("No identifier for items in " + cc);
					break;
				}
				String cc_ = id.length()>=2? id.substring(0, 2) : "";
				if(cc_.equals(cc)) continue;
				h.put("id", cc + "_" + id);
			}

			//export all
			System.out.println("*** All");
			System.out.println(all.size());
			CSVUtil.save(all, destinationPath+"data/csv/all.csv", HCUtil.cols_);
			Collection<Feature> fs = CSVUtil.CSVToFeatures(all, "lon", "lat");
			HCUtil.applyTypes(fs);
			GeoData.save(fs, destinationPath + "data/geojson/all.geojson", ProjectionUtil.getWGS_84_CRS());
			GeoData.save(fs, destinationPath + "data/gpkg/all.gpkg", ProjectionUtil.getWGS_84_CRS());

			{
				//export for web
				ArrayList<Map<String, String>> data = CSVUtil.load(destinationPath+"data/csv/all.csv");
				for(Map<String, String> d : data) {
					//load lat/lon
					double lon = Double.parseDouble(d.get("lon"));
					d.remove("lon");
					double lat = Double.parseDouble(d.get("lat"));
					d.remove("lat");

					//project to LAEA
					Coordinate c = ProjectionUtil.project(new Coordinate(lat,lon), ProjectionUtil.getWGS_84_CRS(), ProjectionUtil.getETRS89_LAEA_CRS());
					d.put("x", ""+(int)c.y);
					d.put("y", ""+(int)c.x);

					d.remove("id");
					d.remove("cc");
					d.remove("geo_qual");
				}

				//save
				CSVUtil.save(data, destinationPath+"map/hcs.csv");
			}
		}

		System.out.println("End");
	}

}
