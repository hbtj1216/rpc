package com.tao.rpc.example;

public class CustomObject {
	
	private String name;
	private int age;
	
	
	public CustomObject(String name, int age) {
		this.name = name;
		this.age = age;
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	
	
	@Override
	public String toString() {
		return "CustomObject [name=" + name + ", age=" + age + "]";
	}
	
	
	@Override
	public boolean equals(Object obj) {

		CustomObject object = null;
		if(obj instanceof CustomObject)
			object = (CustomObject) obj;
		else 
			return false;
		
		return ((this.name.equals(object.name)) && (this.age == object.age));
	}
			
}
