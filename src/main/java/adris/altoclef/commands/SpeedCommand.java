package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.speedrun.BeatMinecraftSpeedrunTask;

public class SpeedCommand extends Command {
    public SpeedCommand() {
        super("bruh", "Does a bruh (beats the game).");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.runUserTask(new BeatMinecraftSpeedrunTask(), this::finish);
    }
}