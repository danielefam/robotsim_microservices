package fr.tp.inf112.projects.robotsim.notifier;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import fr.tp.inf112.projects.canvas.controller.Observer;

public class LocalNotifier implements FactoryModelChangedNotifier {
	
	private transient List<Observer> observers;
	
	
	public LocalNotifier() {
		observers = null;
	}

	@Override
	public void notifyObservers() {
		for (final Observer observer : getObservers()) {
			observer.modelChanged();
		}
	}

	@Override
	public boolean addObserver(Observer observer) {
		return getObservers().add(observer);	
	}

	@Override
	public boolean removeObserver(Observer observer) {
		return getObservers().remove(observer);
	}

	public List<Observer> getObservers() {
		if (observers == null) {
			observers = new ArrayList<>();
		}
	
		return observers;
	}

	


}
