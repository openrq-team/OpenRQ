package RQLibrary;

import java.util.Comparator;


public class ComparatorEncodingSymbol implements Comparator{

	@Override
	public int compare(Object arg0, Object arg1) {

		if(arg0.getClass().getName() != "EncodingSymbol" ||	arg1.getClass().getName() != "EncodingSymbol") throw new IllegalArgumentException("Comparator kaput");
		
		EncodingSymbol s1 = (EncodingSymbol) arg0;
		EncodingSymbol s2 = (EncodingSymbol) arg1;
		
		if(s1.getESI()<s2.getESI()){
			
			return -1;
		}
		else{
			
			if(s1.getESI()>s2.getESI()){
				
				return 1;
			}
			else{
				
				return 0;
			}
		}
	}
}
