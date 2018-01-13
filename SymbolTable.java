package cop5556fa17;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import cop5556fa17.AST.Declaration;
import cop5556fa17.Attributes;
import cop5556fa17.AST.Declaration;

class Attributes
{
	private Declaration attribute;
	
	Attributes(Declaration dc)
	{
		
		attribute = dc;
	}
	
	
	
	Declaration getAttribute()
	{
		return attribute;
	}
}

public class SymbolTable {
	Stack<Integer> scope_stack=null;
	HashMap<String,ArrayList<Attributes>> sym_tab=null;
	
	public boolean insert(String ident, Declaration dec){
		ArrayList<Attributes> attr_list=null;
		int size;
		if(sym_tab.containsKey(ident))
		{
			attr_list=sym_tab.get(ident);
			size=attr_list.size();
			
				Attributes attr=new Attributes(dec);
				attr_list.add(attr);
				sym_tab.put(ident, attr_list);
		}
		else
		{
			 Attributes attr=new Attributes(dec);
			 attr_list=new ArrayList<Attributes>();
			 attr_list.add(attr);
			 sym_tab.put(ident, attr_list);
		}
		return true;
	}
	
	public TypeUtils.Type lookupType(String ident){
		Declaration dec=null;
		if(sym_tab.containsKey(ident))
		{
			ArrayList<Attributes> alist=null;
			alist=sym_tab.get(ident);
			dec=alist.get(0).getAttribute();
		}
		if(dec==null) return null;
		else return TypeUtils.getType(dec.firstToken);
	}
	
	public Declaration lookupDec(String ident){
		Declaration dec=null;
		if(sym_tab.containsKey(ident))
		{
			ArrayList<Attributes> alist=null;
			alist=sym_tab.get(ident);
			dec=alist.get(0).getAttribute();
		}
		return dec;
	}
	
	public SymbolTable() {
		
		scope_stack=new Stack<Integer>();
		scope_stack.add(0);
		sym_tab=new HashMap<String,ArrayList<Attributes>>();
	}


	@Override
	public String toString() {
		//TODO:  IMPLEMENT THIS
		return "";
	}

}
