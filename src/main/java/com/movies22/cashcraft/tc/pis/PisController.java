package com.movies22.cashcraft.tc.pis;

import java.util.concurrent.ConcurrentHashMap;

import com.bergerkiller.bukkit.sl.API.Variables;
import com.movies22.cashcraft.tc.controller.BaseController;

public class PisController extends BaseController {
	private ConcurrentHashMap<String, PIS> pis;
	private ConcurrentHashMap<String, DynamicPIS> dynPis;
	
	public PisController() {
		this.pis = new ConcurrentHashMap<String, PIS>();
		this.dynPis = new ConcurrentHashMap<String, DynamicPIS>();
	}
	
	public void addDynPis(String s, DynamicPIS p) {
		this.dynPis.put(s, p);
	}

	public PIS getPis(String s) {
		PIS a = this.pis.get(s);
		if(a == null) {
			a = new PIS(s);
			this.pis.put(s, a);
		}
		return a;
	}
	
	public void updateSigns() {
		this.pis.values().forEach(pis -> {
			if(!pis.lastVar.equals(pis.variable)) {
				Variables.get(pis.name).set(pis.variable);
				pis.lastVar = pis.variable;
			}
		});
		this.dynPis.values().forEach(dynpis -> {
			dynpis.updateSign();
		});
	}
	
	
	public void clear() {
		this.pis.clear();
		this.dynPis.clear();
	}
	
	@Override
	public void doFixedTick() {
		this.pis.values().forEach(pis -> {
			pis.doStep();
		});
		this.dynPis.values().forEach(pis -> {
			pis.doStep();
		});
		
	}
}
