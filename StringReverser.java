String heap = "";


static String reverse(String str) {
    
    if(heap.length == null){
        return str;
    }
    else if(heap.length() > 0) {
        return reverse(str.concat(heap.charAt(0)));
    }
    else if(heap.length() == 1) {
        String last = str.concat(heap.charAt(0));
        heap = null;
        return reverse(last);
    }
    else {
        heap = heap.concat(str.charAt(str.length()-1);
        return reverse(str.substring(0, str.length()-1));
    }
}

