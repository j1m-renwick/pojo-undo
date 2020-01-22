# Pojo Undo

This library uses CGLIB to proxy POJO classes, giving them the ability to undo the setting of one or more variables in the same order that they have been set.  

### Usage

**NOTE:** the below is an example using Groovy, but the same principle applies to any POJO with getter and setter methods.
    
```

// POJO classes must implement the Undoable interface
class UndoablePojo implements Undoable {
    String fieldOne
    String fieldTwo
    Boolean fieldThree
    int fieldFour
    float fieldFive
}

UndoablePojo proxy = PojoWrapper.wrap(UndoablePojo.class)

// use start() to begin a block of variables that you would like to undo later
proxy.start()

proxy.setFieldOne("first")
proxy.setFieldTwo("second")

// call finish() to end the block
proxy.finish()

proxy.start()
proxy.setFieldFour(9876)
proxy.setFieldFive(1.234)
proxy.finish()

proxy.fieldOne == "first"
proxy.fieldTwo == "second"
proxy.fieldFour == 9876
proxy.fieldFive == 1.234f

// call undo() to revert the last block of variables to their previous values
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
         
```


The library also supports setting and reverting variables one at the time without the need for calling the `start()` or `finish()` methods. 
Enable this by calling `proxy.useBlocks(false)`.  This can be re-enabled at any time by calling `proxy.useBlocks(true)`.

```

UndoablePojo proxy = PojoWrapper.wrap(UndoablePojo.class)

proxy.start()
proxy.setFieldOne("firstFieldValue1")
proxy.setFieldTwo("secondFieldValue1")
proxy.finish()

// disable block functionality
proxy.useBlocks(false)

// fields can be set without the need for start() or finish() methods
proxy.setFieldOne("firstFieldValue2")
proxy.setFieldTwo("firstFieldValue2")
proxy.undo()
proxy.undo()

proxy.fieldOne == "firstFieldValue1"
proxy.fieldTwo == "secondFieldValue1"

// block functionality re-enabled
proxy.useBlocks(true)

proxy.start()
proxy.setFieldOne("firstFieldValue3")
proxy.setFieldTwo("secondFieldValue3")
proxy.finish()

proxy.undo()

proxy.fieldOne == "firstFieldValue1"
proxy.fieldTwo == "secondFieldValue1"

```