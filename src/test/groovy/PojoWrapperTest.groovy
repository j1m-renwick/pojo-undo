import pojos.StandardPojo
import pojos.UndoablePojo
import spock.lang.Specification
import wrappers.PojoWrapper

class PojoWrapperTest extends Specification {

    def "wrap returns the expected proxy object"() {
        expect:
        def actual = PojoWrapper.wrap(UndoablePojo.class)
        actual instanceof UndoablePojo
    }

    def "wrap returns the expected exception if the class does not implement the pack.Undoable trait"() {
        when:
        PojoWrapper.wrap(StandardPojo.class)

        then:
        thrown(IllegalArgumentException.class)
    }

}