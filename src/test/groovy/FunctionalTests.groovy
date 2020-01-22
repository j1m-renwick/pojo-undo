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

    def "multiple proxies do not interfere with each other"() {
        given:
        UndoablePojo proxyOne = PojoWrapper.wrap(UndoablePojo.class)
        UndoablePojo proxyTwo = PojoWrapper.wrap(UndoablePojo.class)

        when:
        proxyOne.start()
        proxyTwo.start()
        proxyOne.setFieldOne("proxyOneValue")
        proxyTwo.setFieldOne("proxyTwoValue")
        proxyOne.finish()
        proxyTwo.finish()

        proxyOne.undo()

        then:
        proxyOne.fieldOne == null
        proxyTwo.fieldOne == "proxyTwoValue"

    }

    def "changing using block functionality functions as expected"() {
        expect:
        UndoablePojo proxy = PojoWrapper.wrap(UndoablePojo.class)

        proxy.start()
        proxy.setFieldOne("firstFieldValue1")
        proxy.setFieldTwo("secondFieldValue1")
        proxy.finish()

        proxy.useBlocks(false)
        proxy.setFieldOne("firstFieldValue2")
        proxy.setFieldTwo("firstFieldValue2")
        proxy.undo()
        proxy.undo()

        proxy.fieldOne == "firstFieldValue1"
        proxy.fieldTwo == "secondFieldValue1"

        proxy.useBlocks(true)
        proxy.start()
        proxy.setFieldOne("firstFieldValue3")
        proxy.setFieldTwo("secondFieldValue3")
        proxy.finish()

        proxy.undo()

        proxy.fieldOne == "firstFieldValue1"
        proxy.fieldTwo == "secondFieldValue1"

    }

}