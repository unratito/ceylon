package ceylon.language;

import static com.redhat.ceylon.compiler.java.Util.toInt;
import static com.redhat.ceylon.compiler.java.runtime.model.TypeDescriptor.intersection;
import static java.lang.System.arraycopy;
import static java.util.Arrays.copyOfRange;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import com.redhat.ceylon.compiler.java.Util;
import com.redhat.ceylon.compiler.java.language.AbstractArrayIterable;
import com.redhat.ceylon.compiler.java.metadata.Annotation;
import com.redhat.ceylon.compiler.java.metadata.Annotations;
import com.redhat.ceylon.compiler.java.metadata.Ceylon;
import com.redhat.ceylon.compiler.java.metadata.Class;
import com.redhat.ceylon.compiler.java.metadata.ConstructorName;
import com.redhat.ceylon.compiler.java.metadata.Defaulted;
import com.redhat.ceylon.compiler.java.metadata.FunctionalParameter;
import com.redhat.ceylon.compiler.java.metadata.Ignore;
import com.redhat.ceylon.compiler.java.metadata.Name;
import com.redhat.ceylon.compiler.java.metadata.SatisfiedTypes;
import com.redhat.ceylon.compiler.java.metadata.Transient;
import com.redhat.ceylon.compiler.java.metadata.TypeInfo;
import com.redhat.ceylon.compiler.java.metadata.TypeParameter;
import com.redhat.ceylon.compiler.java.metadata.TypeParameters;
import com.redhat.ceylon.compiler.java.runtime.metamodel.Metamodel;
import com.redhat.ceylon.compiler.java.runtime.model.ReifiedType;
import com.redhat.ceylon.compiler.java.runtime.model.TypeDescriptor;
import com.redhat.ceylon.compiler.java.runtime.serialization.$Serialization$;
import com.redhat.ceylon.compiler.java.runtime.serialization.Serializable;

import ceylon.language.impl.BaseIterable;
import ceylon.language.impl.BaseIterator;
import ceylon.language.impl.MemberImpl;
import ceylon.language.impl.rethrow_;
import ceylon.language.meta.declaration.ClassDeclaration;
import ceylon.language.meta.declaration.ValueDeclaration;
import ceylon.language.serialization.Member;
import ceylon.language.serialization.ReachableReference;

@Ceylon(major = 8)
@Class(constructors=true, extendsType="ceylon.language::Basic")
@TypeParameters(@TypeParameter(value = "Element"))
@SatisfiedTypes({
    "ceylon.language::SearchableList<Element>",
    "ceylon.language::Ranged<ceylon.language::Integer,Element,ceylon.language::Array<Element>>"
})
public final class Array<Element>
        implements Serializable, SearchableList<Element>,
                    ReifiedType, java.io.Serializable {
    
    private static final long serialVersionUID = 8612707712992151156L;

    private static final Finished FIN = finished_.get_();
    
    private static final java.lang.Object[] EMPTY_ARRAY = new java.lang.Object[0];    
    private static final java.lang.String[] EMPTY_STRING_ARRAY = new java.lang.String[0];
    private static final boolean[] EMPTY_BOOLEAN_ARRAY = new boolean[0];
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    private static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    
    /** The array, could be Object[], int[], boolean[] etc. Never null. */
    private final java.lang.Object array;
    /** The array if it's an Object[], otherwise null */
    private final java.lang.Object[] objectArray;
    /** The array if it's a String[], otherwise null */
    private final java.lang.String[] stringArray;
    /** The array if it's a long[], otherwise null */
    private final long[] longArray;
    /** The array if it's a double[], otherwise null */
    private final double[] doubleArray;
    /** The array if it's a boolean[], otherwise null */
    private final boolean[] booleanArray;
    /** The array if it's an int[], otherwise null */
    private final int[] intArray;
    /** The array if it's a byte[], otherwise null */
    private final byte[] byteArray;
    /** The array.length, cached for speed */
    private final int size;
    /** The reified element type */
    private final TypeDescriptor $reifiedElement;
    /** The element type, for switching */
    private final ArrayType elementType;

    
    @Ignore
    public Array(final TypeDescriptor $reifiedElement, 
            int size, Element element) {
        this($reifiedElement, 
                createArrayWithElement($reifiedElement, size, element));
    }
    
    public Array(@Ignore final TypeDescriptor $reifiedElement, 
            @Name("elements")
            @TypeInfo("ceylon.language::Iterable<Element,ceylon.language::Null>")
            final ceylon.language.Iterable<? extends Element,?> elements) {
        this($reifiedElement, 
                createArrayFromIterable($reifiedElement, elements));
    }
    
    @Ignore
    public static final Array.ofSize_ ofSize_ = null;
    
    @Ceylon(major = 8)
    @Ignore
    @ConstructorName("ofSize")
    public static final class ofSize_ {
        private ofSize_() {}
    }
    
    @Name("ofSize")
    public Array(@Ignore final TypeDescriptor $reifiedElement, 
    @Ignore Array.ofSize_ $name$, 
    @Name("size")
    @TypeInfo("ceylon.language::Integer")
    final long size,
    @Name("element")
    @TypeInfo("Element")
    final Element element) {
        this($reifiedElement, toSize(size), element);
    }
    
    private static long maxArraySize = runtime_.get_().getMaxArraySize();
    
    private static int toSize(long size) {
        if (size>maxArraySize) {
            throw new AssertionError("array size must be no larger than " + maxArraySize);
        }
        else if (size<0) {
            return 0;
        }
        else {
            return (int) size;
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <Element> java.lang.Object createArrayFromIterable(
            final TypeDescriptor $reifiedElement,
            final Iterable<? extends Element,?> elements) {
        
        if (elements instanceof List) {
            return createArrayFromList($reifiedElement, (List<? extends Element>) elements);
        }
        
        final ArrayList<Element> list = new ArrayList<Element>();
        Iterator<?> iterator = elements.iterator();
        java.lang.Object elem;
        while ((elem=iterator.next())!=FIN) {
            list.add((Element) elem);
        }
        
        final int size = list.size();
        
        switch (elementType($reifiedElement)) {
        case CeylonString:
            //note: we don't unbox strings in an Array<String?>
            //      because it would break javaObjectArray()
            java.lang.String[] stringArray = new java.lang.String[size];
            for (int i=0; i<size; i++) {
                String string = (String) list.get(i);
                stringArray[i] = string==null ? null : string.value;
            }
            return stringArray;
        case CeylonInteger:
            long[] longPrecisionArray = new long[size];
            for (int i=0; i<size; i++) {
                longPrecisionArray[i] = ((Integer) list.get(i)).value;
            }
            return longPrecisionArray;
        case CeylonFloat:
            double[] doublePrecisionArray = new double[size];
            for (int i=0; i<size; i++) {
                doublePrecisionArray[i] = ((Float) list.get(i)).value;
            }
            return doublePrecisionArray;
        case CeylonCharacter:
            int[] codepointArray = new int[size];
            for (int i=0; i<size; i++) {
                codepointArray[i] = ((Character) list.get(i)).codePoint;
            }
            return codepointArray;
        case CeylonBoolean:
            boolean[] boolArray = new boolean[size];
            for (int i=0; i<size; i++) {
                boolArray[i] = ((Boolean) list.get(i)).booleanValue();
            }
            return boolArray;
        case CeylonByte:
            byte[] bitsArray = new byte[size];
            for (int i=0; i<size; i++) {
                bitsArray[i] = ((Byte) list.get(i)).value;
            }
            return bitsArray;
        case JavaBoolean:
            boolean[] booleanArray = new boolean[size];
            for (int i=0; i<size; i++) {
                booleanArray[i] = (java.lang.Boolean) list.get(i);
            }
            return booleanArray;
        case JavaCharacter:
            char[] charArray = new char[size];
            for (int i=0; i<size; i++) {
                charArray[i] = (java.lang.Character) list.get(i);
            }
            return charArray;
        case JavaFloat:
            float[] floatArray = new float[size];
            for (int i=0; i<size; i++) {
                floatArray[i] = (java.lang.Float) list.get(i);
            }
            return floatArray;
        case JavaDouble:
            double[] doubleArray = new double[size];
            for (int i=0; i<size; i++) {
                doubleArray[i] = (java.lang.Double) list.get(i);
            }
            return doubleArray;
        case JavaByte:
            byte[] byteArray = new byte[size];
            for (int i=0; i<size; i++) {
                byteArray[i] = (java.lang.Byte) list.get(i);
            }
            return byteArray;
        case JavaShort:
            short[] shortArray = new short[size];
            for (int i=0; i<size; i++) {
                shortArray[i] = (java.lang.Short) list.get(i);
            }
            return shortArray;
        case JavaInteger:
            int[] intArray = new int[size];
            for (int i=0; i<size; i++) {
                intArray[i] = (java.lang.Integer) list.get(i);
            }
            return intArray;
        case JavaLong:
            long[] longArray = new long[size];
            for (int i=0; i<size; i++) {
                longArray[i] =(java.lang.Long) list.get(i);
            }
            return longArray;
        default:
            java.lang.Class<?> clazz = 
                    $reifiedElement.getArrayElementClass();
            java.lang.Object[] array = (java.lang.Object[]) 
                    java.lang.reflect.Array.newInstance(clazz, size);
            for (int i=0; i<size; i++) {
                array[i] = list.get(i);
            }
            return array;
        }

    }
    
    private static <Element> java.lang.Object createArrayFromList(
            final TypeDescriptor $reifiedElement,
            final List<? extends Element> elements) {
        
        if (elements instanceof Array) {
            return createArrayFromArray($reifiedElement, 
                    (Array<? extends Element>) elements);
        }
        
        int size = Util.toInt(elements.getSize());
        
        if (elements instanceof ceylon.language.String
                && !$reifiedElement.containsNull()
                && $reifiedElement.getArrayElementClass()==ceylon.language.Character.class) {
            int[] array = new int[size];
            java.lang.String string = elements.toString();
            for (int i=0, offset = 0; i<size; i++) {
                int codePoint = string.codePointAt(offset);
                offset += java.lang.Character.charCount(codePoint);
                array[i] = codePoint;
            }
            return array;
        }
        
        switch (elementType($reifiedElement)) {
        case CeylonString:
            //note: we don't unbox strings in an Array<String?>
            //      because it would break javaObjectArray()
            java.lang.String[] stringArray = new java.lang.String[size];
            for (int i=0; i<size; i++) {
                String string = (String) elements.getFromFirst(i);
                stringArray[i] = string==null ? null : string.value;
            }
            return stringArray;
        case CeylonInteger:
            long[] longPrecisionArray = new long[size];
            for (int i=0; i<size; i++) {
                Integer e = (Integer) elements.getFromFirst(i);
                longPrecisionArray[i] = e.value;
            }
            return longPrecisionArray;
        case CeylonFloat:
            double[] doublePrecisionArray = new double[size];
            for (int i=0; i<size; i++) {
                Float e = (Float) elements.getFromFirst(i);
                doublePrecisionArray[i] = e.value;
            }
            return doublePrecisionArray;
        case CeylonCharacter:
            int[] codepointArray = new int[size];
            for (int i=0; i<size; i++) {
                Character e = (Character) elements.getFromFirst(i);
                codepointArray[i] = e.codePoint;
            }
            return codepointArray;
        case CeylonBoolean:
            boolean[] boolArray = new boolean[size];
            for (int i=0; i<size; i++) {
                Boolean e = (Boolean) elements.getFromFirst(i);
                boolArray[i] = e.booleanValue();
            }
            return boolArray;
        case CeylonByte:
            byte[] bitsArray = new byte[size];
            for (int i=0; i<size; i++) {
                Byte e = (Byte) elements.getFromFirst(i);
                bitsArray[i] = e.value;
            }
            return bitsArray;
        case JavaBoolean:
            boolean[] booleanArray = new boolean[size];
            for (int i=0; i<size; i++) {
                booleanArray[i] = (java.lang.Boolean)
                        elements.getFromFirst(i);
            }
            return booleanArray;
        case JavaCharacter:
            char[] charArray = new char[size];
            for (int i=0; i<size; i++) {
                charArray[i] = (java.lang.Character)
                        elements.getFromFirst(i);
            }
            return charArray;
        case JavaFloat:
            float[] floatArray = new float[size];
            for (int i=0; i<size; i++) {
                floatArray[i] = (java.lang.Float)
                        elements.getFromFirst(i);
            }
            return floatArray;
        case JavaDouble:
            double[] doubleArray = new double[size];
            for (int i=0; i<size; i++) {
                doubleArray[i] = (java.lang.Double)
                        elements.getFromFirst(i);
            }
            return doubleArray;
        case JavaByte:
            byte[] byteArray = new byte[size];
            for (int i=0; i<size; i++) {
                byteArray[i] = (java.lang.Byte)
                        elements.getFromFirst(i);
            }
            return byteArray;
        case JavaShort:
            short[] shortArray = new short[size];
            for (int i=0; i<size; i++) {
                shortArray[i] = (java.lang.Short)
                        elements.getFromFirst(i);
            }
            return shortArray;
        case JavaInteger:
            int[] intArray = new int[size];
            for (int i=0; i<size; i++) {
                intArray[i] = (java.lang.Integer)
                        elements.getFromFirst(i);
            }
            return intArray;
        case JavaLong:
            long[] longArray = new long[size];
            for (int i=0; i<size; i++) {
                longArray[i] = (java.lang.Long)
                        elements.getFromFirst(i);
            }
            return longArray;
        default:
            java.lang.Class<?> clazz = 
                    $reifiedElement.getArrayElementClass();
            java.lang.Object[] objectArray = (java.lang.Object[]) 
                    java.lang.reflect.Array.newInstance(clazz, size);
            for (int i=0; i<size; i++) {
                objectArray[i] = elements.getFromFirst(i);
            }
            return objectArray;
        }
    }

    private static <Element> java.lang.Object createArrayFromArray(
            final TypeDescriptor $reifiedElement,
            final Array<? extends Element> elements) {

        final int size = Util.toInt(elements.getSize());
        final java.lang.Class<?> clazz = 
                $reifiedElement.getArrayElementClass();

        switch (elementType($reifiedElement)) {
        case CeylonString:  
            //note: we don't unbox strings in an Array<String?>
            //      because it would break javaObjectArray()
            java.lang.String[] stringArray = new java.lang.String[size];
            arraycopy(elements.array, 0, stringArray, 0, size);
            return stringArray;
        case CeylonInteger:
            long[] longPrecisionArray = new long[size];
            arraycopy(elements.array, 0, longPrecisionArray, 0, size);
            return longPrecisionArray;
        case CeylonFloat:
            double[] doublePrecisionArray = new double[size];
            arraycopy(elements.array, 0, doublePrecisionArray, 0, size);
            return doublePrecisionArray;
        case CeylonCharacter:
            int[] codepointArray = new int[size];
            arraycopy(elements.array, 0, codepointArray, 0, size);
            return codepointArray;
        case CeylonBoolean:
            boolean[] boolArray = new boolean[size];
            arraycopy(elements.array, 0, boolArray, 0, size);
            return boolArray;
        case CeylonByte:
            byte[] bitsArray = new byte[size];
            arraycopy(elements.array, 0, bitsArray, 0, size);
            return bitsArray;
        case JavaBoolean:
            boolean[] booleanArray = new boolean[size];
            arraycopy(elements.array, 0, booleanArray, 0, size);
            return booleanArray;
        case JavaCharacter:
            char[] charArray = new char[size];
            arraycopy(elements.array, 0, charArray, 0, size);
            return charArray;
        case JavaFloat:
            float[] floatArray = new float[size];
            arraycopy(elements.array, 0, floatArray, 0, size);
            return floatArray;
        case JavaDouble:
            double[] doubleArray = new double[size];
            arraycopy(elements.array, 0, doubleArray, 0, size);
            return doubleArray;
        case JavaByte:
            byte[] byteArray = new byte[size];
            arraycopy(elements.array, 0, byteArray, 0, size);
            return byteArray;
        case JavaShort:
            short[] shortArray = new short[size];
            arraycopy(elements.array, 0, shortArray, 0, size);
            return shortArray;
        case JavaInteger:
            int[] intArray = new int[size];
            arraycopy(elements.array, 0, intArray, 0, size);
            return intArray;
        case JavaLong:
            long[] longArray = new long[size];
            arraycopy(elements.array, 0, longArray, 0, size);
            return longArray;
        default:
            java.lang.Object[] objectArray = (java.lang.Object[]) 
            java.lang.reflect.Array.newInstance(clazz, size);
            java.lang.Object otherArray = elements.array;
            if (otherArray.getClass()==objectArray.getClass()) {
                arraycopy(otherArray, 0, objectArray, 0, size);
            }
            else {
                for (int i=0; i<size; i++) {
                    objectArray[i] = elements.getFromFirst(i);
                }
            }
            return objectArray;
        }
    }
    
    private static <Element> java.lang.Object createArrayWithElement(
        final TypeDescriptor $reifiedElement,
        final int size, final Element element) {
        switch (elementType($reifiedElement)) {
        case CeylonInteger:
            long[] longPrecisionArray = new long[size];
            if (element!=null) {
                long value = ((Integer) element).value;
                if (value!=0l) Arrays.fill(longPrecisionArray, value);
            }
            return longPrecisionArray;
        case JavaLong:
            long[] longArray = new long[size];
            if (element!=null) {
                long longValue = (java.lang.Long) element;
                if (longValue!=0l) Arrays.fill(longArray, longValue);
            }
            return longArray;
        case CeylonFloat:
            double[] doublePrecisionArray = new double[size];
            if (element!=null) {
                double value = ((Float) element).value;
                if (value!=0.0d) Arrays.fill(doublePrecisionArray, value);
            }
            return doublePrecisionArray;
        case JavaDouble:
            double[] doubleArray = new double[size];
            if (element!=null) {
                double value = (java.lang.Double) element;
                if (value!=0.0d) Arrays.fill(doubleArray, value);
            }
            return doubleArray;
        case CeylonCharacter:
            int[] codepointArray = new int[size];
            if (element!=null) {
                int value = ((Character) element).codePoint;
                if (value!=0) Arrays.fill(codepointArray, value);
            }
            return codepointArray;
        case JavaInteger:
            int[] intArray = new int[size];
            if (element!=null) {
                int intValue = (java.lang.Integer) element;
                if (intValue!=0) Arrays.fill(intArray, intValue);
            }
            return intArray;
        case CeylonByte:
            byte[] byteArray = new byte[size];
            if (element!=null) {
                byte value = ((Byte) element).value;
                if (value!=0) Arrays.fill(byteArray, value);
            }
            return byteArray;
        case JavaByte:
            byte[] bitsArray = new byte[size];
            byte value = (java.lang.Byte) element;
            if (value!=0) Arrays.fill(bitsArray, value);
            return bitsArray;
        case CeylonBoolean:
            boolean[] boolArray = new boolean[size];
            if (element!=null) {
                boolean boolValue = ((Boolean) element).booleanValue();
                if (boolValue!=false) Arrays.fill(boolArray, boolValue);
            }
            return boolArray;
        case JavaBoolean:
            boolean[] booleanArray = new boolean[size];
            if (element!=null) {
                boolean booleanValue = (java.lang.Boolean) element;
                if (booleanValue!=false) Arrays.fill(booleanArray, booleanValue);
            }
            return booleanArray;
        case JavaCharacter:
            char[] charArray = new char[size];
            if (element!=null) {
                char charValue = (java.lang.Character) element;
                if (charValue!=0) Arrays.fill(charArray, charValue);
            }
            return charArray;
        case JavaShort:
            short[] shortArray = new short[size];
            if (element!=null) {
                short shortValue = (java.lang.Short) element;
                if (shortValue!=0) Arrays.fill(shortArray, shortValue);
            }
            return shortArray;
        case JavaFloat:
            float[] floatArray = new float[size];
            if (element!=null) {
                float floatValue = (java.lang.Float) element;
                if (floatValue!=0.0f) Arrays.fill(floatArray, floatValue);
            }
            return floatArray;
        case CeylonString:
            //note: we don't unbox strings in an Array<String?>
            //      because it would break javaObjectArray()
            java.lang.String[] stringArray = new java.lang.String[size];
            if (element!=null) {
                String string = (String) element;
                Arrays.fill(stringArray, string.value);
            }
            return stringArray;
        default:
            java.lang.Class<?> elementClass = 
                    $reifiedElement.getArrayElementClass();
            java.lang.Object[] objectArray = (java.lang.Object[]) 
                    java.lang.reflect.Array.newInstance(elementClass, size);
            if (element!=null) {
                Arrays.fill(objectArray, element);
            }
            return objectArray;
        }
    }
        
    @Ignore
    private Array(@Ignore TypeDescriptor $reifiedElement, java.lang.Object array) {
        this.$reifiedElement = $reifiedElement;
        assert(array.getClass().isArray());
        elementType = elementType($reifiedElement);
        switch(elementType) {
        case Other:
            objectArray = (java.lang.Object[]) array;
            size = objectArray.length;
            longArray = null;
            doubleArray = null;
            intArray = null;
            booleanArray = null;
            byteArray = null;
            stringArray = null;
            break;
        case CeylonInteger:
            if (array==EMPTY_ARRAY) 
                array = EMPTY_LONG_ARRAY;
            longArray = (long[]) array;
            size = longArray.length;
            objectArray = null;
            doubleArray = null;
            intArray = null;
            booleanArray = null;
            byteArray = null;
            stringArray = null;
            break;
        case CeylonFloat:
            if (array==EMPTY_ARRAY) 
                array = EMPTY_DOUBLE_ARRAY;
            doubleArray = (double[]) array;
            size = doubleArray.length;
            objectArray = null;
            longArray = null;
            intArray = null;
            booleanArray = null;
            byteArray = null;
            stringArray = null;
            break;
        case CeylonByte:
            if (array==EMPTY_ARRAY) 
                array = EMPTY_BYTE_ARRAY;
            byteArray = (byte[]) array;
            intArray = null;
            size = byteArray.length;
            objectArray = null;
            longArray = null;
            doubleArray = null;
            booleanArray = null;
            stringArray = null;
            break;
        case CeylonCharacter:
            if (array==EMPTY_ARRAY) 
                array = EMPTY_INT_ARRAY;
            intArray = (int[]) array;
            size = intArray.length;
            objectArray = null;
            longArray = null;
            doubleArray = null;
            booleanArray = null;
            byteArray =  null;
            stringArray = null;
            break;
        case CeylonBoolean:
            if (array==EMPTY_ARRAY) 
                array = EMPTY_BOOLEAN_ARRAY;
            booleanArray = (boolean[]) array;
            size = booleanArray.length;
            objectArray = null;
            longArray = null;
            doubleArray = null;
            intArray = null;
            byteArray =  null;
            stringArray = null;
            break;
        case CeylonString:
            if (array==EMPTY_ARRAY) 
                array = EMPTY_STRING_ARRAY;
            stringArray = (java.lang.String[]) array;
            size = stringArray.length;
            objectArray = null;
            longArray = null;
            doubleArray = null;
            intArray = null;
            byteArray =  null;
            booleanArray = null;
            break;
        default:
            this.size = java.lang.reflect.Array.getLength(array);
            objectArray = null;
            longArray = null;
            doubleArray = null;
            booleanArray = null;
            intArray = null;
            byteArray =  null;
            stringArray = null;
        }
        this.array = array;
    }
    
    @Ignore
    public static <T> Array<T> instance(T[] array) {
        if (array == null) {
            return null;
        }
        java.lang.Class<?> componentType = array.getClass().getComponentType();
        TypeDescriptor optionalType = TypeDescriptor.union(Null.$TypeDescriptor$,
                TypeDescriptor.klass(componentType));
        return new Array<T>(optionalType, array);
    }
    
    private static final TypeDescriptor CHAR_TYPE = 
            TypeDescriptor.klass(java.lang.Character.class);
    @Ignore
    public static Array<java.lang.Character> instance(char[] array) {
        if (array == null) {
            return null;
        }
        return new Array<java.lang.Character>(CHAR_TYPE, array);
    }

    private static final TypeDescriptor BYTE_TYPE = 
            TypeDescriptor.klass(java.lang.Byte.class);
    @Ignore
    public static Array<java.lang.Byte> instance(byte[] array) {
        if (array == null) {
            return null;
        }
        return new Array<java.lang.Byte>(BYTE_TYPE, array);
    }

    private static final TypeDescriptor SHORT_TYPE = 
            TypeDescriptor.klass(java.lang.Short.class);
    @Ignore
    public static Array<java.lang.Short> instance(short[] array) {
        if (array == null) {
            return null;
        }
        return new Array<java.lang.Short>(SHORT_TYPE, array);
    }
    
    private static final TypeDescriptor INT_TYPE = 
            TypeDescriptor.klass(java.lang.Integer.class);
    @Ignore
    public static Array<java.lang.Integer> instance(int[] array) {
        if (array == null) {
            return null;
        }
        return new Array<java.lang.Integer>(INT_TYPE, array);
    }
    
    private static final TypeDescriptor LONG_TYPE = 
            TypeDescriptor.klass(java.lang.Long.class);
    @Ignore
    public static Array<java.lang.Long> instance(long[] array) {
        if (array == null) {
            return null;
        }
        return new Array<java.lang.Long>(LONG_TYPE, array);
    }
    
    private static final TypeDescriptor FLOAT_TYPE = 
            TypeDescriptor.klass(java.lang.Float.class);
    @Ignore
    public static Array<java.lang.Float> instance(float[] array) {
        if (array == null) {
            return null;
        }
        return new Array<java.lang.Float>(FLOAT_TYPE, array);
    }
    
    private static final TypeDescriptor DOUBLE_TYPE = 
            TypeDescriptor.klass(java.lang.Double.class);
    @Ignore
    public static Array<java.lang.Double> instance(double[] array) {
        if (array == null) {
            return null;
        }
        return new Array<java.lang.Double>(DOUBLE_TYPE, array);
    }
    
    private static final TypeDescriptor BOOLEAN_TYPE = 
            TypeDescriptor.klass(java.lang.Boolean.class);
    @Ignore
    public static Array<java.lang.Boolean> instance(boolean[] array) {
        if (array == null) {
            return null;
        }
        return new Array<java.lang.Boolean>(BOOLEAN_TYPE, array);
    }
    
    @Ignore
    public static Array<Float> instanceForFloats(double[] array) {
        if (array == null) {
            return null;
        }
        return new Array<Float>(Float.$TypeDescriptor$, array);
    }

    @Ignore
    public static Array<Boolean> instanceForBooleans(boolean[] array) {
        if (array == null) {
            return null;
        }
        return new Array<Boolean>(Boolean.$TypeDescriptor$, array);
    }
    
    @Ignore
    public static Array<Byte> instanceForBytes(byte[] array) {
        if (array == null) {
            return null;
        }
        return new Array<Byte>(Byte.$TypeDescriptor$, array);
    }
    
    @Ignore
    public static Array<Character> instanceForCodePoints(int[] array) {
        if (array == null) {
            return null;
        }
        return new Array<Character>(Character.$TypeDescriptor$, array);
    }

    @Ignore
    public static Array<Integer> instanceForIntegers(long[] array) {
        if (array == null) {
            return null;
        }
        return new Array<Integer>(Integer.$TypeDescriptor$, array);
    }

    @Override
    public Array<Element> spanFrom(@Name("from") Integer from) {
        return span(from, Integer.instance(size));
    }
    
    @Override
    public Array<Element> spanTo(@Name("to") Integer to) {
        return span(Integer.instance(0), to);
    }
    
    @Override
    public Array<Element> span(@Name("from") Integer from,
            @Name("to") Integer to) {
        long fromIndex = from.longValue(); //inclusive
        long toIndex = to.longValue(); //inclusive
        boolean revert = toIndex < fromIndex;
        if (revert) {
            long swap = toIndex;
            toIndex = fromIndex;
            fromIndex = swap;
        }
        if (fromIndex<0) {
            fromIndex = 0;
        }
        if (toIndex>=size) {
            toIndex = size-1;
        }
        if (fromIndex>=size || toIndex<0 || toIndex<fromIndex) {
            return new Array<Element>($reifiedElement, EMPTY_ARRAY);
        }
        else {
            int resultFromIndex = toInt(fromIndex); //inclusive
            int resultToIndex = toInt(toIndex+1); //exclusive
            java.lang.Object newArray;
            if (array instanceof char[]) {
                char[] copy = copyOfRange((char[])array, 
                        resultFromIndex, resultToIndex);
                if (revert) {
                    for (int i = 0; i<copy.length/2; i++) {
                        char temp = copy[i];
                        copy[i] = copy[copy.length-1-i];
                        copy[copy.length-1-i] = temp;
                    }
                }
                newArray = copy;
            }
            else if (array instanceof byte[]) {
                byte[] copy = copyOfRange((byte[])array, 
                        resultFromIndex, resultToIndex);
                if (revert) {
                    for (int i = 0; i<copy.length/2; i++) {
                        byte temp = copy[i];
                        copy[i] = copy[copy.length-1-i];
                        copy[copy.length-1-i] = temp;
                    }
                }
                newArray = copy;
            }
            else if (array instanceof short[]) {
                short[] copy = copyOfRange((short[])array, 
                        resultFromIndex, resultToIndex);
                if (revert) {
                    for (int i = 0; i<copy.length/2; i++) {
                        short temp = copy[i];
                        copy[i] = copy[copy.length-1-i];
                        copy[copy.length-1-i] = temp;
                    }
                }
                newArray = copy;
            }
            else if (array instanceof int[]) {
                int[] copy = copyOfRange((int[])array, 
                        resultFromIndex, resultToIndex);
                if (revert) {
                    for (int i = 0; i<copy.length/2; i++) {
                        int temp = copy[i];
                        copy[i] = copy[copy.length-1-i];
                        copy[copy.length-1-i] = temp;
                    }
                }
                newArray = copy;
            }
            else if (array instanceof long[]) {
                long[] copy = copyOfRange((long[])array, 
                    resultFromIndex, resultToIndex);
                if (revert) {
                    for (int i = 0; i<copy.length/2; i++) {
                        long temp = copy[i];
                        copy[i] = copy[copy.length-1-i];
                        copy[copy.length-1-i] = temp;
                    }
                }
                newArray = copy;
            }
            else if (array instanceof float[]) {
                float[] copy = copyOfRange((float[])array, 
                    resultFromIndex, resultToIndex);
                if (revert) {
                    for (int i = 0; i<copy.length/2; i++) {
                        float temp = copy[i];
                        copy[i] = copy[copy.length-1-i];
                        copy[copy.length-1-i] = temp;
                    }
                }
                newArray = copy;
            }
            else if (array instanceof double[]) {
                double[] copy = copyOfRange((double[])array, 
                    resultFromIndex, resultToIndex);
                if (revert) {
                    for (int i = 0; i<copy.length/2; i++) {
                        double temp = copy[i];
                        copy[i] = copy[copy.length-1-i];
                        copy[copy.length-1-i] = temp;
                    }
                }
                newArray = copy;
            }
            else if (array instanceof boolean[]) {
                boolean[] copy = copyOfRange((boolean[])array, 
                    resultFromIndex, resultToIndex);
                if (revert) {
                    for (int i = 0; i<copy.length/2; i++) {
                        boolean temp = copy[i];
                        copy[i] = copy[copy.length-1-i];
                        copy[copy.length-1-i] = temp;
                    }
                }
                newArray = copy;
            }
            else {
                java.lang.Object[] copy = 
                        copyOfRange((java.lang.Object[])array, 
                    resultFromIndex, resultToIndex);
                if (revert) {
                    for (int i = 0; i<copy.length/2; i++) {
                        java.lang.Object temp = copy[i];
                        copy[i] = copy[copy.length-1-i];
                        copy[copy.length-1-i] = temp;
                    }
                }
                newArray = copy;
            }
            return new Array<Element>($reifiedElement, newArray);
        }
    }

    @Override
    public Array<Element> measure(@Name("from") Integer from,
            @Name("length") long length) {
        long fromIndex = from.longValue(); //inclusive
        long toIndex = fromIndex + length; //exclusive
        if (fromIndex<0) {
            fromIndex=0;
        }
        if (toIndex > size) {
            toIndex = size;
        }
        if (fromIndex>=size || toIndex<=0) {
            return new Array<Element>($reifiedElement, EMPTY_ARRAY);
        }
        else {
            int resultToIndex = toInt(toIndex);
            int resultFromIndex = toInt(fromIndex);
            java.lang.Object newArray;
            if (array instanceof char[]) {
                newArray = copyOfRange((char[])array, 
                    resultFromIndex, resultToIndex);
            }
            else if (array instanceof byte[]) {
                newArray = copyOfRange((byte[])array, 
                        resultFromIndex, resultToIndex);
            }
            else if (array instanceof short[]) {
                newArray = copyOfRange((short[])array, 
                        resultFromIndex, resultToIndex);
            }
            else if (array instanceof int[]) {
                newArray = copyOfRange((int[])array, 
                        resultFromIndex, resultToIndex);
            }
            else if (array instanceof long[]) {
                newArray = copyOfRange((long[])array, 
                        resultFromIndex, resultToIndex);
            }
            else if (array instanceof float[]) {
                newArray = copyOfRange((float[])array, 
                        resultFromIndex, resultToIndex);
            }
            else if (array instanceof double[]) {
                newArray = copyOfRange((double[])array, 
                        resultFromIndex, resultToIndex);
            }
            else if (array instanceof boolean[]) {
                newArray = copyOfRange((boolean[])array, 
                        resultFromIndex, resultToIndex);
            }
            else {
                newArray = copyOfRange((java.lang.Object[])array, 
                        resultFromIndex, resultToIndex);
            }
            return new Array<Element>($reifiedElement, newArray);
        }
    }

    @Override
    @TypeInfo("ceylon.language::Null|ceylon.language::Integer")
    public Integer getLastIndex() {
        return getEmpty() ? null : Integer.instance(size - 1);
    }

    @Override
    public boolean getEmpty() {
        return size == 0;
    }

    @Override
    public long getSize() {
        return size;
    }

    @Override
    public boolean defines(@Name("index") Integer key) {
        long ind = key.longValue();
        return ind >= 0 && ind < size;
    }
    
    @Ignore
    private final class CoalescedArrayIterator 
    extends BaseIterator<Element> {

        private int index = 0;
        // ok to cast here, since we know the size must fit in an int
        
        CoalescedArrayIterator(TypeDescriptor $reified$Element) {
            super($reified$Element);
        }
        
        @Override
        public java.lang.Object next() {
            if (index<size) {
                Element result;
                boolean isNull;
                do {
                    result = unsafeItem(index++);
                    isNull = result==null;
                }
                while (isNull && index<size);
                return isNull ? FIN : result;
            }
            else {
                return FIN;
            }
        }

        @Override
        public java.lang.String toString() {
            return Array.this.toString() + ".coalesced.iterator()";
        }
    }
    
    @Ignore
    private final class CoalescedArrayIterable 
    extends BaseIterable<Element, java.lang.Object>
    implements Iterable<Element, java.lang.Object> {
        
        private final TypeDescriptor $reifiedElement;
        
        CoalescedArrayIterable(TypeDescriptor $reifiedElement) {
            super($reifiedElement, Null.$TypeDescriptor$);
            this.$reifiedElement = $reifiedElement;
        }
        
        @Override
        public Iterator<? extends Element> iterator() {
            return new CoalescedArrayIterator($reifiedElement);
        }
        
    }
    
    @Override
    @TypeInfo("ceylon.language::Iterable<Element&ceylon.language::Object,ceylon.language::Null>")
    public Iterable<? extends Element, ? extends java.lang.Object> getCoalesced() {
        return new CoalescedArrayIterable(intersection($reifiedElement, Object.$TypeDescriptor$));
    }
    
    @Ignore
    private final class ArrayIterator extends BaseIterator<Element> {
        
        private int index = 0;
        // ok to cast here, since we know the size must fit in an int
        
        private ArrayIterator(TypeDescriptor $reified$Element) {
            super($reified$Element);
        }

        @Override
        public java.lang.Object next() {
            if (index<size) {
                return unsafeItem(index++);
            }
            else {
                return FIN;
            }
        }

        @Override
        public java.lang.String toString() {
            return Array.this.toString() + ".iterator()";
        }
    }
    
    @Ignore
    final class ArrayIterable 
    extends AbstractArrayIterable<Element, java.lang.Object> {

        ArrayIterable() {
            // ok to cast here, since we know the size must fit in an int
            super($reifiedElement, array, (int)size);
        }
        
        protected ArrayIterable(java.lang.Object array, int start,
                int len, int step) {
            super($reifiedElement, array, start, len, step);
        }

        @Override
        protected ArrayIterable newInstance(java.lang.Object array, 
                int start, int len, int step) {
            return new ArrayIterable(array, start, len, step);
        }

        @Override
        protected Element get(java.lang.Object array, int index) {
            return unsafeItem(index);
        }
        
        /** 
         * If this is an Iterable over an {@code Array<Character>} 
         * (wrapping a {@code int[]}) with unit step size, then 
         * returns a String of those codepoints. 
         * Otherwise returns null.
         */
        @Ignore
        java.lang.String stringValue() {
            if (array instanceof int[]
                    && step == 1) {
                // ok to cast here, since we know the size must fit in an int
                return new java.lang.String((int[])array, start, 
                        (int)this.getSize());
            } 
            return null;
        }
    }
    
    @Override
    public Iterator<Element> iterator() {
        return new ArrayIterator($reifiedElement);
    }
    
//    @Override
//    @TypeInfo("ceylon.language::Null|Element")
//    public Element get(@Name("index") Integer key) {
//        return getFromFirst(key.longValue());
//    }

    @Override
    @TypeInfo("ceylon.language::Null|Element")
    public Element getFromLast(@Name("index") long index) {
        if (index < 0 || index >= size) {
            return null;
        }
        else {
            //typecast is safe because we just checked
            return unsafeItem(size-1-(int) index);
        }
    }
    
    @Override
    @TypeInfo("ceylon.language::Null|Element")
    @SuppressWarnings("unchecked")
    public Element getFromFirst(@Name("index") long index) {
        if (index < 0 || index >= size) {
            return null;
        }
        else {
            //typecast is safe because we just checked
            int i = (int) index;
            if (objectArray!=null)
                return (Element) objectArray[i];
            else if (longArray!=null)
                return (Element) Integer.instance(longArray[i]);
            else if (doubleArray!=null)
                return (Element) Float.instance(doubleArray[i]);
            else if (byteArray!=null)
                return (Element) Byte.instance(byteArray[i]);
            else if (booleanArray!=null)
                return (Element) Boolean.instance(booleanArray[i]);
            else if (intArray!=null)
                return (Element) Character.instance(intArray[i]);
            else if (stringArray!=null)
                return (Element) String.instance(stringArray[i]);
            else
                return getJavaItem(i);
        }
    }
    
    /*@Override
    @TypeInfo("ceylon.language::Entry<ceylon.language::Boolean,ceylon.language::Null|Element>")
    public Entry<? extends Boolean, ? extends Element> lookup(@Name("index") Integer index) {
        long ind = index.longValue();
        boolean defined = ind >= 0 && ind < size;
        Element item = defined ? 
                unsafeItem((int) ind) : null;
        return new Entry<Boolean,Element>(
                Integer.$TypeDescriptor$, 
                $reifiedElement,
                Boolean.instance(defined), 
                item);
    }*/
    
    // Used by the jvm backend code to avoid boxing the index
    @SuppressWarnings("unchecked")
    @Ignore
    public final Element unsafeItem(int index) {
        if (objectArray!=null)
            return (Element) objectArray[index];
        else if (longArray!=null)
            return (Element) Integer.instance(longArray[index]);
        else if (doubleArray!=null)
            return (Element) Float.instance(doubleArray[index]);
        else if (byteArray!=null)
            return (Element) Byte.instance(byteArray[index]);
        else if (booleanArray!=null)
            return (Element) Boolean.instance(booleanArray[index]);
        else if (intArray!=null)
            return (Element) Character.instance(intArray[index]);
        else if (stringArray!=null)
            return (Element) String.instance(stringArray[index]);
        else
            return getJavaItem(index);
    }
    
    @SuppressWarnings("unchecked")
    private Element getJavaItem(int index) {
        switch (elementType) {
        case JavaLong:
            return (Element) (java.lang.Long) ((long[])array)[index];
        case JavaDouble:
            return (Element) (java.lang.Double) ((double[])array)[index];
        case JavaInteger:
            return (Element) (java.lang.Integer) ((int[])array)[index];
        case JavaByte:
            return (Element) (java.lang.Byte) ((byte[])array)[index];
        case JavaBoolean:
            return (Element) (java.lang.Boolean) ((boolean[])array)[index];
        case JavaCharacter:
            return (Element) (java.lang.Character) ((char[])array)[index];
        case JavaShort:
            return (Element) (java.lang.Short) ((short[])array)[index];
        case JavaFloat:
            return (Element) (java.lang.Float) ((float[])array)[index];
        case JavaString:
            return (Element) ((java.lang.String[])array)[index];
        default: 
            throw new AssertionError("unknown element type");
        }
    }

    @Override
    public boolean occursAt(
            @Name("index") long ind, 
            @Name("element") Element element) {
        if (ind<0 || ind>=size) {
            return false;
        }
        int index = (int) ind;
        if (objectArray!=null) {
            java.lang.Object obj = objectArray[index];
            return obj==element || 
                    obj!=null && element!=null
                    && obj.equals(element);
        }
        else if (longArray!=null)
            return Integer.equals(longArray[index], element);
        else if (doubleArray!=null)
            return Float.equals(doubleArray[index], element);
        else if (byteArray!=null)
            return Byte.equals(byteArray[index], element);
        else if (booleanArray!=null)
            return Boolean.equals(booleanArray[index], element);
        else if (intArray!=null)
            return Character.equals(intArray[index], element);
        else if (stringArray!=null)
            return String.equals(stringArray[index], element);
        else
            return javaItemOccursAt(index, element);
    }
    
    private boolean javaItemOccursAt(int index, Element element) {
        switch (elementType) {
        case JavaLong:
            if (element instanceof java.lang.Long) {
                return ((long[])array)[index]==((java.lang.Long)element).longValue();
            }
        case JavaDouble:
            if (element instanceof java.lang.Double) {
                return ((double[])array)[index]==((java.lang.Double)element).doubleValue();
            }
        case JavaInteger:
            if (element instanceof java.lang.Integer) {
                return ((int[])array)[index]==((java.lang.Integer)element).intValue();
            }
        case JavaByte:
            if (element instanceof java.lang.Byte) {
                return ((byte[])array)[index]==((java.lang.Byte)element).byteValue();
            }
        case JavaBoolean:
            if (element instanceof java.lang.Boolean) {
                return ((boolean[])array)[index]==((java.lang.Boolean)element).booleanValue();
            }
        case JavaCharacter:
            if (element instanceof java.lang.Character) {
                return ((char[])array)[index]==((java.lang.Character)element).charValue();
            }
        case JavaShort:
            if (element instanceof java.lang.Short) {
                return ((short[])array)[index]==((java.lang.Short)element).shortValue();
            }
        case JavaFloat:
            if (element instanceof java.lang.Float) {
                return ((float[])array)[index]==((java.lang.Float)element).floatValue();
            }
        case JavaString:
            if (element instanceof java.lang.String) {
                return ((java.lang.String[])array)[index].equals(element);
            }
        default: 
            return false;
        }
    }

    public void set(
            @Name("index") @TypeInfo("ceylon.language::Integer") long index,
            @Name("element") @TypeInfo("Element") Element element) {
        if (index<0) {
            throw new AssertionError("array index " + index + 
                    " may not be negative");
        }
        else if (index>=size) {
            throw new AssertionError("array index " + index + 
                    " must be less than size of array " + size);
        }
        else {
            int idx = (int) index; //typecast is safe 'cos we just checked above
            if (objectArray!=null)
                objectArray[idx] = element;
            else if (longArray!=null)
                longArray[idx] = ((Integer) element).value;
            else if (doubleArray!=null)
                doubleArray[idx] = ((Float) element).value;
            else if (byteArray!=null)
                byteArray[idx] = ((Byte) element).value;
            else if (booleanArray!=null)
                booleanArray[idx] = ((Boolean) element).booleanValue();
            else if (intArray!=null)
                intArray[idx] = ((Character) element).codePoint;
            else if (stringArray!=null)
                stringArray[idx] = ((String) element).value;
            else
                setJavaItem(element, idx);
        }
    }
    
    public void swap(
            @Name("i") @TypeInfo("ceylon.language::Integer") long p,
            @Name("j") @TypeInfo("ceylon.language::Integer") long q) {
        if (p<0 || q<0) {
            throw new AssertionError("array index may not be negative");
        }
        else if (p>=size || q>=size) {
            throw new AssertionError("array index must be less than size of array " + size);
        }
        int i = (int) p;
        int j = (int) q;
        if (i==j) return;
        if (array instanceof java.lang.Object[]) {
            java.lang.Object[] objectArray = (java.lang.Object[]) array;
            java.lang.Object x = objectArray[i];
            java.lang.Object y = objectArray[j];
            objectArray[j] = x;
            objectArray[i] = y;
        }
        else if (array instanceof long[]) {
            long[] longArray = (long[]) array;
            long x = longArray[i];
            long y = longArray[j];
            longArray[j] = x;
            longArray[i] = y;
        }
        else if (array instanceof int[]) {
            int[] intArray = (int[]) array;
            int x = intArray[i];
            int y = intArray[j];
            intArray[j] = x;
            intArray[i] = y;
        }
        else if (array instanceof short[]) {
            short[] shortArray = (short[]) array;
            short x = shortArray[i];
            short y = shortArray[j];
            shortArray[j] = x;
            shortArray[i] = y;
        }
        else if (array instanceof byte[]) {
            byte[] byteArray = (byte[]) array;
            byte x = byteArray[i];
            byte y = byteArray[j];
            byteArray[j] = x;
            byteArray[i] = y;
        }
        else if (array instanceof boolean[]) {
            boolean[] booleanArray = (boolean[]) array;
            boolean x = booleanArray[i];
            boolean y = booleanArray[j];
            booleanArray[j] = x;
            booleanArray[i] = y;
        }
        else if (array instanceof double[]) {
            double[] doubleArray = (double[]) array;
            double x = doubleArray[i];
            double y = doubleArray[j];
            doubleArray[j] = x;
            doubleArray[i] = y;
        }
        else if (array instanceof float[]) {
            float[] floatArray = (float[]) array;
            float x = floatArray[i];
            float y = floatArray[j];
            floatArray[j] = x;
            floatArray[i] = y;
        }
        else {
            throw new AssertionError("impossible array type");
        }
    }

    public void move(
            @Name("from") @TypeInfo("ceylon.language::Integer") long f,
            @Name("to") @TypeInfo("ceylon.language::Integer") long t) {
        if (f<0 || t<0) {
            throw new AssertionError("array index may not be negative");
        }
        else if (f>=size || t>=size) {
            throw new AssertionError("array index must be less than size of array " + size);
        }
        int from = (int) f;
        int to = (int) t;
        if (from==to) return;
        int len;
        int srcPos;
        int destPos;
        if (from>to) {
            len = from-to;
            srcPos = to;
            destPos = to+1;
        }
        else {
            len = to-from;
            srcPos = from+1;
            destPos = from;
        }
        if (array instanceof java.lang.Object[]) {
            java.lang.Object[] objectArray = (java.lang.Object[]) array;
            java.lang.Object x = objectArray[from];
            System.arraycopy(objectArray, srcPos, objectArray, destPos, len);
            objectArray[to] = x;
        }
        else if (array instanceof long[]) {
            long[] longArray = (long[]) array;
            long x = longArray[from];
            System.arraycopy(longArray, srcPos, longArray, destPos, len);
            longArray[to] = x;
        }
        else if (array instanceof int[]) {
            int[] intArray = (int[]) array;
            int x = intArray[from];
            System.arraycopy(intArray, srcPos, intArray, destPos, len);
            intArray[to] = x;
        }
        else if (array instanceof short[]) {
            short[] shortArray = (short[]) array;
            short x = shortArray[from];
            System.arraycopy(shortArray, srcPos, shortArray, destPos, len);
            shortArray[to] = x;
        }
        else if (array instanceof byte[]) {
            byte[] byteArray = (byte[]) array;
            byte x = byteArray[from];
            System.arraycopy(byteArray, srcPos, byteArray, destPos, len);
            byteArray[to] = x;
        }
        else if (array instanceof boolean[]) {
            boolean[] booleanArray = (boolean[]) array;
            boolean x = booleanArray[from];
            System.arraycopy(booleanArray, srcPos, booleanArray, destPos, len);
            booleanArray[to] = x;
        }
        else if (array instanceof double[]) {
            double[] doubleArray = (double[]) array;
            double x = doubleArray[from];
            System.arraycopy(doubleArray, srcPos, doubleArray, destPos, len);
            doubleArray[to] = x;
        }
        else if (array instanceof float[]) {
            float[] floatArray = (float[]) array;
            float x = floatArray[from];
            System.arraycopy(floatArray, srcPos, floatArray, destPos, len);
            floatArray[to] = x;
        }
        else {
            throw new AssertionError("impossible array type");
        }
    }

    private void setJavaItem(Element element, int idx) {
        switch (elementType) {
        case JavaLong:
            ((long[]) array)[idx] = (java.lang.Long) element;
            break;
        case JavaDouble:
            ((double[]) array)[idx] = (java.lang.Double) element;
            break;
        case JavaInteger:
            ((int[]) array)[idx] = (java.lang.Integer) element;
            break;
        case JavaByte:
            ((byte[]) array)[idx] = (java.lang.Byte) element;
            break;
        case JavaBoolean:
            ((boolean[]) array)[idx] = (java.lang.Boolean) element;
            break;
        case JavaCharacter:
            ((char[]) array)[idx] = (java.lang.Character) element;
            break;
        case JavaShort:
            ((short[]) array)[idx] = (java.lang.Short) element;
            break;
        case JavaFloat:
            ((float[]) array)[idx] = (java.lang.Float) element;
            break;
        case JavaString:
            ((java.lang.String[]) array)[idx] = (java.lang.String) element;
            break;
        default:
            throw new AssertionError("unknown element type");
        }
    }

    
    @Override
    public Array<Element> $clone() {
        return new Array<Element>($reifiedElement, copyArray());
    }
    
    private java.lang.Object copyArray() {
        if (array instanceof java.lang.Object[]) {
            return Arrays.copyOf((java.lang.Object[]) array, 
                    ((java.lang.Object[]) array).length);
        }
        else if (array instanceof long[]) {
            return Arrays.copyOf((long[]) array, 
                    ((long[]) array).length);
        }
        else if (array instanceof double[]) {
            return Arrays.copyOf((double[]) array, 
                    ((double[]) array).length);
        }
        else if (array instanceof boolean[]) {
            return Arrays.copyOf((boolean[]) array, 
                    ((boolean[]) array).length);
        }
        else if (array instanceof int[]) {
            return Arrays.copyOf((int[]) array, 
                    ((int[]) array).length);
        }
        else if (array instanceof byte[]) {
            return Arrays.copyOf((byte[]) array, 
                    ((byte[]) array).length);
        }
        else if (array instanceof short[]) {
            return Arrays.copyOf((short[]) array, 
                    ((short[]) array).length);
        }
        else if (array instanceof float[]) {
            return Arrays.copyOf((float[]) array, 
                    ((float[]) array).length);
        }
        else if (array instanceof char[]) {
            return Arrays.copyOf((char[]) array, 
                    ((char[]) array).length);
        }
        else {
            throw new AssertionError("impossible array type");
        }
    }

    @Ignore
    public java.lang.Object toArray() {
        return array;
    }

    @Override
    public boolean contains(@Name("element") 
    @TypeInfo("ceylon.language::Object")
    java.lang.Object element) {
        // FIXME Very inefficient for primitive types due to boxing
        for (int i=0; i<size; i++) {
            Element elem = unsafeItem(i);
            if (elem != null && elem.equals(element)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public long count(@Name("selecting")
    @FunctionalParameter("(element)") 
    @TypeInfo("ceylon.language::Boolean(Element)")
    Callable<? extends Boolean> selecting) {
        // FIXME Very inefficient for primitive types due to boxing
        int count=0;
        for (int i=0; i<size; i++) {
            if (selecting.$call$(unsafeItem(i)).booleanValue()) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean any(@Name("selecting")
    @FunctionalParameter("(element)") 
    @TypeInfo("ceylon.language::Boolean(Element)")
    Callable<? extends Boolean> selecting) {
        for (int i=0; i<size; i++) {
            if (selecting.$call$(unsafeItem(i)).booleanValue()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean every(@Name("selecting")
    @FunctionalParameter("(element)") 
    @TypeInfo("ceylon.language::Boolean(Element)")
    Callable<? extends Boolean> selecting) {
        for (int i=0; i<size; i++) {
            if (!selecting.$call$(unsafeItem(i)).booleanValue()) {
                return false;
            }
        }
        return true;
    }

    @Override
    @TypeInfo("ceylon.language::Null|Element")
    public Element find(
            @Name("selecting")
            @FunctionalParameter("(element)") 
            @TypeInfo("ceylon.language::Boolean(Element&ceylon.language::Object)")
            Callable<? extends Boolean> selecting) {
        for (int i=0; i<size; i++) {
            Element elem = unsafeItem(i);
            if (elem!=null &&
                    selecting.$call$(elem).booleanValue()) {
                return elem;
            }
        }
        return null;
    }
    
    @Override
    @TypeInfo("ceylon.language::Null|Element")
    public Element findLast(
            @Name("selecting")
            @FunctionalParameter("(element)") 
            @TypeInfo("ceylon.language::Boolean(Element&ceylon.language::Object)")
    Callable<? extends Boolean> selecting) {
        for (int i=size-1; i>=0; i--) {
            Element elem = unsafeItem(i);
            if (elem!=null &&
                    selecting.$call$(elem).booleanValue()) {
                return elem;
            }
        }
        return null;
    }
    
    @Override @TypeInfo("ceylon.language::Integer?")
    public Integer firstIndexWhere(
            @Name("selecting")
            @FunctionalParameter("(element)") 
            @TypeInfo("ceylon.language::Boolean(Element&ceylon.language::Object)")
            Callable<? extends Boolean> selecting) {
        for (int i=0; i<size; i++) {
            Element elem = unsafeItem(i);
            if (elem!=null &&
                    selecting.$call$(elem).booleanValue()) {
                return Integer.instance(i);
            }
        }
        return null;
    }

    @Override @TypeInfo("ceylon.language::Integer?")
    public Integer lastIndexWhere(
            @Name("selecting")
            @FunctionalParameter("(element)") 
            @TypeInfo("ceylon.language::Boolean(Element&ceylon.language::Object)")
            Callable<? extends Boolean> selecting) {
        for (int i=size-1; i>=0; i--) {
            Element elem = unsafeItem(i);
            if (elem!=null &&
                    selecting.$call$(elem).booleanValue()) {
                return Integer.instance(i);
            }
        }
        return null;
    }

    @Override
    @TypeInfo("ceylon.language::Null|ceylon.language::Entry<ceylon.language::Integer,Element&ceylon.language::Object>")
    public Entry<? extends Integer,? extends Element> 
    locate(
            @Name("selecting")
            @FunctionalParameter("(element)") 
            @TypeInfo("ceylon.language::Boolean(Element&ceylon.language::Object)")
            Callable<? extends Boolean> selecting) {
        for (int i=0; i<size; i++) {
            Element elem = unsafeItem(i);
            if (elem!=null &&
                    selecting.$call$(elem).booleanValue()) {
                return new Entry<Integer,Element>(
                        Integer.$TypeDescriptor$, $reifiedElement, 
                        Integer.instance(i), elem);
            }
        }
        return null;
    }
    
    @Override
    @TypeInfo("ceylon.language::Null|ceylon.language::Entry<ceylon.language::Integer,Element&ceylon.language::Object>")
    public Entry<? extends Integer,? extends Element> 
    locateLast(
            @Name("selecting")
            @FunctionalParameter("(element)") 
            @TypeInfo("ceylon.language::Boolean(Element&ceylon.language::Object)")
            Callable<? extends Boolean> selecting) {
        for (int i=size-1; i>=0; i--) {
            Element elem = unsafeItem(i);
            if (elem!=null &&
                    selecting.$call$(elem).booleanValue()) {
                return new Entry<Integer,Element>(
                        Integer.$TypeDescriptor$, $reifiedElement, 
                        Integer.instance(i), elem);
            }
        }
        return null;
    }
    @Override
    @TypeInfo("Result|Element|ceylon.language::Null")
    public <Result> java.lang.Object 
    reduce(@Ignore TypeDescriptor $reifiedResult,
            @Name("accumulating")
            @FunctionalParameter("(partial,element)") 
            @TypeInfo("ceylon.language::Callable<Result,ceylon.language::Tuple<Result|Element,Result|Element,ceylon.language::Tuple<Element,Element,ceylon.language::Empty>>>")
            Callable<? extends Result> accumulating) {
        if (size==0) {
            return null;
        }
        java.lang.Object partial = unsafeItem(0);
        for (int i=1; i<size; i++) {
            partial = accumulating.$call$(partial,unsafeItem(i));
        }
        return partial;
    }
    
    @Override
    @Annotations({ @Annotation("actual") })
    @TypeInfo("ceylon.language::Null|Element")
    public Element getFirst() {
        if (size>0) {
            return unsafeItem(0);
        }
        else {
            return null;
        }
    }
    
    @Override
    @Annotations({ @Annotation("actual") })
    @TypeInfo("ceylon.language::Null|Element")
    public Element getLast() {
        return size > 0 ? unsafeItem(size-1) : null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    @TypeInfo("ceylon.language::Sequential<Element>")
    public Sequential<? extends Element> 
    sort(@Name("comparing") @FunctionalParameter("(x,y)")
    @TypeInfo("ceylon.language::Comparison(Element,Element)") 
    Callable<? extends Comparison> comparing) {
        if (getEmpty()) {
            return (Sequential<? extends Element>) empty_.get_();
        }
        Array<Element> clone = $clone();
        clone.sortInPlace(comparing);
        return new ArraySequence<Element>($reifiedElement, clone);
    }
    /*@SuppressWarnings("unchecked")
    @TypeInfo("ceylon.language::Sequential<Element>")
    public Sequential<? extends Element> 
    reverse() {
        if (getEmpty()) {
            return (Sequential<? extends Element>) empty_.get_();
        }
        Array<Element> clone = $clone();
        clone.reverseInPlace();
        return new ArraySequence<Element>($reifiedElement, clone);
    }*/
    
    @Override
    @Annotations({ @Annotation("actual") })
    @TypeInfo("ceylon.language::Iterable<Element,ceylon.language::Null>")
    public Iterable<? extends Element, ?> 
    skip(@Name("skipping") long skipping) {
        int intSkip = toInt(skipping);
        // ok to cast here, since we know the size must fit in an int
        int length = (int) size;
        if (skipping <= 0) {
            return this;
        }
        return new ArrayIterable(this.array, intSkip, 
                Math.max(0, length-intSkip), 1);
    }

    @Override
    @Annotations({ @Annotation("actual") })
    @TypeInfo("ceylon.language::Iterable<Element,ceylon.language::Null>")
    public Iterable<? extends Element, ?> 
    take(@Name("taking") long taking) {
        // ok to cast here, since we know the size must fit in an int
        int length = (int)size;
        if (taking >= length) {
            return this;
        }
        return new ArrayIterable(this.array, 0, 
                Math.max(toInt(taking), 0), 1);
    }

    @Override
    @Annotations({ @Annotation("actual") })
    @TypeInfo("ceylon.language::Iterable<Element,ceylon.language::Null>")
    public Iterable<? extends Element, ?> 
    by(@Name("step") long step) {
        if (step <= 0) {
            throw new AssertionError("step size must be greater than zero");
        }
        else if (step == 1) {
            return this;
        }
        return new ArrayIterable(array, 0, 
                toInt((size+step-1)/step), 
                toInt(step));
    }
    
    @SuppressWarnings("unchecked")
    @Override 
    @TypeInfo("ceylon.language::Sequential<Element>")
    public Sequential<? extends Element> sequence() {
        if (getEmpty()) {
            return (Sequential<? extends Element>) empty_.get_();
        }
        else {
            return new ArraySequence<Element>($reifiedElement, 
                    new Array<Element>($reifiedElement, copyArray()));
        }
    }
    
    @Ignore
    public long copyTo$sourcePosition(Array<Element> destination){
        return 0L;
    }

    @Ignore
    public long copyTo$destinationPosition(Array<Element> destination, 
            long sourcePosition){
        return 0L;
    }

    @Ignore
    public long copyTo$length(Array<Element> destination, 
            long sourcePosition, long destinationPosition){
        return Math.min(size-sourcePosition,
                destination.getSize()-destinationPosition);
    }

    @Ignore
    public void copyTo(Array<Element> destination){
        copyTo(destination, 0L, 0L);
    }

    @Ignore
    public void copyTo(Array<Element> destination, long sourcePosition) {
        copyTo(destination, sourcePosition, 0L);
    }

    @Ignore
    public void copyTo(Array<Element> destination, 
            long sourcePosition, long destinationPosition) {
        copyTo(destination, sourcePosition, destinationPosition, 
                copyTo$length(destination, sourcePosition, destinationPosition));
    }

    public void copyTo(@Name("destination") Array<Element> destination, 
                       @Name("sourcePosition") @Defaulted long sourcePosition, 
                       @Name("destinationPosition") @Defaulted long destinationPosition, 
                       @Name("length") @Defaulted long length) {
        if (length>0) {
            arraycopy(array, Util.toInt(sourcePosition), destination.array, 
                    Util.toInt(destinationPosition), Util.toInt(length));
        }
    }
    
    /** Provided for binary compatibility with Ceylon 1.1 */
    @Ignore @Deprecated
    public void copyTo(Array<Element> destination, int sourcePosition) {
        copyTo(destination, (long)sourcePosition, (long)0);
    }

    /** Provided for binary compatibility with Ceylon 1.1 */
    @Ignore @Deprecated
    public void copyTo(Array<Element> destination, 
            int sourcePosition, int destinationPosition) {
        copyTo(destination, (long)sourcePosition, (long)destinationPosition);
    }
    
    /** Provided for binary compatibility with Ceylon 1.1 */
    @Ignore @Deprecated
    public void copyTo(Array<Element> destination, 
            int sourcePosition, int destinationPosition, int length) {
        copyTo(destination, (long)sourcePosition, (long)destinationPosition, 
                (long)length);
    }

    @Override
    @Ignore
    public TypeDescriptor $getType$() {
        return TypeDescriptor.klass(Array.class, $reifiedElement);
    }
    
    @Override
    @TypeInfo(declaredVoid=true, value="ceylon.language::Anything")
    public java.lang.Object each(
            @Name("step")
            @FunctionalParameter("!(element)")
            @TypeInfo("ceylon.language::Anything(Element)")
            Callable<? extends java.lang.Object> step) {
        for (int i=0; i<size; i++) {
            step.$call$(unsafeItem(i));
        }
        return null;
    }
    
    @Override
    @TypeInfo("{Element*}")
    public Iterable<? extends Element, ? extends java.lang.Object> filter(
            @Name("selecting")
            @FunctionalParameter("(element)") 
            @TypeInfo("ceylon.language::Boolean(Element)")
            final Callable<? extends Boolean> selecting) {
        return new BaseIterable<Element, java.lang.Object>
                ($reifiedElement, Null.$TypeDescriptor$) {
            @Override
            public Iterator<? extends Element> iterator() {
                return new BaseIterator<Element>
                        ($reifiedElement) {
                    int index = 0;
                    @Override
                    public java.lang.Object next() {
                        while (index<size) {
                            Element element = unsafeItem(index++);
                            if (selecting.$call$(element).booleanValue()){
                                return element;
                            }
                        }
                        return finished_.get_();
                    }
                };
            }
        };
    }

    @Override
    @TypeInfo("{ceylon.language::Integer*}")
    public Iterable<? extends Integer, ? extends java.lang.Object> indexesWhere(
            @Name("selecting")
            @FunctionalParameter("(element)") 
            @TypeInfo("ceylon.language::Boolean(Element&ceylon.language::Object)")
            final Callable<? extends Boolean> selecting) {
        return new BaseIterable<Integer, java.lang.Object>
                (Integer.$TypeDescriptor$, Null.$TypeDescriptor$) {
            @Override
            public Iterator<? extends Integer> iterator() {
                return new BaseIterator<Integer>
                        (Integer.$TypeDescriptor$) {
                    int index = 0;
                    @Override
                    public java.lang.Object next() {
                        while (index<size) {
                            Element element = unsafeItem(index);
                            if (selecting.$call$(element).booleanValue()){
                                return Integer.instance(index++);
                            }
                            index++;
                        }
                        return finished_.get_();
                    }
                };
            }
        };
    }

    @Override
    @TypeInfo("{ceylon.language::Entry<ceylon.language::Integer,Element>*}")
    public Iterable<? extends Entry<? extends Integer, ? extends Element>, ? extends java.lang.Object> locations(
            @Name("selecting")
            @FunctionalParameter("(element)") 
            @TypeInfo("ceylon.language::Boolean(Element&ceylon.language::Object)")
            final Callable<? extends Boolean> selecting) {
        final TypeDescriptor klass = 
                TypeDescriptor.klass(Entry.class, 
                        Integer.$TypeDescriptor$, $reifiedElement);
        return new BaseIterable<Entry<? extends Integer, ? extends Element>, java.lang.Object>
                (klass, Null.$TypeDescriptor$) {
            @Override
            public Iterator<? extends Entry<? extends Integer, ? extends Element>> iterator() {
                return new BaseIterator<Entry<? extends Integer, ? extends Element>>(klass) {
                    int index = 0;
                    @Override
                    public java.lang.Object next() {
                        while (index<size) {
                            Element element = unsafeItem(index);
                            if (selecting.$call$(element).booleanValue()){
                                return new Entry(Integer.$TypeDescriptor$, 
                                        $reifiedElement,
                                        Integer.instance(index++), element);
                            }
                            index++;
                        }
                        return finished_.get_();
                    }
                };
            }
        };
    }

    //This one we don't really need to override:
    @Override
    @TypeInfo("{ceylon.language::Integer*}")
    public Iterable<? extends Integer, ? extends java.lang.Object> occurrences(
            @Name("element") final Element element, 
            @Defaulted @Name("from") final long from, 
            @Defaulted @Name("length") final long length) {
        return new BaseIterable<Integer, Object>
                (Integer.$TypeDescriptor$, Null.$TypeDescriptor$) {
            @Override
            public Iterator<? extends Integer> iterator() {
                return new BaseIterator<Integer>
                        (Integer.$TypeDescriptor$) {
                    long index = from;
                    @Override
                    public java.lang.Object next() {
                        for (;
                                index<size 
                                    && index<from+length;
                                index++) {
                            if (occursAt(index, element)) {
                                return Integer.instance(index++);
                            }
                        }
                        return finished_.get_();
                    }
                };
            }
            @Override
            public long getSize() {
                int size = 0;
                for (long index = from; 
                        index<Array.this.size 
                            && index<from+length; 
                        index++) {
                    if (occursAt(index, element)) {
                        size++;
                    }
                }
                return size;
            }
        };
    }

    @Override
    @TypeInfo("ceylon.language::Null|ceylon.language::Integer")
    public Integer firstOccurrence(
            @Name("element") Element element,
            @Defaulted @Name("from") long from,
            @Defaulted @Name("length") long length) {
        if (from>=size || length<=0) {
            return null;
        }
        if (from<0) {
            length+=from;
            from = 0;
        }
        for (int i=(int)from; 
                i<size && i<from+length; 
                i++) {
            if (occursAt(i, element)) {
                return Integer.instance(i);
            }
        }
        return null;
    }
    
    @Override
    @TypeInfo("ceylon.language::Null|ceylon.language::Integer")
    public Integer lastOccurrence(
            @Name("element") Element element,
            @Defaulted @Name("from") long from,
            @Defaulted @Name("length") long length) {
        if (from>=size || length<=0) {
            return null;
        }
        if (from<0) {
            length+=from;
            from = 0;
        }
        for (int i=(int)from; 
                i<size && i<from+length; 
                i++) {
            int j = size-1-i;
            if (occursAt(j, element)) {
                return Integer.instance(j);
            }
        }
        return null;
    }
    
    @Override
    public boolean occurs(
            @Name("element") Element element,
            @Defaulted @Name("from") long from,
            @Defaulted @Name("length") long length) {
        if (from>=size || length<=0) {
            return false;
        }
        if (from<0) {
            length+=from;
            from = 0;
        }
        for (int i=(int)from; 
                i<size && i<from+length; 
                i++) {
            if (occursAt(i, element)) {
                return true;
            }
        }
        return false;
    }
    
    public void reverseInPlace() {
        if (array instanceof java.lang.Object[]) {
            for (int index=0; index<size/2; index++) {
                java.lang.Object[] arr = (java.lang.Object[]) array;
                java.lang.Object swap = arr[index];
                int indexFromLast = size-index-1;
                arr[index] = arr[indexFromLast];
                arr[indexFromLast] = swap;
            }
        }
        else if (array instanceof long[]) {
            for (int index=0; index<size/2; index++) {
                long[] arr = (long[]) array;
                long swap = arr[index];
                int indexFromLast = size-index-1;
                arr[index] = arr[indexFromLast];
                arr[indexFromLast] = swap;
            }
        }
        else if (array instanceof int[]) {
            for (int index=0; index<size/2; index++) {
                int[] arr = (int[]) array;
                int swap = arr[index];
                int indexFromLast = size-index-1;
                arr[index] = arr[indexFromLast];
                arr[indexFromLast] = swap;
            }
        }
        else if (array instanceof short[]) {
            for (int index=0; index<size/2; index++) {
                short[] arr = (short[]) array;
                short swap = arr[index];
                int indexFromLast = size-index-1;
                arr[index] = arr[indexFromLast];
                arr[indexFromLast] = swap;
            }
        }
        else if (array instanceof byte[]) {
            for (int index=0; index<size/2; index++) {
                byte[] arr = (byte[]) array;
                byte swap = arr[index];
                int indexFromLast = size-index-1;
                arr[index] = arr[indexFromLast];
                arr[indexFromLast] = swap;
            }
        }
        else if (array instanceof double[]) {
            for (int index=0; index<size/2; index++) {
                double[] arr = (double[]) array;
                double swap = arr[index];
                int indexFromLast = size-index-1;
                arr[index] = arr[indexFromLast];
                arr[indexFromLast] = swap;
            }
        }
        else if (array instanceof float[]) {
            for (int index=0; index<size/2; index++) {
                float[] arr = (float[]) array;
                float swap = arr[index];
                int indexFromLast = size-index-1;
                arr[index] = arr[indexFromLast];
                arr[indexFromLast] = swap;
            }
        }
        else if (array instanceof boolean[]) {
            for (int index=0; index<size/2; index++) {
                boolean[] arr = (boolean[]) array;
                boolean swap = arr[index];
                int indexFromLast = size-index-1;
                arr[index] = arr[indexFromLast];
                arr[indexFromLast] = swap;
            }
        }
        else if (array instanceof char[]) {
            for (int index=0; index<size/2; index++) {
                char[] arr = (char[]) array;
                char swap = arr[index];
                int indexFromLast = size-index-1;
                arr[index] = arr[indexFromLast];
                arr[indexFromLast] = swap;
            }
        }
        else {
            throw new AssertionError("illegal array type");
        }
    }
    
    public void sortInPlace(
            @Name("comparing") @FunctionalParameter("(x,y)")
            @TypeInfo("ceylon.language::Callable<ceylon.language::Comparison,ceylon.language::Tuple<Element,Element,ceylon.language::Tuple<Element,Element,ceylon.language::Empty>>>") 
            final Callable<? extends Comparison> comparing) {
        java.util.List<Element> list = 
                new java.util.AbstractList<Element>() {
            @Override
            public Element get(int index) {
                return unsafeItem(index);
            }
            @Override
            public Element set(int index, Element element) {
                // Strictly this method should return the element that was at 
                // the given index, but that might require even more boxing 
                // and in practice the return value
                // doesn't seem to be used by the sorting algorithm
                Array.this.set(index, element);
                return null;
            }
            @Override
            public int size() {
                return (int)size;
            }
            @Override
            public java.lang.Object[] toArray() {
                if (array instanceof java.lang.Object[] &&
                    !(array instanceof java.lang.String[])) {
                    return (java.lang.Object[]) array;
                }
                else {
                    int size = size();
                    java.lang.Object[] result = 
                            new java.lang.Object[size];
                    for (int i=0; i<size; i++) {
                        result[i] = unsafeItem(i);
                    }
                    return result;
                }
            }
        };
        Comparator<Element> comparator = 
                new Comparator<Element>() {
            public int compare(Element x, Element y) {
                Comparison result = comparing.$call$(x, y);
                if (result==larger_.get_()) return 1;
                if (result==smaller_.get_()) return -1;
                return 0;
            }
        };
        Collections.<Element>sort(list, comparator);
    }
    
    private enum ArrayType {
        CeylonInteger("longArray"),   // 0
        JavaLong,                     // 1 
        CeylonFloat("doubleArray"),   // 2
        JavaDouble,                   // 3
        CeylonCharacter("intArray"),  // 4
        JavaInteger,                  // 5
        CeylonByte("byteArray"),      // 6
        JavaByte,                     // 7
        CeylonBoolean("booleanArray"),// 8
        JavaBoolean,                  // 9
        JavaCharacter,                // 10
        JavaShort,                    // 11
        JavaFloat,                    // 12
        CeylonString("stringArray"),  // 13
        JavaString,                   // 14
        Other("objectArray");         // 15
        
        /** The name of the field, apart from "array" which should be set for this element type */
        final java.lang.String fieldName;
        
        ArrayType(java.lang.String fieldName) {
            this.fieldName = fieldName;
        }
        ArrayType() {
            this.fieldName = null;
        }
    }
    
    private static ArrayType elementType(TypeDescriptor $reifiedElement) {
        if ($reifiedElement.containsNull()) {
            return ArrayType.Other;
        }
        
        java.lang.Class<?> arrayElementClass = 
                $reifiedElement.getArrayElementClass();
        if (arrayElementClass == Integer.class) {
            return ArrayType.CeylonInteger;
        }
        else if (arrayElementClass == java.lang.Long.class) {
            return ArrayType.JavaLong;
        }
        else if (arrayElementClass == Float.class) {
            return ArrayType.CeylonFloat;
        }
        else if (arrayElementClass == java.lang.Double.class) {
            return ArrayType.JavaDouble;
        } 
        else if (arrayElementClass == Character.class) {
            return ArrayType.CeylonCharacter;
        }
        else if (arrayElementClass == java.lang.Integer.class) {
            return ArrayType.JavaInteger;
        } 
        else if (arrayElementClass == Byte.class) {
            return ArrayType.CeylonByte;
        }
        else if (arrayElementClass == java.lang.Byte.class) {
            return ArrayType.JavaByte;
        } 
        else if (arrayElementClass == Boolean.class) {
            return ArrayType.CeylonBoolean;
        }
        else if (arrayElementClass == java.lang.Boolean.class) {
            return ArrayType.JavaBoolean;
        } 
        else if (arrayElementClass == java.lang.Character.class) {
            return ArrayType.JavaCharacter;
        } 
        else if (arrayElementClass == java.lang.Short.class) {
            return ArrayType.JavaShort;
        } 
        else if (arrayElementClass == java.lang.Float.class) {
            return ArrayType.JavaFloat;
        } 
        else if (arrayElementClass == String.class) {
            return ArrayType.CeylonString;
        }
        else if (arrayElementClass == java.lang.String.class) {
            return ArrayType.JavaString;
        } 
        else {
            return ArrayType.Other;
        }
    }
    

    @Override
    public boolean equals(
            @Name("that")
            java.lang.Object that) {
        if (that instanceof Array) {
            java.lang.Object x = ((Array<?>) that).array;
            java.lang.Object y = ((Array<?>) this).array;
            if (x instanceof double[] && y instanceof double[]) {
                return Arrays.equals((double[]) x, (double[]) y);
            }
            else if (x instanceof float[] && y instanceof float[]) {
                return Arrays.equals((float[]) x, (float[]) y);
            }
            else if (x instanceof long[] && y instanceof long[]) {
                return Arrays.equals((long[]) x, (long[]) y);
            }
            else if (x instanceof int[] && y instanceof int[]) {
                return Arrays.equals((int[]) x, (int[]) y);
            }
            else if (x instanceof short[] && y instanceof short[]) {
                return Arrays.equals((short[]) x, (short[]) y);
            }
            else if (x instanceof byte[] && y instanceof byte[]) {
                return Arrays.equals((byte[]) x, (byte[]) y);
            }
            else if (x instanceof boolean[] && y instanceof boolean[]) {
                return Arrays.equals((boolean[]) x, (boolean[]) y);
            }
            else if (x instanceof char[] && y instanceof char[]) {
                return Arrays.equals((char[]) x, (char[]) y);
            }
            else if (x instanceof Object[] && y instanceof Object[]) {
                return Arrays.equals((Object[]) x, (Object[]) y);
            }
        }
        return $ceylon$language$List$impl().equals(that);
    }
    
    @Override
    @Transient
    public int hashCode() {
        //TODO: optimize hash computation to avoid boxing!!
        return $ceylon$language$List$impl().hashCode();
    }
    
    @Override
    @Transient
    public java.lang.String toString() {
        return $ceylon$language$Collection$impl().toString();
    }
    
    @Ignore
    public Array($Serialization$ ignored, TypeDescriptor $reifiedElement) {
        this.$reifiedElement = $reifiedElement;
        this.elementType = elementType($reifiedElement);
        array = null;
        objectArray = null;
        longArray = null;
        intArray = null;
        doubleArray = null;
        booleanArray = null;
        byteArray = null;
        stringArray = null;
        size = 0;
        
    }
    
    @Ignore
    @Override
    public java.util.Collection<ReachableReference> $references$() {
        /*if (attr == null) {
            java.lang.String qualAttrName = (java.lang.String)ref;
            String className = qualAttrName.substring(0, qualAttrName.lastIndexOf('.'));
            String attrName = qualAttrName.substring(qualAttrName.lastIndexOf('.')+1);
            ClassDeclaration classDeclaration = klass;
            while (!classDeclaration.getQualifiedName().equals(className)) {
                classDeclaration = classDeclaration.getExtendedType().getDeclaration();
            }
            attr = classDeclaration.getDeclaredMemberDeclaration(ValueDeclaration.$TypeDescriptor$, attrName);
        }*/
        return Collections.<ReachableReference>singletonList(
                new MemberImpl(getArraySize()));
    }
    @Ignore
    protected ValueDeclaration getArraySize() {
        return (ValueDeclaration)((ClassDeclaration)Metamodel.getOrCreateMetamodel(Array.class)).getMemberDeclaration(ValueDeclaration.$TypeDescriptor$, "size");
    }
    
    @Ignore
    @Override
    public java.lang.Object $get$(ReachableReference indexOrAttr) {
        if (indexOrAttr instanceof Member
                && getArraySize().equals(((Member)indexOrAttr).getAttribute())) {
            return ceylon.language.Integer.instance(size);
        } else if (indexOrAttr instanceof ceylon.language.serialization.Element){
            return getFromFirst(((ceylon.language.serialization.Element)indexOrAttr).getIndex());
        }
        throw new AssertionError("unknown reference " + indexOrAttr);
    }
    
    @Ignore
    @Override
    public void $set$(ReachableReference reference, java.lang.Object instance) {
        if (reference instanceof Member
                && getArraySize().equals(((Member) reference).getAttribute())) {
            int size = toSize(((ceylon.language.Integer)instance).value);
            try {
                Lookup lookup = MethodHandles.lookup();
                Util.setter(lookup, "size").invokeExact(this, size);
                java.lang.Object array = createArrayWithElement($reifiedElement, size, null);
                Util.setter(lookup, "array")
                    .invokeExact(this, array);
                if (elementType.fieldName != null) {
                    MethodHandle fieldSetter = Util.setter(lookup, elementType.fieldName);
                    switch (elementType) {
                    case CeylonInteger:
                        fieldSetter.invokeExact(this, (long[])array);
                        break;
                    case CeylonFloat:
                        fieldSetter.invokeExact(this, (double[])array);
                        break;
                    case CeylonCharacter:
                        fieldSetter.invokeExact(this, (int[])array);
                        break;
                    case CeylonByte:
                        fieldSetter.invokeExact(this, (byte[])array);
                        break;
                    case CeylonBoolean:
                        fieldSetter.invokeExact(this, (boolean[])array);
                        break;
                    case CeylonString:
                        fieldSetter.invokeExact(this, (java.lang.String[])array);
                        break;
                    case Other:
                        fieldSetter.invokeExact(this, (java.lang.Object[])array);
                        break;
                    default:
                        fieldSetter.invoke(this, array);
                        break;
                    }
                }
            } catch (java.lang.Throwable t) {
                rethrow_.rethrow(t);
            }
        } else {
            ceylon.language.serialization.Element index  = (ceylon.language.serialization.Element)reference;
            set(index.getIndex(), (Element)instance);
        }
    }

    @Override @Ignore
    public List$impl<? extends Element> $ceylon$language$List$impl() {
        // TODO Auto-generated method stub
        return new List$impl<Element>($reifiedElement, this);
    }

    @Override @Ignore
    public <Result> Sequential<? extends Result> collect(TypeDescriptor arg0, Callable<? extends Result> arg1) {
        return $ceylon$language$List$impl().collect(arg0, arg1);
    }

    @Override @Ignore
    public boolean endsWith(List<? extends java.lang.Object> sublist) {
        return $ceylon$language$List$impl().endsWith(sublist);
    }

    @Override @Ignore
    public Element get(Integer index) {
        //NOTE IMPORTANT OPTIMIZATION HERE
        return getFromFirst(index.value);
    }

    @Override @Ignore
    public List<? extends Integer> getKeys() {
        return $ceylon$language$List$impl().getKeys();
    }

    @Override @Ignore
    public List<? extends Element> getRest() {
        return $ceylon$language$List$impl().getRest();
    }

    @Override @Ignore
    public List<? extends Element> getReversed() {
        return $ceylon$language$List$impl().getReversed();
    }

    @Override @Ignore
    public List<? extends Element> initial(long arg0) {
        return $ceylon$language$List$impl().initial(arg0);
    }

    @Override @Ignore
    public boolean longerThan(long arg0) {
        return $ceylon$language$List$impl().longerThan(arg0);
    }

    @Override @Ignore
    public <Other> List patch(TypeDescriptor arg0, List<? extends Other> arg1) {
        return $ceylon$language$List$impl().patch(arg0, arg1);
    }

    @Override @Ignore
    public <Other> List patch(TypeDescriptor arg0, List<? extends Other> arg1, long arg2) {
        return $ceylon$language$List$impl().patch(arg0, arg1, arg2);
    }

    @Override @Ignore
    public <Other> List patch(TypeDescriptor arg0, List<? extends Other> arg1, long arg2, long arg3) {
        return $ceylon$language$List$impl().patch(arg0, arg1, arg3);
    }

    @Override @Ignore
    public <Other> long patch$from(TypeDescriptor arg0, List<? extends Other> arg1) {
        return $ceylon$language$List$impl().patch$from(arg0, arg1);
    }

    @Override @Ignore
    public <Other> long patch$length(TypeDescriptor arg0, List<? extends Other> arg1, long arg2) {
        return $ceylon$language$List$impl().patch$length(arg0, arg1, arg2);
    }

    @Override @Ignore
    public List<? extends Element> repeat(long arg0) {
        return $ceylon$language$List$impl().repeat(arg0);
    }

    @Override @Ignore
    public boolean shorterThan(long arg0) {
        return $ceylon$language$List$impl().shorterThan(arg0);
    }

    @Override @Ignore
    public Sequence<? extends List<? extends Element>> slice(long arg0) {
        return $ceylon$language$List$impl().slice(arg0);
    }

    @Override @Ignore
    public boolean startsWith(List<? extends java.lang.Object> sublist) {
        return $ceylon$language$List$impl().startsWith(sublist);
    }

    @Override @Ignore
    public List<? extends Element> sublist(long arg0, long arg1) {
        return $ceylon$language$List$impl().sublist(arg0, arg1);
    }

    @Override @Ignore
    public List<? extends Element> sublistFrom(long arg0) {
        return $ceylon$language$List$impl().sublistFrom(arg0);
    }

    @Override @Ignore
    public List<? extends Element> sublistTo(long arg0) {
        return $ceylon$language$List$impl().sublistTo(arg0);
    }

    @Override @Ignore
    public List<? extends Element> terminal(long arg0) {
        return $ceylon$language$List$impl().terminal(arg0);
    }

    @Override @Ignore
    public List<? extends Element> trim(Callable<? extends Boolean> arg0) {
        return $ceylon$language$List$impl().trim(arg0);
    }

    @Override @Ignore
    public List<? extends Element> trimLeading(Callable<? extends Boolean> arg0) {
        return $ceylon$language$List$impl().trimLeading(arg0);
    }

    @Override @Ignore
    public List<? extends Element> trimTrailing(Callable<? extends Boolean> arg0) {
        return $ceylon$language$List$impl().trimTrailing(arg0);
    }

    @Override @Ignore
    public Collection$impl<? extends Element> $ceylon$language$Collection$impl() {
        return new Collection$impl<Element>($reifiedElement, this);
    }

    @Override @Ignore
    public Iterable<? extends Sequence<? extends Element>, ? extends java.lang.Object> getPermutations() {
        return $ceylon$language$Collection$impl().getPermutations();
    }

    @Override @Ignore
    public Iterable$impl<? extends Element, ? extends java.lang.Object> $ceylon$language$Iterable$impl() {
        return new Iterable$impl<Element,java.lang.Object>($reifiedElement,Null.$TypeDescriptor$, this);
    }

    @Override @Ignore
    public <Other, OtherAbsent> Iterable chain(TypeDescriptor arg0, TypeDescriptor arg1,
            Iterable<? extends Other, ? extends OtherAbsent> arg2) {
        return $ceylon$language$Iterable$impl().chain(arg0, arg1, arg2);
    }

    @Override @Ignore
    public <Default> Iterable defaultNullElements(TypeDescriptor arg0, Default arg1) {
        return $ceylon$language$Iterable$impl().defaultNullElements(arg0, arg1);
    }

    @Override @Ignore
    public <Result, OtherAbsent> Iterable flatMap(TypeDescriptor arg0, TypeDescriptor arg1,
            Callable<? extends Iterable<? extends Result, ? extends OtherAbsent>> arg2) {
        return $ceylon$language$Iterable$impl().flatMap(arg0, arg1, arg2);
    }

    @Override @Ignore
    public <Result> Callable<? extends Result> fold(TypeDescriptor arg0, Result arg1) {
        return $ceylon$language$Iterable$impl().fold(arg0, arg1);
    }

    @Override @Ignore
    public <Other> Iterable follow(TypeDescriptor arg0, Other arg1) {
        return $ceylon$language$Iterable$impl().follow(arg0, arg1);
    }

    @Override @Ignore
    public Iterable<? extends Element, ? extends java.lang.Object> getCycled() {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().getCycled();
    }

    @Override @Ignore
    public Iterable<? extends Element, ? extends java.lang.Object> getDistinct() {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().getDistinct();
    }

    /*@Override @Ignore
    public Set<? extends Element> elements() {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().elements();
    }*/

    @Override @Ignore
    public Iterable<? extends Element, ? extends java.lang.Object> getExceptLast() {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().getExceptLast();
    }

    @Override @Ignore
    public Iterable<? extends Entry<? extends Integer, ? extends Element>, ? extends java.lang.Object> getIndexed() {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().getIndexed();
    }

    @Override @Ignore
    public Iterable<? extends Sequence<? extends Element>, ? extends java.lang.Object> getPaired() {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().getPaired();
    }

    @Override @Ignore
    public <Group> Map<? extends Group, ? extends Sequence<? extends Element>> group(TypeDescriptor arg0,
            Callable<? extends Group> arg1) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().group(arg0, arg1);
    }

    @Override @Ignore
    public <Group,Result> Map<? extends Group, ? extends Result> summarize(TypeDescriptor arg0,
            TypeDescriptor arg1, Callable<? extends Group> arg2, Callable<? extends Result> arg3) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().summarize(arg0, arg1, arg2, arg3);
    }

    @Override @Ignore
    public java.lang.Object indexes() {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().indexes();
    }

    @Override @Ignore
    public <Other> Iterable interpose(TypeDescriptor arg0, Other arg1) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().interpose(arg0, arg1);
    }

    @Override @Ignore
    public <Other> Iterable interpose(TypeDescriptor arg0, Other arg1, long arg2) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().interpose(arg0, arg1, arg2);
    }

    @Override @Ignore
    public <Other> long interpose$step(TypeDescriptor arg0, Other arg1) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().interpose$step(arg0, arg1);
    }

    @Override @Ignore
    public <Result> Iterable<? extends Result, ? extends java.lang.Object> map(TypeDescriptor arg0,
            Callable<? extends Result> arg1) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().map(arg0, arg1);
    }

    @Override @Ignore
    public java.lang.Object max(Callable<? extends Comparison> arg0) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().max(arg0);
    }

    @Override @Ignore
    public <Type> Iterable narrow(TypeDescriptor arg0) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().narrow(arg0);
    }

    @Override @Ignore
    public Iterable<? extends Sequence<? extends Element>, ? extends java.lang.Object> partition(long arg0) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().partition(arg0);
    }

    @Override @Ignore
    public <Other, OtherAbsent> Iterable product(TypeDescriptor arg0, TypeDescriptor arg1,
            Iterable<? extends Other, ? extends OtherAbsent> arg2) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().product(arg0, arg1, arg2);
    }

    @Override @Ignore
    public <Result> Callable<? extends Iterable<? extends Result, ? extends java.lang.Object>> scan(TypeDescriptor arg0,
            Result arg1) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().scan(arg0, arg1);
    }

    @Override @Ignore
    public Sequential<? extends Element> select(Callable<? extends Boolean> arg0) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().select(arg0);
    }

    @Override @Ignore
    public Iterable<? extends Element, ? extends java.lang.Object> skipWhile(Callable<? extends Boolean> arg0) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().skipWhile(arg0);
    }

    @Override @Ignore
    public <Result, Args extends Sequential<? extends java.lang.Object>> Callable<? extends Iterable<? extends Result, ? extends java.lang.Object>> spread(
            TypeDescriptor arg0, TypeDescriptor arg1, Callable<? extends Callable<? extends Result>> arg2) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().spread(arg0, arg1, arg2);
    }

    @Override @Ignore
    public Iterable<? extends Element, ? extends java.lang.Object> takeWhile(Callable<? extends Boolean> arg0) {
        // TODO Auto-generated method stub
        return $ceylon$language$Iterable$impl().takeWhile(arg0);
    }

    @Override @Ignore
    public Category$impl<? super java.lang.Object> $ceylon$language$Category$impl() {
        return new Category$impl<java.lang.Object>(Object.$TypeDescriptor$, this);
    }

    @Override @Ignore
    public boolean containsAny(Iterable<? extends java.lang.Object, ? extends java.lang.Object> arg0) {
        // TODO Auto-generated method stub
        return $ceylon$language$Category$impl().containsAny(arg0);
    }

    @Override @Ignore
    public boolean containsEvery(Iterable<? extends java.lang.Object, ? extends java.lang.Object> arg0) {
        // TODO Auto-generated method stub
        return $ceylon$language$Category$impl().containsEvery(arg0);
    }

    @Override @Ignore
    public Correspondence$impl<? super Integer, ? extends Element> $ceylon$language$Correspondence$impl() {
        // TODO Auto-generated method stub
        return new Correspondence$impl<Integer, Element>(Integer.$TypeDescriptor$, $reifiedElement, this);
    }

    @Override @Ignore
    public boolean definesAny(Iterable<? extends Integer, ? extends java.lang.Object> arg0) {
        // TODO Auto-generated method stub
        return $ceylon$language$Correspondence$impl().definesAny(arg0);
    }

    @Override @Ignore
    public boolean definesEvery(Iterable<? extends Integer, ? extends java.lang.Object> arg0) {
        // TODO Auto-generated method stub
        return $ceylon$language$Correspondence$impl().definesEvery(arg0);
    }

    @Override @Ignore
    public <Absent> Iterable<? extends Element, ? extends Absent> getAll(TypeDescriptor arg0,
            Iterable<? extends Integer, ? extends Absent> arg1) {
        // TODO Auto-generated method stub
        return $ceylon$language$Correspondence$impl().getAll(arg0, arg1);
    }

    @Override @Ignore
    public SearchableList$impl<Element> $ceylon$language$SearchableList$impl() {
        return new SearchableList$impl<Element>($reifiedElement, this);
    }
    
    @Override @Ignore
    public Integer firstInclusion(List<? extends Element> arg0) {
        return $ceylon$language$SearchableList$impl().firstInclusion(arg0);
    }

    @Override @Ignore
    public long firstInclusion$from(List<? extends Element> arg0) {
        return 0;
    }

    @Override @Ignore
    public Integer firstOccurrence(Element arg0) {
        return firstOccurrence(arg0, 0);
    }

    @Override @Ignore
    public Integer firstOccurrence(Element arg0, long arg1) {
        return firstOccurrence(arg0, arg1, size-arg1);
    }

    @Override @Ignore
    public long firstOccurrence$from(Element arg0) {
        return 0;
    }

    @Override @Ignore
    public long firstOccurrence$length(Element arg0, long arg1) {
        return size - arg1;
    }

    @Override @Ignore
    public boolean includes(List<? extends Element> arg0) {
        return $ceylon$language$SearchableList$impl().includes(arg0);
    }

    @Override @Ignore
    public long includes$from(List<? extends Element> arg0) {
        return 0;
    }

    @Override @Ignore
    public Iterable<? extends Integer, ? extends java.lang.Object> inclusions(List<? extends Element> arg0) {
        return $ceylon$language$SearchableList$impl().inclusions(arg0);
    }

    @Override @Ignore
    public Iterable<? extends Integer, ? extends java.lang.Object> inclusions(List<? extends Element> arg0,
            long arg1) {
        return $ceylon$language$SearchableList$impl().inclusions(arg0, arg1);
    }

    @Override @Ignore
    public long inclusions$from(List<? extends Element> arg0) {
        return 0;
    }

    @Override @Ignore
    public Integer lastInclusion(List<? extends Element> arg0) {
        return $ceylon$language$SearchableList$impl().lastInclusion(arg0);
    }

    @Override @Ignore
    public long lastInclusion$from(List<? extends Element> arg0) {
        return 0;
    }

    @Override @Ignore
    public Integer lastOccurrence(Element arg0) {
        return lastOccurrence(arg0, 0);
    }

    @Override @Ignore
    public Integer lastOccurrence(Element arg0, long arg1) {
        return lastOccurrence(arg0, arg1, size-arg1);
    }

    @Override @Ignore
    public long lastOccurrence$from(Element arg0) {
        return 0;
    }

    @Override @Ignore
    public long lastOccurrence$length(Element arg0, long arg1) {
        return size - arg1;
    }

    @Override @Ignore
    public Iterable<? extends Integer, ? extends java.lang.Object> occurrences(Element arg0) {
        return occurrences(arg0, 0);
    }

    @Override @Ignore
    public Iterable<? extends Integer, ? extends java.lang.Object> occurrences(Element arg0, long arg1) {
        return occurrences(arg0, arg1, size-arg1);
    }

    @Override @Ignore
    public long occurrences$from(Element arg0) {
        return 0;
    }

    @Override @Ignore
    public long occurrences$length(Element arg0, long arg1) {
        return size - arg1;
    }

    @Override @Ignore
    public boolean occurs(Element arg0) {
        return occurs(arg0, 0);
    }

    @Override @Ignore
    public boolean occurs(Element arg0, long arg1) {
        return occurs(arg0, arg1, size-arg1);
    }

    @Override @Ignore
    public long occurs$from(Element arg0) {
        return 0;
    }

    @Override @Ignore
    public long occurs$length(Element arg0, long arg1) {
        return size - arg1;
    }

    @Override @Ignore
    public Integer firstInclusion(List<? extends Element> arg0, long arg1) {
        // TODO Auto-generated method stub
        return $ceylon$language$SearchableList$impl().firstInclusion(arg0, arg1);
    }

    @Override @Ignore
    public boolean includes(List<? extends Element> arg0, long arg1) {
        // TODO Auto-generated method stub
        return $ceylon$language$SearchableList$impl().includes(arg0, arg1);
    }

    @Override @Ignore
    public boolean includesAt(long arg0, List<? extends Element> arg1) {
        // TODO Auto-generated method stub
        return $ceylon$language$SearchableList$impl().includesAt(arg0, arg1);
    }

    @Override @Ignore
    public Integer lastInclusion(List<? extends Element> arg0, long arg1) {
        // TODO Auto-generated method stub
        return $ceylon$language$SearchableList$impl().lastInclusion(arg0, arg1);
    }

    @Override @Ignore
    public <Item> Map<? extends Element, ? extends Item>
    tabulate(TypeDescriptor arg0, Callable<? extends Item> arg1) {
        return $ceylon$language$Iterable$impl().tabulate(arg0, arg1);
    }

    @Override @Ignore
    public Map<? extends Element, ? extends Integer>
    frequencies() {
        return $ceylon$language$Iterable$impl().frequencies();
    }

    @Override @Ignore
    public <Result> List<? extends Result> mapElements(TypeDescriptor arg0,
            Callable<? extends Result> arg1) {
        // TODO Auto-generated method stub
        return $ceylon$language$List$impl().mapElements(arg0, arg1);
    }

}
