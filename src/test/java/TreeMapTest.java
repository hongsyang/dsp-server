import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by thunders on 2018/8/6.
 */
public class TreeMapTest {
    public static void main(String[] args) {
        TreeMap<Integer,Integer> tm = new TreeMap<>();
        tm.put(5,5);
        tm.put(4,4);
        tm.put(3,3);
        tm.put(1,1);
        tm.put(2,2);

        SortedMap sm = tm.tailMap(3);
        for(Object obj : sm.keySet()){
            Integer i = (Integer)obj;
            System.out.println(i);
        }

    }
}
