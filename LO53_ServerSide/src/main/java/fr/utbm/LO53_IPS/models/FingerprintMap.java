package fr.utbm.LO53_IPS.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FingerprintMap {
	private int length; // length of the map
	private int width; // width of the map
	private List<HistogramFingerprint> map = new ArrayList<HistogramFingerprint>();
	
	public FingerprintMap(){}
	
	public FingerprintMap(int length, int width, ArrayList<HistogramFingerprint> map){
		this.length = length;
		this.width = width;
		this.map = map;
	}
	
	public List<HistogramFingerprint> getMap() {
		return map;
	}

	public void setMap(List<HistogramFingerprint> map) {
		this.map = map;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
	
	public Coordinate findPosition(List<RSSIHistogram> vector_RSSI_Histogram, Device device){
		boolean test = false;
		int i = 1;
		Coordinate last_position_user = device.getPositions().get(device.getPositions().size()).getPosition();
		List<HistogramFingerprint> search_zone = find_circle(last_position_user,i);
		List<Double> distance = new ArrayList<Double>();
		double distance_min;
		double threshold = 10;
		while(!test){
			for(HistogramFingerprint fprint : search_zone){
				distance.add(compute_probability(vector_RSSI_Histogram,fprint.getHistogramSamples()));
			}
			distance_min = Collections.min(distance);
			if(distance_min < threshold){
				test = true;
			}
			else{
				++i;
				search_zone = find_circle(last_position_user,i);
			}
		}
		return null;
	}
	
	public double compute_probability(List<RSSIHistogram> list_histogram1, List<RSSIHistogram> list_histogram2){
		boolean end = false;
		int i = 0, id1,id2;
		RSSIHistogram histogram1,histogram2;
		double probability = 1;
		if(list_histogram1.size() != list_histogram2.size()){
			end = true;
			probability = 0;
		}
		while(!end){
			if(i<list_histogram1.size()){
				histogram1 = list_histogram1.get(i);
				id1 = histogram1.getDevice().getDeviceID();
				
				histogram2 = list_histogram2.get(i);
				id2 = histogram2.getDevice().getDeviceID();
				
				if(id1 == id2){
					probability = probability * compute_probability_2histogram(histogram1, histogram2);
					++i;
				}
			}
			else{
				end =  true;
				probability = 0;
			}
		}
		
		return probability;
	}
	
	public double compute_probability_2histogram(RSSIHistogram histogram1, RSSIHistogram histogram2){
		int i = 0;
		int j = 0;
		List<BarRSSIHistogram> value_histogram1 = histogram1.getValue();
		List<BarRSSIHistogram> value_histogram2 = histogram2.getValue();
		BarRSSIHistogram value_histogram1_i, value_histogram2_j;
		int prob = 0;
		while(i<histogram1.getValue().size() && j<histogram2.getValue().size()){
			value_histogram1_i = value_histogram1.get(i);
			value_histogram2_j = value_histogram2.get(j);
			if(value_histogram1_i.getValue() == value_histogram2_j.getValue()){
				prob = prob + Math.min(value_histogram1_i.getOccurences(),value_histogram2_j.getOccurences());
				++i;
				++j;
			}
			
			else if(value_histogram1_i.getValue() < value_histogram2_j.getValue()){
				++i;
			}
			
			else{
				++j;
			}
		}
		
		return prob;
	}
	public List<HistogramFingerprint> find_circle(Coordinate position, int size){ 
		int index_tmp;
		Coordinate pos_tmp = new Coordinate();
		List<HistogramFingerprint> circle = new ArrayList<HistogramFingerprint>();
		int k = 1;
		for(int i=-size;i<size+1;++i){
			if (position.getX()+i>=0){
				pos_tmp.setX(position.getX()+i);
				if(i==0||i==size*2){
					k = 1;
				}
				else{
					k = 2*size;
				}
				for(int j=-size;j<size+1;j=j+k){
					if(position.getY()+j>=0){
						pos_tmp.setY(position.getY()+j);
						index_tmp = find_index(pos_tmp);
						circle.add(map.get(index_tmp));
					}
				}
			}
		}
		return circle;
	}
	
	public int find_index(Coordinate position){
		return position.getX()*width + position.getY();
	}
}
