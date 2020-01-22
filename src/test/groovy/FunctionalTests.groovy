import pojos.UndoablePojo
import spock.lang.Specification
import wrappers.PojoWrapper

class FunctionalTests extends Specification {

    def "multiple non-overlapping undoable blocks are cleared out as expected"() {
        expect:
        UndoablePojo proxy = PojoWrapper.wrap(UndoablePojo.class)

        proxy.start()
        proxy.setFieldOne("first")
        proxy.setFieldTwo("second")
        proxy.finish()

        proxy.start()
        proxy.setFieldFour(9876)
        proxy.setFieldFive(1.234)
        proxy.finish()

        proxy.fieldOne == "first"
        proxy.fieldTwo == "second"
        proxy.fieldFour == 9876
        proxy.fieldFive == 1.234f

        proxy.undo()
        proxy.fieldOne == "first"
        proxy.fieldTwo == "second"
        proxy.fieldFour == 0
        proxy.fieldFive == 0

        proxy.undo()
        proxy.fieldOne == null
        proxy.fieldTwo == null
        proxy.fieldFour == 0
        proxy.fieldFive == 0
    }

    def "multiple overlapping undoable blocks are cleared out as expected"() {
        expect:
        UndoablePojo proxy = PojoWrapper.wrap(UndoablePojo.class)

        proxy.start()
        proxy.setFieldOne("first")
        proxy.setFieldTwo("second")
        proxy.finish()

        proxy.start()
        proxy.setFieldOne("first_changed")
        proxy.setFieldFive(1.234)
        proxy.finish()

        proxy.fieldOne == "first_changed"
        proxy.fieldTwo == "second"
        proxy.fieldFive == 1.234f

        proxy.undo()
        proxy.fieldOne == "first"
        proxy.fieldTwo == "second"
        proxy.fieldFive == 0

        proxy.undo()
        proxy.fieldOne == null
        proxy.fieldTwo == null
        proxy.fieldFive == 0
    }

    def "calling undo on an empty stack does nothing"() {
        expect:
        UndoablePojo proxy = PojoWrapper.wrap(UndoablePojo.class)
        proxy.undo()

        proxy.fieldOne == null
        proxy.fieldTwo == null
        proxy.fieldThree == null
        proxy.fieldFour == 0
        proxy.fieldFive == 0
    }

    def "calling a setter before calling start() throws the expected exception"() {
        given:
        UndoablePojo proxy = PojoWrapper.wrap(UndoablePojo.class)

        when:
        proxy.setFieldOne("first")

        then:
        thrown(IllegalStateException.class)
    }

}