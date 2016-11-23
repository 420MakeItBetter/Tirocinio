package main;

import Client.commands.Command;

/**
 * Created by machiara on 22/11/16.
 */
public class CommandTask<T extends Command,R extends ResponseTask> {

    T command;
    R task;

    CommandTask(){}

}
