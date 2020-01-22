import pojos.UndoablePojo
import spock.lang.Specification

import java.lang.reflect.Field

class UndoableTest extends Specification {

    UndoablePojo underTest

    def setup() {
        underTest = new UndoablePojo()
    }

    def "undo pops the stack and calls the specified methods with the specified values"() {
        given:
        underTest.fieldOne = "initialFieldOneValue"
        underTest.fieldTwo = "initialFieldTwoValue"
        underTest.fieldThree = true
        underTest.fieldFour = 1234
        underTest.stack.push([new Tuple2<>(UndoablePojo.class.getDeclaredMethod("setFieldThree", Boolean.class), false)])
        underTest.stack.push([new Tuple2<>(UndoablePojo.class.getDeclaredMethod("setFieldOne", String.class), "a_value_to_set"),
                         new Tuple2<>(UndoablePojo.class.getDeclaredMethod("setFieldTwo", String.class), null),
                         new Tuple2<>(UndoablePojo.class.getDeclaredMethod("setFieldFour", int), 9999)])

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

        setTraitField('inProgress', false)
        underTest.tuples = [new Tuple2<>(UndoablePojo.class.getDeclaredMethod("setFieldThree", Boolean.class), false)]
        when:
        underTest.start()

        then:
        ((boolean) getTraitField('inProgress'))
        underTest.tuples.isEmpty()
    }

    def "start throws expected exception if it is called when blocks are not enabled"() {
        when:
        underTest.useBlocks(false)
        underTest.start()

        then:
        thrown(IllegalStateException.class)
    }

    def "start throws expected exception if it is called twice in a row"() {
        when:
        underTest.start()
        underTest.start()

        then:
        thrown(IllegalStateException.class)
    }

    def "finish pushes tuple list to the stack and unsets inProgress flag"() {
        given:
        setTraitField('inProgress', true)
        underTest.tuples = [new Tuple2<>(UndoablePojo.class.getDeclaredMethod("setFieldThree", Boolean.class), false)]

        when:
        underTest.finish()

        then:
        !((boolean) getTraitField('inProgress'))
        underTest.tuples.size() == 1
    }

    def "finish throws expected exception if it is called when blocks are not enabled"() {
        when:
        underTest.useBlocks(false)
        underTest.finish()

        then:
        thrown(IllegalStateException.class)
    }

    def "finish throws expected exception if it is called before start() is"() {
        when:
        underTest.finish()

        then:
        thrown(IllegalStateException.class)
    }

    def "useBlocks set appropriate flags when flag is true"() {
        given:
        setTraitField('inProgress', true)
        setTraitField('useBlocks', false)

        when:
        underTest.useBlocks(true)

        then:
        !(boolean) getTraitField('inProgress')
        (boolean) getTraitField('useBlocks')
    }

    def "useBlocks set appropriate flags when flag is false"() {
        given:
        setTraitField('inProgress', false)
        setTraitField('useBlocks', true)

        when:
        underTest.useBlocks(false)

        then:
        (boolean) getTraitField('inProgress')
        !(boolean) getTraitField('useBlocks')
        underTest.tuples == null
    }

    // UTILITY METHODS

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

}
