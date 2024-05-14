package com.velocitypowered.tp_duel1;


import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.FileReader;

/*
    This class reads the contents of the json file duelLocation.json and returns x,y,z of the location of where the duel will take place

    Input: It just reads the contents of duelLocation.json, configured by you! The programmer
    Output: [x,y,z] long array of where the duel will take place
 */
public class ReadJSONFile {
    public static Long[] main(String[] args) {
        long[] coordinates = new long[3];

        JSONParser jsonParser = new JSONParser();

        Long x = null;
        Long y = null;
        Long z = null;
        try {
            JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader("duelLocation.json"));
            x = (Long) jsonObject.get("x");
            y = (Long) jsonObject.get("y");
            z = (Long) jsonObject.get("z");
            coordinates[0] = x;
            coordinates[1] = y;
            coordinates[2] = z;
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        Long[] dueling_location = new Long[3];
        dueling_location[0] = x;
        dueling_location[1] = y;
        dueling_location[2] = z;

        // Print the values
        //System.out.println("x: " + coordinates[0]);
        //System.out.println("y: " + coordinates[1]);
        //System.out.println("z: " + coordinates[2]);
        return dueling_location;
    }
}
