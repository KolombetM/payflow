package playground;

public class NameFormatter {
    public NameFormatter(){
        System.out.println("Create formatter");
    }

    public String format(String name) {
        name = name.trim();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}
