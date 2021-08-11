class Main {

    boolean field = true;

    public void largeMethod(boolean b) {
        var a = b;
        if (b) {
            a = 6 + b;
        } else {
            a = 7;
        }
        System.out.println(a);
    }
    public int f(int z) {
        if (field) {
            field = (z % 2 == 0);
        }
        System.out.println(field);
        return z + 1;
    }
    public void mutatesField(Main main) {
        main.field = !main.field;
    }

}
