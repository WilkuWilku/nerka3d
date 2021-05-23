package pl.ee.nerkabackend.processing.collections;

import java.util.ArrayList;
import java.util.List;

public class LoopArrayList<T> extends ArrayList<T> {

    public LoopArrayList(List<T> inputList) {
        super(inputList);
    }

    @Override
    public T get(int index) {
        int modIndex = index % this.size();
        while(modIndex < 0) {
            modIndex += this.size();
        }
        return super.get(modIndex);
    }

    @Override
    public T set(int index, T element) {
        int modIndex = index % this.size();
        while(modIndex < 0) {
            modIndex += this.size();
        }
        return super.set(modIndex, element);
    }
}
