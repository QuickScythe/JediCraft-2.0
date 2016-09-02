package com.mineswine.jedicraft.utils;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.configuration.serialization.SerializableAs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SerializationHelper {

	public static HashMap<String,Object> serialize(Class<?> clazz, boolean whitelist, Object instance){
		HashMap<String,Object> serializedData = new HashMap<String,Object>();
		for (Field field : clazz.getFields()){
			Object obj = instance;
			if (Modifier.isStatic(field.getModifiers())){
				obj = null;
			}
			if (!field.isAccessible()){
				field.setAccessible(true);
			}
			if (Modifier.isTransient(field.getModifiers())) continue;
			if (whitelist && !field.isAnnotationPresent(DoSerialize.class)) continue;
			try {
				if (field.getClass().isArray()){
					field.getClass().cast(Object[].class);
				}
				serializedData.put(field.getName(), field.get(obj));
			} catch (Exception ex){
				ex.printStackTrace();
			}
		}
		return serializedData;
	}

	public static void deserialize(Class<?> clazz, boolean whitelist, Object instance, Map<String,Object> data, boolean ignoreNull){
		for (Field field : clazz.getFields()){
			Object obj = instance;
			if (Modifier.isStatic(field.getModifiers())){
				obj = null;
			}
			if (!field.isAccessible()){
				field.setAccessible(true);
			}
			if (Modifier.isTransient(field.getModifiers())) continue;
			if (whitelist && !field.isAnnotationPresent(DoSerialize.class)) continue;
			try {
				if (ignoreNull && data.get(field.getName()) == null) continue;
				if (field.getType().isArray()){
					ArrayList<?> dat = (ArrayList<?>) data.get(field.getName());
					field.set(obj, dat.toArray());
					continue;
				}
				field.set(obj,(Object)data.get(field.getName()));
			} catch (Exception ex){
				ex.printStackTrace();
			}
		}
	}

	public static void registerSerialization(Class<? extends ConfigurationSerializable>...classes){
		for (Class<? extends ConfigurationSerializable> clazz : classes){
			ConfigurationSerialization.registerClass(clazz, ((SerializableAs)clazz.getAnnotation(SerializableAs.class)).value());
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface DoSerialize{

	}
}
