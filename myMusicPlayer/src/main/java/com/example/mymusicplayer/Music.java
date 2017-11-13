package com.example.mymusicplayer;

public class Music {
	private int _id;
	private String _titles;
	private String _artists;
	private int position;
	
	
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public String get_titles() {
		return _titles;
	}
	public void set_titles(String _titles) {
		this._titles = _titles;
	}
	public String get_artists() {
		return _artists;
	}
	public void set_artists(String _artists) {
		this._artists = _artists;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
	
	
}
