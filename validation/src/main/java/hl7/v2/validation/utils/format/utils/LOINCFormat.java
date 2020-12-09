package hl7.v2.validation.utils.format.utils;

public class LOINCFormat {
	
	public static boolean isValid(String code) {
        if (!code.matches("\\d{1,5}-\\d")) {
            return false;
        }
        String extract = code.substring(0, code.indexOf("-"));
        String checkDigit = mod10(extract);
        String loinc = String.format("%s-%s", extract, checkDigit);
        return code.equals(loinc);
    }
	
    /**
     * Mod10 algorithm for LOINC
     * 
     * @param code
     * @return
     */
    private static String mod10(String code) {
        if (!code.matches("\\d+")) {
            return null;
        }
        if (code.length() > 5) {
            return null;
        }
        // if length < 5, add leading "0"
        StringBuffer input = new StringBuffer(code);
        while (input.length() < 5) {
            input.insert(0, "0");
        }

        // 1. Using the number 12345, assign positions to the digits, from right
        // to left.

        // 2. Take the odd digit positions counting from the right (1st, 3rd,
        // 5th, etc.)
        StringBuffer odd = new StringBuffer();
        for (int i = 0; 2 * i < input.length(); i++) {
            odd.insert(0, input.charAt(2 * i));
        }

        // 3.Multiply by 2.
        int odd2 = Integer.parseInt(odd.toString()) * 2;

        // 4. Take the even digit positions starting from the right (2nd, 4th,
        // etc.).
        StringBuffer even = new StringBuffer();
        for (int i = 0; 2 * i + 1 < input.length(); i++) {
            even.insert(0, input.charAt(2 * i + 1));
        }

        // 5.Append (4) to the front of the results of (3).
        even.append(odd2);

        // 6. Add the digits of (5) together.
        double add = 0;
        for (int i = 0; i < even.length(); i++) {
            add = add + Integer.parseInt(even.substring(i, i + 1));
        }

        // 7. Find the next highest multiple of 10.
        double multiple = Math.ceil(add / 10) * 10;

        // 8. Subtract (6) from (7).
        Long result = Math.round(multiple - add);

        return result.toString();
    }

}
