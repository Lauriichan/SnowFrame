package me.lauriichan.snowframe.data;

import me.lauriichan.snowframe.resource.source.IDataSource;

public interface IDataHandler<T> {
    
    static class Wrapper<T> {
        
        private volatile int version;
        private volatile T value;
        
        public T value() {
            return value;
        }
        
        public void value(T value) {
            this.value = value;
        }
        
        public int version() {
            return version;
        }
        
        public void version(int version) {
            this.version = version;
        }
        
    }
    
    void load(Wrapper<T> wrapper, IDataSource source) throws Exception;
    
    void save(Wrapper<T> wrapper, IDataSource source) throws Exception;

}
