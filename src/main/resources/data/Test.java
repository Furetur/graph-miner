import java.util.Map;

class Main {

    boolean field = true;

    public void largeMethod(boolean b) {
        var a = 5;
        if (b) {
            a = 6;
        } else {
            a = 7;
        }
        System.out.println(a);
    }
    public void f(int z) {
        if (field) {
            field = false
        }
        System.out.println(field);
    }

}
