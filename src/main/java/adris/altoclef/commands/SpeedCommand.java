package adris.altoclef.commands;

import adris.altoclef.AltoClef;
import adris.altoclef.commandsystem.ArgParser;
import adris.altoclef.commandsystem.Command;
import adris.altoclef.tasks.speedrun.BeatMinecraftSpeedrunTask;

public class SpeedCommand extends Command {
    public SpeedCommand() {
        super("speed", "Beats the game (20dinosaurs speed version).");
    }

    @Override
    protected void call(AltoClef mod, ArgParser parser) {
        mod.runUserTask(new BeatMinecraftSpeedrunTask(), this::finish);
    }
}