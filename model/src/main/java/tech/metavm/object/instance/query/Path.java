package tech.metavm.object.instance.query;

import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

import java.util.Arrays;
import java.util.List;

public class Path {

    public static boolean isIndexItem(String pathItem) {
        return ValueUtil.isIntegerStr(pathItem);
    }

    public static boolean isAsteriskItem(String pathITem) {
        return pathITem.equalsIgnoreCase("*");
    }

    public static int parseIndexItem(String pathItem) {
        if(isIndexItem(pathItem)) {
            return Integer.parseInt(pathItem);
        }
        else {
            throw new InternalException("'" + pathItem + "' is not an index item");
        }
    }

    public static Path create(String pathString) {
        List<String> values = Arrays.asList(pathString.split("\\."));
        return new Path(values, 0, values.size());
    }

    private final List<String> values;
    private final int offset;
    private final int length;

    public Path(List<String> values, int offset, int length) {
        this.values = values;
        this.offset = offset;
        this.length = length;
    }

    public List<String> getPath() {
        return this.values.subList(offset, offset + length);
    }

    public String getPathString() {
        return NncUtils.joinWithDot(getPath());
    }

    public String firstItem() {
        return this.values.get(offset);
    }

    public String getItem(int index) {
        checkIndex(index);
        return this.values.get(offset + index);
    }

    public int length() {
        return length;
    }

    public Path subPath(int start, int end) {
        if(end < start) {
            throw new InternalException("end can not be less than start");
        }
        checkIndex(start);
        checkIndex(end - 1);
        return new Path(this.values, offset + start, end - start);
    }

    private void checkIndex(int index) {
        if(index < offset || index >= offset + length) {
            throw new IndexOutOfBoundsException();
        }
    }

    public int firstItemAsIndex() {
        return parseIndexItem(firstItem());
    }

    public Path subPath() {
        return new Path(values, offset + 1, length - 1);
    }

    public boolean hasSubPath() {
        return length > 0;
    }

    public boolean isEmpty() {
        return length == 0;
    }

    @Override
    public String toString() {
        return getPathString();
    }
}
