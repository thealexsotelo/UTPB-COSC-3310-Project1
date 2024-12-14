import java.util.Arrays;

/**
 * <h1>UInt</h1>
 * Represents an unsigned integer using a boolean array to store the binary representation.
 * Each bit is stored as a boolean value, where true represents 1 and false represents 0.
 */
public class UInt {

    protected boolean[] bits; // The array representing the bits of the unsigned integer.
    protected int length;     // The number of bits used to represent the unsigned integer.

    /**
     * Constructor to clone an existing UInt object.
     *
     * @param toClone The UInt object to clone.
     */
    public UInt(UInt toClone) {
        this.length = toClone.length;
        this.bits = Arrays.copyOf(toClone.bits, this.length);
    }

    /**
     * Constructor to create a UInt from an integer value.
     *
     * @param i The integer value to convert to UInt.
     */
    public UInt(int i) {
        length = (i == 0) ? 1 : (int) (Math.ceil(Math.log(i) / Math.log(2)) + 1);
        bits = new boolean[length];
        for (int b = length - 1; b >= 0; b--) {
            bits[b] = (i % 2) == 1;
            i = i >> 1;
        }
    }

    @Override
    public UInt clone() {
        return new UInt(this);
    }

    public static UInt clone(UInt u) {
        return new UInt(u);
    }

    private void resize(int newLength) {
        if (newLength <= length) return;
        if (newLength <= 0) {
            throw new IllegalArgumentException("Invalid array size: " + newLength);
        }
        boolean[] newBits = new boolean[newLength];
        System.arraycopy(bits, 0, newBits, newLength - length, length);
        bits = newBits;
        length = newLength;
    }

    public String getLastTwoBits() {
        if (length == 0) return "00";
        if (length == 1) return (bits[0] ? "1" : "0") + "0";
        return (bits[length - 2] ? "1" : "0") + (bits[length - 1] ? "1" : "0");
    }

    private void trim() {
        int msbIndex = -1;
        for (int i = 0; i < bits.length; i++) {
            if (bits[i]) {
                msbIndex = i;
            }
        }
        if (msbIndex == -1) {
            bits = new boolean[1];
            length = 1;
            return;
        }
        int newLength = msbIndex + 1;
        boolean[] trimmedBits = new boolean[newLength];
        System.arraycopy(bits, 0, trimmedBits, 0, newLength);
        bits = trimmedBits;
        length = newLength;
    }

    private void shiftRight(int positions) {
        if (positions <= 0) return;
        for (int i = this.length - 1; i >= positions; i--) {
            bits[i] = bits[i - positions];
        }
        for (int i = 0; i < positions; i++) {
            bits[i] = false;
        }
    }

    public int toInt() {
        int t = 0;
        for (int i = 0; i < length; i++) {
            t = (t << 1) + (bits[i] ? 1 : 0);
        }
        return t;
    }

    public static int toInt(UInt u) {
        return u.toInt();
    }

    public String toString() {
        StringBuilder s = new StringBuilder("0b");
        // Construct the String starting with the most-significant bit.
        for (int i = 0; i < length; i++) {
            // Again, we use a ternary here to convert from true/false to 1/0
            s.append(bits[i] ? "1" : "0");
        }
        return s.toString();
    }


    public void negate() {
        this.invert();
        UInt one = new UInt(1);
        this.add(one);
        this.trim();
    }

    private void invert() {
        for (int i = 0; i < length; i++) {
            bits[i] = !bits[i];
        }
    }

    public void add(UInt u) {
        int maxLength = Math.max(this.length, u.length) + 1;
        this.resize(maxLength);
        boolean carry = false;
        for (int i = 1; i <= maxLength; i++) {
            boolean bitA = (this.length - i >= 0) ? this.bits[this.length - i] : false;
            boolean bitB = (u.length - i >= 0) ? u.bits[u.length - i] : false;
            boolean sum = bitA ^ bitB ^ carry;
            carry = (bitA && bitB) || (carry && (bitA || bitB));
            this.bits[this.length - i] = sum;
        }
    }

    public static UInt add(UInt a, UInt b) {
        UInt temp = a.clone();
        temp.add(b);
        return temp;
    }

    public void sub(UInt u) {
        UInt negated = u.clone();
        negated.negate();
        this.add(negated);
        if (this.toInt() < 0) {
            this.bits = new boolean[1];
            this.length = 1;
        }
        this.trim();
    }

    public static UInt sub(UInt a, UInt b) {
        UInt result = a.clone();
        result.sub(b);
        return result;
    }

    public void and(UInt u) {
        int maxLength = Math.max(this.length, u.length);
        this.resize(maxLength);
        u.resize(maxLength);
        for (int i = 0; i < maxLength; i++) {
            this.bits[i] = this.bits[i] & u.bits[i];
        }
    }

    public static UInt and(UInt a, UInt b) {
        UInt temp = a.clone();
        temp.and(b);
        return temp;
    }

    public void or(UInt u) {
        int maxLength = Math.max(this.length, u.length);
        this.resize(maxLength);
        u.resize(maxLength);
        for (int i = 0; i < maxLength; i++) {
            this.bits[i] = this.bits[i] | u.bits[i];
        }
    }

    public static UInt or(UInt a, UInt b) {
        UInt temp = a.clone();
        temp.or(b);
        return temp;
    }

    public void xor(UInt u) {
        int maxLength = Math.max(this.length, u.length);
        this.resize(maxLength);
        u.resize(maxLength);
        for (int i = 0; i < maxLength; i++) {
            this.bits[i] = this.bits[i] ^ u.bits[i];
        }
    }

    public static UInt xor(UInt a, UInt b) {
        UInt temp = a.clone();
        temp.xor(b);
        return temp;
    }

    public void negativeAddition(UInt u) {
        boolean isThisNegative = this.bits[0];
        boolean isUNegative = u.bits[0];
        UInt absThis = this.clone();
        UInt absU = u.clone();
        if (isThisNegative) absThis.negate();
        if (isUNegative) absU.negate();
        absThis.add(absU);
        if (isThisNegative == isUNegative) {
            this.bits = absThis.bits;
            this.length = absThis.length;
            if (isThisNegative) this.negate();
        } else {
            if (absThis.toInt() >= absU.toInt()) {
                this.bits = absThis.bits;
                this.length = absThis.length;
            } else {
                this.bits = absU.bits;
                this.length = absU.length;
                this.negate();
            }
        }
    }

    public void mul(UInt u) {
        int n = Math.max(this.length, u.length);
        this.resize(n + 1);
        UInt multiplicand = this.clone();
        UInt multiplier = u.clone();
        multiplier.resize(n + 1);
        UInt accumulator = new UInt(0);
        boolean qMinus1 = false;
        for (int i = 0; i < n; i++) {
            String lastTwoBits = multiplier.getLastTwoBits();
            boolean q0 = lastTwoBits.charAt(1) == '1';
            qMinus1 = lastTwoBits.charAt(0) == '1';
            if (q0 && !qMinus1) {
                accumulator.sub(multiplicand);
            } else if (!q0 && qMinus1) {
                accumulator.add(multiplicand);
            }
            qMinus1 = multiplier.bits[multiplier.length - 1];
            multiplier.shiftRight(1);
            multiplier.bits[0] = accumulator.bits[accumulator.length - 1];
            accumulator.shiftRight(1);
        }
        this.bits = new boolean[accumulator.length + multiplier.length];
        this.length = accumulator.length + multiplier.length;
        System.arraycopy(accumulator.bits, 0, this.bits, 0, accumulator.length);
        System.arraycopy(multiplier.bits, 0, this.bits, accumulator.length, multiplier.length);
        this.trim();
    }

    public static UInt mul(UInt a, UInt b) {
        UInt result = a.clone();
        result.mul(b);
        return result;
    }
}

