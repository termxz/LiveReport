package io.termxz.spigot.observer;

import io.termxz.spigot.data.report.Report;

public interface Subject {

    void register(Observer observer);
    void unregister(Observer observer);
    void notifyObservers(Report report);

}
