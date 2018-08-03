package cn.shuzilm.util.geo;

import com.vividsolutions.jts.geom.Coordinate;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

/**
 * Created by thunders on 2018/7/31.
 */
public class GeoTools {
    public static void main(String[] args) {
        try{
            CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");


            CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:3857");

            MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);


            Coordinate coorDst = new Coordinate();

            JTS.transform(new Coordinate(40, 116),coorDst, transform);


            System.out.println(coorDst);

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
}

