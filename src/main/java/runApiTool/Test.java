package runApiTool;

import java.io.Serializable;

public class Test implements Serializable {
    public String Name;

    public int Id;

    public Test(int id, String name){
        Id = id;
        Name = name;
    }
}
