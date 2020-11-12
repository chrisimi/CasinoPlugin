package com.chrisimi.casinoplugin.commands.admin;

import com.chrisimi.casinoplugin.scripts.DataManager;
import com.chrisimi.commands.Command;
import com.chrisimi.commands.Event;
import com.chrisimi.commands.UsageType;

/**
 * the command instance for /casino exportdata
 */
public class ExportDataCommand extends Command
{
    public ExportDataCommand()
    {
        this.command = "exportdata";
        this.description = "Exports the data from one database to another database. Use -overwrite to overwrite the data (CAUTION! data loss). Valid databases: file, mysql";
        this.argumentsDescription = "[from database] [to database] [(optinal) -overwrite]";
        this.enableArguments = true;
        this.permissions = new String[] {"casino.admin"};
        this.usageType = UsageType.PLAYER_CONSOLE;
    }
    @Override
    public void execute(Event event)
    {
        if(event.getArgs().length < 2) return;

        DataManager.DBMode fromDatabase = parseDb(event.getArgs()[0]);
        DataManager.DBMode toDatabase = parseDb(event.getArgs()[1]);

        if(fromDatabase == toDatabase || fromDatabase == null || toDatabase == null)
            return;

        boolean success = DataManager.getInstance().exportData(fromDatabase, toDatabase, String.join(" ", event.getArgs()).contains("-overwrite"));
    }

    private DataManager.DBMode parseDb(String string)
    {
        for(DataManager.DBMode dbMode : DataManager.DBMode.values())
        {
            if(string.equalsIgnoreCase(dbMode.toString()))
                return dbMode;
        }

        return null;
    }
}
