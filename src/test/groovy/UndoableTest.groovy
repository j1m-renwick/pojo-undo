import pojos.UndoablePojo
import spock.lang.Specification

import java.lang.reflect.Field

class UndoableTest extends Specification {

    UndoablePojo underTest

    def setup() {
        underTest = new UndoablePojo()
    }

    def getTraitField(String fieldName) {
        Field fieldToSet = UndoablePojo.class.getDeclaredFields()
                .find { field -> field.getName().endsWith('__' + fieldName) }
        fieldToSet.setAccessible(true)
        return fieldToSet.get(underTest)
    }

    void setTraitField(String fieldName, Object fieldValue) {
        Field fieldToSet = UndoablePojo.class.getDeclaredFields()
                .find { field -> field.getName().endsWith('__' + fieldName) }
        fieldToSet.setAccessible(true)
        fieldToSet.set(underTest, fieldValue)
    }

    def "undo pops the stack and calls the specified methods with the specified values"() {
        given:
        underTest.fieldOne = "initialFieldOneValue"
        underTest.fieldTwo = "initialFieldTwoValue"
        underTest.fieldThree = true
        underTest.fieldFour = 1234
        Stack traitStack = getTraitField('stack') as Stack
        traitStack.push([new Tuple2<>(UndoablePojo.class.getDeclaredMethod("setFieldThree", Boolean.class), false)])
        traitStack.push([new Tuple2<>(UndoablePojo.class.getDeclaredMethod("setFieldOne", String.class), "a_value_to_set"),
                         new Tuple2<>(UndoablePojo.class.getDeclaredMethod("setFieldTwo", String.class), null),
                         new Tuple2<>(UndoablePojo.class.getDeclaredMethod("setFieldFour", int), 9999)])

        when:
        underTest.undo()

        then:
        underTest.fieldOne == "a_value_to_set"
        underTest.fieldTwo == null
        underTest.fieldThree
        underTest.fieldFour == 9999
        traitStack.size() == 1

    }

    def "start resets tuple list and sets inProgress flag"() {
        given:

        setTraitField('inProgress', false)
        underTest.tuples = [new Tuple2<>(UndoablePojo.class.getDeclaredMethod("setFieldThree", Boolean.class), false)]
        when:
        underTest.start()

        then:
        ((boolean) getTraitField('inProgress'))
        underTest.tuples.isEmpty()
    }

    def "start throws expected exception if it is called twice in a row"() {
        when:
        underTest.start()
        underTest.start()

        then:
        thrown(IllegalStateException.class)
    }

    def "stop pushes tuple list to the stack and unsets inProgress flag"() {
        given:
        setTraitField('inProgress', true)
        underTest.tuples = [new Tuple2<>(UndoablePojo.class.getDeclaredMethod("setFieldThree", Boolean.class), false)]

        when:
        underTest.finish()

        then:
        !((boolean) getTraitField('inProgress'))
        underTest.tuples.size() == 1
    }

}
