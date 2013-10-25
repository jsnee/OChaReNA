package com.jophus.ocharena.image;

import java.io.File;
import java.util.ArrayList;

public class Filters {
	private File file;
	private FilterOptions options;

	public Filters(String filepath) {
		options = new FilterOptions();
		file = new File(filepath);
	}
	
	public void threshold(int minValue) {
		options.setFilterType(FilterType.THRESHOLD);
		options.add(minValue);
	}
	
	public enum FilterType {
		GREYSCALE, THRESHOLD
	}

	public class FilterOptions {

		private ArrayList<Number> filterOptions;
		private FilterType filterType;
		
		public FilterOptions() {
			this.filterOptions = new ArrayList<Number>();
		}
		
		public void add(Number value) {
			this.filterOptions.add(value);
		}
		
		public void setFilterType(FilterType type) {
			this.filterType = type;
			clearOptions();
		}
		
		public FilterType getFilterType() {
			return this.filterType;
		}
		
		public ArrayList<Number> getOptions() {
			return this.filterOptions;
		}
		
		public void clearOptions() {
			if (this.filterOptions.isEmpty()) {
				return;
			} else {
				this.filterOptions.clear();
			}
		}

	}
}
