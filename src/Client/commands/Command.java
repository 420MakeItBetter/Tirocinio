package Client.commands;

import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Matteo on 15/11/2016.
 */
public abstract class Command implements Serializable {

    public abstract void execute(ObjectOutputStream out);

}
