package RQLibrary;

import java.util.Comparator;

final class ComparatorEncodingSymbol implements Comparator<EncodingSymbol>{

	@Override
	public int compare(EncodingSymbol s1, EncodingSymbol s2) {
		
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