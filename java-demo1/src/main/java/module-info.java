module javademo {

    exports com.dawell.java.demo1;

    requires java.base;
    // lombok jdk12 support fail！
    //    requires static lombok;
    requires spring.core;
    requires java.management;
    requires fastjson;
    requires jdk.unsupported;
    //    requires java.logging;

}