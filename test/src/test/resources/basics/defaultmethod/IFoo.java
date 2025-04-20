package defaultmethod;

public interface IFoo {

    default int foo() {
        return 0;
    }

}
