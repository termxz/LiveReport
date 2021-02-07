package io.termxz.spigot.observer;

import io.termxz.spigot.data.report.Report;

import java.util.ArrayList;
import java.util.List;

public class ReportSubject implements Subject {

    private List<Observer> observers = new ArrayList<>();

    @Override
    public void register(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void unregister(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Report report) {
        observers.forEach(observer -> observer.update(report));
    }
}
