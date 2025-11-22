package fr.tp.inf112.projects.robotsim.notifier;

import java.util.List;

import fr.tp.inf112.projects.canvas.controller.Observer;

public interface FactoryModelChangedNotifier {

	public void notifyObservers();
	public boolean addObserver(Observer observer);
	public boolean removeObserver(Observer observer);
	public List<Observer> getObservers();
	
}
