/**
 * 
 */
package eu.europa.ec.eurostat.basicservices.education;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import eu.europa.ec.eurostat.basicservices.BasicServicesValidation;
import eu.europa.ec.eurostat.jgiscotools.io.CSVUtil;

/**
 * Check if the education CSV files are compliant with the specs.
 * 
 * @author julien Gaffuri
 *
 */
public class Validation {

	//run validation process for each country
	public static void main(String[] args) {
		System.out.println("Start");
		for(String cc : EducationUtil.ccs) {
			System.out.println("*** " + cc);
			ArrayList<Map<String, String>> data = CSVUtil.load(EducationUtil.path + cc+"/"+cc+".csv");
			System.out.println(data.size());
			validate(data, cc);
		}
		System.out.println("End");
	}

	//validate 
	public static void validate(Collection<Map<String, String>> data, String cc) {
		boolean b;

		//validation on all aspects common to other basic services
		BasicServicesValidation.validate(data, cc, EducationUtil.cols_);

		//TODO other tests ?
		//checks on "level"
		//checks on "fields"
		//check empty columns

		//check public_private - public/private
		b = BasicServicesValidation.checkValuesAmong(data, "public_private", "", "public", "private");
		if(!b) System.err.println("Problem with public_private values for " + cc);


		//non null columns
		b = BasicServicesValidation.checkValuesNotNullOrEmpty(data, "name");
		if(!b) System.err.println("Missing values for hospital_name format for " + cc);

	}

}