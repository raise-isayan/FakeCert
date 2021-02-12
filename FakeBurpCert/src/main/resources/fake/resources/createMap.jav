public static java.util.Map createMap() {
    java.util.Map map = new java.util.LinkedHashMap();
    java.io.BufferedReader reader = null;
    try {
        reader = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream("cert.txt"), "UTF-8"));
        String line = null;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) {
                continue;
            }
            String[] inputs = line.split("\t", 3);
            java.util.List item = new java.util.ArrayList();
            item.add(new java.util.AbstractMap.SimpleEntry(inputs[1], (inputs.length > 2) ? inputs[2] : ""));
            java.util.List attrs = (java.util.List) map.putIfAbsent(java.util.regex.Pattern.compile(inputs[0]), item);
            if (attrs != null) {
                attrs.addAll(item);
            }
        }
    } catch (java.io.IOException ex) {
        ex.printStackTrace();
    } finally {
        if (reader != null) {
            try {
                reader.close();
            } catch (java.io.IOException ex) {
            }
        }
    }
    return map;
}    
