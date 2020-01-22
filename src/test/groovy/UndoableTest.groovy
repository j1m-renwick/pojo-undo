import pojos.UndoablePojo
import spock.lang.Specification

class UndoableTest extends Specification {

    def "undo pops the stack and calls the specified methods with the specified values"() {
        given:
        UndoablePojo underTest = new UndoablePojo()
        underTest.fieldOne = "initialFieldOneValue"
        underTest.fieldTwo = "initialFieldTwoValue"
        underTest.fieldThree = true
        underTest.fieldFour = 1234
        underTest.stack.push([new Tuple2<>(TestPojo.class.getDeclaredMethod("setFieldThree", Boolean.class), false)])
        underTest.stack.push([new Tuple2<>(TestPojo.class.getDeclaredMethod("setFieldOne", String.class), "a_value_to_set"),
                             new Tuple2<>(TestPojo.class.getDeclaredMethod("setFieldTwo", String.class), null),
                             new Tuple2<>(TestPojo.class.getDeclaredMethod("setFieldFour", int), 9999)])

        when:
        underTest.undo()

        then:
        underTest.fieldOne == "a_value_to_set"
        underTest.fieldTwo == null
        underTest.fieldThree
        underTest.fieldFour == 9999
        underTest.stack.size() == 1

    }

    def "start resets tuple list and sets inProgress flag"() {
        given:
        UndoablePojo underTest = new UndoablePojo()
        underTest.inProgress = false
        underTest.tuples = [new Tuple2<>(TestPojo.class.getDeclaredMethod("setFieldThree", Boolean.class), false)]

        when:
        underTest.start()

        then:
        underTest.inProgress
        underTest.tuples.isEmpty()
    }

    def "start throws expected exception if it is called twice in a row"() {
        given:
        UndoablePojo underTest = new UndoablePojo()

        when:
        underTest.start()
        underTest.start()

        then:
        thrown(IllegalStateException.class)
    }

    def "stop pushes tuple list to the stack and unsets inProgress flag"() {
        given:
        UndoablePojo underTest = new UndoablePojo()
        underTest.inProgress = true
        underTest.tuples = [new Tuple2<>(TestPojo.class.getDeclaredMethod("setFieldThree", Boolean.class), false)]

        when:
        underTest.finish()

        then:
        !underTest.inProgress
        underTest.stack.size() == 1

    }

}
