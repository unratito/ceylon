package ceylon.language;

import com.redhat.ceylon.compiler.java.metadata.Ceylon;
import com.redhat.ceylon.compiler.java.metadata.Class;
import com.redhat.ceylon.compiler.java.metadata.Ignore;
import com.redhat.ceylon.compiler.java.metadata.Name;
import com.redhat.ceylon.compiler.java.metadata.SatisfiedTypes;
import com.redhat.ceylon.compiler.java.metadata.TypeInfo;

@Ceylon
@SatisfiedTypes({
    "ceylon.language.Castable<ceylon.language.Integer|ceylon.language.Float>",
    "ceylon.language.Integral<ceylon.language.Integer>"
})
@Class(extendsType="ceylon.language.Object")
public final class Integer
    implements Castable<Numeric>, Integral<Integer> {
    
    final long value;
    private Integer(long l) {
        value = l;
    }
    
    public static Integer instance(long l) {
        return new Integer(l);
    }
    
    @Override
    public Integer plus(@Name("other") Integer op) {
        return instance(value + op.value);
    }
    
    @Override
    public Integer minus(@Name("other") Integer op) {
        return instance(value - op.value);
    }
    
    @Override
    public Integer times(@Name("other") Integer op) {
        return instance(value * op.value);
    }
    
    @Override
    public Integer divided(@Name("other") Integer op) {
        return instance(value / op.value);
    }
    
    @Override
    public Integer power(@Name("other") Integer op) {
        return instance((long) Math.pow(value, op.value)); // FIXME: ugly
    }
    
    @Ignore
    public Float plus(Float op) {
        return Float.instance(value + op.value);
    }
    
    @Ignore
    public Float minus(Float op) {
        return Float.instance(value - op.value);
    }
    
    @Ignore
    public Float times(Float op) {
        return Float.instance(value * op.value);
    }
    
    @Ignore
    public Float divided(Float op) {
        return Float.instance(value / op.value);
    }
    
    @Ignore
    public Float power(Float op) {
        return Float.instance(Math.pow(value, op.value)); // FIXME: ugly
    }
    
    @Override
    public Integer getMagnitude() {		
        return instance(Math.abs(value));
    }
    
    @Override
    public Integer getFractionalPart() {		
        return instance(0);
    }
    
    @Override
    public Integer getWholePart() {		
        return this;
    }
    
    @Override
    public boolean getPositive() {
        return value > 0;
    }
    
    @Override
    public boolean getNegative() {
        return value < 0;
    }
    
    @Override
    public long getSign() {
        if (value > 0)
            return 1;
        if (value < 0)
            return -1;
        return 0;
    }
    
    @Override
    public Integer remainder(@Name("other") Integer op) {
        return instance(value % op.value);
    }
    
    @Override
    public Integer getPositiveValue() {
        return this;
    }
    
    @Override
    public Integer getNegativeValue() {
        return instance(-value);
    }
    
    public boolean test(Integer op) {
        return value == op.value;
    }
    
    @Override
    public Comparison compare(@Name("other") Integer op) {
        long x = value;
        long y = op.value;
        return (x < y) ? smaller.getSmaller() :
            ((x == y) ? equal.getEqual() : larger.getLarger());
    }
    
    @Override
    public java.lang.String toString() {
        return java.lang.Long.toString(value);
    }
    
    // Conversions between numeric types
    
    @TypeInfo(value="ceylon.language.Integer")
    @Override
    public long getInteger() {
        return value;
    }
    
    @TypeInfo(value="ceylon.language.Float")
    @Override
    public double getFloat() {
        return (double) value;
    }
    
    @Override
    public boolean getUnit() {
        return value==1;
    }
    
    @Override
    public boolean getZero() {
        return value==0;
    }
    
    // Just a kludge til we have full autoboxing
    public long longValue() {
        return value;
    }
    
    @Override
    public Integer getPredecessor() {
        return Integer.instance(value - 1);
    }
    
    @Override
    public Integer getSuccessor() {
        return Integer.instance(value + 1);
    }
    
    // Probably not spec-conformant
    public Integer complement() {
        return instance(~value);
    }
    
    @Override
    public boolean equals(@Name("that") @TypeInfo("ceylon.language.Equality") java.lang.Object that) {
        if (that instanceof Integer) {
            return value == ((Integer)that).value;
        } 
        else if (that instanceof Integer) {
            return value == ((Integer)that).value;
        } 
        else if (that instanceof Float) {
            return value == ((Float)that).value;
        } 
        else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
    	return (int)(value ^ (value >>> 32));
    }
    
    /*@Override
    public boolean largerThan(@Name("other") Integer other) {
        return value > other.value;
    }
    
    @Override
    public boolean smallerThan(@Name("other") Integer other) {
        return value < other.value;
    }
    
    @Override
    public boolean asLargeAs(@Name("other") Integer other) {
        return value >= other.value;
    }
    
    @Override
    public boolean asSmallAs(@Name("other") Integer other) {
        return value <= other.value;
    }*/
    
    @Override
    public <CastValue extends Numeric> CastValue castTo() {
        return (CastValue)this;
    }
    
    @TypeInfo("ceylon.language.Character")
    public int getCharacter() {
        return (int) value;
    }
    
}
