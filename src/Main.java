import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
        List list = Arrays.asList(12,1,2,2,3,5);
        list.forEach((i)->System.out.println(i));
    }

public static void method(String a) {
        try {
            Integer.valueOf(a);
        }catch (NumberFormatException ne) {
            //log.error("Exception while converting", ne);
        }

}
}