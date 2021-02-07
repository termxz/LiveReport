package io.termxz.spigot.api.events;

import io.termxz.spigot.data.report.Report;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ReportSubmittedEvent extends Event {

    private final Report report;

    public ReportSubmittedEvent(Report report) {
        this.report = report;
    }

    public Report getReport() {
        return report;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
