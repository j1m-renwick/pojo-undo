package traits

import java.lang.reflect.Method

trait Undoable {

    Stack<List<Tuple2<Method, Object>>> stack = new Stack<>()
    List<Tuple2<Method, Object>> tuples
    boolean freeze
    boolean inProgress

    void undo() {
        freeze = true
        if (!stack.empty()) {
            List<Tuple2<Method, Object>> tuples = stack.pop()
            for (Tuple2<Method, Object> tuple : tuples) {
                Object arg = tuple.getSecond()
                if (arg == null) {
                    tuple.getFirst().invoke(this, [null].toArray())
                } else {
                    tuple.getFirst().invoke(this, arg)
                }
            }
        }
        freeze = false
    }

    void start() {
        freeze = true
        if (inProgress) {
            throw new IllegalStateException("block is already in progress - you must first call finish() before calling start()")
        }
        inProgress = true
        tuples = []
        freeze = false
    }

    // TODO add a check for calling finish multiple times()?
    void finish() {
        freeze = true
        inProgress = false
        stack.push(tuples)
        freeze = false
    }

}