import imports.package { 
    m=method, 
    a=attribute, 
    C=Class { i=count, 
              In=Inner, 
              m=method }, 
    I=Interface, 
    D=Dupe,
    o=op
}

class TestAliases() {
    m();
    C c = C("gavin");
    I i = c;
    String name = c.name;
    c.i++;
    C.In cin = c.In();
    c.m("hello",0);
    D d = D();
    d.hello();
    String hello = a;
    Dupe dupe = Dupe();
    dupe.goodbye();
    o(1);
}